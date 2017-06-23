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
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.OperationNotSupportedException;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.finroc.core.datatype.DataTypeReference;
import org.finroc.core.remote.RemoteType;
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
public class DataTypeEditor extends PropertyEditComponent<DataTypeReference> implements ActionListener {

    /** UID */
    private static final long serialVersionUID = -8458082858218885773L;

    static class TypeEntry {
        String longName;
        String namespace;
        String shortName;
        Object plainType;
        Object listType;

        @Override
        public String toString() {
            return shortName;
        }
    }

    private String[] namespaces;
    TreeMap<String, TypeEntry> types = new TreeMap<String, TypeEntry>(String.CASE_INSENSITIVE_ORDER);
    private Dimension typeSelectorMinDimension;
    private Dimension typeSelectorPreferredDimension;

    private JComboBox<String> namespaceSelector;
    private JComboBox<TypeEntry> typeSelector;
    private JCheckBox listTypeSelector;

    public DataTypeEditor(Object[] values) {

        // Process types
        for (Object value : values) {
            if (value.toString().equals(DataTypeBase.NULL_TYPE)) {
                continue;
            }

            String longName = value.toString();
            boolean listType = longName.startsWith("List<");
            while (longName.startsWith("List<")) {
                longName = longName.substring("List<".length(), longName.length() - 1);
            }
            TypeEntry entry = types.get(longName);
            if (entry == null) {
                // Create new entry
                entry = new TypeEntry();
                entry.longName = longName;

                // Split name into namespace and short name
                int namespaceDividerIndex = -1;
                for (int i = 0; i < longName.length(); i++) {
                    char c = longName.charAt(i);
                    if (c == '.') {
                        namespaceDividerIndex = i;
                    }
                    if (c == '<') {
                        break;
                    }
                }
                entry.namespace = namespaceDividerIndex < 0 ? "(global)" : longName.substring(0, namespaceDividerIndex);
                entry.shortName = namespaceDividerIndex < 0 ? longName : longName.substring(namespaceDividerIndex + 1);
                types.put(longName, entry);
            }
            if (listType) {
                entry.listType = value;
            } else {
                entry.plainType = value;
            }
        }

        // Generate list with namespaces
        TreeSet<String> namespaces = new TreeSet<String>();
        for (TypeEntry entry : types.values()) {
            namespaces.add(entry.namespace);
        }
        this.namespaces = namespaces.toArray(new String[0]);

        // Calculate min dimension for type selector combo box
        String[] typeStrings = new String[types.values().size()];
        int index = 0;
        for (TypeEntry entry : types.values()) {
            typeStrings[index] = entry.shortName;
            index++;
        }
        typeSelectorMinDimension = new JComboBox<>(typeStrings).getMinimumSize();
        typeSelectorPreferredDimension = new JComboBox<>(typeStrings).getPreferredSize();
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
            namespaceSelector = new JComboBox<String>(namespaces);
            namespaceSelector.addActionListener(this);
            add(namespaceSelector, BorderLayout.WEST);
            namespaceSelector.setEnabled(isModifiable());

            typeSelector = new JComboBox<TypeEntry>();
            typeSelector.addActionListener(this);
            add(typeSelector, BorderLayout.CENTER);
            typeSelector.setEnabled(isModifiable());
            typeSelector.setMinimumSize(typeSelectorMinDimension);
            typeSelector.setPreferredSize(typeSelectorPreferredDimension);

            listTypeSelector = new JCheckBox("std::vector");
            add(listTypeSelector, BorderLayout.EAST);
            listTypeSelector.setEnabled(isModifiable());

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
        TypeEntry entry = (TypeEntry)selectedType;
        return new DataTypeReference((listTypeSelector.isSelected() ? entry.listType : entry.plainType).toString());
    }

    @Override
    protected void valueUpdated(DataTypeReference type) {
        String longName = type.toString();
        boolean listType = longName.startsWith("List<");
        if (listType) {
            longName = longName.substring("List<".length(), longName.length() - 1);
        }
        TypeEntry entry = type == null ? null : types.get(longName);
        if (entry == null) {
            namespaceSelector.setSelectedIndex(0);
            typeSelector.setSelectedIndex(0);
        } else {
            namespaceSelector.setSelectedItem(entry.namespace);
            typeSelector.setSelectedItem(entry);
            listTypeSelector.setSelected(listType);
        }
        updateButtonStates();
    }

    private void updateButtonStates() {
        Object selectedType = typeSelector.getSelectedItem();
        if (selectedType == null) {
            listTypeSelector.setEnabled(false);
        } else {
            TypeEntry entry = (TypeEntry)selectedType;
            listTypeSelector.setEnabled(entry.listType != null && entry.plainType != null);
            if (entry.listType == null) {
                listTypeSelector.setSelected(false);
            }
            if (entry.plainType == null) {
                listTypeSelector.setSelected(true);
            }
        }
    }

    private Object[] getEnumConstants(Object type) {
        if (type instanceof DataTypeBase) {
            return ((DataTypeBase)type).getEnumConstants();
        } else if (type instanceof RemoteType) {
            return ((RemoteType)type).getEnumConstants();
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == namespaceSelector) {

            // filter list
            ArrayList<TypeEntry> filteredTypes = new ArrayList<TypeEntry>();
            if (namespaceSelector.getSelectedItem() == null) {
                filteredTypes.addAll(types.values());
            } else {
                for (TypeEntry entry : types.values()) {
                    if (entry.namespace.equals(namespaceSelector.getSelectedItem())) {
                        filteredTypes.add(entry);
                    }
                }
            }
            typeSelector.setModel(new JComboBox<TypeEntry>(filteredTypes.toArray(new TypeEntry[0])).getModel());
            typeSelector.setSelectedIndex(0);
            updateButtonStates();
        } else if (e.getSource() == typeSelector) {
            updateButtonStates();
        }
    }
}
