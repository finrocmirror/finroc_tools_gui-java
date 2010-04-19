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
package org.finroc.gui.widgets;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetOutput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetPorts;
import org.finroc.gui.WidgetUI;

import org.finroc.plugin.datatype.ContainsStrings;
import org.finroc.plugin.datatype.mca.StringBlackboardBuffer;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.std.PortBase;
import org.finroc.core.port.std.PortListener;


/**
 * @author max
 *
 */
public class TextEditor extends Widget {

    /** UID */
    private static final long serialVersionUID = 783829462389320L;

    /** Text input and output */
    private WidgetInput.Std<ContainsStrings> textInput;
    private WidgetOutput.Std<StringBlackboardBuffer> textOutput;

    @Override
    protected WidgetUI createWidgetUI() {
        return new TextEditorUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort, WidgetPorts<?> collection) {
        return suggestion.derive(forPort == textInput ? ContainsStrings.TYPE : StringBlackboardBuffer.TYPE);
    }

    class TextEditorUI extends WidgetUI implements ActionListener, PortListener<ContainsStrings> {

        /** UID */
        private static final long serialVersionUID = -3456236219915623650L;

        private JTextArea textArea;
        private JButton undoButton, commitButton;

        TextEditorUI() {
            super(RenderMode.Swing);
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new GridLayout(1, 2));
            undoButton = new JButton("Undo Changes");
            undoButton.addActionListener(this);
            buttonPanel.add(undoButton);
            commitButton = new JButton("Commit Changes");
            commitButton.addActionListener(this);
            buttonPanel.add(commitButton);
            setLayout(new BorderLayout());
            textArea = new JTextArea();
            textArea.setTabSize(2);
            add(new JScrollPane(textArea), BorderLayout.CENTER);
            add(buttonPanel, BorderLayout.SOUTH);
            textInput.addChangeListener(this);
            portChanged(null, null);
        }

        @Override
        protected boolean isWidgetFocusable() {
            return true;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == commitButton) {

                StringBlackboardBuffer buffer = textOutput.getUnusedBuffer();
                String[] lines = textArea.getText().split("\n");
                int maxLength = 0;
                for (String l : lines) {
                    maxLength = Math.max(maxLength, l.length());
                }
                buffer.resize(lines.length, lines.length, maxLength + 1, false);
                for (int i = 0; i < lines.length; i++) {
                    buffer.setString(i, lines[i]);
                }
                textOutput.publish(buffer);
            } else {
                portChanged(null, null);
            }
        }


        @Override
        public void portChanged(PortBase origin, ContainsStrings value) {
            ContainsStrings cs = textInput.getAutoLocked();
            if (cs != null) {
                textArea.setText(ContainsStrings.Util.toSingleString(cs).toString());
            } else {
                textArea.setText("");
            }
            releaseAllLocks();
        }
    }
}
