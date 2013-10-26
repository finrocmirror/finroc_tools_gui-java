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
package org.finroc.tools.gui;

import java.awt.Frame;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;
import org.finroc.tools.gui.util.embeddedfiles.FileManager;
import org.finroc.tools.gui.util.propertyeditor.FieldAccessor;
import org.finroc.tools.gui.util.propertyeditor.FieldAccessorFactory;
import org.finroc.tools.gui.util.propertyeditor.PropertyAccessor;
import org.finroc.tools.gui.util.propertyeditor.gui.CustomTypePortAccessor;
import org.finroc.tools.gui.util.propertyeditor.gui.PropertiesDialog;


/**
 * @author Max Reichardt
 *
 * Property Editor for widgets
 */
public class WidgetPropertiesDialog extends PropertiesDialog {

    /** UID */
    private static final long serialVersionUID = -5469189977864562246L;

    public WidgetPropertiesDialog(Frame owner, Widget w, FileManager efm) {
        super(owner, w, efm, true);
    }

    public WidgetPropertiesDialog(JFrame owner, List<Widget> sel, FileManager efm) {
        super(owner, new ArrayList<Object>(sel), efm, true);
    }

    protected void changed(Object o) {
        Widget w = (Widget)o;
        w.fireDataModelEvent(DataModelListener.Event.WidgetPropertiesChanged, w);
    }

    @Override
    protected List < PropertyAccessor<? >> getProperties(List<Object> o) {
        return new WidgetFieldAccessorFactory().createAccessors(o);
    }

    private class WidgetFieldAccessorFactory extends FieldAccessorFactory {

        @Override
        protected boolean ignoreField(Field f) {
            if (WidgetPort.class.isAssignableFrom(f.getType())) {
                return true;
            }
            if (WidgetPorts.class.isAssignableFrom(f.getType())) {
                return true;
            }
            if (f.getDeclaringClass().equals(DataModelBase.class)) {
                return true;
            }
            return false;
        }

        @Override
        protected FieldAccessor customFieldHandling(Field f, Object x) {
            if (f.getType() == WidgetOutput.Custom.class) {
                return new CustomTypePortAccessor(f, x);
            }
            return null;
        }
    }

}
