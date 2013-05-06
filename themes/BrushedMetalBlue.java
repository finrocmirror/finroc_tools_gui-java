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
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.util.gui.IconManager;
import org.finroc.tools.gui.util.gui.MPanel;
import org.finroc.tools.gui.util.gui.MToolBar;
import org.finroc.tools.gui.widgets.GeometryRenderer;

/**
 * @author Max Reichardt
 *
 * Polished theme in brushed metal and blue LEDs
 */
public class BrushedMetalBlue extends Theme {

    /** Default background color */
    public final Color BACKGROUND = new Color(0.2f, 0.2f, 0.2f);
    public final Color ALT_BACKGROUND = new Color(204, 204, 204);
    //public final Color ALT_BACKGROUND = new Color(new JCheckBox().getBackground().getRGB());
    public final Color LABEL = new Color(0.8f, 0.8f, 0.8f);
    public final ImageIcon TITANIUM = (ImageIcon)IconManager.getInstance().getIcon("brushed-titanium-max.png");
    public final ImageIcon ALU = (ImageIcon)IconManager.getInstance().getIcon("brushed-alu-max.png");

    @Override
    public void initGUIPanel(MPanel panel) {
        panel.setBackground(TITANIUM);
    }

    @Override
    public Color getDefaultColor(DefaultColor dc) {
        switch (dc) {
        case BACKGROUND:
        case ALTERNATIVE_LABEL:
            return BACKGROUND;
        case ALTERNATIVE_BACKGROUND:
            return ALT_BACKGROUND;
        case LABEL:
            return LABEL;
        case LCD_BACKGROUND:
        case OSCILLOSCOPE_BACKGROUND:
            return new Color(0, 0, 0.15f);
        case LCD_ENABLED:
            return new Color(0.15f, 0.15f, 1f);
        case LCD_DISABLED:
            return new Color(0, 0, 0.2f);
        case LED:
            return new Color(0.1f, 0.1f, 1f);
        case SLIDER_BACKGROUND:
            return new Color(0, 0, 0);
        case JOYSTICK_BACKGROUND:
            return new Color(0.5f, 0.5f, 0.5f);
        case JOYSTICK_FOREGROUND:
            return Color.WHITE;
        case GEOMETRY_BACKGROUND:
            return Color.gray;
        case OSCILLOSCOPE_FOREGROUND:
            return new Color(60, 102, 255);
        case OSCILLOSCOPE_SCALE:
            return new Color(0, 0, 0.25f);
        case OSCILLOSCOPE_SCALE_MAJOR:
            return new Color(0, 0, 0.33f);
        default:
            return null;
        }
    }

    public boolean useAlternativeColorSet(Widget w) {
        Color c = w.getBackground();
        return c.getRed() + c.getGreen() + c.getBlue() >= 384;
    }

    @Override
    public void processWidget(Widget w, WidgetUI wui) {
        boolean alt = useAlternativeColorSet(w);
        boolean oglwidget = w.getClass().getSimpleName().equals("OpenGLWidget") || w.getClass().getSimpleName().equals("Oscilloscope"); // TODO: this is not nice
        wui.setBackground(alt ? ALT_BACKGROUND : BACKGROUND);
        String label = w.getLabel();
        if (label != null && !label.equals("") && (!oglwidget)) {
            TitledBorder tb = new TitledBorder(label);
            tb.setTitleColor(alt ? BACKGROUND : LABEL);
            if (!alt) {
                //tb.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                //wui.setTitleBorder(tb);
                tb.setBorder(BorderFactory.createEmptyBorder());
                wui.setTitleBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), tb));
                wui.setBackground((ImageIcon)null);
            } else {
                tb.setBorder(BorderFactory.createEmptyBorder());
                wui.setBackground(ALU);
                wui.setTitleBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED), tb));
            }
            tb.setTitleFont(wui.getFont().deriveFont(Font.PLAIN));
        } else {
            if (!alt) {
                wui.setBackground((ImageIcon)null);
                wui.setTitleBorder(null);
                if (oglwidget || (w instanceof GeometryRenderer)) {
                    wui.setTitleBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                }
            } else {
                wui.setBackground(ALU);
                wui.setTitleBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
            }
        }
        wui.setOpaque(false);
    }

    @Override
    public Border createThinBorder() {
        return BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
    }

    @Override
    public Color getLabelColor(Widget w, WidgetUI slider, Color labelColor) {
        return useAlternativeColorSet(w) ? BACKGROUND : LABEL;
    }

    @Override
    public void initToolbar(MToolBar tb) {
        tb.setOpaque(false);
        /*tb.setBackground(ALT_BACKGROUND);
        tb.setBackground(ALU);*/
    }

    @Override
    public boolean useOpaquePanels() {
        return false;
    }
}
