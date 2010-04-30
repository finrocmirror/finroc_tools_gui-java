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
package org.finroc.gui.util.propertyeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.finroc.gui.util.embeddedfiles.AbstractFile;
import org.finroc.gui.util.embeddedfiles.ExternalFolder;
import org.finroc.gui.util.gui.FileDialog;


/**
 * @author max
 *
 */
public class AbstractFileEditor extends PropertyEditComponent<AbstractFile> implements ActionListener {

    /** UID */
    private static final long serialVersionUID = 2486687318726499512L;

    /** Change-Button */
    private JButton button;

    private JTextField curFileText;

    private AbstractFile embeddedFile;

    private Class<? extends AbstractFile> clazz;
    private String[] extensions;

    public AbstractFileEditor(Class<? extends AbstractFile> clazz, String[] extensions) {
        this.clazz = clazz;
        this.extensions = extensions;
    }

    protected void createAndShow() {
        button = new JButton("Change...");
        embeddedFile = getCurWidgetValue();
        curFileText = new JTextField();
        curFileText.setMinimumSize(new Dimension(100, curFileText.getPreferredSize().height));
        if (embeddedFile != null) {
            curFileText.setText(embeddedFile.toString());
        } else {
            curFileText.setText("");
        }
        curFileText.setEnabled(false);
        button.addActionListener(this);
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(curFileText, BorderLayout.CENTER);
        jp.add(button, BorderLayout.EAST);
        createStdLayoutWith(jp);
    }

    @Override
    public AbstractFile getCurEditorValue() {
        return embeddedFile;
    }

    public void actionPerformed(ActionEvent e) {

        File f = folder() ? FileDialog.showOpenPathDialog("Choose Path") : FileDialog.showOpenDialog("Choose File", extensions);
        if (f != null) {
            try {
                embeddedFile = getParentDialog().getEmbeddedFileManager().loadFile(f, clazz);
                curFileText.setText(embeddedFile.toString());
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, ex.getMessage());
            }
        }
        embeddedFile = null;
        curFileText.setText("");
    }

    /**
     * @return Is user prompted to select a folder instead of a file?
     */
    private boolean folder() {
        return ExternalFolder.class.isAssignableFrom(clazz);
    }
}