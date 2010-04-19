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
package org.finroc.gui;

import java.awt.Frame;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.finroc.gui.abstractbase.DataModelBase;
import org.finroc.gui.abstractbase.DataModelListener;
import org.finroc.gui.util.embeddedfiles.FileManager;
import org.finroc.gui.util.propertyeditor.PropertiesDialog;


/**
 * @author max
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

    protected void changed(Object o) {
        Widget w = (Widget)o;
        w.fireDataModelEvent(DataModelListener.Event.WidgetPropertiesChanged, w);
    }
}
