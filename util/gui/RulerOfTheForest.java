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
package org.finroc.tools.gui.util.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class RulerOfTheForest extends JPanel {

    /** UID */
    private static final long serialVersionUID = -4128981461613296756L;

    private static final int RULERHEIGHT = 30;
    private double MAXTICKSPERPIXEL = 0.2;
    private double min, max;
    private int orientation;
    private Line2D.Double tempLine = new Line2D.Double();
    private static Font font = new JLabel("").getFont().deriveFont(Font.PLAIN, 8);
    private boolean mirrored;
    private List<Integer> minorTicksList = new ArrayList<Integer>();
    private List<Integer> majorTicksList = new ArrayList<Integer>();
    private static final int TOP = 1, BOTTOM = 2, LEFT = 4, RIGHT = 8;
    private int borders;

    public RulerOfTheForest(int orientation, double maxTicksPerPixel, boolean mirrored, int borders) {
        this(orientation, borders);
        MAXTICKSPERPIXEL = maxTicksPerPixel;
        this.mirrored = mirrored;
    }

    public RulerOfTheForest(int orientation, int borders) {
        //setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.orientation = orientation;
        this.borders = borders;
        if (orientation == SwingConstants.HORIZONTAL) {
            setPreferredSize(new Dimension(0, RULERHEIGHT));
        } else {
            setPreferredSize(new Dimension(RULERHEIGHT, 0));
        }
    }

    public void setMinAndMax(double min, double max) {
        this.min = min;
        this.max = max;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setFont(font);
        g2d.setColor(new Color(0, 0, 0));

        // reset lists
        minorTicksList.clear();
        majorTicksList.clear();

        // Draw Border
        if ((borders & BOTTOM) > 0) {
            g2d.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1); // bottom
        }
        if ((borders & TOP) > 0) {
            g2d.drawLine(0, 0, getWidth() - 1, 0);  // top
        }
        if ((borders & LEFT) > 0) {
            g2d.drawLine(0, 0, 0, getHeight() - 1); // left
        }
        if ((borders & RIGHT) > 0) {
            g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight() - 1); // right
        }

        // transform Graphics for vertical ruler
        if (orientation == SwingConstants.VERTICAL) {
            g2d.translate(0, getHeight() - 1);
            g2d.rotate(-Math.PI / 2);
        }

        // Calculate tick size
        double pixelLength = getLength() - 1;
        double realLength = max - min;
        if (realLength == 0 || pixelLength == 0) {
            return;
        }
        double minorTickSize = Math.signum(realLength);
        while (realLength / minorTickSize > pixelLength * MAXTICKSPERPIXEL) {
            minorTickSize *= 10;
        }
        double majorTickSize = minorTickSize * 10;

        // Draw minor ticks
        double start = (Math.ceil(min / minorTickSize) * minorTickSize); // in mm
        for (double d = start; minorTickSize < 0 ^ d < max; d += minorTickSize) {
            drawTick(g2d, d, ((d - min) / realLength) * pixelLength, 3, false);
        }

        // Draw major ticks
        start = (Math.ceil(min / majorTickSize) * majorTickSize); // in mm
        for (double d = start; minorTickSize < 0 ^ d < max; d += majorTickSize) {
            drawTick(g2d, d, ((d - min) / realLength) * pixelLength, 7, true);
        }

        g2d.dispose();
    }

    private void drawTick(Graphics2D g, double realPos, double pixelPos, int height, boolean drawLabel) {
        if (!mirrored) {
            tempLine.setLine(pixelPos, RULERHEIGHT - 1, pixelPos, RULERHEIGHT - 1 - height);
        } else {
            tempLine.setLine(pixelPos, 0, pixelPos, height);
        }
        if (drawLabel) {
            String s = "" + ((int)realPos);
            Rectangle2D r = font.getStringBounds(s, g.getFontRenderContext());
            if (!mirrored) {
                g.drawString("" + ((int)realPos), (int)(pixelPos - r.getWidth() / 2), (int)(-r.getMinY() + 7));
            } else {
                g.drawString("" + ((int)realPos), (int)(pixelPos - r.getWidth() / 2), (int)(-r.getMinY() + 12));
            }
        }
        g.draw(tempLine);

        // fill lists
        if (drawLabel) {
            majorTicksList.add((int)Math.round(pixelPos));
        } else {
            minorTicksList.add((int)Math.round(pixelPos));
        }
    }

    private double getLength() {
        return orientation == SwingConstants.HORIZONTAL ? getWidth() : getHeight();
    }

    public List<Integer> getMajorTicks() {
        return majorTicksList;
    }

    public List<Integer> getMinorTicks() {
        return minorTicksList;
    }


    public static class RulerLabel extends JPanel {

        /** UID */
        private static final long serialVersionUID = -8770560627435640115L;

        public RulerLabel() {
            setPreferredSize(new Dimension(RULERHEIGHT, RULERHEIGHT));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            //setMinimumSize(new Dimension(HEIGHT, HEIGHT));
            //setMaximumSize(new Dimension(HEIGHT, HEIGHT));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
        }
    }
}
