//
// You received this file as part of Finroc
// A Framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui.widgets;

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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetPorts;
import org.finroc.tools.gui.WidgetPortsListener;
import org.finroc.tools.gui.WidgetUI;

import org.finroc.plugins.data_types.BehaviorStatus;
import org.finroc.plugins.data_types.Ib2cServiceClient;
import org.finroc.core.RuntimeEnvironment;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.remote.ModelNode;
import org.finroc.core.remote.PortWrapperTreeNode;
import org.finroc.core.remote.RemotePort;
import org.finroc.core.remote.RemoteRuntime;


/**
 * @author Max Reichardt
 *
 */
public class BehaviorSignals extends Widget {

    /** UID */
    private static final long serialVersionUID = -83683824582007L;

    /** Ports: Status inputs */
    public WidgetPorts<WidgetInput.Std<BehaviorStatus>> statusInputs =
        new WidgetPorts<WidgetInput.Std<BehaviorStatus>>("Behavior Status", 3, WidgetInput.Std.class, this);

    /** Parameters */
    public int numberOfStatusInputs = 3;

    /**
     * Global client port for ib2c service
     * May only be accessed by AWT thread for thread-safety (is reconnected)
     */
    private static transient Ib2cServiceClient ib2cServiceClientPort;

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (statusInputs != null && forPort.getDescription().startsWith("Behavior Status")) {
            return suggestion.derive(BehaviorStatus.TYPE);
        }
        return suggestion;
    }

    @Override
    protected void setDefaultColors() {
        useAlternativeColors();
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new BehaviourSignalsUI();
    }

    /**
     * Obtains service port client instance (creates it if necessary)
     * May only be accessed by AWT thread for thread-safety
     */
    private static Ib2cServiceClient getIb2cServiceClientPort() {
        if (ib2cServiceClientPort == null) {
            ib2cServiceClientPort = new Ib2cServiceClient("Ib2c Service Client", RuntimeEnvironment.getInstance());
        }
        return ib2cServiceClientPort;
    }

    class BehaviourSignalsUI extends WidgetUI implements WidgetPortsListener {

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
            statusInputs.addChangeListener(this);
            //signalsTest.addChangeListener(this);
            setLayout(new GridBagLayout());

            // create Titlebar
            gbc.insets = padInsets;
            gbc.gridy = 0;
            gbc.weightx = 0.0;
            gbc.weighty = 0.000001;
            gbc.anchor = GridBagConstraints.NORTHEAST;
            add(reference, gbc);
            gbc.anchor = GridBagConstraints.NORTH;
            JLabel auto = new JLabel("Stimulation");
            auto.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            gbc.weightx = 0;
            add(auto, gbc);
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
            widgetPropertiesChanged();
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
            statusInputs.setSize(numberOfStatusInputs);
            initPorts();

            // entries changed ?
            while (entries.size() < numberOfStatusInputs) {
                entries.add(new Entry(entries.size() + 1));
            }
            while (entries.size() > numberOfStatusInputs) {
                entries.remove(entries.size() - 1).clear();
            }
        }

        @Override
        public void portChanged(final WidgetPorts<?> origin, final AbstractPort port, final Object value) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    int index = origin.indexOf(port);
                    if (origin == statusInputs && index >= 0) {
                        BehaviorStatus status = (BehaviorStatus)value;

                        // Ensure that we have a line below the last entry
                        gbc.insets = (index == entries.size() - 1) ? padInsets : nullInsets;

                        entries.get(index).update(status);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Rectangle r = super.getRenderBounds();
            g.drawLine(r.x + 3, (int)reference.getBounds().getMaxY(), r.width - 3, (int)reference.getBounds().getMaxY());
        }

        class Entry implements ActionListener {
            JLabel label;
            //JCheckBox auto, enable;
            JComboBox stimulationMode;
            MBar activation, rating, activity;
            BehaviorStatus info;
            final int index;
            int remoteModuleHandle;
            BehaviorStatus.StimulationMode currentStimulationMode = BehaviorStatus.StimulationMode.Auto;

            public Entry(int lineIndex) {
                index = lineIndex - 1;
                label = new JLabel();
                stimulationMode = new JComboBox(BehaviorStatus.StimulationMode.values());
                //activation = new MBar(new Color(0.5f, 0.5f, 1));
                //Color background = new Color(0.5f, 0.6f, 1, 0.2f);
                //Color background = new Color(0, 0, 0.15f, 0.05f);
                Color background = new Color(1, 1, 0.7f, 0.2f);
                activation = new MBar(new Color(1, 0.97f, 0f, 0.43f), background);
                activity = new MBar(new Color(0.05f, 0.75f, 0, 0.4f), background);
                rating = new MBar(new Color(1, 0.2f, 0f, 0.47f), background);
                stimulationMode.addActionListener(this);
                stimulationMode.setOpaque(false);

                gbc.gridy = lineIndex;
                gbc.anchor = GridBagConstraints.EAST;
                gbc.fill = GridBagConstraints.NONE;
                add(label, gbc);
                gbc.anchor = GridBagConstraints.CENTER;
                gbc.insets.left = 3;
                gbc.insets.right = 3;
                add(stimulationMode, gbc);
                gbc.insets.left = 0;
                gbc.insets.right = 0;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.fill = GridBagConstraints.BOTH;
                add(activation, gbc);
                add(activity, gbc);
                add(rating, gbc);
            }

            public void update(BehaviorStatus status) {
                label.setText(status.name);
                currentStimulationMode = status.stimulationMode;
                stimulationMode.setSelectedItem(status.stimulationMode);
                activation.setValue(status.activation);
                activity.setValue(status.activity);
                rating.setValue(status.targetRating);
                remoteModuleHandle = status.moduleHandle;
            }

            public void clear() {
                remove(activity);
                remove(stimulationMode);
                remove(activation);
                remove(label);
                remove(rating);
            }

            public void actionPerformed(ActionEvent e) {
                if (stimulationMode.getSelectedItem() == currentStimulationMode) {
                    return;
                }
                currentStimulationMode = (BehaviorStatus.StimulationMode)stimulationMode.getSelectedItem();

                try {
                    if (remoteModuleHandle != 0) {
                        for (PortWrapperTreeNode portNode : statusInputs.get(index).getConnectionPartners()) {

                            // find runtime parent
                            RemotePort port = (RemotePort)portNode;
                            RemoteRuntime runtime = RemoteRuntime.find(port);
                            ModelNode servicePortNode = runtime.getChildByQualifiedName(Ib2cServiceClient.QUALIFIED_PORT_NAME, '/');
                            if (!(servicePortNode instanceof RemotePort)) {
                                //log(LogLevel.LL_WARNING, logDomain, "Could not find remote ib2c service port");
                                System.out.println("Could not find remote ib2c service port"); // TODO: proper log message (after update)
                                break;
                            }

                            Ib2cServiceClient clientPort = getIb2cServiceClientPort();
                            clientPort.getWrapped().disconnectAll();
                            clientPort.connectTo(((RemotePort)servicePortNode).getPort());
                            clientPort.setStimulationMode(remoteModuleHandle, (BehaviorStatus.StimulationMode)stimulationMode.getSelectedItem());
                            clientPort.getWrapped().disconnectAll();
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace(); // TODO: proper log message (after update)
                }
            }
        }
    }
}

class MBar extends JPanel {

    /** UID */
    private static final long serialVersionUID = 7958656286249229217L;

    public double curValue;
    public Color color, background;
    //public static Color background = new Color(240, 245, 255);
    //public static Color background = new Color(240, 245, 255);

    public MBar(Color color, Color background) {
        this.color = color;
        this.background = background;
        this.setOpaque(false);
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
