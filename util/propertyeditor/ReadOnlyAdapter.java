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

/**
 * @author max
 *
 * Wraps Property accessor read-only
 */
public class ReadOnlyAdapter<T> extends PropertyAccessorAdapter<T, T> {

    public ReadOnlyAdapter(PropertyAccessor<T> wrapped) {
        super(wrapped, wrapped.getType());
    }

    @Override
    public T get() throws Exception {
        return wrapped.get();
    }

    @Override
    public void set(T newValue) throws Exception {
        throw new Exception("Read-only");
    }

    @Override
    public boolean isModifiable() {
        return false;
    }
}
