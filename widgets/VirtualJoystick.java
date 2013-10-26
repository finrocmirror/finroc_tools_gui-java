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
package org.finroc.tools.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;


import javax.naming.OperationNotSupportedException;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.commons.Util;
import org.finroc.tools.gui.commons.fastdraw.BufferedImageARGBColorable;
import org.finroc.tools.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.tools.gui.commons.fastdraw.InvisibleComponent;
import org.finroc.tools.gui.themes.Theme;

import org.finroc.core.port.PortCreationInfo;


/**
 * @author Max Reichardt
 *
 */
public class VirtualJoystick extends Picture {

    /** UID */
    private static final long serialVersionUID = 3806880236997654888L;

    private Color joystickBackground = getDefaultColor(Theme.DefaultColor.JOYSTICK_BACKGROUND);
    private Color foreground = getDefaultColor(Theme.DefaultColor.JOYSTICK_FOREGROUND);
    private boolean resetOnRelease = true;
    transient Point curPos = null;
    private transient int pointColor = 0xFF00;
    private boolean logarithmicScale = false;
    public double xLeft = -1;
    public double xRight = 1;
    public double yTop = 1;
    public double yBottom = -1;
    protected int zeroRadius = 0;

    private static BufferedImageARGBColorable circle = null;

    public WidgetOutput.Numeric x;
    public WidgetOutput.Numeric y;

    @Override
    protected WidgetUI createWidgetUI() {
        return new VirtualJoystickUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion;
    }

    class VirtualJoystickUI extends Picture.PictureUI implements MouseInputListener {

        /** UID */
        private static final long serialVersionUID = -6539674025052372200L;

        private Point center;

        VirtualJoystickUI() {
            setLayout(new BorderLayout());
            JComponent jp = new InvisibleComponent();
            this.add(jp, BorderLayout.CENTER);
            jp.addMouseMotionListener(this);
            jp.addMouseListener(this);
            setBackground(joystickBackground);
        }

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            Rectangle r = new Rectangle(renderSize);
            if (picture != null) {
                super.renderToCache(cache, renderSize, resized);
            } else {
                cache.drawFilledRectangle(r, joystickBackground.getRGB());
            }

            if (circle == null) {
                try {
                    circle = new BufferedImageARGBColorable(getClass().getResource("JoystickPoint.png"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            if (curPos == null || resized) {
                curPos = getCenter();
            }

            center = getCenter();
            cache.drawRectangle(r, foreground.getRGB());
            cache.drawHorizontalLine(0, center.y, renderSize.width, foreground.getRGB());
            cache.drawVerticalLine(center.x, 0, renderSize.height, foreground.getRGB());
            circle.blitToInColor(cache, new Point(curPos.x - 10, curPos.y - 10), circle.getBounds(), pointColor != 0 ? pointColor : 0xFF00);
        }

        Point getCenter() {
            return new Point(getRenderWidth() / 2, getRenderHeight() / 2);
        }

        public void mouseDragged(MouseEvent e) {
            setPos(e.getPoint());
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
        public void mousePressed(MouseEvent e) {
            setPos(e.getPoint());
        }

        public void mouseReleased(MouseEvent e) {
            if (resetOnRelease && e.getButton() == MouseEvent.BUTTON1) {
                setPos(null);
            }
        }

        public void mouseMoved(MouseEvent e) {}

        public void setPos(Point p) {

            double xCenter = (xLeft + xRight) / 2;
            double yCenter = (yTop + yBottom) / 2;
            double widgetCenterX = this.getCenter().x;
            double widgetCenterY = this.getCenter().y;

            if (p == null) {
                x.publish(xCenter);
                y.publish(yCenter);
                curPos = getCenter();
                pointColor = 0xFF00;
            } else {
                curPos.x = Util.toInterval(p.x, 10, getRenderWidth() - 11);
                curPos.y = Util.toInterval(p.y, 10, getRenderHeight() - 11);
                double xf = 0;
                double yf = 0;
                if (curPos.x > widgetCenterX + zeroRadius) {
                    xf = (Util.posInInterval(curPos.x, widgetCenterX + zeroRadius, getRenderWidth() - 11));
                } else if (curPos.x < widgetCenterX - zeroRadius) {
                    xf = (Util.posInInterval(curPos.x, 10, widgetCenterX - zeroRadius)) - 1;
                }
                if (curPos.y > widgetCenterY + zeroRadius) {
                    yf = (Util.posInInterval(curPos.y, widgetCenterY + zeroRadius, getRenderHeight() - 11));
                } else if (curPos.y < widgetCenterY - zeroRadius) {
                    yf = (Util.posInInterval(curPos.y, 10, widgetCenterY - zeroRadius)) - 1;
                }

                int colorFactor = (int)(Math.pow(Math.max(Math.abs(xf), Math.abs(yf)), 3) * 255);
                if (logarithmicScale) {
                    //xf = Math.signum(xf) * Math.pow(10, Math.abs(xf)) / 10;
                    //yf = Math.signum(yf) * Math.pow(10, Math.abs(yf)) / 10;
                    xf = Math.pow(xf, 3);
                    yf = Math.pow(yf, 3);
                }
                xf = (xf + 1) / 2;
                yf = (yf + 1) / 2;
                //x.setValue(new NumberWithDefault(xf * xRight + (1-xf) * xLeft, xCenter));
                //y.setValue(new NumberWithDefault(yf * yBottom + (1-yf) * yTop, yCenter));
                x.publish(xf * xRight + (1 - xf) * xLeft);
                y.publish(yf * yBottom + (1 - yf) * yTop);
                pointColor = (255 - colorFactor) << 8 | colorFactor << 16;
            }
            setChanged();
            repaint();
        }
    }
}
