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

import org.finroc.tools.gui.abstractbase.DataModelBase;

import org.finroc.core.FrameworkElement;

/**
 * @author Max Reichardt
 *
 * This is the main GUI window
 */
public class GUIWindow extends DataModelBase<GUI, GUI, GUIPanel> {

    /** UID */
    private static final long serialVersionUID = -3741704628937934143L;

    public GUIWindow(GUI parent) {
        super(parent);
    }

    @Override
    protected FrameworkElement createFrameworkElement() {
        return new FrameworkElement(getParent().getFrameworkElement(), "GUI Window");
    }

    @Override
    public void restore(GUI parent) {
        super.restore(parent);
        frameworkElement.init();
    }
}
