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
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.UIBase;
import org.finroc.tools.gui.commons.EventRouter;
import org.finroc.tools.gui.commons.fastdraw.FastCustomDrawableComponent;
import org.finroc.tools.gui.themes.Themes;

import org.finroc.core.port.ThreadLocalCache;


public abstract class WidgetUI extends FastCustomDrawableComponent {

    /** UID */
    private static final long serialVersionUID = -5145148033769674805L;

    private MouseEventHooker hooker;

    private Border titleBorder;

    private WidgetUIContainer container;

    private GUIPanelUI parent;

    /** Screen layout traits */
    public static final int TRAIT_DISPLAYS_LABEL = 1,
                            TRAIT_REQUIRES_BORDER_IN_DARK_COLORING = 2;

    /** Screen layout traits */
    private final int layoutTraits;

    protected WidgetUI(RenderMode renderMode) {
        this(renderMode, 0);
    }

    protected WidgetUI(RenderMode renderMode, int traits) {
        super(renderMode);
        layoutTraits = traits;
        this.setOpaque(useOpaquePanels());
    }

    public class WidgetUIContainer extends UIBase<GUIPanelUI, WidgetUI, Widget, WidgetUIContainer> {

        public WidgetUIContainer(GUIPanelUI parent, WidgetUI ui, Widget model) {
            super(parent, ui, model);
        }

        public void dataModelChanged(DataModelBase <? , ? , ? > caller, Event event, Object param) {
            //System.out.println("''");
            if (event == Event.widgetBoundsChanged) {
                setBounds(model.getBounds());
                //System.out.println(getSize());
                validate();
                repaint();
            }
            if (event == Event.WidgetPropertiesChanged) {
                updateBorder();
                widgetPropertiesChanged();
                ui.setChanged();
                repaint();
            }
        }

        public void setEnabled(boolean b) {
            WidgetUI.this.setEnabled(b);
        }

        public void dispose() {
            WidgetUI.this.dispose();
            super.dispose();
        }
    }

    public void init(GUIPanelUI parent, Widget model) {
        container = new WidgetUIContainer(parent, this, model);
        this.parent = parent;
        updateBorder();
        init(this);
    }

    /** Can be overridden to perform custom cleanup operations */
    protected void dispose() {}

    protected void init(Component c) {

        // Mouse Events "hooken"
        boolean exists = false;
        for (MouseListener ml : c.getMouseListeners()) {
            if (ml == hooker) {
                exists = true;
                continue;
            }
            c.removeMouseListener(ml);
            EventRouter.addListener(c, ml, MouseListener.class);
            if (!exists) {
                hooker = (hooker == null) ? new MouseEventHooker() : hooker;
                c.addMouseListener(hooker);
                exists = true;
            }
        }
        exists = false;
        for (MouseMotionListener ml : c.getMouseMotionListeners()) {
            if (ml == hooker) {
                exists = true;
                continue;
            }
            c.removeMouseMotionListener(ml);
            EventRouter.addListener(c, ml, MouseMotionListener.class);
            if (!exists) {
                hooker = (hooker == null) ? new MouseEventHooker() : hooker;
                c.addMouseMotionListener(hooker);
                exists = true;
            }
        }

        // damit TAB funktioniert...
        if (!isWidgetFocusable()) {
            c.setFocusable(false);
        }
        c.setFocusTraversalKeysEnabled(false);

        // rekursiv Subkomponenten durchgehen
        if (c instanceof Container) {
            for (Component c2 : ((Container)c).getComponents()) {
                init(c2);
            }
        }
    }

    protected boolean isWidgetFocusable() {
        return false;
    }

    protected Widget getModel() {
        return container.getModel();
    }

    private void updateBorder() {
        Themes.getCurTheme().processWidget(getModel(), this);
    }

    public void setTitleBorder(Border tb) {
        titleBorder = tb;
        super.setBorder(tb);
    }

    public void setBorder(Border b) {
        if (titleBorder != null) {
            super.setBorder(BorderFactory.createCompoundBorder(titleBorder, b));
        } else {
            super.setBorder(b);
        }
    }

    /**
     * @return Label color to actually use in widget
     */
    public Color getLabelColor(Widget w) {
        return Themes.getCurTheme().getLabelColor(w, this, w.getLabelColor());
    }

    public void widgetPropertiesChanged() {}

    private class MouseEventHooker implements MouseInputListener {

        public void mouseClicked(MouseEvent e) {
            if (sendEventToParent(e)) {
                adjustEvent(e);
                parent.processMouseEvent(e);
            } else {
                EventRouter.fireMouseClickedEvent(e.getSource(), e);
            }
        }

        public void mouseEntered(MouseEvent e) {
            if (sendEventToParent(e)) {
                adjustEvent(e);
                parent.processMouseEvent(e);
            } else {
                EventRouter.fireMouseEnteredEvent(e.getSource(), e);
            }
        }

        public void mouseExited(MouseEvent e) {
            if (sendEventToParent(e)) {
                adjustEvent(e);
                parent.processMouseEvent(e);
            } else {
                EventRouter.fireMouseExitedEvent(e.getSource(), e);
            }
        }

        public void mousePressed(MouseEvent e) {
            if (sendEventToParent(e)) {
                adjustEvent(e);
                parent.processMouseEvent(e);
            } else {
                EventRouter.fireMousePressedEvent(e.getSource(), e);
            }
        }

        public void mouseReleased(MouseEvent e) {
            if (sendEventToParent(e)) {
                adjustEvent(e);
                parent.processMouseEvent(e);
            } else {
                EventRouter.fireMouseReleasedEvent(e.getSource(), e);
            }
        }

        public void mouseDragged(MouseEvent e) {
            if (sendEventToParent(e)) {
                adjustEvent(e);
                parent.processMouseMotionEvent(e);
            } else {
                EventRouter.fireMouseDraggedEvent(e.getSource(), e);
            }
        }

        public void mouseMoved(MouseEvent e) {
            if (sendEventToParent(e)) {
                adjustEvent(e);
                parent.processMouseMotionEvent(e);
            } else {
                EventRouter.fireMouseMovedEvent(e.getSource(), e);
            }
        }

        private boolean sendEventToParent(MouseEvent e) {
            GUIWindowUIBase<?> win = container.getParent().getParent();
            GUIWindowUIBase.EditMode mode = win.getEditMode();
            return (!isEnabled()) || ((mode == GUIWindowUIBase.EditMode.editObject || mode == GUIWindowUIBase.EditMode.createObject) && ((e.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) || win.isCtrlPressed());
        }

        private void adjustEvent(MouseEvent e) {
            Component cur = e.getComponent();
            while (cur != parent.asComponent()) {
                e.translatePoint(cur.getX(), cur.getY());
                cur = cur.getParent();
            }
            e.setSource(parent.asComponent());
        }
    }

    public WidgetUIContainer asGUIModelElement() {
        return container;
    }

    public void releaseAllLocks() {
        ThreadLocalCache.getFast().releaseAllLocks();
    }

    public boolean useOpaquePanels() {
        return Themes.getCurTheme().useOpaquePanels();
    }

    /**
     * @return Screen layout traits
     */
    public int getLayoutTraits() {
        return layoutTraits;
    }
}
