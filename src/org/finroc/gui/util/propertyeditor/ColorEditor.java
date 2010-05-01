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
package org.finroc.gui.util.propertyeditor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.naming.OperationNotSupportedException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

import org.finroc.gui.commons.fastdraw.BufferedImageRGB;


/**
 * @author max
 *
 */
public class ColorEditor extends PropertyEditComponent<Color> implements ActionListener {

    /** UID */
    private static final long serialVersionUID = 2486687318726499512L;

    private JButton button;

    private BufferedImageRGB icon;

    private Color curColor;

    protected void createAndShow() {
        button = new JButton("");
        icon = new BufferedImageRGB(10, 10);
        setButtonColor(getCurWidgetValue());
        button.addActionListener(this);
        createStdLayoutWith(button);
    }

    @Override
    public void createAndShowMinimal(Color c) throws OperationNotSupportedException {
        button = new JButton("");
        icon = new BufferedImageRGB(10, 10);
        setButtonColor(c);
        button.addActionListener(this);
        add(button);
    }

    private void setButtonColor(Color curWidgetValue) {
        if (curWidgetValue == null) {
            curWidgetValue = new Color(0, 0, 0);
        }
        icon.drawFilledRectangle(icon.getBounds(), curWidgetValue.getRGB());
        button.setIcon(new ImageIcon(icon.getBufferedImage()));
        curColor = curWidgetValue;
    }

    @Override
    public Color getCurEditorValue() {
        return curColor;
    }


    public void actionPerformed(ActionEvent e) {
        Color c = JColorChooser.showDialog(this, "Choose Color (" + getPropertyName() + ")", curColor);
        if (c != null) {
            setButtonColor(c);
        }
    }
}
