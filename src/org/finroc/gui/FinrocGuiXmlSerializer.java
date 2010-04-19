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

import org.finroc.gui.abstractbase.DataModelBase;

import com.thoughtworks.xstream.XStream;

/**
 * @author max
 *
 * This class sets up a XStream-Serializer configured to create nice XML-Code for
 * FinrocGUI
 *
 * XStream is from http://xstream.codehaus.org/
 */
public class FinrocGuiXmlSerializer {

    private static XStream xstream = null;

    public static XStream getInstance() {

        // already initialized?
        if (xstream != null) {
            return xstream;
        }

        // initialize
        xstream = new XStream(/*new DomDriver()*/);
        xstream.alias("fingui", GUI.class);
        xstream.alias("Window", GUIWindow.class);
        xstream.alias("Panel", GUIPanel.class);
        xstream.addImplicitCollection(DataModelBase.class, "children");
        //xstream.addImplicitCollection(GUIPanel.class, "widgets");
        //xstream.registerConverter(new WidgetConverter(xstream.getMapper(), new AnnotationProvider()));
        return xstream;
    }
}

/**
 * @author max
 *
 * Generic converter for widgets
 */
/*class WidgetConverter extends AnnotationReflectionConverter {

    public WidgetConverter(Mapper mapper, AnnotationProvider annotationProvider) {
        super(mapper, new WidgetReflectionProvider(), annotationProvider);
    }

    // für alle Unterklassen von Widget einsetzen
    public boolean canConvert(Class clazz) {
        return Widget.class.isAssignableFrom(clazz);
    }

    // damit bounds-Feld aktualisiert wird
    public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
        ((Widget)value).updateBeforeSave();
        super.marshal(value, writer, context);
    }

    // damit Instanz über normalen Konstruktor erstellt wird
    protected Object instantiateNewInstance(HierarchicalStreamReader reader, UnmarshallingContext context) {

        try {
        // from XStream
            String readResolveValue = reader.getAttribute(mapper.aliasForAttribute("resolves-to"));
            Object currentObject = context.currentObject();
            if (currentObject != null) {
                return currentObject;
            } else if (readResolveValue != null) {
                return mapper.realClass(readResolveValue).newInstance(); // changed
            } else {
                return context.getRequiredType().newInstance(); // changed
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate Widget");
        }
    }
}*/

/**
 * @author max
 *
 * Replace method of Sun14ReflectionProvider, to not visit fields of Widget's superclasses
 */
/*class WidgetReflectionProvider extends Sun14ReflectionProvider {
    public void visitSerializableFields(Object object, ReflectionProvider.Visitor visitor) {
        for (Iterator iterator = fieldDictionary.serializableFieldsFor(object.getClass()); iterator.hasNext();) {
            Field field = (Field) iterator.next();

            // Skip Widget and all superclasses
            if (!Widget.class.isAssignableFrom(field.getDeclaringClass())) {
                continue;
            }

            if (!fieldModifiersSupported(field)) {
                continue;
            }
            validateFieldAccess(field);
            try {
                Object value = field.get(object);
                visitor.visit(field.getName(), field.getType(), field.getDeclaringClass(), value);
            } catch (IllegalArgumentException e) {
                throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
            } catch (IllegalAccessException e) {
                throw new ObjectAccessException("Could not get field " + field.getClass() + "." + field.getName(), e);
            }
        }
    }
}*/
