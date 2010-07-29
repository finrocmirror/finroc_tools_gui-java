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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.finroc.gui.Widget;
import org.finroc.gui.WidgetInput;
import org.finroc.gui.WidgetOutput;
import org.finroc.gui.WidgetPort;
import org.finroc.gui.WidgetUI;
import org.finroc.gui.themes.Themes;

import org.finroc.log.LogLevel;
import org.finroc.plugin.datatype.BehaviourInfo;
import org.finroc.plugin.datatype.mca.BehaviourInfoBlackboard;
import org.finroc.plugin.datatype.ContainsStrings;
import org.finroc.plugin.datatype.mca.MCA;
import org.finroc.plugin.blackboard.BlackboardBuffer;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.rpc.MethodCallException;
import org.finroc.core.port.std.PortBase;
import org.finroc.core.port.std.PortData;
import org.finroc.core.port.std.PortListener;


/**
 * @author max
 *
 */
public class BehaviourSignals extends Widget {

    /** UID */
    private static final long serialVersionUID = -83683824582007L;

    /** Blackboards */
    public WidgetInput.Std<ContainsStrings> names;
    public WidgetOutput.Blackboard<BehaviourInfoBlackboard> signals;

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (forPort == names) {
            return suggestion.derive(ContainsStrings.TYPE);
        } else if (forPort == signals) {
            PortCreationInfo pci = suggestion.derive(BehaviourInfoBlackboard.TYPE);
            pci.setFlag(PortFlags.PUSH_STRATEGY, true);
            return pci;
        }
        return null;
    }

    @Override
    protected void setDefaultColors() {
        setBackground(Themes.getCurTheme().standardBackground());
        setLabelColor(Themes.getCurTheme().standardLabel());
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new BehaviourSignalsUI();
    }

    @SuppressWarnings("unchecked")
    class BehaviourSignalsUI extends WidgetUI implements PortListener {

        /** UID */
        private static final long serialVersionUID = -27396902858121219L;

        List<Entry> entries = new ArrayList<Entry>();
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel reference = new JLabel("Behaviour");
        private Font font = reference.getFont().deriveFont(Font.PLAIN);
        private Insets nullInsets = new Insets(0, 0, 0, 0);
        private Insets padInsets = new Insets(0, 0, 2, 0);
        int bbElemSize = -1;

        BehaviourSignalsUI() {
            super(RenderMode.Swing);
            signals.addChangeListener(this);
            names.addChangeListener(this);
            setLayout(new GridBagLayout());

            // create Titlebar
            gbc.insets = padInsets;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            gbc.weighty = 0.000001;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            add(reference, gbc);
            gbc.anchor = GridBagConstraints.NORTH;
            JLabel auto = new JLabel("Auto");
            JLabel enable = new JLabel("Enable");
            auto.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            enable.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            gbc.weightx = 0;
            add(auto, gbc);
            add(enable, gbc);
            gbc.anchor = GridBagConstraints.NORTHWEST;
            gbc.weightx = 0.1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            JLabel l1 = new JLabel("Activation");
            JLabel l2 = new JLabel("Activity");
            JLabel l3 = new JLabel("T. Rating");
            int width = (int)Math.max(l1.getPreferredSize().getWidth(), Math.max(l2.getPreferredSize().getWidth(), l3.getPreferredSize().getWidth())) + 1;
            Dimension d = new Dimension(width, (int)l1.getPreferredSize().getHeight());
            l1.setPreferredSize(d);
            l2.setPreferredSize(d);
            l3.setPreferredSize(d);
            l1.setMinimumSize(d);
            l2.setMinimumSize(d);
            l3.setMinimumSize(d);
            add(l1, gbc);
            add(l2, gbc);
            add(l3, gbc);

            gbc.insets = nullInsets;
            gbc.weightx = 0;
            gbc.weighty = 0.1;
            portChanged(null, null);
        }

        @Override
        public void add(Component comp, Object constraints) {
            if (comp instanceof JLabel) {
                ((JLabel)comp).setFont(font);
            }
            super.add(comp, constraints);
        }

        @Override
        public void widgetPropertiesChanged() {

        }

        @Override
        public void portChanged(PortBase origin, PortData value) {

            BehaviourInfoBlackboard bil = signals.readAutoLocked();
            ContainsStrings strings = names.getAutoLocked();
            if (bil != null) {
                bbElemSize = bil.getElementSize();
            }

            // no data ?
            if (bil == null || strings == null || bil.size() <= 0 || strings.stringCount() <= 0) {
                while (entries.size() > 0) {
                    entries.remove(0).clear();
                }
                releaseAllLocks();
                return;
            }

            // entries changed ?
            while (entries.size() < bil.size()) {
                entries.add(new Entry(entries.size() + 1));
            }
            while (entries.size() > bil.size()) {
                entries.remove(entries.size() - 1).clear();
            }

            // update values
            for (int i = 0; i < entries.size(); i++) {
                BehaviourInfo.Entry bi = bil.getEntry(i);
                //entries.get(i).update(bi, (bi.beh_id >= strings.size()) ? "" : strings.get(bi.beh_id));

                // Rand unter letzter Zeile sicherstellen
                gbc.insets = (i == entries.size() - 1) ? padInsets : nullInsets;

                entries.get(i).update(bi, (i >= strings.stringCount()) ? "" : strings.getString(i).toString());
            }

            releaseAllLocks();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle r = super.getRenderBounds();
            g.drawLine(r.x + 3, (int)reference.getBounds().getMaxY(), r.width - 3, (int)reference.getBounds().getMaxY());
        }

        class Entry implements ActionListener {
            JLabel label;
            JCheckBox auto, enable;
            MBar activation, rating, activity;
            BehaviourInfo info;
            final int index;

            public Entry(int lineIndex) {
                index = lineIndex - 1;
                label = new JLabel();
                auto = new JCheckBox();
                enable = new JCheckBox();
                activation = new MBar(new Color(0.5f, 0.5f, 1));
                activity = new MBar(new Color(0.5f, 1, 0.5f));
                rating = new MBar(new Color(1, 0.5f, 0.5f));
                auto.addActionListener(this);
                enable.addActionListener(this);
                auto.setOpaque(false);
                enable.setOpaque(false);

                gbc.gridy = lineIndex;
                gbc.anchor = GridBagConstraints.EAST;
                gbc.fill = GridBagConstraints.NONE;
                add(label, gbc);
                add(auto, gbc);
                gbc.anchor = GridBagConstraints.CENTER;
                add(enable, gbc);
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.BOTH;
                add(activation, gbc);
                add(activity, gbc);
                add(rating, gbc);
            }

            public void update(BehaviourInfo.Entry bi, String descr) {
                label.setText(descr);
                auto.setSelected(bi.getAutoMode());
                enable.setSelected(bi.isEnabled());
                activation.setValue(bi.getActivation());
                activity.setValue(bi.getActivity());
                rating.setValue(bi.getTargetRating());
            }

            public void clear() {
                remove(activity);
                remove(auto);
                remove(enable);
                remove(activation);
                remove(label);
                remove(rating);
            }

            public void actionPerformed(ActionEvent e) {

                // hehe... we only send one byte... asynchronously
                assert(bbElemSize > 0);
                int offset = bbElemSize * index + (e.getSource() == enable ? MCA.tBehaviourInfo._enabled.getOffset() : MCA.tBehaviourInfo._auto_mode.getOffset());
                byte b = ((JCheckBox)e.getSource()).isSelected() ? (byte)1 : 0;
                BlackboardBuffer buf = signals.getClient().getUnusedBuffer();
                buf.resize(1, 1, 1, false);
                buf.getBuffer().putByte(0, b);
                try {
                    signals.getClient().commitAsynchChange(offset, buf);
                } catch (MethodCallException e1) {
                    log(LogLevel.LL_WARNING, logDomain, "Warning: Couldn't commit behaviour info blackboard change");
                }
            }
        }
    }
}

class MBar extends JPanel {

    /** UID */
    private static final long serialVersionUID = 7958656286249229217L;

    public double curValue;
    public Color color;
    public static Color background = new Color(255, 255, 220);

    public MBar(Color color) {
        this.color = color;
        this.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 4));
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int w = getWidth();
        int h = getHeight();
        int valuePos = (int)(curValue * (w - 4));
        Color temp = g.getColor();

        g.setColor(background);
        g.fillRect(valuePos, 1, w - 4 - valuePos, h - 2);
        g.setColor(color);
        g.fillRect(0, 1, valuePos, h - 2);

        g.setColor(temp);
    }

    public double getValue() {
        return curValue;
    }

    public void setValue(double curValue) {
        this.curValue = curValue;
        repaint();
    }
}
