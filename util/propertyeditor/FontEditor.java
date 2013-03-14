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
package org.finroc.tools.gui.util.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.connectina.swing.fontchooser.JFontChooser;

/**
 * @author Max Reichardt
 *
 */
public class FontEditor extends PropertyEditComponent<Font> implements ActionListener {

    /** UID */
    private static final long serialVersionUID = 45745748726499512L;

    private JButton button;

    private Font curFont;

    protected void createAndShow() throws Exception {
        curFont = getCurWidgetValue();
        button = new JButton(curFont.getFontName());
        button.addActionListener(this);
        button.setEnabled(isModifiable());
        add(button, BorderLayout.WEST);
    }

    @Override
    public Font getCurEditorValue() {
        return curFont;
    }


    public void actionPerformed(ActionEvent e) {
        Container c = this.getParent();
        while (!(c instanceof Window)) {
            c = c.getParent();
        }
        Font newFont = JFontChooser.showDialog((Window)c, curFont);
        if (newFont != null) {
            curFont = newFont;
            button.setText(curFont.getFontName());
        }
    }

    @Override
    protected void valueUpdated(Font t) {
        button.setText(curFont.getFontName());
    }
}
