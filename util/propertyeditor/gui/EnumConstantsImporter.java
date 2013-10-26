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
package org.finroc.tools.gui.util.propertyeditor.gui;

import org.finroc.core.datatype.DataTypeReference;

/**
 * @author Max Reichardt
 *
 * Interfaces for classes that can import enum constants.
 */
public interface EnumConstantsImporter {

    /**
     * Import enum constants of specified enum class.
     *
     * @param enumType Enum type to import constants from.
     */
    public void importEnumConstants(DataTypeReference enumType);

    /**
     * Is enum constants importing supported?
     */
    public boolean importEnumConstantsSupported();
}
