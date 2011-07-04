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
package org.finroc.tools.gui.commons.fastdraw;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import com.kitfox.svg.RenderableElement;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;

import org.finroc.tools.gui.FinrocGUI;
import org.rrlib.finroc_core_utils.log.LogLevel;
import org.rrlib.finroc_core_utils.serialization.DataType;

/**
 * @author max
 *
 * SVG-Element that is buffered in intermediate buffer
 */
public class BufferedConvexSVG extends BufferedImageRGBConvexShape {

    /** UID */
    private static final long serialVersionUID = -1740092106641062565L;

    public final static DataType<BufferedConvexSVG> TYPE = new DataType<BufferedConvexSVG>(BufferedConvexSVG.class);

    /** xOffset and yOffset scaled */
    private int xOffset, yOffset;

    /** Dimension of scaled parent diagram (and officially this object) */
    private Dimension diagramDimension;
    private double scaleFactor = 1;

    /** reference to source element */
    private SVGElement element;
    private SVGDiagram parent;
    private int backgroundColor;
    private int color;

    private boolean initColor, initBuffer;

    public BufferedConvexSVG(SVG svg, String elementName, Color backgroundColor) throws Exception {
        this(svg.getDiagram(), svg.getElement(elementName), backgroundColor);
    }

    public BufferedConvexSVG(SVGDiagram parent, SVGElement svg, Color backgroundColor) throws Exception {
        if (svg == null) {
            throw new Exception("element is null");
        }
        element = svg;
        this.parent = parent;
        initBuffer = true;
        initColor = true;
        this.backgroundColor = backgroundColor.getRGB();
    }

    public void setDiagramSize(Dimension fitDiagramTo) {

        double scaleFactorX = fitDiagramTo.width / parent.getWidth();
        double scaleFactorY = fitDiagramTo.height / parent.getHeight();
        double scaleFactor = Math.min(scaleFactorX, scaleFactorY);
        if (this.scaleFactor == scaleFactor) {
            return;
        }
        this.scaleFactor = scaleFactor;
        diagramDimension = new Dimension((int)Math.round(parent.getWidth() * scaleFactor),
                                         (int)Math.round(parent.getHeight() * scaleFactor));
        initBuffer = true;
    }

    private void initBuffer() throws Exception {
        if (diagramDimension == null) {
            throw new Exception("Set diagram size first");
        }
        RenderableElement re = (RenderableElement)element;
        Rectangle2D bb = re.getBoundingBox();
        Rectangle2D scaledBB = new Rectangle2D.Double(bb.getX() * scaleFactor, bb.getY() * scaleFactor, bb.getWidth() * scaleFactor, bb.getHeight() * scaleFactor);
        xOffset = (int)Math.floor(scaledBB.getX());
        yOffset = (int)Math.floor(scaledBB.getY());
        int width = (int)Math.ceil(scaledBB.getMaxX() - xOffset);
        int height = (int)Math.ceil(scaledBB.getMaxY() - yOffset);
        wrapped = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        initBuffer = false;
        initColor();
    }

    private void initColor() throws Exception {
        RenderableElement re = (RenderableElement)element;
        Arrays.fill(getBuffer(), backgroundColor);
        AffineTransform at = new AffineTransform();
        at.setToScale(scaleFactor, scaleFactor);
        Graphics2D g = wrapped.createGraphics();
        g.setBackground(new Color(backgroundColor));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setTransform(at);
        g.translate(-((double)(xOffset)) / scaleFactor, -((double)(yOffset)) / scaleFactor);
        re.render(g);

        super.init(backgroundColor);
        initColor = false;
    }

    @Override
    public void blitTo(Destination destination, Point dest, Rectangle sourceArea) {
        if (!sourceArea.getSize().equals(diagramDimension)) {
            setDiagramSize(sourceArea.getSize());
        }
        try {
            if (initBuffer) {
                initBuffer();
            } else if (initColor) {
                initColor();
            }
        } catch (Exception e) {
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), e);
            return;
        }
        Point newDest = new Point(dest.x + xOffset - sourceArea.x, dest.y + yOffset - sourceArea.y);
        Rectangle newSourceArea = new Rectangle(wrapped.getWidth(), wrapped.getHeight());
        super.blitTo(destination, newDest, newSourceArea);
    }

    public void setBackground(Color color) {
        if (color.getRGB() == backgroundColor) {
            return;
        }
        backgroundColor = color.getRGB();
        initColor = true;
    }

    public void setColor(Color color) throws SVGElementException {
        if (color.getRGB() == this.color) {
            return;
        }
        this.color = color.getRGB();
        String hexString = Integer.toHexString(color.getRGB() & 0xFFFFFF);
        String s = "#" + "000000".substring(hexString.length()) + hexString;
        element.setAttribute("stroke", 0, s);
        element.setAttribute("fill", 0, s);
        initColor = true;
    }
}
