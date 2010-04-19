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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.finroc.gui.commons.reflection.ReflectionCallback;
import org.finroc.gui.commons.reflection.ReflectionHelper;
import org.finroc.gui.util.embeddedfiles.FileManager;


/**
 * @author max
 *
 * Dialog to edit widget properties. Ultra-generic...
 * GUI elements are automatically created.
 */
public class PropertiesDialog extends JDialog implements ActionListener {

    /** UID */
    private static final long serialVersionUID = -3537793359933146900L;

    private Map<Field, PropertyEditComponent<?>> components;
    private List<PropertyEditComponent<?>> componentsList;  // for constant order

    private JButton btnOkay, btnCancel, btnApply;

    private FileManager efm;

    private List<Object> objects;

    public PropertiesDialog(Object o) {
        this((Frame)null, o, null, true);
    }

    public PropertiesDialog(Frame owner, Object o, FileManager efm, boolean applyButton) {
        super(owner, true);
        List<Object> os = new ArrayList<Object>();
        os.add(o);
        init(os, efm, applyButton);
    }

    public PropertiesDialog(Dialog owner, Object o, FileManager efm, boolean applyButton) {
        super(owner, true);
        List<Object> os = new ArrayList<Object>();
        os.add(o);
        init(os, efm, applyButton);
    }

    public PropertiesDialog(Frame owner, List<Object> o, FileManager efm, boolean applyButton) {
        super(owner, true);
        init(o, efm, applyButton);
    }

    public PropertiesDialog(Dialog owner, List<Object> o, FileManager efm, boolean applyButton) {
        super(owner, true);
        init(o, efm, applyButton);
    }

    public void init(List<Object> o, FileManager efm, boolean applyButton) {
        this.objects = o;
        this.efm = efm;

        // inspect widget and create components
        components = new HashMap<Field, PropertyEditComponent<?>>();
        componentsList = new ArrayList<PropertyEditComponent<?>>();
        try {
            for (final Object x : o) {
                ReflectionHelper.visitAllFields(x.getClass(), true, true, new ReflectionCallback<Field>() {
                    public void reflectionCallback(Field f, int id) throws Exception {
                        if (!components.containsKey(f)) {
                            if (!Modifier.isTransient(f.getModifiers())) {
                                if (!ignoreField(f) && f.getAnnotation(NotInPropertyEditor.class) == null) {
                                    try {
                                        PropertyEditComponent<?> pec = PropertyEditComponent.getInstance(f, x, PropertiesDialog.this);
                                        components.put(f, pec);
                                        componentsList.add(pec);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace(); // skip this property
                                    }
                                }
                            }
                        } else {
                            components.get(f).addObject(x);
                        }
                    }
                }, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // create content pane
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        JPanel propertyPanel = new JPanel();
        propertyPanel.setBorder(BorderFactory.createTitledBorder("Properties"));
        propertyPanel.setLayout(new BoxLayout(propertyPanel, BoxLayout.PAGE_AXIS));
        for (PropertyEditComponent<?> wpec : componentsList) {
            propertyPanel.add(wpec);
        }
        JScrollPane jsp = new JScrollPane(propertyPanel);
        main.add(jsp, BorderLayout.CENTER);

        // create buttons
        JPanel buttonPanel = new JPanel();
        btnOkay = new JButton("Okay");
        btnCancel = new JButton("Cancel");
        btnApply = new JButton("Apply");
        btnOkay.addActionListener(this);
        btnCancel.addActionListener(this);
        btnApply.addActionListener(this);
        buttonPanel.add(btnOkay);
        if (applyButton) {
            buttonPanel.add(btnApply);
        }
        buttonPanel.add(btnCancel);
        main.add(buttonPanel, BorderLayout.SOUTH);
        //main.add(new JComboBox(new String[]{"test1", "test2"}), BorderLayout.NORTH);
        //main.add(new JButton("test"), BorderLayout.CENTER);
        getContentPane().add(main, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    /**
     * may be overridden for specific editors
     *
     * @param f Field
     * @return Should field not be editable?
     */
    protected boolean ignoreField(Field f) {
        return false;
    }

    public void actionPerformed(ActionEvent e) {

        // save changes ?
        if (e.getSource() == btnOkay || e.getSource() == btnApply) {
            for (PropertyEditComponent<?> wpec : componentsList) {
                wpec.applyChanges();
            }
            for (Object o : objects) {
                changed(o);
            }
        }

        // close window ?
        if (e.getSource() == btnCancel || e.getSource() == btnOkay) {
            setVisible(false);
            getRootPane().removeAll();
            dispose();
        }
    }

    protected void changed(Object o) {
    }

    public FileManager getEmbeddedFileManager() {
        return efm;
    }
}
