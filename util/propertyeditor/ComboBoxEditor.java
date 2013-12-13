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
import javax.swing.JComboBox;

import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

/**
 * @author Max Reichardt
 *
 * Editor with ComboBox
 */
public class ComboBoxEditor<T> extends PropertyEditComponent<T> {

    /** UID */
    private static final long serialVersionUID = -6118534525281404885L;

    protected JComboBox jcmb;

    /** Possible values */
    private final T[] values;

    public ComboBoxEditor(T[] values) {
        this.values = values;
    }

    @Override
    protected void createAndShow() {
        try {
            jcmb = new JComboBox(values);
            valueUpdated(getCurWidgetValue());
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, this, e);
        }
        jcmb.setMinimumSize(new Dimension(TEXTFIELDWIDTH, jcmb.getPreferredSize().height));
        add(jcmb, BorderLayout.CENTER);
        jcmb.setEnabled(isModifiable());
    }

    @Override
    public void createAndShowMinimal(T object) throws OperationNotSupportedException {
        try {
            jcmb = new JComboBox(values);
            valueUpdated(object);
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, this, e);
        }
        add(jcmb);
        jcmb.setEnabled(isModifiable());
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getCurEditorValue() {
        return (T)jcmb.getSelectedItem();
    }

    @Override
    protected void valueUpdated(T t) {
        jcmb.setSelectedItem(t);
    }
}
