//
// You received this file as part of Finroc
// A Framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui.util.propertyeditor;

import java.lang.annotation.Annotation;

/**
 * @author Max Reichardt
 *
 * Abstract interface for accessing a property that can be edited.
 */
public interface PropertyAccessor<T> {

    /**
     * @return Get type of object to be edited
     */
    public Class<T> getType();

    /**
     * @return Current value (should be a copy, if value is not immutable/passed by value)
     */
    public T get() throws Exception;

    /**
     * @param newValue Value to set property to (may be the same instance that was acquired with get)
     */
    public void set(T newValue) throws Exception;

    /**
     * @return Property name
     */
    public String getName();

    /**
     * @param ann Annotation class
     * @return Does property have specified annotation?
     */
    public <A extends Annotation> A getAnnotation(Class<A> ann);

    /**
     * @return Is property modifiable? (Usually true - otherwise editor is disabled and only shows value)
     */
    public boolean isModifiable();
}
