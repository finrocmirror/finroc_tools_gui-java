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
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.naming.OperationNotSupportedException;
import javax.swing.JPanel;

import org.finroc.tools.gui.FinrocGUI;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author max
 *
 * This is the superclass for components that will be visible on FastCustomDrawingPanels
 */
public abstract class FastCustomDrawableComponent extends JPanel {

    /** UID */
    private static final long serialVersionUID = 8289849736884579514L;


    public enum RenderMode { Swing, Cached }
    private RenderMode renderMode;
    private BufferedImageRGB cache = new BufferedImageRGB(1, 1);
    //private static BufferedImageRGB renderModeTest = new BufferedImageRGB(10, 10);

    // changed ?
    private boolean changed;

    /** resize flag (for optimization) */
    public static boolean resizing;

    protected FastCustomDrawableComponent(RenderMode renderMode) {
        this.renderMode = renderMode;
    }

    /**
     * (re-)initializes cache (e.g. when resizing)
     */
    private void initCache() {

        if (renderMode == RenderMode.Cached) {
            Dimension size = getRenderSize();

            if (!resizing) {
                cache = new BufferedImageRGB(size);
                //System.out.println("reallocating: " + (cache.getWidth() * cache.getHeight() * 4) + " bytes");
            } else if (cache.getWidth() < size.width || cache.getHeight() < size.height) {
                cache = new BufferedImageRGB(size.width + 100, size.height + 100);
                //System.out.println("reallocating: " + (cache.getWidth() * cache.getHeight() * 4) + " bytes");
            }
            changed = true;
        }
    }

    protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    @Override
    protected void paintComponent(Graphics g) {

        if (renderMode == RenderMode.Cached) {

            Dimension size = getRenderSize();
            Insets i = getInsets();

            if (size.width >= 1 && size.height >= 1) {

                // reinit cache?
                boolean resized = false;
                if ((cache == null) || !(size.equals(cache.getSize()))) {
                    initCache();
                    resized = true;
                }

                // update cache
                if (hasChanged()) {
                    if (renderMode == RenderMode.Cached) {
                        try {
                            renderToCache(cache, size, resized);
                            changed = false;
                        } catch (OperationNotSupportedException e) {
                            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), e);
                        }
                    }
                }

                // blit
                if (!resizing) {
                    g.drawImage(cache.getBufferedImage(), i.left, i.top, null);
                } else {
                    g.drawImage(cache.getBufferedImage().getSubimage(0, 0, size.width, size.height), i.left, i.top, null);
                }
            }

            // draw border background?
            if (getBorder() != null && !getBorder().isBorderOpaque()) {
                Color c = g.getColor();
                g.setColor(getBackground());
                g.fillRect(0, 0, i.left, getHeight());
                g.fillRect(getWidth() - i.right, 0, i.right, getHeight());
                g.fillRect(i.left, 0, getWidth() - i.left - i.right, i.top);
                g.fillRect(i.left, getHeight() - i.bottom, getWidth() - i.left - i.right, i.bottom);
                g.setColor(c);
            }
        } else {
            super.paintComponent(g);
        }
        //FPSDetector.tick();
    }

    /** getSize() minus borders */
    public Dimension getRenderSize() {
        Insets i = getInsets();
        Dimension size = getSize();
        return new Dimension(size.width - i.left - i.right, size.height - i.top - i.bottom);
    }

    public int getRenderWidth() {
        Insets i = getInsets();
        return getWidth() - i.left - i.right;
    }

    public int getRenderHeight() {
        Insets i = getInsets();
        return getHeight() - i.top - i.bottom;
    }

    public int getRenderX() {
        return getInsets().left;
    }

    public int getRenderY() {
        return getInsets().top;
    }

    public Rectangle getRenderBounds() {
        Insets i = getInsets();
        return new Rectangle(i.left, i.top, getWidth() - i.left - i.right, getHeight() - i.top - i.bottom);
    }

    public boolean hasChanged() {
        return changed;
    }

    public void setChanged() {
        changed = true;
    }

    protected BufferedImageRGB getCache() {
        return cache;
    }
}
