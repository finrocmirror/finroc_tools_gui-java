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
package org.finroc.tools.gui.widgets;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.text.NumberFormat;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetPorts;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.commons.Util;
import org.finroc.tools.gui.themes.Theme;
import org.finroc.tools.gui.themes.Themes;
import org.finroc.tools.gui.util.embeddedfiles.EmbeddedPaintable;
import org.finroc.tools.gui.util.embeddedfiles.EmbeddedPaintables;
import org.finroc.tools.gui.util.gui.AdvancedMouseListener;
import org.finroc.tools.gui.util.gui.MAction;
import org.finroc.tools.gui.util.gui.MActionEvent;
import org.finroc.tools.gui.util.gui.MPopupMenu;
import org.finroc.tools.gui.util.gui.MToolBar;
import org.finroc.tools.gui.util.gui.MouseEventListener;
import org.finroc.tools.gui.util.gui.RulerOfTheForest;
import org.finroc.tools.gui.util.propertyeditor.NotInPropertyEditor;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;
import org.rrlib.serialization.NumericRepresentation;
import org.finroc.plugins.data_types.Angle;
import org.finroc.plugins.data_types.Matrix3x3d;
import org.finroc.plugins.data_types.Paintable;
import org.finroc.plugins.data_types.PaintablePortData;
import org.finroc.plugins.data_types.Pose2D;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;
import org.finroc.core.port.ThreadLocalCache;

public class GeometryRenderer extends Widget {

    /** UID */
    private static final long serialVersionUID = -27724571246923200L;

    /** number of edges for each map object */
    public static final int MAP_OBJECT_EDGE_COUNT = 4;

    /** Geometry to render */
    public WidgetPorts<WidgetInput.Std<PaintablePortData>> geometry;
    public WidgetPorts<WidgetInput.CC<Matrix3x3d>> geometryTransformations;
    public WidgetPorts<WidgetInput.Numeric> objectCoordinates;
    public WidgetPorts<WidgetInput.CC<Pose2D>> objectPoses;
    public WidgetOutput.Numeric clickX;
    public WidgetOutput.Numeric clickY;
    public WidgetOutput.Numeric clickCounter;
    public WidgetOutput.CC<Pose2D> clickPose;

    /** Parameters */
    public int numberOfGeometries = 3;
    public boolean invertYAxis;
    public double zoom = 100;
    public double translationX = 0;
    public double translationY = 0;
    public double rotation = 0;
    public boolean antialiasing = false;
    public double lineWidth = 1;
    public boolean showRulers = true;
    public boolean showCoordinates = false;
    @NotInPropertyEditor
    public transient boolean invertObjectYInput = false;
    public boolean resetClickPosOnMouseRelease = false;
    public boolean hideToolbar = false;
    public EmbeddedPaintables mapObjects = new EmbeddedPaintables();
    public int drawGeometriesAfterMapObject = 2;
    public boolean provideGeometryPoseInputs = false;

    public GeometryRenderer() {
        geometry = new WidgetPorts<WidgetInput.Std<PaintablePortData>>("geometry", 3, WidgetInput.Std.class, this);
        geometryTransformations = new WidgetPorts<WidgetInput.CC<Matrix3x3d>>("", 0, WidgetInput.CC.class, this);
        objectCoordinates = new WidgetPorts<WidgetInput.Numeric>("", 0, WidgetInput.Numeric.class, this);
        objectPoses = new WidgetPorts<WidgetInput.CC<Pose2D>>("", 0, WidgetInput.CC.class, this);
    }

    @Override
    protected void setDefaultColors() {
        setBackground(getDefaultColor(Theme.DefaultColor.GEOMETRY_BACKGROUND));
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new GeometryRendererUI();
    }

    enum Action { Point, Move, Zoom, Rotate, ResetPoint, Home, MouseOver, Center, Watch, Deselect, WheelZoom }
    enum Mode { Normal, Move, Zoom, Rotate}

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (geometry != null && geometry.contains(forPort)) {
            return suggestion.derive(PaintablePortData.TYPE);
        } else if (forPort == clickPose) {
            PortCreationInfo info = suggestion.derive(Pose2D.TYPE);
            info.setFlag(FrameworkElementFlags.NO_INITIAL_PUSHING, true);
            return info;
        } else if (objectPoses != null && objectPoses.contains(forPort)) {
            return suggestion.derive(Pose2D.TYPE);
        } else if (geometryTransformations != null && geometryTransformations.contains(forPort)) {
            return suggestion.derive(Matrix3x3d.TYPE);
        }
        return suggestion;
    }

    protected class GeometryRendererUI extends WidgetUI implements PortListener<PaintablePortData>, ActionListener, MouseEventListener<Action>, ComponentListener {

        Renderer renderer;
        AdvancedMouseListener<Mode, Action> listener;
        protected MToolBar toolbar;
        AffineTransform transform = new AffineTransform();
        RulerOfTheForest leftRuler, topRuler;
        RulerOfTheForest.RulerLabel rulerLabel;
        JLabel cordBar;
        NumberFormat nf = NumberFormat.getInstance();
        EmbeddedPaintable trackObject = null;  // object to track
        Point lastMousePressPoint;
        Point lastMouseDragPoint;

        public GeometryRendererUI() {
            super(RenderMode.Swing);
            this.setLayout(new BorderLayout());
            JPanel centerPanel = new JPanel();
            cordBar = new JLabel(" ");
            cordBar.setBorder(BorderFactory.createEtchedBorder());
            cordBar.setHorizontalAlignment(JLabel.CENTER);
            cordBar.setFont(cordBar.getFont().deriveFont(Font.PLAIN, 10));
            centerPanel.setLayout(new BorderLayout());
            centerPanel.setOpaque(useOpaquePanels());
            renderer = new Renderer();
            renderer.addComponentListener(this);
            centerPanel.add(renderer, BorderLayout.CENTER);
            add(centerPanel, BorderLayout.CENTER);
            centerPanel.add(cordBar, BorderLayout.SOUTH);

            // init mouse listener
            listener = new AdvancedMouseListener<Mode, Action>();
            renderer.addMouseListener(listener);
            renderer.addMouseMotionListener(listener);
            renderer.addMouseWheelListener(listener);
            listener.addMouseAction(AdvancedMouseListener.EventType.PressDragMove, AdvancedMouseListener.Button.Left, Mode.Normal, Action.Point, this);
            listener.addMouseAction(AdvancedMouseListener.EventType.Release, AdvancedMouseListener.Button.Left, Mode.Normal, Action.ResetPoint, this);
            listener.addMouseAction(AdvancedMouseListener.EventType.DragMove, AdvancedMouseListener.Button.Left, Mode.Rotate, Action.Rotate, this);
            listener.addMouseAction(AdvancedMouseListener.EventType.DragMove, AdvancedMouseListener.Button.Left, Mode.Zoom, Action.Zoom, this);
            listener.addMouseAction(AdvancedMouseListener.EventType.DragMove, AdvancedMouseListener.Button.Left, Mode.Move, Action.Move, this);
            listener.addMouseAction(AdvancedMouseListener.EventType.DragMove, AdvancedMouseListener.Button.Middle, null, Action.Zoom, this);
            listener.addMouseAction(AdvancedMouseListener.EventType.Wheel, null, null, Action.WheelZoom, this);
            listener.addMouseAction(AdvancedMouseListener.EventType.MouseOver, null, null, Action.MouseOver, this);
            listener.setMode(Mode.Normal);

            // create Toolbar
            toolbar = new MToolBar("GeometryWidget Control", MToolBar.VERTICAL);
            toolbar.addToggleButton(new MAction(Mode.Normal, "arrow.png", "Point Mode", this, Cursor.HAND_CURSOR));
            toolbar.addToggleButton(new MAction(Mode.Move, "move.png", "Move Mode", this, Cursor.MOVE_CURSOR));
            toolbar.addToggleButton(new MAction(Mode.Zoom, "zoom.png", "Zoom Mode", this, Cursor.NW_RESIZE_CURSOR));
            toolbar.addToggleButton(new MAction(Mode.Rotate, "rotate.png", "Rotation Mode", this, Cursor.HAND_CURSOR));
            toolbar.add(new MAction(Action.Home, "home.png", "Center origin", this));
            toolbar.addToggleButton(new MAction(Action.Center, "find.png", "Center object", this), true);
            toolbar.addToggleButton(new MAction(Action.Watch, "watch.png", "Watch object", this), true);
            toolbar.setSelected(Mode.Normal);
            renderer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add(toolbar, BorderLayout.WEST);

            // create ruler
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
            topPanel.setOpaque(useOpaquePanels());
            leftRuler = new RulerOfTheForest(SwingConstants.VERTICAL, 14);
            topRuler = new RulerOfTheForest(SwingConstants.HORIZONTAL, 11);
            rulerLabel = new RulerOfTheForest.RulerLabel(RulerOfTheForest.RulerLabel.Position.NO_ETCHED_BORDER);
            topPanel.add(topRuler, BorderLayout.CENTER);
            topPanel.add(rulerLabel, BorderLayout.WEST);
            centerPanel.add(topPanel, BorderLayout.NORTH);
            centerPanel.add(leftRuler, BorderLayout.WEST);
            updateRulers();

            cordBar.setBackground(toolbar.getBackground());
            widgetPropertiesChanged();
        }

        @SuppressWarnings("rawtypes")
        public void actionPerformed(ActionEvent ae) {
            Enum e = ((MActionEvent)ae).getEnumID();
            if (e instanceof Mode) {
                renderer.setCursor(Cursor.getPredefinedCursor((Integer)((MActionEvent)ae).getCustomData()));
                listener.setMode((Mode)e);
                toolbar.setSelected(e);
                return;
            } else if (e == Action.Home) { // Home-Button
                translationX = 0;
                translationY = 0;
                rotation = 0;
                updateRulers();
                stopTracking();
                renderer.repaint();
            } else if (e == Action.Center || e == Action.Watch) {
                EmbeddedPaintable mapObject = (EmbeddedPaintable)((MActionEvent)ae).getCustomData();

                if (mapObject == null) {
                    // no object selected => show Popup Menu for selection
                    MPopupMenu mpm = new MPopupMenu();
                    for (EmbeddedPaintable ep : mapObjects) {
                        mpm.add(new MAction(e, null, ep.getName(), this, ep));
                    }
                    AbstractButton ab = toolbar.getButton(e);
                    ab.setSelected(true);
                    mpm.setCancelAction(new MAction(Action.Deselect, this, e));
                    mpm.show(toolbar, ab.getX() + ab.getWidth(), ab.getY());
                } else if (e.equals(Action.Center)) {
                    // center object
                    int index = mapObjects.indexOf(mapObject) * MAP_OBJECT_EDGE_COUNT;
                    translationX = -objectCoordinates.get(index).getDouble();
                    translationY = invertObjectYInput ? objectCoordinates.get(index + 1).getDouble() : -objectCoordinates.get(index + 1).getDouble();
                    toolbar.setSelected(e, false);
                    renderer.repaint();
                } else if (e.equals(Action.Watch)) {
                    // track object
                    trackObject = mapObject;
                    renderer.repaint();
                }
            } else if (e == Action.Deselect) { // Deselect button (when PopupMenu was cancelled)
                Enum o = (Enum)((MActionEvent)ae).getCustomData();
                toolbar.setSelected(o, false);
                if (o == Action.Watch) {
                    stopTracking();
                }
            }
        }

        public void stopTracking() {
            trackObject = null;
            toolbar.setSelected(Action.Watch, false);
        }

        public void mouseEvent(Action id, AdvancedMouseListener <? , Action > source, MouseEvent me) {
            switch (id) {
            case Point:
                    lastMouseDragPoint = me.getPoint();
                if (resetClickPosOnMouseRelease) { // old behaviour
                    Point2D transformed = getPoint(lastMouseDragPoint);
                    clickX.publish(transformed.getX());
                    clickY.publish(transformed.getY());
                    clickCounter.publish(clickCounter.getDouble() + 1);
                    Log.log(LogLevel.DEBUG, "Target pose " + transformed.toString());
                } else {
                    if (me.getID() == MouseEvent.MOUSE_PRESSED) {
                        lastMousePressPoint = lastMouseDragPoint;
                    }
                    repaint();
                }
                break;
            case ResetPoint:
                if (resetClickPosOnMouseRelease) {
                    clickX.publish(0);
                    clickY.publish(0);
                } else {
                    Point2D transformedPress = getPoint(lastMousePressPoint);
                    Point2D transformedRelease = getPoint(lastMouseDragPoint);

                    clickX.publish(transformedPress.getX());
                    clickY.publish(transformedPress.getY());
                    clickCounter.publish(clickCounter.getDouble() + 1);

                    double yaw = Math.atan2(transformedRelease.getY() - transformedPress.getY(), transformedRelease.getX() - transformedPress.getX());
                    Pose2D publish = new Pose2D();
                    publish.x = transformedPress.getX();
                    publish.y = transformedPress.getY();
                    publish.yaw = yaw;
                    clickPose.publish(publish);
                    Log.log(LogLevel.DEBUG, "Target pose " + publish.toString());

                    lastMousePressPoint = null;
                }
                break;
            case WheelZoom:
                stopTracking();
                zoom *= Math.pow(0.97, source.getLastWheelMove() * 1.5);
                renderer.repaint();
                updateRulers();
                break;
            case Zoom:
                stopTracking();
                double diff = source.getDiffToLastPosX() + source.getDiffToLastPosY();
                zoom *= Math.pow(0.97, diff);
                renderer.repaint();
                updateRulers();
                break;
            case Rotate:
                stopTracking();
                //diff = source.getDiffToLastPosX() + source.getDiffToLastPosY();
                Point2D center = new Point2D.Double(getRenderWidth() / 2, getRenderHeight() / 2);
                Point2D curPoint = source.getLastPos();
                double curAngle = Math.atan2(center.getY() - curPoint.getY(), center.getX() - curPoint.getX());
                Point2D lastPoint = source.getSecondLastPos();
                double lastAngle = Math.atan2(center.getY() - lastPoint.getY(), center.getX() - lastPoint.getX());
                diff = curAngle - lastAngle;
                if (invertYAxis) {
                    rotation += diff;
                } else {
                    rotation -= diff;// / 180;
                }
                renderer.repaint();
                updateRulers();
                break;
            case Move:
                stopTracking();
                transform.setToIdentity();
                transform.scale(zoom, invertYAxis ? zoom : -zoom);
                transform.rotate(rotation);
                try {
                    //transform.invert();
                    Util.invert(transform);
                } catch (NoninvertibleTransformException e) {
                    Log.log(LogLevel.ERROR, e);
                }
                Point2D temp = new Point2D.Double(source.getDiffToLastPosX(), source.getDiffToLastPosY());
                temp = transform.transform(temp, null);
                translationX += temp.getX();
                translationY += temp.getY();
                renderer.repaint();
                updateRulers();
                break;
            case MouseOver:
                temp = getPoint(me.getPoint());
                cordBar.setText("(" + nf.format(temp.getX()) + ", " + nf.format(temp.getY()) + ")");
                break;
            }
        }

        public void updateRulers() {

            // calculate distance from edges to origin
            Point2D topleft = getPoint(new Point(0, 0));
            Point2D bottomright = getPoint(new Point(renderer.getWidth() - 1, renderer.getHeight() - 1));
            Point2D topright = getPoint(new Point(renderer.getWidth() - 1, 0));
            Point2D bottomleft = getPoint(new Point(0, renderer.getHeight() - 1));

            double top = Line2D.ptLineDist(topleft.getX(), topleft.getY(), topright.getX(), topright.getY(), 0, 0);
            double bottom = Line2D.ptLineDist(bottomleft.getX(), bottomleft.getY(), bottomright.getX(), bottomright.getY(), 0, 0);
            double left = Line2D.ptLineDist(topleft.getX(), topleft.getY(), bottomleft.getX(), bottomleft.getY(), 0, 0);
            double right = Line2D.ptLineDist(topright.getX(), topright.getY(), bottomright.getX(), bottomright.getY(), 0, 0);
            if (Math.abs(top - bottom) < topleft.distance(bottomleft) - 0.1) {
                top = -top;
            } else if (top > bottom) {
                top = -top;
                bottom = -bottom;
            }
            if (Math.abs(left - right) < topleft.distance(topright) - 0.1) {
                left = -left;
            } else if (left > right) {
                left = -left;
                right = -right;
            }

            if (invertYAxis) {
                leftRuler.setMinAndMax(bottom, top);
            } else {
                leftRuler.setMinAndMax(-bottom, -top);
            }
            topRuler.setMinAndMax(left, right);
            topRuler.setVisible(showRulers);
            leftRuler.setVisible(showRulers);
            rulerLabel.setVisible(showRulers);
            cordBar.setVisible(showCoordinates);
        }

        public void componentResized(ComponentEvent e) {
            updateRulers();
        }

        /**
         * Transforms screen point from GeometryWidget back to world coordinates
         *
         * @param p screen Point
         * @return Point in world coordinates
         */
        public Point2D getPoint(Point p) {
            transform.setToIdentity();
            transform.translate(renderer.getSize().width / 2, renderer.getSize().height / 2);

            // Apply widget translation/rotation/zoom
            transform.scale(zoom, invertYAxis ? zoom : -zoom);
            transform.rotate(rotation);
            transform.translate(translationX, translationY);
            try {
                //transform.invert();  // only in Java 1.6
                Util.invert(transform);
            } catch (NoninvertibleTransformException e) {
                Log.log(LogLevel.ERROR, e);
            }
            Point2D.Double temp = new Point2D.Double(p.x, p.y);
            return transform.transform(temp, null);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void widgetPropertiesChanged() {
            //setBackground(GeometryRenderer.this.getBackground());
            renderer.setBackground(GeometryRenderer.this.getBackground());
            toolbar.setVisible(!hideToolbar);

            if (Themes.nimbusLookAndFeel()) {
                Color c = getLabelColor(GeometryRenderer.this);
                Color b = getBackground();
                leftRuler.setOpaque(false);
                leftRuler.setBackground(b);
                leftRuler.setForeground(c);
                topRuler.setBackground(b);
                topRuler.setOpaque(false);
                topRuler.setForeground(c);
                rulerLabel.setOpaque(false);
                rulerLabel.setBackground(b);
                rulerLabel.setForeground(c);
            }

            if (objectPoses == null) {
                objectPoses = new WidgetPorts<WidgetInput.CC<Pose2D>>("", 0, WidgetInput.CC.class, GeometryRenderer.this);
            }

            // Init Input Ports
            while (geometry.size() < numberOfGeometries) {
                WidgetInput.Std<PaintablePortData> p = new WidgetInput.Std<PaintablePortData>();
                geometry.add(p);
                p.setDescription("geometry " + geometry.size());
            }
            while (geometry.size() > numberOfGeometries) {  // remove last entries
                geometry.remove(geometry.size() - 1);
            }
            while (provideGeometryPoseInputs && geometryTransformations.size() < numberOfGeometries) {
                WidgetInput.CC<Matrix3x3d> p = new WidgetInput.CC<Matrix3x3d>();
                geometryTransformations.add(p);
                p.setDescription("geometry " + geometryTransformations.size() + " transformation");
            }
            while (geometryTransformations.size() > numberOfGeometries || ((!provideGeometryPoseInputs) && geometryTransformations.size() > 0)) {  // remove last entries
                geometryTransformations.remove(geometryTransformations.size() - 1);
            }
            while (objectCoordinates.size() / MAP_OBJECT_EDGE_COUNT < mapObjects.size()) {
                objectCoordinates.add(new WidgetInput.Numeric());
            }
            while (objectPoses.size() < mapObjects.size()) {
                objectPoses.add(new WidgetInput.CC<Pose2D>());
            }
            while ((objectCoordinates.size() + 2) / MAP_OBJECT_EDGE_COUNT > mapObjects.size()) {
                objectCoordinates.remove(objectCoordinates.size() - 1);
            }
            while (objectPoses.size() > mapObjects.size()) {
                objectPoses.remove(objectPoses.size() - 1);
            }
            // update port names
            for (int i = 0; i < mapObjects.size(); i++) {
                String name = mapObjects.get(i).getName();
                objectCoordinates.get(i * MAP_OBJECT_EDGE_COUNT).setDescription(name + " x");
                objectCoordinates.get(i * MAP_OBJECT_EDGE_COUNT + 1).setDescription(name + " y");
                objectCoordinates.get(i * MAP_OBJECT_EDGE_COUNT + 2).setDescription(name + " yaw");
                objectCoordinates.get(i * MAP_OBJECT_EDGE_COUNT + 3).setDescription(name + " hidden");
                objectPoses.get(i).setDescription(name + " pose");
            }

            try {
                initPorts();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // register as listener as inputports
            for (WidgetInput.Std<PaintablePortData> wp : geometry) {
                wp.addChangeListener(this);
            }
            for (WidgetInput.Numeric wp : objectCoordinates) {
                wp.addChangeListener(renderer);
            }
            for (WidgetInput.CC<Pose2D> wp : objectPoses) {
                wp.addChangeListener(renderer);
            }

            stopTracking();

            updateRulers();
        }

        /** UID */
        private static final long serialVersionUID = -922703839059777637L;

        @Override
        public void portChanged(AbstractPort origin, PaintablePortData value) {
            renderer.repaint();
        }

        public void drawGeometries(Graphics2D g2d) {

            // Draw geometries
            for (int i = 0; i < geometry.size(); i++) {
                Paintable p = geometry.get(i).getAutoLocked();
                if (p == null) {
                    continue;
                }
                Matrix3x3d transformation = null;
                if (i < geometryTransformations.size() && geometryTransformations.get(i).asPort().isConnected()) {
                    transformation = geometryTransformations.get(i).getAutoLocked();
                    Graphics2D g = ((Graphics2D)g2d.create());
                    g.transform(new AffineTransform(transformation.values[0], transformation.values[3], transformation.values[1],
                                                    transformation.values[4], transformation.values[2], transformation.values[5]));
                    p.paint(g, null);
                    g.dispose();
                } else {
                    p.paint(g2d, null);
                }
            }
            ThreadLocalCache.get().releaseAllLocks();
        }


        @SuppressWarnings("rawtypes")
        /*@Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            Paintable p = geometry.getValue();
            if (p == null) {
                return;
            }
            b.blitTo(cache, new Rectangle(renderSize));
        }*/

        class Renderer extends JPanel implements PortListener {

            /** UID */
            private static final long serialVersionUID = -6466458277650614547L;

            public Renderer() {
                setOpaque(true);

            }

            double getObjectXCoordinate(int index) {
                if (objectCoordinates.get(index).asPort().isConnected()) {
                    return objectCoordinates.get(index).getDouble();
                } else
                    return objectPoses.get(index / MAP_OBJECT_EDGE_COUNT).getAutoLocked().x;
            }

            double getObjectYCoordinate(int index) {
                if (objectCoordinates.get(index).asPort().isConnected()) {
                    return objectCoordinates.get(index).getDouble();
                } else
                    return objectPoses.get((index - 1) / MAP_OBJECT_EDGE_COUNT).getAutoLocked().y;
            }

            double getObjectYawAngle(int index) {
                if (objectCoordinates.get(index).asPort().isConnected()) {
                    NumericRepresentation angleObject = objectCoordinates.get(index).getAutoLocked();
                    if (angleObject instanceof Angle) {
                        return ((Angle)angleObject).getSignedRad();
                    } else {
                        return angleObject.getNumericRepresentation().doubleValue();
                    }
                } else
                    return objectPoses.get((index - 2) / MAP_OBJECT_EDGE_COUNT).getAutoLocked().yaw;
            }

            @Override
            protected void paintComponent(Graphics g) {

                // Draw background
                super.paintComponent(g);

                // Update translation to tracked object
                if (toolbar.isSelected(Action.Watch) && trackObject != null) {
                    int index = mapObjects.indexOf(trackObject) * MAP_OBJECT_EDGE_COUNT;
                    if (index >= 0) {
                        translationX = -getObjectXCoordinate(index);
                        translationY = invertObjectYInput ? getObjectYCoordinate(index + 1) : -getObjectYCoordinate(index + 1);
                    } else {
                        stopTracking();
                    }
                } else if (trackObject != null) {
                    stopTracking();
                }

                // Move center to center of widget
                Graphics2D g2d = (Graphics2D)g.create();
                g2d.translate(this.getSize().width / 2, this.getSize().height / 2);

                // Apply widget translation/rotation/zoom
                g2d.scale(zoom, invertYAxis ? zoom : -zoom);
                g2d.rotate(rotation);
                g2d.translate(translationX, translationY);
                if (antialiasing) {
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                }
                if (lineWidth != 1.0) {
                    g2d.setStroke(new BasicStroke((float)lineWidth));
                }

                // Draw objects
                boolean geometriesDrawn = false;
                for (int i = 0; i < mapObjects.size(); i++) {

                    // draw geometry (?)
                    if (i == drawGeometriesAfterMapObject) {
                        drawGeometries(g2d);
                        geometriesDrawn = true;
                    }

                    EmbeddedPaintable p = mapObjects.get(i);
                    if (p == null || objectCoordinates.size() < i * MAP_OBJECT_EDGE_COUNT + 1) {
                        continue;
                    }
                    if (objectCoordinates.get(i * MAP_OBJECT_EDGE_COUNT + 3).getDouble() != 0.0) {
                        continue;
                    }
                    Graphics2D temp = ((Graphics2D)g2d.create());
                    temp.translate(getObjectXCoordinate(i * MAP_OBJECT_EDGE_COUNT), getObjectYCoordinate(i * MAP_OBJECT_EDGE_COUNT + 1));
                    temp.rotate(getObjectYawAngle(i * MAP_OBJECT_EDGE_COUNT + 2));
                    if (!invertYAxis) {
                        temp.scale(1, -1);
                    }
                    p.paintToCenter(temp, getRoot().getEmbeddedFileManager());
                    temp.dispose();
                }

                // if geometriesDrawn-parameter is invalid, draw geometries after objects
                if (geometriesDrawn == false) {
                    drawGeometries(g2d);
                }

                g2d.dispose();

                // draw mouse drag line
                if (lastMousePressPoint != null && lastMouseDragPoint != null && (!lastMousePressPoint.equals(lastMouseDragPoint))) {
                    g.setColor(Color.black);
                    g.drawLine(lastMousePressPoint.x, lastMousePressPoint.y, lastMouseDragPoint.x, lastMouseDragPoint.y);
                }

                ThreadLocalCache.get().releaseAllLocks();
            }

            @Override
            public void portChanged(AbstractPort origin, Object value) {
                repaint();
            }
        }

        public void componentHidden(ComponentEvent e) {     }
        public void componentMoved(ComponentEvent e) {      }
        public void componentShown(ComponentEvent e) {      }
    }
}
