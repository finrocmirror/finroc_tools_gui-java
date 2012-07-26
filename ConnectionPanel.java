/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2007-2012 Max Reichardt
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
package org.finroc.tools.gui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.finroc.core.port.PortFlags;
import org.finroc.core.portdatabase.FinrocTypeInfo;
import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;
import org.finroc.tools.gui.themes.Themes;
import org.finroc.tools.gui.util.gui.MJTree;
import org.finroc.tools.gui.util.treemodel.PortWrapper;
import org.finroc.tools.gui.util.treemodel.TreePortWrapper;
import org.finroc.tools.gui.util.treemodel.Uid;


/**
 * @author Max Reichardt
 *
 * This panel is used to connect the panel's to the widgets
 */
public class ConnectionPanel extends JPanel implements ComponentListener, DataModelListener, MouseInputListener, ActionListener {

    /** UID */
    private static final long serialVersionUID = -4672134128946142093L;

    /** for optimized drawing */
    private static int MAXTRANSPARENTCONNECTIONS = 2;

    /** left and right tree */
    protected MJTree<TreePortWrapper> leftTree;
    protected MJTree<TreePortWrapper> rightTree;
    private JScrollPane leftScrollPane;
    private JScrollPane rightScrollPane;

    /** Color used in connection tree */
    public static final ConnectorIcon.BackgroundColor leftBackgroundColor = new ConnectorIcon.BackgroundColor(new JTree().getBackground());
    public static final ConnectorIcon.BackgroundColor rightBackgroundColor = new ConnectorIcon.BackgroundColor(new Color(242, 242, 255));
    public static final ConnectorIcon.IconColor defaultColor = new ConnectorIcon.IconColor(new Color(100, 100, 200));
    public static final ConnectorIcon.IconColor selectedColor = new ConnectorIcon.IconColor(new Color(255, 30, 30));
    public static final ConnectorIcon.IconColor connectedColor = new ConnectorIcon.IconColor(new Color(30, 200, 30));
    public static final ConnectorIcon.IconColor connectionPartnerMissingColor = new ConnectorIcon.IconColor(new Color(120, 10, 10));

    /** show right tree? */
    private boolean showRightTree;

    /** parent window */
    private Owner parent;

    /** temporary variables for UI behaviour */
    private boolean selectionFromRight;
    private List<TreePortWrapper> startPoints = new ArrayList<TreePortWrapper>();
    private MJTree<TreePortWrapper> startPointTree = null;
    private Point lastMousePos;

    /** WidgetPort that mouse cursor is currently over - NULL if somewhere else */
    TreePortWrapper mouseOver = null;

    /** PopupMenu */
    protected JPopupMenu popupMenu;
    protected boolean popupOnRight;
    JMenuItem miSelectAll, miSelectVisible, miSelectNone, miExpandAll, miCollapseAll, miRemoveConnections, miRefresh, miRemoveAllConnections, miCopyUID, miCopyLinks, miShowPartner;

    /** Tree cell Height */
    public static int HEIGHT = 0;

    static boolean NIMBUS_LOOK_AND_FEEL = Themes.nimbusLookAndFeel();

    public ConnectionPanel(Owner win, Font treeFont) {

        setLayout(new GridLayout(1, 0));
        parent = win;

        // Setup all the scrolling stuff
        leftTree = new MJTree<TreePortWrapper>(TreePortWrapper.class, 3);
        rightTree = new MJTree<TreePortWrapper>(TreePortWrapper.class, 3);
        //setPreferredSize(new Dimension(Math.min(1920, Toolkit.getDefaultToolkit().getScreenSize().width) / 2, 0));
        setMinimumSize(new Dimension(300, 0));
        rightTree.setBackground(rightBackgroundColor.color);
        rightTree.setFocusTraversalKeysEnabled(false);
        rightTree.addKeyListener(win);
        leftTree.setFocusTraversalKeysEnabled(false);
        leftTree.addKeyListener(win);
        leftTree.setRepaintDelegate(this);
        rightTree.setRepaintDelegate(this);
        leftScrollPane = new JScrollPane(leftTree);
        rightScrollPane = new JScrollPane(rightTree);
        leftScrollPane.setPreferredSize(new Dimension(Math.min(1920, Toolkit.getDefaultToolkit().getScreenSize().width) / 4, 0));
        rightScrollPane.setPreferredSize(new Dimension(Math.min(1920, Toolkit.getDefaultToolkit().getScreenSize().width) / 4, 0));
        leftScrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        leftScrollPane.setBorder(BorderFactory.createEmptyBorder());
        rightScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(leftScrollPane, BorderLayout.WEST);
        add(rightScrollPane, BorderLayout.CENTER);

        // setup renderer
        leftTree.setCellRenderer(new GuiTreeCellRenderer(leftTree, false, this));
        rightTree.setCellRenderer(new GuiTreeCellRenderer(rightTree, true, this));

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
        miCopyUID = createMenuEntry("Copy UID");
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
            rightTree.setModel(tm);
            showRightTree = true;
            if (tm.getRoot() instanceof GUIPanel) {
                ((GUIPanel)tm.getRoot()).addDataModelListener(this);
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
        TreePortWrapper tpw = getTreePortWrapperFromPos(rightTree, pos);
        MJTree<TreePortWrapper> curTree = (MJTree<TreePortWrapper>)e.getSource();
        if (tpw == null) {
            tpw = getTreePortWrapperFromPos(leftTree, pos);
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
     * @param element
     */
    protected void setToolTipText(MJTree<TreePortWrapper> tree, TreePortWrapper element) {
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
                link = link.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
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

        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        // Welcher Baum wurde gewählt?
        selectionFromRight = (e.getSource() == rightTree);
        MJTree<TreePortWrapper> selTree = selectionFromRight ? rightTree : leftTree;
        MJTree<TreePortWrapper> otherTree = selectionFromRight ? leftTree : rightTree;

        // existieren Matches?
        boolean allMatches = true;
        for (TreePortWrapper p1 : selTree.getSelectedObjects()) {
            boolean matches = false;
            for (TreePortWrapper p2 : otherTree.getVisibleObjects()) {
                if (canConnect(p1, p2)) {
                    matches = true;
                    break;
                }
            }
            allMatches &= matches;
        }

        // wenn ja, alle Matches im anderen Baum markieren
        if (allMatches) {
            List<TreePortWrapper> matches = new ArrayList<TreePortWrapper>();
            for (TreePortWrapper p : otherTree.getVisibleObjects()) {
                for (TreePortWrapper p2 : selTree.getSelectedObjects()) {
                    if (canConnect(p, p2)) {
                        matches.add(p);
                    }
                }
            }
            otherTree.setSelectedObjects(matches, false);
            ((GuiTreeCellRenderer)otherTree.getCellRenderer()).showSelectionAfter(400);
        }

        // Startpunkte setzen
        startPoints.clear();
        startPointTree = selTree;
        for (TreePortWrapper p : selTree.getSelectedObjects()) {
            startPoints.add(p);
        }
    }

    /**
     * (may be overridden)
     *
     * @param port Port in question
     * @param partner Partner Port
     * @return Line starting position
     */
    protected ConnectorIcon.LineStart getLineStartPosition(PortWrapper port, PortWrapper partner) {
        if (port instanceof WidgetPort) {
            return ConnectorIcon.LineStart.Default;
        } else {
            return ((WidgetPort<?>)partner).isInputPort() ? ConnectorIcon.LineStart.Incoming : ConnectorIcon.LineStart.Outgoing;
        }
    }

    /**
     * @param tree Tree that contains port
     * @param p Port
     * @param lineStart Type of line to determine starting position
     * @return Start point of connection line
     */
    private Point getLineStartPoint(MJTree<TreePortWrapper> tree, TreePortWrapper p, ConnectorIcon.LineStart lineStart) {
        Rectangle r2 = tree.getObjectBounds(p, true);  // Bounds vom Eintrag
        Rectangle r = tree.getObjectBounds(p, false);  // Bounds vom Eintrag

        r.x += tree.getLocationOnScreen().x - getLocationOnScreen().x;  // Koordinaten im ConnectionPanel
        r.y += tree.getLocationOnScreen().y - getLocationOnScreen().y;
        ConnectorIcon icon = getConnectorIcon(p, tree == rightTree, defaultColor, false);
        Point relativeStartPoint = icon.getLineStart(lineStart);
        //int offset = (!p.isInputPort()) ? r.height / 2 - 1 : 0;  // An Spitze oder in Vertiefung ansetzen?
        //int xpos = selFromRight ? r.x + offset : r.x + r.width - offset - 4; // Linker oder rechter Baum
        int xpos = tree == leftTree ? (r.x + r.width - icon.getIconWidth() + relativeStartPoint.x) : (r.x + icon.getIconWidth() + 1 - relativeStartPoint.x);
        int ypos = relativeStartPoint.y;
        if (NIMBUS_LOOK_AND_FEEL) {
            xpos -= (tree == leftTree) ? 5 : 2;
            ypos += 2;
        }

        // xpos an den Rand setzen, falls es Baum sonst überschreitet
        if (tree == leftTree) {
            xpos = Math.min(xpos, r2.x + r2.width + leftTree.getLocationOnScreen().x - getLocationOnScreen().x);
        } else {
            xpos = Math.max(xpos, r2.x + rightTree.getLocationOnScreen().x - getLocationOnScreen().x);
        }

        return new Point(xpos, r.y + ypos);
    }

    /**
     * Can the two ports be connected?
     * (may be overridden)
     *
     * @param o1 port 1
     * @param o2 port 2
     * @return Answer
     */
    protected boolean canConnect(TreePortWrapper o1, TreePortWrapper o2) {
        WidgetPort<?> wp = null;
        TreePortWrapper other = null;
        if ((o1 instanceof WidgetPort<?> && o2 instanceof WidgetPort<?>) || o1 == null || o2 == null) {
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
        if (wp.isInputPort()) {
            return wp.getPort().mayConnectTo(other.getPort(), false);
        } else {
            return other.getPort().mayConnectTo(wp.getPort(), false);
        }
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
            miRemoveConnections.setEnabled(getTreeNodeFromPos((MJTree<TreePortWrapper>)e.getSource()) != null);
            TreePortWrapper tnp = getTreePortWrapperFromPos(popupOnRight ? rightTree : leftTree);
            if (tnp instanceof WidgetPort<?>) {
                WidgetPort<?> wp = (WidgetPort<?>)tnp;
                miCopyUID.setEnabled(false);
                miCopyLinks.setEnabled(wp.getConnectionLinks().size() > 0);
                miShowPartner.setEnabled(miCopyLinks.isEnabled() && wp.getConnectionPartners().size() > 0);
            } else {
                miCopyUID.setEnabled(!popupOnRight && (tnp instanceof Uid));
                miCopyLinks.setEnabled(false);
                miShowPartner.setEnabled(false);
            }
            return;
        }

        saveLastMousePos(e);
        List<TreePortWrapper> hypo = null;
        if (startPoints.size() > 0) {
            hypo = hypotheticalConnection();
        }

        if (hypo == null) {
            if (startPoints.size() > 0) {
                JTree otherTree = selectionFromRight ? leftTree : rightTree;
                otherTree.clearSelection();
                ((GuiTreeCellRenderer)otherTree.getCellRenderer()).showSelectionAfter(0);
            }
        } else {
            // connect
            MJTree<TreePortWrapper> selTree = selectionFromRight ? rightTree : leftTree;
            List<TreePortWrapper> srcNodes = selTree.getSelectedObjects();
            for (int i = 0; i < srcNodes.size(); i++) {
                connect(srcNodes.get(i), hypo.get(i));
            }
            leftTree.setSelectedObjects(null, true);
            rightTree.setSelectedObjects(null, true);
            parent.addUndoBufferEntry("Connect");
        }
        startPoints.clear();
        lastMousePos = null;
        repaint();
    }

    /**
     * Connect two ports
     * (may be overridden)
     *
     * @param port Port 1
     * @param port2 Port 2
     */
    protected void connect(TreePortWrapper port, TreePortWrapper port2) {
        if (port == null || port2 == null) {
            return;
        }
        WidgetPort<?> wp = (WidgetPort<?>)((port instanceof WidgetPort<?>) ? port : port2);
        PortWrapper other = ((wp == port) ? port2 : port);
        wp.connectTo(other);
    }

    public void mouseDragged(MouseEvent e) {
        if (startPoints.size() > 0) {
            saveLastMousePos(e);
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
        super.paintChildren(g);
        //leftTree.set
        //new Thread().set

        if (startPoints.size() > 0 && lastMousePos != null) {
            List<TreePortWrapper> drawFat = hypotheticalConnection();
            if (drawFat == null) {
                for (TreePortWrapper p : startPoints) {
                    drawLine(g, getLineStartPoint(startPointTree, p, ConnectorIcon.LineStart.Default), lastMousePos, Color.BLACK, false, false);
                }
            } else {
                MJTree<TreePortWrapper> otherTree = selectionFromRight ? leftTree : rightTree;
                //((GuiTreeCellRenderer)otherTree.getCellRenderer()).showSelectionAfter(0);
                for (int i = 0; i < startPoints.size(); i++) {
                    if (drawFat.get(i) != null) {
                        Point p1 = getLineStartPoint(startPointTree, startPoints.get(i), getLineStartPosition(startPoints.get(i), drawFat.get(i)));
                        Point p2 = getLineStartPoint(otherTree, drawFat.get(i), getLineStartPosition(drawFat.get(i), startPoints.get(i)));
                        drawLine(g, p1, p2, selectedColor.color, true, false);
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
    protected void drawConnectionsHelper(Graphics g, MJTree<TreePortWrapper> fromTree, MJTree<TreePortWrapper> toTree) {
        if (!showRightTree) {
            return;
        }

        Rectangle visible = this.getVisibleRect();

        // count connections first, to decide if transparent drawing is possible
        List<TreePortWrapper> temp = fromTree.getVisibleObjects();
        int count = 0;
        boolean transparent = true;
        for (TreePortWrapper port : temp) {
            count += getConnectionPartners(port).size();
            if (count > MAXTRANSPARENTCONNECTIONS) {
                transparent = false;
                break;
            }
        }

        for (TreePortWrapper port : temp) {
            for (PortWrapper port2 : getConnectionPartners(port)) {
                if (port2 != null) {
                    if (toTree.isVisible((TreePortWrapper)port2)) {
                        Point p1 = getLineStartPoint(fromTree, port, getLineStartPosition(port, port2));
                        Point p2 = getLineStartPoint(toTree, (TreePortWrapper)port2, getLineStartPosition(port2, port));
                        Color c = connectedColor.color;
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
     * Get ports in other tree that provided port is connected to
     * (may be overridden)
     *
     * @param port Port
     * @return List of ports that port is connected to
     */
    protected List<PortWrapper> getConnectionPartners(TreePortWrapper port) {
        return ((WidgetPort<?>)port).getConnectionPartners();
    }

    private void drawLine(Graphics g, Point p1, Point p2, Color c, boolean fat, boolean transparency) {
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

    private List<TreePortWrapper> hypotheticalConnection() {

        MJTree<TreePortWrapper> selTree = selectionFromRight ? rightTree : leftTree;
        MJTree<TreePortWrapper> otherTree = selectionFromRight ? leftTree : rightTree;

        TreePortWrapper tn = getTreePortWrapperFromPos(otherTree);

        // can connect to this node?
        List<TreePortWrapper> nodesToConnect = selTree.getSelectedObjects();
        boolean canConnect = false;
        for (TreePortWrapper srcNode : nodesToConnect) {
            canConnect |= canConnect(tn, srcNode);
        }
        if (!canConnect) {
            //System.out.println("4");
            return null;
        }

        // Find "best" connection
        List<TreePortWrapper> potentialPartners = otherTree.getVisibleObjects();
        //System.out.println("visible objects: " + potentialPartners.size());
        int startElement = potentialPartners.indexOf(tn);
        assert(startElement >= 0);
        List<TreePortWrapper> result = new ArrayList<TreePortWrapper>();
        for (TreePortWrapper srcNode : nodesToConnect) {
            int i = startElement;
            TreePortWrapper partner = null;
            do {
                TreePortWrapper potentialPartner = potentialPartners.get(i);
                if (canConnect(srcNode, potentialPartner)) {
                    if (!result.contains(potentialPartner)) {
                        partner = potentialPartners.get(i);
                        break;
                    }
                }
                i = (i + 1) % potentialPartners.size();
            } while (i != startElement);
            result.add(partner);
        }
        assert result.size() == nodesToConnect.size();
        return result;
    }

    public TreeNode getTreeNodeFromPos(MJTree<TreePortWrapper> otherTree) {
        return getTreeNodeFromPos(otherTree, lastMousePos);
    }

    public TreePortWrapper getTreePortWrapperFromPos(MJTree<TreePortWrapper> otherTree) {
        return getTreePortWrapperFromPos(otherTree, lastMousePos);
    }

    public TreeNode getTreeNodeFromPos(MJTree<TreePortWrapper> otherTree, Point pos) {

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
        return (TreeNode)tp.getLastPathComponent();
    }

    public TreePortWrapper getTreePortWrapperFromPos(MJTree<TreePortWrapper> otherTree, Point pos) {
        TreeNode tn = getTreeNodeFromPos(otherTree, pos);
        if (!(tn instanceof TreePortWrapper)) {
            //System.out.println("3");
            return null;
        }
        return (TreePortWrapper)tn;
    }

    public void actionPerformed(ActionEvent e) {
        MJTree<TreePortWrapper> ptree = popupOnRight ? rightTree : leftTree;
        if (e.getSource() == miSelectAll) {
            ptree.setSelectedObjects(ptree.getObjects(), true);
            ptree.expandAll(20);
        } else if (e.getSource() == miSelectNone) {
            ptree.setSelectedObjects(null, true);
        } else if (e.getSource() == miSelectVisible) {
            ptree.setSelectedObjects(ptree.getVisibleObjects(), true);
        } else if (e.getSource() == miRemoveConnections) {
            TreePortWrapper tnp = getTreePortWrapperFromPos(ptree);
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
            for (TreePortWrapper wptnp : rightTree.getObjects()) {
                removeConnections(wptnp);
                /*WidgetPort<?> wp = (WidgetPort<?>)wptnp;
                wp.clearConnections();*/
            }
            parent.addUndoBufferEntry("Remove all connections");
            repaint();
        } else if (e.getSource() == miCopyUID) {
            TreePortWrapper tnp = getTreePortWrapperFromPos(ptree);
            String uid = "Element has no UID";
            if (tnp instanceof Uid) {
                uid = ((Uid)tnp).getUid();
            }
            Clipboard clipboard = getToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(uid), null);
        } else if (e.getSource() == miCopyLinks) {
            TreePortWrapper tnp = getTreePortWrapperFromPos(ptree);
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
            TreePortWrapper tnp = getTreePortWrapperFromPos(ptree);
            WidgetPort<?> wp = (WidgetPort<?>)tnp;
            List<PortWrapper> partners = wp.getConnectionPartners();
            for (PortWrapper partner : partners) {
                TreePath tp = leftTree.getTreePathFor((TreePortWrapper)partner);
                leftTree.scrollPathToVisible(tp);
            }
        }
    }

    /**
     * Remove all connections from specified port
     * (may be overridden)
     *
     * @param tnp Port
     */
    protected void removeConnections(TreePortWrapper tnp) {
        if (tnp instanceof WidgetPort<?>) {
            WidgetPort<?> wp = (WidgetPort<?>)tnp;
            wp.clearConnections();
        } else {
            for (TreePortWrapper wptnp : rightTree.getObjects()) {
                WidgetPort<?> wp = (WidgetPort<?>)wptnp;
                wp.removeConnection(tnp.getPort(), tnp.getUid());
            }
            tnp.getPort().disconnectAll(); // just to make sure...
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

    /**
     * @param listener Raw Selection listener for left tree
     */
    public void addSelectionListener(TreeSelectionListener listener) {
        leftTree.addTreeSelectionListener(listener);
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

    /**
     * Background color for non-ports nodes
     * (may be overridden)
     *
     * @param value Wrapped object
     * @return Color - null, if default color
     */
    protected Color getBranchBackgroundColor(Object value) {
        if (value instanceof Widget && ((Widget)value).getParent().getSelection().contains(value)) {
            return Color.YELLOW;
        }
        return null;
    }

    /**
     * Text color for non-ports nodes
     * (may be overridden)
     *
     * @param value Wrapped object
     * @return Color - null, if default color
     */
    protected Color getBranchTextColor(Object value) {
        return null;
    }

    /**
     * Draw port connected (green) in tree?
     */
    public boolean drawPortConnected(TreePortWrapper port) {
        return port.getPort().isConnected();
    }

    /**
     * @param port Port
     * @param rightTree Icon for right tree?
     * @param color Icon Color
     * @param boolean brighter Brighter icon color?
     * @param iconHeight Icon height
     * @return Connector icon for specified parameters
     */
    public ConnectorIcon getConnectorIcon(TreePortWrapper port, boolean rightTree, ConnectorIcon.IconColor color, boolean brighter) {
        final ConnectorIcon.Type iconType = new ConnectorIcon.Type();
        boolean rpc = FinrocTypeInfo.isMethodType(port.getPort().getDataType(), true);
        boolean leftTreeRPCServerPort = rpc && port.getPort().getFlag(PortFlags.ACCEPTS_DATA);
        iconType.set(port.isInputPort() && (!leftTreeRPCServerPort), (!rpc) && port.getPort().getFlag(PortFlags.PROXY), rpc, rightTree, brighter, color, rightTree ? rightBackgroundColor : leftBackgroundColor);
        return ConnectorIcon.getIcon(iconType, HEIGHT);
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
        private final JTree parent;

        /** Default renderer for JTree */
        private final DefaultTreeCellRenderer defaultRenderer;

        /** Reference to connection panel */
        private final ConnectionPanel panel;

        public GuiTreeCellRenderer(JTree parent, boolean rightTree, ConnectionPanel panel) {
            setBackgroundNonSelectionColor(defaultColor.color);
            backgroundColor = rightTree ? rightBackgroundColor : leftBackgroundColor;
            this.panel = panel;
            setBorderSelectionColor(selectedColor.color);
            setTextSelectionColor(backgroundColor.color);
            setTextNonSelectionColor(backgroundColor.color);
            setBackgroundSelectionColor(selectedColor.color);
            defaultRenderer = new DefaultTreeCellRenderer();
            defaultRenderer.setBackgroundNonSelectionColor(backgroundColor.color);
            this.rightTree = rightTree;
            this.parent = parent;
            timer = new Timer(1000, this);
            timer.stop();
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (!(value instanceof TreePortWrapper)) {
                Color bg = panel.getBranchBackgroundColor(value);
                Color fg = panel.getBranchTextColor(value);
                if (bg != null) {
                    defaultRenderer.setBackgroundNonSelectionColor(bg);
                    defaultRenderer.setBackground(bg);
                } else {
                    defaultRenderer.setBackgroundNonSelectionColor(backgroundColor.color);
                    defaultRenderer.setBackground(backgroundColor.color);
                }
                if (fg != null) {
                    defaultRenderer.setTextNonSelectionColor(fg);
                } else {
                    defaultRenderer.setTextNonSelectionColor(Color.black);
                }
                defaultRenderer.setOpaque(ConnectionPanel.NIMBUS_LOOK_AND_FEEL && bg != null);
                return defaultRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            }


            //JLabel result = new JLabel(value.toString());
            //if (sel) {
            //  result.setBorder(BorderFactory.createLineBorder(selectedBorder));
            //}
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,    row, hasFocus);
            //setBackground(color);
            boolean portSelected = (!timer.isRunning() && sel);
            TreePortWrapper port = (TreePortWrapper)value;
            ConnectorIcon.IconColor color = portSelected ? selectedColor : (panel.drawPortConnected(port) ? connectedColor : (port.getPort().hasLinkEdges() ? connectionPartnerMissingColor : defaultColor));
            Color c = color.color;
            boolean brighter = false;
            if (panel.mouseOver != null) {
                boolean mouseOver = (port.getPort() == panel.mouseOver.getPort()) || port.getPort().isConnectedTo(panel.mouseOver.getPort());
                if (mouseOver) {
                    c = c.brighter();
                    brighter = true;
                }
            }
            setBackgroundSelectionColor(c);
            setBackgroundNonSelectionColor(c);

            setIconTextGap(0);
            if (!rightTree) {
                setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            //this.setHorizontalTextPosition(SwingConstants.RIGHT);
            //setOpaque(true);
            /*result.setFont(c.getFont());
            result.setForeground(sel ? selected : background);*/
            ConnectionPanel.HEIGHT = getPreferredSize().height;
            setIcon(panel.getConnectorIcon(port, rightTree, color, brighter));
            return this;
        }

        @Override
        public void paint(Graphics g) {

            if (ConnectionPanel.NIMBUS_LOOK_AND_FEEL) {
                if (super.selected) {
                    g.setColor(backgroundColor.color);
                    g.setClip(-getX(), 0, parent.getWidth(), getHeight());
                    g.fillRect(-getX(), 0, parent.getWidth(), getHeight());
                }

                // copied from super class to ensure that background is filled
                g.setColor(super.selected ? getBackgroundSelectionColor() : getBackgroundNonSelectionColor());
                if (getComponentOrientation().isLeftToRight()) {
                    g.fillRect(-1, 0, getWidth() - 4, getHeight());
                } else {
                    g.fillRect(0, 0, getWidth() - 5, getHeight());
                }
            }

            setForeground(Color.white);
            super.paint(g);
            Color temp = g.getColor();
            g.setColor(backgroundColor.color);
            g.drawLine(0, 0, getWidth() - 1, 0);
            if (ConnectionPanel.NIMBUS_LOOK_AND_FEEL) {
                g.drawLine(0, 1, getWidth() - 1, 1);
                g.fillRect(getWidth() - 4, 0, 5, getHeight());
                g.fillRect(-1, 0, 1, getHeight());
            }
            g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
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
