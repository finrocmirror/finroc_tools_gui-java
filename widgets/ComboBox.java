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
import java.io.Serializable;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.util.propertyeditor.PropertyList;
import org.finroc.tools.gui.util.propertyeditor.gui.EnumConstantsImporter;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.DataTypeReference;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;
import org.rrlib.finroc_core_utils.serialization.Serialization;


public class ComboBox extends Widget implements EnumConstantsImporter {

    /** UID */
    private static final long serialVersionUID = 8816685145481173845L;

    /** Widget currently has three fixed outputs, variable number would be difficult with property editor */
    public WidgetOutput.Numeric output1, output2, output3;

    /** Output port with custom type */
    public WidgetOutput.Custom customOutput;

    public PropertyList<ComboBoxElement> choices = new PropertyList<ComboBoxElement>(ComboBoxElement.class, 30);

    public ComboBox() {
        choices.add(new ComboBoxElement("Choice 1", 0, 0, 0, "0"));
        choices.add(new ComboBoxElement("Choice 2", 1, 1, 1, "0"));
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new ComboBoxUI();
    }

    @Override
    public void importEnumConstants(DataTypeReference enumType) {
        choices.clear();
        int i = 0;
        for (Object o : enumType.get().getEnumConstants()) {
            String customOutput = (o instanceof Enum<?>) ? Serialization.serialize((Enum<?>)o) : (o.toString() + " (" + i + ")");
            choices.add(new ComboBoxElement(o.toString(), i, i, i, customOutput));
            i++;
        }
    }

    @Override
    public boolean importEnumConstantsSupported() {
        return true;
    }

    public static class ComboBoxElement implements Serializable {

        /** UID */
        private static final long serialVersionUID = 6930215551116910119L;

        private String name = "choice name";
        private double output1 = 0, output2 = 0, output3 = 0;
        private String customOutput = "0";

        public ComboBoxElement() {}

        private ComboBoxElement(String name, double output1, double output2, double output3, String customOutput) {
            this.name = name;
            this.output1 = output1;
            this.output2 = output2;
            this.output3 = output3;
            this.customOutput = customOutput;
        }

        public String toString() {
            return name;
        }
    }

    @SuppressWarnings("rawtypes")
    class ComboBoxUI extends WidgetUI implements PortListener, ActionListener, Runnable {

        /** UID */
        private static final long serialVersionUID = -8663762048760660960L;

        JComboBox comboBox = new JComboBox();
        boolean updatingFromReversePush = true;

        @SuppressWarnings("unchecked")
        public ComboBoxUI() {
            super(RenderMode.Swing);
            output1.addChangeListener(this);
            output2.addChangeListener(this);
            output3.addChangeListener(this);
            customOutput.addChangeListener(this);
            setLayout(new BorderLayout());
            add(comboBox);
            comboBox.addActionListener(this);
            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {
            comboBox.removeAllItems();
            for (ComboBoxElement cbe : choices) {
                comboBox.addItem(cbe);
            }
            portChanged(null, null);
        }

        public void actionPerformed(ActionEvent e) {
            ComboBoxElement cbe = (ComboBoxElement)comboBox.getSelectedItem();
            if (cbe != null && (!updatingFromReversePush)) {
                //System.out.println("Publishing");
                output1.publish(cbe.output1);
                output2.publish(cbe.output2);
                output3.publish(cbe.output3);
                if (cbe.customOutput != null) {
                    customOutput.publishFromString(cbe.customOutput);
                }
            }
        }

        @Override
        public void portChanged(AbstractPort origin, Object value) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            for (ComboBoxElement cbe : choices) {
                if ((output1.getDouble() == cbe.output1 || !output1.getPort().isConnected()) &&
                        (output2.getDouble() == cbe.output2 || !output2.getPort().isConnected()) &&
                        (output3.getDouble() == cbe.output3 || !output3.getPort().isConnected()) &&
                        (Serialization.serialize(customOutput.asPort().getAutoLocked()).equals(cbe.customOutput) || !customOutput.asPort().isConnected())) {
                    updatingFromReversePush = true;
                    comboBox.setSelectedItem(cbe);
                    updatingFromReversePush = false;
                    return;
                }
            }
            releaseAllLocks();
            comboBox.setSelectedItem(null);
        }
    }
}
