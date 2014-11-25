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
package org.finroc.tools.gui.util;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;


/**
 * @author Max Reichardt
 *
 * Completely wraps a Java2D Graphics2D object.
 * By default, every call is forwarded to the wrapped Graphics2D.
 * By creating a subclass, certain methods can be overridden in order to perform
 * custom painting (e.g. transform all colors to grayscale)
 */
public class Graphics2DWrapper extends Graphics2D {

    /** Wrapped Graphics2D object */
    private Graphics2D wrapped;

    /**
     * @return Wrapped Graphics2D object
     */
    public Graphics2D getWrapped() {
        return wrapped;
    }

    /**
     * @param wrapped Wrapped Graphics2D object
     */
    public void setWrapped(Graphics2D wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return wrapped.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        return wrapped.equals(obj);
    }

    /**
     * @see java.awt.Graphics#create()
     */
    public Graphics create() {
        return wrapped.create();
    }

    /**
     * @see java.awt.Graphics#create(int, int, int, int)
     */
    public Graphics create(int x, int y, int width, int height) {
        return wrapped.create(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics#getColor()
     */
    public Color getColor() {
        return wrapped.getColor();
    }

    /**
     * @see java.awt.Graphics#setColor(java.awt.Color)
     */
    public void setColor(Color c) {
        wrapped.setColor(c);
    }

    /**
     * @see java.awt.Graphics#setPaintMode()
     */
    public void setPaintMode() {
        wrapped.setPaintMode();
    }

    /**
     * @see java.awt.Graphics#setXORMode(java.awt.Color)
     */
    public void setXORMode(Color c1) {
        wrapped.setXORMode(c1);
    }

    /**
     * @see java.awt.Graphics#getFont()
     */
    public Font getFont() {
        return wrapped.getFont();
    }

    /**
     * @see java.awt.Graphics#setFont(java.awt.Font)
     */
    public void setFont(Font font) {
        wrapped.setFont(font);
    }

    /**
     * @see java.awt.Graphics#getFontMetrics()
     */
    public FontMetrics getFontMetrics() {
        return wrapped.getFontMetrics();
    }

    /**
     * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
     */
    public FontMetrics getFontMetrics(Font f) {
        return wrapped.getFontMetrics(f);
    }

    /**
     * @see java.awt.Graphics#getClipBounds()
     */
    public Rectangle getClipBounds() {
        return wrapped.getClipBounds();
    }

    /**
     * @see java.awt.Graphics#clipRect(int, int, int, int)
     */
    public void clipRect(int x, int y, int width, int height) {
        wrapped.clipRect(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics#setClip(int, int, int, int)
     */
    public void setClip(int x, int y, int width, int height) {
        wrapped.setClip(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics#getClip()
     */
    public Shape getClip() {
        return wrapped.getClip();
    }

    /**
     * @see java.awt.Graphics#setClip(java.awt.Shape)
     */
    public void setClip(Shape clip) {
        wrapped.setClip(clip);
    }

    /**
     * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        wrapped.copyArea(x, y, width, height, dx, dy);
    }

    /**
     * @see java.awt.Graphics#drawLine(int, int, int, int)
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        wrapped.drawLine(x1, y1, x2, y2);
    }

    /**
     * @see java.awt.Graphics#fillRect(int, int, int, int)
     */
    public void fillRect(int x, int y, int width, int height) {
        wrapped.fillRect(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics#drawRect(int, int, int, int)
     */
    public void drawRect(int x, int y, int width, int height) {
        wrapped.drawRect(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics2D#draw3DRect(int, int, int, int, boolean)
     */
    public void draw3DRect(int x, int y, int width, int height, boolean raised) {
        wrapped.draw3DRect(x, y, width, height, raised);
    }

    /**
     * @see java.awt.Graphics#clearRect(int, int, int, int)
     */
    public void clearRect(int x, int y, int width, int height) {
        wrapped.clearRect(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
     */
    public void drawRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        wrapped.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    /**
     * @see java.awt.Graphics2D#fill3DRect(int, int, int, int, boolean)
     */
    public void fill3DRect(int x, int y, int width, int height, boolean raised) {
        wrapped.fill3DRect(x, y, width, height, raised);
    }

    /**
     * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
     */
    public void fillRoundRect(int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        wrapped.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
    }

    /**
     * @see java.awt.Graphics2D#draw(java.awt.Shape)
     */
    public void draw(Shape s) {
        wrapped.draw(s);
    }

    /**
     * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return wrapped.drawImage(img, xform, obs);
    }

    /**
     * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage, java.awt.image.BufferedImageOp, int, int)
     */
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        wrapped.drawImage(img, op, x, y);
    }

    /**
     * @see java.awt.Graphics#drawOval(int, int, int, int)
     */
    public void drawOval(int x, int y, int width, int height) {
        wrapped.drawOval(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage, java.awt.geom.AffineTransform)
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        wrapped.drawRenderedImage(img, xform);
    }

    /**
     * @see java.awt.Graphics#fillOval(int, int, int, int)
     */
    public void fillOval(int x, int y, int width, int height) {
        wrapped.fillOval(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
     */
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        wrapped.drawArc(x, y, width, height, startAngle, arcAngle);
    }

    /**
     * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage, java.awt.geom.AffineTransform)
     */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        wrapped.drawRenderableImage(img, xform);
    }

    /**
     * @see java.awt.Graphics2D#drawString(java.lang.String, int, int)
     */
    public void drawString(String str, int x, int y) {
        wrapped.drawString(str, x, y);
    }

    /**
     * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
     */
    public void fillArc(int x, int y, int width, int height, int startAngle,
                        int arcAngle) {
        wrapped.fillArc(x, y, width, height, startAngle, arcAngle);
    }

    /**
     * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
     */
    public void drawString(String str, float x, float y) {
        wrapped.drawString(str, x, y);
    }

    /**
     * @see java.awt.Graphics#drawPolyline(int[], int[], int)
     */
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
        wrapped.drawPolyline(xPoints, yPoints, nPoints);
    }

    /**
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, int, int)
     */
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        wrapped.drawString(iterator, x, y);
    }

    /**
     * @see java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        wrapped.drawPolygon(xPoints, yPoints, nPoints);
    }

    /**
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)
     */
    public void drawString(AttributedCharacterIterator iterator, float x,
                           float y) {
        wrapped.drawString(iterator, x, y);
    }

    /**
     * @see java.awt.Graphics#drawPolygon(java.awt.Polygon)
     */
    public void drawPolygon(Polygon p) {
        wrapped.drawPolygon(p);
    }

    /**
     * @see java.awt.Graphics#fillPolygon(int[], int[], int)
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        wrapped.fillPolygon(xPoints, yPoints, nPoints);
    }

    /**
     * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector, float, float)
     */
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        wrapped.drawGlyphVector(g, x, y);
    }

    /**
     * @see java.awt.Graphics#fillPolygon(java.awt.Polygon)
     */
    public void fillPolygon(Polygon p) {
        wrapped.fillPolygon(p);
    }

    /**
     * @see java.awt.Graphics2D#fill(java.awt.Shape)
     */
    public void fill(Shape s) {
        wrapped.fill(s);
    }

    /**
     * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
     */
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        return wrapped.hit(rect, s, onStroke);
    }

    /**
     * @see java.awt.Graphics#drawChars(char[], int, int, int, int)
     */
    public void drawChars(char[] data, int offset, int length, int x, int y) {
        wrapped.drawChars(data, offset, length, x, y);
    }

    /**
     * @see java.awt.Graphics2D#getDeviceConfiguration()
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        return wrapped.getDeviceConfiguration();
    }

    /**
     * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
     */
    public void setComposite(Composite comp) {
        wrapped.setComposite(comp);
    }

    /**
     * @see java.awt.Graphics#drawBytes(byte[], int, int, int, int)
     */
    public void drawBytes(byte[] data, int offset, int length, int x, int y) {
        wrapped.drawBytes(data, offset, length, x, y);
    }

    /**
     * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
     */
    public void setPaint(Paint paint) {
        wrapped.setPaint(paint);
    }

    /**
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return wrapped.drawImage(img, x, y, observer);
    }

    /**
     * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
     */
    public void setStroke(Stroke s) {
        wrapped.setStroke(s);
    }

    /**
     * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key, java.lang.Object)
     */
    public void setRenderingHint(Key hintKey, Object hintValue) {
        wrapped.setRenderingHint(hintKey, hintValue);
    }

    /**
     * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
     */
    public Object getRenderingHint(Key hintKey) {
        return wrapped.getRenderingHint(hintKey);
    }

    /**
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return wrapped.drawImage(img, x, y, width, height, observer);
    }

    /**
     * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
     */
    public void setRenderingHints(Map<?, ?> hints) {
        wrapped.setRenderingHints(hints);
    }

    /**
     * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
     */
    public void addRenderingHints(Map<?, ?> hints) {
        wrapped.addRenderingHints(hints);
    }

    /**
     * @see java.awt.Graphics2D#getRenderingHints()
     */
    public RenderingHints getRenderingHints() {
        return wrapped.getRenderingHints();
    }

    /**
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, Color bgcolor,
                             ImageObserver observer) {
        return wrapped.drawImage(img, x, y, bgcolor, observer);
    }

    /**
     * @see java.awt.Graphics2D#translate(int, int)
     */
    public void translate(int x, int y) {
        wrapped.translate(x, y);
    }

    /**
     * @see java.awt.Graphics2D#translate(double, double)
     */
    public void translate(double tx, double ty) {
        wrapped.translate(tx, ty);
    }

    /**
     * @see java.awt.Graphics2D#rotate(double)
     */
    public void rotate(double theta) {
        wrapped.rotate(theta);
    }

    /**
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        return wrapped.drawImage(img, x, y, width, height, bgcolor, observer);
    }

    /**
     * @see java.awt.Graphics2D#rotate(double, double, double)
     */
    public void rotate(double theta, double x, double y) {
        wrapped.rotate(theta, x, y);
    }

    /**
     * @see java.awt.Graphics2D#scale(double, double)
     */
    public void scale(double sx, double sy) {
        wrapped.scale(sx, sy);
    }

    /**
     * @see java.awt.Graphics2D#shear(double, double)
     */
    public void shear(double shx, double shy) {
        wrapped.shear(shx, shy);
    }

    /**
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
        return wrapped.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }

    /**
     * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
     */
    public void transform(AffineTransform Tx) {
        wrapped.transform(Tx);
    }

    /**
     * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
     */
    public void setTransform(AffineTransform Tx) {
        wrapped.setTransform(Tx);
    }

    /**
     * @see java.awt.Graphics2D#getTransform()
     */
    public AffineTransform getTransform() {
        return wrapped.getTransform();
    }

    /**
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgcolor, ImageObserver observer) {
        return wrapped.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer);
    }

    /**
     * @see java.awt.Graphics2D#getPaint()
     */
    public Paint getPaint() {
        return wrapped.getPaint();
    }

    /**
     * @see java.awt.Graphics2D#getComposite()
     */
    public Composite getComposite() {
        return wrapped.getComposite();
    }

    /**
     * @see java.awt.Graphics2D#setBackground(java.awt.Color)
     */
    public void setBackground(Color color) {
        wrapped.setBackground(color);
    }

    /**
     * @see java.awt.Graphics2D#getBackground()
     */
    public Color getBackground() {
        return wrapped.getBackground();
    }

    /**
     * @see java.awt.Graphics2D#getStroke()
     */
    public Stroke getStroke() {
        return wrapped.getStroke();
    }

    /**
     * @see java.awt.Graphics2D#clip(java.awt.Shape)
     */
    public void clip(Shape s) {
        wrapped.clip(s);
    }

    /**
     * @see java.awt.Graphics2D#getFontRenderContext()
     */
    public FontRenderContext getFontRenderContext() {
        return wrapped.getFontRenderContext();
    }

    /**
     * @see java.awt.Graphics#dispose()
     */
    public void dispose() {
        wrapped.dispose();
    }

    /**
     * @see java.awt.Graphics#finalize()
     */
    public void finalize() {
        wrapped.finalize();
    }

    /**
     * @see java.awt.Graphics#toString()
     */
    public String toString() {
        return wrapped.toString();
    }

    /**
     * @see java.awt.Graphics#getClipRect()
     */
    public Rectangle getClipRect() {
        return wrapped.getClipRect();
    }

    /**
     * @see java.awt.Graphics#hitClip(int, int, int, int)
     */
    public boolean hitClip(int x, int y, int width, int height) {
        return wrapped.hitClip(x, y, width, height);
    }

    /**
     * @see java.awt.Graphics#getClipBounds(java.awt.Rectangle)
     */
    public Rectangle getClipBounds(Rectangle r) {
        return wrapped.getClipBounds(r);
    }
}
