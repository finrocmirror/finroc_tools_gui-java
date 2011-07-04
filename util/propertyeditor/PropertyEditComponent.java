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
package org.finroc.tools.gui.util.propertyeditor;

import java.awt.BorderLayout;

import javax.naming.OperationNotSupportedException;
import javax.swing.JPanel;

import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;

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

    /** Above than average character width */
    protected final int CHAR_WIDTH = 11;

    /** Property accessor */
    private PropertyAccessor<T> property;

    /** current value */
    private T curValue;

    /** Log domain for this class */
    public static final LogDomain logDomain = LogDefinitions.finroc.getSubDomain("property_editor");

    public PropertyEditComponent() {
        setLayout(new BorderLayout());
    }

    /**
     * writes changes to widget
     */
    public void applyChanges() throws Exception {
        T newValue = getCurEditorValue();
        if (isModifiable() && (curValue == null || !curValue.equals(newValue))) {
            property.set(ObjectCloner.clone(newValue));
            curValue = ObjectCloner.clone(newValue);
        }
    }

    /** get current value of component (for saving changes) */
    public abstract T getCurEditorValue() throws Exception;

    protected Class<?> getPropertyType() {
        return property.getType();
    }

    /** create and init components */
    protected abstract void createAndShow() throws Exception;

    /** update value (overwrites any changes in editor) */
    public void updateValue() throws Exception {
        curValue = property.get();
        valueUpdated(ObjectCloner.clone(curValue));
    }

    /**
     * Called when curValue has been updated
     * (change editor value to new value - overwrite any changes in editor)
     */
    protected abstract void valueUpdated(T t);

    public T getCurWidgetValue() throws Exception {
        return property.get();
    }

    public String getPropertyName() {
        return property.getName();
    }

    /**
     * (may be overridden)
     * @return Label of component - null if no label (then component occupies label space as well)
     */
    public String getLabel() {
        return getPropertyName();
    }

    /**
     * @return true, when vertical resizing makes sense
     */
    public boolean isResizable() {
        return false;
    }

    /*protected void createStdLayoutWith(JComponent c) {
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
    }*/

    public void createAndShowMinimal(T object) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     * (Needs to be called once initially)
     *
     * @param property Property accessor to use
     */
    public void init(PropertyAccessor<T> property) throws Exception {
        assert(this.property == null);
        this.property = property;
        curValue = property.get();
    }

    /**
     * @return Is property modifiable?
     */
    protected boolean isModifiable() {
        return property.isModifiable();
    }
}
