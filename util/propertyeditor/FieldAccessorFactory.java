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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.finroc.tools.gui.FinrocGUI;
import org.finroc.tools.gui.commons.reflection.ReflectionCallback;
import org.finroc.tools.gui.commons.reflection.ReflectionHelper;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author max
 *
 * Scans set of objects for fields and creates respective PropertyEditComponents
 */
public class FieldAccessorFactory {

    /** singleton instance */
    private static final FieldAccessorFactory instance = new FieldAccessorFactory();

    /**
     * @return singleton instance
     */
    public static FieldAccessorFactory getInstance() {
        return instance;
    }

    /**
     * Scans object for fields and creates respective PropertyEditComponents
     *
     * @param object Object to scan
     * @return List of accessors for object
     */
    public List < PropertyAccessor<? >> createAccessors(Object object) {
        ArrayList<Object> tmp = new ArrayList<Object>();
        tmp.add(object);
        return createAccessors(tmp);
    }

    /**
     * Scans set of objects for fields and creates respective PropertyEditComponents
     *
     * @param objects Objects to scan
     * @return List of accessors for objects
     */
    public List < PropertyAccessor<? >> createAccessors(Collection<Object> objects) {

        // inspect widget and create components
        final HashMap < Field, FieldAccessor> components = new HashMap < Field, FieldAccessor> ();
        final List < PropertyAccessor<? >> componentsList = new ArrayList < PropertyAccessor<? >> ();
        try {
            for (final Object x : objects) {
                ReflectionHelper.visitAllFields(x.getClass(), true, true, new ReflectionCallback<Field>() {
                    public void reflectionCallback(Field f, int id) throws Exception {
                        if (!components.containsKey(f)) {
                            if (!Modifier.isTransient(f.getModifiers())) {
                                if (!ignoreField(f) && f.getAnnotation(NotInPropertyEditor.class) == null) {
                                    FieldAccessor fa = new FieldAccessor(f, x);
                                    components.put(f, fa);
                                    componentsList.add(fa);
                                }
                            }
                        } else {
                            components.get(f).addObject(x);
                        }
                    }
                }, 0);
            }
        } catch (Exception e) {
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), e);
        }
        return componentsList;
    }

    /**
     * may be overridden by subclass for specific editors
     *
     * @param f Field
     * @return Should field not be editable?
     */
    protected boolean ignoreField(Field f) {
        return false;
    }
}
