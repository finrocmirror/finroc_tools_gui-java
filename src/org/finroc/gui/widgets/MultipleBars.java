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
package org.finroc.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetPorts;
import org.finroc.gui.WidgetPortsListener;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.themes.Themes;
import org.finroc.gui.util.propertyeditor.PropertyList;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;


public class MultipleBars extends Widget {

    /** UID */
    private static final long serialVersionUID = -6261570734653832685L;

    PropertyList<BarProperty> bars = new PropertyList<BarProperty>(BarProperty.class, 25);
    WidgetPorts<WidgetInput.Numeric> inputs = new WidgetPorts<WidgetInput.Numeric>("input", 2, WidgetInput.Numeric.class, this);
    Color barBackground = Themes.getCurTheme().standardBackground().brighter();

    public MultipleBars() {
        // 2 bars by default
        bars.add(new BarProperty());
        bars.add(new BarProperty());
    }

    @Override
    protected void setDefaultColors() {
        setBackground(Themes.getCurTheme().standardBackground());
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new MultipleBarsUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort, WidgetPorts<?> collection) {
        return suggestion;
    }

    /** Properties of one bar */
    public static class BarProperty implements Serializable {

        /** UID */
        private static final long serialVersionUID = 6464808920773228472L;

        String label = "name";
        double min = 0;
        double max = 1;
        Color color = Themes.getCurTheme().lcdEnabled();
    }

    class MultipleBarsUI extends WidgetUI implements WidgetPortsListener, ComponentListener {

        /** UID */
        private static final long serialVersionUID = -6743184196516792815L;

        JPanel labelPanel, barPanel;
        List<JPanel> barPanels = new ArrayList<JPanel>();

        public MultipleBarsUI() {
            super(RenderMode.Swing);

            // create panels
            setLayout(new BorderLayout());
            labelPanel = new JPanel();
            barPanel = new JPanel();
            barPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), BorderFactory.createLineBorder(Color.BLACK)));
            //barPanel.setBackground(barBackground);
            labelPanel.setBorder(BorderFactory.createEmptyBorder(1, 3, 3, 3));
            add(barPanel, BorderLayout.CENTER);
            add(labelPanel, BorderLayout.SOUTH);
            addComponentListener(this);
            inputs.addChangeListener(this);
            widgetPropertiesChanged();
        }

        @Override
        public void portChanged(WidgetPorts<?> origin, AbstractPort port, Object value) {
            for (int i = 0; i < inputs.size(); i++) {
                BarProperty bp = bars.get(i);
                JPanel bar = barPanels.get(i);
                double relValue = (inputs.get(i).getDouble() - bp.min) / (bp.max - bp.min);
                relValue = Math.max(0, Math.min(1, relValue)); // make sure it's between 0 and 1
                double relValueInverse = 1 - relValue;
                int heightInverse = (int)(((double)bar.getHeight()) * relValueInverse);
                bar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(heightInverse, i == 0 ? 6 : 3, 0, i == inputs.size() - 1 ? 6 : 3), BorderFactory.createLineBorder(Color.BLACK)));
                bar.setBackground(barBackground);
            }
            repaint();
        }

        @Override
        public void widgetPropertiesChanged() {

            // adjust number of ports
            inputs.setSize(bars.size());
            initPorts();

            // set layouts and create bars
            barPanel.setLayout(new GridLayout(1, bars.size()));
            labelPanel.setLayout(new GridLayout(1, bars.size()));
            barPanel.removeAll();
            labelPanel.removeAll();
            barPanels.clear();
            for (BarProperty bp : bars) {
                JPanel bar = new JPanel();
                bar.setLayout(new BorderLayout());
                bar.setBackground(bp.color);
                barPanel.add(bar);
                JPanel tmp = new JPanel();
                tmp.setBackground(bp.color);
                bar.add(tmp, BorderLayout.CENTER);
                barPanels.add(bar);
                JLabel l = new JLabel(bp.label);
                l.setFont(l.getFont().deriveFont(Font.PLAIN));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                labelPanel.add(l);
            }

            validate();
            portChanged(null, null, null);
        }

        public void componentHidden(ComponentEvent e) {}
        public void componentMoved(ComponentEvent e) {}
        public void componentShown(ComponentEvent e) {}
        public void componentResized(ComponentEvent e) {
            portChanged(null, null, null);
        }
    }
}
