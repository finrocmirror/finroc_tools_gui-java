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
package org.finroc.tools.gui.util.treemodel;

import org.finroc.core.FrameworkElement;
import org.finroc.core.port.AbstractPort;

class InterfaceNodePort extends InterfaceNode implements TreePortWrapper {

    /** UID */
    private static final long serialVersionUID = 245735681L;

    public InterfaceNodePort(FrameworkElement.Link link) {
        super(link);
    }

    public AbstractPort getPort() {
        return (AbstractPort)wrapped.getChild();
    }

    public boolean isInputPort() {
        return !getPort().isInputPort(); // the other way round for GUI ... for historical reasons
    }
}
