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
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.finroc.gui.abstractbase.DataModelBase;
import org.finroc.gui.abstractbase.DataModelListener;
import org.finroc.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.gui.util.gui.MJTree;
import org.finroc.gui.util.treemodel.PortWrapper;
import org.finroc.gui.util.treemodel.TreePortWrapper;
import org.finroc.gui.util.treemodel.Uid;


/**
 * @author max
 *
 * This panel is used to connect the panel's to the widgets
 */
public class ConnectionPanel extends JPanel implements ComponentListener, DataModelListener, MouseInputListener, ActionListener {

    /** UID */
    private static final long serialVersionUID = -4672134128946142093L;

    /** for optimized drawing */
    private static int MAXTRANSPARENTCONNECTIONS = 3;

    /** left and right tree */
    private MJTree<TreePortWrapper> leftTree;
    private MJTree<TreePortWrapper> rightTree;
    private JScrollPane leftScrollPane;
    private JScrollPane rightScrollPane;

    /** parent window */
    private GUIWindowUI parent;

    /** temporary variables for UI behaviour */
    private boolean selectionFromRight;
    private List<Point> startPoints = new ArrayList<Point>();
    private Point lastMousePos;

    /** PopupMenu */
    private JPopupMenu popupMenu;
    private boolean popupOnRight;
    JMenuItem miSelectAll, miSelectVisible, miSelectNone, miExpandAll, miCollapseAll, miRemoveConnections, miRefresh, miRemoveAllConnections, miCopyUID;

    public ConnectionPanel(GUIWindowUI win, Font treeFont) {

        setLayout(new GridLayout(1, 0));
        parent = win;

        // Setup all the scrolling stuff
        leftTree = new MJTree<TreePortWrapper>(TreePortWrapper.class, 3);
        rightTree = new MJTree<TreePortWrapper>(WidgetPort.class, 2);
        setPreferredSize(new Dimension(400, 0));
        setMinimumSize(new Dimension(300, 0));
        rightTree.setBackground(new Color(242, 242, 255));
        rightTree.setFocusTraversalKeysEnabled(false);
        rightTree.addKeyListener(win);
        leftTree.setFocusTraversalKeysEnabled(false);
        leftTree.addKeyListener(win);
        leftTree.setRepaintDelegate(this);
        rightTree.setRepaintDelegate(this);
        leftScrollPane = new JScrollPane(leftTree);
        rightScrollPane = new JScrollPane(rightTree);
        leftScrollPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        leftScrollPane.setBorder(BorderFactory.createEmptyBorder());
        rightScrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(leftScrollPane, BorderLayout.WEST);
        add(rightScrollPane, BorderLayout.CENTER);

        // setup renderer
        leftTree.setCellRenderer(new GuiTreeCellRenderer(leftTree, leftTree.getBackground(), false));
        rightTree.setCellRenderer(new GuiTreeCellRenderer(rightTree, rightTree.getBackground(), true));

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
        rightTree.setModel(tm);
        ((GUIPanel)tm.getRoot()).addDataModelListener(this);
    }

    public void dataModelChanged(DataModelBase<?,?,?> caller, Event event, Object param) {
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
    public void mouseMoved(MouseEvent e) {}
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
        for (TreePortWrapper p : selTree.getSelectedObjects()) {
            startPoints.add(getStartingPoint(selTree, p, selectionFromRight));
        }
    }

    private Point getStartingPoint(MJTree<TreePortWrapper> tree, TreePortWrapper p, boolean selFromRight) {
        Rectangle r2 = tree.getObjectBounds(p, true);  // Bounds vom Eintrag
        Rectangle r = tree.getObjectBounds(p, false);  // Bounds vom Eintrag

        r.x += tree.getLocationOnScreen().x - getLocationOnScreen().x;  // Koordinaten im ConnectionPanel
        r.y += tree.getLocationOnScreen().y - getLocationOnScreen().y;
        int offset = (!p.isInputPort()) ? r.height / 2 - 1 : 0;  // An Spitze oder in Vertiefung ansetzen?
        int xpos = selFromRight? r.x + offset : r.x + r.width - offset - 4;  // Linker oder rechter Baum

        // xpos an den Rand setzen, falls es Baum sonst überschreitet
        if (tree == leftTree) {
            xpos = Math.min(xpos, r2.x + r2.width + leftTree.getLocationOnScreen().x - getLocationOnScreen().x);
        } else {
            xpos = Math.max(xpos, r2.x + rightTree.getLocationOnScreen().x - getLocationOnScreen().x);
        }

        return new Point(xpos, r.y + r.height / 2);
    }

    private boolean canConnect(TreePortWrapper o1, TreePortWrapper o2) {
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
            return wp.getPort().mayConnectTo(other.getPort());
        } else {
            return other.getPort().mayConnectTo(wp.getPort());
        }
    }


    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {

        if (e.getButton() == MouseEvent.BUTTON3) {
            popupOnRight = e.getSource() == rightTree;
            Point p = ((JComponent)e.getSource()).getLocationOnScreen();
            Point p2 = getLocationOnScreen();
            popupMenu.show(this, e.getX() + p.x - p2.x, e.getY() + p.y - p2.y);
            saveLastMousePos(e);
            miRemoveConnections.setEnabled(getTreeNodeFromPos((MJTree<TreePortWrapper>)e.getSource()) != null);
            TreePortWrapper tnp = getTreeNodeFromPos(leftTree);
            miCopyUID.setEnabled(!popupOnRight && (tnp instanceof Uid));
            return;
        }

        saveLastMousePos(e);
        List<TreePortWrapper> hypo = null;
        if (startPoints.size() > 0) {
            hypo = hypotheticalConnection();
        }

        if (hypo == null) {
            if (startPoints.size() > 0) {
                JTree otherTree = selectionFromRight? leftTree : rightTree;
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

    private void connect(TreePortWrapper port, TreePortWrapper port2) {
        if (port == null || port2 == null) {
            return;
        }
        WidgetPort<?> wp = (WidgetPort<?>)((port instanceof WidgetPort<?>)? port : port2);
        PortWrapper other = ((wp == port)? port2 : port);
        wp.connectTo(other);
        /*if (wp.isInputPort()) {
            wp.getPort().connectToSource(other.getPort());
        } else {
            wp.getPort().connectToTarget(other.getPort());
        }*/
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
                for (Point p : startPoints) {
                    drawLine(g, p, lastMousePos, Color.BLACK, false, false);
                }
            } else {
                MJTree<TreePortWrapper> otherTree = selectionFromRight ? leftTree : rightTree;
                //((GuiTreeCellRenderer)otherTree.getCellRenderer()).showSelectionAfter(0);
                for (int i = 0; i < startPoints.size(); i++) {
                    if (drawFat.get(i) != null) {
                        Point p1 = startPoints.get(i);
                        Point p2 = getStartingPoint(otherTree, drawFat.get(i), !selectionFromRight);
                        drawLine(g, p1, p2, GuiTreeCellRenderer.selected, true, false);
                    }
                }
            }
        }

        // draw existing connections
        Rectangle visible = this.getVisibleRect();

        // count connections first, to decide if transparent drawing is possible
        List<TreePortWrapper> temp = rightTree.getVisibleObjects();
        int count = 0;
        boolean transparent = true;
        for (TreePortWrapper port : temp) {
            count += ((WidgetPort<?>)port).getConnectionPartners().size();
            if (count > MAXTRANSPARENTCONNECTIONS) {
                transparent = false;
                break;
            }
        }

        for (TreePortWrapper port : temp) {
            for (PortWrapper port2 : ((WidgetPort<?>)port).getConnectionPartners()) {
                if (port2 != null) {
                    if (leftTree.isVisible((TreePortWrapper)port2)) {
                        Point p1 = getStartingPoint(rightTree, port, true);
                        Point p2 = getStartingPoint(leftTree, (TreePortWrapper)port2, false);
                        if (visible.contains(p1) && visible.contains(p2)) {
                            drawLine(g, p1, p2, GuiTreeCellRenderer.color, true, transparent);
                        } else if (g.getClipBounds().intersectsLine(p1.x, p1.y, p2.x, p2.y)) {
                            drawLine(g, p1, p2, GuiTreeCellRenderer.color, false, false);
                        }
                    }
                }
            }
        }
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

        TreePortWrapper tn = getTreeNodeFromPos(otherTree);

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
                i = (i+1) % potentialPartners.size();
            } while (i != startElement);
            result.add(partner);
        }
        assert result.size() == nodesToConnect.size();
        return result;
    }

    public TreePortWrapper getTreeNodeFromPos(MJTree<TreePortWrapper> otherTree) {
        // getPosition on JTree
        Point relMousePos = new Point(lastMousePos);
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
        TreeNode tn = (TreeNode)tp.getLastPathComponent();
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
            TreePortWrapper tnp = getTreeNodeFromPos(ptree);
            if (tnp instanceof WidgetPort<?>) {
                WidgetPort<?> wp = (WidgetPort<?>)tnp;
                wp.clearConnections();
            } else {
                for (TreePortWrapper wptnp : rightTree.getObjects()) {
                    WidgetPort<?> wp = (WidgetPort<?>)wptnp;
                    wp.removeConnection(tnp.getPort(), tnp.getUid());
                }
            }
            parent.addUndoBufferEntry("Remove connections");
            repaint();
        } else if (e.getSource() == miRefresh) {
            parent.getParent().updateInterface();
        } else if (e.getSource() == miExpandAll) {
            ptree.expandAll(20);
        } else if (e.getSource() == miCollapseAll) {
            ptree.collapseAll();
        } else if (e.getSource() == miRemoveAllConnections) {
            for (TreePortWrapper wptnp : rightTree.getObjects()) {
                WidgetPort<?> wp = (WidgetPort<?>)wptnp;
                wp.clearConnections();
            }
            parent.addUndoBufferEntry("Remove all connections");
            repaint();
        } else if (e.getSource() == miCopyUID) {
            TreePortWrapper tnp = getTreeNodeFromPos(ptree);
            String uid = "Element has no UID";
            if (tnp instanceof Uid) {
                uid = ((Uid)tnp).getUid();
            }
            Clipboard clipboard = getToolkit().getSystemClipboard();
            clipboard.setContents(new StringSelection(uid), null);
        }
    }

    public void setTreeFont(Font f) {
        leftTree.setFont(f);
        rightTree.setFont(f);
    }
}

/**
 * @author max
 *
 * Tree-Cell-Renderer for the GUI-Tree
 */
class GuiTreeCellRenderer extends DefaultTreeCellRenderer implements ActionListener {

    public static final Color color = new Color(100, 100, 200);
    private Color background;
    //private static Color selectedBorder = new Color(240, 140, 20);
    public static final Color selected = new Color(255, 30, 30);

    private static Map<String,Icon> iconCache = new HashMap<String,Icon>();
    private boolean rightTree;

    /** for showing selection after some time */
    private Timer timer;

    private JTree parent;

    /** UID */
    private static final long serialVersionUID = 4342216001043562115L;

    private DefaultTreeCellRenderer defaultRenderer;

    public GuiTreeCellRenderer(JTree parent, Color background, boolean rightTree) {
        setBackgroundNonSelectionColor(color);
        this.background = background;
        setBorderSelectionColor(selected);
        setTextSelectionColor(background);
        setTextNonSelectionColor(background);
        setBackgroundSelectionColor(selected);
        defaultRenderer = new DefaultTreeCellRenderer();
        defaultRenderer.setBackgroundNonSelectionColor(background);
        this.rightTree = rightTree;
        this.parent = parent;
        timer = new Timer(1000, this);
        timer.stop();
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (!(value instanceof TreePortWrapper)) {
            if (value instanceof Widget && ((Widget)value).getParent().getSelection().contains(value)) {
                defaultRenderer.setBackgroundNonSelectionColor(Color.YELLOW);
            } else {
                defaultRenderer.setBackgroundNonSelectionColor(background);
            }
            return defaultRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,   row, hasFocus);
        }


        //JLabel result = new JLabel(value.toString());
        //if (sel) {
        //  result.setBorder(BorderFactory.createLineBorder(selectedBorder));
        //}
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,    row, hasFocus);
        //setBackground(color);
        setBackgroundSelectionColor((!timer.isRunning() && sel)? selected : color);
        setIconTextGap(0);
        if (!rightTree) {
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        //this.setHorizontalTextPosition(SwingConstants.RIGHT);
        //setOpaque(true);
        /*result.setFont(c.getFont());
        result.setForeground(sel ? selected : background);*/
        setIcon(createInputIcon(getPreferredSize().height, !((TreePortWrapper)value).isInputPort(), rightTree, !timer.isRunning() && sel));
        /*return result;*/
        return this;
    }



    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Color temp = g.getColor();
        g.setColor(background);
        g.drawLine(0, 0, getWidth() - 1, 0);
        g.drawLine(0, getHeight() - 1, getWidth() - 1, getHeight() - 1);
        g.setColor(temp);
    }

    private Icon createInputIcon(int height, boolean input, boolean front, boolean sel) {
        String key = new Boolean(input).toString() + new Boolean(front).toString() + new Boolean(sel).toString() + height;
        Icon temp = iconCache.get(key);
        if (temp != null) {
            return temp;
        }

        int backgroundTemp = background.getRGB();
        int colorTemp = sel ? selected.getRGB() : color.getRGB();

        BufferedImageRGB img = new BufferedImageRGB(height / 2 + 1, height);
        img.drawFilledRectangle(img.getBounds(), input? colorTemp : backgroundTemp);

        // Dreieck malen
        int[] buffer = img.getBuffer();
        int top = 0;
        int bottom = (img.getHeight() - 1) * img.getWidth();
        int count = 1;
        while (top <= bottom) {
            for (int i = 0; i < count; i++) {
                buffer[top + i] = input ? backgroundTemp : colorTemp;
                buffer[bottom + i] = input ? backgroundTemp : colorTemp;
            }
            top += img.getWidth();
            bottom -= img.getWidth();
            count++;
        }

        // Bei Bedarf spiegeln
        if (front != input) {
            img.mirrorLeftRight();
        }

        ImageIcon ii = new ImageIcon(img.getBufferedImage());
        iconCache.put(key, ii);
        return ii;
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
