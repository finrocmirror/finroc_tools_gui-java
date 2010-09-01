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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.finroc.gui.commons.Util;
import org.finroc.gui.commons.reflection.ReflectionCallback;
import org.finroc.gui.commons.reflection.ReflectionHelper;
import org.finroc.gui.util.ObjectCloner;

@SuppressWarnings("rawtypes")
public class PropertyListEditor extends PropertyEditComponent<PropertyList> implements ChangeListener, ReflectionCallback<Field> {

    /** UID */
    private static final long serialVersionUID = 7101918838675494068L;

    JPanel listPanel;

    List<Field> attributes = new ArrayList<Field>();
    List<PropertyEditComponent[]> guielems = new ArrayList<PropertyEditComponent[]>();
    GridBagConstraints gbc = new GridBagConstraints();
    JSpinner spinner;

    @Override
    protected void createAndShow() {
        setBorder(BorderFactory.createTitledBorder(getPropertyName()));
        setMinimumSize(new Dimension(200, 100));
        setLayout(new BorderLayout());
        listPanel = new JPanel();
        PropertyList pl = getCurWidgetValue();
        SpinnerNumberModel snm = new SpinnerNumberModel(pl.size(), 0, pl.getMaxEntries(), 1);
        spinner = new JSpinner(snm);
        spinner.addChangeListener(this);
        JPanel spinnerPanel = new JPanel();
        spinnerPanel.add(spinner);
        add(spinnerPanel, BorderLayout.LINE_START);
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        // create attribute list
        try {
            ReflectionHelper.visitAllFields(pl.getEntryClass(), true, true, this, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            Field attr = attributes.get(i);
            JLabel lbl = new JLabel(Util.asWords(attr.getName()));
            lbl.setVerticalAlignment(SwingConstants.TOP);
            lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            listPanel.add(lbl, gbc);
        }

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.NONE;
        for (int i = 0; i < pl.size(); i++) {
            createComponents(pl.get(i));
        }
        validate();
        repaint();
    }

    @SuppressWarnings("unchecked")
    private void createComponents(Object object) {
        gbc.gridy = guielems.size() + 1;
        PropertyEditComponent[] pecs = new PropertyEditComponent[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            Field attr = attributes.get(i);
            Class type = attr.getType();
            PropertyEditComponent pec = null;
            if (attr.getType().equals(String.class)) {
                pec = new StringEditor();
            } else if (type.equals(Color.class)) {
                pec = new ColorEditor();
            } else if (Number.class.isAssignableFrom(type) || type.equals(int.class) || type.equals(double.class) || type.equals(float.class) || type.equals(long.class) || type.equals(short.class) || type.equals(byte.class)) {
                pec = new NumberEditor();
            } else if (Enum.class.isAssignableFrom(type)) {
                pec = new EnumEditor();
            } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
                pec = new BooleanEditor();
            } else {
                throw new RuntimeException("Type not supported");
            }
            pec.property = attr;
            try {
                pec.createAndShowMinimal(attr.get(object));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            pecs[i] = pec;
            gbc.gridx = i;
            listPanel.add(pec, gbc);
        }
        guielems.add(pecs);
    }

    public void reflectionCallback(Field f, int id) throws Exception {
        // attribute list creation
        attributes.add(f);
    }


    @SuppressWarnings("unchecked")
    @Override
    public PropertyList getCurEditorValue() {
        PropertyList newList = ObjectCloner.clone(getCurWidgetValue());
        newList.clear();
        for (PropertyEditComponent[] pecs : guielems) {
            try {
                Object obj = newList.getEntryClass().newInstance();
                for (int i = 0; i < attributes.size(); i++) {
                    Field attr = attributes.get(i);
                    attr.set(obj, pecs[i].getCurEditorValue());
                }
                newList.add(obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return newList;
    }

    public void stateChanged(ChangeEvent ce) {
        try {
            while (guielems.size() < (Integer)spinner.getValue()) {
                createComponents(getCurWidgetValue().getEntryClass().newInstance());
            }
            while (guielems.size() > (Integer)spinner.getValue()) {
                PropertyEditComponent[] pecs = guielems.get(guielems.size() - 1);
                for (PropertyEditComponent pec : pecs) {
                    listPanel.remove(pec);
                }
                guielems.remove(guielems.size() - 1);
            }
            validate();
            repaint();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
