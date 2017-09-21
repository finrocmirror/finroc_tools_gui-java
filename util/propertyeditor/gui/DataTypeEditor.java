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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeMap;

import javax.naming.OperationNotSupportedException;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.finroc.core.datatype.DataTypeReference;
import org.finroc.tools.gui.util.propertyeditor.PropertyEditComponent;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;
import org.rrlib.serialization.rtti.DataTypeBase;

/**
 * @author Max Reichardt
 *
 * Editor for data types: Adds button to editor that lets user
 * import enum constants.
 */
public class DataTypeEditor extends PropertyEditComponent<DataTypeReference> implements ActionListener, DocumentListener, Comparator<Object> {

    /** UID */
    private static final long serialVersionUID = -8458082858218885773L;

    TreeMap<String, Object> types = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

    private Dimension typeSelectorMinDimension;
    private Dimension typeSelectorPreferredDimension;

    private JTextField typeFilter;
    private JComboBox<Object> typeSelector;

    public DataTypeEditor(Object[] values) {

        // Process types
        for (Object value : values) {
            if (value.toString().equals(DataTypeBase.NULL_TYPE)) {
                continue;
            }
            types.put(value.toString(), value);
        }

        // Calculate min dimension for type selector combo box
        JComboBox<Object> testBox = new JComboBox<Object>(types.values().toArray());
        typeSelectorMinDimension = testBox.getMinimumSize();
        typeSelectorPreferredDimension = testBox.getPreferredSize();
    }

    @Override
    protected void createAndShow() {
        try {
            createAndShowMinimal(getCurWidgetValue());
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, this, e);
        }
    }

    @Override
    public void createAndShowMinimal(DataTypeReference currentValue) throws OperationNotSupportedException {
        try {
            typeFilter = new JTextField();
            typeFilter.addActionListener(this);
            add(typeFilter, BorderLayout.WEST);
            typeFilter.setEnabled(isModifiable());
            typeFilter.setPreferredSize(new Dimension(200, typeFilter.getPreferredSize().height));
            typeFilter.getDocument().addDocumentListener(this);

            typeSelector = new JComboBox<Object>(types.values().toArray());
            typeSelector.addActionListener(this);
            add(typeSelector, BorderLayout.CENTER);
            typeSelector.setEnabled(isModifiable());
            typeSelector.setMinimumSize(typeSelectorMinDimension);
            typeSelector.setPreferredSize(typeSelectorPreferredDimension);

            valueUpdated(currentValue);
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, this, e);
        }
    }

    @Override
    public DataTypeReference getCurEditorValue() {
        Object selectedType = typeSelector.getSelectedItem();
        if (selectedType == null) {
            return new DataTypeReference(DataTypeBase.NULL_TYPE);
        }
        return new DataTypeReference(selectedType.toString());
    }

    @Override
    protected void valueUpdated(DataTypeReference type) {
        Object entry = type == null ? null : types.get(type.toString());
        if (entry == null) {
            typeSelector.setSelectedIndex(0);
        } else {
            typeSelector.setSelectedItem(entry);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private void filterTypes(DocumentEvent e) {
        if (typeFilter.getText().trim().length() == 0) {
            typeSelector.setModel(new JComboBox<Object>(types.values().toArray()).getModel());
            return;
        }

        // filter list
        ArrayList<Object> filteredTypes = new ArrayList<Object>();
        String[] filters = typeFilter.getText().trim().split(" ");
        for (int i = 0; i < filters.length; i++) {
            filters[i] = filters[i].trim().toLowerCase();
        }
        for (Object type : types.values()) {
            boolean matches = true;
            String typeName = type.toString().toLowerCase();
            for (String filter : filters) {
                matches &= (typeName.contains(filter));
            }
            if (matches) {
                filteredTypes.add(type);
            }
        }

        // sort filtered types by length
        filteredTypes.sort(this);

        typeSelector.setModel(new JComboBox<Object>(filteredTypes.toArray()).getModel());
        if (typeSelector.getItemCount() > 0) {
            typeSelector.setSelectedIndex(0);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        filterTypes(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        filterTypes(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        filterTypes(e);
    }

    @Override
    public int compare(Object o1, Object o2) {
        return Integer.compare(o1.toString().length(), o2.toString().length());
    }
}
