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
package org.finroc.tools.gui.commons.fastdraw;

import java.net.URL;

/**
 * @author Max Reichardt
 *
 * Image mit Alpha-Information im Pixel
 */
public class BufferedImageARGBColorAdd extends BufferedImageARGBColorable {

    /** UID */
    private static final long serialVersionUID = 4253400348181769150L;

    public BufferedImageARGBColorAdd(URL resource) throws Exception {
        super(resource);
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
            int resultR = ((Math.min(srcPixelR + colorR, 255) * alpha) >> 8) + ((destPixelR * alphaM) >> 8);
            int resultG = ((Math.min(srcPixelG + colorG, 255) * alpha) >> 8) + ((destPixelG * alphaM) >> 8);
            int resultB = ((Math.min(srcPixelB + colorB, 255) * alpha) >> 8) + ((destPixelB * alphaM) >> 8);

            destBuffer[destOffset] = BitOps.merge(resultR, resultG, resultB);
            destOffset++;
            srcOffset++;
        }
    }
}
