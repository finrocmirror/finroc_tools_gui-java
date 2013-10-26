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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;

import org.finroc.tools.gui.FinrocGUI;
import org.rrlib.finroc_core_utils.log.LogLevel;
import org.finroc.plugins.data_types.Blittable;
import org.finroc.plugins.data_types.Paintable;

/**
 * @author Max Reichardt
 *
 * Base class for different types of buffered image
 *
 * They usually have 32 bits per pixel and Int-Array-Buffers
 */
public abstract class FastBufferedImage extends Blittable implements Paintable {

    /** UID */
    private static final long serialVersionUID = 820927637285545160L;

    /**
     * Wrapped BufferedImage
     */
    protected BufferedImage wrapped;

    /**
     * Graphics to draw into this buffered image - lazyly initialized
     */
    private Graphics2D graphics = null;

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
                @SuppressWarnings("unused")
                int type = img.getType();
                if (img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
                    buffer[i] = BitOps.merge(bs[i * 4], bs[i * 4 + 3], bs[i * 4 + 2], bs[i * 4 + 1]);
                } else {
                    buffer[i] = BitOps.merge(bs[i * 4 + 3], bs[i * 4 + 2], bs[i * 4 + 1], bs[i * 4]);
                } /*else if (img.getType() == BufferedImage.TYPE){
                    buffer[i] = BitOps.merge(bs[i*4+3], bs[i*4+1], bs[i*4+2], bs[i*4]);
                }*/
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
            FinrocGUI.logDomain.log(LogLevel.ERROR, toString(), e);
        }
    }

    public int getPixel(int x, int y) {
        return getBuffer()[y * getWidth() + x];
    }

    public Graphics2D getGraphics() {
        if (graphics == null) {
            graphics = wrapped.createGraphics();
        }
        return graphics;
        //return wrapped.createGraphics();
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

    /*
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
     */
}
