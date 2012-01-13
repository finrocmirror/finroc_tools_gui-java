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
import java.awt.image.BufferedImage;
import java.awt.Graphics;

import javax.imageio.ImageIO;
import javax.naming.OperationNotSupportedException;
import javax.swing.event.MouseInputListener;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
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

  enum ScaleMode { scaleFast, scaleSmooth, scaleAreaAverage };

    /** UID */
    private static final long serialVersionUID = -8452479527434504700L;

    public WidgetInput.Std<HasBlittable> videoInput;

    /** Image index in image source - in case we receive lists of blittables */
    public int imageIndexInSource;

    /** Shows or hides the tool bar*/
    private boolean hideToolbar = true;

    /** Activates image scaling */
    private boolean scaleImage = false;

    /** Indicates whether to keep image aspect ration or not */
    private boolean keepAspectRatio = true;

  private ScaleMode scaleMode = ScaleMode.scaleFast;

    @Override
    protected WidgetUI createWidgetUI() {
        return new VideoWindowUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(HasBlittable.TYPE);
    }

  enum Action { SaveSingleImage }
  //  enum Mode { Normal, Move, Zoom, Rotate}


  class VideoWindowUI extends WidgetUI implements PortListener<HasBlittable>, MouseInputListener, ActionListener {

        MToolBar toolbar;

        /** Dimension of last blitted image */
        private int lastWidth, lastHeight;

        public VideoWindowUI() {
            super(RenderMode.Cached);
            this.setLayout(new BorderLayout());
            videoInput.addChangeListener(this);
            addMouseListener(this);
            addMouseMotionListener(this);

            toolbar = new MToolBar("GeometryWidget Control", MToolBar.VERTICAL);
	    //            toolbar.addToggleButton(new MAction(Mode.Normal, "arrow.png", "Point Mode", this, Cursor.HAND_CURSOR));
	    toolbar.add(new MAction(Action.SaveSingleImage, "document-save.png", "Save Image", this));

            add(toolbar, BorderLayout.WEST);
        }

        public void actionPerformed(ActionEvent ae) {
	  Enum e = ((MActionEvent)ae).getEnumID();

	  if (e == Action.SaveSingleImage) {
	    System.out.println ("save image.\n");
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

	      bl = this.getScaledInstance (input_image, renderSize, scaleMode, keepAspectRatio);
	    }

	    bl.blitTo (cache, new Rectangle(renderSize));
            releaseAllLocks();
        }

        @Override
        public void portChanged(AbstractPort origin, HasBlittable value) {
            this.setChanged();
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {}
        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        @Override
        public void mouseDragged(MouseEvent e) {}
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
                File f = FileDialog.showSaveDialog("Save Image as...", "png");
                if (f != null) {
                    try {
                        ImageIO.write(image.getBufferedImage(), "png", f);
                    } catch (IOException e1) {
                        getRoot().getFingui().showErrorMessage(e1);
                    }
                }

                releaseAllLocks();
            }
        }

	private BufferedImageRGB getScaledInstance (BufferedImageRGB input_image, Dimension renderSize, ScaleMode scaleMode, boolean keepAspectRatio) {
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

	    }
	    else {
	      scaled_image = input_image.getBufferedImage().getScaledInstance(-1,
									      renderSize.height, 
									      scale_mode);

	    }
	  }
	  else {
	    scaled_image = input_image.getBufferedImage().getScaledInstance(renderSize.width,
									    renderSize.height, 
									    scale_mode);
	  }
	      
	  BufferedImageRGB output_image = new BufferedImageRGB(renderSize.width,
							       renderSize.height);
	      
	  Graphics g = output_image.getBufferedImage().createGraphics();
	  g.drawImage(scaled_image, 0, 0, null);
	  g.dispose();

	  return output_image;
	}

    }
}

