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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import org.finroc.tools.gui.FinrocGUI;
import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.util.embeddedfiles.EmbeddedFile;
import org.finroc.tools.gui.util.embeddedfiles.ValidExtensions;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.CoreBoolean;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 */
public class Button extends Widget {

    /** UID */
    private static final long serialVersionUID = 1357256003356L;

    /** Button output ports */
    public WidgetOutput.Numeric pressCounter;
    public WidgetOutput.Numeric emitValue;
    public WidgetOutput.CC<CoreBoolean> buttonPressed;

    /** Button parameters */
    public String text = "Button";
    @ValidExtensions({ "jpg", "png", "gif" })
    public EmbeddedFile iconFile;
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
            return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
        }
        if (forPort == buttonPressed) {
            return suggestion.derive(CoreBoolean.TYPE).derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
        }
        return null;
    }

    public String toString() {
        return "Button" + (text.length() == 0 ? "" : (" (\"" + text + "\")"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    class ButtonUI extends WidgetUI implements ActionListener, MouseListener, PortListener {

        /** UID */
        private static final long serialVersionUID = -9876567882234222L;

        private AbstractButton button;

        ButtonUI() {
            super(RenderMode.Swing);
            setLayout(new BorderLayout());
            emitValue.addChangeListener(this);
            buttonPressed.addChangeListener(this);
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
            if (iconFile != null) {
                try {
                    Icon icon = new ImageIcon(ImageIO.read(iconFile.getInputStream(getRoot().getEmbeddedFileManager())));
                    button.setIcon(icon);
                } catch (Exception e) {
                    FinrocGUI.logDomain.log(LogLevel.ERROR, toString(), e);
                }
            }
            portChanged(null, null);
        }

        public void actionPerformed(ActionEvent e) {
            pressCounter.publish(pressCounter.getInt() + 1);
            if (button instanceof JToggleButton) {
                emitValue.publish(((JToggleButton)button).isSelected() ? emitValuePush : emitValueRelease);
                buttonPressed.publish(CoreBoolean.getInstance(((JToggleButton)button).isSelected()));
            }
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {
            if (button instanceof JButton) {
                emitValue.publish(emitValuePush);
                buttonPressed.publish(CoreBoolean.TRUE);
            }
        }
        public void mouseReleased(MouseEvent e) {
            if (button instanceof JButton) {
                emitValue.publish(emitValueRelease);
                buttonPressed.publish(CoreBoolean.FALSE);
            }
        }

        @Override
        public void portChanged(AbstractPort origin, Object value) {
            if (toggleButton) {
                if (origin == emitValue.getPort()) {
                    double val = emitValue.getDouble();
                    button.setSelected(Math.abs(val - emitValuePush) < Math.abs(val - emitValueRelease));
                } else if (origin == buttonPressed.getPort()) {
                    CoreBoolean b = (CoreBoolean)value;
                    button.setSelected(b.get());
                }
            }
        }
    }
}
