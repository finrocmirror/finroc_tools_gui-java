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
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.finroc.gui.GUIPanel;
import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetPorts;
import org.finroc.gui.WidgetPortsListener;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.commons.fastdraw.BufferedImageARGBColorAdd;
import org.finroc.gui.commons.fastdraw.BufferedImageARGBColorable;
import org.finroc.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.gui.themes.Themes;
import org.finroc.gui.util.propertyeditor.PropertyList;
import org.finroc.plugin.datatype.StringList;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;

public class LED extends Widget {

    /** UID */
    private static final long serialVersionUID = -2456813467442L;

    public WidgetPorts<WidgetInput.Numeric> signals = new WidgetPorts<WidgetInput.Numeric>("signal", 2, WidgetInput.Numeric.class, this);

    @SuppressWarnings("unused")
    private transient Color ledColor = Themes.getCurTheme().ledColor(); // outdated - kept for backward compatibility
    public StringList descriptions = new StringList(); // outdated - kept for backward compatibility

    float fontSize = 18;

    PropertyList<LEDProperty> leds = new PropertyList<LEDProperty>(LEDProperty.class, 25);

    /** Raw-Icons only need to be initialized once application-wide */
    static ImageIcon ledOff;
    static BufferedImageARGBColorable ledOn;

    public LED() {
        setBackground(Themes.getCurTheme().panelBackground());
        leds.add(new LEDProperty("LED 1"));
        leds.add(new LEDProperty("LED 2"));
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new LEDWidgetUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion;
    }

    @Override
    public void restore(GUIPanel parent) {
        // import legacy widget settings
        if (descriptions != null && descriptions.size() > 0) {
            leds = new PropertyList<LEDProperty>(LEDProperty.class, 25);
            for (String s : descriptions) {
                leds.add(new LEDProperty(s));
            }
        }

        super.restore(parent);
    }

    /** Properties of one bar */
    public static class LEDProperty implements Serializable {

        /** UID */
        private static final long serialVersionUID = 6464808920773228472L;

        private Color off = new Color(0, 0, 0), on = Themes.getCurTheme().ledColor();
        private double lowerLimit = 1, upperLimit = Double.POSITIVE_INFINITY;
        String label = "LED";

        public LEDProperty() {}

        public LEDProperty(String label) {
            this.label = label;
        }
    }

    class LEDWidgetUI extends WidgetUI implements WidgetPortsListener {

        /** UID */
        private static final long serialVersionUID = 1667635834447207L;

        List<LEDPanel> panels = new ArrayList<LEDPanel>();

        LEDWidgetUI() {
            super(RenderMode.Swing);

            // init icons
            if (ledOff == null) {
                try {
                    ledOff = new ImageIcon(LED.class.getResource("led-off.png"));
                    ledOn = new BufferedImageARGBColorAdd(LED.class.getResource("led-on.png"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            signals.addChangeListener(this);
            widgetPropertiesChanged();
        }

        @Override
        public void portChanged(WidgetPorts<?> origin, AbstractPort port, Object value) {
            for (int i = 0; i < signals.size(); i++) {
                double d = signals.get(i).getDouble();
                LEDPanel pan = panels.get(i);
                pan.on = d >= pan.info.lowerLimit && d <= pan.info.upperLimit;
            }
            repaint();
        }


        @Override
        public void widgetPropertiesChanged() {

            int numberOfLEDs = leds.size();

            // adjust number of ports
            signals.setSize(numberOfLEDs);
            initPorts();

            // adjust number of panels
            setLayout(new GridLayout(numberOfLEDs, 1));
            while (panels.size() < numberOfLEDs) {
                LEDPanel p = new LEDPanel();
                panels.add(p);
                add(p);
            }
            while (panels.size() > numberOfLEDs) {
                remove(panels.remove(panels.size() - 1));
            }

            // update panels
            for (int i = 0; i < numberOfLEDs; i++) {
                panels.get(i).update(leds.get(i));
            }

            // redraw
            validate();
            portChanged(null, null, null);
        }

        class LEDPanel extends JPanel implements Icon {

            /** UID */
            private static final long serialVersionUID = -5589317268272363967L;

            private JLabel jl;
            private boolean on;
            BufferedImageRGB renderBuffer;  // buffer for rendering
            BufferedImageRGB renderBufferOff;  // buffer for rendering
            LEDProperty info = null;

            public LEDPanel() {
                setLayout(new BorderLayout());
                jl = new JLabel();
                jl.setFont(jl.getFont().deriveFont(fontSize));
                jl.setBorder(BorderFactory.createEmptyBorder(0, 7, 0, 0));
                jl.setHorizontalAlignment(SwingConstants.LEFT);
                jl.setIcon(this);
                setBackground(LED.this.getBackground());
                add(jl, BorderLayout.CENTER);
                renderBuffer = new BufferedImageRGB(ledOn.getSize());
                renderBufferOff = new BufferedImageRGB(ledOn.getSize());
            }

            public void update(LEDProperty info) {
                jl.setText(info.label);
                jl.setForeground(LED.this.getLabelColor());
                if (jl.getFont().getSize() != fontSize) {
                    jl.setFont(jl.getFont().deriveFont(fontSize));
                }
                setBackground(LED.this.getBackground());

                // update Icons
                renderBuffer.fill(LED.this.getBackground().getRGB());
                ledOn.blitToInColor(renderBuffer, new Point(0, 0), ledOn.getBounds(), info.on.getRGB());
                renderBufferOff.fill(LED.this.getBackground().getRGB());
                ledOn.blitToInColor(renderBufferOff, new Point(0, 0), ledOn.getBounds(), info.off.getRGB());
                this.info = info;
            }

            public int getIconHeight() {
                return ledOff.getIconHeight();
            }

            public int getIconWidth() {
                return ledOff.getIconWidth();
            }

            public void paintIcon(Component c, Graphics g, int x, int y) {
                if (on) {
                    renderBuffer.paintIcon(c, g, x, y);
                } else if (info.off.equals(Color.BLACK)) {
                    ledOff.paintIcon(c, g, x, y);
                } else {
                    renderBufferOff.paintIcon(c, g, x, y);
                }
            }
        }
    }
}

