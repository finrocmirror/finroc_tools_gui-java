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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.finroc.gui.commons.Util;
import org.finroc.gui.util.ObjectCloner;
import org.finroc.gui.util.embeddedfiles.AbstractFile;
import org.finroc.gui.util.embeddedfiles.AbstractFiles;
import org.finroc.gui.util.embeddedfiles.EmbeddedPaintable;
import org.finroc.gui.util.embeddedfiles.ValidExtensions;
import org.finroc.plugin.datatype.StringList;


/**
 * @author max
 *
 * Component for editing one property of a widget.
 *
 * Values should be either copied or immutable (for multi-object editing)
 */
public abstract class PropertyEditComponent<T> extends JPanel {

    /** UID */
    private static final long serialVersionUID = 908490162821129814L;

    protected final int LABELWIDTH = 150;
    protected final int TEXTFIELDWIDTH = 150;

    /** current value */
    private T curValue;

    /** Property that is changed */
    Field property;

    /** Widgets that are edited */
    private List<Object> widgets = new ArrayList<Object>();

    /** Reference to dialog */
    private PropertiesDialog parent;

    /** writes changes to widget
     * @param curValue */
    public void applyChanges() {
        try {
            T newValue = getCurEditorValue();
            if (curValue == null || !curValue.equals(newValue)) {
                for (Object o : widgets) {
                    property.set(o, ObjectCloner.clone(newValue));
                }
            }
            curValue = ObjectCloner.clone(newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /** get current value of component (for saving changes) */
    public abstract T getCurEditorValue();


    @SuppressWarnings("rawtypes")
    public static PropertyEditComponent<?> getInstance(Field f, Object tmp, PropertiesDialog parent) throws ClassNotFoundException {
        Class<?> type = f.getType();
        PropertyEditComponent wpec = null;
        if (type.equals(String.class)) {
            wpec = new StringEditor();
        } else if (Number.class.isAssignableFrom(type) || type.equals(int.class) || type.equals(double.class) || type.equals(float.class) || type.equals(long.class) || type.equals(short.class) || type.equals(byte.class)) {
            wpec = new NumberEditor();
        } else if (type.equals(Color.class)) {
            wpec = new ColorEditor();
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            wpec = new BooleanEditor();
        } else if (type.equals(Font.class)) {
            wpec = new FontEditor();
        } else if (type.equals(EmbeddedPaintable.class)) {
            wpec = new AbstractFileEditor(EmbeddedPaintable.class, EmbeddedPaintable.SUPPORTED_EXTENSIONS);
        } else if (AbstractFile.class.isAssignableFrom(type)) {

            String[] extensions = null;

            // Are valid extensions provided via annotation?
            ValidExtensions ve = f.getAnnotation(ValidExtensions.class);
            if (ve != null) {
                extensions = ve.value();
            }

            wpec = new AbstractFileEditor((Class <? extends AbstractFile >)type, extensions);
        } else if (AbstractFiles.class.isAssignableFrom(type)) {
            wpec = new AbstractFilesEditor();
        } else if (StringList.class.isAssignableFrom(type)) {
            wpec = new StringListEditor();
        } else if (PropertyList.class.isAssignableFrom(type)) {
            wpec = new PropertyListEditor();
        } else if (Enum.class.isAssignableFrom(type)) {
            wpec = new EnumEditor();
        }

        if (wpec == null) {
            throw new ClassNotFoundException("No WidgetPropertyEditComponent for Type " + type.getSimpleName() + " available");
        }
        wpec.property = f;
        wpec.widgets.add(tmp);
        wpec.parent = parent;
        wpec.curValue = wpec.getCurWidgetValue();
        wpec.createAndShow();
        return wpec;
    }

    public void addObject(Object o) {
        widgets.add(o);
    }

    protected Class<?> getPropertyType() {
        return property.getType();
    }

    /** create and init components */
    protected abstract void createAndShow();


    @SuppressWarnings("unchecked")
    public T getCurWidgetValue() {
        try {
            return ObjectCloner.clone((T)property.get(widgets.get(0)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPropertyName() {
        return Util.asWords(property.getName());
    }

    protected PropertiesDialog getParentDialog() {
        return parent;
    }

    protected void createStdLayoutWith(JComponent c) {
        JLabel label = new JLabel(getPropertyName());
        label.setMinimumSize(new Dimension(LABELWIDTH, 0));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        setLayout(new GridLayout(1, 2));
        add(label);
        JPanel jp = new JPanel();
        jp.setBorder(BorderFactory.createEmptyBorder(3, 11, 3, 5));
        jp.setLayout(new BorderLayout());
        jp.add(c, BorderLayout.CENTER);
        add(jp);
    }

    public void createAndShowMinimal(T object) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }
}
