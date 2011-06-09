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
import java.awt.Point;
import java.awt.Rectangle;

import javax.naming.OperationNotSupportedException;

import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.plugin.datatype.HasBlittable;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;

public class VideoJoystick extends VirtualJoystick {

    /** UID */
    private static final long serialVersionUID = 960660239432850435L;

    private WidgetInput.Std<HasBlittable> video;

    /** Image index in image source - in case we receive lists of blittables */
    public int imageIndexInSource;

    public VideoJoystick() {
        zeroRadius = 10;
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new VideoJoystickUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (forPort == video) {
            return suggestion.derive(HasBlittable.TYPE);
        }
        return super.getPortCreationInfo(suggestion, forPort);
    }

    class VideoJoystickUI extends VirtualJoystickUI implements PortListener<HasBlittable> {

        /** UID */
        private static final long serialVersionUID = -4884477845301064039L;

        VideoJoystickUI() {
            super();
            video.addChangeListener(this);
        }

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            HasBlittable b = video.getAutoLocked();
            if (b == null || imageIndexInSource >= b.getNumberOfBlittables()) {
                cache.fill(0);
            } else {
                b.getBlittable(imageIndexInSource).blitTo(cache, new Rectangle(renderSize));
            }

            // initial position
            if (curPos == null) {
                curPos = getCenter();
            }

            // draw line
            Point center = getCenter();
            if (!center.equals(curPos)) {
                cache.drawLine(center, curPos, Color.white);
            }
            releaseAllLocks();
        }

        @Override
        public void portChanged(AbstractPort origin, HasBlittable value) {
            setChanged();
            repaint();
        }
    }

}
