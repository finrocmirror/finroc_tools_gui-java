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
package org.finroc.tools.gui.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.plugins.data_types.ContainsStrings;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.CoreString;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;

/**
 * @author Patrick Fleischmann
 *
 */
public class BlackboardDropDown extends Widget {

    /** UID */
    private static final long serialVersionUID = 8816685145481173845L;

    /** Output port returns selected index */
    public WidgetOutput.Numeric selectedIndex;

    /** Output port returns selected element */
    public WidgetOutput.Std<CoreString> selectedElement;

    /** Input Blackboard contains list elements as strings */
    public WidgetInput.Std<ContainsStrings> elements;

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort < ? > forPort) {
        if (forPort == elements) {
            return suggestion.derive(ContainsStrings.TYPE);
        } else if (forPort == selectedElement) {
            return suggestion.derive(CoreString.TYPE).derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
        } else if (forPort == selectedIndex) {
            return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
        }

        return suggestion;
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new BlackboardDropDownUI();
    }

    @SuppressWarnings("rawtypes")
    class BlackboardDropDownUI extends WidgetUI implements PortListener, ActionListener, Runnable {
        /** UID */
        private static final long serialVersionUID = -4319918865786225484L;

        JComboBox<String> comboBox;
        boolean updatingFromPortListener = true;

        @SuppressWarnings("unchecked")
        public BlackboardDropDownUI() {
            super(RenderMode.Swing);
            setLayout(new BorderLayout());
            comboBox = new JComboBox();
            add(comboBox);

            // combobox change listener
            comboBox.addActionListener(this);

            // port listeners
            elements.addChangeListener(this);
            selectedIndex.addChangeListener(this);
            selectedElement.addChangeListener(this);
        }

        @Override
        protected boolean isWidgetFocusable() {
            return true;
        }

        public void actionPerformed(ActionEvent e) {
            Object selectedItem = comboBox.getSelectedItem();
            if (selectedItem != null && (!updatingFromPortListener)) {
                selectedIndex.publish(comboBox.getSelectedIndex());
                CoreString string = selectedElement.getUnusedBuffer();
                string.set(selectedItem.toString());
                selectedElement.publish(string);
            }
        }

        @Override
        public void portChanged(AbstractPort origin, Object value) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            updatingFromPortListener = true;
            ContainsStrings value = elements.getAutoLocked();
            boolean elementsChanged = comboBox.getItemCount() != ((value == null) ? 0 : value.stringCount());
            if (!elementsChanged) {
                for (int i = 0; i < comboBox.getItemCount(); i++) {
                    elementsChanged |= (!value.getString(i).equals(comboBox.getItemAt(i).toString()));
                }
            }
            if (elementsChanged) {
                comboBox.removeAllItems();
                if (value != null && value.stringCount() > 0) {
                    for (int i = 0; i < value.stringCount(); i++) {
                        comboBox.addItem(value.getString(i).toString());
                    }
                }
            }
            if ((!selectedElement.getPort().isConnected()) && (!selectedIndex.getPort().isConnected())) {
                comboBox.setSelectedIndex(-1);
            } else {
                int index = -1;
                for (int i = 0; i < comboBox.getItemCount(); i++) {
                    String item = comboBox.getItemAt(i);
                    if (((selectedIndex.getInt() == i) || !selectedIndex.getPort().isConnected()) &&
                            ((selectedElement.asPort().getAutoLocked().toString().equals(item)) || !selectedElement.getPort().isConnected())) {
                        index = i;
                    }
                }
                comboBox.setSelectedIndex(index);
            }

            releaseAllLocks();
            updatingFromPortListener = false;
        }
    }
}
