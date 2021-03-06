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
package org.finroc.tools.gui.widgets;

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
import javax.swing.SwingUtilities;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.plugins.data_types.StringList;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;


/**
 * @author Max Reichardt
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
        useAlternativeColors();
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new RadioButtonsUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
    }

    class RadioButtonsUI extends WidgetUI implements ActionListener, ComponentListener, PortListener<CoreNumber>, Runnable {

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
                    jrb.setForeground(getLabelColor(RadioButtons.this));
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
            if (itemCount > 0 && getRenderHeight() > 0) { // to prevent option buttons sticking to the left edge
                setBorder(BorderFactory.createEmptyBorder(0, (getRenderHeight() / itemCount) / 3 , 0, 0));
            }
        }

        @Override
        public void portChanged(AbstractPort origin, CoreNumber value) {
            SwingUtilities.invokeLater(this);
        }

        @Override
        public void run() {
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
