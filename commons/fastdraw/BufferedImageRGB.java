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
package org.finroc.tools.gui.commons.fastdraw;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import javax.swing.Icon;

import org.finroc.plugins.data_types.Blittable;
import org.finroc.plugins.data_types.util.FastBufferedImage;
import org.rrlib.serialization.BinaryInputStream;
import org.rrlib.serialization.BinaryOutputStream;
import org.rrlib.serialization.BinarySerializable;
import org.rrlib.serialization.rtti.DataType;

/**
 * @author Max Reichardt
 *
 * Image class that extends BufferedImage.
 * Image manipulation should be reasonably fast using this class.
 */
public class BufferedImageRGB extends FastBufferedImage implements Icon, Blittable.Destination, BinarySerializable {

    public final static DataType<BufferedImageRGB> TYPE = new DataType<BufferedImageRGB>(BufferedImageRGB.class);

    public BufferedImageRGB() {
        this(8, 8);
    }

    public BufferedImageRGB(int width, int height) {
        super(width, height);
    }

    public BufferedImageRGB(Dimension dim) {
        this(dim.width, dim.height);
    }

    public BufferedImageRGB(BufferedImage img, boolean forceIntBuffer) {
        super(img, forceIntBuffer);
    }

    public void drawFilledRectangle(Rectangle destArea, int rgb) {
        int destPos = destArea.y * getWidth() + destArea.x;
        for (int y = 0; y < destArea.height; y++) {
            Arrays.fill(getBuffer(), destPos, destPos + destArea.width, rgb);
            destPos += getWidth();
        }
    }

    public void mirrorLeftRight() {
        int[] buffer = getBuffer();
        int[] bufferTemp = new int[getWidth()];
        int bufferPos;
        for (int y = 0; y < getHeight(); y++) {
            // fill bufferTemp with row
            bufferPos = y * getWidth();
            System.arraycopy(buffer, bufferPos, bufferTemp, 0, getWidth());

            for (int i = 0; i < getWidth(); i++) {
                buffer[bufferPos + i] = bufferTemp[getWidth() - 1 - i];
            }
        }
    }

    public void drawRectangle(Rectangle r, int color) {
        drawHorizontalLine(r.x, r.y, r.width, color);
        drawHorizontalLine(r.x, r.y + r.height - 1, r.width, color);
        drawVerticalLine(r.x, r.y, r.height, color);
        drawVerticalLine(r.x + r.width - 1, r.y, r.height, color);
    }

    public void drawVerticalLine(int x, int y, int height, int color) {
        int width = getWidth();
        int startOffset = y * width + x;
        int[] buffer = getBuffer();
        for (int i = 0; i < height; i++) {
            buffer[startOffset] = color;
            startOffset += width;
        }
    }

    public void drawHorizontalLine(int x, int y, int width, int color) {
        int startOffset = y * getWidth() + x;
        Arrays.fill(getBuffer(), startOffset, startOffset + width, color);
    }

    @Override
    protected void blitLineToRGB(int[] destBuffer, int destOffset, int srcX, int srcY, int srcOffset, int width) {
        System.arraycopy(getBuffer(), srcOffset, destBuffer, destOffset, width);
    }

    public BufferedImage getBufferedImage() {
        return wrapped;
    }

    public void fill(int rgb) {
        Arrays.fill(getBuffer(), rgb);
    }

    public int getIconHeight() {
        return getHeight();
    }

    public int getIconWidth() {
        return getWidth();
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(wrapped, x, y, null);
    }

    public void drawLine(Point p1, Point p2, Color c) {
        Graphics g = wrapped.createGraphics();
        g.setColor(c);
        g.drawLine(p1.x, p1.y, p2.x, p2.y);
    }

    /**
     * Merge two colors in RGB format
     *
     * @param color1 first color
     * @param color2 second color
     * @param alpha Alpha (0-256). 0 = Color1, 256 = Color2
     * @return Merged Color
     */
    public static int mergeColors(int color1, int color2, int alpha) {
        int r1 = BitOps.getByte(color1, 1);
        int g1 = BitOps.getByte(color1, 2);
        int b1 = BitOps.getByte(color1, 3);
        int r2 = BitOps.getByte(color2, 1);
        int g2 = BitOps.getByte(color2, 2);
        int b2 = BitOps.getByte(color2, 3);
        int r = ((r1 * (256 - alpha) + r2 * alpha) >> 8) << 16;
        int g = ((g1 * (256 - alpha) + g2 * alpha) >> 8) << 8;
        int b = ((b1 * (256 - alpha) + b2 * alpha) >> 8);
        return r | g | b;
    }

    /**
     * Merge two colors in RGB format
     *
     * @param color1 first color
     * @param color2 second color
     * @param alpha Alpha (0-1). 0 = Color1, 1 = Color2
     * @return Merged Color
     */
    public static int mergeColors(int color1, int color2, float alpha) {
        return mergeColors(color1, color2, (int)(alpha * 256));
    }

    @Override
    public void serialize(BinaryOutputStream stream) {
        stream.writeInt(getWidth());
        stream.writeInt(getHeight());
        int[] raster = getBuffer();
        for (int i = 0, n = getWidth() * getHeight(); i < n; i++) {
            stream.writeInt(raster[i]);
        }
    }

    @Override
    public void deserialize(BinaryInputStream stream) throws Exception {
        int width = stream.readInt();
        int height = stream.readInt();
        if (width != getWidth() || height != getHeight()) {
            wrapped = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
        int[] raster = getBuffer();
        for (int i = 0, n = getWidth() * getHeight(); i < n; i++) {
            raster[i] = stream.readInt();
        }
    }

    /**
     * @param width New Width
     * @param height New Height
     */
    public void resize(int width, int height) {
        if (width != wrapped.getWidth() || height != wrapped.getHeight()) {
            wrapped = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }
    }
}
