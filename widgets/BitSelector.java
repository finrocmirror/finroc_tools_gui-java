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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
public class BitSelector extends Widget {

    /** UID */
    private static final long serialVersionUID = 738476502968322245L;

    /** Output port */
    public WidgetOutput.Numeric value;

    /** RadioButtons description & key (format: "description = key" */
    public StringList descriptions = new StringList("Description 1\nDescription 2");

    private boolean hideButtonsAndValueDisplay = false;

    @Override
    protected void setDefaultColors() {
        useAlternativeColors();
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new BitSelectorUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return suggestion.derive(suggestion.flags | FrameworkElementFlags.PUSH_STRATEGY_REVERSE);
    }

    class BitSelectorUI extends WidgetUI implements ActionListener, ComponentListener, PortListener<CoreNumber> {

        /** UID */
        private static final long serialVersionUID = -720131048479825628L;

        private List<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
        private JPanel checkBoxPanel = new JPanel();
        private JTextField valueDisplay = new JTextField();
        private JButton all, none;


        BitSelectorUI() {
            super(RenderMode.Swing);
            checkBoxPanel.setOpaque(useOpaquePanels());
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
            all = new JButton("all");
            none = new JButton("none");
            all.addActionListener(this);
            none.addActionListener(this);
            topPanel.add(all);
            topPanel.add(none);
            valueDisplay.setMinimumSize(new Dimension(50, 22));
            topPanel.add(valueDisplay);
            valueDisplay.setEnabled(false);
            setLayout(new BorderLayout());
            add(topPanel, BorderLayout.NORTH);
            add(checkBoxPanel, BorderLayout.CENTER);
            addComponentListener(this);
            value.addChangeListener(this);
            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {
            all.setVisible(!hideButtonsAndValueDisplay);
            none.setVisible(!hideButtonsAndValueDisplay);
            valueDisplay.setVisible(!hideButtonsAndValueDisplay);

            // remove old buttons
            if (descriptions.size() >= 1) {
                checkBoxPanel.setLayout(new GridLayout(descriptions.size(), 1));
            }
            while (checkboxes.size() < descriptions.size()) {
                JCheckBox chb = new JCheckBox();
                chb.addActionListener(this);
                checkboxes.add(chb);
                checkBoxPanel.add(chb);
            }
            while (checkboxes.size() > descriptions.size()) {
                checkboxes.remove(checkboxes.size() - 1);
            }

            for (int i = 0; i < checkboxes.size(); i++) {
                checkboxes.get(i).setText(descriptions.get(i));
            }

            validate();
            repaint();
            //actionPerformed(null);  // update value
            componentResized(null);
            portChanged(null, null);
        }

        public void actionPerformed(ActionEvent e) {
            if (e != null) {
                if (e.getSource() == all) {
                    for (JCheckBox jcb : checkboxes) {
                        jcb.setSelected(true);
                    }
                } else if (e.getSource() == none) {
                    for (JCheckBox jcb : checkboxes) {
                        jcb.setSelected(false);
                    }
                }
            }
            int mask = 1;
            int result = 0;
            for (JCheckBox jcb : checkboxes) {
                if (jcb.isSelected()) {
                    result |= mask;
                }
                mask <<= 1;
            }
            valueDisplay.setText("" + result);
            value.publish(result);
        }

        public void componentHidden(ComponentEvent e) {}
        public void componentShown(ComponentEvent e) {}
        public void componentMoved(ComponentEvent e) {}

        public void componentResized(ComponentEvent e) {
            if (descriptions.size() > 0 && checkBoxPanel.getHeight() > 0) {
                checkBoxPanel.setBorder(BorderFactory.createCompoundBorder(
                                            BorderFactory.createRaisedBevelBorder(),
                                            BorderFactory.createEmptyBorder(0, (checkBoxPanel.getHeight() / descriptions.size()) / 4 , 0, 0)));
            }
        }

        @Override
        public void portChanged(AbstractPort origin, CoreNumber value) {
            int mask = 1;
            int curValue = BitSelector.this.value.getInt();
            for (JCheckBox jcb : checkboxes) {
                jcb.setSelected((curValue & mask) > 0);
                mask <<= 1;
            }
        }

    }
}
