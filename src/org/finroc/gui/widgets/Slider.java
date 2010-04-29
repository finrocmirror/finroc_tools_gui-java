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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetOutput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.themes.Themes;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortListener;


/**
 * @author max
 *
 */
public class Slider extends Widget {

    /** UID */
    private static final long serialVersionUID = 2468949292381381445L;

    /** Slider-Value output port */
    private WidgetOutput.Numeric value;

    /** Slider parameters */
    private double minimum = 0, maximum = 10, stepSize = 0.1;

    /** Slider background */
    private Color sliderBackground = Themes.getCurTheme().sliderBackground();

    /** Show ticks? Show labels? */
    private boolean showTicks = true, showLabels = true;

    @Override
    protected WidgetUI createWidgetUI() {
        return new SliderUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | PortFlags.ACCEPTS_REVERSE_DATA_PUSH);
    }

    class SliderUI extends WidgetUI implements ChangeListener, ComponentListener, CCPortListener<CoreNumber> {

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
            slider.setBackground(sliderBackground);
            slider.setPaintTicks(showTicks);
            slider.setPaintLabels(showLabels);
            slider.setParams(minimum, maximum, stepSize);
            slider.setLabelColor(Slider.this.getLabelColor());
        }

        @Override
        public void portChanged(CCPortBase origin, CoreNumber value) {
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
    JLabel maxLabel, minLabel;

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
        }
        minLabel.setText("" + min);
        maxLabel.setText("" + max);
        labelTable.clear();
        labelTable.put(0, minLabel);
        labelTable.put(numberOfSteps, maxLabel);
        setLabelTable(labelTable);
    }

    public double getDoubleValue() {
        return min + getValue() * step;
    }

    // Call after first updateParams
    public void setLabelColor(Color c) {
        minLabel.setForeground(c);
        maxLabel.setForeground(c);
    }
}
