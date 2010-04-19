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
import java.awt.Rectangle;


import javax.naming.OperationNotSupportedException;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetPorts;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.commons.fastdraw.Blittable;
import org.finroc.gui.commons.fastdraw.BufferedImageRGB;

import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.std.PortBase;
import org.finroc.core.port.std.PortListener;


public class VideoRenderer extends Widget {

    /** UID */
    private static final long serialVersionUID = -8452479527434504700L;

    public WidgetInput.Std<Blittable> videoInput;

    @Override
    protected WidgetUI createWidgetUI() {
        return new VideoWindowUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort, WidgetPorts<?> collection) {
        return suggestion.derive(Blittable.TYPE);
    }

    class VideoWindowUI extends WidgetUI implements PortListener<Blittable> {

        public VideoWindowUI() {
            super(RenderMode.Cached);
            videoInput.addChangeListener(this);
        }

        /** UID */
        private static final long serialVersionUID = -922703839059777637L;

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            Blittable b = videoInput.getAutoLocked();
            if (b == null) {
                cache.fill(0);
                releaseAllLocks();
                return;
            }
            b.blitTo(cache, new Rectangle(renderSize));
            releaseAllLocks();
        }

        @Override
        public void portChanged(PortBase origin, Blittable value) {
            this.setChanged();
            repaint();
        }
    }
}
