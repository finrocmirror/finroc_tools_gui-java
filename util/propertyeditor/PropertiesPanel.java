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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.finroc.tools.gui.themes.Themes;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

/**
 * @author Max Reichardt
 *
 * Basic Panel that contains property editing components
 */
public class PropertiesPanel extends JPanel {

    /** UID */
    private static final long serialVersionUID = 3468734647319739046L;

    /** Factories to use to create components from accessors */
    private final ComponentFactory[] componentFactories;

    /** List of all property edit components on panel */
    private final ArrayList < PropertyEditComponent<? >> componentList = new ArrayList < PropertyEditComponent<? >> ();

    /** Gridbag constraints for layout */
    private final GridBagConstraints gbcLabel = new GridBagConstraints();
    private final GridBagConstraints gbcComp = new GridBagConstraints();
    private final GridBagConstraints gbcExtra = new GridBagConstraints();

    public PropertiesPanel(ComponentFactory... componentFactories) {
        this.componentFactories = componentFactories;
        setLayout(new GridBagLayout());
        //setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    /**
     * Initialize panel
     *
     * @param properties List with properties to create components for
     * @param labelAlignmentLeft Align labels on left hand side?
     */
    @SuppressWarnings({ "rawtypes" })
    public void init(Collection <? extends PropertyAccessor<? >> properties, boolean labelAlignmentLeft) {
        componentList.clear();

        for (PropertyAccessor property : properties) {
            PropertyEditComponent<?> wpec = null;
            for (ComponentFactory cf : componentFactories) {
                try {
                    wpec = cf.createComponent(property, this);
                    if (wpec != null) {
                        break;
                    }
                } catch (Exception e) {
                    Log.log(LogLevel.ERROR, this, e);
                }
            }
            if (wpec != null) {
                componentList.add(wpec);
            } else {
                Log.log(LogLevel.WARNING, this, "Cannot find component type for type " + property.getType().getName()); // skip this property
            }
        }

        boolean resizables = false;
        for (int i = 0; i < componentList.size(); i++) {
            addComponent(componentList.get(i), i, labelAlignmentLeft);
            resizables |= componentList.get(i).isResizable();
        }

        if (!resizables) {
            gbcExtra.gridy = componentList.size();
            gbcExtra.gridwidth = 2;
            gbcExtra.weighty = 0.9;
            JPanel extraPanel = new JPanel();
            extraPanel.setOpaque(false);
            add(extraPanel, gbcExtra);
        }
    }

    /**
     * Adds component to panel
     * (may be overridden for customizations)
     *
     * @param comp Component to add
     * @param index Index of component
     * @param labelAlignmentLeft Align labels on left hand side?
     */
    protected void addComponent(PropertyEditComponent<?> comp, int index, boolean labelAlignmentLeft) {
        try {
            comp.createAndShow();
        } catch (Exception e) {
            Log.log(LogLevel.ERROR, this, e);
        }

        // Create label?
        String label = comp.getLabel();
        if (label != null) {
            gbcLabel.anchor = labelAlignmentLeft ? GridBagConstraints.WEST : GridBagConstraints.EAST;
            gbcLabel.gridx = 0;
            gbcLabel.gridy = index;
            gbcLabel.insets = new Insets(0, 0, 0, 10);
            gbcLabel.weightx = 0.0001;
            gbcLabel.weighty = 0.0001;
            JLabel jlabel = new JLabel(label);
            //jlabel.setHorizontalAlignment(labelAlignment);
            add(jlabel, gbcLabel);
        }

        // Add component
        gbcComp.anchor = GridBagConstraints.WEST;
        gbcComp.fill = GridBagConstraints.BOTH;
        gbcComp.gridwidth = label == null ? 2 : 1;
        gbcComp.gridx = 1;
        if (!Themes.nimbusLookAndFeel()) {
            gbcComp.insets = new Insets(1, 0, 1, 0);
        }
        gbcComp.gridy = index;
        gbcComp.weightx = 0.9;
        gbcComp.weighty = comp.isResizable() ? 0.2 : 0.0001;
        add(comp, gbcComp);
    }

    /**
     * @return Unmodifiable list of components
     */
    public List < PropertyEditComponent<? >> getComponentList() {
        return Collections.unmodifiableList(componentList);
    }

    /**
     * @return Factories to use to create components from accessors
     */
    public ComponentFactory[] getComponentFactories() {
        return componentFactories;
    }
}
