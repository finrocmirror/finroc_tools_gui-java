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
package org.finroc.gui.util.propertyeditor;

import java.awt.Dimension;
import java.lang.reflect.Method;

import javax.naming.OperationNotSupportedException;
import javax.swing.JComboBox;

/**
 * @author max
 *
 */
public class EnumEditor extends PropertyEditComponent<Enum> {

    /** UID */
    private static final long serialVersionUID = -6118534525281404885L;

    JComboBox jcmb;

    @Override
    protected void createAndShow() {
        try {
            Method m = property.getType().getMethod("values");
            m.setAccessible(true);
            Enum[] values = (Enum[])m.invoke(null);
            jcmb = new JComboBox(values);
            jcmb.setSelectedItem(getCurWidgetValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        jcmb.setPreferredSize(new Dimension(TEXTFIELDWIDTH, jcmb.getPreferredSize().height));
        createStdLayoutWith(jcmb);
    }



    @Override
    public void createAndShowMinimal(Enum object) throws OperationNotSupportedException {
        try {
            Method m = property.getType().getMethod("values");
            m.setAccessible(true);
            Enum[] values = (Enum[])m.invoke(null);
            jcmb = new JComboBox(values);
            jcmb.setSelectedItem(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        add(jcmb);
    }



    @Override
    public Enum getCurEditorValue() {
        return (Enum)jcmb.getSelectedItem();
    }

}
