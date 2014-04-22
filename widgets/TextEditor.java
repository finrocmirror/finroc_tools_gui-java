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
package org.finroc.tools.gui.widgets;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;

import org.finroc.plugins.data_types.ContainsStrings;
import org.finroc.plugins.data_types.StdStringList;
import org.finroc.plugins.data_types.mca.StringBlackboardBuffer;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;


/**
 * @author Max Reichardt
 *
 */
public class TextEditor extends Widget {

    /** UID */
    private static final long serialVersionUID = 783829462389320L;

    /** Text input and output */
    private WidgetInput.Std<ContainsStrings> textInput;
    private WidgetOutput.Std<StringBlackboardBuffer> textOutput;
    private WidgetOutput.Std<StdStringList> stringListOutput;

    private boolean hideButtons = false;

    @Override
    protected WidgetUI createWidgetUI() {
        return new TextEditorUI();
    }

    @Override
    protected void setDefaultColors() {
        useAlternativeColors();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(forPort == textInput ? ContainsStrings.TYPE : (forPort == textOutput ? StringBlackboardBuffer.TYPE : StdStringList.TYPE));
    }

    class TextEditorUI extends WidgetUI implements ActionListener, PortListener<ContainsStrings>, Runnable {

        /** UID */
        private static final long serialVersionUID = -3456236219915623650L;

        private JTextArea textArea;
        private JButton undoButton, commitButton;
        private JPanel buttonPanel;

        TextEditorUI() {
            super(RenderMode.Swing);
            buttonPanel = new JPanel();
            buttonPanel.setOpaque(useOpaquePanels());
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
            widgetPropertiesChanged();
        }

        @Override
        protected boolean isWidgetFocusable() {
            return true;
        }

        @Override
        public void widgetPropertiesChanged() {
            buttonPanel.setVisible(!hideButtons);

            validate();
            repaint();
            //actionPerformed(null);  // update value
            portChanged(null, null);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == commitButton) {

                StringBlackboardBuffer buffer = textOutput.getUnusedBuffer();
                StdStringList listBuffer = stringListOutput.getUnusedBuffer();
                String[] lines = textArea.getText().split("\n");
                int maxLength = 0;
                for (String l : lines) {
                    maxLength = Math.max(maxLength, l.length());
                }
                buffer.resize(lines.length, lines.length, maxLength + 1, false);
                listBuffer.setSize(lines.length);
                for (int i = 0; i < lines.length; i++) {
                    buffer.setString(i, lines[i]);
                    listBuffer.setString(i, lines[i]);
                }
                textOutput.publish(buffer);
                stringListOutput.publish(listBuffer);
            } else {
                portChanged(null, null);
            }
        }

        @Override
        public void portChanged(AbstractPort origin, ContainsStrings value) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
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
