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

import java.util.Arrays;
import java.util.Comparator;

import org.finroc.tools.gui.util.embeddedfiles.AbstractFile;
import org.finroc.tools.gui.util.embeddedfiles.AbstractFiles;
import org.finroc.tools.gui.util.embeddedfiles.EmbeddedPaintable;
import org.finroc.tools.gui.util.embeddedfiles.ValidExtensions;
import org.finroc.tools.gui.util.propertyeditor.ComponentFactory;
import org.finroc.tools.gui.util.propertyeditor.PropertiesPanel;
import org.finroc.tools.gui.util.propertyeditor.PropertyAccessor;
import org.finroc.tools.gui.util.propertyeditor.PropertyAccessorAdapter;
import org.finroc.tools.gui.util.propertyeditor.PropertyEditComponent;
import org.finroc.tools.gui.util.propertyeditor.StringEditor;
import org.finroc.core.datatype.DataTypeReference;
import org.finroc.core.datatype.Unit;
import org.finroc.plugins.data_types.StringList;
import org.rrlib.serialization.rtti.DataTypeBase;

/**
 * @author Max Reichardt
 *
 * Component factory for GUI types
 */
public class GuiComponentFactory implements ComponentFactory, Comparator<DataTypeReference> {

    /** Reference to dialog */
    private PropertiesDialog parent;

    public GuiComponentFactory(PropertiesDialog propertiesDialog) {
        parent = propertiesDialog;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public PropertyEditComponent<?> createComponent(PropertyAccessor<?> acc, PropertiesPanel panel) throws Exception {
        Class<?> type = acc.getType();
        PropertyEditComponent wpec = null;
        if (type.equals(EmbeddedPaintable.class)) {
            wpec = new AbstractFileEditor(EmbeddedPaintable.class, EmbeddedPaintable.SUPPORTED_EXTENSIONS, parent);
        } else if (AbstractFile.class.isAssignableFrom(type)) {

            String[] extensions = null;

            // Are valid extensions provided via annotation?
            ValidExtensions ve = acc.getAnnotation(ValidExtensions.class);
            if (ve != null) {
                extensions = ve.value();
            }

            wpec = new AbstractFileEditor((Class <? extends AbstractFile >)type, extensions, parent);
        } else if (AbstractFiles.class.isAssignableFrom(type)) {
            wpec = new AbstractFilesEditor(parent);
        } else if (StringList.class.isAssignableFrom(type)) {
            wpec = new StringEditor(-1);
            acc = new StringListAdapter((PropertyAccessor<StringList>)acc);
        } else if (DataTypeReference.class.isAssignableFrom(acc.getType())) {
            DataTypeReference[] types = new DataTypeReference[DataTypeBase.getTypeCount()];
            for (short i = 0; i < types.length; i++) {
                types[i] = new DataTypeReference(DataTypeBase.getType(i));
            }
            Arrays.sort(types, this);
            wpec = new DataTypeEditor(types, (acc instanceof EnumConstantsImporter) ? (EnumConstantsImporter)acc : null, panel);
        } else if (Unit.class.equals(type)) {
            wpec = new UnitEditor();
        }
        if (wpec != null) {
            wpec.init(acc);
        }
        return wpec;
    }

    /**
     * Allows using StringLists in TextEditor
     */
    public class StringListAdapter extends PropertyAccessorAdapter<StringList, String> {

        public StringListAdapter(PropertyAccessor<StringList> wrapped) {
            super(wrapped, String.class);
        }

        @Override
        public void set(String newValue) throws Exception {
            wrapped.set(new StringList(newValue));
        }

        @Override
        public String get() throws Exception {
            return wrapped.get().toString();
        }
    }

    @Override
    public int compare(DataTypeReference o1, DataTypeReference o2) {
        return o1.toString().compareToIgnoreCase(o2.toString());
    }
}
