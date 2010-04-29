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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.naming.OperationNotSupportedException;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.gui.themes.Themes;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortListener;


/**
 * @author max
 *
 */
public class ArtificialHorizon extends Widget {

    /** UID */
    private static final long serialVersionUID = 1357256003356L;

    /** Button output ports */
    public WidgetInput.Numeric inclineX;
    public WidgetInput.Numeric inclineY;

    /** Button parameters */
    //public int degreeScale = 12;
    //public double scalingFactor = 1;
    public int warningDelimiter = 30;
    public int inputValueFactorX = 1;
    public int inputValueFactorY = 1;

    private Color background = Themes.getCurTheme().panelBackground();

    @Override
    protected WidgetUI createWidgetUI() {
        return new ArtificialHorizonUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return null;
    }

    /** Sky Color */
    private Color sky = new Color(0.4f, 0.9f, 1.0f);

    /** Ground color for small angles */
    private Color farGround = new Color(0.2f, 1.0f, 0.2f);

    /** Ground color for great angles */
    private Color nearGround = new Color(0.9f, 0.5f, 0.0f);

    /** Constants for calculation */
    private static int SHIFT = 16, SHIFT_MULT = 1 << SHIFT, WHITE = 0xFFFFFF, BLOCK_RED = 0xAA0000;

    class ArtificialHorizonUI extends WidgetUI implements CCPortListener<CoreNumber> {

        /** UID */
        private static final long serialVersionUID = -9876567882234222L;

        /** Calculated after every resize: Indentations for circle: number of pixels for each row that can be skipped */
        private int[] circleIndents = new int[0];

        /** current circleIndents was calculated for this size */
        private int indentsForSize = 0;

        /** Color table for angles (+1000) */
        private final int[] colorTable = new int[2000];

        ArtificialHorizonUI() {
            super(RenderMode.Cached);
            inclineX.addChangeListener(this);
            inclineY.addChangeListener(this);
            widgetPropertiesChanged();
        }

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {

            // draw background
            Rectangle r = new Rectangle(renderSize);
            cache.drawFilledRectangle(r, background.getRGB());

            final int size = Math.min(renderSize.height, renderSize.width);
            if (indentsForSize != size) {
                calculateCircleIndents(cache, size);
                indentsForSize = size;
            }

            // calculation values
            final int mx = size / 2;
            final int my = size / 2;
            final double incX = -inclineX.getDouble() * inputValueFactorX;
            final double incY = inclineY.getDouble() * inputValueFactorY;
            final double yDegrees = 45;
            final double radX = (incX / 180) * Math.PI;
            final double yAngleIncrement = Math.cos(radX) * (yDegrees / (double)my);
            final double xAngleIncrement = Math.sin(radX) * (yDegrees / (double)my);
            final double yDegreesStartMiddle = -incY + yDegrees * Math.cos(radX);
            final double startVal = yDegreesStartMiddle - (mx * xAngleIncrement);

            // as fixed-precision shifted integers
            int curLineStart = (int)(startVal * SHIFT_MULT);
            final int yIncrement = (int)(yAngleIncrement * SHIFT_MULT);
            final int xIncrement = (int)(xAngleIncrement * SHIFT_MULT);

            // draw horizon
            int current = 0;
            for (int y = 0; y < size; y++) {
                current = curLineStart + circleIndents[y] * xIncrement;
                for (int x = circleIndents[y], n = size - circleIndents[y]; x < n; x++) {
                    cache.setPixel(x, y, colorTable[(current >> SHIFT) + 1000]);
                    current += xIncrement;
                }
                curLineStart -= yIncrement;
            }

            // constants
            final int s10 = size / 10;
            final int s20 = size / 20;
            final int s40 = size / 40;
            final int s60 = size / 60;

            // draw red blocks
            cache.drawFilledRectangle(new Rectangle(mx - 2, my - 2, 5, 5), BLOCK_RED);
            cache.drawFilledRectangle(new Rectangle(mx - 9 * s40, my - 2, 7 * s40, 5), BLOCK_RED);
            cache.drawFilledRectangle(new Rectangle(mx + 2 * s40, my - 2, 7 * s40, 5), BLOCK_RED);

            // draw edge scale
            cache.drawHorizontalLine(0, my, s10, WHITE);
            cache.drawHorizontalLine(size - s10, my, s10, WHITE);
            Graphics2D gfx = cache.getGraphics();
            gfx.setTransform(new AffineTransform()); // reset any transformations
            gfx.translate(mx, my);
            gfx.setColor(Color.WHITE);
            for (int i = 5; i <= 20; i += 5) {
                double angle = (90.0 + i);
                double angRad = (angle / 180) * Math.PI;
                double len = (i % 20 == 0) ? 3 : (i % 10 == 0) ? 2 : 1;
                len *= s60;
                int x1 = (int)(Math.cos(angRad) * (double)my);
                int y1 = (int)(Math.sin(angRad) * (double)my);
                int x2 = (int)(Math.cos(angRad) * (my - len));
                int y2 = (int)(Math.sin(angRad) * (my - len));
                gfx.drawLine(x1, y1, x2, y2);
                gfx.drawLine(-x1, y1, -x2, y2);
                gfx.drawLine(x1, -y1, x2, -y2);
                gfx.drawLine(-x1, -y1, -x2, -y2);
            }

            // draw turning scale
            gfx.rotate(radX);
            final double pixelPerAngle = ((double)my / yDegrees);
            gfx.drawLine(-5 * s20, 0, 5 * s20, 0);
            for (int i = 5; i < 21.0; i += 5) {
                int mYDistance = (int)(((double)i) * pixelPerAngle);
                int len = (i % 20 == 0) ? 3 * s20 : (i % 10 == 0) ? 2 * s20 : s20;
                gfx.drawLine(-len, mYDistance, len, mYDistance);
                gfx.drawLine(-len, -mYDistance, len, -mYDistance);
                if (i % 10 == 0) {
                    gfx.drawString("" + i, len + 3, -mYDistance + 5);
                    gfx.drawString("-" + i, len + 3, mYDistance + 5);
                }
            }
            gfx.setColor(Color.LIGHT_GRAY);
            gfx.drawLine(0, -my, 0, my);
            gfx.rotate(-radX);

        }

        private void calculateCircleIndents(BufferedImageRGB cache, int renderSize) {
            double angleStep = 90.0 / (renderSize * 50);
            final int mx = renderSize / 2;
            final int my = renderSize / 2;
            final double dmx = mx;
            final double dmy = my;
            int altColor = background.getRGB() != 0 ? 0 : 0xffffff;
            for (double d = 0; d < 360; d += angleStep) {
                int x = (int)(dmx + (Math.cos(d) * dmx));
                int y = (int)(dmy + (Math.sin(d) * dmy));
                cache.setPixel(x, y, altColor);
            }
            circleIndents = new int[renderSize];
            for (int y = 0; y < renderSize; y++) {
                int x = 0;
                circleIndents[y] = mx;
                for (; x < mx; x++) {
                    if (cache.getPixel(x, y) == altColor) {
                        circleIndents[y] = x;
                        break;
                    }
                }
            }
        }

        @Override
        public void widgetPropertiesChanged() {

            // Calculate color table
            colorTable[1000] = sky.getRGB();
            colorTable[1180] = farGround.getRGB();
            colorTable[1270] = nearGround.getRGB();
            for (int i = 1; i < 180; i++) {
                colorTable[i + 1000] = sky.getRGB();
            }
            float[] br = new float[3];
            float[] gr = new float[3];
            for (int i = 1; i < 90; i++) {
                float brown = (float)i / 90.0f;
                float green = 1.0f - brown;
                nearGround.getColorComponents(br);
                farGround.getColorComponents(gr);
                for (int j = 0; j < 3; j++) {
                    br[j] = brown * br[j] + green * gr[j];
                }
                colorTable[i + 1180] = new Color(br[0], br[1], br[2]).getRGB();
                colorTable[1360 - i] = new Color(br[0], br[1], br[2]).getRGB();
            }

            for (int i = 999; i >= 0; i--) {
                colorTable[i] = colorTable[i + 360];
            }
            for (int i = 1360; i < 2000; i++) {
                colorTable[i] = colorTable[i - 360];
            }

            super.setChanged();
            repaint();
        }

        @Override
        public void portChanged(CCPortBase origin, CoreNumber value) {
            super.setChanged();
            repaint();
        }
    }
}
