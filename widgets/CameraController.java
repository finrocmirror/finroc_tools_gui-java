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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetInput;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.themes.Themes;

import org.finroc.plugins.data_types.CameraFeature;
import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;
import org.finroc.core.port.std.PortDataManager;
import org.finroc.core.portdatabase.ReusableGenericObjectManager;
import org.rrlib.finroc_core_utils.serialization.Serialization;
import org.rrlib.finroc_core_utils.xml.XMLDocument;
import org.rrlib.finroc_core_utils.xml.XMLNode;


/**
 * @author Max Reichardt
 *
 */
public class CameraController extends Widget {

    /** UID */
    private static final long serialVersionUID = -83683824582007L;

    /** Ports */
    public WidgetInput.Std<CameraFeature.Set> cameraState;
    public WidgetOutput.Std<CameraFeature> changeRequests;

    boolean dumpButtonVisible = false;

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        if (forPort == cameraState) {
            return suggestion.derive(CameraFeature.SET_TYPE);
        } else if (forPort == changeRequests) {
            return suggestion.derive(CameraFeature.TYPE).derive(suggestion.flags | FrameworkElementFlags.NO_INITIAL_PUSHING);
        } /*else if (forPort == signalsTest) {
            PortCreationInfo pci = suggestion.derive(BehaviourInfo.TYPE.getListType());
            return pci;
        }*/
        return null;
    }

    @Override
    protected void setDefaultColors() {
        useAlternativeColors();
    }

    @Override
    protected WidgetUI createWidgetUI() {
        return new CameraControllerUI();
    }

    class CameraControllerUI extends WidgetUI implements PortListener<CameraFeature.Set>, ActionListener {

        /** UID */
        private static final long serialVersionUID = -27396902858121219L;

        List<CameraControllerPanel> cpanels = new ArrayList<CameraControllerPanel>();
        JButton dumpSettings = new JButton("Dump camera settings to console");
        JPanel main = new JPanel();
        GridLayout layout = new GridLayout();

        CameraControllerUI() {
            super(RenderMode.Swing);
            cameraState.addChangeListener(this);
            setLayout(new BorderLayout());

            main.setOpaque(useOpaquePanels());
            main.setLayout(layout);
            add(main, BorderLayout.CENTER);
            add(dumpSettings, BorderLayout.SOUTH);
            dumpSettings.addActionListener(this);

            // create Titlebar
            portChanged(null, null);

            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {
            dumpSettings.setVisible(dumpButtonVisible);
        }

        @Override
        public void portChanged(AbstractPort origin, CameraFeature.Set value) {

            CameraFeature.Set cfs = cameraState.getAutoLocked();

            // no data ?
            if (cfs == null) {
                main.removeAll();
                cpanels.clear();
                releaseAllLocks();
                return;
            }

            // entries changed ?
            int panelCount = 0;
            for (int i = 0; i < CameraFeature.ID.eCF_DIMENSION.ordinal(); i++) {
                if (cfs.features[i].isAvailable()) {
                    panelCount++;
                }
            }
            layout.setRows(panelCount > 0 ? panelCount : 1);

            while (cpanels.size() < panelCount) {
                CameraControllerPanel cp = new CameraControllerPanel();
                main.add(cp);
                cpanels.add(cp);
            }
            while (cpanels.size() > panelCount) {
                CameraControllerPanel cp = cpanels.remove(cpanels.size() - 1);
                main.remove(cp);
                cpanels.remove(cp);
            }

            // update rows
            int idx = 0;
            for (int i = 0; i < CameraFeature.ID.eCF_DIMENSION.ordinal(); i++) {
                if (cfs.features[i].isAvailable()) {
                    cpanels.get(idx).update(cfs.features[i]);
                    idx++;
                }
            }

            releaseAllLocks();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == dumpSettings) {
                try {
                    XMLDocument xml = new XMLDocument();
                    XMLNode root = xml.addRootNode("settings");
                    CameraFeature.Set cfs = cameraState.getAutoLocked();
                    cfs.serialize(root);
                    releaseAllLocks();
                    System.out.println(xml.getXMLDump(true));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        class CameraControllerPanel extends JPanel implements ActionListener, ChangeListener {

            /** UID */
            private static final long serialVersionUID = 5179081200310675562L;

            CameraFeature.ID featureIndex = CameraFeature.ID.eCF_DIMENSION;
            TitledBorder tb = new TitledBorder("");
            JComboBox modeSelection = new JComboBox();
            JCheckBox onOff = new JCheckBox("On");
            JSlider[] sliders = new JSlider[3];
            boolean updating = false; // temporarily deactivates reaction to action events
            JPanel sliderPanel = new JPanel();
            GridLayout sliderGrid = new GridLayout(1, 1);
            JTextField editBox = new JTextField("");
            CameraFeature currentState = new CameraFeature();

            public CameraControllerPanel() {
                tb.setBorder(Themes.getCurTheme().createThinBorder());
                super.setBorder(tb);
                setLayout(new BorderLayout());
                sliderPanel.setLayout(sliderGrid);
                JPanel leftPanel = new JPanel();
                leftPanel.setLayout(new BorderLayout());
                leftPanel.setBorder(new EmptyBorder(5, 0, 5, 5));

                sliders[0] = new JSlider();
                sliders[0].setPaintLabels(true);
                sliders[0].setPaintTicks(true);
                sliders[1] = new JSlider();
                sliders[1].setPaintLabels(true);
                sliders[1].setPaintTicks(true);
                sliders[2] = new JSlider();
                sliders[2].setPaintLabels(true);
                sliders[2].setPaintTicks(true);

                leftPanel.add(onOff, BorderLayout.WEST);
                leftPanel.add(modeSelection, BorderLayout.EAST);
                sliderPanel.add(sliders[0]);
                add(leftPanel, BorderLayout.WEST);
                add(sliderPanel, BorderLayout.CENTER);
                add(editBox, BorderLayout.EAST);

                modeSelection.setPreferredSize(new Dimension(110, modeSelection.getMinimumSize().height));
                editBox.setPreferredSize(new Dimension(50, editBox.getMaximumSize().height));

                onOff.addActionListener(this);
                modeSelection.addActionListener(this);
                sliders[0].addChangeListener(this);
                sliders[1].addChangeListener(this);
                sliders[2].addChangeListener(this);
            }

            public synchronized void update(CameraFeature cf) {
                Serialization.deepCopy(cf, currentState, null);
                updating = true;
                featureIndex = cf.getFeatureId();
                tb.setTitle(cf.getFeatureName());
                onOff.setEnabled(cf.on_off.available);
                onOff.setVisible(cf.on_off.available);
                boolean on = (!cf.on_off.available) || cf.on_off.active;
                onOff.setSelected(on);
                modeSelection.setEnabled(on);

                List < Enum<? >> modes = new ArrayList < Enum<? >> ();
                if (cf.absolute.available) {
                    modes.add(CameraFeature.Mode.ABSOLUTE);
                }
                if (cf.manual.available) {
                    modes.add(CameraFeature.Mode.MANUAL);
                }
                if (cf.automatic.available) {
                    modes.add(CameraFeature.Mode.AUTOMATIC);
                }
                if (!on || cf.getMode() == CameraFeature.Mode.OFF) {
                    modes.add(CameraFeature.Mode.OFF);
                }
                modeSelection.setModel(new DefaultComboBoxModel(modes.toArray()));

                if (modeSelection.getSelectedItem() != cf.getMode()) {
                    modeSelection.setSelectedItem(cf.getMode());
                }

                // adjust number of sliders
                for (int i = cf.getNumberOfValues(); i < 3; i++) {
                    if (sliderPanel.isAncestorOf(sliders[i])) {
                        sliderPanel.remove(sliders[i]);
                    }
                }
                for (int i = 1; i < cf.getNumberOfValues(); i++) {
                    if (!sliderPanel.isAncestorOf(sliders[i])) {
                        sliderPanel.add(sliders[i]);
                    }
                }
                sliderGrid.setColumns(cf.getNumberOfValues());

                if (cf.getMode() == CameraFeature.Mode.MANUAL) {
                    for (int i = 0; i < cf.getNumberOfValues(); i++) {
                        sliders[i].setEnabled(on);
                        sliders[i].setMinimum((int)cf.manual.min);
                        sliders[i].setMaximum((int)cf.manual.max);
                        float diff = cf.manual.max - cf.manual.min;
                        sliders[i].setMajorTickSpacing((int)(diff < 1000 ? Math.pow(10, Math.floor(Math.log10(diff))) : (diff / 2)));
                        sliders[i].setValue((int)cf.getValue(i));
                    }
                }

                if (cf.getMode() == CameraFeature.Mode.ABSOLUTE) {
                    sliders[0].setEnabled(on);
                    sliders[0].setMinimum((int)cf.absolute.min);
                    sliders[0].setMaximum((int)cf.absolute.max);
                    float diff = cf.absolute.max - cf.absolute.min;
                    sliders[0].setMajorTickSpacing((int)(diff < 1000 ? Math.pow(10, Math.floor(Math.log10(diff))) : (diff / 2)));
                    sliders[0].setValue((int)cf.getAbsoluteValue());
                }

                editBox.setText(cf.getMode() == CameraFeature.Mode.ABSOLUTE ? ("" + cf.getAbsoluteValue()) : ("" + cf.getValue(0)));

                if (cf.getMode() == CameraFeature.Mode.AUTOMATIC) {
                    for (int i = 0; i < cf.getNumberOfValues(); i++) {
                        sliders[i].setEnabled(false);
                    }
                }

                updating = false;
            }

            public synchronized void actionPerformed(ActionEvent e) {
                if (updating) {
                    return;
                }

                CameraFeature cf = changeRequests.getUnusedBuffer();
                Serialization.deepCopy(currentState, cf, null);
                cf.setFeatureId(featureIndex);
                boolean changed = false;
                if (e.getSource() == onOff) {
                    //cf.setMode(onOff.isSelected() ? CameraFeature.Mode.MANUAL : CameraFeature.Mode.OFF);
                    cf.on_off.active = onOff.isSelected();
                    changed = true;
                }

                if (e.getSource() == modeSelection) {
                    CameraFeature.Mode mode = (CameraFeature.Mode)modeSelection.getSelectedItem();
                    if (mode != null) {
                        cf.setMode(mode);
                        changed = true;
                    }
                }

                if (changed) {
                    changeRequests.publish(cf);
                    update(cf);
                } else {
                    ((PortDataManager)ReusableGenericObjectManager.getManager(cf)).recycleUnused();
                }
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                if (updating) {
                    return;
                }

                for (int i = 0; i < 3; i++) {
                    if (e.getSource() == sliders[i]) {
                        CameraFeature cf = changeRequests.getUnusedBuffer();
                        Serialization.deepCopy(currentState, cf, null);
                        cf.setFeatureId(featureIndex);
                        cf.setMode((CameraFeature.Mode)modeSelection.getSelectedItem());
                        if (cf.getMode() == null) {
                            ((PortDataManager)ReusableGenericObjectManager.getManager(cf)).recycleUnused();
                            return;
                        }

                        if (cf.getMode() == CameraFeature.Mode.MANUAL) {
                            cf.setValue(i, sliders[i].getValue());
                        } else if (cf.getMode() == CameraFeature.Mode.ABSOLUTE) {
                            assert(i == 0);
                            cf.setAbsoluteValue(sliders[0].getValue());
                        }

                        changeRequests.publish(cf);
                        update(cf);
                        return;
                    }
                }
            }
        }
    }
}
