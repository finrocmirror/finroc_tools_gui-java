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
package org.finroc.tools.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import org.finroc.core.plugin.ExternalConnection;

public class StatusBar extends JPanel {

    /** UID */
    private static final long serialVersionUID = 5649680331936258749L;

    /** Elements to show status */
    private JLabel leftText, rightText, color;

    /** Elements of connections status color */
    private final float[] colorGood = new float[3], colorBad = new float[3], colorMix = new float[3];

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

        add(Box.createRigidArea(new Dimension(5, 0)));
        add(leftText);
        add(Box.createHorizontalGlue());
        add(rightText);
        add(Box.createRigidArea(new Dimension(5, 0)));
        add(color);
        add(Box.createRigidArea(new Dimension(5, 0)));

        Color.GREEN.getColorComponents(colorGood);
        Color.DARK_GRAY.getColorComponents(colorBad);
        setStatus(new ArrayList<ExternalConnection>());
    }

    public void setStatus(List<ExternalConnection> connections) {

        // left part: basic status
        int connectedCount = 0;
        for (ExternalConnection ioi : connections) {
            if (ioi.isConnected()) {
                connectedCount++;
            }
        }

        if (connectedCount <= 0) {
            leftText.setText("not connected");
            color.setBackground(Color.RED);
            rightText.setText("");
            return;
        } else {
            if (connections.size() > 0) {
                leftText.setText("connected (" + connectedCount + "/" + connections.size() + ")");
            } else {
                leftText.setText("connected");
            }
        }

        // right part
        String status = "";
        float worstQuality = 1.0f;
        float bestQuality = -1.0f;

        for (ExternalConnection ec : connections) {
            if (ec.isConnected()) {
                float q = ec.getConnectionQuality();
                worstQuality = Math.min(worstQuality, q);
                bestQuality = Math.max(bestQuality, q);
                status += (status.length() == 0 ? "" : "; ") + ec.getStatus(true);
            }
        }

        // set color to worst connection status
        float f0 = 1 - worstQuality;
        float f1 = worstQuality;
        for (int i = 0; i < 3; i++) {
            colorMix[i] = f0 * colorBad[i] + f1 * colorGood[i];
        }
        Color mix = new Color(colorMix[0], colorMix[1], colorMix[2]);
        color.setBackground(mix);

        rightText.setText(status);
        repaint();
    }
}
