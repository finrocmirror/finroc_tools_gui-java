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
package org.finroc.gui.commons.fastdraw;

import java.awt.image.BufferedImage;

import org.finroc.core.portdatabase.DataType;
import org.finroc.core.portdatabase.DataTypeRegister;

/**
 * @author max
 *
 * Class for partly transparent images with convex shape.
 * That means each scan line has a certain offset and width <= image width.
 */
public class BufferedImageRGBConvexShape extends FastBufferedImage {

    /** UID */
    private static final long serialVersionUID = 7351558429252555162L;

    public static DataType TYPE = DataTypeRegister.getInstance().getDataType(BufferedImageRGBConvexShape.class);

    public enum BackgroundColorPixel { TopLeft, TopRight, BottomLeft, BottomRight }

    public BufferedImageRGBConvexShape(int width, int height) {
        super(width, height);
    }

    public BufferedImageRGBConvexShape() {}

    public BufferedImageRGBConvexShape(BufferedImage image, BackgroundColorPixel bcp) {
        super(image, true);
        int backgroundColor = 0;
        switch (bcp) {
        case TopLeft:
            backgroundColor = getPixel(0, 0);
            break;
        case TopRight:
            backgroundColor = getPixel(getWidth() - 1, 0);
            break;
        case BottomLeft:
            backgroundColor = getPixel(0, getHeight() - 1);
            break;
        case BottomRight:
            backgroundColor = getPixel(getWidth() - 1, getHeight() - 1);
            break;
        }
        init(backgroundColor);
    }

    /** Offset in destination image of line */
    private int[] lineOffset;

    /** Width of area to blit */
    private int[] lineWidth;

    @Override
    protected void blitLineToRGB(int[] destBuffer, int destOffset, int srcX, int srcY, int srcOffset, int width) {

        // maybe adjust srcoffset and width
        if (lineOffset[srcY] > srcX) {
            int diff = lineOffset[srcY] - srcX;
            srcX = lineOffset[srcY];
            srcOffset += diff;
            destOffset += diff;
            width -= diff;
        }
        if (srcX + width > lineOffset[srcY] + lineWidth[srcY]) {
            width = lineOffset[srcY] + lineWidth[srcY] - srcX;
        }
        if (width <= 0) {
            return;
        }
        System.arraycopy(getBuffer(), srcOffset, destBuffer, destOffset, width);
    }

    /**
     * Init lineOffset and lineWidth parameters.
     *
     * @param backgroundColor Color of background, which is cut away
     */
    public void init(int backgroundColor) {
        int height = wrapped.getHeight();
        int width = wrapped.getWidth();
        lineOffset = new int[height];
        lineWidth = new int[height];
        int[] buffer = getBuffer();
        for (int y = 0; y < height; y++) {
            int srcOffset = y * width;
            int curWidth = 0;
            while (curWidth < width && buffer[srcOffset] == backgroundColor) {
                srcOffset++;
                curWidth++;
            }
            lineOffset[y] = curWidth;
            while (curWidth < width && buffer[srcOffset] != backgroundColor) {
                srcOffset++;
                curWidth++;
            }
            lineWidth[y] = curWidth - lineOffset[y];
        }
    }

    public BufferedImage getBufferedImage() {
        return wrapped;
    }
}
