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
package org.finroc.tools.gui.themes;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.util.gui.MPanel;
import org.finroc.tools.gui.util.gui.MToolBar;

/**
 * @author Max Reichardt
 *
 * Classic/original jmcagui look and feel.
 * Somewhat inconsistent.
 */
public class Default extends Theme {

    public final Color BACKGROUND = new Color(0.2f, 0.2f, 0.2f);
    public final Color ALT_BACKGROUND = new Color(new JCheckBox().getBackground().getRGB());
    public final Color BORDER = new Color(184, 207, 229);

    @Override
    public void initGUIPanel(MPanel panel) {
        panel.setBackground(BACKGROUND);
    }

    @Override
    public void processWidget(Widget w, WidgetUI wui) {
        wui.setBackground(w.getBackground());
        String label = w.getLabel();
        if (label != null && !label.equals("")) {
            TitledBorder tb = new TitledBorder(label);
            tb.setTitleColor(w.getLabelColor());
            tb.setBorder(BorderFactory.createLineBorder(BORDER));
            wui.setTitleBorder(tb);
        } else {
            wui.setTitleBorder(null);
        }
        wui.setOpaque(true);
    }

    @Override
    public Color getDefaultColor(DefaultColor dc) {
        switch (dc) {
        case BACKGROUND:
            return BACKGROUND;
        case LABEL:
            return Color.white;
        case ALTERNATIVE_BACKGROUND:
            return ALT_BACKGROUND;
        case ALTERNATIVE_LABEL:
            return Color.black;
        case LCD_BACKGROUND:
            return new Color(0, 0, 0.15f);
        case LCD_ENABLED:
            return new Color(0.15f, 0.15f, 1f);
        case LCD_DISABLED:
            return new Color(0, 0, 0.2f);
        case LED:
            return new Color(0.1f, 1f, 0.1f);
        case SLIDER_BACKGROUND:
            return new Color(0, 0, 0.2f);
        case JOYSTICK_BACKGROUND:
            return new Color(0.5f, 0.5f, 0.5f);
        case JOYSTICK_FOREGROUND:
            return Color.WHITE;
        case GEOMETRY_BACKGROUND:
            return Color.gray;
        case OSCILLOSCOPE_BACKGROUND:
            return new Color(0, 0.2f, 0);
        case OSCILLOSCOPE_FOREGROUND:
            return new Color(0, 1.0f, 0);
        case OSCILLOSCOPE_SCALE:
            return new Color(0, 0.25f, 0);
        case OSCILLOSCOPE_SCALE_MAJOR:
            return new Color(0, 0.33f, 0);
        default:
            return null;
        }
    }

    @Override
    public Border createThinBorder() {
        return BorderFactory.createLineBorder(BACKGROUND);
    }

    @Override
    public void initToolbar(MToolBar tb) {
    }

    @Override
    public boolean useOpaquePanels() {
        return true;
    }
}
