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

    /** Registered colors for icons */
    private static final ArrayList<Color> iconColors = new ArrayList<Color>();

    /** Registered colors for icon backgrounds */
    private static final ArrayList<Color> backgroundColors = new ArrayList<Color>();

    /** Maximum index for icon cache (see calculation in Type.indexInCache()) */
    private static final int MAX_INDEX = 1 << 10;

    /** Available cached icons */
    private static final ConnectorIcon iconCache[] = new ConnectorIcon[MAX_INDEX];

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
     * @param type Icon to get
     * @param height Height of icon
     * @return Icon
     */
    public static ConnectorIcon getIcon(Type type, int height) {
        ConnectorIcon icon = iconCache[type.getIndexInCache()];
        if (icon == null) {
            // Note with respect to concurrency: Since always the same icons are generated,
            // it does not hurt if concurrent threads generate the same icon in parallel.
            // One of them will be garbage collected.
            icon = createIcon(type, height);
            iconCache[type.getIndexInCache()] = icon;
        } else if (icon.getIconHeight() != height) {
            Log.log(LogLevel.WARNING, "Icons of different size not properly supported yet"); // Could be added with reasonable effort
            return createIcon(type, height);
        }
        return icon;
    }

    /** Indexed Icon Color */
    public static class IconColor {
        public final Color color;
        public final int index;

        public IconColor(Color c) {
            color = c;
            index = registerIconColor(c);
        }
    }

    /** Indexed Background Color */
    public static class BackgroundColor {
        public final Color color;
        public final int index;

        public BackgroundColor(Color c) {
            color = c;
            index = registerBackgroundColor(c);
        }
    }

    /**
     * Unique id/type of every icon
     */
    public static class Type {

        /** Output port? (displayed as outgoing arrow - or as client) */
        public boolean outputPort;

        /** Proxy/routing port? */
        public boolean proxy;

        /** RPC port? */
        public boolean rpc;

        /** In right connection panel tree? */
        public boolean rightTree;

        /** Brighter version of foreground color? */
        public boolean brighterColor;

        /** Color of icon (Register index in iconColors) */
        public IconColor color;

        /** Background color of icon (Register index in backgroundColors) */
        public BackgroundColor background;

        /**
         * Method to set all variables at once for convenience
         */
        public void set(boolean outputPort, boolean proxy, boolean rpc, boolean rightTree, boolean brighterColor, IconColor color, BackgroundColor background) {
            this.outputPort = outputPort;
            this.proxy = proxy;
            this.rpc = rpc;
            this.rightTree = rightTree;
            this.brighterColor = brighterColor;
            this.color = color;
            this.background = background;
        }

        /**
         * @return Index in icon cache (10 bit: [background color 2 bit][color 3 bit][flags 5 bit]
         */
        private int getIndexInCache() {
            return (outputPort ? 1 : 0) | (proxy ? 2 : 0) | (rpc ? 4 : 0) | (rightTree ? 8 : 0)  | (brighterColor ? 16 : 0) | color.index << 5 | background.index << 8;
        }
    }

    /**
     * Draws/creates connector icon of specified type
     *
     * @param type Type of icon to draw
     * @return Created icon
     */
    private static ConnectorIcon createIcon(Type type, int height) {
        assert(height > 0);
        Color color = type.brighterColor ? type.color.color.brighter() : type.color.color;
        Color background = type.background.color;
        boolean inputPort = !type.outputPort;
        int colorInt = color.getRGB();
        int backgroundInt = background.getRGB();
        int width = (height + 1) / 2;
        if (type.rpc) {
            width = type.proxy ? 9 : (inputPort ? 12 : 13);
        }
        BufferedImageRGB img = new BufferedImageRGB(width, height);
        ConnectorIcon icon = new ConnectorIcon();
        icon.defaultLineStart.y = height / 2;
        img.drawFilledRectangle(img.getBounds(), inputPort && (!type.rpc) && (!type.proxy) ? colorInt : backgroundInt);
        int[] buffer = img.getBuffer();

        if (!type.rpc) {

            if (!type.proxy) {

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
                if (type.outputPort) {
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

            if (!type.proxy) {
                img.drawFilledRectangle(new Rectangle(0, middle - 1, 4, 3), colorInt);
            } else {
                img.drawFilledRectangle(new Rectangle(0, h4, 4, 2), colorInt);
                img.drawFilledRectangle(new Rectangle(0, height - h4 - 1, 4, 2), colorInt);
            }

            Graphics2D g = img.getBufferedImage().createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            if (type.proxy) {
                g.drawOval(4, 0, middle, middle);
                g.fillOval(3, height - h4 - 1 - (h4 / 2), h4 + 2, h4 + 2);
            } else if (!inputPort) {
                g.drawOval(4, middle - 6, 13, 13);
            } else {
                g.fillOval(4, middle - 4, 9, 9);
            }
            icon.defaultLineStart.x = img.getWidth() - 3;

            if (type.proxy) {
                icon.outgoingLineStart = new Point(img.getWidth() - 2, h4 + 1);
                icon.incomingLineStart = new Point(img.getWidth() - 1, height - h4);
            }
        }

        if (type.rightTree) {
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
}
