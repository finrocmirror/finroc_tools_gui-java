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
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

@SuppressWarnings("rawtypes")
public class PropertyListEditor extends PropertyEditComponent < PropertyListAccessor<? >> implements ChangeListener {

    /** UID */
    private static final long serialVersionUID = 7101918838675494068L;

    JPanel listPanel;

    List<PropertyEditComponent[]> guielems = new ArrayList<PropertyEditComponent[]>();
    GridBagConstraints gbc = new GridBagConstraints();
    JSpinner spinner;

    /** Log domain for this class */
    public static final LogDomain logDomain = LogDefinitions.finroc.getSubDomain("property_editor");

    /** Factories to use to create components from accessors */
    private final ComponentFactory[] componentFactories;

    /** Current value */
    private PropertyListAccessor list;

    public PropertyListEditor(ComponentFactory... componentFactories) {
        this.componentFactories = componentFactories;
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
            assert(object.getClass().equals(list.getElementType()));
            List < PropertyAccessor<? >> attributes = list.getElementAccessors(object);
            PropertyEditComponent[] pecs = new PropertyEditComponent[attributes.size()];
            for (int i = 0; i < attributes.size(); i++) {
                PropertyAccessor property = attributes.get(i);
                if ((!isModifiable()) && property.isModifiable()) {
                    property = new ReadOnlyAdapter(property);
                }
                PropertyEditComponent wpec = null;
                for (ComponentFactory cf : componentFactories) {
                    wpec = cf.createComponent(property, null);
                    if (wpec != null) {
                        break;
                    }
                }
                if (wpec != null) {
                    try {
                        wpec.createAndShowMinimal(wpec.getCurWidgetValue());
                    } catch (Exception e) {
                        logDomain.log(LogLevel.LL_WARNING, getLogDescription(), "Cannot create minimal component type for type " + property.getType().getName()); // skip this property
                        logDomain.log(LogLevel.LL_WARNING, getLogDescription(), e); // skip this property
                    }
                    pecs[i] = wpec;
                    gbc.gridx = i;
                    listPanel.add(wpec, gbc);
                } else {
                    logDomain.log(LogLevel.LL_WARNING, getLogDescription(), "Cannot find component type for type " + property.getType().getName()); // skip this property
                }
            }

            guielems.add(pecs);
        } catch (Exception e) {
            PropertiesPanel.logDomain.log(LogLevel.LL_ERROR, "PropertyListEditor", e);
            JOptionPane.showMessageDialog(null, e.getClass().getName() + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * @return log description
     */
    private String getLogDescription() {
        return getClass().getSimpleName();
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
        gbc.weightx = 0.5;
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
