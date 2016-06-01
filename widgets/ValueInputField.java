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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.finroc.tools.gui.GUIPanel;
import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.datatype.DataTypeReference;
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

    /** Output port with custom type */
    public WidgetOutput.Custom value;

    /** Background colors of widget */
    private static final Color EDITING_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.white;

    @Override
    protected WidgetUI createWidgetUI() {
        return new ValueInputFieldUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE).derive(value.getType() == null ? CoreNumber.TYPE : null);
    }

    @SuppressWarnings("rawtypes")
    class ValueInputFieldUI extends WidgetUI implements CaretListener, ActionListener, PortListener, Runnable {

        /** UID */
        private static final long serialVersionUID = -3628234631895609L;

        /** For this duration (in ms) after user presses a key, widget value will not be overwritten */
        private static final long GRACE_PERIOD = 2000;

        private JTextField textfield;

        /** Last time when user pressed a key */
        private long lastInputTime = 0;

        /** Flag to indicate not to react on caret event */
        private boolean ignoreUpdate;

        ValueInputFieldUI() {
            super(RenderMode.Swing);
            textfield = new JTextField();
            textfield.setBackground(DEFAULT_BACKGROUND_COLOR);
            setLayout(new BorderLayout());
            add(textfield, BorderLayout.CENTER);
            widgetPropertiesChanged();
            textfield.addCaretListener(this);
            textfield.addActionListener(this);
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

        @Override
        public void caretUpdate(CaretEvent e) {
            if (ignoreUpdate) {
                return;
            }
            lastInputTime = System.currentTimeMillis();
            textfield.setBackground(textfield.getText().equals(value.asPort().getAutoLocked().toString()) ? DEFAULT_BACKGROUND_COLOR : EDITING_BACKGROUND_COLOR);
            this.releaseAllLocks();
        }

        public synchronized void actionPerformed(ActionEvent e) {
            try {
                value.publishFromString(textfield.getText());
                lastInputTime = 0;
                textfield.setBackground(DEFAULT_BACKGROUND_COLOR);
            } catch (Exception ex) {
                // not possible ... never mind
            }
        }

        @Override
        public void portChanged(AbstractPort origin, Object value) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() - GRACE_PERIOD > lastInputTime) {
                ignoreUpdate = true;
                textfield.setText(value == null ? "" : ("" + value.asPort().getAutoLocked().toString()));
                this.releaseAllLocks();
                textfield.setBackground(DEFAULT_BACKGROUND_COLOR);
                ignoreUpdate = false;
            }
        }
    }
}
