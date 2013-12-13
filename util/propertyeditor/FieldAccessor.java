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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.finroc.tools.gui.commons.Util;
import org.rrlib.serialization.Serialization;

/**
 * @author Max Reichardt
 *
 * Property Accessor for fields of class
 */
@SuppressWarnings("rawtypes")
public class FieldAccessor implements PropertyAccessor {

    /** Property that is changed */
    protected final Field property;

    /** Objects that are edited */
    protected List<Object> objects = new ArrayList<Object>();

    public FieldAccessor(Field property, Object... objects) {
        this.property = property;
        for (Object o : objects) {
            this.objects.add(o);
        }
    }

    @Override
    public Class getType() {
        return property.getType();
    }

    @Override
    public Object get() {
        try {
            return Serialization.deepCopy(property.get(objects.get(0)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void set(Object newValue) {
        try {
            for (Object o : objects) {
                property.set(o, Serialization.deepCopy(newValue));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return Util.asWords(property.getName());
    }

    /**
     * Add object that is edited
     *
     * @param o Object
     */
    public void addObject(Object o) {
        objects.add(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Annotation getAnnotation(Class ann) {
        return property.getAnnotation(ann);
    }

    @Override
    public boolean isModifiable() {
        return !Modifier.isFinal(property.getModifiers());
    }
}
