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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetPorts;
import org.finroc.tools.gui.WidgetPortsListener;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.themes.Theme;
import org.finroc.tools.gui.themes.Themes;
import org.finroc.tools.gui.util.gui.RulerOfTheForest;
import org.finroc.tools.gui.util.propertyeditor.PropertyList;
import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.thread.LoopThread;
import org.rrlib.finroc_core_utils.log.LogLevel;

import org.finroc.plugins.data_types.PartWiseLinearFunction;
import org.rrlib.finroc_core_utils.rtti.DataType;
import org.rrlib.finroc_core_utils.rtti.DataTypeBase;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;

import com.thoughtworks.xstream.annotations.XStreamAlias;

public class Oscilloscope extends Widget {

    /** UID */
    private static final long serialVersionUID = 1892231028811778314L;

    WidgetPorts<WidgetInput.Numeric> signals = new WidgetPorts<WidgetInput.Numeric>("signal", 1, WidgetInput.Numeric.class, this);
    double timeScaleMaxInMs = 10000;
    double leftScaleMin = 0;
    double leftScaleMax = 110;
    double rightScaleMin = 0;
    double rightScaleMax = 110;
    PropertyList<SignalProperty> channels = new PropertyList<SignalProperty>(SignalProperty.class, 25);
    long timerIntervalInMs = 100;

    public Oscilloscope() {
        channels.add(new SignalProperty());
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new OscilloscopeUI();
    }

    @Override
    protected void setDefaultColors() {
        useAlternativeColors();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion;
    }

    @XStreamAlias("OscilloscopeSignal")
    public static class SignalProperty implements Serializable {

        /** UID */
        private static final long serialVersionUID = 1190544484724927518L;

        private static enum DrawMode { dots, lines }
        private static enum Scale { left, right }

        DrawMode drawMode = DrawMode.lines;
        Scale useScale = Scale.left;
        Color color = getDefaultColor(Theme.DefaultColor.OSCILLOSCOPE_FOREGROUND);
    }

    class OscilloscopeUI extends WidgetUI implements WidgetPortsListener {

        /** UID */
        private static final long serialVersionUID = 3206071864061439004L;

        RulerOfTheForest left, bottom, right;
        RulerOfTheForest.RulerLabel bottomLeft, bottomRight;
        OscilloscopeMainPanel main;
        boolean skip;
        List<OscilloscopeFunction> functions = new ArrayList<OscilloscopeFunction>();
        JLabel label = new JLabel();

        OscilloscopeThread thread;

        public OscilloscopeUI() {
            super(RenderMode.Swing);

            // create GUI elements
            setLayout(new BorderLayout());
            bottom = new RulerOfTheForest(SwingConstants.HORIZONTAL, 0.25, true, 3);
            left = new RulerOfTheForest(SwingConstants.VERTICAL, 0.25, false, 13);
            right = new RulerOfTheForest(SwingConstants.VERTICAL, 0.25, true, 13);
            JPanel south = new JPanel();
            south.setOpaque(useOpaquePanels());
            south.setLayout(new BorderLayout());
            bottomLeft = new RulerOfTheForest.RulerLabel(RulerOfTheForest.RulerLabel.Position.SW);
            south.add(bottomLeft, BorderLayout.WEST);
            south.add(bottom, BorderLayout.CENTER);
            bottomRight = new RulerOfTheForest.RulerLabel(RulerOfTheForest.RulerLabel.Position.SE);
            south.add(bottomRight, BorderLayout.EAST);
            label.setVisible(false);
            add(label, BorderLayout.PAGE_START);
            add(left, BorderLayout.WEST);
            add(right, BorderLayout.EAST);
            add(south, BorderLayout.SOUTH);
            main = new OscilloscopeMainPanel();
            add(main, BorderLayout.CENTER);

            // create thread
            thread = new OscilloscopeThread();
            thread.start();
            log(LogLevel.LL_DEBUG, logDomain, "Oscilloscope Thread started.");

            signals.addChangeListener(this);
            widgetPropertiesChanged();
        }

        @Override
        public void portChanged(WidgetPorts<?> origin, AbstractPort port, Object value) {
            this.setChanged();
            repaint();
        }

        @Override
        protected void paintChildren(Graphics g) {
            skip = true;
            super.paintChildren(g);
            skip = false;
            g.translate(main.getBounds().x, main.getBounds().y);
            main.paintComponent(g);
            g.translate(-main.getBounds().x, main.getBounds().y);
        }

        @Override
        public void widgetPropertiesChanged() {
            bottom.setMinAndMax(0, timeScaleMaxInMs);
            left.setMinAndMax(leftScaleMin, leftScaleMax);
            right.setMinAndMax(rightScaleMin, rightScaleMax);
            thread.setCycleTime(timerIntervalInMs);
            synchronized (functions) {
                signals.setSize(channels.size());
                initPorts();
                while (functions.size() < channels.size()) {
                    functions.add(new OscilloscopeFunction());
                }
                while (functions.size() > channels.size()) {
                    functions.remove(functions.size() - 1);
                }
            }

            if (Themes.nimbusLookAndFeel()) {
                Color c = getLabelColor(Oscilloscope.this);
                boolean alt = Themes.getCurTheme().getDefaultColor(Theme.DefaultColor.ALTERNATIVE_LABEL).equals(c);
                Color b = getDefaultColor(alt ? Theme.DefaultColor.ALTERNATIVE_BACKGROUND : Theme.DefaultColor.BACKGROUND);
                int rulerHeight = alt && Themes.nimbusLookAndFeel() ? RulerOfTheForest.RULERHEIGHT - 3 : RulerOfTheForest.RULERHEIGHT;
                left.setBackground(b);
                right.setBackground(b);
                bottom.setBackground(b);
                bottomLeft.setBackground(b);
                bottomRight.setBackground(b);
                left.setForeground(c);
                right.setForeground(c);
                bottom.setForeground(c);
                bottomLeft.setForeground(c);
                bottomRight.setForeground(c);
                left.setPreferredSize(rulerHeight);
                right.setPreferredSize(rulerHeight);
                bottom.setPreferredSize(rulerHeight);
                bottomLeft.setPreferredSize(new Dimension(rulerHeight, rulerHeight));
                bottomRight.setPreferredSize(new Dimension(rulerHeight, rulerHeight));
                if (getLabel() != null && getLabel().length() > 0) {
                    label.setText(getLabel());
                    label.setVisible(true);
                    label.setForeground(c);
                    label.setBorder(alt ? BorderFactory.createEmptyBorder(3, 7, 4, 5) : BorderFactory.createEmptyBorder(5, 8, 5, 5));
                } else {
                    label.setVisible(false);
                }
            }
        }

        class OscilloscopeThread extends LoopThread {

            long startTime, lastTime;

            public OscilloscopeThread() {
                super(timerIntervalInMs, false);
                startTime = System.currentTimeMillis();
                lastTime = startTime;
            }

            @Override
            public void mainLoopCallback() throws Exception {
                if (!isVisible()) {
                    stopLoop(); // disposed
                    log(LogLevel.LL_DEBUG, logDomain, "Oscilloscope Thread stopped.");
                    return;
                }

                try {
                    synchronized (functions) {
                        long time = System.currentTimeMillis();
                        for (int i = 0; i < functions.size(); i++) {
                            functions.get(i).addNewValue((time - startTime) % timeScaleMaxInMs, signals.get(i).getDouble(), (lastTime - startTime + 1) % timeScaleMaxInMs);
                        }
                        lastTime = time;
                    }
                    repaint();
                } catch (Exception e) {
                    log(LogLevel.LL_DEBUG_WARNING, logDomain, "Oscilloscope Thread skipped loop, because of temporary exception");
                }
            }
        }

        class OscilloscopeMainPanel extends JPanel {

            /** UID */
            private static final long serialVersionUID = -2946030738495285342L;

            public OscilloscopeMainPanel() {
                setBackground(getDefaultColor(Theme.DefaultColor.OSCILLOSCOPE_BACKGROUND));
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (skip) {
                    return;
                }

                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.setClip(0, 0, getWidth(), getHeight());

                // Minor lines
                g2d.setColor(getDefaultColor(Theme.DefaultColor.OSCILLOSCOPE_SCALE));
                for (Integer i : bottom.getMinorTicks()) {
                    g2d.drawLine(i, 0, i, getHeight() - 1);
                }
                for (Integer i : right.getMinorTicks()) {
                    g2d.drawLine(0, getHeight() - i - 1, getWidth() - 1, getHeight() - i - 1);
                }
                for (Integer i : left.getMinorTicks()) {
                    g2d.drawLine(0, getHeight() - i - 1, getWidth() - 1, getHeight() - i - 1);
                }

                // Vertical major lines
                g2d.setColor(getDefaultColor(Theme.DefaultColor.OSCILLOSCOPE_SCALE_MAJOR));
                for (Integer i : bottom.getMajorTicks()) {
                    g2d.drawLine(i, 0, i, getHeight() - 1);
                }
                for (Integer i : right.getMajorTicks()) {
                    g2d.drawLine(0, getHeight() - i - 1, getWidth() - 1, getHeight() - i - 1);
                }
                for (Integer i : left.getMajorTicks()) {
                    g2d.drawLine(0, getHeight() - i - 1, getWidth() - 1, getHeight() - i - 1);
                }

                // draw functions
                synchronized (functions) {
                    for (int i = 0; i < functions.size(); i++) {
                        List<PartWiseLinearFunction.Node> nodes = functions.get(i).getNodeList();
                        g2d.setColor(channels.get(i).color);

                        PartWiseLinearFunction.Node last = null;
                        boolean left = channels.get(i).useScale.equals(SignalProperty.Scale.left);
                        for (PartWiseLinearFunction.Node node : nodes) {
                            int xnow = (int)Math.round(getXCord(node.x));
                            int ynow = (int)Math.round(getYCord(node.y, left));
                            if (channels.get(i).drawMode.equals(SignalProperty.DrawMode.lines)) {
                                if (last != null) {
                                    int xlast = (int)Math.round(getXCord(last.x));
                                    int ylast = (int)Math.round(getYCord(last.y, left));
                                    g2d.drawLine(xlast, ylast, xnow, ynow);
                                }
                                last = node;
                                if (node.x == functions.get(i).curTime) {
                                    last = null;
                                }
                            } else {
                                g2d.drawLine(xnow, ynow, xnow, ynow);
                            }
                        }
                    }
                }
            }

            public double getYCord(double y, boolean leftScale) {
                double yRel = 0;
                if (leftScale) {
                    yRel = (y - leftScaleMin) / (leftScaleMax - leftScaleMin);
                } else {
                    yRel = (y - rightScaleMin) / (rightScaleMax - rightScaleMin);
                }
                return ((double)(getHeight() - 1)) * (1 - yRel);
            }

            public double getXCord(double x) {
                double xRel = x / timeScaleMaxInMs;
                return ((double)getWidth()) * xRel;
            }
        }

        class OscilloscopeFunction extends PartWiseLinearFunction {

            public double curTime = -100;

            /** UID */
            private static final long serialVersionUID = -4657542107242253198L;

            public void addNewValue(double time, double d, double lastTime) {

                if (time > lastTime) {
                    // remove entries between lastTime and time
                    int idx = getInsertIndex(lastTime);
                    while (idx < nodes.size() && nodes.get(idx).x <= time) {
                        //System.out.println("removing " + time + " " + lastTime);
                        nodes.remove(idx);
                    }

                    // add new entry
                    //System.out.println("adding " + nodes.size());
                    addEntry(time, d);
                } else {
                    // remove entries between lastTime and time
                    int idx = getInsertIndex(lastTime);
                    while (nodes.size() > idx) {
                        nodes.remove(nodes.size() - 1);
                    }
                    while (nodes.size() > 0 && nodes.get(0).x <= time) {
                        nodes.remove(0);
                    }

                    // add new entries
                    addEntry(time + timeScaleMaxInMs, d);
                    addEntry(time, d);
                    addEntry(lastTime - timeScaleMaxInMs, nodes.get(nodes.size() - 1).y);
                }

                curTime = time;
            }

            List<PartWiseLinearFunction.Node> getNodeList() {
                return nodes;
            }
        }
    }

    /** Data type of this class */
    @Const public final static DataTypeBase TYPE = new DataType<OscilloscopeUI.OscilloscopeFunction>(OscilloscopeUI.OscilloscopeFunction.class);
}
