/**
 * You received this file as part of FinGUI - a universal
 * (Web-)GUI editor for Robotic Systems.
 *
 * Copyright (C) 2012 Patrick Fleischmann
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
package org.finroc.tools.gui.widgets;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetOutput;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.finroc.tools.gui.util.gui.IconManager;

import org.finroc.core.datatype.CoreString;
import org.finroc.core.port.PortCreationInfo;

public class FileChooser extends Widget {

    /** UID */
    private static final long serialVersionUID = -4086621176900592220L;

    /** Button output ports */
    public WidgetOutput.Std<CoreString> filename;

    /** Button parameters */
    public boolean saveDialog = false;

    static Icon openFileIcon, saveFileIcon;

    @Override
    protected WidgetUI createWidgetUI() {
        return new FileChooserUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion,
            WidgetPort<?> forPort) {
        if (forPort == filename) {
            return suggestion.derive(CoreString.TYPE);
        }
        return suggestion;
    }

    class FileChooserUI extends WidgetUI implements ActionListener {
        /** UID */
        private static final long serialVersionUID = 8792391753652899811L;

        private AbstractButton button;

        FileChooserUI() {
            super(RenderMode.Swing);

            // load icons
            openFileIcon = IconManager.getInstance().getIcon("document-open.png");
            saveFileIcon = IconManager.getInstance().getIcon("document-save.png");

            // add button
            setLayout(new BorderLayout());
            button = new JButton();
            add(button, BorderLayout.CENTER);
            button.addActionListener(this);

            widgetPropertiesChanged();
        }

        @Override
        public void widgetPropertiesChanged() {
            if (saveDialog) {
                button.setIcon(saveFileIcon);
            } else {
                button.setIcon(openFileIcon);
            }
        }

        public void actionPerformed(ActionEvent e) {
            final JFileChooser fc = new JFileChooser();

            int returnVal = JFileChooser.ABORT;
            if (saveDialog) {
                returnVal = fc.showSaveDialog(this);
            } else {
                returnVal = fc.showOpenDialog(this);
            }

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                System.out.println(fc.getSelectedFile().getAbsolutePath());
                CoreString file_name = filename.getUnusedBuffer();
                file_name.set(fc.getSelectedFile().getAbsolutePath());
                filename.publish(file_name);
            }
        }

    }
}
