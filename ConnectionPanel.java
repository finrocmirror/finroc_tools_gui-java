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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.event.MouseInputListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.remote.Definitions;
import org.finroc.core.remote.HasURI;
import org.finroc.core.remote.ModelNode;
import org.finroc.core.remote.PortWrapper;
import org.finroc.core.remote.RemoteFrameworkElement;
import org.finroc.core.remote.RemotePort;
import org.finroc.tools.gui.ConnectorIcon.IconColor;
import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;
import org.finroc.tools.gui.commons.Util;
import org.finroc.tools.gui.themes.Themes;
import org.finroc.tools.gui.util.ElementFilter;
import org.finroc.tools.gui.util.gui.MJTree;
import org.rrlib.serialization.rtti.DataTypeBase;


/**
 * @author Max Reichardt
 *
 * This panel is used to connect the panel's to the widgets
 */
public class ConnectionPanel extends JPanel implements ComponentListener, DataModelListener, MouseInputListener, ActionListener, ElementFilter<Object> {

    /** UID */
    private static final long serialVersionUID = -4672134128946142093L;

    /** for optimized drawing */
    private static int MAXTRANSPARENTCONNECTIONS = 2;

    /** left and right tree */
    protected MJTree<Object> leftTree;
    protected MJTree<Object> rightTree;
    private JScrollPane leftScrollPane;
    private JScrollPane rightScrollPane;

    /** Color used in connection tree */
    public static final ConnectorIcon.BackgroundColor leftBackgroundColor = new ConnectorIcon.BackgroundColor(255, 255, 255);
    public static final ConnectorIcon.BackgroundColor rightBackgroundColor = new ConnectorIcon.BackgroundColor(242, 242, 255);
    public static final ConnectorIcon.IconColor defaultColor = new ConnectorIcon.IconColor(100, 100, 200);
    public static final ConnectorIcon.IconColor selectedColor = new ConnectorIcon.IconColor(255, 30, 30);
    public static final ConnectorIcon.IconColor connectedColor = new ConnectorIcon.IconColor(30, 200, 30);
    public static final ConnectorIcon.IconColor connectionPartnerMissingColor = new ConnectorIcon.IconColor(120, 10, 10);
    public static final ConnectorIcon.IconColor sensorInterfaceColor = new ConnectorIcon.IconColor(new Color(253, 251, 160), new Color(249, 247, 0));
    public static final ConnectorIcon.IconColor controllerInterfaceColor = new ConnectorIcon.IconColor(new Color(255, 190, 210), new Color(255, 125, 165));
    public static final ConnectorIcon.IconColor interfaceColor = new ConnectorIcon.IconColor(defaultColor.brighter().brighter(), defaultColor.brighter(), defaultColor.brighter());

    /** show right tree? */
    protected boolean showRightTree;

    /** parent window */
    private Owner parent;

    /** temporary variables for UI behaviour */
    protected boolean selectionFromRight;
    private List<Object> startPoints = new ArrayList<Object>();
    private MJTree<Object> startPointTree = null;
    private Point lastMousePos;
    protected final HashMap<Object, Definitions.TypeConversionRating> highlightedElementRatings = new HashMap<Object, Definitions.TypeConversionRating>();

    /** Tree node that mouse cursor is currently over - NULL if somewhere else */
    protected Object mouseOver = null;

    /** PopupMenu */
    protected JPopupMenu popupMenu;
    protected boolean popupOnRight;
    protected JMenuItem miSelectAll, miSelectVisible, miSelectNone, miExpandAll, miCollapseAll, miRemoveConnections, miRefresh, miRemoveAllConnections, miCopyURI, miCopyHostSpecificURI, miCopyLinks, miShowPartner;

    /** Tree cell Height */
    public static int HEIGHT = 0;

    /** Temporary object always returned by getNodeAppearance (allocated here to avoid object allocation whenever getNodeAppearance is called; may only be accessed by AWT Thread) */
    protected final NodeRenderingStyle tempRenderingStyle = new NodeRenderingStyle();

    /** Temporary object always returned by checkConnect (allocated here to avoid object allocation whenever getNodeAppearance is called; may only be accessed by AWT Thread) */
    protected final CheckConnectResult tempCheckConnectResult = new CheckConnectResult();

    /** Constant for text color in NodeRenderingStyle that signals that background color should be used */
    public static final Color TEXT_COLOR_BACKGROUND = new Color(255, 255, 255);

    static boolean NIMBUS_LOOK_AND_FEEL = Themes.nimbusLookAndFeel();

    /** Show hidden elements? */
    private boolean showHiddenElements = false;

    public ConnectionPanel(Owner win, Font treeFont) {

        setLayout(new GridLayout(1, 0));
        parent = win;

        // Setup all the scrolling stuff
        leftTree = new MJTree<Object>(this, 3);
        rightTree = new MJTree<Object>(this, 3);
        //setPreferredSize(new Dimension(Math.min(1920, Toolkit.getDefaultToolkit().getScreenSize().width) / 2, 0));
        setMinimumSize(new Dimension(300, 0));

        rightTree.setBackground(rightBackgroundColor);
        rightTree.setFocusTraversalKeysEnabled(false);
        rightTree.addKeyListener(win);
        rightTree.setRepaintDelegate(this);

        leftTree.setBackground(leftBackgroundColor);
        leftTree.setFocusTraversalKeysEnabled(false);
        leftTree.addKeyListener(win);
        leftTree.setRepaintDelegate(this);

        leftScrollPane = new JScrollPane(leftTree);
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(40);
        rightScrollPane = new JScrollPane(rightTree);
        rightScrollPane.getVerticalScrollBar().setUnitIncrement(40);
        leftScrollPane.setPreferredSize(new Dimension(Math.min(1920, Toolkit.getDefaultToolkit().getScreenSize().width) / 4, 0));
        rightScrollPane.setPreferredSize(new Dimension(Math.min(1920, Toolkit.getDefaultToolkit().getScreenSize().width) / 4, 0));
        leftScrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        leftScrollPane.setBorder(BorderFactory.createEmptyBorder());
        rightScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(leftScrollPane);
        add(rightScrollPane);

        // setup renderer
        leftTree.setCellRenderer(new GuiTreeCellRenderer(leftTree, false));
        rightTree.setCellRenderer(new GuiTreeCellRenderer(rightTree, true));

        // prevent row background from being highlighted
        if (ConnectionPanel.NIMBUS_LOOK_AND_FEEL) {
            UIDefaults uiDefaults = new UIDefaults();
            uiDefaults.put("Tree.selectionBackground", null);
            leftTree.putClientProperty("Nimbus.Overrides", uiDefaults);
            leftTree.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
            rightTree.putClientProperty("Nimbus.Overrides", uiDefaults);
            rightTree.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        }

        // init Listeners
        leftTree.addMouseListener(this);
        leftTree.addMouseMotionListener(this);
        rightTree.addMouseListener(this);
        rightTree.addMouseMotionListener(this);
        addComponentListener(this);

        // Create PopupMenu
        popupMenu = new JPopupMenu();
        miRemoveConnections = createMenuEntry("Remove Connection(s)");
        miRemoveAllConnections = createMenuEntry("Remove All Connections");
        miRefresh = createMenuEntry("Refresh");
        popupMenu.addSeparator();
        miExpandAll = createMenuEntry("Expand All");
        miCollapseAll = createMenuEntry("Collapse All");
        popupMenu.addSeparator();
        miSelectAll = createMenuEntry("Select All");
        miSelectVisible = createMenuEntry("Select Visible");
        miSelectNone = createMenuEntry("Select None");
        popupMenu.addSeparator();
        miCopyURI = createMenuEntry("Copy URI");
        miCopyHostSpecificURI = createMenuEntry("Copy host-specific URI");
        miCopyLinks = createMenuEntry("Copy Connection Links");
        miShowPartner = createMenuEntry("Show Connection Partner(s)");
        setTreeFont(treeFont);
    }

    private JMenuItem createMenuEntry(String string) {
        JMenuItem item = new JMenuItem(string);
        item.addActionListener(this);
        popupMenu.add(item);
        return item;
    }

    // Component interface implementation
    public void componentHidden(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
    public void componentResized(ComponentEvent e) {
        leftTree.getParent().validate();
        rightTree.getParent().validate();
    }


    public void setLeftTree(TreeModel tm) {
        //leftTree.setModel(null);
        leftTree.setModel(tm);
        leftTree.expandRoot();
    }

    public void setRightTree(TreeModel tm) {
        if (tm != null) {
            rightTree.setExpandNewElements(false);
            rightTree.setModel(tm);
            showRightTree = true;
            if (tm.getRoot() instanceof GUIPanel) {
                ((GUIPanel)tm.getRoot()).addDataModelListener(this);
                rightTree.setExpandNewElements(true);
            }
            if (getComponentCount() == 1) {
                add(rightScrollPane);
                this.validate();
                repaint();
            }
        } else {
            showRightTree = false;
            rightTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
            if (getComponentCount() == 2) {
                remove(rightScrollPane);
                this.validate();
                //pack();
                repaint();
            }
        }
        //rightScrollPane.setVisible(showRightTree);
    }

    /**
     * @return log description
     */
    public String getLogDescription() {
        return getClass().getSimpleName();
    }

    /**
     * @return tree model of right tree
     */
    public TreeModel getRightTree() {
        return rightTree.getModel();
    }

    /**
     * @return tree model of left tree
     */
    public TreeModel getLeftTree() {
        return leftTree.getModel();
    }

    public void dataModelChanged(DataModelBase <? , ? , ? > caller, Event event, Object param) {
        if (event == Event.SelectionChanged) {
            rightTree.repaint();
        }
        if (event == Event.ChildAdded) {
            rightTree.expandRoot();
        }
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    @SuppressWarnings("unchecked")
    public void mouseMoved(MouseEvent e) {

        // determine which element cursor is currently over
        Point pos = e.getPoint();
        Point diff = ((JComponent)e.getSource()).getLocationOnScreen();
        pos.x += diff.x - getLocationOnScreen().x;
        pos.y += diff.y - getLocationOnScreen().y;
        Object tpw = getTreeNodeFromPos(rightTree, pos);
        MJTree<Object> curTree = (MJTree<Object>)e.getSource();
        if (tpw == null) {
            tpw = getTreeNodeFromPos(leftTree, pos);
        }

        if (tpw != mouseOver) {
            mouseOver = tpw;

            // tool tip text
            setToolTipText(curTree, tpw);

            rightTree.repaint();
            leftTree.repaint();
        }
    }

    /**
     * Set tool tip for element
     * (may be overridden)
     *
     * @param tree Tree that we need to set tool tip text of
     * @param element Element to (possibly) set tool tip for
     */
    protected void setToolTipText(MJTree<Object> tree, Object element) {
        if (element == null || (!(element instanceof WidgetPort<?>))) {
            tree.setToolTipText(null);
            return;
        }

        WidgetPort<?> wp = (WidgetPort<?>)element;
        Set<String> links = wp.getConnectionLinks();
        if (links.size() == 0) {
            tree.setToolTipText(null);
        } else {
            String s = null;
            for (String link : links) {
                link = Util.escapeForHtml(link);
                if (s == null) {
                    s = "<html><p>" + link;
                } else {
                    s += "<br/>" + link; // + "</br>";
                }
            }
            tree.setToolTipText(s + "</p></html>");
        }
    }

    public void mousePressed(MouseEvent e) {

        if (e.getButton() != MouseEvent.BUTTON1 || showRightTree == false) {
            return;
        }

        // Which tree was chosen
        selectionFromRight = (e.getSource() == rightTree);
        MJTree<Object> selTree = selectionFromRight ? rightTree : leftTree;
        MJTree<Object> otherTree = selectionFromRight ? leftTree : rightTree;

        // Which matches exist?
        highlightedElementRatings.clear();
        for (Object otherNode : otherTree.getVisibleObjects()) {
            CheckConnectResult connections = checkConnect(otherNode);
            if (connections.impossibleHint == null || connections.impossibleHint.length() == 0) {
                highlightedElementRatings.put(otherNode, connections.minScore);
            }
        }

        // Mark all matches in other tree
        if (highlightedElementRatings.size() > 0) {
            otherTree.setSelectedObjects(new ArrayList<Object>(highlightedElementRatings.keySet()), false);
            ((GuiTreeCellRenderer)otherTree.getCellRenderer()).showSelectionAfter(400);
        } else {
            otherTree.setSelectedObjects(null, false);
        }

        // Set start points
        startPoints.clear();
        startPointTree = selTree;
        if (highlightedElementRatings.size() > 0) {
            startPoints.addAll(selTree.getSelectedObjects());
        }
    }

    /**
     * (may be overridden)
     *
     * @param node Tree node in question
     * @param partner Partner Port
     * @return Line starting position
     */
    protected ConnectorIcon.LineStart getLineStartPosition(Object node, Object partner) {
        if (node instanceof WidgetPort) {
            return ConnectorIcon.LineStart.Default;
        } else {
            return ((WidgetPort<?>)partner).isOutputPort() ? ConnectorIcon.LineStart.Incoming : ConnectorIcon.LineStart.Outgoing;
        }
    }

    /**
     * @param tree Tree that contains node
     * @param p Tree node
     * @param lineStart Type of line to determine starting position
     * @return Start point of connection line
     */
    protected Point getLineStartPoint(MJTree<Object> tree, Object p, ConnectorIcon.LineStart lineStart) {
        Rectangle r2 = tree.getObjectBounds(p, true);  // Bounds of tree node
        Rectangle r = tree.getObjectBounds(p, false);  // Bounds of tree node

        r.x += tree.getLocationOnScreen().x - getLocationOnScreen().x;  // coordinates on connection panel
        r.y += tree.getLocationOnScreen().y - getLocationOnScreen().y;
        ConnectorIcon icon = getNodeAppearance(p, tree, false).getIcon();
        int xpos = 0, ypos = 0;
        if (icon != null) {
            Point relativeStartPoint = icon.getLineStart(lineStart);
            xpos = tree == leftTree ? (r.x + r.width - icon.getIconWidth() + relativeStartPoint.x) : (r.x + icon.getIconWidth() + 1 - relativeStartPoint.x);
            ypos = relativeStartPoint.y;
            if (NIMBUS_LOOK_AND_FEEL) {
                xpos -= (tree == leftTree) ? 5 : 2;
                ypos += 2;
            }
        } else {
            xpos = tree == leftTree ? (r.x + r.width) : (r.x);
            ypos = HEIGHT / 2;
        }

        // Set xpos to tree widget edge if otherwise exceed tree widget bounds
        if (tree == leftTree) {
            xpos = Math.min(xpos, r2.x + r2.width + leftTree.getLocationOnScreen().x - getLocationOnScreen().x);
        } else {
            xpos = Math.max(xpos, r2.x + rightTree.getLocationOnScreen().x - getLocationOnScreen().x);
        }

        return new Point(xpos, r.y + ypos);
    }

    /**
     * Can the two ports be connected?
     * (Helper method for hypotheticalConnection; may be overridden)
     *
     * @param o1 Object 1
     * @param o2 Object 2
     * @return Answer
     */
    protected boolean canConnect(Object o1, Object o2) {
        WidgetPort<?> wp = null;
        Object other = null;
        int widgetPorts = ((o1 instanceof WidgetPort<?>) ? 1 : 0) + ((o2 instanceof WidgetPort<?>) ? 1 : 0);
        int portWrappers = ((o1 instanceof PortWrapper) ? 1 : 0) + ((o2 instanceof PortWrapper) ? 1 : 0);
        if (portWrappers != 2 || widgetPorts != 1 || o1 == null || o2 == null) {
            return false;
        } else if (o1 instanceof WidgetPort<?>) {
            wp = (WidgetPort<?>)o1;
            other = o2;
        } else if (o2 instanceof WidgetPort<?>) {
            wp = (WidgetPort<?>)o2;
            other = o1;
        } else {
            return false;
        }
        if (wp.isOutputPort()) {
            return wp.getPort().mayConnectTo(((PortWrapper)other).getPort(), false);
        } else {
            return ((PortWrapper)other).getPort().mayConnectTo(wp.getPort(), false);
        }
    }

    /**
     * Updates popup menu
     *
     * @param treeNode Selected tree node
     */
    protected void updatePopupMenu(Object treeNode) {
        if (treeNode instanceof WidgetPort<?>) {
            WidgetPort<?> wp = (WidgetPort<?>)treeNode;
            miCopyURI.setEnabled(false);
            miCopyLinks.setEnabled(wp.getConnectionLinks().size() > 0);
            miShowPartner.setEnabled(miCopyLinks.isEnabled() && wp.getPort().isConnected());
        } else {
            miCopyURI.setEnabled(!popupOnRight && (treeNode instanceof HasURI));
            miCopyLinks.setEnabled(false);
            miShowPartner.setEnabled(false);
        }
        miCopyHostSpecificURI.setEnabled(miCopyURI.isEnabled());
    }

    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON3) {
            if (!(e.getSource() instanceof MJTree<?>)) {
                return;
            }

            popupOnRight = e.getSource() == rightTree;
            Point p = ((JComponent)e.getSource()).getLocationOnScreen();
            Point p2 = getLocationOnScreen();
            popupMenu.show(this, e.getX() + p.x - p2.x, e.getY() + p.y - p2.y);
            saveLastMousePos(e);
            Object tn = getTreeNodeFromPos((MJTree<Object>)e.getSource());
            miRemoveConnections.setEnabled(tn != null);
            updatePopupMenu(tn);
            return;
        }

        saveLastMousePos(e);
        CheckConnectResult newConnections = null;
        MJTree<Object> otherTree = selectionFromRight ? leftTree : rightTree;
        if (startPoints.size() > 0) {
            newConnections = checkConnect(getTreeNodeFromPos(otherTree));
        }

        if (newConnections == null || (newConnections.impossibleHint != null && newConnections.impossibleHint.length() > 0)) {
            if (otherTree.getSelectionCount() > 0) {
                otherTree.clearSelection();
                ((GuiTreeCellRenderer)otherTree.getCellRenderer()).showSelectionAfter(0);
            }
        } else {
            // connect
            MJTree<Object> selectionTree = selectionFromRight ? rightTree : leftTree;
            List<Object> sourceNodes = selectionTree.getSelectedObjects();
            connect(sourceNodes, newConnections.partnerNodes);
            leftTree.setSelectedObjects(null, true);
            rightTree.setSelectedObjects(null, true);
            parent.addUndoBufferEntry("Connect");
        }
        startPoints.clear();
        lastMousePos = null;
        repaint();
    }

    /**
     * Connects two nodes
     * (may be overridden)
     *
     * @param node1 Node 1
     * @param node2 Node 2
     */
    protected void connect(Object node1, Object node2) {
        if (node1 == null || node2 == null || (!(node1 instanceof PortWrapper)) || (!(node2 instanceof PortWrapper))) {
            return;
        }
        WidgetPort<?> wp = (WidgetPort<?>)((node1 instanceof WidgetPort<?>) ? node1 : node2);
        PortWrapper other = (PortWrapper)((wp == node1) ? node2 : node1);
        wp.connectTo(other);
    }

    /**
     * Connects two list of nodes
     * (may be overridden)
     *
     * @param nodes1 Node list 1
     * @param nodes2 Node list 2
     */
    protected void connect(List<Object> nodes1, List<Object> nodes2) {
        for (int i = 0; i < nodes1.size(); i++) {
            connect(nodes1.get(i), nodes2.get(i));
        }
    }

    public void mouseDragged(MouseEvent e) {
        saveLastMousePos(e);
        if (startPoints.size() > 0) {
            repaint();
        }
    }

    public void saveLastMousePos(MouseEvent e) {
        lastMousePos = e.getPoint();
        Point diff = ((JComponent)e.getSource()).getLocationOnScreen();
        lastMousePos.x += diff.x - getLocationOnScreen().x;
        lastMousePos.y += diff.y - getLocationOnScreen().y;
    }

    @Override
    protected void paintChildren(Graphics g) {
        //((Graphics2D)g).scale(0.75, 0.75);
        try {
            super.paintChildren(g);
        } catch (Exception e) {}

        if (startPoints.size() > 0 && lastMousePos != null) {
            MJTree<Object> otherTree = selectionFromRight ? leftTree : rightTree;
            CheckConnectResult drawFat = checkConnect(getTreeNodeFromPos(otherTree));
            if (drawFat.partnerNodes.size() == 0) {
                for (Object p : startPoints) {
                    drawLine(g, getLineStartPoint(startPointTree, p, ConnectorIcon.LineStart.Default), lastMousePos, Color.BLACK, false, false);
                }
            } else {
                //((GuiTreeCellRenderer)otherTree.getCellRenderer()).showSelectionAfter(0);
                for (int i = 0; i < startPoints.size(); i++) {
                    if (i < drawFat.partnerNodes.size()) {
                        Point p1 = getLineStartPoint(startPointTree, startPoints.get(i), getLineStartPosition(startPoints.get(i), drawFat.partnerNodes.get(i)));
                        Point p2 = getLineStartPoint(otherTree, drawFat.partnerNodes.get(i), getLineStartPosition(drawFat.partnerNodes.get(i), startPoints.get(i)));
                        drawLine(g, p1, p2, selectedColor, true, false);
                    }
                }
            }
        }

        // draw existing connections
        drawConnections(g);
    }

    /**
     * Draw connections
     * (may be overridden)
     *
     * @param g Graphics object to draw to
     */
    protected void drawConnections(Graphics g) {
        drawConnectionsHelper(g, rightTree, leftTree);
    }

    /**
     * Helper for drawing connections from one tree to the other
     * (if both tree should be searched - like in finstruct - call twice)
     *
     * @param g Graphics object to draw to
     * @param fromTree Tree to search for connections
     * @param toTree Destination tree
     */
    protected void drawConnectionsHelper(Graphics g, MJTree<Object> fromTree, MJTree<Object> toTree) {
        if (!showRightTree) {
            return;
        }

        Rectangle visible = this.getVisibleRect();

        // count connections first, to decide if transparent drawing is possible
        List<Object> temp = fromTree.getVisibleObjects();
        int count = 0;
        boolean transparent = true;
        for (Object port : temp) {
            count += getConnectionPartners(port).size();
            if (count > MAXTRANSPARENTCONNECTIONS) {
                transparent = false;
                break;
            }
        }

        for (Object port : temp) {
            for (Object port2 : getConnectionPartners(port)) {
                if (port2 != null) {
                    if (toTree.isVisible(port2)) {
                        Point p1 = getLineStartPoint(fromTree, port, getLineStartPosition(port, port2));
                        Point p2 = getLineStartPoint(toTree, port2, getLineStartPosition(port2, port));
                        Color c = connectedColor;
                        if (mouseOver != null) {
                            if (mouseOver == port || mouseOver == port2) {
                                c = c.brighter();
                            }
                        }
                        if (visible.contains(p1) && visible.contains(p2)) {
                            drawLine(g, p1, p2, c, true, transparent);
                        } else if (g.getClipBounds().intersectsLine(p1.x, p1.y, p2.x, p2.y)) {
                            drawLine(g, p1, p2, c, false, false);
                        }
                    }
                }
            }
        }
    }

    /**
     * Get tree nodes in other tree that provided tree node is connected to
     * (may be overridden)
     *
     * @param port Tree node to check
     * @return List of nodes that port is connected to
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<Object> getConnectionPartners(Object treeNode) {
        if (treeNode instanceof WidgetPort<?>) {
            return (List)((WidgetPort<?>)treeNode).getConnectionPartners();
        }
        return new ArrayList<Object>();
    }

    protected void drawLine(Graphics g, Point p1, Point p2, Color c, boolean fat, boolean transparency) {
        Graphics2D g2d = (Graphics2D)g;
        g.setColor(c);
        if (transparency) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.4f));
        } else {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
        }
        g2d.setStroke(new BasicStroke(fat ? 2.0f : 1.0f));
        g.drawLine(p1.x, p1.y, p2.x, p2.y);

        /*if (!fat) {
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        } else {
            g.drawLine(p1.x, p1.y, p2.x+1, p2.y);
            g.drawLine(p1.x, p1.y-1, p2.x+1, p2.y-1);
            g.drawLine(p1.x-1, p1.y, p2.x, p2.y);
            g.drawLine(p1.x-1, p1.y-1, p2.x, p2.y-1);
        }*/
    }

    /**
     * Result of checkConnect() method
     * Used like a struct -> public fields.
     */
    public static class CheckConnectResult {

        /** Partner nodes that source nodes (with same index) could/would be connected to */
        public final ArrayList<Object> partnerNodes = new ArrayList<Object>();

        /** Scores for connection from source node #index to partner node #index (-> list has the same size as partnerNodes) */
        public final ArrayList<Definitions.TypeConversionRating> connectionScores = new ArrayList<Definitions.TypeConversionRating>();

        /** Minimum score in connectionScores */
        public Definitions.TypeConversionRating minScore;

        /** If there is no suitable combination to connect source nodes to partner nodes: contains hint/reason why not (as may be displayed as tool tip atop tree node) */
        public String impossibleHint;
    }

    /**
     * Get hypothetical connections for selected objects in current tree with visible nodes in other tree
     *
     * @param other Tree node in other tree to connect to
     * @param resultBuffer Buffer to write result to (optional). Is returned if provided - otherwise a new object is created.
     * @return List of nodes to connect to. null if there is no suitable combination.
     */
    protected CheckConnectResult checkConnect(Object other) {
        assert(SwingUtilities.isEventDispatchThread());
        return checkConnect(other, tempCheckConnectResult);
    }

    /**
     * Get hypothetical connections for selected objects in current tree with visible nodes in other tree
     *
     * @param other Tree node in other tree to connect to
     * @param Buffer to write result to
     * @return List of nodes to connect to. null if there is no suitable combination.
     */
    protected CheckConnectResult checkConnect(Object other, CheckConnectResult resultBuffer) {

        // Init objects
        resultBuffer.partnerNodes.clear();
        resultBuffer.connectionScores.clear();
        resultBuffer.minScore = Definitions.TypeConversionRating.IMPOSSIBLE;
        resultBuffer.impossibleHint = "";
        if (other == null) {
            resultBuffer.impossibleHint = "Partner object is null";
            return resultBuffer;
        }

        MJTree<Object> selectionTree = selectionFromRight ? rightTree : leftTree;
        MJTree<Object> otherTree = selectionFromRight ? leftTree : rightTree;

        // Can connect to this node?
        List<Object> nodesToConnect = selectionTree.getSelectedObjects();
        boolean canConnect = false;
        for (Object srcNode : nodesToConnect) {
            canConnect |= canConnect(other, srcNode);
            if (canConnect) {
                break;
            }
        }
        if (!canConnect) {
            resultBuffer.impossibleHint = "No selected element in other tree can connect to this one";
            return resultBuffer;
        }

        // Find "best" connection
        List<Object> potentialPartners = otherTree.getVisibleObjects();
        int startElement = potentialPartners.indexOf(other);
        assert(startElement >= 0);
        for (Object srcNode : nodesToConnect) {
            int i = startElement;
            Object partner = null;
            do {
                Object potentialPartner = potentialPartners.get(i);
                if (canConnect(srcNode, potentialPartner)) {
                    if (!resultBuffer.partnerNodes.contains(potentialPartner)) {
                        partner = potentialPartners.get(i);
                        break;
                    }
                }
                i = (i + 1) % potentialPartners.size();
            } while (i != startElement);
            if (partner == null) {
                resultBuffer.impossibleHint = "No connection partner for '" + srcNode.toString();
                resultBuffer.partnerNodes.clear();
                resultBuffer.connectionScores.clear();
                return resultBuffer;
            }
            resultBuffer.partnerNodes.add(partner);
            resultBuffer.connectionScores.add(Definitions.TypeConversionRating.NO_CONVERSION);
        }
        resultBuffer.minScore = Definitions.TypeConversionRating.NO_CONVERSION;
        resultBuffer.impossibleHint = "";
        return resultBuffer;
    }

    public Object getTreeNodeFromPos(MJTree<Object> otherTree) {
        return getTreeNodeFromPos(otherTree, lastMousePos);
    }

    public Object getTreeNodeFromPos(MJTree<Object> otherTree, Point pos) {

        if (otherTree == rightTree && (!showRightTree)) {
            return null;
        }

        // getPosition on JTree
        Point relMousePos = new Point(pos);
        Point diff = otherTree.getLocationOnScreen();
        relMousePos.x -= (diff.x - getLocationOnScreen().x);
        relMousePos.y -= (diff.y - getLocationOnScreen().y);

        // cursor on other Tree?
        if (!otherTree.getVisibleRect().contains(relMousePos)) {
            //System.out.println(relMousePos);
            return null;
        }

        // Get Node
        TreePath tp = otherTree.getPathForLocation(relMousePos.x, relMousePos.y);
        if (tp == null) {
            //System.out.println("2");
            return null;
        }
        return tp.getLastPathComponent();
    }

    public void actionPerformed(ActionEvent e) {
        MJTree<Object> ptree = popupOnRight ? rightTree : leftTree;
        if (e.getSource() == miSelectAll) {
            ptree.setSelectedObjects(ptree.getObjects(), true);
            ptree.expandAll(20);
        } else if (e.getSource() == miSelectNone) {
            ptree.setSelectedObjects(null, true);
        } else if (e.getSource() == miSelectVisible) {
            ptree.setSelectedObjects(ptree.getVisibleObjects(), true);
        } else if (e.getSource() == miRemoveConnections) {
            Object tnp = getTreeNodeFromPos(ptree);
            removeConnections(tnp);
            parent.addUndoBufferEntry("Remove connections");
            repaint();
        } else if (e.getSource() == miRefresh) {
            parent.refreshConnectionPanelModels();
            //parent.getParent().updateInterface();
        } else if (e.getSource() == miExpandAll) {
            ptree.expandAll(20);
        } else if (e.getSource() == miCollapseAll) {
            ptree.collapseAll();
        } else if (e.getSource() == miRemoveAllConnections) {
            int result = JOptionPane.showConfirmDialog(this, "This will remove all connections (from all ports). Are you sure?", "Are you sure?", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                for (Object wptnp : rightTree.getObjects()) {
                    removeConnections(wptnp);
                    /*WidgetPort<?> wp = (WidgetPort<?>)wptnp;
                    wp.clearConnections();*/
                }
                parent.addUndoBufferEntry("Remove all connections");
                repaint();
            }
        } else if (e.getSource() == miCopyURI || e.getSource() == miCopyHostSpecificURI) {
            Object tnp = getTreeNodeFromPos(ptree);
            String uriString = "Element has no UID";
            if (tnp instanceof HasURI) {
                URI uri = ((HasURI)tnp).getURI();
                if (e.getSource() == miCopyURI) {
                    uri = Util.getHostIndependentURI(uri);
                    try {
                        uri = new URI(uri.getScheme(), null, uri.getPath(), null);
                    } catch (Exception ex) {
                        uriString = "Erroneous UID (" + ex.getMessage() + ")";
                    }
                }
                uriString = uri.toString();
            }
            Clipboard clipboard = getToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(uriString), null);
        } else if (e.getSource() == miCopyLinks) {
            Object tnp = getTreeNodeFromPos(ptree);
            String uid = "";
            if (tnp instanceof WidgetPort<?>) {
                for (String s : ((WidgetPort<?>)tnp).getConnectionLinks()) {
                    if (uid.length() > 0) {
                        uid += "\n";
                    }
                    uid += s;
                }
            }
            Clipboard clipboard = getToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(uid), null);
        } else if (e.getSource() == miShowPartner) {
            Object tnp = getTreeNodeFromPos(ptree);
            WidgetPort<?> wp = (WidgetPort<?>)tnp;
            List<RemotePort> partners = wp.getConnectionPartners();
            for (Object partner : partners) {
                TreePath tp = leftTree.getTreePathFor(partner);
                leftTree.scrollPathToVisible(tp);
            }
        }
    }

    /**
     * Remove all connections from specified tree node
     * (may be overridden)
     *
     * @param tnp tree node
     */
    protected void removeConnections(Object tnp) {
        if (tnp instanceof WidgetPort<?>) {
            WidgetPort<?> wp = (WidgetPort<?>)tnp;
            wp.clearConnections();
        } else if (tnp instanceof PortWrapper) {
            for (Object wptnp : rightTree.getObjects()) {
                WidgetPort<?> wp = (WidgetPort<?>)wptnp;
                wp.removeConnection(((PortWrapper)tnp).getPort(), ((HasURI)tnp).getURI());
            }
            ((PortWrapper)tnp).getPort().disconnectAll(); // just to make sure...
        }
    }

    public void setTreeFont(Font f) {
        leftTree.setFont(f);
        rightTree.setFont(f);
    }

    /**
     * GUI window that owns panel
     */
    public interface Owner extends KeyListener {

        /**
         * Add undo buffer entry
         *
         * @param string Entry name
         */
        public void addUndoBufferEntry(String string);

        /**
         * Refresh models that are displayed in connection panel
         */
        public void refreshConnectionPanelModels();
    }

    @Override
    public void paint(Graphics g) {
        TreeModel tml = leftTree.getModel();
        TreeModel tmr = rightTree.getModel();
        if (tml != null) {
            synchronized (tml) {
                if (tmr != null) {
                    synchronized (tmr) {
                        super.paint(g);
                    }
                } else {
                    super.paint(g);
                }
            }
        } else {
            if (tmr != null) {
                synchronized (tmr) {
                    super.paint(g);
                }
            } else {
                super.paint(g);
            }
        }
    }

    @Override
    public boolean acceptElement(Object element) {
        return element instanceof PortWrapper;
    }

    /**
     * @param show Whether to show hidden elements
     */
    public void setShowHiddenElements(boolean show) {
        showHiddenElements = show;
    }

    /**
     * Rendering style for node in tree
     */
    public class NodeRenderingStyle {

        /** Text color (null means UI default) */
        public Color textColor;

        /**
         * Color of node (null is tree background)
         * If this is an icon color, node is drawn with connector icon
         */
        public Color nodeColor;

        /** Type of connector icon (see flags in ConnectorIcon class) */
        public int iconType;

        /**
         * Resets all values to their defaults
         *
         * @return Reference to this object
         */
        public void reset() {
            textColor = null;
            nodeColor = null;
            iconType = 0;
        }

        /**
         * @return Returns icon for
         */
        public ConnectorIcon getIcon() {
            if (nodeColor instanceof IconColor) {
                return ConnectorIcon.getIcon(iconType, (IconColor)nodeColor, (iconType & ConnectorIcon.RIGHT_TREE) != 0 ? rightBackgroundColor : leftBackgroundColor, HEIGHT);
            }
            return null;
        }
    }

    /**
     * Obtains rendering style for tree node.
     * (may be overridden)
     *
     * BRIGHTER_COLOR flag is typically not set by this method, but by caller.
     *
     * @param node Tree Node to obtain style for
     * @param tree Tree widget that node is drawn in
     * @param selected Is node selected in widget?
     * @return Rendering style. Note that object is reused on next call to this method (to avoid frequent memory allocations).
     */
    protected NodeRenderingStyle getNodeAppearance(Object node, MJTree<Object> tree, boolean selected) {
        assert(SwingUtilities.isEventDispatchThread());
        NodeRenderingStyle result = tempRenderingStyle;
        result.reset();
        if (node instanceof PortWrapper) {
            AbstractPort port = ((PortWrapper)node).getPort();
            result.textColor = tree.getBackground();
            boolean rpc = (port.getDataType().getTypeTraits() & DataTypeBase.IS_RPC_TYPE) != 0; //FinrocTypeInfo.isMethodType(port.getDataType(), true);
            boolean leftTreeRPCServerPort = rpc && port.getFlag(FrameworkElementFlags.ACCEPTS_DATA);
            boolean mouseOverFlag = (mouseOver instanceof PortWrapper) && (port == ((PortWrapper)mouseOver).getPort() || port.isConnectedTo(((PortWrapper)mouseOver).getPort()));
            result.nodeColor = selected ? selectedColor : (port.isConnected() ? connectedColor : (port.hasLinkEdges() ? connectionPartnerMissingColor : defaultColor));
            result.iconType = (port.isOutputPort() && (!leftTreeRPCServerPort) ? ConnectorIcon.OUTPUT : 0) | // in fingui rpc ports that can be used as server should always have server icon
                              (node.getClass().equals(RemotePort.class) && ((RemotePort)node).isProxy() ? ConnectorIcon.PROXY : 0) |
                              (rpc ? ConnectorIcon.RPC : 0) |
                              (mouseOverFlag ? ConnectorIcon.BRIGHTER_COLOR : 0);
        } else if (node.getClass().equals(RemoteFrameworkElement.class)) {
            RemoteFrameworkElement element = (RemoteFrameworkElement)node;
            if (element.isInterface()) {
                result.nodeColor = element.isSensorInterface() ? sensorInterfaceColor : (element.isControllerInterface() ? controllerInterfaceColor : interfaceColor);
                boolean rpc = element.isRpcOnlyInterface();
                result.iconType = (element.isOutputOnlyInterface() && (!rpc) ? ConnectorIcon.OUTPUT : 0) | // in fingui rpc interfaces should always have server icon (proxy)
                                  (element.isProxyInterface() || (rpc) ? ConnectorIcon.PROXY : 0) |
                                  (rpc ? ConnectorIcon.RPC : 0);
            }
        } else if (node instanceof Widget && ((Widget)node).getParent().getSelection().contains(node)) {
            result.nodeColor = Color.YELLOW;
        }
        result.iconType |= (tree == rightTree) ? ConnectorIcon.RIGHT_TREE : 0;

        return result;
    }

    /**
     * @author Max Reichardt
     *
     * Tree-Cell-Renderer for the GUI-Tree
     */
    class GuiTreeCellRenderer extends DefaultTreeCellRenderer implements ActionListener {

        /** UID */
        private static final long serialVersionUID = 4342216001043562115L;

        /** Colors */
        public final ConnectorIcon.BackgroundColor backgroundColor;

        /** Renderer for tree on the right? */
        private final boolean rightTree;

        /** for showing selection after some time */
        private final Timer timer;

        /** Tree renderer belong to */
        private final MJTree<Object> parent;

        /** Default renderer for JTree - and one for invisible nodes */
        private final DefaultTreeCellRenderer defaultRenderer, invisibleRenderer;

        /** Color for contour */
        private Color contourColor;

        public GuiTreeCellRenderer(MJTree<Object> parent, boolean rightTree) {
            setBackgroundNonSelectionColor(defaultColor);
            backgroundColor = rightTree ? rightBackgroundColor : leftBackgroundColor;
            setBorderSelectionColor(selectedColor);
            setBackgroundSelectionColor(selectedColor);
            defaultRenderer = new DefaultTreeCellRenderer();
            defaultRenderer.setBackgroundNonSelectionColor(backgroundColor);
            invisibleRenderer = new DefaultTreeCellRenderer();
            invisibleRenderer.setPreferredSize(new Dimension(1, 0));
            this.rightTree = rightTree;
            this.parent = parent;
            timer = new Timer(1000, this);
            timer.stop();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if ((!showHiddenElements) && (value instanceof ModelNode) && ((ModelNode)value).isHidden(true)) {
                return invisibleRenderer;
            }
            NodeRenderingStyle style = getNodeAppearance(value, parent, !timer.isRunning() && sel);
            if (style.nodeColor == null || style.nodeColor.getClass().equals(Color.class)) {
                Color bg = style.nodeColor == null ? backgroundColor : style.nodeColor;
                Color fg = style.textColor == null ? Color.black : style.textColor;
                defaultRenderer.setBackgroundNonSelectionColor(bg);
                defaultRenderer.setBackground(bg);
                defaultRenderer.setBackgroundSelectionColor(bg);
                defaultRenderer.setTextNonSelectionColor(fg);
                defaultRenderer.setTextSelectionColor(fg);
                defaultRenderer.setOpaque(ConnectionPanel.NIMBUS_LOOK_AND_FEEL && bg != null);
                return defaultRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            }


            //JLabel result = new JLabel(value.toString());
            //if (sel) {
            //  result.setBorder(BorderFactory.createLineBorder(selectedBorder));
            //}
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            //setBackground(color);
            Color c = style.nodeColor;
            contourColor = ((IconColor)c).contour;
            if ((style.iconType & ConnectorIcon.BRIGHTER_COLOR) != 0) {
                c = ((IconColor)c).brighter;
            }
            setBackgroundSelectionColor(c);
            setBackgroundNonSelectionColor(c);
            setBorderSelectionColor(c);
            Color textColor = style.textColor == null ? Color.black : (style.textColor == TEXT_COLOR_BACKGROUND ? backgroundColor : style.textColor);
            setTextSelectionColor(textColor);
            setTextNonSelectionColor(textColor);

            setIconTextGap(0);
            if (!rightTree) {
                setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            //this.setHorizontalTextPosition(SwingConstants.RIGHT);
            //setOpaque(true);
            /*result.setFont(c.getFont());
            result.setForeground(sel ? selected : background);*/
            ConnectionPanel.HEIGHT = super.getPreferredSize().height;
            setIcon(style.getIcon());
            return this;
        }

        @Override
        public void paint(Graphics g) {

            if (ConnectionPanel.NIMBUS_LOOK_AND_FEEL) {
                // TODO: do we still need that?
                // copied from super class to ensure that background is filled
                g.setColor(super.selected ? getBackgroundSelectionColor() : getBackgroundNonSelectionColor());
                if (getComponentOrientation().isLeftToRight()) {
                    g.fillRect(-1, 0, getWidth() - 4, getHeight());
                } else {
                    g.fillRect(0, 0, getWidth() - 5, getHeight());
                }
            }

            setForeground(this.getTextNonSelectionColor());
            super.paint(g);
            Color temp = g.getColor();

            g.setColor(backgroundColor);
            g.drawLine(0, 0, getWidth() - 1, 0);
            ConnectorIcon icon = (ConnectorIcon)getIcon();
            int iconStartX = rightTree ? 0 : (getWidth() - getIcon().getIconWidth() - 5);
            if (ConnectionPanel.NIMBUS_LOOK_AND_FEEL) {
                g.fillRect(getWidth() - 5, 0, 5, getHeight());
                g.fillRect(-1, 0, 1, getHeight());
                g.drawLine(iconStartX, 1, iconStartX + icon.getIconWidth(), 1);
            }
            g.drawLine(iconStartX, getHeight() - 1, iconStartX + icon.getIconWidth(), getHeight() - 1);
            g.setColor(contourColor);
            int baseWidth = getWidth() - 6;
            if (ConnectionPanel.NIMBUS_LOOK_AND_FEEL) {
                g.drawLine(rightTree ? iconStartX + icon.getInsetTop() : 0, 1, rightTree ? baseWidth : (baseWidth - icon.getInsetTop()), 1);
            }
            g.drawLine(rightTree ? iconStartX + icon.getInsetBottom() : 0, getHeight() - 1, rightTree ? baseWidth : (baseWidth - icon.getInsetBottom()), getHeight() - 1);

            g.setColor(temp);
        }

        public void showSelectionAfter(int l) {
            timer.stop();
            if (l > 0) {
                timer.setInitialDelay(l);
                timer.start();
            } else {
                parent.repaint();
            }
        }

        public void actionPerformed(ActionEvent e) {
            // timer ticked;
            timer.stop();
            parent.repaint();
        }
    }
}
