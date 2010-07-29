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
package org.finroc.gui.widgets;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.naming.OperationNotSupportedException;

import org.finroc.gui.FinrocGUI;
import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.gui.commons.fastdraw.SVG;
import org.finroc.log.LogLevel;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortListener;

/**
 * @author max
 *
 */
public class Compass extends Widget {

    /** UID */
    private static final long serialVersionUID = -2354672452749745370L;

    /** input */
    public WidgetInput.Numeric yaw;

    public double inputScaleFactor = 1;

    public boolean turnNeedle = false;

    private static SVG svgBack, svgDynamic, svgNeedle;
    private static int xOffset, yOffset, svgWidth, svgHeight; // offset of images etc.
    private static double svgSize, halfSVGSize;


    @Override
    protected WidgetUI createWidgetUI() {
        return new CompassUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion;
    }

    public static synchronized void initSVG() {
        if (svgNeedle != null) {
            return;
        }
        try {
            svgBack = SVG.createInstance(LCD.class.getResource("CompassBack.svg"), true);
            svgDynamic = SVG.createInstance(LCD.class.getResource("CompassDynamic.svg"), true);
            svgNeedle = SVG.createInstance(LCD.class.getResource("CompassNeedle.svg"), true);
            xOffset = (int)svgBack.getBounds().getMinX() - 2;
            yOffset = (int)svgBack.getBounds().getMinY() - 2;
            svgWidth = (int)svgBack.getBounds().getWidth() + 4;
            svgHeight = (int)svgBack.getBounds().getHeight() + 4;
            svgSize = Math.min(svgWidth, svgHeight);
            halfSVGSize = svgSize / 2;
        } catch (Exception e) {
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, "Compass", e);
        }
    }

    private class CompassUI extends WidgetUI implements CCPortListener<CoreNumber> {

        /** UID */
        private static final long serialVersionUID = -5577967894215832L;

        private BufferedImageRGB backgroundBuffer;
        private Dimension curBackgroundBufferSize = new Dimension(0, 0);
        private double factor;
        private AffineTransform stdTransform;
        private boolean propChange = false;

        private CompassUI() {
            super(RenderMode.Cached);
            initSVG();
            yaw.addChangeListener(this);
        }

        @Override
        public void portChanged(CCPortBase origin, CoreNumber value) {
            setChanged();
            repaint();
        }

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            if (propChange || backgroundBuffer == null || (!renderSize.equals(curBackgroundBufferSize))) {

                // recalculate background buffer
                if (backgroundBuffer == null) {
                    backgroundBuffer = new BufferedImageRGB(new Dimension(renderSize.width + 100, renderSize.height + 100));
                } else if (backgroundBuffer.getWidth() < renderSize.width || backgroundBuffer.getHeight() < renderSize.height) {
                    backgroundBuffer = new BufferedImageRGB(new Dimension(Math.max(renderSize.width, backgroundBuffer.getWidth()) + 100, Math.max(renderSize.height, backgroundBuffer.getHeight()) + 100));
                }

                curBackgroundBufferSize.setSize(renderSize);
                propChange = false;
                backgroundBuffer.fill(getBackground().getRGB());
                double size = Math.min(renderSize.width, renderSize.height);
                factor = size / svgSize;

                Graphics2D g = backgroundBuffer.getGraphics();
                stdTransform = g.getTransform();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setClip(0, 0, renderSize.width, renderSize.height);

                g.scale(factor, factor);
                g.translate(-xOffset, -yOffset);
                svgBack.paint(g);
                if (turnNeedle) {
                    svgDynamic.paint(g);
                }
                g.setTransform(stdTransform);
            }

            // draw background
            backgroundBuffer.blitTo(cache);

            // draw turnable scale
            Graphics2D g = cache.getGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            if (!turnNeedle) {
                g.setClip(0, 0, renderSize.width, renderSize.height);
                g.scale(factor, factor);
                g.translate(halfSVGSize, halfSVGSize);
                g.rotate(((yaw.getDouble() * inputScaleFactor) / 180.0) * Math.PI);
                g.translate(-(xOffset + halfSVGSize), -(yOffset + halfSVGSize));
                svgDynamic.paint(g);
                g.setTransform(stdTransform);
            }

            // draw needle
            double turn = turnNeedle ? -yaw.getDouble() * inputScaleFactor : 0;
            g.setClip(0, 0, renderSize.width, renderSize.height);
            g.scale(factor, factor);
            g.translate(halfSVGSize, halfSVGSize);
            g.rotate(((turn - 28.0) / 180.0) * Math.PI);
            g.translate(-(xOffset + halfSVGSize), -(yOffset + halfSVGSize));
            svgNeedle.paint(g);
            g.setTransform(stdTransform);
        }

        @Override
        public void widgetPropertiesChanged() {
            propChange = true;
        }
    }
}
