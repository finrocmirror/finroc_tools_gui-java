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

import java.awt.BorderLayout;
import java.awt.Dimension;


import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.finroc.plugin.datatype.StringList;

/**
 * @author max
 *
 */
public class StringListEditor extends PropertyEditComponent<StringList> {

    /** UID */
    private static final long serialVersionUID = 958511015637260235L;

    private JTextArea curStrings;

    protected void createAndShow() {
        curStrings = new JTextArea();
        curStrings.setText(getCurWidgetValue().toString());
        JPanel jp = new JPanel();
        jp.setBorder(BorderFactory.createTitledBorder(getPropertyName()));
        jp.setLayout(new BorderLayout());
        jp.setPreferredSize(new Dimension(LABELWIDTH + TEXTFIELDWIDTH, 128));
        jp.add(new JScrollPane(curStrings), BorderLayout.CENTER);
        add(jp);
    }

    @Override
    public StringList getCurEditorValue() {
        return new StringList(curStrings.getText());
    }
}
