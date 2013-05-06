//
// You received this file as part of Finroc
// A Framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.CoreBoolean;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;

/**
 * @author Max Reichardt
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
        return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
    }

    @SuppressWarnings("rawtypes")
    private class CheckBoxUI extends WidgetUI implements ActionListener, PortListener {

        /** UID */
        private static final long serialVersionUID = -5106178045019582395L;

        /** Swing component for visual representation */
        private JCheckBox checkBox;

        @SuppressWarnings("unchecked")
        private CheckBoxUI() {
            super(RenderMode.Swing); // use Swing render mode
            setLayout(new BorderLayout());
            checkBox = new JCheckBox();  // create JCheckBox
            checkBox.addActionListener(this);  // register as listener
            add(checkBox, BorderLayout.CENTER); // add check box the centre
            value.addChangeListener(this); // register as listener at port
            boolValue.addChangeListener(this); // register as listener at port
            widgetPropertiesChanged(); // call properties changed method
            portChanged(null, null); // set initial value
        }

        @Override
        public void widgetPropertiesChanged() {
            checkBox.setText(text);
            checkBox.setBackground(CheckBox.this.getBackground());
            checkBox.setForeground(getLabelColor(CheckBox.this));
        }

        public void actionPerformed(ActionEvent e) {
            value.publish(checkBox.isSelected() ? 1 : 0);
            boolValue.asPort().publish(CoreBoolean.getInstance(checkBox.isSelected()));
        }

        @Override
        public void portChanged(AbstractPort origin, Object val) {
            if (value.getPort().isConnected()) {
                checkBox.setSelected(value.getDouble() != 0);
            } else if (boolValue.getPort().isConnected()) {
                checkBox.setSelected(boolValue.asPort().getAutoLocked().get());
                releaseAllLocks();
            }
        }
    }
}
