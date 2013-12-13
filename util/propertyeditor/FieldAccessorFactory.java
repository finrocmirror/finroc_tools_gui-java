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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.finroc.tools.gui.commons.reflection.ReflectionCallback;
import org.finroc.tools.gui.commons.reflection.ReflectionHelper;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

/**
 * @author Max Reichardt
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
                                } else {
                                    FieldAccessor fa = customFieldHandling(f, x);
                                    if (fa != null) {
                                        components.put(f, fa);
                                        componentsList.add(fa);
                                    }
                                }
                            }
                        } else {
                            components.get(f).addObject(x);
                        }
                    }
                }, 0);
            }
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, this, e);
        }
        return componentsList;
    }

    /**
     * It's possible to override this in order to handle field in another custom way.
     * (if it is not handled in default way already).
     *
     * @param f Field
     * @param x Object currently inspected
     * @return Field Accessor - or null if not handled
     */
    protected FieldAccessor customFieldHandling(Field f, Object x) {
        return null;
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
