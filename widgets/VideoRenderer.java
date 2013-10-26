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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import java.lang.Math;

import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;
import javax.swing.event.MouseInputListener;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.commons.fastdraw.BufferedImageRGB;
import org.finroc.tools.gui.util.gui.FileDialog;
import org.finroc.tools.gui.util.gui.MAction;
import org.finroc.tools.gui.util.gui.MActionEvent;
import org.finroc.tools.gui.util.gui.MToolBar;
import org.finroc.plugins.data_types.Blittable;
import org.finroc.plugins.data_types.HasBlittable;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;

public class VideoRenderer extends Widget {

    enum Mode {SaveToFile, SaveToPort}

    enum ScaleMode { scaleFast, scaleSmooth, scaleAreaAverage };

    /** UID */
    private static final long serialVersionUID = -8452479527434504700L;

    public WidgetInput.Std<HasBlittable> videoInput;

    public WidgetOutput.Std<org.finroc.plugins.data_types.mca.Image> videoSelection;

    public WidgetOutput.Numeric imageCounter;

    /** Image index in image source - in case we receive lists of blittables */
    public int imageIndexInSource;

    /** Shows or hides the tool bar*/
    private boolean hideToolbar = true;

    /** Activates image scaling */
    private boolean scaleImage = false;

    /** Indicates whether to keep image aspect ration or not */
    private boolean keepAspectRatio = true;

    private ScaleMode scaleMode = ScaleMode.scaleFast;

    private java.lang.String fileExtension = "png";

    @Override
    protected WidgetUI createWidgetUI() {
        return new VideoWindowUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (forPort == videoSelection) {
            return suggestion.derive(org.finroc.plugins.data_types.mca.Image.TYPE);
        }
        if (forPort == videoInput) {
            return suggestion.derive(HasBlittable.TYPE);
        }
        return null;
    }

    class VideoWindowUI extends WidgetUI implements PortListener<HasBlittable>, MouseInputListener, ActionListener {

        MToolBar toolbar;

        private BufferedImageRGB temp_image = null;

        private Point marking_start_point;

        private Point marking_current_point;

        private Point marking_end_point;

        private boolean paint_marking = false;

        private Mode current_mode = Mode.SaveToFile;

        /** Dimension of last blitted image */
        private int lastWidth, lastHeight;

        private int image_counter = 0;

        public VideoWindowUI() {
            super(RenderMode.Cached);
            this.setLayout(new BorderLayout());
            videoInput.addChangeListener(this);
            //      videoSelection.addChangeListener(this);

            addMouseListener(this);
            addMouseMotionListener(this);

            toolbar = new MToolBar("GeometryWidget Control", MToolBar.VERTICAL);
            //            toolbar.addToggleButton(new MAction(Mode.Normal, "arrow.png", "Point Mode", this, Cursor.HAND_CURSOR));
            toolbar.addToggleButton(new MAction(Mode.SaveToFile, "document-save.png", "Save Image To File", this, Cursor.CROSSHAIR_CURSOR));
            toolbar.addToggleButton(new MAction(Mode.SaveToPort, "to-port.png", "Save Image to Port", this, Cursor.CROSSHAIR_CURSOR));

            toolbar.setSelected(Mode.SaveToFile);
            add(toolbar, BorderLayout.WEST);
            toolbar.setVisible(!hideToolbar);
        }

        public void actionPerformed(ActionEvent ae) {
            Enum<?> e = ((MActionEvent)ae).getEnumID();
            if (e instanceof Mode) {
                //      renderer.setCursor(Cursor.getPredefinedCursor((Integer)((MActionEvent)ae).getCustomData()));
                current_mode = (Mode) e;
                toolbar.setSelected(e);
            }
        }

        public void widgetPropertiesChanged() {
            toolbar.setVisible(!hideToolbar);
        }

        /** UID */
        private static final long serialVersionUID = -922703839059777637L;

        @Override
        protected void renderToCache(BufferedImageRGB cache, Dimension renderSize, boolean resized) throws OperationNotSupportedException {
            HasBlittable b = videoInput.getAutoLocked();
            if (b == null || imageIndexInSource >= b.getNumberOfBlittables()) {
                cache.fill(0);
                releaseAllLocks();
                return;
            }
            Blittable bl = b.getBlittable(imageIndexInSource);
            if (bl.getWidth() != lastWidth || bl.getHeight() != lastHeight) {
                cache.fill(0);
                lastWidth = bl.getWidth();
                lastHeight = bl.getHeight();
            }

            if (scaleImage) {
                BufferedImageRGB input_image = new BufferedImageRGB(bl.getWidth(), bl.getHeight());
                bl.blitTo(input_image);

                bl = this.getScaledInstance(input_image, renderSize, scaleMode, keepAspectRatio);
            }

            bl.blitTo(cache, new Rectangle(renderSize));

            releaseAllLocks();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (this.paint_marking) {
                int x = (int) this.marking_start_point.getX();
                int y = (int) this.marking_start_point.getY();
                int width = (int) Math.abs(x - this.marking_current_point.getX());
                int height = (int) Math.abs(y - this.marking_current_point.getY());
                if (x > (int) this.marking_current_point.getX()) {
                    x = (int) this.marking_current_point.getX();
                }
                if (y > (int) this.marking_current_point.getY()) {
                    y = (int) this.marking_current_point.getY();
                }

                g.drawRect(x, y, width, height);
            }
        }

        @Override
        public void portChanged(AbstractPort origin, HasBlittable value) {
            this.setChanged();
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {}
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                this.marking_start_point = e.getPoint();
                this.marking_current_point = e.getPoint();
                this.paint_marking = true;
            }
        }
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        @Override
        public void mouseDragged(MouseEvent e) {
            // trigger repaint
            if (this.paint_marking) {
                this.setChanged();
                repaint();
                this.marking_current_point = e.getPoint();
            }
        }
        @Override
        public void mouseMoved(MouseEvent e) {}

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON2) {
                HasBlittable b = videoInput.getAutoLocked();
                if (b == null || imageIndexInSource >= b.getNumberOfBlittables() || (b.getBlittable(imageIndexInSource).getHeight() <= 0 && b.getBlittable(imageIndexInSource).getWidth() <= 0)) {
                    releaseAllLocks();
                    return;
                }

                Blittable bl = b.getBlittable(imageIndexInSource);
                BufferedImageRGB image = new BufferedImageRGB(bl.getWidth(), bl.getHeight());
                bl.blitTo(image);
                File f = FileDialog.showSaveDialog("Save Image as...", fileExtension);
                if (f != null) {
                    try {
                        ImageIO.write(image.getBufferedImage(), fileExtension, f);
                    } catch (IOException e1) {
                        getRoot().getFingui().showErrorMessage(e1);
                    }
                }

                releaseAllLocks();
            }

            if (e.getButton() == MouseEvent.BUTTON1) {
                this.paint_marking = true;
                this.marking_end_point = e.getPoint();

                // remove markings again
                this.paint_marking = false;
                this.setChanged();
                repaint();

                // compute selection rectangle
                int x = (int) this.marking_start_point.getX();
                int y = (int) this.marking_start_point.getY();
                int width = (int) Math.abs(x - this.marking_end_point.getX());
                int height = (int) Math.abs(y - this.marking_end_point.getY());
                if (x > (int) this.marking_end_point.getX()) {
                    x = (int) this.marking_end_point.getX();
                }
                if (y > (int) this.marking_end_point.getY()) {
                    y = (int) this.marking_end_point.getY();
                }
                //Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);

                // create and render image from JPanel for export
                BufferedImageRGB image_selection = new BufferedImageRGB(width, height);
                Graphics2D g_region = image_selection.getBufferedImage().createGraphics();
                g_region.translate(-x, -y);
                this.printAll(g_region);

                // save image if applicable
                if (image_selection != null) {
                    if (current_mode == Mode.SaveToFile) {
                        File f = FileDialog.showSaveDialog("Save Image Selection as...", fileExtension);
                        if (f != null) {
                            try {
                                ImageIO.write(image_selection.getBufferedImage(), fileExtension, f);
                            } catch (IOException e1) {
                                getRoot().getFingui().showErrorMessage(e1);
                            }
                        }
                    }

                    if (current_mode == Mode.SaveToPort) {
                        org.finroc.plugins.data_types.mca.Image output_image = videoSelection.getUnusedBuffer();

                        System.out.println("retrieved buffer: " + output_image.getWidth() + ", " + output_image.getHeight());

                        output_image.setImageDataRGB32(image_selection.getWidth(), image_selection.getHeight(), image_selection.getBuffer());
                        videoSelection.publish(output_image);
                        imageCounter.publish(image_counter++);

                        System.out.println("exporting image " + image_counter + " via port (" + output_image.getWidth() + ", " + output_image.getHeight() + ")");
                    }
                }
            }
        }

        private BufferedImageRGB getScaledInstance(BufferedImageRGB input_image, Dimension renderSize, ScaleMode scaleMode, boolean keepAspectRatio) {
            int scale_mode = Image.SCALE_FAST;

            if (scaleMode == ScaleMode.scaleFast) {
                scale_mode = Image.SCALE_FAST;
            }
            if (scaleMode == ScaleMode.scaleSmooth) {
                scale_mode = Image.SCALE_SMOOTH;
            }
            if (scaleMode == ScaleMode.scaleAreaAverage) {
                scale_mode = Image.SCALE_AREA_AVERAGING;
            }
            Image scaled_image = null;
            if (keepAspectRatio) {

                double x_aspect = (double) input_image.getWidth() / (double) renderSize.width;
                double y_aspect = (double) input_image.getHeight() / (double) renderSize.height;

                if (x_aspect > y_aspect) {
                    scaled_image = input_image.getBufferedImage().getScaledInstance(renderSize.width,
                                   -1,
                                   scale_mode);

                } else {
                    scaled_image = input_image.getBufferedImage().getScaledInstance(-1,
                                   renderSize.height,
                                   scale_mode);

                }
            } else {
                scaled_image = input_image.getBufferedImage().getScaledInstance(renderSize.width,
                               renderSize.height,
                               scale_mode);
            }

            if ((temp_image == null) ||
                    (temp_image.getWidth() != renderSize.width) ||
                    (temp_image.getHeight() != renderSize.height)) {
                temp_image = new BufferedImageRGB(renderSize.width,
                                                  renderSize.height);
            }

            Graphics g = temp_image.getBufferedImage().createGraphics();
            g.drawImage(scaled_image, 0, 0, null);
            g.dispose();

            return temp_image;
        }

    }
}

