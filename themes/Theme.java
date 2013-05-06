//
// You received this file as part of Finroc
// A Framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui.themes;

import java.awt.Color;

import javax.swing.border.Border;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.util.gui.MPanel;
import org.finroc.tools.gui.util.gui.MToolBar;

/**
 * @author Max Reichardt
 *
 * Base class for "themes" - the possibility to customize look & feel of fingui widgets a little
 */
public abstract class Theme {

    /**
     * @param panel GUI Panel to initialize
     */
    public abstract void initGUIPanel(MPanel panel);

    /**
     * Adjust widget after property change (especially borders)
     *
     * @param widgetUI Widget panel to update
     */
    public abstract void processWidget(Widget w, WidgetUI wui);

    public enum DefaultColor {
        BACKGROUND,
        LABEL,
        ALTERNATIVE_BACKGROUND,
        ALTERNATIVE_LABEL,
        LCD_BACKGROUND,
        LCD_ENABLED,
        LCD_DISABLED,
        LED,
        SLIDER_BACKGROUND,
        JOYSTICK_BACKGROUND,
        JOYSTICK_FOREGROUND,
        GEOMETRY_BACKGROUND,
        OSCILLOSCOPE_BACKGROUND,
        OSCILLOSCOPE_FOREGROUND,
        OSCILLOSCOPE_SCALE,
        OSCILLOSCOPE_SCALE_MAJOR
    }

    /**
     * @param dc Type of element
     * @return Default color for specified element
     */
    public abstract Color getDefaultColor(DefaultColor dc);

    /**
     * Create thin border inside widget
     *
     * @return Border
     */
    public abstract Border createThinBorder();

    /**
     * @return Label color to use
     */
    public Color getLabelColor(Widget w, WidgetUI slider, Color labelColor) {
        return labelColor;
    }

    /**
     * @param tb Toolbar to initialize
     */
    public abstract void initToolbar(MToolBar tb);

    /**
     * @return Use opaque panels?
     */
    public abstract boolean useOpaquePanels();
}
