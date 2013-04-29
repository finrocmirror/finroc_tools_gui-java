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

import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;


/**
 * @author Max Reichardt
 *
 */
public class ValueInputField extends Widget {

    /** UID */
    private static final long serialVersionUID = -4427052569245621L;

    /** Button output port */
    public WidgetOutput.Numeric value;

    @Override
    protected WidgetUI createWidgetUI() {
        return new ValueInputFieldUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
    }


    class ValueInputFieldUI extends WidgetUI implements CaretListener, PortListener<CoreNumber> {

        /** UID */
        private static final long serialVersionUID = -3628234631895609L;

        private JTextField textfield;
        boolean ignoreUpdate;

        ValueInputFieldUI() {
            super(RenderMode.Swing);
            textfield = new JTextField();
            setLayout(new BorderLayout());
            add(textfield, BorderLayout.CENTER);
            widgetPropertiesChanged();
            textfield.addCaretListener(this);
            value.addChangeListener(this);
            portChanged(null, null);
        }

        @Override
        protected boolean isWidgetFocusable() {
            return true;
        }

        @Override
        public void widgetPropertiesChanged() {
        }

        public synchronized void caretUpdate(CaretEvent e) {
            try {
                if (!ignoreUpdate) {
                    value.publish(Double.parseDouble(textfield.getText()));
                }
            } catch (Exception ex) {
                // not possible ... never mind
            }
        }

        @Override
        public void portChanged(AbstractPort origin, CoreNumber value) {
            ignoreUpdate = true;
            textfield.setText(value == null ? "" : ("" + value.toString()));
            ignoreUpdate = false;
        }
    }
}
