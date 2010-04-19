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

import java.awt.Dimension;

import javax.naming.OperationNotSupportedException;
import javax.swing.JTextField;

/**
 * @author max
 *
 */
public class StringEditor extends PropertyEditComponent<String> {

    /** UID */
    private static final long serialVersionUID = 2486687318726499512L;

    private JTextField jtf;

    protected void createAndShow() {
        jtf = new JTextField();
        jtf.setText(getCurWidgetValue());
        jtf.setMinimumSize(new Dimension(TEXTFIELDWIDTH, jtf.getPreferredSize().height));
        //jtf.setPreferredSize(new Dimension(TEXTFIELDWIDTH, jtf.getPreferredSize().height));
        createStdLayoutWith(jtf);
    }

    @Override
    public void createAndShowMinimal(String s) throws OperationNotSupportedException {
        jtf = new JTextField();
        jtf.setText(s);
        jtf.setMinimumSize(new Dimension(TEXTFIELDWIDTH, jtf.getPreferredSize().height));
        //jtf.setPreferredSize();
        add(jtf);
    }

    @Override
    public String getCurEditorValue() {
        return jtf.getText();
    }

}
