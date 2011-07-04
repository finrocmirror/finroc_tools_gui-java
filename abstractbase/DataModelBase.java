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
package org.finroc.tools.gui.abstractbase;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;


import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.finroc.tools.gui.FinrocGUI;
import org.finroc.tools.gui.commons.EventRouter;
import org.finroc.tools.gui.util.propertyeditor.NotInPropertyEditor;
import org.rrlib.finroc_core_utils.jc.log.LogUser;
import org.rrlib.finroc_core_utils.log.LogDomain;

import org.finroc.core.FrameworkElement;

/**
 * @author max
 *
 * Base class for all classes implementing the fingui data model
 */
@SuppressWarnings("rawtypes")
public abstract class DataModelBase < R extends DataModelBase < R, ? , ? >, P extends DataModelBase < R, ? , ? >, C extends DataModelBase > extends LogUser implements Serializable, TreeNode {

    /** UID */
    private static final long serialVersionUID = -8960599158692149672L;

    /** children */
    @NotInPropertyEditor
    protected Vector<C> children = new Vector<C>();;

    /** parent */
    protected transient P parent;

    /** don't send any event until object is initialized */
    protected transient boolean initialized = false;

    /** Tree model with this treenode as root (created as needed) */
    protected transient DefaultTreeModel treeModel = null;

    /** Framework element (backend) corresponding to this Data model element */
    protected transient FrameworkElement frameworkElement = null;

    /** log domain for gui */
    public transient LogDomain logDomain = FinrocGUI.logDomain;

    public DataModelBase(P parent) {
        if (parent != null) {
            restore(parent);
        }
    }

    /**
     * Called during initialization (when all required values have been set)
     */
    @SuppressWarnings("unchecked")
    public void restore(P parent) {
        this.parent = parent;
        if (children == null) {
            children = new Vector<C>();
        }
        if (frameworkElement == null) {
            frameworkElement = createFrameworkElement();
        }
        for (C child : children) {
            child.restore(this);
        }
        if (logDomain == null) {
            logDomain = FinrocGUI.logDomain;
        }
        initialized = true;
    }

    /**
     * Creates framework element (backend) corresponding to this Data model element
     *
     * @return Created element
     */
    protected abstract FrameworkElement createFrameworkElement();

    /**
     * Called when object is deleted
     */
    public void dispose() {
        for (C child : children) {
            child.dispose();
        }
        if (frameworkElement != null) {
            if (!frameworkElement.isDeleted()) {
                frameworkElement.managedDelete();
            }
            frameworkElement = null;
        }
        children = null;
        parent = null;
        EventRouter.objectDisposed(this);
    }

    public void addDataModelListener(DataModelListener dml) {
        EventRouter.addListener(this, dml, DataModelListener.class);
    }

    public void removeDataModelListener(DataModelListener dml) {
        EventRouter.removeListener(this, dml, DataModelListener.class);
    }

    public void fireDataModelEvent(DataModelListener.Event ev, Object param) {
        if (!initialized) {
            return;
        }

        assert SwingUtilities.isEventDispatchThread();
        JmcaguiEventRouter.fireDataModelEvent(this, ev, param);

        // fire events for TreeModel that may exist
        for (DataModelBase <? , ? , ? > dmbi = this; dmbi != null; dmbi = dmbi.getParent()) {
            if (dmbi instanceof DataModelBase) {
                DefaultTreeModel tm = ((DataModelBase <? , ? , ? >)dmbi).treeModel;
                if (tm != null) {
                    if (ev == DataModelListener.Event.ChildAdded) {
                        tm.nodesWereInserted(this, new int[] {children.indexOf(param)});
                        //tm.nodeStructureChanged(this);
                    } else if (ev == DataModelListener.Event.ChildRemoved) {
                        tm.nodeStructureChanged(this);
                    } else if (ev == DataModelListener.Event.ChildrenChanged) {
                        tm.nodeStructureChanged(this);
                    } else if (ev == DataModelListener.Event.WidgetPropertiesChanged) {
                        tm.nodeStructureChanged(this);
                    }
                }
            }
        }
    }

    // Methods from TreeNode interface
    public Enumeration<C> children() {
        if (children == null) {
            return null;
        }
        return children.elements();
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public C getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public int getIndex(TreeNode node) {
        return children.indexOf(node);
    }

    public P getParent() {
        return parent;
    }

    @SuppressWarnings("unchecked")
    public R getRoot() {
        DataModelBase < R, ? , ? > dmbi = this;
        while (dmbi.getParent() != null) {
            dmbi = dmbi.getParent();
        }
        return (R)dmbi;
    }

    public boolean isLeaf() {
        return getChildCount() == 0;
    }

    public void add(C newChild) {
        children.add(newChild);
        fireDataModelEvent(DataModelListener.Event.ChildAdded, newChild);
    }

    public void remove(List<C> sel) {
        for (int i = sel.size() - 1; i >= 0; i--) {
            remove(sel.get(i));
        }
    }

    public void remove(C c) {
        children.remove(c);
        c.dispose();
        fireDataModelEvent(DataModelListener.Event.ChildRemoved, c);
    }

    @SuppressWarnings("unchecked")
    public void replaceChild(C old, C newChild) {
        children.set(children.indexOf(old), newChild);
        old.dispose();
        newChild.restore(this);
    }

    public List<C> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public TreeModel getTreeModel() {
        if (treeModel == null) {
            treeModel = new DefaultTreeModel(this);
        }
        return treeModel;
    }

    /**
     * Move child to different position in list
     *
     * @param child Child to move
     * @param newIndex New position in list
     */
    public void moveChildTo(C child, int newIndex) {
        int idx = children.indexOf(child);
        if (idx < 0 || idx == newIndex || newIndex >= children.size()) {
            return;
        }
        C c = children.remove(idx);
        children.add(newIndex, c);
    }
}
