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
package org.finroc.tools.gui.util.gui;

import java.awt.event.ActionEvent;

/**
 * @author Max Reichardt
 *
 * ActionEvent with reference to (M)Action
 */
public class MActionEvent extends ActionEvent {

    /** UID */
    private static final long serialVersionUID = 5910921574533286846L;

    private MAction action;

    public MActionEvent(MAction action, ActionEvent ae) {
        super(ae.getSource(), ae.getID(), ae.getActionCommand(), ae.getWhen(), ae.getModifiers());
        this.action = action;
    }

    public Enum<?> getEnumID() {
        return action.getID();
    }

    public Object getCustomData() {
        return action.getCustomData();
    }

    public MAction getAction() {
        return action;
    }
}
