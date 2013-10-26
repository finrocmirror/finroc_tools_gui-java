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
package org.finroc.tools.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;

import org.finroc.core.FrameworkElement;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


/**
 * @author Max Reichardt
 *
 */
public class GUIPanel extends DataModelBase<GUI, GUIWindow, Widget> {

    /** UID */
    private static final long serialVersionUID = -4533212678935459906L;

    @XStreamAsAttribute
    private String name = "   ";

    public GUIPanel(GUIWindow parent) {
        super(parent);
    }

    /** Selection */
    private transient Set<Widget> selection;

    public void setParent(GUIWindow parent) {
        this.parent = parent;
    }

    public String toString() {
        return name;
    }

    /**
     * @return Selection object
     */
    public Set<Widget> getSelection() {
        return Collections.unmodifiableSet(selection);
    }

    /**
     * called after deserialization. Restores transient attributes.
     */
    public void restore(GUIWindow parent) {
        super.restore(parent);
        frameworkElement.init();
        selection = new HashSet<Widget>();
    }

    public void remove(Widget widget) {
        super.remove(widget);
        if (selection.contains(widget)) {
            selection.remove(widget);
            fireDataModelEvent(DataModelListener.Event.SelectionChanged, selection);
        }
    }

    public void selectAll() {
        selection.clear();
        selection.addAll(children);
        fireDataModelEvent(DataModelListener.Event.SelectionChanged, selection);
    }

    public void setSelection(Collection<Widget> selection2) {
        selection.clear();
        if (selection2 != null) {
            selection.addAll(selection2);
        }
        fireDataModelEvent(DataModelListener.Event.SelectionChanged, selection);
    }

    public void setName(String name2) {
        name = name2;
    }

    @Override
    protected FrameworkElement createFrameworkElement() {
        return new FrameworkElement(getParent().getFrameworkElement(), "GUI Panel" + ((name != null) ? (" " + name) : ""));
    }

}
