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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.finroc.tools.gui.themes.Themes;

public class RulerOfTheForest extends JPanel {

    /** UID */
    private static final long serialVersionUID = -4128981461613296756L;

    public static final int RULERHEIGHT = 30;
    private int rulerHeight = RULERHEIGHT;
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
    private DecimalFormat labelFormat = new DecimalFormat();

    public RulerOfTheForest(int orientation, double maxTicksPerPixel, boolean mirrored, int borders) {
        this(orientation, borders);
        setOpaque(Themes.getCurTheme().useOpaquePanels());
        MAXTICKSPERPIXEL = maxTicksPerPixel;
        this.mirrored = mirrored;
    }

    public RulerOfTheForest(int orientation, int borders) {
        //setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.orientation = orientation;
        this.borders = borders;
        setPreferredSize(RULERHEIGHT);
    }

    public void setPreferredSize(int rulerHeight) {
        if (orientation == SwingConstants.HORIZONTAL) {
            setPreferredSize(new Dimension(0, rulerHeight));
        } else {
            setPreferredSize(new Dimension(rulerHeight, 0));
        }
        this.rulerHeight = rulerHeight;
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
        if (Themes.nimbusLookAndFeel()) {
            g2d.setColor(getForeground().darker());
        } else {
            g2d.setColor(new Color(0, 0, 0));
        }

        // reset lists
        minorTicksList.clear();
        majorTicksList.clear();

        // Draw Border
        if (!Themes.nimbusLookAndFeel()) {
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
        int exponent = 0;
        while (realLength / minorTickSize > pixelLength * MAXTICKSPERPIXEL) {
            minorTickSize *= 10;
            exponent++;
        }
        while (realLength / (minorTickSize / 10) < pixelLength * MAXTICKSPERPIXEL) {
            minorTickSize /= 10;
            exponent--;
        }

        // Count minor ticks
        double ticksPerLabel = 1;
        if (realLength / minorTickSize >= 30) {
            ticksPerLabel = 10;
        } else if (realLength / minorTickSize >= 20) {
            ticksPerLabel = 5;
        } else if (realLength / minorTickSize >= 8) {
            ticksPerLabel = 2;
        }

        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumFractionDigits(exponent >= 0 ? 0 : ((-exponent) - (ticksPerLabel < 10 ? 0 : 1)));
        labelFormat.setMaximumFractionDigits(labelFormat.getMaximumFractionDigits());

        // Draw minor ticks
        double start = (Math.ceil(min / minorTickSize) * minorTickSize);
        for (double d = start; minorTickSize < 0 ^ d < max; d += minorTickSize) {
            long tickIndex = Math.round(d / minorTickSize);
            boolean majorTick = (tickIndex % 10) == 0;
            boolean label = (tickIndex % ticksPerLabel) == 0;
            drawTick(g2d, d, ((d - min) / realLength) * pixelLength, majorTick ? 7 : 3, label);
        }

        g2d.dispose();
    }

    private void drawTick(Graphics2D g, double realPos, double pixelPos, int height, boolean drawLabel) {
        if (!mirrored) {
            tempLine.setLine(pixelPos, rulerHeight - 1, pixelPos, rulerHeight - 1 - height);
        } else {
            tempLine.setLine(pixelPos, 0, pixelPos, height);
        }
        if (drawLabel) {
            String s = labelFormat.format(realPos);
            if (s.equals("-0")) {
                s = "0";
            }
            Rectangle2D r = font.getStringBounds(s, g.getFontRenderContext());
            if (!mirrored) {
                g.drawString(s, (int)(pixelPos - r.getWidth() / 2), (int)(-r.getMinY() + 7));
            } else {
                g.drawString(s, (int)(pixelPos - r.getWidth() / 2), (int)(-r.getMinY() + 12));
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

        public enum Position { NW, SW, NE, SE, NO_ETCHED_BORDER };
        Position position;

        public RulerLabel(Position p) {
            setOpaque(Themes.getCurTheme().useOpaquePanels());
            setPreferredSize(new Dimension(RULERHEIGHT, RULERHEIGHT));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.position = p;
            //setMinimumSize(new Dimension(HEIGHT, HEIGHT));
            //setMaximumSize(new Dimension(HEIGHT, HEIGHT));
        }

        public void setForeground(Color c) {
            super.setForeground(c);
            if (Themes.nimbusLookAndFeel()) {
                //setBorder(null);
                //setBorder(BorderFactory.createLineBorder(c));
                setBorder(Themes.getCurTheme().createThinBorder());
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
        }

        protected void paintBorder(Graphics g) {
            //super.paintBorder(g);
            Border border = getBorder();
            if (border instanceof EtchedBorder) {
                if (position == Position.NW) {
                    // TODO
                    super.paintBorder(g);
                } else if (position == Position.SW) {
                    g.setClip(-1, 0, getWidth() + 1, getHeight());
                    border.paintBorder(this, g, -2, 0, getWidth() + 2, getHeight() + 5);
                } else if (position == Position.NE) {
                    // TODO
                    super.paintBorder(g);
                } else if (position == Position.SE) {
                    border.paintBorder(this, g, 0, 0, getWidth() + 5, getHeight() + 5);
                }
            } else {
                super.paintBorder(g);
            }
        }
    }
}
