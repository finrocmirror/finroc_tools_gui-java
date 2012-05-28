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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.finroc.plugins.data_types.Angle;
import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.themes.Theme;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.PortListener;


/**
 * @author max
 *
 */
public class Knob extends Widget {

    /** UID */
    private static final long serialVersionUID = 2468949292381381445L;

    /** Slider-Value output port */
    private WidgetOutput.Std<Angle> value;

    /** Slider parameters */
    private double minimum = 0, maximum = 360, stepSize = 0.1;

    /** Slider background */
    private Color sliderBackground = getDefaultColor(Theme.DefaultColor.SLIDER_BACKGROUND);

    /** Show ticks? Show labels? */
    private boolean showTicks = true, showLabels = true;

    @Override
    protected WidgetUI createWidgetUI() {
        return new SliderUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | PortFlags.ACCEPTS_REVERSE_DATA_PUSH).derive(Angle.TYPE);
    }

    class SliderUI extends WidgetUI implements ChangeListener, ComponentListener, PortListener<Angle> {

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
            Angle a = value.getUnusedBuffer();
            a.setDeg(slider.getDoubleValue());
            value.publish(a);
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
            slider.setLabelColor(Knob.this.getLabelColor());
        }

        @Override
        public void portChanged(AbstractPort origin, Angle value) {
            if (value != null) {
                slider.setValue(value.getSignedDeg());
            }
        }
    }
}

