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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * @author max
 *
 * Action with enum-ID
 */
public class MAction extends AbstractAction {

    /** UID */
    private static final long serialVersionUID = 2968006125643024705L;

    private Enum<?> id;
    private ActionListener listener;
    private Object customData;

    public MAction(Enum<?> id, String text, String iconfilename, int mnemonic, ActionListener listener, Object customData) {
        this.id = id;
        this.listener = listener;
        this.customData = customData;
        putValue(Action.NAME, text);
        putValue(Action.SHORT_DESCRIPTION, text);
        if (iconfilename != null) {
            putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon(iconfilename));
        }
        putValue(Action.MNEMONIC_KEY, mnemonic);
    }

    public MAction(Enum<?> id, String largeIconfilename, String text, ActionListener listener, Object customData) {
        this(id, text, largeIconfilename, -1, listener, customData);
    }

    public MAction(Enum<?> id, String largeIconfilename, String text, ActionListener listener) {
        this(id, text, largeIconfilename, -1, listener, null);
    }

    public MAction(Enum<?> id, ActionListener listener, Object customData) {
        this(id, null, null, listener, customData);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        listener.actionPerformed(new MActionEvent(this, e));
    }

    public Enum<?> getID() {
        return id;
    }

    public Object getCustomData() {
        return customData;
    }
}
