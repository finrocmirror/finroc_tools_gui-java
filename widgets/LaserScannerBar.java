//
// You received this file as part of Finroc
// A Framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui.widgets;

import java.awt.Color;
import java.awt.Dimension;

import javax.naming.OperationNotSupportedException;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.commons.fastdraw.BufferedImageRGB;

import org.finroc.plugins.data_types.Function;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;


public class LaserScannerBar extends Widget {

    /** UID */
    private static final long serialVersionUID = -4297918119999527909L;

    public Color minColor = Color.WHITE;
    public Color maxColor = Color.BLACK;
    public double minValue = 0;
    public double maxValue = 2000;
    public boolean mirrored = false;

    public WidgetInput.Std<Function> inputFunction;

    @Override
    protected WidgetUI createWidgetUI() {
        return new LaserScannerBarUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(Function.TYPE);
    }

    class LaserScannerBarUI extends WidgetUI implements PortListener<Function> {

        /** UID */
        private static final long serialVersionUID = 3384400171418823343L;

        public LaserScannerBarUI() {
            super(RenderMode.Cached);
            inputFunction.addChangeListener(this);
        }

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            Function f = inputFunction.getAutoLocked();

            // empty function => black
            if (f == null || Double.isNaN(f.getMinX())) {
                cache.fill(0);
                releaseAllLocks();
                return;
            }

            for (int x = 0; x < renderSize.width; x++) {
                double xRel = ((double)x) / ((double)renderSize.width);
                double xAbs = f.getMinX() + xRel * (f.getMaxX() - f.getMinX());
                double y = f.getY(xAbs);
                float alpha = (float)((y - minValue) / (maxValue - minValue));
                alpha = Math.max(0, Math.min(1, alpha));
                int color = BufferedImageRGB.mergeColors(minColor.getRGB(), maxColor.getRGB(), alpha);
                if (!mirrored) {
                    cache.drawVerticalLine(x, 0, renderSize.height, color);
                } else {
                    cache.drawVerticalLine(renderSize.width - 1 - x, 0, renderSize.height, color);
                }
            }
            releaseAllLocks();
        }

        @Override
        public void portChanged(AbstractPort origin, Function value) {
            this.setChanged();
            repaint();
        }
    }
}
