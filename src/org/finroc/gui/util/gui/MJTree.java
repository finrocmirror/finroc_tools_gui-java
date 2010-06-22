/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2007-2010 Max Reichardt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.finroc.gui.util.gui;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.finroc.gui.commons.EventRouter;


/**
 * @author max
 *
 * More convenient JTree.
 *
 * Due to performance, simpleness reasons, only works with TreeModels
 * whose Nodes implement the TreeNode interface.
 */
public class MJTree<T extends TreeNode> extends JTree implements MouseListener {

    /** UID */
    private static final long serialVersionUID = 1166608861920603326L;

    /** Type of objects that can be selected */
    private Class <? extends T > selectableObjectType;

    /** temporary variable - for letting selection get smaller at mouse release (for drag & drop) */
    private TreePath[] newSelection;

    /** Component to route repaint requests to */
    private JComponent repaintDelegate = null;

    /** Up to which level should new model be expanded by default? */
    private int newModelExpandLevel;

    @SuppressWarnings("unchecked")
    public MJTree(Class<?> selectableObjectType, int newModelExpandLevel) {
        this.selectableObjectType = (Class <? extends T >)selectableObjectType;
        this.newModelExpandLevel = newModelExpandLevel;
        super.addMouseListener(this);
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

    public boolean hasCorrectType(Object o) {
        return selectableObjectType.isAssignableFrom(o.getClass());
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

    public TreePath getTreePathFor(TreeNode object) {
        LinkedList<TreeNode> temp = new LinkedList<TreeNode>();
        temp.add(object);
        TreeNode temp2 = object;
        while (temp2 != getModel().getRoot()) {
            temp2 = temp2.getParent();
            temp.addFirst(temp2);
        }
        return new TreePath(temp.toArray());
    }

    public void setModel(TreeModel tm) {
        if (tm.getRoot() != null && !(tm.getRoot() instanceof TreeNode)) {
            throw new RuntimeException("MJTree only works for Trees with TreeNodes as nodes");
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
            getObjectsHelper(result, (TreeNode)getModel().getRoot());
        }
        return result;
    }

    public List<T> getVisibleObjects() {
        List<T> result = getObjects();
        for (Iterator<T> i = result.iterator(); i.hasNext();) {
            if (!isVisible(getTreePathFor(i.next()))) {
                i.remove();
            }
        }
        return result;
    }


    @SuppressWarnings("unchecked")
    private void getObjectsHelper(List<T> result, TreeNode curNode) {
        if (hasCorrectType(curNode)) {
            result.add((T)curNode);
        }

        for (int i = 0; i < curNode.getChildCount(); i++) {
            getObjectsHelper(result, curNode.getChildAt(i));
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
        expandPath(new TreePath(new Object[] {getModel().getRoot()}));
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
        expandAllHelper((TreeNode)getModel().getRoot(), true, new LinkedList<TreeNode>(), expandLevel);
    }
    public void collapseAll() {
        expandAllHelper((TreeNode)getModel().getRoot(), false, new LinkedList<TreeNode>(), 20);
    }
    private void expandAllHelper(TreeNode tn, boolean expand, List<TreeNode> curPath, int levelsLeft) {
        if (levelsLeft == 0) {
            return;
        }
        curPath.add(tn);
        if (!tn.isLeaf()) {
            for (int i = 0; i < tn.getChildCount(); i++) {
                expandAllHelper(tn.getChildAt(i), expand, curPath, levelsLeft - 1);
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
            //setExpandedState(e.getTreePath(), true);
            TreePath p = e.getTreePath();
            for (int i : e.getChildIndices()) {
                setExpandedState(p.pathByAddingChild(((TreeNode)p.getLastPathComponent()).getChildAt(i)), true);
            }
        }
    }
}
