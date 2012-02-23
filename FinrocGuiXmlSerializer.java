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
package org.finroc.tools.gui;

import java.lang.reflect.Field;
import java.util.Collection;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.util.embeddedfiles.AbstractFiles;
import org.finroc.tools.gui.util.embeddedfiles.EmbeddedPaintable;
import org.finroc.tools.gui.util.propertyeditor.PropertyList;
import org.finroc.tools.gui.util.embeddedfiles.ExternalFolder;
import org.finroc.core.datatype.Unit;
import org.finroc.plugins.data_types.StringList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.CollectionConverter;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;

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
        xstream.addImmutableType(java.awt.Color.class);
        xstream.alias("fingui", GUI.class);
        xstream.alias("Window", GUIWindow.class);
        xstream.alias("Panel", GUIPanel.class);
        xstream.alias("InputPortNumeric", WidgetInput.Numeric.class);
        xstream.alias("InputPortCC", WidgetInput.CC.class);
        xstream.alias("InputPort", WidgetInput.Std.class);
        xstream.alias("OutputPortNumeric", WidgetOutput.Numeric.class);
        xstream.alias("OutputPortCC", WidgetOutput.CC.class);
        xstream.alias("OutputPort", WidgetOutput.Std.class);
        xstream.alias("BlackboardPort", WidgetOutput.Blackboard.class);
        xstream.alias("EmbeddedPaintable", EmbeddedPaintable.class);
        xstream.alias("ExternalFolder", ExternalFolder.class);
        xstream.addImplicitCollection(DataModelBase.class, "children");
        xstream.registerConverter(new WidgetConverter(xstream.getMapper(), xstream.getReflectionProvider()));
        xstream.registerConverter(new PanelConverter(xstream.getMapper(), xstream.getReflectionProvider()));
        xstream.registerConverter(new FinguiCollectionConverter(xstream.getMapper()));
        xstream.registerConverter(new UnitConverter());
        xstream.processAnnotations(GUIPanel.class);
        //xstream.addImplicitCollection(GUIPanel.class, "widgets");
        //xstream.registerConverter(new WidgetConverter(xstream.getMapper(), new AnnotationProvider()));
        return xstream;
    }

    /**
     * Converter for StringList mainly
     */
    @SuppressWarnings("rawtypes")
    static class FinguiCollectionConverter extends CollectionConverter {

        public FinguiCollectionConverter(Mapper mapper) {
            super(mapper);
        }

        @Override
        public boolean canConvert(Class type) {
            return type.equals(StringList.class) || type.equals(PropertyList.class) || type.equals(WidgetPorts.class) || AbstractFiles.class.isAssignableFrom(type);
        }

        @Override
        public void populateCollection(HierarchicalStreamReader reader, UnmarshallingContext context, Collection collection) {
            super.populateCollection(reader, context, collection);
        }
    }

    /**
     * Converter for GUI panels. More robust when widgets are missing than standard converter.
     */
    @SuppressWarnings("rawtypes")
    static class PanelConverter extends AbstractReflectionConverter {

        public PanelConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
            super(mapper, reflectionProvider);
        }

        @Override
        public boolean canConvert(Class type) {
            return type.equals(GUIPanel.class);
        }

        @Override
        public Object doUnmarshal(Object result, HierarchicalStreamReader reader, UnmarshallingContext context) {
            GUIPanel panel = (GUIPanel)result;

            panel.setName(reader.getAttribute("name"));

            while (reader.hasMoreChildren()) {
                reader.moveDown();

                String originalNodeName = reader.getNodeName();
                Class type = null;
                try {
                    type = mapper.realClass(originalNodeName);
                } catch (Exception e) {
                    System.out.println("Warning: Cannot instantiate widget of unknown type '" + originalNodeName + "'");
                }

                if (type != null) {
                    Widget value = (Widget)context.convertAnother(result, type);
                    panel.ensureChildVectorIsInstantiated();
                    panel.add(value);
                }

                reader.moveUp();
            }

            return result;
        }
    }

    /**
     * Mapper class for widgets
     */
    @SuppressWarnings("rawtypes")
    static class WidgetMapper extends MapperWrapper {

        public WidgetMapper(Mapper wrapped) {
            super(wrapped);
        }

        @Override
        public ImplicitCollectionMapping getImplicitCollectionDefForFieldName(Class itemType, String fieldName) {
            if (Widget.class.isAssignableFrom(itemType)) {
                return null;
            }
            return super.getImplicitCollectionDefForFieldName(itemType, fieldName);
        }

        @Override
        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
            if (DataModelBase.class.isAssignableFrom(definedIn) && fieldName.equals("children")) {
                return false;
            }
            return super.shouldSerializeMember(definedIn, fieldName);
        }

        @Override
        public Class realClass(String elementName) {
            if (Character.isLowerCase(elementName.charAt(0))) {
                return null; // This must be a field that no longer exists
            }
            return super.realClass(elementName);
        }
    }

    /**
     * Special, more robust converter for widgets
     */
    @SuppressWarnings("rawtypes")
    static class WidgetConverter extends AbstractReflectionConverter {

        HierarchicalStreamReader reader;
        FinguiCollectionConverter converter;

        public WidgetConverter(Mapper mapper, ReflectionProvider reflectionProvider) {
            super(new WidgetMapper(mapper), reflectionProvider);
            converter = new FinguiCollectionConverter(mapper);
        }

        @Override
        public boolean canConvert(Class type) {
            return Widget.class.isAssignableFrom(type);
        }

        @Override
        protected Object instantiateNewInstance(HierarchicalStreamReader reader, UnmarshallingContext context) {
            String attributeName = mapper.aliasForSystemAttribute("resolves-to");
            String readResolveValue = attributeName == null ? null : reader.getAttribute(attributeName);
            Object currentObject = context.currentObject();
            if (currentObject != null) {
                return currentObject;
            } else if (readResolveValue != null) {
                try {
                    return mapper.realClass(readResolveValue).newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Could not instantiate class " + mapper.realClass(readResolveValue).getSimpleName(), e);
                }
            } else {
                try {
                    return context.getRequiredType().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Could not instantiate class " + context.getRequiredType(), e);
                }
            }
        }

        @Override
        public Object doUnmarshal(Object result, HierarchicalStreamReader reader, UnmarshallingContext context) {
            this.reader = reader;
            return super.doUnmarshal(result, reader, context);
        }

        @Override
        protected Object unmarshallField(UnmarshallingContext context, Object result, Class type, Field field) {
            if (WidgetPorts.class.isAssignableFrom(field.getType()) || PropertyList.class.isAssignableFrom(field.getType())) {
                try {
                    field.setAccessible(true);
                    Collection c = (Collection)field.get(result);
                    c.clear();
                    converter.populateCollection(reader, context, c);
                    if (WidgetPorts.class.isAssignableFrom(field.getType())) {
                        ((WidgetPorts<?>)c).initialize();
                    }
                    return c;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return super.unmarshallField(context, result, type, field);
        }

    }

    static class UnitConverter implements Converter {

        @SuppressWarnings("rawtypes")
        @Override
        public boolean canConvert(Class c) {
            return Unit.class.equals(c);
        }

        @Override
        public void marshal(Object unit, HierarchicalStreamWriter writer, MarshallingContext context) {
            writer.setValue(unit.toString());
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            String u = reader.getValue();
            try {
                return Unit.getUnit(u);
            } catch (Exception e) {
                if (u != null && u.length() > 0) {
                    System.out.println("Warning: Do not know unit '" + u + "'.");
                }
                return null;
            }
        }
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
