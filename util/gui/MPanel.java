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
package org.finroc.tools.gui.util.gui;

import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author Max Reichardt
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
