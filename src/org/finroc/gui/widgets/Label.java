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

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.themes.Themes;
import org.finroc.serialization.RRLibSerializable;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;


public class Label extends Widget {

    /** UID */
    private static final long serialVersionUID = -1817091621155537442L;

    WidgetInput.Std<RRLibSerializable> text;

    Color textColor = Themes.getCurTheme().standardLabel();

    float fontSize = 36;

    @Override
    protected void setDefaultColors() {
        setBackground(Themes.getCurTheme().standardBackground());
        setLabelColor(Themes.getCurTheme().standardLabel());
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new LabelWidgetUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(RRLibSerializable.TYPE);
    }

    @SuppressWarnings("rawtypes")
    class LabelWidgetUI extends WidgetUI implements PortListener {

        /** UID */
        private static final long serialVersionUID = 1643584696919287207L;

        JLabel label;

        @SuppressWarnings("unchecked")
        LabelWidgetUI() {
            super(RenderMode.Swing);
            setLayout(new BorderLayout());
            label = new JLabel("test");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
            text.addChangeListener(this);
            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {
            label.setForeground(textColor);
            label.setFont(label.getFont().deriveFont(fontSize));
        }

        @Override
        public void portChanged(AbstractPort origin, Object value) {
            label.setText(value == null ? "null" : value.toString());
        }
    }
}

