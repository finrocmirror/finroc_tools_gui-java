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

import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.net.URL;

import javax.imageio.ImageIO;

import org.finroc.core.portdatabase.DataType;
import org.finroc.core.portdatabase.DataTypeRegister;

/**
 * @author max
 *
 * Image mit Alpha-Information im Pixel
 */
public class BufferedImageARGBColorable extends FastBufferedImage {

    /** UID */
    private static final long serialVersionUID = 4192556769255842657L;

    public static DataType TYPE = DataTypeRegister.getInstance().getDataType(BufferedImageARGBColorable.class);

    public BufferedImageARGBColorable(URL resource) throws Exception {
        super(ImageIO.read(new BufferedInputStream(resource.openStream())), true);
    }

    protected int colorR = 256, colorG = 256, colorB = 256; // Temporäre Variablen, in denen zusätzlich Blitting-Parameter (Farbe) abgelegt werden

    public void blitToInColor(BufferedImageRGB destination, Point dest, Rectangle sourceArea, int color) {
        colorR = BitOps.getByte(color, 1);
        colorG = BitOps.getByte(color, 2);
        colorB = BitOps.getByte(color, 3);
        super.blitTo(destination, dest, sourceArea);
    }

    @Override
    protected void blitLineToRGB(int[] destBuffer, int destOffset, int srcX, int srcY, int srcOffset, int width) {
        int[] srcBuffer = getBuffer();
        for (int x = 0; x < width; x++) {

            // with pseudo-MMX(/SIMD)  --- not anymore--- not possible :-(
            int srcPixel = srcBuffer[srcOffset];
            int destPixel = destBuffer[destOffset];
            int srcPixelR = BitOps.getByte(srcPixel, 1);
            int srcPixelG = BitOps.getByte(srcPixel, 2);
            int srcPixelB = BitOps.getByte(srcPixel, 3);
            int destPixelR = BitOps.getByte(destPixel, 1);
            int destPixelG = BitOps.getByte(destPixel, 2);
            int destPixelB = BitOps.getByte(destPixel, 3);
            int alpha = BitOps.cleanByte((byte)((srcPixel >> 24) & 0xFF));
            int alphaM = 256 - alpha;
            int resultR = ((srcPixelR * colorR * alpha) >> 16) + ((destPixelR * alphaM) >> 8);
            int resultG = ((srcPixelG * colorG * alpha) >> 16) + ((destPixelG * alphaM) >> 8);
            int resultB = ((srcPixelB * colorB * alpha) >> 16) + ((destPixelB * alphaM) >> 8);

            destBuffer[destOffset] = BitOps.merge(resultR, resultG, resultB);
            destOffset++;
            srcOffset++;
        }
    }
}
