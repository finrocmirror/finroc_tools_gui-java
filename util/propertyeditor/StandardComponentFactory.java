/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2010 Max Reichardt
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

import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Method;

import org.finroc.tools.gui.FinrocGUI;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author max
 *
 * Component factory for standard types.
 * If other factories are used, this can be used to aggregate them.
 */
public class StandardComponentFactory implements ComponentFactory {

    @SuppressWarnings( { "rawtypes", "unchecked" })
    @Override
    public PropertyEditComponent<?> createComponent(PropertyAccessor<?> acc, PropertiesPanel panel) throws Exception {
        Class<?> type = acc.getType();
        PropertyEditComponent wpec = null;
        if (type.equals(String.class)) {
            wpec = new StringEditor(acc.getAnnotation(LongText.class) != null ? -1 : 0);
        } else if (Number.class.isAssignableFrom(type) || type.equals(int.class) || type.equals(double.class) || type.equals(float.class) || type.equals(long.class) || type.equals(short.class) || type.equals(byte.class)) {
            wpec = new NumberEditor();
        } else if (type.equals(Color.class)) {
            wpec = new ColorEditor();
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            wpec = new BooleanEditor();
        } else if (type.equals(Font.class)) {
            wpec = new FontEditor();
        } else if (Enum.class.isAssignableFrom(type)) {
            wpec = new ComboBoxEditor<Enum>(getEnumConstants((Class <? extends Enum >)type));
        } else if (PropertyListAccessor.class.isAssignableFrom(type)) {
            wpec = new PropertyListEditor(panel.getComponentFactories());
        }
        if (wpec != null) {
            wpec.init(acc);
        }
        return wpec;

        //wpec.widgets.add(tmp);
        //wpec.parent = parent;
        //wpec.curValue = wpec.getCurWidgetValue();
        //wpec.createAndShow();
    }

    /**
     * @param enumClass Enum class
     * @return All enum constants of this class
     */
    @SuppressWarnings("rawtypes")
    public static Enum[] getEnumConstants(Class <? extends Enum > enumClass) {
        try {
            Method m = enumClass.getMethod("values");
            m.setAccessible(true);
            Enum[] values = (Enum[])m.invoke(null);
            return values;
        } catch (Exception e) {
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, "EnumEditor", e);
        }
        return new Enum[0];
    }
}
