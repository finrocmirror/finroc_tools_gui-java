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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.finroc.core.buffer.CoreInput;
import org.finroc.core.buffer.CoreOutput;
import org.finroc.core.port.std.PortDataDelegate;
import org.finroc.core.port.std.PortDataManager;
import org.finroc.core.port.std.PortDataReference;
import org.finroc.core.portdatabase.DataType;
import org.finroc.core.portdatabase.DataTypeRegister;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.plugin.datatype.Blittable;
import org.finroc.plugin.datatype.HasBlittable;

public class CompressedImage extends Blittable implements HasBlittable {

    private byte[] compressedData;
    private int dataSize;

    public static DataType TYPE = DataTypeRegister.getInstance().getDataType(CompressedImage.class);

    private transient BufferedImage uncompressedImage;

    /** UID */
    private static final long serialVersionUID = 854673830566034823L;

    public CompressedImage(byte[] data) {
        compressedData = data;
    }

    @Override
    protected void blitLineToRGB(int[] destBuffer, int destOffset, int srcX, int srcY, int srcOffset, int width) {
        /*checkUncompressed();
        byte[] src = uncompressedImage.getBufferByte();
        srcOffset *= 4 + 1;
        for (int i = 0; i < width; i++) {
            try {
                destBuffer[destOffset] = toInt(src[srcOffset], src[srcOffset + 1], src[srcOffset + 2]);
            } catch (Exception e) {

            }
            srcOffset += 3;
            destOffset++;
        }*/
    }

    @Override
    public void blitTo(Destination destination, Point dest, Rectangle sourceArea) {
        checkUncompressed();
        destination.getBufferedImage().createGraphics().drawImage(uncompressedImage, dest.x, dest.y, sourceArea.width, sourceArea.height, Color.black, null);
    }

    protected int toInt(byte r, byte g, byte b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    @Override
    public int getHeight() {
        checkUncompressed();
        return uncompressedImage.getHeight();
    }

    @Override
    public int getWidth() {
        checkUncompressed();
        return uncompressedImage.getWidth();
    }

    private void checkUncompressed() {
        if (uncompressedImage == null) {
            try {
                uncompressedImage = ImageIO.read(new ByteArrayInputStream(compressedData));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void deserialize(CoreInput is) {
        int size = is.readInt();
        if (compressedData.length < size) {
            compressedData = new byte[size * 2]; // keep some bytes for reserve...
        }
        dataSize = size;
        is.readFully(compressedData, 0, dataSize);
    }

    @Override
    public void serialize(CoreOutput os) {
        os.writeInt(dataSize);
        os.write(compressedData, 0, dataSize);
    }

    @Override
    public Blittable getBlittable() {
        return this;
    }

    // stuff to make this valid port data

    @JavaOnly private PortDataDelegate delegate = new PortDataDelegate(this);
    @Override @JavaOnly public PortDataReference getCurReference() {
        return delegate.getCurReference();
    }
    @Override @JavaOnly public PortDataManager getManager() {
        return delegate.getManager();
    }
    @Override @JavaOnly public DataType getType() {
        return delegate.getType();
    }
    @Override public void handleRecycle() {}
}
