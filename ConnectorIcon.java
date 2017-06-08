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
package org.finroc.tools.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.finroc.tools.gui.commons.fastdraw.BufferedImageRGB;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

/**
 * @author Max Reichardt
 *
 * Icon(s) displayed in connection panel behind ports to indicate their type.
 */
public class ConnectorIcon extends ImageIcon {

    /** Enumeration for possible connection line starting points */
    public enum LineStart { Default, Outgoing, Incoming }

    /** UID */
    private static final long serialVersionUID = -4938833725657839492L;

    /**
     * Relative position of incoming connection line end, outgoing
     * connection line start and line start in unknown case
     */
    private Point incomingLineStart, outgoingLineStart, defaultLineStart = new Point();

    /** Insets (white pixels) at top an bottom */
    private int insetTop = Integer.MIN_VALUE, insetBottom = Integer.MIN_VALUE;

    /** Registered colors for icons */
    private static final ArrayList<Color> iconColors = new ArrayList<Color>();

    /** Registered colors for icon backgrounds */
    private static final ArrayList<Color> backgroundColors = new ArrayList<Color>();

    /** Maximum index for icon cache (see calculation in Type.indexInCache()) */
    private static final int MAX_INDEX = 1 << 11;

    /** Available cached icons */
    private static final ConnectorIcon iconCache[] = new ConnectorIcon[MAX_INDEX];

    /** Flags specifying connector icon */
    public static final int OUTPUT = 1, PROXY = 2, RPC = 4, RIGHT_TREE = 8, BRIGHTER_COLOR = 16;

    private ConnectorIcon() {
    }

    /**
     * @return Relative position of incoming connection line end
     */
    public Point getLineStart(LineStart lineStart) {
        return lineStart == LineStart.Default ? defaultLineStart : lineStart == LineStart.Outgoing ? outgoingLineStart : incomingLineStart;
    }

    /**
     * @param c Color to register
     * @return Index to use when referencing this color
     */
    private synchronized static int registerIconColor(Color c) {
        if (iconColors.contains(c)) {
            return iconColors.indexOf(c);
        }
        iconColors.add(c);
        return iconColors.size() - 1;
    }

    /**
     * @param c Color to register
     * @return Index to use when referencing this color
     */
    private synchronized static int registerBackgroundColor(Color c) {
        if (backgroundColors.contains(c)) {
            return backgroundColors.indexOf(c);
        }
        backgroundColors.add(c);
        return backgroundColors.size() - 1;
    }

    /**
     * @param iconType Type of connector icon composed from flags above (see above)
     * @param color Color of icon
     * @param background Background color of icon
     * @param height Height of icon
     * @return Icon
     */
    public static ConnectorIcon getIcon(int iconType, IconColor color, BackgroundColor background, int height) {
        int indexInCache = iconType | color.index << 5 | background.index << 9; // 11 bit: [background color 2 bit][color 4 bit][flags 5 bit]
        ConnectorIcon icon = iconCache[indexInCache];
        if (icon == null) {
            // Note with respect to concurrency: Since always the same icons are generated,
            // it does not hurt if concurrent threads generate the same icon in parallel.
            // One of them will be garbage collected.
            icon = createIcon(iconType, color, background, height);
            iconCache[indexInCache] = icon;
        } else if (icon.getIconHeight() != height) {
            Log.log(LogLevel.WARNING, "Icons of different size not properly supported yet"); // Could be added with reasonable effort
            return createIcon(iconType, color, background, height);
        }
        return icon;
    }

    /** Indexed Icon Color */
    public static class IconColor extends Color {

        /** UID */
        private static final long serialVersionUID = 1019761626697067967L;

        /** Index of color */
        public final int index;

        /** Plain color */
        public final Color plainColor;

        /** Brighter color */
        public final Color brighter;

        /** Contour color */
        public final Color contour;

        public IconColor(int r, int g, int b) {
            super(r, g, b);
            brighter = this.brighter();
            contour = this.darker();
            plainColor = new Color(r, g, b);
            index = registerIconColor(this);
        }

        public IconColor(Color normal, Color brighter) {
            super(normal.getRed(), normal.getGreen(), normal.getBlue());
            this.brighter = brighter;
            this.contour = new Color((int)(normal.getRed() * 0.83), (int)(normal.getGreen() * 0.83), (int)(normal.getBlue() * 0.83));
            plainColor = normal;
            index = registerIconColor(this);
        }

        public IconColor(Color normal, Color brighter, Color contour) {
            super(normal.getRed(), normal.getGreen(), normal.getBlue());
            this.brighter = brighter;
            this.contour = contour;
            plainColor = normal;
            index = registerIconColor(this);
        }
    }

    /** Indexed Background Color */
    public static class BackgroundColor extends Color {

        /** UID */
        private static final long serialVersionUID = -6447560429557999239L;

        /** Index of color */
        public final int index;

        public BackgroundColor(int r, int g, int b) {
            super(r, g, b);
            index = registerBackgroundColor(this);
        }
    }

    /**
     * Draws/creates connector icon of specified type
     *
     * @param iconType Type of connector icon composed from flags above (see above)
     * @param iconColor Color of icon
     * @param background Background color of icon
     * @return Created icon
     */
    private static ConnectorIcon createIcon(int iconType, IconColor iconColor, BackgroundColor background, int height) {
        assert(height > 0);
        Color color = (iconType & BRIGHTER_COLOR) != 0 ? iconColor.brighter : iconColor;
        boolean inputPort = (iconType & OUTPUT) == 0;
        boolean proxy = (iconType & PROXY) != 0;
        boolean rpc = (iconType & RPC) != 0;
        int colorInt = color.getRGB();
        int backgroundInt = background.getRGB();
        int width = (height + 1) / 2;
        if (rpc) {
            width = proxy ? 9 : (inputPort ? 14 : 12);
        }
        BufferedImageRGB img = new BufferedImageRGB(width, height);
        ConnectorIcon icon = new ConnectorIcon();
        icon.defaultLineStart.y = height / 2;
        img.drawFilledRectangle(img.getBounds(), inputPort && (!rpc) && (!proxy) ? colorInt : backgroundInt);
        int[] buffer = img.getBuffer();

        if (!rpc) {

            if (!proxy) {

                // draw triangle
                int top = 0;
                int bottom = (img.getHeight() - 1) * img.getWidth();
                int count = 1;
                while (top <= bottom) {
                    for (int i = 0; i < count; i++) {
                        buffer[top + i] = inputPort ? backgroundInt : colorInt;
                        buffer[bottom + i] = inputPort ? backgroundInt : colorInt;
                    }
                    top += img.getWidth();
                    bottom -= img.getWidth();
                    count++;
                }
                icon.defaultLineStart.x = inputPort ? 0 : width;

                if (inputPort) {
                    img.mirrorLeftRight();
                }

            } else {
                int primaryHeight = height - 6;
                icon.defaultLineStart.y = primaryHeight / 2;
                if (!inputPort) {
                    for (int i = 0; i < primaryHeight; i++) {
                        img.drawLine(new Point(0, i), new Point(3 + primaryHeight / 2 - Math.abs(i - primaryHeight / 2), i), color);
                    }
                    for (int i = 0; i < 5; i++) {
                        int h = primaryHeight + 1;
                        img.drawLine(new Point(0, h + i), new Point(1 + Math.abs(2 - i), h + i), color);
                    }
                    icon.defaultLineStart.x = 4 + primaryHeight / 2;
                    icon.outgoingLineStart = icon.defaultLineStart;
                    icon.incomingLineStart = new Point(1, height - 3);
                } else {
                    for (int i = 0; i < primaryHeight; i++) {
                        img.drawLine(new Point(0, i), new Point(Math.abs(i - primaryHeight / 2), i), color);
                    }
                    for (int i = 0; i < 5; i++) {
                        int h = primaryHeight + 1;
                        img.drawLine(new Point(0, h + i), new Point(primaryHeight / 2 - Math.abs(2 - i), h + i), color);
                    }
                    icon.defaultLineStart.x = 1;
                    icon.incomingLineStart = icon.defaultLineStart;
                    icon.outgoingLineStart = new Point(primaryHeight / 2, height - 3);
                }
            }

        } else {

            int middle = height / 2;
            int h4 = height / 4;
            img.drawLine(new Point(0, 0), new Point(0, height - 1), color);

            if (!proxy) {
                img.drawFilledRectangle(new Rectangle(0, middle - 1, 4, 3), colorInt);
            } else {
                img.drawFilledRectangle(new Rectangle(0, h4, 4, 2), colorInt);
                img.drawFilledRectangle(new Rectangle(0, height - h4 - 1, 4, 2), colorInt);
            }

            Graphics2D g = img.getBufferedImage().createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            if (proxy) {
                g.drawOval(4, 0, middle, middle);
                g.fillOval(3, height - h4 - 1 - (h4 / 2), h4 + 2, h4 + 2);
            } else if (!inputPort) {
                g.drawOval(4, middle - 6, 13, 13);
            } else {
                g.fillOval(4, middle - 4, 9, 9);
            }
            icon.defaultLineStart.x = img.getWidth() - 3;

            if (proxy) {
                icon.outgoingLineStart = new Point(img.getWidth() - 2, h4 + 1);
                icon.incomingLineStart = new Point(img.getWidth() - 1, height - h4);
            }
        }

        // scan for insets
        for (int i = 0; i < img.getIconWidth(); i++) {
            if (icon.insetTop == Integer.MIN_VALUE && img.getPixel(i, 0) == backgroundInt) {
                icon.insetTop = (img.getWidth() - i) + (img.getPixel(i, 1) != backgroundInt ? 1 : 0);
            }
            if (icon.insetBottom == Integer.MIN_VALUE && img.getPixel(i, img.getIconHeight() - 1) == backgroundInt) {
                icon.insetBottom = (img.getWidth() - i) + (img.getPixel(i, img.getIconHeight() - 2) != backgroundInt ? 1 : 0);
            }
        }

        if ((iconType & RIGHT_TREE) > 0) {
            img.mirrorLeftRight();
        }
        if (icon.incomingLineStart == null) {
            icon.incomingLineStart = icon.defaultLineStart;
        }
        if (icon.outgoingLineStart == null) {
            icon.outgoingLineStart = icon.defaultLineStart;
        }
        icon.setImage(img.getBufferedImage());

        return icon;
    }

    /**
     * @return Insets (white pixels) at top
     */
    public int getInsetTop() {
        return insetTop;
    }

    /**
     * @return Insets (white pixels) at bottom
     */
    public int getInsetBottom() {
        return insetBottom;
    }
}
