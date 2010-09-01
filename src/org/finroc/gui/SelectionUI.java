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
package org.finroc.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.event.MouseInputListener;

import org.finroc.gui.abstractbase.DataModelBase;
import org.finroc.gui.abstractbase.DataModelListener;
import org.finroc.gui.commons.fastdraw.FastCustomDrawableComponent;


public class SelectionUI implements MouseInputListener, DataModelListener {

    /** Size of 'resize rectangles' */
    private final static int RESIZE_RECT_SIZE = 7;

    /** Minimum size for selection during resizing */
    private final static int MINIMUM_SIZE = 10;

    /** Parent */
    private GUIPanelUI parent;

    /** selected elements */
    private List<SelectedElement> elements = new ArrayList<SelectedElement>();

    /** bounds of selection */
    private Rectangle oldBounds, bounds;

    /** small rectangles for resizing */
    private Map<Integer, Rectangle> resizeRectangles = new HashMap<Integer, Rectangle>();

    /** Current mode of cursor (Cursor.x-constant) */
    private int mode = -1;

    /** for movement */
    private Point firstPos = null;

    /** Did something change during action? */
    private boolean somethingChanged = false;

    public SelectionUI(GUIPanelUI parent) {
        parent.addMouseListener(this);
        parent.addMouseMotionListener(this);
        parent.getModel().addDataModelListener(this);
        this.parent = parent;
        addRect(Cursor.NE_RESIZE_CURSOR);
        addRect(Cursor.N_RESIZE_CURSOR);
        addRect(Cursor.NW_RESIZE_CURSOR);
        addRect(Cursor.W_RESIZE_CURSOR);
        addRect(Cursor.E_RESIZE_CURSOR);
        addRect(Cursor.SW_RESIZE_CURSOR);
        addRect(Cursor.S_RESIZE_CURSOR);
        addRect(Cursor.SE_RESIZE_CURSOR);
    }

    private void addRect(int cursorType) {
        resizeRectangles.put(cursorType, new Rectangle(0, 0, RESIZE_RECT_SIZE, RESIZE_RECT_SIZE));
    }

    private class SelectedElement {
        Widget widget;
        Rectangle oldBounds;
        SelectedElement(Widget c) {
            widget = c;
            oldBounds = c.getBounds();
        }
    }

    private void updateBounds() {
        if (elements.size() == 0) {
            setSelection(null);
            return;
        }
        bounds = computeBounds();
        oldBounds = new Rectangle(bounds);
        for (SelectedElement elem : elements) {
            elem.oldBounds = elem.widget.getBounds();
        }
    }

    private Rectangle computeBounds() {
        Rectangle r = elements.get(0).widget.getBounds();
        for (SelectedElement e : elements) {
            r = r.union(e.widget.getBounds());
        }
        return r;
    }

    public boolean isEmpty() {
        return elements.size() == 0;
    }

    public void drawSelection(Graphics g) {
        if (!isEmpty()) {

            g.setColor(Color.WHITE);

            // draw box
            g.drawRect(bounds.x - 1, bounds.y - 1, bounds.width + 1, bounds.height + 1);

            // draw small rectangles for resizing
            computeResizeRectangles();
            for (Rectangle r : resizeRectangles.values()) {
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        }
    }

    private void computeResizeRectangles() {
        int xmin = bounds.x - RESIZE_RECT_SIZE;
        int xmax = bounds.x + bounds.width;
        int xmiddle = (xmin + xmax) / 2;
        int ymin = bounds.y - RESIZE_RECT_SIZE;
        int ymax = bounds.y + bounds.height;
        int ymiddle = (ymin + ymax) / 2;

        Rectangle r = resizeRectangles.get(Cursor.NW_RESIZE_CURSOR);
        r.x = xmin;
        r.y = ymin;
        r = resizeRectangles.get(Cursor.N_RESIZE_CURSOR);
        r.x = xmiddle;
        r.y = ymin;
        r = resizeRectangles.get(Cursor.NE_RESIZE_CURSOR);
        r.x = xmax;
        r.y = ymin;
        r = resizeRectangles.get(Cursor.W_RESIZE_CURSOR);
        r.x = xmin;
        r.y = ymiddle;
        r = resizeRectangles.get(Cursor.E_RESIZE_CURSOR);
        r.x = xmax;
        r.y = ymiddle;
        r = resizeRectangles.get(Cursor.SW_RESIZE_CURSOR);
        r.x = xmin;
        r.y = ymax;
        r = resizeRectangles.get(Cursor.S_RESIZE_CURSOR);
        r.x = xmiddle;
        r.y = ymax;
        r = resizeRectangles.get(Cursor.SE_RESIZE_CURSOR);
        r.x = xmax;
        r.y = ymax;
    }


    /**
     * @return Bounds enhanced by RESIZE_RECT_SIZE pixels for correct redrawing
     */
    public Rectangle getBounds() {
        if (bounds == null) {
            return null;
        }
        return new Rectangle(bounds.x - RESIZE_RECT_SIZE, bounds.y - RESIZE_RECT_SIZE, bounds.width + 2* RESIZE_RECT_SIZE, bounds.height + 2* RESIZE_RECT_SIZE);
    }

    /**
     * Is this as position where cursor should change etc.?
     *
     * @param point Point
     * @return answer
     */
    public boolean criticalPosition(Point point) {
        if (isEmpty()) {
            return false;
        }
        for (Rectangle r : resizeRectangles.values()) {
            if (r.getBounds().contains(point)) {
                return true;
            }
        }
        return false;
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
        if (isEmpty()) {
            return;
        }
        firstPos = new Point(e.getPoint());
        Cursor c = getCursor(e);
        if (c != null) {
            mode = getCursor(e).getType();
            somethingChanged = false;
            if (mode != Cursor.MOVE_CURSOR) {  // optimization
                FastCustomDrawableComponent.resizing = true;
            }

            // for correct snapping
            if (parent.getParent().snapToGrid()) {
                switch (mode) {
                case Cursor.MOVE_CURSOR:
                    Point locOnGrid = parent.snapToGrid(new Point(bounds.x, bounds.y));
                    Point diff = new Point(bounds.x - locOnGrid.x, bounds.y - locOnGrid.y);
                    Point mousePosOnGrid = parent.snapToGrid(firstPos);
                    firstPos = new Point(mousePosOnGrid.x + diff.x, mousePosOnGrid.y + diff.y);
                    break;
                case Cursor.NW_RESIZE_CURSOR:
                    firstPos = new Point(bounds.x, bounds.y);
                    break;
                case Cursor.N_RESIZE_CURSOR:
                    firstPos = new Point((int)bounds.getCenterX(), bounds.y);
                    break;
                case Cursor.NE_RESIZE_CURSOR:
                    firstPos = new Point((int)bounds.getMaxX(), bounds.y);
                    break;
                case Cursor.SW_RESIZE_CURSOR:
                    firstPos = new Point(bounds.x, (int)bounds.getMaxY());
                    break;
                case Cursor.S_RESIZE_CURSOR:
                    firstPos = new Point((int)bounds.getCenterX(), (int)bounds.getMaxY());
                    break;
                case Cursor.SE_RESIZE_CURSOR:
                    firstPos = new Point((int)bounds.getMaxX(), (int)bounds.getMaxY());
                    break;
                case Cursor.W_RESIZE_CURSOR:
                    firstPos = new Point(bounds.x, (int)bounds.getCenterY());
                    break;
                case Cursor.E_RESIZE_CURSOR:
                    firstPos = new Point((int)bounds.getMaxX(), (int)bounds.getCenterY());
                    break;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (somethingChanged) {
            update(e.getPoint());
        }
        if (!isEmpty() && (mode != -1) && (e.getButton() == MouseEvent.BUTTON1) && somethingChanged) {
            parent.getParent().addUndoBufferEntry(mode == Cursor.MOVE_CURSOR ? "Move" : "Resize");
        }
        mode = -1;
        FastCustomDrawableComponent.resizing = false;
    }

    public void mouseDragged(MouseEvent e) {
        update(e.getPoint());
    }

    private void update(Point mousePos) {
        //System.out.println("Update" + new Random().nextInt());
        if (!isEmpty() || (mode != -1)) {
            somethingChanged = true;
            mousePos = parent.snapToGrid(mousePos);
            Point diff = new Point(mousePos.x - firstPos.x, mousePos.y - firstPos.y);
            firstPos = mousePos;
            //Rectangle old = getBounds();
            Rectangle currentBounds = null;

            switch (mode) {
            case Cursor.MOVE_CURSOR:
                for (SelectedElement e : elements) {
                    Point loc = e.widget.getLocation();
                    e.widget.setLocation(loc.x + diff.x, loc.y + diff.y);
                }
                bounds = computeBounds();
                break;
            case Cursor.NW_RESIZE_CURSOR:
                currentBounds = new Rectangle(bounds.x + diff.x, bounds.y + diff.y, bounds.width - diff.x, bounds.height - diff.y);
                break;
            case Cursor.N_RESIZE_CURSOR:
                currentBounds = new Rectangle(bounds.x, bounds.y + diff.y, bounds.width, bounds.height - diff.y);
                break;
            case Cursor.NE_RESIZE_CURSOR:
                currentBounds = new Rectangle(bounds.x, bounds.y + diff.y, bounds.width + diff.x, bounds.height - diff.y);
                break;
            case Cursor.SW_RESIZE_CURSOR:
                currentBounds = new Rectangle(bounds.x + diff.x, bounds.y, bounds.width - diff.x, bounds.height + diff.y);
                break;
            case Cursor.S_RESIZE_CURSOR:
                currentBounds = new Rectangle(bounds.x, bounds.y, bounds.width, bounds.height + diff.y);
                break;
            case Cursor.SE_RESIZE_CURSOR:
                currentBounds = new Rectangle(bounds.x, bounds.y, bounds.width + diff.x, bounds.height + diff.y);
                break;
            case Cursor.W_RESIZE_CURSOR:
                currentBounds = new Rectangle(bounds.x + diff.x, bounds.y, bounds.width - diff.x, bounds.height);
                break;
            case Cursor.E_RESIZE_CURSOR:
                currentBounds = new Rectangle(bounds.x, bounds.y, bounds.width + diff.x, bounds.height);
                break;
            }

            // resize operation?
            if (currentBounds != null && currentBounds.getWidth() >= MINIMUM_SIZE && currentBounds.getHeight() >= MINIMUM_SIZE) {
                //System.out.println(currentBounds);
                double scaleFactorX = (double)currentBounds.getWidth() / (double)oldBounds.getWidth();
                double scaleFactorY = (double)currentBounds.getHeight() / (double)oldBounds.getHeight();
                for (SelectedElement elem : elements) {
                    double x = (elem.oldBounds.getX() - oldBounds.getX()) * scaleFactorX + currentBounds.getX();
                    double y = (elem.oldBounds.getY() - oldBounds.getY()) * scaleFactorY + currentBounds.getY();
                    double w = (double)elem.oldBounds.width * scaleFactorX;
                    double h = (double)elem.oldBounds.height * scaleFactorY;
                    elem.widget.setBounds(new Rectangle((int)x, (int)y, (int)w, (int)h));
                }
                bounds = currentBounds;
            }

            // repaint
            //parent.paintImmediately(old.union(getBounds()));
            //parent.repaint(old.union(getBounds()));
            parent.repaint();
        }
    }

    public Cursor getCursor(MouseEvent e) {
        if (isEmpty()) {
            return null;
        }
        for (Entry<Integer, Rectangle> entry : resizeRectangles.entrySet()) {
            if (entry.getValue().contains(e.getPoint())) {
                return Cursor.getPredefinedCursor(entry.getKey());
            }
        }
        if (parent.getParent().isCtrlPressed()) { // don't move if ctrl is pressed
            return null;
        }
        for (SelectedElement elem : elements) {
            if (elem.widget.getBounds().contains(e.getPoint())) {
                return Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
            }
        }

        return null;
    }

    public void mouseMoved(MouseEvent e) {  }


    @SuppressWarnings("rawtypes")
    public void dataModelChanged(DataModelBase caller, Event event, Object param) {
        if (caller != parent.getModel() && !parent.getModel().getChildren().contains(caller)) {
            caller.removeDataModelListener(this);
            return;
        }

        if (event != Event.SelectionChanged) {
            return;
        }

        Rectangle temp = getBounds();
        setSelection(parent.getModel().getSelection());
        try {
            parent.repaint(getBounds().union(temp));
        } catch (Exception e) {
            parent.repaint();
        }
    }

    public void setSelection(Set<Widget> selection) {

        // disable selected components
        for (WidgetUI.WidgetUIContainer wui : parent) {
            wui.setEnabled(!selection.contains(wui.getModel()));
        }
        bounds = null;
        oldBounds = null;
        elements.clear();
        if (selection == null) {
            return;
        }
        for (Widget c : selection) {
            elements.add(new SelectedElement(c));
            updateBounds();
        }
    }
}
