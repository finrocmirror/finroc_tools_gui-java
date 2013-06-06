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
package org.finroc.tools.gui.util.embeddedfiles;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;

import javax.imageio.ImageIO;

import org.finroc.tools.gui.FinrocGUI;
import org.finroc.tools.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.tools.gui.commons.fastdraw.SVG;
import org.rrlib.finroc_core_utils.log.LogLevel;
import org.finroc.plugins.data_types.Paintable;


/**
 * @author Max Reichardt
 *
 */
public class EmbeddedPaintable extends EmbeddedFile {

    /** UID */
    private static final long serialVersionUID = -8756656252190819830L;

    /** instance of paintable, to paint it directly if requested */
    private transient Paintable paintInstance = null;

    /** Name of screen */
    private String name;

    /** size & position offset of paintable object */
    private double centerX, centerY, scale;

    /** size of original */
    private transient Rectangle originalBounds;

    public static final String[] SUPPORTED_EXTENSIONS = new String[] {"svg", "png", "jpg", "gif"};

    protected EmbeddedPaintable(File file) {
        super(file);
        name = file.getName();
    }

    @Override
    void init(FileManager efm) {
        if (originalBounds == null || paintInstance == null) {

            // init paint Instance
            if (paintInstance == null) {
                try {
                    paintInstance = getPaintable(efm);
                } catch (Exception e) {
                    FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), e);
                    return;
                }
            }

            if (paintInstance instanceof BufferedImageRGB) {
                originalBounds = ((BufferedImageRGB)paintInstance).getBounds();
            } else if (paintInstance instanceof SVG) {
                Rectangle2D temp = ((SVG)paintInstance).getBounds();
                originalBounds = new Rectangle((int)temp.getX() - 1, (int)temp.getY() - 1, (int)temp.getWidth() + 2, (int)temp.getHeight() + 2);
            }
        }

        // init params (only once)
        if (scale == 0) {
            scale = 1;
            if (paintInstance instanceof BufferedImageRGB) {
                centerX = ((BufferedImageRGB)paintInstance).getWidth() / 2;
                centerY = ((BufferedImageRGB)paintInstance).getHeight() / 2;
            }
        }
    }

    public void paintToCenter(Graphics2D g, FileManager efm) {
        init(efm);
        if (paintInstance == null) {
            return;
        }

        // Wie stark wird skaliert?
        g.scale(scale, scale);

        // Wo wird der Mittelpunkt hinbewegt?
        g.translate(-centerX, -centerY);

        paintInstance.paint(g);
    }

    public void paintToTopLeft(Graphics2D g, Rectangle fitTo, boolean preserveAspectRatio, FileManager efm) {
        init(efm);
        if (paintInstance == null) {
            return;
        }
        //Rectangle clipBounds = g.getClipBounds();
        //g.clipRect(clipBounds.x + originalBounds.x, clipBounds.y + originalBounds.y, clipBounds.width, clipBounds.height);
        AffineTransform at = g.getTransform();
        if (fitTo != null) {
            double factorX = ((double)fitTo.width) / ((double)originalBounds.width);
            double factorY = ((double)fitTo.height) / ((double)originalBounds.height);
            double factor = Math.min(factorX, factorY);
            //System.out.println(factorX + " " + factorY + " " + factor);
            if (preserveAspectRatio) {
                g.scale(factor, factor);
            } else {
                g.scale(factorX, factorY);
            }
        }
        g.translate(-originalBounds.x, -originalBounds.y);
        paintInstance.paint(g);
        g.setTransform(at);
    }

    public Paintable getPaintable(FileManager efm) throws Exception {
        String name = getFileName().toLowerCase();
        if (name.endsWith(".svg")) {
            return new SVG(getInputStream(efm));
        } else if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".gif")) {
            return new BufferedImageRGB(ImageIO.read(getInputStream(efm)), false);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void paint(Graphics2D g, FileManager efm) {
        paintToTopLeft(g, null, true, efm);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        EmbeddedPaintable ep = (EmbeddedPaintable)o;
        return (name.equals(ep.name) && centerX == ep.centerX && centerY == ep.centerY && scale == ep.scale);
    }

    public String toString() {
        return name;
    }
}
