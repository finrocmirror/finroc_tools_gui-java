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
package org.finroc.gui.themes;

import java.awt.Color;

/**
 * @author max
 *
 */
public abstract class Theme {

    /** Background of panel */
    public abstract Color panelBackground();

    /** Background color of widget (at least of the border) */
    public Color widgetBackground() {
        return standardBackground();
    };

    /** Color of Label */
    public Color widgetLabel() {
        return standardLabel();
    };

    public abstract Color standardLabel();
    public abstract Color standardBackground();

    public Color geometryBackground() {
        return standardBackground();
    };

    public Color lcdBackground() {
        return standardBackground();
    };
    public Color lcdEnabled() {
        return standardLabel();
    };
    public Color lcdDisabled() {
        return standardBackground().brighter();
    };

    public Color ledColor() {
        return new Color(0.1f, 1f, 0.1f);
    };

    /** Slider background */
    public Color sliderBackground() {
        return standardBackground();
    };

    public Color joystickBackground() {
        return standardBackground();
    };
    public Color joystickForeground() {
        return standardLabel();
    };

    public Color borderColor() {
        return standardLabel().darker();
    };
}
