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
package org.finroc.tools.gui.util.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.naming.OperationNotSupportedException;
import javax.swing.JTextField;

public class NumberEditor extends PropertyEditComponent<Number> {

    /** UID */
    private static final long serialVersionUID = 14573650185683L;

    private JTextField jtf;

    protected void createAndShow() throws Exception {
        jtf = new JTextField();
        jtf.setEnabled(isModifiable());
        valueUpdated(getCurWidgetValue());
        jtf.setPreferredSize(new Dimension(TEXTFIELDWIDTH, jtf.getPreferredSize().height));
        add(jtf, BorderLayout.WEST);
    }


    @Override
    public void createAndShowMinimal(Number number) throws OperationNotSupportedException {
        jtf = new JTextField();
        jtf.setEnabled(isModifiable());
        valueUpdated(number);
        jtf.setPreferredSize(new Dimension(TEXTFIELDWIDTH / 2, jtf.getPreferredSize().height));
        add(jtf);
    }


    @Override
    public Number getCurEditorValue() {
        Class<?> t = getPropertyType();
        if (t.equals(Double.class) || t.equals(Number.class) || t.equals(double.class)) {
            try {
                return Double.parseDouble(jtf.getText());
            } catch (Exception e) {
                return Double.NaN;
            }
        } else if (t.equals(Float.class) || t.equals(float.class)) {
            return Float.parseFloat(jtf.getText());
        } else if (t.equals(Integer.class) || t.equals(int.class)) {
            return Integer.parseInt(jtf.getText());
        } else if (t.equals(Short.class) || t.equals(short.class)) {
            return Short.parseShort(jtf.getText());
        } else if (t.equals(Byte.class) || t.equals(byte.class)) {
            return Byte.parseByte(jtf.getText());
        } else if (t.equals(Long.class) || t.equals(long.class)) {
            return Long.parseLong(jtf.getText());
        }
        throw new RuntimeException("This shouldn't happen");
    }


    @Override
    protected void valueUpdated(Number t) {
        jtf.setText(t.toString());
    }
}
