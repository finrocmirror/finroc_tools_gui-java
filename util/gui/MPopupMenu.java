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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * @author Max Reichardt
 *
 * Slightly enhanced JPopupMenu.
 */
public class MPopupMenu extends JPopupMenu implements PopupMenuListener {

    /** UID */
    private static final long serialVersionUID = 8887090956269065746L;

    private Action cancelAction;
    private boolean registered;

    public void setCancelAction(Action a) {
        cancelAction = a;
        if (!registered) {
            addPopupMenuListener(this);
            registered = true;
        }
    }

    public void popupMenuCanceled(PopupMenuEvent e) {
        if (cancelAction != null) {
            cancelAction.actionPerformed(new ActionEvent(this, 0, ""));
        }
    }

    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
}
