/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2011 Max Reichardt
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
package org.finroc.tools.gui.util.propertyeditor.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.finroc.core.datatype.DataTypeReference;
import org.finroc.tools.gui.util.propertyeditor.ComboBoxEditor;
import org.finroc.tools.gui.util.propertyeditor.PropertiesPanel;
import org.finroc.tools.gui.util.propertyeditor.PropertyEditComponent;

/**
 * @author max
 *
 * Editor for data types: Adds button to editor that lets user
 * import enum constants.
 */
public class DataTypeEditor extends ComboBoxEditor<DataTypeReference> implements ActionListener {

    /** UID */
    private static final long serialVersionUID = -8458082858218885773L;

    private JButton importEnumConstants;

    private EnumConstantsImporter importer;

    private PropertiesPanel propPanel;

    public DataTypeEditor(DataTypeReference[] values, EnumConstantsImporter importer, PropertiesPanel propPanel) {
        super(values);
        this.importer = importer;
        this.propPanel = propPanel;
    }

    @Override
    protected void createAndShow() {
        super.createAndShow();
        if (importer != null) {
            importEnumConstants = new JButton("Import Enum Constants");
            importEnumConstants.addActionListener(this);
            add(importEnumConstants, BorderLayout.EAST);
            jcmb.addActionListener(this);
            updateButtonState();
        }
    }

    private void updateButtonState() {
        importEnumConstants.setEnabled(Enum.class.isAssignableFrom(getCurEditorValue().get().getJavaClass()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == importEnumConstants) {
            try {
                for (PropertyEditComponent<?> pec : propPanel.getComponentList()) {
                    pec.applyChanges();
                }
                importer.importEnumConstants(getCurEditorValue());
                for (PropertyEditComponent<?> pec : propPanel.getComponentList()) {
                    pec.updateValue();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (e.getSource() == jcmb) {
            updateButtonState();
        }
    }
}
