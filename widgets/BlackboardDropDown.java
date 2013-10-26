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

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.plugins.data_types.mca.StringBlackboardBuffer;

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

    /** Input Blackboard contains list elements as strings */
    public WidgetInput.Std<StringBlackboardBuffer> elements;

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion,
            WidgetPort < ? > forPort) {
        if (forPort == elements) {
            return suggestion.derive(StringBlackboardBuffer.TYPE);
        }
        return suggestion;
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new BlackboardDropDownUI();
    }

    class BlackboardDropDownUI extends WidgetUI implements
        PortListener<StringBlackboardBuffer>, ActionListener {
        /** UID */
        private static final long serialVersionUID = -4319918865786225484L;

        JComboBox comboBox;

        public BlackboardDropDownUI() {
            super(RenderMode.Swing);
            setLayout(new BorderLayout());
            comboBox = new JComboBox();
            add(comboBox);

            // comboxbox change listner
            comboBox.addActionListener(this);

            // input black change listner
            elements.addChangeListener(this);
        }

        @Override
        protected boolean isWidgetFocusable() {
            return true;
        }

        public void actionPerformed(ActionEvent e) {
            selectedIndex.publish(comboBox.getSelectedIndex());
        }

        @Override
        public void portChanged(AbstractPort origin, StringBlackboardBuffer value) {
            comboBox.removeAllItems();
            if (value != null && value.stringCount() > 0) {
                for (int i = 0; i < value.stringCount(); i++) {
                    comboBox.addItem(value.getString(i).toString());
                }

            }
        }
    }
}
