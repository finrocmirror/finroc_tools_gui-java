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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.finroc.gui.abstractbase.DataModelBase;
import org.finroc.gui.abstractbase.DataModelListener;
import org.finroc.gui.commons.Util;
import org.finroc.gui.commons.reflection.ReflectionCallback;
import org.finroc.gui.commons.reflection.ReflectionHelper;
import org.finroc.gui.themes.Themes;
import org.finroc.gui.util.embeddedfiles.AbstractFile;
import org.finroc.gui.util.embeddedfiles.AbstractFiles;
import org.finroc.gui.util.embeddedfiles.HasEmbeddedFiles;
import org.finroc.gui.util.propertyeditor.NotInPropertyEditor;

import org.finroc.core.FrameworkElement;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.ThreadLocalCache;


/**
 * @author max
 *
 * This is the base class for all widgets used in the GUI
 */

public abstract class Widget extends DataModelBase<GUI, GUIPanel, WidgetPort<?>> implements ReflectionCallback<Field>, HasEmbeddedFiles {

    /** UID */
    private static final long serialVersionUID = -5622497094531263503L;

    /** for serialization */
    @NotInPropertyEditor
    private Rectangle bounds;

    /** Label of widget (optional) */
    private String label;

    /** Color of Label */
    private Color labelColor = Themes.getCurTheme().widgetLabel();

    /** Background color of widget (at least of the border) */
    private Color background = Themes.getCurTheme().widgetBackground();

    /** temporary list */
    @NotInPropertyEditor
    private List<AbstractFile> embeddedFiles;

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
        super.restore(parent);
        frameworkElement.init();
        //getRoot().getJmcagui().addConnectionListener(this);
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
        if (!WidgetAndInterfaceRegister.appletMode) { // Browser GUI doesn't need to reinitialize ports
            children.clear();
            try {
                ReflectionHelper.visitAllFields(getClass(), true, true, this, 0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            fireDataModelEvent(DataModelListener.Event.ChildrenChanged, this);
        }
    }

    public synchronized Collection<AbstractFile> getEmbeddedFiles() {
        embeddedFiles = new ArrayList<AbstractFile>();
        try {
            ReflectionHelper.visitAllFields(getClass(), true, true, this, 1);
        } catch (Exception e) {
            e.printStackTrace();
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
     * @param collection Any collection this port is part of - otherwise null
     * @return PortCreationInfo to create port with (null means use suggestion)
     */
    protected abstract PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort, WidgetPorts<?> collection);

    @Override
    protected FrameworkElement createFrameworkElement() {
        return new FrameworkElement(getClass().getSimpleName());
    }
}
