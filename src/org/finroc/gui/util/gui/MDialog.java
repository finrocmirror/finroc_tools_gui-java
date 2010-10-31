/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2010 Max Reichardt
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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

/**
 * @author max
 *
 * JDialog with some additional methods for convenience
 */
public abstract class MDialog extends JDialog implements ActionListener {

    /** UID */
    private static final long serialVersionUID = -1243745311686880097L;

    public MDialog(Frame owner, boolean modal) {
        super(owner, modal);
    }

    public MDialog(Dialog owner, boolean modal) {
        super(owner, modal);
    }

    /**
     * Create button
     *
     * @param text button text
     * @param panel panel to add button to
     */
    public JButton createButton(String text, JPanel panel) {
        JButton jb = new JButton(text);
        jb.addActionListener(this);
        panel.add(jb);
        return jb;
    }

    /**
     * Close dialog
     */
    protected void close() {
        setVisible(false);
        getRootPane().removeAll();
        dispose();
    }
}
