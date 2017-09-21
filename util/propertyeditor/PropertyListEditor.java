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
package org.finroc.tools.gui.util.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.finroc.tools.gui.commons.Util;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

@SuppressWarnings("rawtypes")
public class PropertyListEditor extends PropertyEditComponent < PropertyListAccessor<? >> implements ChangeListener {

    /** UID */
    private static final long serialVersionUID = 7101918838675494068L;

    JPanel listPanel;

    List<PropertyEditComponent[]> guielems = new ArrayList<PropertyEditComponent[]>();
    GridBagConstraints gbc = new GridBagConstraints();
    JSpinner spinner;

    /** Factories to use to create components from accessors */
    private final ComponentFactory[] componentFactories;

    /** Current value */
    private PropertyListAccessor list;

    /** Reference to properties panel */
    private PropertiesPanel propertiesPanel;

    public PropertyListEditor(PropertiesPanel panel, ComponentFactory... componentFactories) {
        this.componentFactories = componentFactories;
        propertiesPanel = panel;
    }

    @Override
    protected void createAndShow() throws Exception {
        setBorder(BorderFactory.createTitledBorder(""/*getPropertyName()*/));
        setMinimumSize(new Dimension(200, 100));
        setLayout(new BorderLayout());
        valueUpdated(getCurWidgetValue());
    }

    @SuppressWarnings("unchecked")
    private void createComponents(Object object) {
        try {
            gbc.gridy = guielems.size() + 1;
            //assert(object.getClass().equals(list.getElementType()));
            assert(list.getElementType().isAssignableFrom(object.getClass()));
            List < PropertyAccessor<? >> attributes = list.getElementAccessors(object);
            PropertyEditComponent[] pecs = new PropertyEditComponent[attributes.size()];
            for (int i = 0; i < attributes.size(); i++) {
                PropertyAccessor property = attributes.get(i);
                if ((!isModifiable()) && property.isModifiable()) {
                    property = new ReadOnlyAdapter(property);
                }
                PropertyEditComponent wpec = null;
                for (ComponentFactory cf : componentFactories) {
                    wpec = cf.createComponent(property, propertiesPanel);
                    if (wpec != null) {
                        break;
                    }
                }
                if (wpec != null) {
                    try {
                        wpec.createAndShowMinimal(wpec.getCurWidgetValue());
                    } catch (Exception e) {
                        Log.log(LogLevel.WARNING, this, "Cannot create minimal component type for type " + property.getType().getName()); // skip this property
                        Log.log(LogLevel.WARNING, this, e); // skip this property
                    }
                    pecs[i] = wpec;
                    gbc.gridx = i;
                    gbc.weightx = wpec.getMinimalDefaultGridWeight();
                    gbc.fill = gbc.weightx > 0 ? GridBagConstraints.BOTH : 0;
                    listPanel.add(wpec, gbc);
                } else {
                    Log.log(LogLevel.WARNING, this, "Cannot find component type for type " + property.getType().getName()); // skip this property
                }
            }

            guielems.add(pecs);
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, this, e);
            JOptionPane.showMessageDialog(null, e.getClass().getName() + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public PropertyListAccessor<?> getCurEditorValue() throws Exception {
        for (PropertyEditComponent[] pecs : guielems) {
            for (PropertyEditComponent pec : pecs) {
                pec.applyChanges();
            }
        }
        return list;
    }

    public void stateChanged(ChangeEvent ce) {
        try {
            assert(guielems.size() == list.size());
            while (list.size() < (Integer)spinner.getValue()) {
                list.addElement();
                createComponents(list.get(list.size() - 1));
            }
            while (list.size() > (Integer)spinner.getValue()) {
                list.removeElement(list.size() - 1);
                PropertyEditComponent[] pecs = guielems.get(guielems.size() - 1);
                for (PropertyEditComponent pec : pecs) {
                    listPanel.remove(pec);
                }
                guielems.remove(guielems.size() - 1);
            }
            assert(guielems.size() == list.size());
            validate();
            repaint();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void valueUpdated(PropertyListAccessor<?> t) {
        removeAll();
        guielems.clear();
        listPanel = new JPanel();
        list = t;
        SpinnerNumberModel snm = new SpinnerNumberModel(list.size(), 0, list.getMaxEntries(), 1);
        spinner = new JSpinner(snm);
        spinner.addChangeListener(this);
        spinner.setEnabled(isModifiable());
        JPanel spinnerPanel = new JPanel();
        spinnerPanel.add(spinner);
        add(spinnerPanel, BorderLayout.LINE_START);
        JPanel helper = new JPanel();
        helper.setLayout(new BorderLayout());
        helper.add(listPanel, BorderLayout.NORTH);
        add(new JScrollPane(helper), BorderLayout.CENTER);

        // create attribute list
        boolean tempSizeIncrease = false;
        if (list.size() == 0) {
            tempSizeIncrease = true;
            list.addElement();
        }
        @SuppressWarnings("unchecked")
        List < PropertyAccessor<? >> attributes = list.getElementAccessors(list.get(0));
        if (tempSizeIncrease) {
            list.removeElement(0);
        }

        // create list with controls
        listPanel.setLayout(new GridBagLayout());
        //listPanel.setBackground(Color.BLUE);
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        //gbc.fill = GridBagConstraints.VERTICAL;
        gbc.weightx = 0.00000001;
        gbc.weighty = 0.00000001;
        for (int i = 0; i < attributes.size(); i++) {
            gbc.gridx = i;
            PropertyAccessor<?> attr = attributes.get(i);
            JLabel lbl = new JLabel(Util.asWords(attr.getName()));
            lbl.setVerticalAlignment(SwingConstants.TOP);
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            listPanel.add(lbl, gbc);
        }

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        for (int i = 0; i < list.size(); i++) {
            createComponents(list.get(i));
        }
        assert(guielems.size() == list.size());
        validate();
        repaint();
    }

    @Override
    public boolean isResizable() {
        return true;
    }
}
