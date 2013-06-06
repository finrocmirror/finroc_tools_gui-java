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
package org.finroc.tools.gui.util.gui;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * @author Max Reichardt
 *
 * Nice, convenient, higher level toolbar
 */
public class MToolBar extends JToolBar {

    /** UID */
    private static final long serialVersionUID = -1271048336146051984L;

    /** Register with ActionEnums and actions (not used yet) */
    private Map < Enum<?>, Action > actionRegister = new HashMap < Enum<?>, Action > ();
    private Map < Enum<?>, AbstractButton > buttonRegister = new HashMap < Enum<?>, AbstractButton > ();
    private ButtonGroup buttonGroup = new ButtonGroup();

    public MToolBar(String string) {
        this(string, MToolBar.HORIZONTAL);
    }

    public MToolBar(String string, int orientation) {
        super(string);
        setBorder(BorderFactory.createEtchedBorder());
        this.setOrientation(orientation);
        this.setFloatable(false);
    }

    public JButton createButton(String iconFilename, String toolTip, ActionListener listener) {
        JButton jb = new JButton();
        jb.setIcon(IconManager.getInstance().getIcon(iconFilename));
        jb.addActionListener(listener);
        jb.setToolTipText(toolTip);
        jb.setFocusable(false);
        jb.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        add(jb);
        return jb;
    }

    public JToggleButton createToggleButton(String iconFilename, String toolTip, ActionListener listener) {
        JToggleButton jtb = new JToggleButton();
        jtb.setIcon(IconManager.getInstance().getIcon(iconFilename));
        jtb.addActionListener(listener);
        jtb.setToolTipText(toolTip);
        jtb.setFocusable(false);
        jtb.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        add(jtb);
        return jtb;
    }

    public JButton add(Action a) {
        JButton jb = super.add(a);
        jb.setFocusable(false);
        jb.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        if (a instanceof MAction) {
            Enum<?> e = ((MAction)a).getID();
            actionRegister.put(e, a);
            buttonRegister.put(e, jb);
        }
        return jb;
    }

    public JToggleButton addToggleButton(Action a) {
        return addToggleButton(a, false);
    }

    public JToggleButton addToggleButton(Action a, boolean independantSelection) {
        JToggleButton jb = new JToggleButton(a);
        jb.setFocusable(false);
        jb.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jb.setText("");
        if (a instanceof MAction) {
            MAction ma = (MAction)a;
            Enum<?> e = ma.getID();
            actionRegister.put(e, a);
            buttonRegister.put(e, jb);
            if (ma.getValue(Action.SMALL_ICON) == null) {
                jb.setText(ma.getValue(Action.NAME).toString());
            }
        }
        add(jb);
        if (!independantSelection) {
            buttonGroup.add(jb);
        }
        return jb;
    }

    public AbstractButton getButton(Enum<?> e) {
        return buttonRegister.get(e);
    }

    public void setSelected(Enum<?> e) {
        setSelected(e, true);
    }

    public void setSelected(Enum<?> e, boolean selected) {
        AbstractButton jb = buttonRegister.get(e);
        jb.setSelected(selected);
    }

    /**
     * @param e Enum
     * @return Is button associated with specified enum selected?
     */
    public boolean isSelected(Enum<?> e) {
        AbstractButton btn = buttonRegister.get(e);
        if (btn == null) {
            return false;
        }
        return btn.isSelected();
    }

    /**
     * Is any button associated with any of the provided enum constants selected?
     *
     * @param values Enum values
     * @return Such a button
     */
    public < E extends Enum<? >> E getSelection(E[] values) {
        for (Map.Entry < Enum<?>, AbstractButton > button : buttonRegister.entrySet()) {
            if (button.getValue().isSelected()) {
                for (E e : values) {
                    if (e == button.getKey()) {
                        return e;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Inserts another button group.
     * All toggle buttons added now, belong to this new group
     */
    public void startNextButtonGroup() {
        buttonGroup = new ButtonGroup();
    }

    /**
     * Clear toolbar
     */
    public void clear() {
        actionRegister.clear();
        startNextButtonGroup();
        buttonRegister.clear();
        super.removeAll();
    }
}
