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

/**
 * @author Max Reichardt
 *
 * Creates property editor components
 */
public interface ComponentFactory {

    /**
     * @param acc Property accessor
     * @param panel Reference to panel
     * @return Component for editing value of property (is already initialized)
     */
    public PropertyEditComponent<?> createComponent(PropertyAccessor<?> acc, PropertiesPanel panel) throws Exception;
}
