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

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.themes.Theme;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;
import org.rrlib.serialization.BinarySerializable;


public class Label extends Widget {

    /** UID */
    private static final long serialVersionUID = -1817091621155537442L;

    public WidgetInput.Std<BinarySerializable> text;

    public Color textColor = getDefaultColor(Theme.DefaultColor.ALTERNATIVE_LABEL);

    public float fontSize = 36;

    @Override
    protected void setDefaultColors() {
        useAlternativeColors();
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new LabelWidgetUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(BinarySerializable.TYPE);
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

