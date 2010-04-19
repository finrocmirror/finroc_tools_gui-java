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
package org.finroc.gui.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JComboBox;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetOutput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetPorts;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.util.propertyeditor.PropertyList;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortListener;


public class ComboBox extends Widget {

    /** UID */
    private static final long serialVersionUID = 8816685145481173845L;

    /** Widget currently has three fixed outputs, variable number would be difficult with property editor */
    public WidgetOutput.Numeric output1, output2, output3;

    public PropertyList<ComboBoxElement> choices = new PropertyList<ComboBoxElement>(ComboBoxElement.class, 30);

    public ComboBox() {
        choices.add(new ComboBoxElement("Choice 1", 0, 0, 0));
        choices.add(new ComboBoxElement("Choice 2", 1, 1, 1));
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort, WidgetPorts<?> collection) {
        return suggestion.derive(suggestion.flags | PortFlags.ACCEPTS_REVERSE_DATA_PUSH);
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new ComboBoxUI();
    }

    public static class ComboBoxElement implements Serializable {

        /** UID */
        private static final long serialVersionUID = 6930215551116910119L;

        private String name = "choice name";
        private double output1 = 0, output2 = 0, output3 = 0;

        public ComboBoxElement() {}

        private ComboBoxElement(String name, double output1, double output2, double output3) {
            this.name = name;
            this.output1 = output1;
            this.output2 = output2;
            this.output3 = output3;
        }

        public String toString() {
            return name;
        }
    }

    class ComboBoxUI extends WidgetUI implements CCPortListener<CoreNumber>, ActionListener {

        /** UID */
        private static final long serialVersionUID = -8663762048760660960L;

        JComboBox comboBox = new JComboBox();

        public ComboBoxUI() {
            super(RenderMode.Swing);
            output1.addChangeListener(this);
            output2.addChangeListener(this);
            output3.addChangeListener(this);
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
            if (cbe != null) {
                output1.publish(cbe.output1);
                output2.publish(cbe.output2);
                output3.publish(cbe.output3);
            }
        }

        @Override
        public void portChanged(CCPortBase origin, CoreNumber value) {
            for (ComboBoxElement cbe : choices) {
                if (output1.getDouble() == cbe.output1 &&
                        output2.getDouble() == cbe.output2 &&
                        output3.getDouble() == cbe.output3) {
                    comboBox.setSelectedItem(cbe);
                    return;
                }
            }
            comboBox.setSelectedItem(null);
        }
    }
}
