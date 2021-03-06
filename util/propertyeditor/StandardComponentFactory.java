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

import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.finroc.plugins.data_types.ContainsStrings;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

/**
 * @author Max Reichardt
 *
 * Component factory for standard types.
 * If other factories are used, this can be used to aggregate them.
 */
public class StandardComponentFactory implements ComponentFactory {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public PropertyEditComponent<?> createComponent(PropertyAccessor<?> acc, PropertiesPanel panel) throws Exception {
        Class<?> type = acc.getType();
        PropertyEditComponent wpec = null;
        if (acc.getAnnotation(EditComponent.class) != null) {
            Class componentClass = acc.getAnnotation(EditComponent.class).value();
            Constructor constructor = componentClass.getConstructor(PropertiesPanel.class);
            assert(constructor != null);
            wpec = (PropertyEditComponent)constructor.newInstance(panel);
        } else if (type.equals(String.class)) {
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
            wpec = new PropertyListEditor(panel, panel.getComponentFactories());
        } else if (ContainsStrings.class.isAssignableFrom(type)) {
            wpec = new StringEditor(-1);
            acc = new ContainsStringsAdapter((PropertyAccessor)acc);
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
            Log.log(LogLevel.ERROR, e);
        }
        return new Enum[0];
    }

    /**
     * Allows using StringLists in TextEditor
     */
    public class ContainsStringsAdapter extends PropertyAccessorAdapter<ContainsStrings, String> {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public ContainsStringsAdapter(PropertyAccessor wrapped) {
            super(wrapped, String.class);
        }

        @Override
        public void set(String newValue) throws Exception {
            ContainsStrings cs = wrapped.getType().newInstance();
            if (newValue.trim().length() > 0) {
                String[] strings = newValue.split("\n");
                cs.setSize(strings.length);
                for (int i = 0; i < strings.length; i++) {
                    cs.setString(i, strings[i]);
                }
            }
            wrapped.set(cs);
        }

        @Override
        public String get() throws Exception {
            ContainsStrings cs = wrapped.get();
            if (cs == null) {
                return null;
            }
            String s = "";
            for (int i = 0; i < cs.stringCount(); i++) {
                s += cs.getString(i) + "\n";
            }
            return cs.stringCount() == 0 ? "" : s.substring(0, s.length() - 1);
        }
    }

}
