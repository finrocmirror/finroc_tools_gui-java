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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;
import org.finroc.tools.gui.commons.Util;
import org.finroc.tools.gui.commons.reflection.ReflectionCallback;
import org.finroc.tools.gui.commons.reflection.ReflectionHelper;
import org.finroc.tools.gui.themes.Theme;
import org.finroc.tools.gui.themes.Themes;
import org.finroc.tools.gui.util.embeddedfiles.AbstractFile;
import org.finroc.tools.gui.util.embeddedfiles.AbstractFiles;
import org.finroc.tools.gui.util.embeddedfiles.FileManager;
import org.finroc.tools.gui.util.embeddedfiles.HasEmbeddedFiles;
import org.finroc.tools.gui.util.propertyeditor.NotInPropertyEditor;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;
import org.rrlib.serialization.BinaryInputStream;
import org.rrlib.serialization.BinaryOutputStream;
import org.rrlib.serialization.MemoryBuffer;
import org.rrlib.serialization.ObjectFieldSerializer;
import org.rrlib.serialization.Serialization;
import org.rrlib.serialization.Serialization.DataEncoding;
import org.rrlib.xml.XMLNode;

import org.finroc.core.FrameworkElement;
import org.finroc.core.FrameworkElement.ChildIterator;
import org.finroc.core.LockOrderLevels;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.ThreadLocalCache;


/**
 * @author Max Reichardt
 *
 * This is the base class for all widgets used in the GUI
 */

public abstract class Widget extends DataModelBase < GUI, GUIPanel, WidgetPort<? >> implements ReflectionCallback<Field>, HasEmbeddedFiles {

    /** UID */
    private static final long serialVersionUID = -5622497094531263503L;

    /** for serialization */
    @NotInPropertyEditor
    private Rectangle bounds;

    /** Label of widget (optional) */
    private String label;

    /** Color of Label */
    private Color labelColor = getDefaultColor(Theme.DefaultColor.LABEL);

    /** Background color of widget (at least of the border) */
    private Color background = getDefaultColor(Theme.DefaultColor.BACKGROUND);

    /** temporary list */
    @NotInPropertyEditor
    private List<AbstractFile> embeddedFiles;

    /** Single instance of XML serialization class */
    private static final XMLSerializer XML_SERIALIZER = new XMLSerializer();

    public Widget() {
        super(null);
        try {
            initPorts();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setDefaultColors();
    }

    /**
     * called after deserialization. Restores transient attributes.
     */
    public void restore(GUIPanel parent) {
        try {
            initPorts();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        super.restore(parent);
        parent.getFrameworkElement().addChild(frameworkElement);
        frameworkElement.init();
        //getRoot().getJmcagui().addConnectionListener(this);

        // remove obsolete finroc framework elements
        ChildIterator ci = new ChildIterator(frameworkElement);
        FrameworkElement port = null;
        while ((port = ci.next()) != null) {
            boolean found = false;
            for (WidgetPort<?> wp : children) {
                if (wp.getFrameworkElement() == port) {
                    found = true;
                }
            }
            if (!found) {
                port.managedDelete();
            }
        }
    }

    /**
     * Create graphical representation (JComponent) of widget to diplay on screen.
     *
     * @return Graphical representation
     */
    public WidgetUI createUI() {
        ThreadLocalCache.get();
        WidgetUI result = createWidgetUI();
        result.setBounds(bounds);
        return result;
    }

    /**
     * Create graphical representation (JComponent) of widget to diplay on screen.
     * Has to be implemented by subclass.
     *
     * @return Graphical representation
     */
    protected abstract WidgetUI createWidgetUI();

    public String toString() {
        if (label == null || label == "") {
            return getClass().getSimpleName();
        } else {
            return label + " (" + getClass().getSimpleName() + ")";
        }
    }

    /**
     * init Ports
     */
    protected void initPorts() {
        children.clear();
        try {
            ReflectionHelper.visitAllFields(getClass(), false, true, this, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        fireDataModelEvent(DataModelListener.Event.ChildrenChanged, this);
    }

    public synchronized Collection<AbstractFile> getEmbeddedFiles() {
        embeddedFiles = new ArrayList<AbstractFile>();
        try {
            ReflectionHelper.visitAllFields(getClass(), true, true, this, 1);
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, this, e);
        }
        return embeddedFiles;
    }

    public void reflectionCallback(Field f, int id) throws Exception {

        if (id == 0) { // called from initPorts()
            if (WidgetPort.class.isAssignableFrom(f.getType())) {
                WidgetPort<?> p = (WidgetPort<?>)f.get(this);
                if (p == null) {
                    p = (WidgetPort<?>)f.getType().getConstructor().newInstance();
                    p.setDescription(Util.asWords(f.getName()));
                }
                //p.restore(this);
                f.set(this, p);
                p.restore(this);
                children.add(p);
            } else if (WidgetPorts.class.isAssignableFrom(f.getType())) {
                List<?> l = (List<?>)f.get(this);
                if (l != null) {
                    for (Object o : l) {
                        if (o == null) {
                            continue;
                        }
                        WidgetPort<?> p = (WidgetPort<?>)o;
                        //p.restore(this);
                        p.restore(this);
                        children.add(p);
                    }
                }
            }

        } else if (id == 1) { // called from getEmbeddedFiles
            if (AbstractFile.class.isAssignableFrom(f.getType())) {
                AbstractFile ef = (AbstractFile)f.get(this);
                if (ef != null) {
                    embeddedFiles.add(ef);
                }
            } else if (AbstractFiles.class.isAssignableFrom(f.getType())) {
                AbstractFiles<?> efs = (AbstractFiles<?>)f.get(this);
                if (efs != null) {
                    embeddedFiles.addAll(efs);
                }
            }
        }
    }

//  // notify all ports
//  public void connectionEvent(Object source, Event e) {
//      if (e == Event.interfaceUpdated) {
//          for (WidgetPort<?> p : children) {
//              p.interfaceUpdated();
//          }
//      }
//  }


    // below: bounds related stuff
    public Rectangle getBounds() {
        return bounds;
    }

    public void setBounds(Rectangle bounds) {
        if ((this.bounds != null) && (this.bounds.equals(bounds))) {
            return;
        }
        this.bounds = bounds;
        fireDataModelEvent(DataModelListener.Event.widgetBoundsChanged, this);
    }

    public void setSize(Dimension dimension) {
        setBounds(new Rectangle(bounds.getLocation(), dimension));
    }

    public void setLocation(Point p) {
        setBounds(new Rectangle(p, bounds.getSize()));
    }

    public void setParent(GUIPanel parent) {
        this.parent = parent;
    }

    public void setLocation(int i, int j) {
        setBounds(new Rectangle(i, j, bounds.width, bounds.height));
    }

    public Point getLocation() {
        return bounds.getLocation();
    }

    // below: appearance-related getters and setters
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Color getBackground() {
        return background;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public void setLabelColor(Color labelColor) {
        this.labelColor = labelColor;
    }

    /** may be overriden */
    protected void setDefaultColors() {
    }

    /**
     * This method needs to be overridden by subclass.
     * It queries PortCreationInfo for a specific port.
     *
     * Ratio: (Even if a GUI is saved and reloaded - the type of port may be modified)
     *
     * @param suggestion Suggested PortCreationInfo (you may derive from this; if this is not a number port, at least the type needs to be set)
     * @param forPort Port that is created (compare it with any port defined in class)
     * @return PortCreationInfo to create port with (null means use suggestion)
     */
    protected abstract PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort);

    @Override
    protected FrameworkElement createFrameworkElement() {
        return new FrameworkElement(null, getClass().getSimpleName(), 0, LockOrderLevels.LEAF_GROUP);
    }

    /**
     * Convenience method to get default colors from theme
     *
     * @param dc Element type
     * @return Default color for element type
     */
    public static Color getDefaultColor(Theme.DefaultColor dc) {
        return Themes.getCurTheme().getDefaultColor(dc);
    }

    /**
     * Use alternative background and label colors
     */
    public void useAlternativeColors() {
        setBackground(getDefaultColor(Theme.DefaultColor.ALTERNATIVE_BACKGROUND));
        setLabelColor(getDefaultColor(Theme.DefaultColor.ALTERNATIVE_LABEL));
    }

    @Override
    public void serialize(XMLNode node) throws Exception {
        serialize(node.addChildNode("bounds"), bounds);
        node.addChildNode("label").setContent(label);
        serialize(node.addChildNode("labelColor"), labelColor);
        serialize(node.addChildNode("background"), background);

        if (embeddedFiles != null) {
            XMLNode fileNode = node.addChildNode("embeddedFiles"); // TODO: it's ugly that embedded files are serialized twice; change this, when XStream is discarded
            for (AbstractFile file : embeddedFiles) {
                FileManager.serializeFile(fileNode, file);
            }
        }

        XML_SERIALIZER.serialize(node, this);
    }

    @Override
    public void deserialize(XMLNode node) throws Exception {
        super.children.clear();
        for (XMLNode child : node.children()) {
            try {
                if (child.getName().equals("bounds")) {
                    bounds = deserializeRectangle(child);
                } else if (child.getName().equals("label")) {
                    label = child.getTextContent();
                } else if (child.getName().equals("labelColor")) {
                    labelColor = deserializeColor(child);
                } else if (child.getName().equals("background")) {
                    background = deserializeColor(child);
                } else if (child.getName().equals("embeddedFiles")) {
                    embeddedFiles = new ArrayList<AbstractFile>();
                    for (XMLNode fileNode : child.children()) {
                        embeddedFiles.add(FileManager.deserializeFile(fileNode));
                    }
                }
            } catch (Exception e) {
                Log.log(LogLevel.ERROR, e);
            }
        }

        try {
            XML_SERIALIZER.deserialize(node, this, true);
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, e);
        }

        // Resolve embedded file references (necessary for loading XStream-generated files with references - pretty ugly, but for compatibility)
        List<AbstractFile> oldEmbeddedFiles = embeddedFiles;
        for (AbstractFile file : getEmbeddedFiles()) {
            if (file.getDeserializationReference() != null) {
                int referenceIndex = 0;
                if (file.getDeserializationReference().endsWith("]")) {
                    String temp = file.getDeserializationReference().substring(file.getDeserializationReference().lastIndexOf('[') + 1);
                    referenceIndex = Integer.parseInt(temp.substring(0, temp.length() - 1)) - 1;
                }
                //Serialization.deepCopy(oldEmbeddedFiles.get(referenceIndex), file);
                MemoryBuffer buf = new MemoryBuffer();
                BinaryOutputStream stream = new BinaryOutputStream(buf);
                stream.writeObject(oldEmbeddedFiles.get(referenceIndex), oldEmbeddedFiles.get(referenceIndex).getClass(), DataEncoding.XML);
                stream.close();
                BinaryInputStream istream = new BinaryInputStream(buf);
                istream.readObject(file, file.getClass(), DataEncoding.XML);
            }
        }
    }

    /**
     * ObjectFieldSerializer customized for widgets
     */
    public static class XMLSerializer extends ObjectFieldSerializer {

        @Override
        public ArrayList<Field> getFieldsToSerialize(Class<?> c) {
            ArrayList<Field> result = new ArrayList<Field>();
            for (Field field : c.getFields()) {
                if ((!Modifier.isTransient(field.getModifiers())) && (!Modifier.isStatic(field.getModifiers()))) {
                    result.add(field);
                }
            }
            if (!c.getSuperclass().equals(Widget.class)) {
                result.addAll(getFieldsToSerialize(c.getSuperclass()));
            }
            return result;
        }

        @Override
        protected void serializeFieldValue(XMLNode node, Object object) throws Exception {
            if (object instanceof Color) {
                serialize(node, (Color)object);
            } else if (object instanceof Rectangle) {
                serialize(node, (Rectangle)object);
            } else {
                super.serializeFieldValue(node, object);
            }
        }

        @Override
        protected Object deserializeFieldValue(XMLNode node, Object deserializeTo, Class<?> type) throws Exception {
            if (type.equals(Color.class)) {
                return deserializeColor(node);
            } else if (type.equals(Rectangle.class)) {
                return deserializeRectangle(node);
            } else {
                return super.deserializeFieldValue(node, deserializeTo, type);
            }
        }
    }

    // Helper methods for convenient serialization of e.g. AWT elements //

    public static void serialize(XMLNode node, Rectangle rect) throws Exception {
        node.addChildNode("x").setContent("" + rect.x);
        node.addChildNode("y").setContent("" + rect.y);
        node.addChildNode("width").setContent("" + rect.width);
        node.addChildNode("height").setContent("" + rect.height);
    }

    public static Rectangle deserializeRectangle(XMLNode node) throws Exception {
        Rectangle rect = new Rectangle();
        for (XMLNode child : node.children()) {
            if (child.getName().equals("x")) {
                rect.x = Integer.parseInt(child.getTextContent());
            }
            if (child.getName().equals("y")) {
                rect.y = Integer.parseInt(child.getTextContent());
            }
            if (child.getName().equals("width")) {
                rect.width = Integer.parseInt(child.getTextContent());
            }
            if (child.getName().equals("height")) {
                rect.height = Integer.parseInt(child.getTextContent());
            }
        }
        return rect;
    }

    public static void serialize(XMLNode node, Color color) throws Exception {
        node.addChildNode("red").setContent("" + color.getRed());
        node.addChildNode("green").setContent("" + color.getGreen());
        node.addChildNode("blue").setContent("" + color.getBlue());
        node.addChildNode("alpha").setContent("" + color.getAlpha());
    }

    public static Color deserializeColor(XMLNode node) throws Exception {
        int r = 0, g = 0, b = 0, alpha = 255;
        for (XMLNode child : node.children()) {
            if (child.getName().equals("red")) {
                r = Integer.parseInt(child.getTextContent());
            }
            if (child.getName().equals("green")) {
                g = Integer.parseInt(child.getTextContent());
            }
            if (child.getName().equals("blue")) {
                b = Integer.parseInt(child.getTextContent());
            }
            if (child.getName().equals("alpha")) {
                alpha = Integer.parseInt(child.getTextContent());
            }
        }
        return new Color(r, g, b, alpha);
    }
}
