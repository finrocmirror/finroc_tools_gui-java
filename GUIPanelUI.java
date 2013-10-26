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
package org.finroc.tools.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MouseInputListener;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;
import org.finroc.tools.gui.abstractbase.UIBase;
import org.finroc.tools.gui.commons.EventRouter;
import org.finroc.tools.gui.themes.Themes;
import org.finroc.tools.gui.util.gui.MPanel;


/**
 * @author Max Reichardt
 *
 * View of GUIPanel
 */
public class GUIPanelUI extends UIBase < GUIWindowUIBase<?>, GUIPanelUI.GUIPanelUIJPanel, GUIPanel, WidgetUI.WidgetUIContainer > implements MouseInputListener, ActionListener {

    /** UID */
    private static final long serialVersionUID = 5486418827698342216L;

    /** Rectangle for creating widget */
    private transient Rectangle createRectangle;

    /** Last MouseEvent */
    private transient MouseEvent lastEvent;

    /** PopUp-Menu for Right-Click */
    private transient JPopupMenu popupMenu;
    private static enum ReleasedOn { Widget, Selection, EmptySpace }
    private transient JMenuItem miCut, miCopy, miPaste, miDelete, miSelect, miSelectAll, miSelectNone, miProperties;
    private transient Widget curWidget;
    private transient Point curPos;

    /** Selection */
    private SelectionUI selection;

    public GUIPanelUI(GUIWindowUIBase<?> parent, GUIPanel model) {
        super(parent, null, model);
        ui = new GUIPanelUIJPanel();
        this.parent = parent;
        selection = new SelectionUI(this);
        setLayout(null);
        Themes.getCurTheme().initGUIPanel(ui);
        addMouseListener(this);
        addMouseMotionListener(this);

        // Create PopupMenu
        popupMenu = new JPopupMenu();
        miCut = createMenuEntry("Cut");
        miCopy = createMenuEntry("Copy");
        miPaste = createMenuEntry("Paste");
        miDelete = createMenuEntry("Delete");
        popupMenu.addSeparator();
        miSelect = createMenuEntry("Select");
        miSelectAll = createMenuEntry("Select All");
        miSelectNone = createMenuEntry("Select None");
        popupMenu.addSeparator();
        //miEditConnections = createMenuEntry("Edit Connections...");
        miProperties = createMenuEntry("Properties...");

        dataModelChanged(model, DataModelListener.Event.CompleteChange, model);
    }

    public void dispose() {
        super.dispose();
        EventRouter.objectDisposed(selection);
        selection = null;
    }

    /**
     * Convenient method the create menu entries and add this Window as listener
     *
     * @param string Text of menu entry
     * @return Created menu entry
     */
    private JMenuItem createMenuEntry(String string) {
        JMenuItem item = new JMenuItem(string);
        item.addActionListener(this);
        popupMenu.add(item);
        return item;
    }

    private void setMenuItemsEnabled(ReleasedOn ro) {
        // cut/copy/delete enabled?
        miCut.setEnabled(model.getSelection().size() > 0 || ro == ReleasedOn.Widget);
        miCopy.setEnabled(miCut.isEnabled());
        miDelete.setEnabled(miCut.isEnabled());
        // paste enabled?
        miPaste.setEnabled(parent.getClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor));
        // edit properties & connections enabled?
        //miEditConnections.setEnabled(ro == ReleasedOn.Widget);
        miProperties.setEnabled(ro == ReleasedOn.Widget);
        // select
        miSelect.setEnabled(ro == ReleasedOn.Widget && !model.getSelection().contains(curWidget));
    }

    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (parent == null || parent.getEditMode() == GUIWindowUI.EditMode.none) {
                return;
            }

            if (parent.getEditMode() == GUIWindowUI.EditMode.editObject || parent.getEditMode() == GUIWindowUI.EditMode.ctrlEditObject) {

                // only draw rectangle if outside of all objects
                if (!parent.isCtrlPressed()) {
                    for (Widget w : model.getChildren()) {
                        if (w.getBounds().contains(e.getPoint())) {
                            return;
                        }
                    }
                }
                if (selection.criticalPosition(e.getPoint())) {
                    return;
                }

                // clear selection in ctrl-edit mode when ctrl isn't pressed
                if (parent.getEditMode() == GUIWindowUI.EditMode.ctrlEditObject && (!parent.isCtrlPressed())) {
                    getParent().areaSelected(new Rectangle(-1000000, -1000000, 0, 0));
                    return;
                }

                createRectangle = new Rectangle(e.getPoint());
            } else {
                createRectangle = new Rectangle(snapToGrid(e.getPoint()));
            }

            repaint();
        }
    }

    Point snapToGrid(Point point) {
        if (parent.snapToGrid()) {
            return new Point(((point.x + 8) / 16) * 16, ((point.y + 8) / 16) * 16);
        }
        return point;
    }

    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            if (parent.getEditMode() == GUIWindowUI.EditMode.editObject || parent.getEditMode() == GUIWindowUI.EditMode.ctrlEditObject) {
                if (parent.isCtrlPressed()) {
                    for (Widget w : model.getChildren()) {
                        if (w.getBounds().contains(e.getPoint())) {
                            List<Widget> selection = new ArrayList<Widget>(model.getSelection());
                            if (!model.getSelection().contains(w)) {
                                // select
                                selection.add(w);
                            } else {
                                //  deselect
                                selection.remove(w);
                            }
                            model.setSelection(selection);
                            return;
                        }
                    }
                }
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        if (parent == null) {
            return;
        }
        if (parent.getEditMode() == GUIWindowUI.EditMode.editObject || parent.getEditMode() == GUIWindowUI.EditMode.ctrlEditObject) {
            Cursor c = selection.getCursor(e);
            if (c != null) {
                setCursor(c);
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    public void mouseReleased(MouseEvent me) {
        if (parent == null || parent.getEditMode() == GUIWindowUI.EditMode.none) {
            return;
        }

        if (me.getButton() == MouseEvent.BUTTON3 && (parent.getEditMode() == GUIWindowUI.EditMode.editObject || (parent.getEditMode() == GUIWindowUI.EditMode.ctrlEditObject && parent.isCtrlPressed()))) {
            Point p = me.getPoint();
            curPos = p;

            // Released on widget?
            for (Widget w : model.getChildren()) {
                if (w.getBounds().contains(p)) {
                    curWidget = w;
                    setMenuItemsEnabled(ReleasedOn.Widget);
                    popupMenu.show(ui, me.getX(), me.getY());
                    return;
                }
            }

            // Released on selection
            if (!selection.isEmpty() && selection.getBounds().contains(me.getPoint())) {
                curWidget = null;
                setMenuItemsEnabled(ReleasedOn.Selection);
                popupMenu.show(ui, me.getX(), me.getY());
                return;
            }

            // Released somewhere on panel
            curWidget = null;
            setMenuItemsEnabled(ReleasedOn.EmptySpace);
            popupMenu.show(ui, me.getX(), me.getY());
            return;
        }

        if (createRectangle == null || me.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        updateSelection(me);
        if (parent != null) {
            parent.areaSelected(cleanRectangle(createRectangle));
        }
        createRectangle = null;
        repaint();
    }

    private void updateSelection(MouseEvent me) {
        if (parent == null) {
            return;
        }
        if (lastEvent == null || (lastEvent != null && (lastEvent.getX() != me.getX() || lastEvent.getY() != me.getY()))) {
            lastEvent = me;
            Point p = me.getPoint();
            if (getParent().getEditMode() == GUIWindowUI.EditMode.createObject) {
                p = snapToGrid(p);
            }

            Rectangle exRect = new Rectangle(createRectangle);
            createRectangle.width = p.x - createRectangle.x;
            createRectangle.height = p.y - createRectangle.y;

            // repaint only rectangles
            repaintRect(exRect);
            repaintRect(createRectangle);

            //repaint();
            //for (Component w : this.getComponents()) {
            //  w.repaint();
            //}
        }
    }

    public void repaintRect(Rectangle r) {
        /*ui.repaint(r.x, r.y, 1, r.height + 1);
        ui.repaint(r.x + r.width, r.y, 1, r.height + 1);
        ui.repaint(r.x, r.y, r.width + 1, 1);
        ui.repaint(r.x, r.y + r.height, r.width + 1, 1);*/
        r = cleanRectangle(r);
        ui.repaint(r.x, r.y, r.width + 1, r.height + 1);
    }

    public void mouseDragged(MouseEvent me) {
        if (createRectangle == null) {
            return;
        }
        updateSelection(me);
    }

    /**
     * @param createRectangle Rectangle that may have negative width/height
     * @return Rectangle with positive width and height which covers the same area
     */
    public static Rectangle cleanRectangle(Rectangle createRectangle) {
        return new Rectangle(createRectangle.width >= 0 ? createRectangle.x : createRectangle.x + createRectangle.width,
                             createRectangle.height >= 0 ? createRectangle.y : createRectangle.y + createRectangle.height,
                             Math.abs(createRectangle.width), Math.abs(createRectangle.height));
    }

    /**
     * Calculate and set size of Panel according to Widgets on panel.
     * (needed for correct operation of scrolling panel)
     */
    private void setPreferredSize() {
        Dimension max = new Dimension();
        for (Widget w : model.getChildren()) {
            max.width = Math.max(w.getBounds().x + w.getBounds().width, max.width);
            max.height = Math.max(w.getBounds().y + w.getBounds().height, max.height);
        }
        setPreferredSize(max);
        if (parent != null) {
            parent.validate();
        }
    }


    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        // determine selection
        List<Widget> sel = new ArrayList<Widget>();
        if (curWidget == null) {
            sel.addAll(model.getSelection());
        } else {
            if (model.getSelection().contains(curWidget)) {
                sel.addAll(model.getSelection());
            } else {
                sel.add(curWidget);
            }
        }

        // cut/copy/delete
        if (o == miDelete) {
            ((GUIWindowUI)getParent()).connectionPanel.rightTree.storeExpandedElements();
            model.remove(sel);
            ((GUIWindowUI)getParent()).connectionPanel.rightTree.restoreExpandedElements();
            parent.addUndoBufferEntry("Delete Widgets");
        } else if (o == miCopy || o == miCut) {
            parent.copy(sel, o == miCut);
        } else if (o == miPaste) {
            // paste
            parent.paste(curPos);
        } else if (o == miSelect) {
            // select
            List<Widget> l = new ArrayList<Widget>();
            l.add(curWidget);
            model.setSelection(l);
        } else if (o == miSelectAll) {
            parent.actionPerformed(new ActionEvent(parent.miSelectAll, 0, ""));
        } else if (o == miSelectNone) {
            parent.actionPerformed(new ActionEvent(parent.miSelectNone, 0, ""));
        } else if (o == miProperties) {

            String old = FinrocGuiXmlSerializer.getInstance().toXML(sel);
            new WidgetPropertiesDialog((JFrame)getParent().asComponent(), sel, getModel().getRoot().getEmbeddedFileManager());
            if (!old.equals(FinrocGuiXmlSerializer.getInstance().toXML(sel))) {
                getParent().addUndoBufferEntry("Widget properties changed");
                getParent().repaint();
            }
        }
    }

    public void setParent(GUIWindowUI parent) {
        this.parent = parent;
    }


    public void dataModelChanged(DataModelBase <? , ? , ? > caller, Event event, Object param) {
        /*if (caller != model) {
            caller.removeDataModelListener(this);
            return;
        }*/
        getParent().updateToolBarState();
        if (model == null) {
            return;
        }

        if (event == DataModelListener.Event.ChildAdded || event == DataModelListener.Event.ChildRemoved || event == DataModelListener.Event.CompleteChange) {

            // delete obsolete UIs
            removeObsoleteUIs();

            // create new UIs
            for (Widget w : model.getChildren()) {
                if (getChild(w) == null) {
                    WidgetUI newui = w.createUI();
                    newui.init(this, w);
                    children.add(newui.asGUIModelElement());
                    ui.add(newui);
                    w.addDataModelListener(this);
                }
            }
            setPreferredSize();
            //repaint();
        }

        if (event == Event.widgetBoundsChanged) {
            setPreferredSize();
        }
        getParent().updateToolBarState();
    }

    public class GUIPanelUIJPanel extends MPanel {

        /** UID */
        private static final long serialVersionUID = -8220137772880712617L;

        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            if (createRectangle != null) {
                g.setColor(Color.WHITE);
                //g.
                //g.setXORMode(Color.WHITE);
                Rectangle r = cleanRectangle(createRectangle);
                g.drawRect(r.x, r.y, r.width, r.height);
                //System.out.println("drawing " + createRectangle.toString());
            }
            selection.drawSelection(g);
        }

        @Override
        public void processMouseEvent(MouseEvent e) {
            super.processMouseEvent(e);
        }

        @Override
        public void processMouseMotionEvent(MouseEvent e) {
            super.processMouseMotionEvent(e);
        }
    }

    public boolean widgetsWithSamePosExist(Collection<Widget> elemData) {
        for (Widget w1 : elemData) {
            for (Widget w2 : model.getChildren()) {
                if (w1.getBounds().getLocation().equals(w2.getBounds().getLocation())) {
                    return true;
                }
            }
        }
        return false;
    }

    public String toString() {
        return getModel().toString();
    }

    public void processMouseEvent(MouseEvent e) {
        ui.processMouseEvent(e);
    }

    public void processMouseMotionEvent(MouseEvent e) {
        ui.processMouseMotionEvent(e);
    }
}
