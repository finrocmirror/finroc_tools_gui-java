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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.themes.Theme;
import org.finroc.tools.gui.themes.Themes;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;


/**
 * @author Max Reichardt
 *
 */
public class Slider extends Widget {

    /** UID */
    private static final long serialVersionUID = 2468949292381381445L;

    /** Slider-Value output port */
    public WidgetOutput.Numeric value;

    /** Slider parameters */
    public double minimum = 0, maximum = 10, stepSize = 0.1;

    /** Slider background */
    public Color sliderBackground = getDefaultColor(Theme.DefaultColor.SLIDER_BACKGROUND);

    /** Should minimum be on the left (or at the bottom)? */
    public boolean reverse = false;

    /** Show ticks? Show labels? */
    public boolean showTicks = true, showLabels = true;

    @Override
    protected WidgetUI createWidgetUI() {
        return new SliderUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
    }

    class SliderUI extends WidgetUI implements ChangeListener, ComponentListener, PortListener<CoreNumber>, Runnable {

        /** UID */
        private static final long serialVersionUID = -226842649519588097L;

        private DoubleSlider slider;

        SliderUI() {
            super(RenderMode.Swing);
            slider = new DoubleSlider();
            setLayout(new BorderLayout());
            add(slider, BorderLayout.CENTER);
            widgetPropertiesChanged();

            value.addChangeListener(this);
            addComponentListener(this);

            portChanged(null, null);

            slider.addChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            slider.setToolTipText(String.format("%.2f", slider.getDoubleValue()));
            value.publish(slider.getDoubleValue());
        }

        public void componentHidden(ComponentEvent e) {}
        public void componentShown(ComponentEvent e) {}
        public void componentMoved(ComponentEvent e) {}

        public void componentResized(ComponentEvent e) {
            slider.setOrientation(getWidth() > getHeight() ? JSlider.HORIZONTAL : JSlider.VERTICAL);
            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {
            // Swap value if someone enters invalid minimum and maximum
            if (minimum > maximum) {
                reverse = !reverse;
                double temp = minimum;
                minimum = maximum;
                maximum = temp;
                stepSize = Math.abs(stepSize);
            }
            slider.setBackground(sliderBackground);
            slider.setPaintTicks(showTicks);
            slider.setPaintLabels(showLabels);
            slider.setInverted(reverse);
            slider.setParams(minimum, maximum, stepSize);
            slider.setLabelColor(getLabelColor(Slider.this));
            //slider.setForeground(getLabelColor(Slider.this));  // does not seem to work for ticks
        }

        @Override
        public void portChanged(AbstractPort origin, CoreNumber value) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
            slider.setValue(Slider.this.value.getDouble());
        }
    }
}

/** Slider for double values */
class DoubleSlider extends JSlider {

    /** UID */
    private static final long serialVersionUID = -2482287632595308051L;

    double min, max, step;
    private static final double MAXTICKSPERPIXEL = 0.1;
    Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
    JLabel maxLabel, minLabel, valueLabel, invisibleLabel;
    Graphics graphics; // Graphics object while painting - otherwise null

    public void setValue(double value) {
        if (value <= min) {
            super.setValue(0);
        } else if (value >= max) {
            super.setValue(getMaximum());
        } else {
            super.setValue(Math.round((int)((value - min) / step)));
        }
    }

    public void setParams(double min, double max, double step) {
        this.min = min;
        this.max = max;
        this.step = step;
        setMinimum(0);
        int numberOfSteps = (int)Math.round(((max - min) / step));
        setMaximum(numberOfSteps);
        int minorTicks = 1;
        int dimSize = (getOrientation() == JSlider.HORIZONTAL) ? getWidth() : getHeight();
        while (((double)numberOfSteps) / ((double)minorTicks) > dimSize * MAXTICKSPERPIXEL) {
            minorTicks *= 10;
        }
        setMinorTickSpacing(minorTicks);
        setMajorTickSpacing(minorTicks * 5);
        if (labelTable.size() == 0) {
            maxLabel = new JLabel();
            minLabel = new JLabel();
            valueLabel = new JLabel();
            invisibleLabel = new JLabel();
        }
        minLabel.setText("" + min);
        maxLabel.setText("" + max);
        valueLabel.setText("                                              ");
        FontMetrics metrics = minLabel.getFontMetrics(minLabel.getFont());
        invisibleLabel.setPreferredSize(new Dimension(Math.max(metrics.stringWidth(createValueLabelString(min)), metrics.stringWidth(createValueLabelString(max))) + 5, 0));
        labelTable.clear();
        labelTable.put(0, minLabel);
        labelTable.put(numberOfSteps / 2, valueLabel);
        labelTable.put(numberOfSteps, maxLabel);
        labelTable.put(numberOfSteps + 1, invisibleLabel);
        setLabelTable(labelTable);

        // update value label
        getDoubleValue();
    }

    public double getDoubleValue() {
        updateValueLabel();
        return min + getValue() * step;
    }

    public void updateValueLabel() {
        valueLabel.setText(createValueLabelString(min + getValue() * step));
    }

    public static String createValueLabelString(double value) {
        return "(" + String.format("%.2f", value) + ")";
    }

    // Call after first updateParams
    public void setLabelColor(Color c) {
        minLabel.setForeground(c);
        maxLabel.setForeground(c);
        valueLabel.setForeground(c);
    }

    // evil hack to be able to set the tick color (part I)
    @Override
    protected void paintComponent(Graphics g) {
        graphics = g.create();
        try {
            ui.update(graphics, this);
        } finally {
            graphics.dispose();
        }
        graphics = null;
    }


    // evil hack to be able to set the tick color (part II)
    @Override
    public int getMajorTickSpacing() {
        if (graphics != null && Themes.nimbusLookAndFeel()) {
            boolean bright = minLabel.getForeground().getRed() + minLabel.getForeground().getGreen() + minLabel.getForeground().getBlue() >= 384;
            graphics.setColor(bright ? minLabel.getForeground().darker().darker() : minLabel.getForeground());
            graphics.setColor(bright ? new Color(0.07f, 0.07f, 0.07f) : minLabel.getForeground());
        }
        return super.getMajorTickSpacing();
    }
}
