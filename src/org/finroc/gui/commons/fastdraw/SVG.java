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
package org.finroc.gui.commons.fastdraw;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.finroc.gui.FinrocGUI;
import org.finroc.gui.commons.Util;
import org.finroc.log.LogLevel;
import org.finroc.plugin.datatype.Paintable;

import com.kitfox.svg.SVGCache;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElement;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGPanel;

/**
 * @author max
 *
 * Wraps SVG diagram
 */
public class SVG implements Paintable {

    /** UID */
    private static final long serialVersionUID = 8883013998203770170L;

    /** unique ID for element */
    private String id;
    private static Integer curId = 0;

    /** reference to SVGDiagram */
    private SVGDiagram diagram;

    /** for painting the SVG to graphics */
    private SVGPanel painter;

    /** URI of SVG */
    private URI uri;

    /** last stroke width set */
    private double strokeWidth = -1;

    /** list with all elements */
    private List<SVGElement> elements;

    /** change line width with zooming? */
    private boolean zoomSensitive = true;

    /** bounding box */
    private Rectangle2D bounds;

    /** Cache for faster loading from web */
    private static Map<URL, byte[]> urlCache = new HashMap<URL, byte[]>();


    public static SVG createInstance(URL svgUrl, boolean cache) throws Exception {
        if (!cache) {
            return new SVG(svgUrl.openStream());
        }

        byte[] temp = urlCache.get(svgUrl);
        if (temp == null) {
            FinrocGUI.logDomain.log(LogLevel.LL_DEBUG, "SVG", "Loading SVG " + svgUrl);
            temp = Util.readStreamFully(svgUrl.openStream());
            urlCache.put(svgUrl, temp);
            FinrocGUI.logDomain.log(LogLevel.LL_DEBUG_VERBOSE_1, "SVG", "SVG " + svgUrl + " loaded");
        }
        return new SVG(new ByteArrayInputStream(temp));
    }

    public SVG(URL svgUrl) throws Exception {
        this(svgUrl.openStream());
    }

    public SVG(InputStream is) throws Exception {
        synchronized (curId) {
            id = curId.toString();
            curId++;
        }
        SVGUniverse universe = SVGCache.getSVGUniverse();
        uri = universe.loadSVG(new BufferedReader(new InputStreamReader(is)), "FastDraw-" + id);
        diagram = universe.getDiagram(uri);
    }

    public SVGElement getElement(String elementName) {
        initElements();
        for (SVGElement ele : elements) {
            String eleId = ele.getId();
            if (eleId != null && eleId.equals(elementName)) {
                return ele;
            }
        }
        return null;
    }

    private void initElements() {
        if (elements != null) {
            return;
        }
        elements = new ArrayList<SVGElement>();
        initElementsHelper(diagram.getRoot());
    }

    private void initElementsHelper(SVGElement curNode) {
        for (Object obj : curNode.getChildren(null)) {
            SVGElement ele = (SVGElement)obj;
            elements.add(ele);
            initElementsHelper(ele);
        }
    }

    public List<SVGElement> getAllElements() {
        initElements();
        return elements;
    }

    public SVGDiagram getDiagram() {
        return diagram;
    }

    public void paint(Graphics2D g) {
        if (painter == null) {
            painter = new SVGPanel();
            painter.setSvgURI(uri);
        }
        boolean antialias = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING) == RenderingHints.VALUE_ANTIALIAS_ON;
        painter.setAntiAlias(antialias);
        if (zoomSensitive) {
            double m00 = g.getTransform().getScaleX();
            double m01 = g.getTransform().getShearX();
            double zoom = Math.sqrt(m00 * m00 + m01 * m01);
            double ratio = zoom * strokeWidth;
            if (ratio > 1.1 || ratio < 0.9) {  // only react to major changes and not to rounding errors
                setStrokeWidth(1 / zoom);
            }
        }

        painter.paintComponent(g);
    }

    public void setStrokeWidth(double newWidth) {
        if (newWidth == strokeWidth) {
            return;
        }

        initElements();
        for (SVGElement ele : elements) {
            try {
                ele.setAttribute("stroke-width", 0, newWidth + "px");
            } catch (SVGElementException e) {
                //e.printStackTrace();
            }
        }

        strokeWidth = newWidth;
    }

    public Rectangle2D getBounds() {
        //return diagram.getViewRect();
        if (bounds != null) {
            return bounds;
        }
        try {
            bounds = diagram.getRoot().getBoundingBox();
        } catch (SVGException e) {
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), e);
        }
        return bounds;
    }
}
