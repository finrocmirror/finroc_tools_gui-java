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
import org.finroc.gui.WidgetPorts;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.themes.Themes;

import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortData;
import org.finroc.core.port.cc.CCPortListener;
import org.finroc.core.port.std.PortBase;
import org.finroc.core.port.std.PortData;
import org.finroc.core.port.std.PortListener;


public class Label extends Widget {

    /** UID */
    private static final long serialVersionUID = -1817091621155537442L;

    WidgetInput.Std<PortData> text;
    WidgetInput.CC<CCPortData> cctext;

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
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort, WidgetPorts<?> collection) {
        return suggestion.derive(forPort == text ? PortData.TYPE : CCPortData.TYPE);
    }

    class LabelWidgetUI extends WidgetUI implements PortListener<PortData>, CCPortListener<CCPortData> {

        /** UID */
        private static final long serialVersionUID = 1643584696919287207L;

        JLabel label;

        LabelWidgetUI() {
            super(RenderMode.Swing);
            setLayout(new BorderLayout());
            label = new JLabel("test");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            add(label, BorderLayout.CENTER);
            text.addChangeListener(this);
            cctext.addChangeListener(this);
            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {
            label.setForeground(textColor);
            label.setFont(label.getFont().deriveFont(fontSize));
        }

        @Override
        public void portChanged(PortBase origin, PortData value) {
            label.setText(value == null ? "null" : value.toString());
        }

        @Override
        public void portChanged(CCPortBase origin, CCPortData value) {
            label.setText(value == null ? "null" : value.toString());
        }
    }
}

