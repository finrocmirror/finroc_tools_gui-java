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

import java.util.List;

/**
 * @author Max Reichardt
 *
 * Abstract interface for an editable list.
 * (Usually implemented by wrapper around a list class)
 */
public interface PropertyListAccessor<E> {

    /**
     * @return Type of list elements
     */
    public Class<E> getElementType();

    /**
     * @return Maximum number of entries in this list
     */
    public int getMaxEntries();

    /**
     * @return Size of list
     */
    public int size();

    /**
     * @param index Entry index
     * @return entry
     */
    public E get(int index);

    /**
     * @param element Element type
     * @return List with Accessors for this element
     */
    public List < PropertyAccessor<? >> getElementAccessors(E element);

    /**
     * Add element at end of list
     */
    public void addElement();

    /**
     * Remove entry with specified index
     *
     * @param index Entry index
     */
    public void removeElement(int index);
}
