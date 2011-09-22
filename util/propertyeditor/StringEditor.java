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
package org.finroc.tools.gui.util.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.naming.OperationNotSupportedException;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

/**
 * @author max
 *
 */
public class StringEditor extends PropertyEditComponent<String> {

    /** UID */
    private static final long serialVersionUID = 2486687318726499512L;

    private JTextComponent jtc;

    /**
     * Maximum string length
     *
     * values > 0 are fixed number of characters
     * value = 0 is normal (single-line editor)
     * value = -1 is long (multi-line text editor)
     */
    private int maxStringLength;

    public StringEditor() {
        this(0);
    }

    public StringEditor(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }

    protected void createAndShow() throws Exception {
        if(maxStringLength >= 0) {
            jtc = new JTextField();
            jtc.setText(getCurWidgetValue());
            jtc.setMinimumSize(new Dimension(TEXTFIELDWIDTH, jtc.getPreferredSize().height));
            if(maxStringLength > 0) {
                Dimension d = new Dimension(Math.max(TEXTFIELDWIDTH, CHAR_WIDTH * maxStringLength), jtc.getPreferredSize().height);
                jtc.setMinimumSize(d);
                jtc.setPreferredSize(d);
                add(jtc, BorderLayout.WEST);
            } else {
                jtc.setMinimumSize(new Dimension(TEXTFIELDWIDTH, jtc.getPreferredSize().height));
                add(jtc, BorderLayout.CENTER);
            }
        } else {
            jtc = new JTextArea();
            jtc.setMinimumSize(new Dimension(TEXTFIELDWIDTH, 100));
            jtc.setPreferredSize(new Dimension(TEXTFIELDWIDTH, 100));
            valueUpdated(getCurWidgetValue());
            JPanel jp = new JPanel();
            jp.setBorder(BorderFactory.createTitledBorder(""/*getPropertyName()*/));
            jp.setLayout(new BorderLayout());
            //jp.setPreferredSize(new Dimension(LABELWIDTH + TEXTFIELDWIDTH, 128));
            jp.add(new JScrollPane(jtc), BorderLayout.CENTER);
            add(jp, BorderLayout.CENTER);
        }
        jtc.setEnabled(isModifiable());
    }

    @Override
    public void createAndShowMinimal(String s) throws OperationNotSupportedException {
        jtc = new JTextField();
        jtc.setText(s);
        jtc.setMinimumSize(new Dimension(TEXTFIELDWIDTH, jtc.getPreferredSize().height));
        jtc.setPreferredSize(new Dimension(TEXTFIELDWIDTH, jtc.getPreferredSize().height));
        add(jtc);
        jtc.setEnabled(isModifiable());
    }

    @Override
    public String getCurEditorValue() {
        return jtc.getText();
    }

    @Override
    protected void valueUpdated(String t) {
        jtc.setText(t);
    }

    @Override
    public boolean isResizable() {
        return jtc instanceof JTextArea;
    }
}
