/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2011 Max Reichardt
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
        try {
            byte i = 0;
            while (true) {
                r.add(Unit.getUnit(i));
                i++;
            }
        } catch (Exception e) {}
        Unit[] array = r.toArray(new Unit[0]);
        Arrays.sort(array, new Comparator<Unit>() {
            @Override
            public int compare(Unit o1, Unit o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return array;
    }

    public UnitEditor() {
        super(getValues());
    }

}
