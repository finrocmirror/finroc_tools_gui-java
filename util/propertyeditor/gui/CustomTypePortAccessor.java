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
package org.finroc.tools.gui.util.propertyeditor.gui;

import java.lang.reflect.Field;

import org.finroc.core.datatype.DataTypeReference;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.util.propertyeditor.FieldAccessor;

/**
 * @author Max Reichardt
 *
 */
public class CustomTypePortAccessor extends FieldAccessor implements EnumConstantsImporter {

    public CustomTypePortAccessor(Field property, Object... objects) {
        super(property, objects);
    }

    @Override
    public Class<DataTypeReference> getType() {
        return DataTypeReference.class;
    }

    @Override
    public DataTypeReference get() {
        try {
            return ((WidgetOutput.Custom)property.get(objects.get(0))).getType();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void set(Object newValue) {
        try {
            for (Object o : objects) {
                ((WidgetOutput.Custom)property.get(o)).changeDataType((DataTypeReference)newValue);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void importEnumConstants(DataTypeReference enumType) {
        for (Object o : objects) {
            if (o instanceof EnumConstantsImporter) {
                ((EnumConstantsImporter)o).importEnumConstants(enumType);
            }
        }
    }

    @Override
    public boolean importEnumConstantsSupported() {
        for (Object o : objects) {
            if (o instanceof EnumConstantsImporter) {
                if (((EnumConstantsImporter)o).importEnumConstantsSupported()) {
                    return true;
                }
            }
        }
        return false;
    }
}
