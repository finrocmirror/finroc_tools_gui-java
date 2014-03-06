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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.finroc.tools.gui.util.gui.MDialog;

/**
 * @author Max Reichardt
 *
 * Connect dialog that can be displayed when starting GUI, for instance
 */
public class ConnectDialog extends MDialog implements PropertyChangeListener {

    /** UID */
    private static final long serialVersionUID = 8279978233242623568L;

    private JPanel jp = new JPanel();
    private ArrayList<JButton> buttons = new ArrayList<JButton>();
    private JOptionPane addressInput;

    private int connectionChosen = -1;
    private boolean addressInputChosen;

    public ConnectDialog(Frame owner, boolean modal, boolean showAddressInput, String title) {
        super(owner, modal);
        this.setMinimumSize(new Dimension(280, 80));
        this.setLocation(480, 320);
        getContentPane().setLayout(new BorderLayout());
        JPanel tmp = new JPanel();
        tmp.add(jp);
        getContentPane().add(tmp, BorderLayout.CENTER);
        if (showAddressInput) {
            addressInput = new JOptionPane(title, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, null, null);
            getContentPane().add(addressInput, BorderLayout.NORTH);
            addressInput.setWantsInput(true);
            addressInput.addPropertyChangeListener(this);
            setTitle("Connect");
        } else {
            JLabel imageLabel = new JLabel(UIManager.getIcon("OptionPane.questionIcon"));
            imageLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
            getContentPane().add(imageLabel, BorderLayout.WEST);
            setTitle(title);
        }
    }

    /**
     * Shows connection dialog
     *
     * @param options Connection options
     * @param addressInput Default string address input
     * @return Connection the user chose - or null if he did not choose any
     */
    public String show(List<String> options, String addressInput) {
        jp.setLayout(new GridLayout(options.size() + (this.addressInput != null ? 0 : 1), 1));
        jp.setBorder(new EmptyBorder(7, 0, 7, 0));
        for (String connection : options) {
            buttons.add(createButton(connection, jp));
        }
        if (this.addressInput != null) {
            this.addressInput.setInitialSelectionValue(addressInput);
        } else {
            buttons.add(createButton("No, thank you", jp));
        }

        pack();
        setVisible(true);

        if (addressInputChosen) {
            return this.addressInput.getInputValue() == JOptionPane.UNINITIALIZED_VALUE ? null : this.addressInput.getInputValue().toString();
        } else if (connectionChosen >= 0 && connectionChosen < options.size()) {
            return options.get(connectionChosen);
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        connectionChosen = buttons.indexOf(e.getSource());
        addressInputChosen = false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setVisible(false);
        addressInputChosen = true;
    }

}
