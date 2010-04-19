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

import java.awt.Color;
import java.awt.Dimension;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

public class StatusBar extends JPanel {

    /** UID */
    private static final long serialVersionUID = 5649680331936258749L;

    /** Elements to show status */
    private JLabel leftText, rightText, color;

    public StatusBar() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        leftText = new JLabel("");
        rightText = new JLabel("SDFSDG");
        color = new JLabel("");
        color.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        color.setOpaque(true);
        color.setPreferredSize(new Dimension(9, 9));
        color.setMaximumSize(new Dimension(9, 9));

        add(Box.createRigidArea(new Dimension(5,0)));
        add(leftText);
        add(Box.createHorizontalGlue());
        add(rightText);
        add(Box.createRigidArea(new Dimension(5,0)));
        add(color);
        add(Box.createRigidArea(new Dimension(5,0)));

        setStatus("", 0, 0);
    }

    public void setStatus(String handlerAddress, int connectedCount, int interfaceCount) {
        if (connectedCount <= 0) {
            leftText.setText("not connected");
            color.setBackground(Color.RED);
        } else {
            if (interfaceCount > 0) {
                leftText.setText("connected (" + connectedCount + "/" + interfaceCount + ")");
            } else {
                leftText.setText("connected");
            }
            color.setBackground(Color.GREEN);
        }
        rightText.setText(handlerAddress);
        repaint();
    }
}
