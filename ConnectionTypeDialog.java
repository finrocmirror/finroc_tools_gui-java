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
package org.finroc.tools.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.finroc.core.plugin.CreateExternalConnectionAction;
import org.finroc.core.plugin.Plugins;
import org.finroc.tools.gui.util.gui.MDialog;

/**
 * @author Max Reichardt
 *
 * Dialog to select connection type
 */
public class ConnectionTypeDialog extends MDialog {

    /** UID */
    private static final long serialVersionUID = 5724426328228469002L;

    /** Reference to GUI */
    private final FinrocGUI gui;

    /** JPanel for buttons etc. */
    private final JPanel panel = new JPanel();

    /** Save selection to GUI file? */
    private JCheckBox saveCheckBox = new JCheckBox("Save changes to GUI file");

    /** Buttons for different options */
    private ArrayList<JButton> buttons = new ArrayList<JButton>();

    /** String with action that user chose */
    private String result;

    private static final String DO_NOT_CONNECT_BUTTON_STRING = "Do not connect";

    public ConnectionTypeDialog(Frame owner, FinrocGUI gui) {
        super(owner, true);
        this.setMinimumSize(new Dimension(320, 80));
        this.setLocation(480, 320);
        this.gui = gui;
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        getContentPane().add(panel);
        setTitle("Please choose a default connection type");
        this.add(panel);
    }

    /**
     * Shows connection dialog
     *
     * @param options Connection options
     * @return Connection the user chose - or null if he did not choose any
     */
    public String showDialog() {
        for (CreateExternalConnectionAction ceca : Plugins.getInstance().getExternalConnections()) {
            if ((ceca.getFlags() & CreateExternalConnectionAction.REMOTE_EDGE_INFO) == 0) {
                buttons.add(createButton(ceca.getName(), panel));
                buttons.get(buttons.size() - 1).setAlignmentX(Component.CENTER_ALIGNMENT);
            }
        }

        buttons.add(createButton(DO_NOT_CONNECT_BUTTON_STRING, panel));
        buttons.get(buttons.size() - 1).setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(saveCheckBox);
        saveCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        pack();
        setVisible(true);

        if (saveCheckBox.isSelected() && result != null) {
            gui.getModel().setDefaultConnectionType(result);
            gui.saveGUI();
        }

        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        result = ((JButton)e.getSource()).getText();
        if (result.equals(DO_NOT_CONNECT_BUTTON_STRING)) {
            result = null;
        }
    }

}
