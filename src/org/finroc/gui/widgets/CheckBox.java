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

import javax.swing.JCheckBox;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetOutput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;

import org.finroc.core.datatype.CoreBoolean;
import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortListener;


/**
 * @author max
 *
 */
public class CheckBox extends Widget {

    /** UID */
    private static final long serialVersionUID = -9245824582325601L;

    /** CheckBox output port */
    private WidgetOutput.Numeric value;

    /** Boolean output */
    private WidgetOutput.CC<CoreBoolean> boolValue;

    /** CheckBox text */
    private String text = "CheckBox";

    @Override
    protected WidgetUI createWidgetUI() {
        return new CheckBoxUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (forPort == boolValue) {
            suggestion = suggestion.derive(CoreBoolean.TYPE);
        }
        return suggestion.derive(suggestion.flags | PortFlags.ACCEPTS_REVERSE_DATA_PUSH);
    }

    private class CheckBoxUI extends WidgetUI implements ActionListener, CCPortListener<CoreNumber> {

        /** UID */
        private static final long serialVersionUID = -5106178045019582395L;

        /** Swing component for visual representation */
        private JCheckBox checkBox;

        private CheckBoxUI() {
            super(RenderMode.Swing); // use Swing render mode
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();  // create JCheckBox
            checkBox.addActionListener(this);  // register as listener
            add(checkBox, BorderLayout.CENTER); // add check box the centre
            value.addChangeListener(this); // register as listener at port
            widgetPropertiesChanged(); // call properties changed method
            portChanged(null, null); // set initial value
        }

        @Override
        public void widgetPropertiesChanged() {
            checkBox.setText(text);
        }

        public void actionPerformed(ActionEvent e) {
            value.publish(checkBox.isSelected() ? 1 : 0);
            boolValue.getPort().publish(CoreBoolean.getInstance(checkBox.isSelected()));
        }

        @Override
        public void portChanged(CCPortBase origin, CoreNumber value2) {
            if (value.getPort().isConnected()) {
                checkBox.setSelected(value.getDouble() != 0);
            } else if (boolValue.getPort().isConnected()) {
                checkBox.setSelected(boolValue.getPort().getAutoLocked().get());
                releaseAllLocks();
            }
        }
    }
}
