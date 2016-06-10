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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.themes.Theme;

import org.finroc.core.port.PortCreationInfo;

/**
 * @author Max Reichardt
 *
 * Static label to structure and label other widgets
 * (can also be used for borders etc.)
 */
public class StaticLabel extends Widget {

    /** UID */
    private static final long serialVersionUID = -1817091621155537442L;

    public enum HorizontelAlignment { LEFT, CENTER, RIGHT };
    public enum VerticalAlignment { TOP, CENTER, BOTTOM };

    public String text = "Text";

    public Color textColor = getDefaultColor(Theme.DefaultColor.ALTERNATIVE_LABEL);

    public float fontSize = 14;

    public HorizontelAlignment horizontalAlignment = HorizontelAlignment.LEFT;
    public VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    @Override
    protected void setDefaultColors() {
        useAlternativeColors();
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new StaticLabelUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion;
    }

    class StaticLabelUI extends WidgetUI {

        /** UID */
        private static final long serialVersionUID = 1643584696919287207L;

        JLabel label;

        StaticLabelUI() {
            super(RenderMode.Swing);
            setLayout(new BorderLayout());
            label = new JLabel(text);
            add(label, BorderLayout.CENTER);
            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {
            label.setForeground(textColor);
            label.setFont(label.getFont().deriveFont(fontSize));
            boolean alignLeft = horizontalAlignment == HorizontelAlignment.LEFT;
            boolean alignRight = horizontalAlignment == HorizontelAlignment.RIGHT;
            boolean alignTop = verticalAlignment == VerticalAlignment.TOP;
            boolean alignBottom = verticalAlignment == VerticalAlignment.BOTTOM;
            label.setBorder(BorderFactory.createEmptyBorder(alignTop ? 3 : 0, alignLeft ? 3 : 0, alignBottom ? 3 : 0, alignRight ? 3 : 0));
            label.setHorizontalAlignment(alignLeft ? SwingConstants.LEFT : (alignRight ? SwingConstants.RIGHT : SwingConstants.CENTER));
            label.setVerticalAlignment(alignTop ? SwingConstants.TOP : (alignBottom ? SwingConstants.BOTTOM : SwingConstants.CENTER));
            label.setText(text);
        }
    }
}

