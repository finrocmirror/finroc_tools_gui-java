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
package org.finroc.tools.gui.util.gui;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.finroc.core.remote.ModelNode;
import org.finroc.tools.gui.commons.EventRouter;
import org.finroc.tools.gui.util.ElementFilter;


/**
 * @author Max Reichardt
 *
 * More convenient JTree.
 *
 * Due to reasons of performance and simplicity, only works with TreeModels
 * whose Nodes implement the TreeNode interface - or consists of ModelNodes
 */
public class MJTree<T> extends JTree implements MouseListener {

    /** UID */
    private static final long serialVersionUID = 1166608861920603326L;

    /** Filter for objects that can be selected */
    private final ElementFilter<Object> selectableObjectFilter;

    /** temporary variable - for letting selection get smaller at mouse release (for drag & drop) */
    private TreePath[] newSelection;

    /** Component to route repaint requests to */
    private JComponent repaintDelegate = null;

    /** Up to which level should new model be expanded by default? */
    private int newModelExpandLevel;

    /** Expanded elements stored by storeExpandedElements() */
    private List<TreePath> storedExpandedPaths = null;

    /** Set of formerly expanded elements that no longer exist */
    private Set<List<String>> lostStoredExpandedPaths = new HashSet<List<String>>();

    /** Expand all elements added to tree model? */
    private boolean expandNewElements;


    /**
     * @param selectableObjectFilter Filter for objects that can be selected. Must only accept elements of type T.
     * @param newModelExpandLevel Up to which level should new model be expanded by default?
     */
    public MJTree(ElementFilter<Object> selectableObjectFilter, int newModelExpandLevel) {
        this.selectableObjectFilter = selectableObjectFilter;
        this.newModelExpandLevel = newModelExpandLevel;
        super.addMouseListener(this);
        super.setLargeModel(true);
    }


    @SuppressWarnings("unchecked")
    public List<T> getSelectedObjects() {
        List<T> result = new ArrayList<T>();
        TreePath[] tps = getSelectionPaths();
        if (tps != null) {
            for (TreePath tp : tps) {
                if (hasCorrectType(tp.getLastPathComponent())) {
                    result.add((T)tp.getLastPathComponent());
                }
            }
        }
        return result;
    }

    private boolean hasCorrectType(Object o) {
        return selectableObjectFilter.acceptElement(o);
    }

    public void setSelectedObjects(List<T> objects, boolean overwriteMouseRelease) {
        if (objects == null) {
            setSelectionPaths(null);
            newSelection = overwriteMouseRelease ? null : newSelection;
            return;
        }
        TreePath[] temp = new TreePath[objects.size()];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = getTreePathFor(objects.get(i));
        }
        setSelectionPaths(temp);
        newSelection = overwriteMouseRelease ? temp : newSelection;
    }

    public TreePath getTreePathFor(Object object) {
        LinkedList<Object> temp = new LinkedList<Object>();
        temp.add(object);
        Object temp2 = object;
        while (temp2 != getModel().getRoot()) {
            if (temp2 == null) {
                return null;
            }
            temp2 = getParent(temp2);
            temp.addFirst(temp2);
        }
        return new TreePath(temp.toArray());
    }

    private Object getParent(Object node) {
        if (node instanceof TreeNode) {
            return ((TreeNode)node).getParent();
        } else if (node instanceof ModelNode) {
            return ((ModelNode)node).getParent();
        }
        return null;
    }


    public void setModel(TreeModel tm) {
        if (tm.getRoot() != null && !((tm.getRoot() instanceof TreeNode) || (tm.getRoot() instanceof ModelNode))) {
            throw new RuntimeException("MJTree only works for Trees with TreeNodes or ModelNodes as nodes");
        }
        if (getModel() == tm) {
            return;
        }
        super.setModel(tm);
        collapseAll();
        expandAll(newModelExpandLevel);  // expand model by default
        repaint();
    }

    public List<T> getObjects() {
        List<T> result = new ArrayList<T>();
        if (getModel().getRoot() != null) {
            getObjectsHelper(result, getModel().getRoot());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<T> getVisibleObjects() {
        ArrayList<T> result = new ArrayList<T>();
        final int rowCount = this.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Object component = this.getPathForRow(i).getLastPathComponent();
            if (hasCorrectType(component)) {
                result.add((T)component);
            }
        }
        return result;
    }

    public List<TreePath> getExpandedPaths() {
        ArrayList<TreePath> result = new ArrayList<TreePath>();
        Enumeration<TreePath> en = this.getExpandedDescendants(new TreePath(this.getModel().getRoot()));
        while (en != null && en.hasMoreElements()) {
            result.add(en.nextElement());
        }
        return result;
    }

    /**
     * @param expandNewElements Expand all elements added to tree model?
     */
    public void setExpandNewElements(boolean expandNewElements) {
        this.expandNewElements = expandNewElements;
    }

    @SuppressWarnings("unchecked")
    private void getObjectsHelper(List<T> result, Object curNode) {
        if (hasCorrectType(curNode)) {
            result.add((T)curNode);
        }

        for (int i = 0; i < getModel().getChildCount(curNode); i++) {
            getObjectsHelper(result, getModel().getChild(curNode, i));
        }
    }

    public Rectangle getObjectBounds(T object, boolean visiblePart) {
        Rectangle r = getPathBounds(getTreePathFor(object));
        if (visiblePart) {
            return r.intersection(getVisibleRect());
        }
        return r;
    }

    public void addMouseListener(MouseListener ml) {
        if (ml.getClass().getPackage().getName().startsWith("javax.")) {
            super.addMouseListener(ml);
        } else {
            EventRouter.addListener(this, ml, MouseListener.class);
        }
    }

    public void removeMouseListener(MouseListener ml) {
        if (ml.getClass().getPackage().getName().startsWith("javax.")) {
            super.removeMouseListener(ml);
        } else {
            EventRouter.removeListener(this, ml, MouseListener.class);
        }
    }

    public void mouseClicked(MouseEvent e) {
        EventRouter.fireMouseClickedEvent(this, e);
    }

    public void mouseEntered(MouseEvent e) {
        EventRouter.fireMouseEnteredEvent(this, e);
    }

    public void mouseExited(MouseEvent e) {
        EventRouter.fireMouseExitedEvent(this, e);
    }

    public void mousePressed(MouseEvent e) {
        TreePath[] temp = getSelectionPaths();

        // Is complete new selection contained in old selection? If yes, make selection smaller at mouse release
        boolean keep = true;
        if (temp != null && newSelection != null) {
            for (TreePath tp1 : temp) {
                boolean inSelection = false;
                for (TreePath tp2 : newSelection) {
                    if (tp1.getLastPathComponent() == (tp2.getLastPathComponent())) {
                        inSelection = true;
                        break;
                    }
                }
                keep &= inSelection;
            }
        } else {
            keep = false;
        }

        if (keep) {
            setSelectionPaths(newSelection);
            newSelection = temp;
        } else {
            setSelectedObjects(getSelectedObjects(), false);
            newSelection = getSelectionPaths();
        }

        EventRouter.fireMousePressedEvent(this, e);
    }

    public void mouseReleased(MouseEvent e) {
        EventRouter.fireMouseReleasedEvent(this, e);
        setSelectionPaths(newSelection);
    }

    public boolean isVisible(T object) {
        return isVisible(getTreePathFor(object));
    }

    public void expandRoot() {
        TreeModel tm = getModel();
        expandPath(new TreePath(new Object[] {tm.getRoot()}));
    }

    public void setRepaintDelegate(JComponent jc) {
        repaintDelegate = jc;
    }

    @Override
    public void repaint(long tm, int x, int y, int width, int height) {
        if (repaintDelegate != null) {
            repaintDelegate.repaint();
        } else {
            super.repaint(tm, x, y, width, height);
        }
    }

    @Override
    public void repaint(Rectangle r) {
        if (repaintDelegate != null) {
            repaintDelegate.repaint();
        } else {
            super.repaint(r);
        }
    }

    @Override
    public void repaint() {
        if (repaintDelegate != null) {
            repaintDelegate.repaint();
        } else {
            super.repaint();
        }
    }

    @Override
    public void repaint(int x, int y, int width, int height) {
        if (repaintDelegate != null) {
            repaintDelegate.repaint();
        } else {
            super.repaint(x, y, width, height);
        }
    }

    @Override
    public void repaint(long tm) {
        if (repaintDelegate != null) {
            repaintDelegate.repaint(tm);
        } else {
            super.repaint(tm);
        }
    }

    public void expandAll(int expandLevel) {
        expandAllHelper(getModel().getRoot(), true, new LinkedList<Object>(), expandLevel);
    }
    public void collapseAll() {
        expandAllHelper(getModel().getRoot(), false, new LinkedList<Object>(), 20);
    }
    private void expandAllHelper(Object tn, boolean expand, List<Object> curPath, int levelsLeft) {
        if (levelsLeft == 0) {
            return;
        }
        curPath.add(tn);
        if (!getModel().isLeaf(tn)) {
            for (int i = 0; i < getModel().getChildCount(tn); i++) {
                expandAllHelper(getModel().getChild(tn, i), expand, curPath, levelsLeft - 1);
            }
            if (expand || curPath.size() > 1) {
                setExpandedState(new TreePath(curPath.toArray()), expand);
            }
        }
        curPath.remove(tn);
        repaint();
    }

    @Override
    protected TreeModelListener createTreeModelListener() {
        return new MTreeModelHandler();
    }

    protected class MTreeModelHandler extends TreeModelHandler {

        @Override
        public void treeNodesInserted(TreeModelEvent e) {
            if (expandNewElements) {
                //setExpandedState(e.getTreePath(), true);
                TreePath p = e.getTreePath();
                for (int i : e.getChildIndices()) {
                    setExpandedState(p.pathByAddingChild(getModel().getChild(p.getLastPathComponent(), i)), true);
                }
            }
        }

        @Override
        public void treeStructureChanged(TreeModelEvent e) {
            storeExpandedElements();
            super.treeStructureChanged(e);
            restoreExpandedElements();
        }
    }

    /**
     * Expand element and all parent nodes
     *
     * @param tn Element to expand
     */
    public void expandToElement(Object tn) {
        TreePath tp = getTreePathFor(tn);
        while (tp != null && tp.getPath().length > 0) {
            expandPath(tp);
            tp = tp.getParentPath();
        }
    }

    /**
     * Stores currently expanded elements
     * (expansion state can be restored by calling restoreExpandedElements() )
     */
    public void storeExpandedElements() {
        storedExpandedPaths = getExpandedPaths();
    }

    /**
     * Restore previously stored expanded elements
     * (Ignores missing elements)
     * (Clears stored element list)
     */
    public void restoreExpandedElements() {
        for (TreePath t : storedExpandedPaths) {
            if (isInModel(t.getLastPathComponent())) {
                try {
                    expandPath(t);
                } catch (Exception e) {
                }
            } else if (t.getLastPathComponent() instanceof ModelNode || t.getLastPathComponent() instanceof TreeNode) {
                // Element no longer exists - store
                List<String> lostPath = new ArrayList<String>();
                for (Object element : t.getPath()) {
                    lostPath.add(element.toString());
                }
                lostStoredExpandedPaths.add(lostPath);
            }
        }

        ArrayList<List<String>> lostPathsToDelete = new ArrayList<List<String>>();
        for (List<String> lostPath : lostStoredExpandedPaths) {
            TreePath foundPath = findPath(lostPath.toArray());
            if (foundPath != null) {
                try {
                    expandPath(foundPath);
                    lostPathsToDelete.add(lostPath);
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        }
        lostStoredExpandedPaths.removeAll(lostPathsToDelete);

        storedExpandedPaths.clear();
    }

    /**
     * Is this element part of the current tree model?
     *
     * @param element
     * @return Answer
     */
    private boolean isInModel(Object element) {
        if (element == getModel().getRoot()) {
            return true;
        }
        if (element instanceof ModelNode) {
            return ((ModelNode)element).isNodeAncestor((ModelNode)getModel().getRoot());
        }
        if (element instanceof TreeNode) {
            while (true) {
                element = ((TreeNode)element).getParent();
                if (element == getModel().getRoot()) {
                    return true;
                }
                if (element == null || (!(element instanceof TreeNode))) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Try to find path in current model that matches (toString() comparison) the provided lost path
     *
     * @param lostPathElements Lost path elements
     * @return TreePath in current model if one was found; null otherwise
     */
    public TreePath findPath(Object[] lostPathElements) {
        List<Object> treePath = new ArrayList<Object>();
        Object currentNode = this.getModel().getRoot();
        treePath.add(currentNode);
        for (int i = 1; i < lostPathElements.length; i++) {
            boolean found = false;
            for (int j = 0; j < this.getModel().getChildCount(currentNode); j++) {
                Object child = this.getModel().getChild(currentNode, j);
                if (lostPathElements[i].toString().equals(child.toString())) {
                    currentNode = child;
                    treePath.add(currentNode);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return null;
            }
        }
        return new TreePath(treePath.toArray());
    }

}
