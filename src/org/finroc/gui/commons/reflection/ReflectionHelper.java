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
package org.finroc.gui.commons.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author max
 *
 * This class contains some utility functions to help with common reflection tasks
 */
public class ReflectionHelper {

    /**
     * Copy all attributes from one object to another.
     * Works only if objects share the same superclasses.
     *
     * @param dest Destination object
     * @param source Source object
     */
    public static void copyAllAttributes(final Object dest, final Object source) throws Exception {
        Class<?> c = getCommonSuperClass(dest, source);

        // copy
        visitAllFields(c, true, true, new ReflectionCallback<Field>() {
            public void reflectionCallback(Field f, int id) throws Exception {
                f.set(dest, f.get(source));
            }
        }, 0);
    }


    /**
     * 'Visit' all Fields of class and call callback function with field and provided id
     *
     * @param c Class to visit all fields of
     * @param visitPrivateFields Visit private fields?
     * @param visitSuperClasses Visit fields of superclass?
     * @param callback Callback
     * @param id Id provided to callback
     */
    public static void visitAllFields(Class<?> c, boolean visitPrivateFields, boolean visitSuperClasses, ReflectionCallback<Field> callback, int id) throws Exception {
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isStatic(mod)) {
                continue;
            }
            if (!Modifier.isPublic(mod)) {
                if (!visitPrivateFields) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                } catch (Exception e) {}
            }
            callback.reflectionCallback(f, id);
        }
        if (visitSuperClasses) {
            c = c.getSuperclass();
            if (c != Object.class) {
                visitAllFields(c, visitPrivateFields, visitSuperClasses, callback, id);
            }
        }
    }

    private static Class<?> getCommonSuperClass(Object dest, Object source) {
        Class<?> destClass = dest.getClass();
        Class<?> sourceClass = source.getClass();
        int destSuperClasses = getNumberOfSuperClasses(destClass);
        int sourceSuperClasses = getNumberOfSuperClasses(sourceClass);
        if (destSuperClasses < sourceSuperClasses) {
            sourceClass = getNthSuperClass(sourceClass, sourceSuperClasses - destSuperClasses);
        } else {
            destClass = getNthSuperClass(destClass, destSuperClasses - sourceSuperClasses);
        }
        while (!sourceClass.equals(destClass)) {
            sourceClass = sourceClass.getSuperclass();
            destClass = destClass.getSuperclass();
        }
        return destClass;
    }

    /**
     * @param c Class c
     * @param n n
     * @return Return nth superclass of Class c
     */
    private static Class<?> getNthSuperClass(Class<?> c, int n) {
        Class<?> result = c;
        for (int i = 0; i < n; i++) {
            result = result.getSuperclass();
        }
        return result;
    }

    /**
     * @param c Class c
     * @return Returns number of super classes of class c
     */
    private static int getNumberOfSuperClasses(Class<?> c) {
        int superclasses = -1;
        Class<?> cTemp = c;
        do {
            cTemp = cTemp.getSuperclass();
            superclasses++;
        } while (cTemp != null);
        return superclasses;
    }

    /**
     * Shouldn't be used actually... only for some dirty tests/debugs/hacks
     *
     * @param o
     * @param fieldname
     * @param v
     */
    public static void setField(Object o, String fieldname, Object v) throws Exception {
        Class<?> c = o.getClass();
        Field f = null;
        do {
            try {
                f = c.getDeclaredField(fieldname);
            } catch (Exception e) {
                c = c.getSuperclass();
            }
        } while (f == null);
        f.setAccessible(true);
        f.set(o, v);
    }

    /**
     * Shouldn't be used actually... only for some dirty tests/debugs/hacks
     *
     * @param o
     * @param fieldname
     * @param v
     */
    public static Object getField(Object o, String fieldname) throws Exception {
        Class<?> c = o.getClass();
        Field f = null;
        do {
            try {
                f = c.getDeclaredField(fieldname);
            } catch (Exception e) {
                c = c.getSuperclass();
            }
        } while (f == null);
        f.setAccessible(true);
        return f.get(o);
    }
}
