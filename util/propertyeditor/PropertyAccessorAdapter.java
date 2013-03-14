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

import java.lang.annotation.Annotation;

/**
 * @author Max Reichardt
 *
 * Base class for adapter classes that allow using type A
 * in Editor for class B.
 *
 * Since values are cloned in wrapped accessor, adapter doesn't need
 * to clone values again.
 */
public abstract class PropertyAccessorAdapter<A, B> implements PropertyAccessor<B> {

    /** Wrapped type A accessor */
    protected PropertyAccessor<A> wrapped;

    /** Type that editor accepts */
    private Class<B> editorClass;

    public PropertyAccessorAdapter(PropertyAccessor<A> wrapped, Class<B> editorClass) {
        this.wrapped = wrapped;
        this.editorClass = editorClass;
    }

    @Override
    public Class<B> getType() {
        return editorClass;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public <C extends Annotation> C getAnnotation(Class<C> ann) {
        return wrapped.getAnnotation(ann);
    }

    @Override
    public boolean isModifiable() {
        return wrapped.isModifiable();
    }
}
