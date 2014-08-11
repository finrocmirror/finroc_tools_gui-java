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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.finroc.core.datatype.Unit;
import org.finroc.tools.gui.util.propertyeditor.ComboBoxEditor;

/**
 * @author Max Reichardt
 *
 * Editor for data types: Adds button to editor that lets user
 * import enum constants.
 */
public class UnitEditor extends ComboBoxEditor<Unit> {

    /** UID */
    private static final long serialVersionUID = -8458082858218885773L;

    static Unit[] getValues() {
        ArrayList<Unit> r = new ArrayList<Unit>();
        r.add(null);
        Unit.getAllUnits(r);
        Unit[] array = r.toArray(new Unit[0]);
        Arrays.sort(array, new Comparator<Unit>() {
            @Override
            public int compare(Unit o1, Unit o2) {
                if (o1 == null) {
                    return o2 == null ? 0 : -1;
                } else if (o2 == null) {
                    return 1;
                }
                return o1.toString().compareToIgnoreCase(o2.toString());
            }
        });
        return array;
    }

    public UnitEditor() {
        super(getValues());
    }

}
