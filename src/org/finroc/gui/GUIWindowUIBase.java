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

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.finroc.gui.abstractbase.UIBase;

import org.finroc.core.plugin.ConnectionListener;


public abstract class GUIWindowUIBase<P extends UIBase<?,?,?,?>> extends UIBase<P, Container, GUIWindow, GUIPanelUI> implements ConnectionListener {

    /** Available GUI-Modes and current GUI-Mode */
    public enum EditMode { none, createObject, editObject }
    protected transient EditMode editMode;

    /** JTabbedPane (used for displaying multiple panels) */
    protected transient JTabbedPane tabbedPane;

    /** reference to status bar */
    protected transient StatusBar statusBar;

    public JMenuItem miSelectAll, miSelectNone;

    public GUIWindowUIBase(P parent, Container ui, GUIWindow model) {
        super(parent, ui, model);
    }

    public EditMode getEditMode() {
        return editMode;
    }

    /**
     * Change current Edit mode.
     *
     * @param em new Edit mode
     */
    protected void setEditMode(EditMode em) {
        editMode = em;
        if (em == EditMode.createObject) {
            getCurPanel().getModel().setSelection(null);
            setPanelCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        } else if (em == EditMode.editObject) {
            setPanelCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * @return GUI-Panel that is currently visible
     */
    GUIPanelUI getCurPanel() {
        if (model.getChildren().size() > 1) {
            return getChild((Container)((JScrollPane)tabbedPane.getSelectedComponent()).getViewport().getView());
        }
        return children.get(0);
    }

    /**
     * Set Cursor on panel(s)
     *
     * @param c Cursor
     */
    public void setPanelCursor(Cursor c) {
        for (GUIPanelUI panel : children) {
            panel.setCursor(c);
        }
    }

    // dummy methods... can be overridden
    public boolean snapToGrid() {
        return false;
    }

    public Clipboard getClipboard() {
        return null;
    }

    public boolean isCtrlPressed() {
        return false;
    }

    public void areaSelected(Rectangle rectangle) {
    }

    public void addUndoBufferEntry(String string) {
    }

    public void copy(List<Widget> sel, boolean b) {
    }

    public void paste(Point curPos) {
    }

    public void actionPerformed(ActionEvent event) {
    }

    public void updateToolBarState() {
    }
}
