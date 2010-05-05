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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetPorts;
import org.finroc.gui.WidgetPortsListener;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.commons.LoopThread;
import org.finroc.gui.themes.Themes;
import org.finroc.gui.util.gui.RulerOfTheForest;
import org.finroc.gui.util.propertyeditor.PropertyList;

import org.finroc.plugin.datatype.PartWiseLinearFunction;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.portdatabase.DataType;
import org.finroc.core.portdatabase.DataTypeRegister;

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
        setBackground(Themes.getCurTheme().standardBackground());
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion;
    }

    public static class SignalProperty implements Serializable {

        /** UID */
        private static final long serialVersionUID = 1190544484724927518L;

        private static enum DrawMode { dots, lines }
        private static enum Scale { left, right }

        DrawMode drawMode = DrawMode.lines;
        Scale useScale = Scale.left;
        Color color = new Color(0, 1.0f, 0);
    }

    class OscilloscopeUI extends WidgetUI implements WidgetPortsListener {

        /** UID */
        private static final long serialVersionUID = 3206071864061439004L;

        RulerOfTheForest left, bottom, right;
        OscilloscopeMainPanel main;
        boolean skip;
        List<OscilloscopeFunction> functions = new ArrayList<OscilloscopeFunction>();

        OscilloscopeThread thread;

        public OscilloscopeUI() {
            super(RenderMode.Swing);

            // create GUI elements
            setLayout(new BorderLayout());
            bottom = new RulerOfTheForest(SwingConstants.HORIZONTAL, 0.25, true, 3);
            left = new RulerOfTheForest(SwingConstants.VERTICAL, 0.25, false, 13);
            right = new RulerOfTheForest(SwingConstants.VERTICAL, 0.25, true, 13);
            JPanel south = new JPanel();
            south.setLayout(new BorderLayout());
            south.add(new RulerOfTheForest.RulerLabel(), BorderLayout.WEST);
            south.add(bottom, BorderLayout.CENTER);
            south.add(new RulerOfTheForest.RulerLabel(), BorderLayout.EAST);
            add(left, BorderLayout.WEST);
            add(right, BorderLayout.EAST);
            add(south, BorderLayout.SOUTH);
            main = new OscilloscopeMainPanel();
            add(main, BorderLayout.CENTER);

            // create thread
            thread = new OscilloscopeThread();
            thread.start();
            System.out.println("Oscilloscope Thread started.");

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
            thread.setLoopTime(timerIntervalInMs);
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
                    System.out.println("Oscilloscope Thread stopped.");
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
                    System.out.println("Oscilloscope Thread skipped loop, because of temporary exception");
                }
            }
        }

        class OscilloscopeMainPanel extends JPanel {

            /** UID */
            private static final long serialVersionUID = -2946030738495285342L;

            public OscilloscopeMainPanel() {
                setBackground(new Color(0, 0.2f, 0));
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
                g2d.setColor(new Color(0, 0.25f, 0));
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
                g2d.setColor(new Color(0, 0.33f, 0));
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
                return ((double)(getHeight() - 1)) *(1 - yRel);
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

    static DataType FUNCTION_TYPE = DataTypeRegister.getInstance().getDataType(OscilloscopeUI.OscilloscopeFunction.class);
}
