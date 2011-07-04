/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2011 Max Reichardt
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
package org.finroc.tools.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.finroc.tools.gui.util.gui.MDialog;

/**
 * @author max
 *
 * Connect dialog that can be displayed when starting GUI, for instance
 */
public class ConnectDialog extends MDialog {

    /** UID */
    private static final long serialVersionUID = 8279978233242623568L;

    private JPanel jp = new JPanel();
    private ArrayList<JButton> buttons = new ArrayList<JButton>();

    private int connectionChosen = -1;

    public ConnectDialog(Frame owner, boolean modal) {
        super(owner, modal);
        this.setMinimumSize(new Dimension(280, 80));
        this.setLocation(480, 320);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(jp, BorderLayout.CENTER);
        setTitle("Would you like to connect now?");
    }

    /**
     * Shows connection dialog
     *
     * @param options Connection options
     * @return Connection the user chose - or null if he did not choose any
     */
    public String show(List<String> options) {
        jp.setLayout(new GridLayout(options.size() + 1, 1));
        for (String connection : options) {
            JPanel jp2 = new JPanel();
            jp.add(jp2);
            buttons.add(createButton(connection, jp2));
        }
        JPanel jp2 = new JPanel();
        jp.add(jp2);
        buttons.add(createButton("No, thankyou", jp2));

        pack();
        setVisible(true);

        if (connectionChosen >= 0 && connectionChosen < options.size()) {
            return options.get(connectionChosen);
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        connectionChosen = buttons.indexOf(e.getSource());
    }

}
