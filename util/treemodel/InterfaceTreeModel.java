//
// You received this file as part of Finroc
// A Framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui.util.treemodel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.finroc.core.FrameworkElement;
import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.RuntimeEnvironment;
import org.finroc.core.plugin.ExternalConnection;
import org.finroc.core.remote.ModelHandler;
import org.finroc.core.remote.ModelNode;
import org.finroc.core.remote.RemoteFrameworkElement;

/**
 * @author Max Reichardt
 *
 * Tree Model for interfaces
 */
public class InterfaceTreeModel extends DefaultTreeModel {

    /** UID */
    private static final long serialVersionUID = -5379710469604864477L;

    /** Root node for all interfaces */
    private final ModelNode root;

    /** Framework element that is parent of all connections */
    private final FrameworkElement externalConnectionParent = new FrameworkElement(RuntimeEnvironment.getInstance(), "Interfaces");

//    /** List with active connections */
//    private final ArrayList<ExternalConnection> activeConnection = new ArrayList<ExternalConnection>();

    public InterfaceTreeModel() {
        super(new ModelNode("Interfaces"));
        root = (ModelNode)super.getRoot();
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
        String[] singleNames = qualifiedName.split("" + separator);
        if (singleNames[0].equals(root.toString())) {
            ModelNode currentNode = root;
            for (int i = 1; i < singleNames.length; i++) {
                currentNode = currentNode.getChildByName(singleNames[i]);
                if (currentNode == null) {
                    return null;
                }
            }
            return currentNode;
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

    /** Helper enum for ModelHandler implementation below: Opcodes */
    private enum Operation { ADD, CHANGE, REMOVE, REPLACE, SETMODEL }

    /** Helper enum for ModelHandler implementation below: Remote framework element classes in sorting order */
    private enum ElementClass { INTERFACE, NONPORT, PORT }

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
                sortNewTreeNode(newChild);

                if (newChild.getParent() == parent) {
                    return;
                }

                // use alphabetic sorting for framework elements (with interfaces at the front)
                int index = parent.getChildCount();

                if (parent instanceof RemoteFrameworkElement) {
                    for (int i = 0; i < parent.getChildCount(); i++) {
                        TreeNode child = parent.getChildAt(i);
                        if (compare(newChild, (ModelNode)child) < 0) {
                            InterfaceTreeModel.this.insertNodeInto(newChild, parent, i);
                            return;
                        }
                    }
                }

                InterfaceTreeModel.this.insertNodeInto(newChild, parent, index);
                break;
            case CHANGE:
                node1.setUserObject(name);
                InterfaceTreeModel.this.nodeChanged(node1);
                break;
            case REMOVE:
                parent = (ModelNode)node1.getParent();
                if (parent != null) {
                    index = parent.getIndex(node1);
                    parent.remove(index);
                    //modelHandler.getTreeModel().nodesWereRemoved(this, new int[]{index}, new TreeNode[]{child});   // doesn't work :-( => occasional NullPointerExceptions
                    InterfaceTreeModel.this.nodeStructureChanged(parent);
                }
                break;
            case REPLACE:
                parent = (ModelNode)node1.getParent();
                if (parent != null) {
                    sortNewTreeNode(node2);
                    index = parent.getIndex(node1);
                    assert(index >= 0);
                    parent.remove(node1);
                    parent.insert(node2, index);
                    InterfaceTreeModel.this.nodeStructureChanged(parent);
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
                int flags = ((RemoteFrameworkElement)node).getFlags();
                if ((flags & FrameworkElementFlags.PORT) != 0) {
                    return ElementClass.PORT;
                }
                return ((flags & FrameworkElementFlags.INTERFACE) != 0) ? ElementClass.INTERFACE : ElementClass.NONPORT;
            }
            return ElementClass.NONPORT;
        }

        @Override
        public int compare(ModelNode node1, ModelNode node2) {
            ElementClass class1 = getNodeClass(node1);
            ElementClass class2 = getNodeClass(node2);
            return class1.ordinal() < class2.ordinal() ? -1 : (class1.ordinal() > class2.ordinal() ? 1 : (node1.toString().compareTo(node2.toString())));
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
