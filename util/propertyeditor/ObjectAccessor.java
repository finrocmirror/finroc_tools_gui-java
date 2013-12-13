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

import org.rrlib.serialization.Serialization;

/**
 * @author Max Reichardt
 *
 * Property accessor for single object.
 * (Kind of a loopback accessor wrapper)
 *
 * (Note, that writing changes back to object (set() method) is difficult to generalize.
 *  Therefore, this accessor is unmodifiable by default. Override for different behaviour)
 */
public class ObjectAccessor<T> implements PropertyAccessor<T> {

    /** Wrapped */
    protected final T wrapped;

    /** Name of property */
    protected final String name;

    public ObjectAccessor(String name, T wrapped) {
        this.name = name;
        this.wrapped = wrapped;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getType() {
        return (Class<T>)wrapped.getClass();
    }

    @Override
    public T get() throws Exception {
        return Serialization.deepCopy(wrapped);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> ann) {
        return null;
    }

    @Override
    public void set(T newValue) throws Exception {
        throw new Exception("Unsupported");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isModifiable() {
        return false;
    }
}
