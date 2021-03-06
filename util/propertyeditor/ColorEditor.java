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
package org.finroc.tools.gui.util.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.naming.OperationNotSupportedException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

import org.finroc.tools.gui.commons.fastdraw.BufferedImageRGB;


/**
 * @author Max Reichardt
 *
 */
public class ColorEditor extends PropertyEditComponent<Color> implements ActionListener {

    /** UID */
    private static final long serialVersionUID = 2486687318726499512L;

    private JButton button;

    private BufferedImageRGB icon;

    private Color curColor;

    protected void createAndShow() throws Exception {
        button = new JButton("");
        button.setEnabled(isModifiable());
        icon = new BufferedImageRGB(10, 10);
        valueUpdated(getCurWidgetValue());
        button.addActionListener(this);
        add(button, BorderLayout.WEST);
    }

    @Override
    public void createAndShowMinimal(Color c) throws OperationNotSupportedException {
        button = new JButton("");
        button.setEnabled(isModifiable());
        icon = new BufferedImageRGB(10, 10);
        valueUpdated(c);
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

    @Override
    protected void valueUpdated(Color t) {
        setButtonColor(t);
    }
}
