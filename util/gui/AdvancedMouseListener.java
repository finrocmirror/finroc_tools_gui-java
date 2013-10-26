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
package org.finroc.tools.gui.util.gui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.MouseInputListener;

/**
 * @author Max Reichardt
 *
 * MouseListener with advanced functionality
 */
public class AdvancedMouseListener < S extends Enum<?>, A extends Enum<? >> implements MouseInputListener, MouseWheelListener {

    /** Current State */
    protected S mode;

    protected Point pressPos;
    protected Point lastPos;
    protected Point secondLastPos;
    protected double diffToPressPosX, diffToPressPosY;
    protected double diffToLastPosX, diffToLastPosY;
    protected int curButton;
    protected int lastWheelMove;

    public static enum EventType { PressDragMove, DragMove, Release, MouseOver, Wheel }
    public static enum Button { Left, Middle, Right }

    private List<MouseAction> actions = new ArrayList<MouseAction>();

    public void addMouseAction(EventType et, Button button, S mode, A actionId, MouseEventListener<A> listener) {
        actions.add(new MouseAction(et, button, mode, actionId, listener));
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        pressPos = e.getPoint();
        lastPos = pressPos;
        curButton = e.getButton();
        updateDiffs(e);
        fireAction(EventType.PressDragMove, curButton, e);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        lastWheelMove = e.getWheelRotation();
        fireAction(EventType.Wheel, -1, e);
    }

    public void mouseReleased(MouseEvent e) {
        updateDiffs(e);
        fireAction(EventType.DragMove, curButton, e);
        fireAction(EventType.PressDragMove, curButton, e);
        fireAction(EventType.Release, curButton, e);
    }

    public void mouseDragged(MouseEvent e) {
        updateDiffs(e);
        fireAction(EventType.DragMove, curButton, e);
        fireAction(EventType.PressDragMove, curButton, e);
        fireAction(EventType.MouseOver, curButton, e);
    }

    private void fireAction(EventType et, int curButton, MouseEvent me) {
        for (MouseAction action : actions) {
            if (action.accepts(et, curButton, me)) {
                action.fire(me);
            }
        }
    }

    protected void updateDiffs(MouseEvent e) {
        diffToPressPosX = e.getX() - pressPos.x;
        diffToPressPosY = e.getY() - pressPos.y;
        diffToLastPosX = e.getX() - lastPos.x;
        diffToLastPosY = e.getY() - lastPos.y;
        secondLastPos = lastPos;
        lastPos = e.getPoint();
    }

    public void mouseMoved(MouseEvent e) {
        fireAction(EventType.MouseOver, curButton, e);
    }

    public S getMode() {
        return mode;
    }

    public void setMode(S mode) {
        this.mode = mode;
    }

    private class MouseAction {

        EventType et;
        Button button;
        S mode;
        A actionId;
        MouseEventListener<A> listener;

        private MouseAction(EventType et, Button button, S mode, A actionId, MouseEventListener<A> listener) {
            this.button = button;
            this.mode = mode;
            this.actionId = actionId;
            this.et = et;
            this.listener = listener;
        }

        private boolean accepts(EventType et, int curButton, MouseEvent me) {
            if (this.et != null && et != this.et) {
                return false;
            }
            if (mode != null && mode != AdvancedMouseListener.this.mode) {
                return false;
            }
            if (button != null) {
                if (curButton == MouseEvent.BUTTON1 && button != Button.Left) {
                    return false;
                }
                if (curButton == MouseEvent.BUTTON2 && button != Button.Middle) {
                    return false;
                }
                if (curButton == MouseEvent.BUTTON3 && button != Button.Right) {
                    return false;
                }
            }
            return true;
        }

        private void fire(MouseEvent me) {
            listener.mouseEvent(actionId, AdvancedMouseListener.this, me);
        }
    }

    public int getCurButton() {
        return curButton;
    }

    public double getDiffToLastPosX() {
        return diffToLastPosX;
    }

    public double getDiffToLastPosY() {
        return diffToLastPosY;
    }

    public double getDiffToPressPosX() {
        return diffToPressPosX;
    }

    public double getDiffToPressPosY() {
        return diffToPressPosY;
    }

    public Point getLastPos() {
        return lastPos;
    }

    public Point getPressPos() {
        return pressPos;
    }

    public Point getSecondLastPos() {
        return secondLastPos;
    }

    public int getLastWheelMove() {
        return lastWheelMove;
    }
}
