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
package org.finroc.tools.gui.widgets;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.plugins.data_types.ContainsStrings;
import org.finroc.plugins.data_types.StdStringList;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;

/**
 * @author max
 * @author jens
 *
 * Displays list of check boxes created from string list at input port and outputs selected check boxes as bit vector.
 */
public class CheckBoxGroup extends Widget {

    /** UID */
    private static final long serialVersionUID = -7862015642847164790L;

    /** CheckBox output port */
    private WidgetOutput.Numeric value;

    /** Input port contains list elements as strings */
    public WidgetInput.Std<ContainsStrings> elements;

    /** Whether bit selection output should be inverted or not*/
    public boolean invert_output;

    @Override
    protected void setDefaultColors() {
        useAlternativeColors();
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new CheckBoxUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (forPort == elements) {
            return suggestion.derive(StdStringList.TYPE);
        }
        return suggestion.derive(suggestion.flags);
    }

    @SuppressWarnings("rawtypes")
    private class CheckBoxUI extends WidgetUI implements ActionListener, PortListener {

        /** UID */
        private static final long serialVersionUID = -5106178045019582395L;

        /** Swing component for visual representation */
        private List<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();

        @SuppressWarnings("unchecked")
        private CheckBoxUI() {
            super(RenderMode.Swing); // use Swing render mode
            setLayout(new BorderLayout());
            elements.addChangeListener(this); // register as listener at port
            widgetPropertiesChanged(); // call properties changed method
            portChanged(null, null); // set initial value
        }

        @Override
        public void widgetPropertiesChanged() {
            value.publish(calculateOutput());
        }

        public void actionPerformed(ActionEvent e) {
            value.publish(calculateOutput());
        }

        /** Calculate bit vector from selected check boxes*/
        private int calculateOutput() {
            int i = 1, res = 0;
            for (JCheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected())
                    res += i;
                i *= 2;
            }
            if (invert_output)
                res = ~res;
            return res;
        }

        /** Create and display list of check boxes based on input string list*/
        @Override
        public void portChanged(AbstractPort origin, Object val) {
            if (val != null) {
                ContainsStrings strings = (ContainsStrings)val;
                removeAll();
                checkBoxes.clear();
                for (int i = 0; i < strings.stringCount(); ++i) {
                    checkBoxes.add(new JCheckBox(strings.getString(i).toString()));
                    checkBoxes.get(i).addActionListener(this);
                    add(checkBoxes.get(i), BorderLayout.CENTER); // add check box the center
                }
                if (checkBoxes.size() >= 1) {
                    GridLayout layout = new GridLayout(checkBoxes.size(), 1);
                    layout.setVgap(5);
                    setLayout(layout);
                }
                validate();
            }
        }

    }
}
