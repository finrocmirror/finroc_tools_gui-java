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
import org.rrlib.finroc_core_utils.jc.thread.LoopThread;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

import org.finroc.plugins.data_types.PartWiseLinearFunction;
import org.rrlib.serialization.NumericRepresentation;
import org.rrlib.serialization.rtti.DataType;
import org.rrlib.serialization.rtti.DataTypeBase;
import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.std.PortBase;
import org.finroc.core.port.std.PortDataManager;

public class Oscilloscope extends Widget {

    /** UID */
    private static final long serialVersionUID = 1892231028811778314L;

    public WidgetPorts<WidgetInput.Numeric> signals = new WidgetPorts<WidgetInput.Numeric>("signal", 1, WidgetInput.Numeric.class, this);
    public double timeScaleMaxInMs = 10000;
    public double leftScaleMin = 0;
    public double leftScaleMax = 110;
    public double rightScaleMin = 0;
    public double rightScaleMax = 110;
    public PropertyList<OscilloscopeSignal> channels = new PropertyList<OscilloscopeSignal>(OscilloscopeSignal.class, 25);
    public long timerIntervalInMs = 100;

    private static final double EPSILON = 0.0000001;

    public Oscilloscope() {
        channels.add(new OscilloscopeSignal());
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
        if (signals != null && signals.contains(forPort)) {
            PortCreationInfo info = suggestion.derive(suggestion.flags | FrameworkElementFlags.HAS_QUEUE | FrameworkElementFlags.USES_QUEUE);
            info.maxQueueSize = -1;
            return info;
        }
        return suggestion;
    }

    public static class OscilloscopeSignal implements Serializable {

        /** UID */
        private static final long serialVersionUID = 1190544484724927518L;

        public static enum DrawMode { dots, lines }
        public static enum Scale { left, right }

        public DrawMode drawMode = DrawMode.lines;
        public Scale useScale = Scale.left;
        public Color color = getDefaultColor(Theme.DefaultColor.OSCILLOSCOPE_FOREGROUND);
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
            super(RenderMode.Swing, TRAIT_DISPLAYS_LABEL | TRAIT_REQUIRES_BORDER_IN_DARK_COLORING);

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
            Log.log(LogLevel.DEBUG, this, "Oscilloscope Thread started.");

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
            ArrayList<PortDataManager> dequeuedValues = new ArrayList<PortDataManager>();

            public OscilloscopeThread() {
                super(timerIntervalInMs, false);
                startTime = System.currentTimeMillis();
                lastTime = startTime;
            }

            @Override
            public void mainLoopCallback() throws Exception {
                if (!isVisible()) {
                    stopLoop(); // disposed
                    Log.log(LogLevel.DEBUG, this, "Oscilloscope Thread stopped.");
                    return;
                }

                try {
                    synchronized (functions) {
                        long time = System.currentTimeMillis();
                        for (int i = 0; i < functions.size(); i++) {
                            dequeuedValues.clear();
                            PortDataManager dequeued = null;
                            while ((dequeued = ((PortBase)signals.get(i).getPort()).dequeueSingleAutoLockedRaw()) != null) {
                                dequeuedValues.add(dequeued);
                            }
                            if (dequeuedValues.size() > 0) {
                                // TODO: We could use timestamps attached to values if available - however, this would make things a lot more complex
                                double timestep = (time - lastTime) / ((double)dequeuedValues.size());
                                //System.out.println("Received " + dequeuedValues.size() + " values - timestep " + timestep);
                                double lastValueTime = lastTime;
                                for (int j = 0; j < dequeuedValues.size(); j++) {
                                    double valueTime = lastTime + (j + 1) * timestep;
                                    // TODO: an implementation without EPSILON should be possible - and would be cleaner
                                    functions.get(i).addNewValue((valueTime - startTime) % timeScaleMaxInMs, ((NumericRepresentation)dequeuedValues.get(j).getObject().getData()).getNumericRepresentation().doubleValue(), (lastValueTime - startTime + EPSILON) % timeScaleMaxInMs);
                                    lastValueTime = valueTime;
                                }
                            } else {
                                functions.get(i).addNewValue((time - startTime) % timeScaleMaxInMs, signals.get(i).getDouble(), (lastTime - startTime + EPSILON) % timeScaleMaxInMs);
                            }
                            releaseAllLocks();
                        }
                        lastTime = time;
                    }
                    main.repaint();
                } catch (Exception e) {
                    Log.log(LogLevel.DEBUG_WARNING, this, "Oscilloscope Thread skipped loop, because of temporary exception");
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
                        boolean left = channels.get(i).useScale.equals(OscilloscopeSignal.Scale.left);
                        for (PartWiseLinearFunction.Node node : nodes) {
                            int xnow = (int)Math.round(getXCord(node.x));
                            int ynow = (int)Math.round(getYCord(node.y, left));
                            if (channels.get(i).drawMode.equals(OscilloscopeSignal.DrawMode.lines)) {
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
    public final static DataTypeBase TYPE = new DataType<OscilloscopeUI.OscilloscopeFunction>(OscilloscopeUI.OscilloscopeFunction.class);
}
