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
package org.finroc.gui.util.propertyeditor;

import java.util.List;

/**
 * @author max
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
