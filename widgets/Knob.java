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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import org.finroc.plugins.data_types.Angle;
import org.finroc.tools.gui.FinrocGUI;
import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.commons.fastdraw.SVG;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.PortListener;
import org.rrlib.finroc_core_utils.log.LogLevel;
import org.rrlib.finroc_core_utils.serialization.NumericRepresentation;


/**
 * @author max
 *
 */
public class Knob extends Widget {

    /** UID */
    private static final long serialVersionUID = 193567299267933L;

    /** Value output port */
    private WidgetOutput.Std<Angle> value;

    /** Current value input */
    private WidgetInput.Numeric measuredValue;

    /** Parameters */
    private double minimum = 0, maximum = 360;

    /** Where does scale begin and end? (degree) */
    private double scaleBeginAngle = 0, scaleArcLength = 360;

    /** Number of segments scale should have */
    private int scaleSegments = 8;

    /** Clockwise knob? */
    private boolean clockwise = true;

    /** Ring Background */
    private Color ringBackground = Color.black;

    /** Ring Indication Color */
    private Color ringIndication = new Color(255, 102, 0);

    /** Show ticks? Show labels? */
    private boolean showTicks = true, showLabels = true;

    /** Current value */
    private transient double currentValue;

    /** SVG resources */
    private static SVG svgBack, svgIndicator;
    private static int svgWidth, svgHeight; // offset of images etc.
    private static double svgSize, halfSVGSize;

    @Override
    protected WidgetUI createWidgetUI() {
        return new KnobUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (forPort == value) {
            return suggestion.derive(suggestion.flags | PortFlags.ACCEPTS_REVERSE_DATA_PUSH).derive(Angle.TYPE);
        }
        return suggestion;
    }

    public static synchronized void initSVG() {
        if (svgIndicator != null) {
            return;
        }
        try {
            URL u = LCD.class.getResource("knob-background-gregor.svg");
            if (u == null) {
                u = LCD.class.getResource("knob-background-simple-max.svg");
            }
            svgBack = SVG.createInstance(u, true);
            svgIndicator = SVG.createInstance(LCD.class.getResource("knob-indicator.svg"), true);
            svgWidth = (int)svgBack.getBounds().getWidth() + 4;
            svgHeight = (int)svgBack.getBounds().getHeight() + 4;
            svgSize = Math.min(svgWidth, svgHeight);
            halfSVGSize = svgSize / 2;
        } catch (Exception e) {
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, "Knob", e);
        }
    }

    @SuppressWarnings("rawtypes")
    class KnobUI extends WidgetUI implements PortListener {

        /** UID */
        private static final long serialVersionUID = -226842649519588097L;

        private boolean propChange = false;
        private BufferedImage backgroundBuffer, knobBuffer;
        private double factor; // scale factor
        private double angleDiff;
        private MainPanel mainPanel = new MainPanel();

        @SuppressWarnings("unchecked")
        KnobUI() {
            super(RenderMode.Swing);
            setOpaque(useOpaquePanels());
            initSVG();
            widgetPropertiesChanged();
            value.addChangeListener(this);
            measuredValue.addChangeListener(this);
            portChanged(null, null);
            mainPanel.setOpaque(false);
            setLayout(new BorderLayout());
            add(mainPanel, BorderLayout.CENTER);
        }

        @Override
        public void widgetPropertiesChanged() {
            // TODO: value sanity check
            scaleArcLength = Math.max(0, Math.min(360, scaleArcLength));
            if (maximum < minimum) {
                double tmp = minimum;
                minimum = maximum;
                maximum = tmp;
            } else if (maximum == minimum) {
                maximum = minimum + 1;
            }
            propChange = true;
            angleDiff = (scaleArcLength / (maximum - minimum));
            setChanged();
            repaint();
        }

        @Override
        public void portChanged(AbstractPort origin, Object value) {
            if (value != null) {
                //currentValue = value.
                //setChanged();
                repaint();
            }
        }

        public Angle valueToAngle(double value) {
            Angle a = new Angle();
            a.setDeg((scaleBeginAngle + angleDiff * (value - minimum)));
            return a;
        }

        public double angleToValue(Angle angle) {
            Angle a = new Angle();
            a.setDeg((angle.getUnsignedDeg() - scaleBeginAngle));
            return minimum + a.getUnsignedDeg() / angleDiff;
        }

        private class MainPanel extends JPanel implements MouseInputListener {

            /** UID */
            private static final long serialVersionUID = -2718129091250046008L;

            public MainPanel() {
                this.addMouseListener(this);
                this.addMouseMotionListener(this);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Dimension renderSize = getRenderSize();
                double size = Math.min(renderSize.width, renderSize.height);
                double size12 = size / 12;
                factor = size / svgSize;
                if (propChange || backgroundBuffer == null || renderSize.width != backgroundBuffer.getWidth() || renderSize.height != backgroundBuffer.getHeight()) {
                    if (backgroundBuffer == null || renderSize.width != backgroundBuffer.getWidth() || renderSize.height != backgroundBuffer.getHeight()) {
                        backgroundBuffer = new BufferedImage(renderSize.width, renderSize.height, BufferedImage.TYPE_INT_ARGB);
                        knobBuffer = new BufferedImage(renderSize.width, renderSize.height, BufferedImage.TYPE_INT_ARGB);
                    }

                    // Draw background
                    Graphics2D g2 = backgroundBuffer.createGraphics();
                    g2.setColor(new Color(ringBackground.getRed(), ringBackground.getGreen(), ringBackground.getBlue(), 255));
                    g2.fillOval((int)size12, (int)size12, (int)(size12 * 10.5), (int)(size12 * 10.5));
                    g2.dispose();

                    // Draw knob
                    g2 = knobBuffer.createGraphics();
                    g2.setColor(new Color(0, 0, 0, 0));
                    g2.fillRect(-1, -1, renderSize.width + 2, renderSize.height + 2);
                    propChange = false;

                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.setClip(0, 0, renderSize.width, renderSize.height);

                    g2.scale(factor, factor);
                    svgBack.paint(g2);

                    // draw scale
                    if (showTicks) {

                        // draw ring
                        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, 0));
                        //g2.setColor(new Color(0, 0, 0, 128));
                        g2.setColor(new Color(0, 0, 0, 128));
                        Angle start = valueToAngle(minimum);
                        final double ringInset = 19;
                        g2.drawArc((int)ringInset + 1, (int)ringInset + 1, (int)(svgSize - 2 * ringInset), (int)(svgSize - 2 * ringInset), (clockwise ? (-1) : 1) * (int)start.getUnsignedDeg(), (clockwise ? (-1) : 1) * (int)(scaleArcLength));

                        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, 0));
                        //g2.setColor(new Color(0, 0, 0, 128));
                        //g2.setColor(new Color(60, 60, 60));
                        g2.translate(halfSVGSize + 1, halfSVGSize + 1);
                        double scaleTickInterval = (maximum - minimum) / scaleSegments;
                        AffineTransform at = g2.getTransform();
                        g2.rotate((clockwise ? 1 : -1) * ((scaleBeginAngle / 180) * Math.PI));
                        double currentValue = minimum;
                        for (int i = 0; i < scaleSegments; i++, currentValue += scaleTickInterval) {
                            if (currentValue < -(maximum - minimum) / 80 || currentValue > (maximum - minimum) / 80) {
                                g2.setColor(getTickColor(g2, halfSVGSize * 0.94, 0));
                                g2.drawLine((int)(halfSVGSize * 0.865), 0, (int)(halfSVGSize * 0.945), 0);
                            }
                            g2.rotate((clockwise ? -1 : 1) * ((-angleDiff * scaleTickInterval) / 180) * Math.PI);
                        }
                        if (scaleArcLength != 360) {
                            g2.setColor(getTickColor(g2, halfSVGSize * 0.94, 0));
                            g2.drawLine((int)(halfSVGSize * 0.865), 0, (int)(halfSVGSize * 0.945), 0);
                        }

                        if (minimum <= 0 && maximum >= 0) {
                            g2.setTransform(at);
                            Angle a = valueToAngle(0);
                            g2.rotate((clockwise ? 1 : -1) * a.getUnsignedRad());
                            g2.setColor(getTickColor(g2, halfSVGSize * 0.94, 0));
                            g2.fillOval((int)(halfSVGSize * 0.855), -(int)(halfSVGSize * 0.05), (int)(halfSVGSize * 0.1), (int)(halfSVGSize * 0.1));
                        }
                    }

                    g2.dispose();
                }

                // draw ring
                g.drawImage(backgroundBuffer, 0, 0, null);
                g.setColor(ringIndication);
                Angle start = valueToAngle(minimum);
                double length = angleDiff * ((measuredValue.getPort().isConnected() ? getMeasuredValue() : currentValue) - minimum);
                if (minimum <= 0 && maximum >= 0) {
                    start = valueToAngle(0);
                    length = angleDiff * ((measuredValue.getPort().isConnected() ? getMeasuredValue() : currentValue));
                }
                if (clockwise) {
                    g.fillArc((int)size12, (int)size12, (int)(size12 * 10), (int)(size12 * 10), -(int)start.getUnsignedDeg(), -(int)length);
                } else {
                    g.fillArc((int)size12, (int)size12, (int)(size12 * 10), (int)(size12 * 10), (int)start.getUnsignedDeg(), (int)length);
                }

                // draw knob
                g.drawImage(knobBuffer, 0, 0, null);

                // draw indicator
                Graphics2D g2d = (Graphics2D)g;
                AffineTransform tf = g2d.getTransform();
                g2d.scale(factor, factor);
                g2d.translate(halfSVGSize + 1, halfSVGSize + 1);
                g2d.rotate(0.5 * Math.PI);
                Angle a = valueToAngle(currentValue);
                g2d.rotate(clockwise ? a.getUnsignedRad() : -a.getUnsignedRad());
                g2d.translate(-(halfSVGSize + 1), -(halfSVGSize + 1));
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                svgIndicator.paint(g2d);
                g2d.setTransform(tf);
            }

            private double getMeasuredValue() {
                NumericRepresentation cn = measuredValue.getAutoLocked();
                double measured = cn.getNumericRepresentation().doubleValue();
                if (cn instanceof Angle) {
                    Angle a = (Angle)cn;
                    measured = a.getSignedDeg();
                    while (measured < minimum && (measured + 360 <= maximum || Math.abs((measured + 360) - maximum) < Math.abs(measured - minimum))) {
                        measured += 360;
                    }
                    while (measured > maximum && (measured - 360 >= minimum || Math.abs((measured - 360) - minimum) < Math.abs(measured - maximum))) {
                        measured -= 360;
                    }
                } else {
                    measured = Math.max(minimum, Math.min(maximum, measured));
                }
                releaseAllLocks();
                return measured;
            }

            /** Helper for above to determine tick colors */
            public Color getTickColor(Graphics2D g, double x, double y) {
                Point2D.Double result = new Point2D.Double();
                g.getTransform().transform(new Point2D.Double(x, y), result);
                Color tickColor = new Color(knobBuffer.getRGB((int)result.x, (int)result.y));
                //System.out.println(tickColor.toString());
                return new Color(tickColor.getRed() / 2, tickColor.getGreen() / 2, tickColor.getBlue() / 2);
            }

            @Override
            public void mouseClicked(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {
                setPos(e.getPoint());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setPos(e.getPoint());
            }

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}

            @Override
            public void mouseDragged(MouseEvent e) {
                setPos(e.getPoint());
            }

            @Override
            public void mouseMoved(MouseEvent e) {}

            private void setPos(Point p) {
                Dimension renderSize = getRenderSize();
                double size = Math.min(renderSize.width, renderSize.height) / 2;
                double angle = -Math.atan2(p.y - size, p.x - size);
                Angle a = new Angle();
                a.setRad(clockwise ? -angle : angle);
                double v = angleToValue(a);
                if (v >= minimum && v <= maximum) {
                    currentValue = v;
                } else {
                    double angleToStart = Math.abs(a.getUnsignedDeg() - scaleBeginAngle);
                    double angleToEnd = Math.abs(a.getUnsignedDeg() - (scaleBeginAngle + scaleArcLength));
                    angleToStart = angleToStart > 180 ? 360 - angleToStart : angleToStart; // take shortest angle
                    angleToEnd = angleToEnd > 180 ? 360 - angleToEnd : angleToEnd; // take shortest angle
                    currentValue = (angleToEnd < angleToStart) ? maximum : minimum;
                }
                Angle publish = value.getUnusedBuffer();
                publish.setDeg(currentValue);
                value.publish(publish);
                repaint();
            }
        }
    }
}

