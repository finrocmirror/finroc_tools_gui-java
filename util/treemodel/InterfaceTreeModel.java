//
// You received this file as part of Finroc
// A framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui.util.treemodel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.finroc.core.FrameworkElement;
import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.FrameworkElementTags;
import org.finroc.core.RuntimeEnvironment;
import org.finroc.core.plugin.ExternalConnection;
import org.finroc.core.remote.ModelHandler;
import org.finroc.core.remote.ModelNode;
import org.finroc.core.remote.RemoteFrameworkElement;
import org.finroc.core.remote.RemoteRuntime;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

/**
 * @author Max Reichardt
 *
 * Tree Model for interfaces
 */
public class InterfaceTreeModel implements TreeModel {

    /** Root node for all interfaces */
    private final ModelNode root = new ModelNode("Interfaces");

    /** Framework element that is parent of all connections */
    private final FrameworkElement externalConnectionParent = new FrameworkElement(RuntimeEnvironment.getInstance(), "Interfaces");

    /** Tree model listener list */
    private final ArrayList<TreeModelListener> listener = new ArrayList<TreeModelListener>();

//    /** List with active connections */
//    private final ArrayList<ExternalConnection> activeConnection = new ArrayList<ExternalConnection>();

    /** Tag that indicates that an element should be shown initially */
    private static final String INITIAL_SHOW_TAG = "initially show in tools:";

    /** Tag that indicates that this is a remote runtime */
    private static final String REMOTE_RUNTIME_TAG = "remote_runtime:";

    /** Current list with elements to show initially */
    private final ArrayList<ElementToShowInitially> elementsToShowInitially = new ArrayList<ElementToShowInitially>();

    /** Show hidden elements in finstruct (not necessary and confusing for application developers; Finroc developers can be interested) */
    public final boolean SHOW_HIDDEN_ELEMENTS = false;

    public InterfaceTreeModel() {
        externalConnectionParent.init();
    }

    /**
     * @return List with all the current connections
     */
    public List<ExternalConnection> getActiveInterfaces() {
        List<ExternalConnection> list = new ArrayList<ExternalConnection>();
        FrameworkElement.ChildIterator childIterator = new FrameworkElement.ChildIterator(externalConnectionParent);
        FrameworkElement next = null;
        while ((next = childIterator.next()) != null) {
            list.add((ExternalConnection)next);
        }
        return list;
    }

    /**
     * Returns a child with the specified qualified name
     *
     * @param qualifiedName Qualified name (Names of elements separated with separator char)
     * @param separator Separator
     * @return Child with the specified qualified name. Null if no such child exists.
     */
    public ModelNode getChildByQualifiedName(String qualifiedName, char separator) {
        String rootString = root.toString();
        if (qualifiedName.startsWith(rootString)) {
            if (qualifiedName.length() == rootString.length()) {
                return root;
            }
            if (qualifiedName.charAt(rootString.length()) == separator) {
                return root.getChildByQualifiedName(qualifiedName, rootString.length() + 1, separator);
            }
        }
        return null;
    }

    public ModelNode getRoot() {
        return root;
    }

    /**
     * @return Framework element that is parent of all connections
     */
    public FrameworkElement getRootFrameworkElement() {
        return externalConnectionParent;
    }

    /**
     * @return New model handler (typically for new connection)
     */
    public ModelHandler getNewModelHandlerInstance() {
        return new SingleInterfaceHandler();
    }

    /**
     * @return Element to show initially, if any such elements has appeared
     */
    public RemoteFrameworkElement getElementToShowInitially() {
        RemoteFrameworkElement result = null;
        int resultPriority = Integer.MIN_VALUE;
        for (ElementToShowInitially entry : elementsToShowInitially) {
            RemoteFrameworkElement element = entry.element.get();
            if (element != null && element.isNodeAncestor(root)) {
                if (entry.priority > resultPriority) {
                    result = element;
                    resultPriority = entry.priority;
                }
            }
        }
        return result;
    }

    /**
     * (may only be called by Java AWT Thread)
     * @return Returns list with elements marked as to be shown initially that were added since the last call to this function.
     */
    public List<RemoteFrameworkElement> getAndClearElementsToShowInitially() {
        ArrayList<RemoteFrameworkElement> result = new ArrayList<RemoteFrameworkElement>();
        for (ElementToShowInitially toShow : elementsToShowInitially) {
            RemoteFrameworkElement element = toShow.element.get();
            if (element != null) {
                result.add(element);
            }
        }
        elementsToShowInitially.clear();
        return result;
    }

    /** Helper enum for ModelHandler implementation below: Opcodes */
    private enum Operation { ADD, CHANGE, REMOVE, REPLACE, SETMODEL }

    /** Helper enum for ModelHandler implementation below: Remote framework element classes in sorting order */
    private enum ElementClass { SENSOR_INTERFACE, CONTROLLER_INTERFACE, INTERFACE, PARAMETER_INTERFACE, NONPORT, PORT, HIDDEN }

    /**
     * Contains information on an element to show initially
     */
    private class ElementToShowInitially {

        /** Reference to element */
        WeakReference<RemoteFrameworkElement> element;

        /** Timestamp when it was discovered */
        long discovered;

        /** Priority that was set for initial viewing */
        int priority;
    }

    /**
     * Model handler for single external connection
     */
    class SingleInterfaceHandler implements ModelHandler, Comparator<ModelNode> {

        /** Root node for this interface */
        ModelNode root;

        @Override
        public void addNode(ModelNode parent, ModelNode newChild) {
            callOperation(Operation.ADD, parent, newChild, null);
        }

        @Override
        public void changeNodeName(ModelNode node, String newName) {
            callOperation(Operation.CHANGE, node, null, newName);
        }

        @Override
        public void removeNode(ModelNode childToRemove) {
            assert(childToRemove != null);
            callOperation(Operation.REMOVE, childToRemove, null, null);
        }

        @Override
        public void replaceNode(ModelNode oldNode, ModelNode newNode) {
            assert(oldNode != null && newNode != null);
            callOperation(Operation.REPLACE, oldNode, newNode, null);
        }

        @Override
        public void setModelRoot(final ModelNode newRoot) {
            callOperation(Operation.SETMODEL, newRoot, null, null);
        }

        @Override
        public void updateModel(Runnable updateTask) {
            if (SwingUtilities.isEventDispatchThread()) {
                updateTask.run();
            } else {
                SwingUtilities.invokeLater(updateTask);
            }
        }

        /**
         * We forward all operations to this method to avoid having many inner classes.
         *
         * @param operation Operation to perform
         * @param node1 First node
         * @param node2 Second node (optional)
         * @param name Name (optional)
         */
        private void callOperation(final Operation operation, final ModelNode node1, final ModelNode node2, final String name) {
            if (SwingUtilities.isEventDispatchThread()) {
                callOperationImplementation(operation, node1, node2, name);
            }
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    callOperationImplementation(operation, node1, node2, name);
                }
            });
        }

        /**
         * Implementation of all operations.
         *
         * @param operation Operation to perform
         * @param node1 First node
         * @param node2 Second node (optional)
         * @param name Name (optional)
         */
        private void callOperationImplementation(Operation operation, ModelNode node1, ModelNode node2, String name) {
            switch (operation) {
            case ADD:
                ModelNode parent = node1;
                ModelNode newChild = node2;
                markHiddenElements(newChild);
                sortNewTreeNode(newChild);
                checkForElementsToShow(newChild);

                if (newChild.getParent() == parent) {
                    return;
                }

                // use alphabetic sorting for framework elements (with interfaces at the front)
                if (parent instanceof RemoteFrameworkElement) {
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        ModelNode child = parent.getChildAt(i);
                        if (compare(newChild, child) < 0) {

                            //InterfaceTreeModel.this.insertNodeInto(newChild, parent, i);
                            parent.insertChild(i, newChild);
                            TreeModelEvent event = new TreeModelEvent(InterfaceTreeModel.this, getTreePath(parent), new int[] {i}, new Object[] {newChild});
                            for (int j = listener.size() - 1; j >= 0; j--) {
                                listener.get(j).treeNodesInserted(event);
                            }
                            return;
                        }
                    }
                }


                int index = parent.getChildCount();

                //InterfaceTreeModel.this.insertNodeInto(newChild, parent, index);
                parent.add(newChild);
                TreeModelEvent event = new TreeModelEvent(InterfaceTreeModel.this, getTreePath(parent), new int[] {index}, new Object[] {newChild});
                for (int j = listener.size() - 1; j >= 0; j--) {
                    listener.get(j).treeNodesInserted(event);
                }
                break;
            case CHANGE:
                node1.setName(name);

                //InterfaceTreeModel.this.nodeChanged(node1);
                event = new TreeModelEvent(InterfaceTreeModel.this, getTreePath(node1.getParent()), new int[] {node1.getParent().indexOf(node1)}, new Object[] {node1});
                for (int j = listener.size() - 1; j >= 0; j--) {
                    listener.get(j).treeNodesChanged(event);
                }
                break;
            case REMOVE:
                parent = node1.getParent();
                if (parent != null) {
                    parent.remove(node1);
                    /*index = parent.getIndex(node1);
                    parent.remove(index);*/
                    //modelHandler.getTreeModel().nodesWereRemoved(this, new int[]{index}, new TreeNode[]{child});   // doesn't work :-( => occasional NullPointerExceptions

                    //InterfaceTreeModel.this.nodeStructureChanged(parent);
                    event = new TreeModelEvent(InterfaceTreeModel.this, getTreePath(parent));
                    for (int j = listener.size() - 1; j >= 0; j--) {
                        if (j < listener.size()) {
                            listener.get(j).treeStructureChanged(event);
                        }
                    }
                }
                break;
            case REPLACE:
                parent = node1.getParent();
                if (parent != null) {
                    markHiddenElements(node2);
                    sortNewTreeNode(node2);
                    checkForElementsToShow(node2);
                    parent.replace(node1, node2);
                    /*index = parent.getIndex(node1);
                    assert(index >= 0);
                    parent.remove(node1);
                    parent.insert(node2, index);*/

                    //InterfaceTreeModel.this.nodeStructureChanged(parent);
                    event = new TreeModelEvent(InterfaceTreeModel.this, getTreePath(parent));
                    for (int j = listener.size() - 1; j >= 0; j--) {
                        listener.get(j).treeStructureChanged(event);
                    }
                }
                break;
            case SETMODEL:
                if (this.root != null) {
                    callOperationImplementation(Operation.REPLACE, this.root, node1, null);
                } else {
                    callOperationImplementation(Operation.ADD, InterfaceTreeModel.this.root, node1, null);
                }
                this.root = node1;
                break;
            }
        }

        /**
         * @param node Node
         * @return Node class of specified node
         */
        private ElementClass getNodeClass(ModelNode node) {
            if (node instanceof RemoteFrameworkElement) {
                if (node.isHidden(false)) {
                    return ElementClass.HIDDEN;
                }
                RemoteFrameworkElement element = (RemoteFrameworkElement)node;
                if (element.getFlag(FrameworkElementFlags.PORT)) {
                    return ElementClass.PORT;
                }
                if (node.isInterface()) {
                    return element.isSensorInterface() ? ElementClass.SENSOR_INTERFACE : (element.isControllerInterface() ? ElementClass.CONTROLLER_INTERFACE : (element.getName().startsWith("Parameter") ? ElementClass.PARAMETER_INTERFACE : ElementClass.INTERFACE));
                }
                return ElementClass.NONPORT;
            }
            return ElementClass.NONPORT;
        }

        @Override
        public int compare(ModelNode node1, ModelNode node2) {
            ElementClass class1 = getNodeClass(node1);
            ElementClass class2 = getNodeClass(node2);
            return class1.ordinal() < class2.ordinal() ? -1 : (class1.ordinal() > class2.ordinal() ? 1 : (node1.toString().compareToIgnoreCase(node2.toString())));
        }

        /**
         * Checks all elements for hidden flag
         */
        private void markHiddenElements(ModelNode node) {
            if (SHOW_HIDDEN_ELEMENTS) {
                return;
            }
            if (node instanceof RemoteFrameworkElement && ((RemoteFrameworkElement)node).isTagged(FrameworkElementTags.HIDDEN_IN_TOOLS)) {
                node.setHidden(true);
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                markHiddenElements((ModelNode)node.getChildAt(i));
            }
        }

        /**
         * Sorts elements of specified node and all of its subnodes recursively.
         */
        public void sortNewTreeNode(ModelNode node) {
            if (node instanceof RemoteFrameworkElement) {
                ((RemoteFrameworkElement)node).sortChildren(this);
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                sortNewTreeNode((ModelNode)node.getChildAt(i));
            }
        }

        /**
         * Check element and all of its subelements for elements to initially show
         * and also for remote runtimes to show
         */
        private void checkForElementsToShow(ModelNode node) {
            if (node instanceof RemoteFrameworkElement) {
                List<String> tags = ((RemoteFrameworkElement)node).getTags();
                if (tags != null) {
                    for (String tag : tags) {
                        if (tag.startsWith(INITIAL_SHOW_TAG)) {
                            ElementToShowInitially toShow = new ElementToShowInitially();
                            toShow.element = new WeakReference<RemoteFrameworkElement>((RemoteFrameworkElement)node);
                            toShow.discovered = System.currentTimeMillis();
                            toShow.priority = Integer.parseInt(tag.substring(INITIAL_SHOW_TAG.length()));
                            elementsToShowInitially.add(toShow);
                            break;
                        }
                        if (tag.startsWith(REMOTE_RUNTIME_TAG)) {
                            RemoteRuntime parentRuntime = RemoteRuntime.find(node);
                            new RemoteRemoteRuntime(node.getName(), node.getName(), parentRuntime.getAdminInterface(), tag.substring(REMOTE_RUNTIME_TAG.length()).trim(), node, InterfaceTreeModel.this, this);
                        }
                    }
                }
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                checkForElementsToShow((ModelNode)node.getChildAt(i));
            }
        }
    }

    /**
     * @param node Node to get tree path of
     * @return Treepath for this node - as required by SWING
     */
    public TreePath getTreePath(ModelNode node) {
        int elements = 1;
        ModelNode current = node;
        while (current.getParent() != null) {
            elements++;
            current = current.getParent();
        }

        Object[] result = new Object[elements];
        current = node;
        int index = elements - 1;
        while (current != null) {
            result[index] = current;
            index--;
            current = current.getParent();
        }

        return new TreePath(result);
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((ModelNode)parent).getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof RemoteRuntime) {
            // only return number of non-hidden elements
            for (int i = 0; i < ((ModelNode)parent).getChildCount(); i++) {
                ModelNode child = ((ModelNode)parent).getChildAt(i);
                if (child.isHidden(false)) {
                    return i;
                }
            }
        }
        return ((ModelNode)parent).getChildCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((ModelNode)node).getChildCount() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        Log.log(LogLevel.ERROR, this, "Changing values is not supported");
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return ((ModelNode)parent).indexOf((ModelNode)child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        if (!listener.contains(l)) {
            listener.add(l);
        }
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listener.remove(l);
    }

//    public InterfaceTreeModel() {
//        super(new InterfaceNode(new FrameworkElement(RuntimeEnvironment.getInstance(), "Interfaces").getLink(0)));
//        root = (InterfaceNode)this.getRoot();
//        RuntimeEnvironment.getInstance().addListener(this);
//        elements.put(-getRootFrameworkElement().getHandle(), root);
//        root.wrapped.getChild().init();
//    }
//
//
//    public InterfaceNode getInterfaceNode(FrameworkElement element) {
//        return getInterfaceNode(element, 0);
//    }
//
//    /**
//     * Get Node for element. If it doesn't exist yet - create it
//     *
//     * @param element Framework Element to get node for
//     * @return Node
//     */
//    public InterfaceNode getInterfaceNode(FrameworkElement element, int count) {
//        if (element.isDeleted()) {
//            return null;
//        }
//
//        assert(element.isInitialized());
//        InterfaceNode node = element.isPort() ? ports.get(element.getHandle()) : elements.get(-element.getHandle());
//        if (node != null) {
//            return node;
//        }
//
//        InterfaceNode primary = null;
//        for (int i = 0; i < element.getLinkCount(); i++) {
//            FrameworkElement.Link l = element.getLink(i);
//            if (element.isPort()) {
//                InterfaceNodePort in = new InterfaceNodePort(l);
//                InterfaceNode parent = getInterfaceNode(l.getParent(), count + 1);
//                parent.add(in);
//                if (i == 0) {
//                    primary = in;
//                    ports.put(element.getHandle(), in);
//                } else {
//                    in.next = primary.next;
//                    primary.next = in;
//                }
//                //in.next = ports.get(element.getHandle());
//                //ports.put(element.getHandle(), in);
//            } else {
//                InterfaceNode in = new InterfaceNode(l);
//                InterfaceNode parent = getInterfaceNode(l.getParent(), count + 1);
//                parent.add(in);
//                if (i == 0) {
//                    primary = in;
//                    elements.put(-element.getHandle(), in);
//                } else {
//                    in.next = primary.next;
//                    primary.next = in;
//                }
//                //in.next = elements.get(-element.getHandle());
//                //elements.put(-element.getHandle(), in);
//            }
//        }
//
//        return getInterfaceNode(element, count + 1);
//    }
//
//    @Override
//    public void runtimeChange(final byte changeType, final FrameworkElement element) {
//        if (!element.isChildOf(root.wrapped.getChild(), true)) {
//            return; // not of interest
//        }
//
//        SwingUtilities.invokeLater(new Runnable() {
//            public void run() {
//                synchronized (RuntimeEnvironment.getInstance().getRegistryLock()) {
//                    if (changeType == ADD && element.isReady()) {
//                        final InterfaceNode in = getInterfaceNode(element);
//                        InterfaceTreeModel.this.nodeStructureChanged(in.getParent());
//                    } else if (changeType == REMOVE) {
//                        if (element.isPort()) {
//                            InterfaceNode in = ports.get(element.getHandle());
//                            ports.remove(element.getHandle());
//                            while (in != null) { // remove all links
//                                final InterfaceNode parent = (InterfaceNode)in.getParent();
//                                final int idx = parent.getIndex(in);
//                                final InterfaceNode inCopy = in;
//                                parent.remove(in);
//                                InterfaceTreeModel.this.nodesWereRemoved(parent, new int[] {idx}, new Object[] {inCopy});
//                                in = in.next;
//                            }
//                        } else {
//                            InterfaceNode in = elements.get(-element.getHandle());;
//                            elements.remove(-element.getHandle());
//                            while (in != null) { // remove all links
//                                final InterfaceNode parent = (InterfaceNode)in.getParent();
//                                final int idx = parent.getIndex(in);
//                                final InterfaceNode inCopy = in;
//                                parent.remove(in);
//                                InterfaceTreeModel.this.nodesWereRemoved(parent, new int[] {idx}, new Object[] {inCopy});
//                                in = in.next;
//                            }
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    public void runtimeEdgeChange(byte changeType, AbstractPort source, AbstractPort target) {
//        // do nothing at the moment
//    }
//
//    public PortWrapper getInputPort(String uid) {
//        PortWrapper in = getPort(uid);
//        return (in != null && in.getPort().getFlag(FrameworkElementFlags.EMITS_DATA)) ? in : null;
//    }
//
//    public PortWrapper getOutputPort(String uid) {
//        PortWrapper in = getPort(uid);
//        return (in != null && in.getPort().getFlag(FrameworkElementFlags.ACCEPTS_DATA)) ? in : null;
//    }
//
//    public PortWrapper getPort(String uid) {
//        StringBuilder tmp = new StringBuilder();
//        FrameworkElement el = RuntimeEnvironment.getInstance().getPort(uid);
//        if (el != null && el.isPort()) {
//            InterfaceNodePort inp = ports.get(el.getHandle());
//            while (inp != null) {
//                inp.getPort().getQualifiedLink(tmp, inp.wrapped);
//                if (tmp.toString().equals(uid)) {
//                    return inp;
//                }
//                inp = (InterfaceNodePort)inp.next;
//            }
//            FinrocGUI.logDomain.log(LogLevel.LL_WARNING, "InterfaceTreeModel", "Strange... could not find " + uid + " - although runtime returned something for it");
//        }
//        return null;
//    }
//
//    public int childEntryCount() {
//        return root.getChildCount();
//    }
//
}
