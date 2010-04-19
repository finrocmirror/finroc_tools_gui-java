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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;

import org.finroc.jc.annotation.JavaOnly;
import org.finroc.core.buffer.CoreInput;
import org.finroc.core.buffer.CoreOutput;
import org.finroc.core.port.std.PortDataDelegate;
import org.finroc.core.port.std.PortDataManager;
import org.finroc.core.port.std.PortDataReference;
import org.finroc.core.portdatabase.DataType;
import org.finroc.core.portdatabase.DataTypeRegister;


/**
 * @author max
 *
 * Base class for different types of buffered image
 *
 * They usually have 32 bits per pixel and Int-Array-Buffers
 */
public abstract class FastBufferedImage extends Blittable implements Paintable {

    /** UID */
    private static final long serialVersionUID = 820927637285545160L;

    public static DataType TYPE = DataTypeRegister.getInstance().getDataType(FastBufferedImage.class);

    /**
     * Wrapped BufferedImage
     */
    protected BufferedImage wrapped;

    /**
     * with this constructor, 'wrapped' has to be initialized by subclass
     */
    public FastBufferedImage() {}

    /**
     * @param width
     * @param height
     * @param imageType
     */
    public FastBufferedImage(int width, int height) {
        wrapped = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    public FastBufferedImage(BufferedImage img, boolean forceIntBuffer) {
        //this(img.getWidth(), img.getHeight());
        DataBuffer db = img.getRaster().getDataBuffer();
        if ((db instanceof DataBufferInt) || !forceIntBuffer) {
            //System.arraycopy(((DataBufferInt)db).getData(), 0, buffer, 0,  buffer.length);
            wrapped = img;
        } else if (db instanceof DataBufferByte) {
            wrapped = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            int[] buffer = getBuffer();
            int len = buffer.length;
            byte[] bs = ((DataBufferByte)db).getData();
            for (int i = 0; i < len; i++) {
                buffer[i] = BitOps.merge(bs[i*4+3], bs[i*4+1], bs[i*4+2], bs[i*4]);
            }
        }
    }

    public int[] getBuffer() {
        return ((DataBufferInt)wrapped.getRaster().getDataBuffer()).getData();
    }

    public byte[] getBufferByte() {
        return ((DataBufferByte)wrapped.getRaster().getDataBuffer()).getData();
    }

    public int getWidth() {
        return wrapped.getWidth();
    }
    public int getHeight() {
        return wrapped.getHeight();
    }

    public void save(File file) {
        try {
            ImageIO.write(wrapped, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getPixel(int x, int y) {
        return getBuffer()[y * getWidth() + x];
    }

    public Graphics2D getGraphics() {
        return wrapped.createGraphics();
    }

    public void paint(Graphics2D g) {
        //g.translate(-getWidth() / 2, -getHeight() / 2);
        g.drawImage(wrapped, 0, 0, null);
    }

    public void setPixel(int x, int y, int value) {
        getBuffer()[y * getWidth() + x] = value;
    }

    public void save(String formatName, ByteArrayOutputStream baos) throws Exception {
        ImageIO.write(wrapped, formatName, baos);
    }

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
    @Override
    public void deserialize(CoreInput is) {
        throw new RuntimeException("currently unsupported");
    }

    @Override
    public void serialize(CoreOutput os) {
        throw new RuntimeException("currently unsupported");
    }

}
