/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2012 Max Reichardt
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
package org.finroc.tools.gui.util.gui;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author max
 *
 * JPanel with some functions for convenience
 */
public class MPanel extends JPanel {

    /** UID */
    private static final long serialVersionUID = 8146007407902490501L;

    /** Background image */
    private ImageIcon background;

    /**
     * @param background New background
     */
    public void setBackground(ImageIcon background) {
        this.background = background;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            for (int x = 0; x < getWidth(); x += background.getIconWidth()) {
                for (int y = 0; y < getHeight(); y += background.getIconHeight()) {
                    g.drawImage(background.getImage(), x, y, null);
                }
            }
        }
    }
}
