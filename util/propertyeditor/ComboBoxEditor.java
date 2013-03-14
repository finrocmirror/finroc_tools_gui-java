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
import javax.swing.JComboBox;

import org.finroc.tools.gui.FinrocGUI;
import org.rrlib.finroc_core_utils.log.LogLevel;

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
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), e);
        }
        jcmb.setPreferredSize(new Dimension(TEXTFIELDWIDTH, jcmb.getPreferredSize().height));
        add(jcmb, BorderLayout.WEST);
        jcmb.setEnabled(isModifiable());
    }

    @Override
    public void createAndShowMinimal(T object) throws OperationNotSupportedException {
        try {
            jcmb = new JComboBox(values);
            valueUpdated(object);
        } catch (Exception e) {
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), e);
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
