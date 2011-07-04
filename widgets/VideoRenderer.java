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
package org.finroc.tools.gui.widgets;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;
import javax.swing.event.MouseInputListener;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.tools.gui.util.gui.FileDialog;
import org.finroc.plugins.data_types.Blittable;
import org.finroc.plugins.data_types.HasBlittable;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;


public class VideoRenderer extends Widget {

    /** UID */
    private static final long serialVersionUID = -8452479527434504700L;

    public WidgetInput.Std<HasBlittable> videoInput;

    /** Image index in image source - in case we receive lists of blittables */
    public int imageIndexInSource;

    @Override
    protected WidgetUI createWidgetUI() {
        return new VideoWindowUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(HasBlittable.TYPE);
    }

    class VideoWindowUI extends WidgetUI implements PortListener<HasBlittable>, MouseInputListener {

        public VideoWindowUI() {
            super(RenderMode.Cached);
            videoInput.addChangeListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        /** UID */
        private static final long serialVersionUID = -922703839059777637L;

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            HasBlittable b = videoInput.getAutoLocked();
            if (b == null || imageIndexInSource >= b.getNumberOfBlittables()) {
                cache.fill(0);
                releaseAllLocks();
                return;
            }
            b.getBlittable(imageIndexInSource).blitTo(cache, new Rectangle(renderSize));
            releaseAllLocks();
        }

        @Override
        public void portChanged(AbstractPort origin, HasBlittable value) {
            this.setChanged();
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {}
        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        @Override
        public void mouseDragged(MouseEvent e) {}
        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON2) {
                HasBlittable b = videoInput.getAutoLocked();
                if (b == null || imageIndexInSource >= b.getNumberOfBlittables() || (b.getBlittable(imageIndexInSource).getHeight() <= 0 && b.getBlittable(imageIndexInSource).getWidth() <= 0)) {
                    releaseAllLocks();
                    return;
                }

                Blittable bl = b.getBlittable(imageIndexInSource);
                BufferedImageRGB image = new BufferedImageRGB(bl.getWidth(), bl.getHeight());
                bl.blitTo(image);
                File f = FileDialog.showSaveDialog("Save Image as...", "png");
                if (f != null) {
                    try {
                        ImageIO.write(image.getBufferedImage(), "png", f);
                    } catch (IOException e1) {
                        getRoot().getFingui().showErrorMessage(e1);
                    }
                }

                releaseAllLocks();
            }
        }

    }
}
