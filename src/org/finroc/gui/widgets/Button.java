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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetOutput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortListener;


/**
 * @author max
 *
 */
public class Button extends Widget {

    /** UID */
    private static final long serialVersionUID = 1357256003356L;

    /** Button output ports */
    public WidgetOutput.Numeric pressCounter;
    public WidgetOutput.Numeric emitValue;

    /** Button parameters */
    public String text = "Button";
    public double emitValuePush = 1;
    public double emitValueRelease = 0;
    public boolean toggleButton = false;

    @Override
    protected WidgetUI createWidgetUI() {
        return new ButtonUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (forPort == emitValue) {
            return suggestion.derive(suggestion.flags | PortFlags.ACCEPTS_REVERSE_DATA_PUSH);
        }
        return null;
    }

    public String toString() {
        return "Button" + (text.length() == 0 ? "" : (" (\"" + text + "\")"));
    }

    class ButtonUI extends WidgetUI implements ActionListener, MouseListener, CCPortListener<CoreNumber> {

        /** UID */
        private static final long serialVersionUID = -9876567882234222L;

        private AbstractButton button;

        ButtonUI() {
            super(RenderMode.Swing);
            setLayout(new BorderLayout());
            emitValue.addChangeListener(this);
            widgetPropertiesChanged();
            //outputValueChanged(null);
        }

        /*public boolean isWidgetFocusable() {
            return true;
        }*/

        @Override
        public void widgetPropertiesChanged() {
            if (button != null) {
                remove(button);
            }
            if (!toggleButton) {
                button = new JButton();
            } else {
                button = new JToggleButton();
            }
            add(button, BorderLayout.CENTER);
            button.addActionListener(this);
            button.addMouseListener(this);
            init(button);
            button.setText(text);
            //button.setBackground(new Color(0,0,1));
            //emitValue.setValue(emitValueRelease);
            portChanged(null, null);
        }

        public void actionPerformed(ActionEvent e) {
            pressCounter.publish(pressCounter.getInt() + 1);
            if (button instanceof JToggleButton) {
                emitValue.publish(((JToggleButton)button).isSelected() ? emitValuePush : emitValueRelease);
            }
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {
            if (button instanceof JButton) {
                emitValue.publish(emitValuePush);
            }
        }
        public void mouseReleased(MouseEvent e) {
            if (button instanceof JButton) {
                emitValue.publish(emitValueRelease);
            }
        }

        @Override
        public void portChanged(CCPortBase origin, CoreNumber value) {
            if (toggleButton) {
                double val = emitValue.getDouble();
                button.setSelected(Math.abs(val - emitValuePush) < Math.abs(val - emitValueRelease));
            }
        }
    }
}
