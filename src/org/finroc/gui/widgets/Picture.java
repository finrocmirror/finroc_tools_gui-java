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
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.naming.OperationNotSupportedException;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.gui.util.embeddedfiles.EmbeddedPaintable;

import org.finroc.core.port.PortCreationInfo;


public class Picture extends Widget {

    /** UID */
    private static final long serialVersionUID = -6272458243456235119L;

    private EmbeddedPaintable picture;

    private boolean scaleToFit;

    private boolean preserveAspectRatio;

    @Override
    protected WidgetUI createWidgetUI() {
        return new PictureUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return null;
    }

    class PictureUI extends WidgetUI {

        public PictureUI() {
            super(RenderMode.Cached);
            widgetPropertiesChanged();
        }

        /** UID */
        private static final long serialVersionUID = -1151555070135103957L;

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            cache.fill(getBackground().getRGB());
            if (picture == null) {
                return;
            }

            Graphics2D g2d = cache.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setClip(0, 0, renderSize.width, renderSize.height);

            picture.paintToTopLeft(g2d, scaleToFit ? new Rectangle(renderSize) : null, preserveAspectRatio, getRoot().getEmbeddedFileManager());
        }

        @Override
        public void widgetPropertiesChanged() {  // Picture changed
            /*try {
                paintable = picture.getPaintable();
            } catch (Exception e) {
                //e.printStackTrace();
            }*/
            setChanged();
        }


    }
}
