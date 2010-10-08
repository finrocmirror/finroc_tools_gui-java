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
package org.finroc.gui.util.propertyeditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author max
 *
 * List of property entries (with possibly multiple attributes)
 */
public class PropertyList<T extends Serializable> extends ArrayList<T> implements PropertyListAccessor<T> {

    /** UID */
    private static final long serialVersionUID = 3604338104379425977L;

    /** Class of entries in this list */
    private Class<T> entryClass;

    /** Maximum entries in this list */
    private int maxEntries;

    public PropertyList(Class<T> entryClass, int maxEntries) {
        this.entryClass = entryClass;
        this.maxEntries = maxEntries;
    }

    @Override
    public int getMaxEntries() {
        return maxEntries;
    }

    @Override
    public Class<T> getElementType() {
        return entryClass;
    }

    @Override
    public List < PropertyAccessor<? >> getElementAccessors(T element) {
        return FieldAccessorFactory.getInstance().createAccessors(element);
    }

    @Override
    public void addElement() {
        try {
            add(entryClass.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeElement(int index) {
        remove(index);
    }
}
