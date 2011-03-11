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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetOutput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.themes.Themes;
import org.finroc.plugin.datatype.StringList;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.PortListener;


/**
 * @author max
 *
 */
public class RadioButtons extends Widget {

    /** UID */
    private static final long serialVersionUID = 738476502968322245L;

    /** Output port */
    public WidgetOutput.Numeric selected;

    /** RadioButtons description & key (format: "description = key" */
    public StringList options = new StringList("Option 1=0\nOption 2=1");

    @Override
    protected void setDefaultColors() {
        setBackground(Themes.getCurTheme().standardBackground());
        setLabelColor(Themes.getCurTheme().standardLabel());
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new RadioButtonsUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | PortFlags.ACCEPTS_REVERSE_DATA_PUSH);
    }

    class RadioButtonsUI extends WidgetUI implements ActionListener, ComponentListener, PortListener<CoreNumber> {

        /** UID */
        private static final long serialVersionUID = -720131048479825628L;

        private int itemCount;
        private List<JRadioButton> buttons = new ArrayList<JRadioButton>();

        RadioButtonsUI() {
            super(RenderMode.Swing);
            addComponentListener(this);
            selected.addChangeListener(this);
            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {

            // remove old buttons
            for (JRadioButton jrb : buttons) {
                remove(jrb);
            }
            buttons.clear();

            ButtonGroup group = new ButtonGroup();
            itemCount = 0;
            for (String s : options) {
                if (!s.contains("=")) {
                    continue;
                }
                try {
                    String[] strings = s.split("=");
                    JRadioButton jrb = new JRadioButton(strings[0].trim());
                    jrb.addActionListener(this);
                    double d = 0;
                    try {
                        d = Double.parseDouble(strings[1].trim());
                    } catch (Exception e) {}
                    jrb.setActionCommand("" + d);
                    group.add(jrb);
                    init(jrb); // MouseEvents "hooken"
                    add(jrb);
                    buttons.add(jrb);
                    jrb.setBackground(RadioButtons.this.getBackground());
                    jrb.setForeground(getLabelColor());
                    itemCount++;
                } catch (Exception e) {}
            }
            if (itemCount >= 1) {
                setLayout(new GridLayout(itemCount, 1));
            }
            componentResized(null);
            portChanged(null, null);
        }

        public void actionPerformed(ActionEvent e) {
            selected.publish(Double.parseDouble(e.getActionCommand()));
        }

        public void componentHidden(ComponentEvent e) {}
        public void componentShown(ComponentEvent e) {}
        public void componentMoved(ComponentEvent e) {}

        public void componentResized(ComponentEvent e) {
            if (itemCount > 0 && getRenderHeight() > 0) {
                setBorder(BorderFactory.createEmptyBorder(0, (getRenderHeight() / itemCount) / 3 , 0, 0));
            }
        }

        @Override
        public void portChanged(AbstractPort origin, CoreNumber value) {
            double curValue = selected.getDouble();
            double bestDiff = Double.MAX_VALUE;
            JRadioButton bestSelection = null;
            for (JRadioButton jrb : buttons) {
                double jrbVal = Double.parseDouble(jrb.getActionCommand());
                if (Math.abs(jrbVal - curValue) < bestDiff) {
                    bestSelection = jrb;
                    bestDiff = Math.abs(jrbVal - curValue);
                }
            }
            if (bestSelection != null) {
                bestSelection.setSelected(true);
            }
        }
    }
}
