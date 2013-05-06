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
 * @author Max Reichardt
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

    /** Maximum string length for JTextField editor (to avoid frozen user interface) */
    private static final int MAX_TEXT_FIELD_STRING_LENGTH = 1000;

    /** Set to true if string is too long for text field. Apply changes will be inactive in this case */
    private boolean stringTooLong;

    public StringEditor() {
        this(0);
    }

    public StringEditor(int maxStringLength) {
        this.maxStringLength = maxStringLength;
    }

    protected void createAndShow() throws Exception {
        if (maxStringLength >= 0) {
            jtc = new JTextField();
            valueUpdated(getCurWidgetValue());
            jtc.setMinimumSize(new Dimension(TEXTFIELDWIDTH, jtc.getPreferredSize().height));
            if (maxStringLength > 0) {
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
            //jtc.setPreferredSize(new Dimension(TEXTFIELDWIDTH, 100));
            valueUpdated(getCurWidgetValue());
            JPanel jp = new JPanel();
            jp.setBorder(BorderFactory.createTitledBorder(""/*getPropertyName()*/));
            jp.setLayout(new BorderLayout());
            jp.setPreferredSize(new Dimension(TEXTFIELDWIDTH, 100));
            jp.add(new JScrollPane(jtc), BorderLayout.CENTER);
            add(jp, BorderLayout.CENTER);
        }
        jtc.setEnabled(isModifiable());
    }

    @Override
    public void createAndShowMinimal(String s) throws OperationNotSupportedException {
        jtc = new JTextField();
        valueUpdated(s);
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
        if (t == null) {
            jtc.setText("");
        } else if (jtc instanceof JTextField && t.length() > MAX_TEXT_FIELD_STRING_LENGTH) {
            jtc.setText(t.substring(0, 1000) + "...");
            stringTooLong = true;
        } else {
            jtc.setText(t);
        }
    }

    @Override
    public boolean isResizable() {
        return jtc instanceof JTextArea;
    }

    @Override
    public void applyChanges() throws Exception {
        if (!stringTooLong) {
            super.applyChanges();
        }
    }
}
