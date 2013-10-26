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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.finroc.tools.gui.util.embeddedfiles.FileManager;
import org.finroc.tools.gui.util.gui.MDialog;
import org.finroc.tools.gui.util.propertyeditor.FieldAccessorFactory;
import org.finroc.tools.gui.util.propertyeditor.PropertiesPanel;
import org.finroc.tools.gui.util.propertyeditor.PropertyAccessor;
import org.finroc.tools.gui.util.propertyeditor.PropertyEditComponent;
import org.finroc.tools.gui.util.propertyeditor.StandardComponentFactory;

/**
 * @author Max Reichardt
 *
 * Dialog to edit widget properties. Ultra-generic...
 * GUI elements are automatically created.
 */
public class PropertiesDialog extends MDialog {

    /** UID */
    private static final long serialVersionUID = -3537793359933146900L;

    //private Map < Field, PropertyEditComponent<? >> components;
    //private List < PropertyEditComponent<? >> componentsList;  // for constant order

    /** Embedded properties panel */
    private PropertiesPanel propertyPanel;

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

        List < PropertyAccessor<? >> properties = getProperties(o);

        // create content pane
        JPanel main = new JPanel();
        main.setLayout(new BorderLayout());
        propertyPanel = new PropertiesPanel(new StandardComponentFactory(), new GuiComponentFactory(this));
        JScrollPane jsp = new JScrollPane(propertyPanel);
        main.add(jsp, BorderLayout.CENTER);
        getContentPane().add(main, BorderLayout.CENTER);

        propertyPanel.init(properties, false);
        propertyPanel.setBorder(BorderFactory.createTitledBorder("Properties"));

        // create buttons
        JPanel buttonPanel = new JPanel();
        btnOkay = createButton("Okay", buttonPanel);
        if (applyButton) {
            btnApply = createButton("Apply", buttonPanel);
        }
        btnCancel = createButton("Cancel", buttonPanel);
        main.add(buttonPanel, BorderLayout.SOUTH);

        //main.add(new JComboBox(new String[]{"test1", "test2"}), BorderLayout.NORTH);
        //main.add(new JButton("test"), BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    /**
     * Get properties (may be overridden)
     *
     * @param o Objects
     * @return Properties
     */
    protected List < PropertyAccessor<? >> getProperties(List<Object> o) {
        return new FieldAccessorFactory().createAccessors(o);
    }

    public void actionPerformed(ActionEvent e) {

        // save changes ?
        if (e.getSource() == btnOkay || e.getSource() == btnApply) {
            for (PropertyEditComponent<?> wpec : propertyPanel.getComponentList()) {
                try {
                    wpec.applyChanges();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
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

    /**
     * @return Unmodifiable list of objects that are edited with this editor
     */
    public List<Object> getObjects() {
        return Collections.unmodifiableList(objects);
    }
}
