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
package org.finroc.tools.gui.util.propertyeditor.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.finroc.tools.gui.util.embeddedfiles.AbstractFile;
import org.finroc.tools.gui.util.embeddedfiles.ExternalFolder;
import org.finroc.tools.gui.util.gui.FileDialog;
import org.finroc.tools.gui.util.propertyeditor.PropertyEditComponent;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;


/**
 * @author Max Reichardt
 *
 */
public class AbstractFileEditor extends PropertyEditComponent<AbstractFile> implements ActionListener {

    /** UID */
    private static final long serialVersionUID = 2486687318726499512L;

    /** Change-Button */
    private JButton button;

    private JTextField curFileText;

    private AbstractFile embeddedFile;

    private Class <? extends AbstractFile > clazz;
    private String[] extensions;

    /** reference to properties dialog */
    private final PropertiesDialog parent;

    public AbstractFileEditor(Class <? extends AbstractFile > clazz, String[] extensions, PropertiesDialog parent) {
        this.clazz = clazz;
        this.extensions = extensions;
        this.parent = parent;
    }

    protected void createAndShow() throws Exception {
        button = new JButton("Change...");
        button.setEnabled(isModifiable());
        curFileText = new JTextField();
        curFileText.setMinimumSize(new Dimension(100, curFileText.getPreferredSize().height));
        curFileText.setPreferredSize(curFileText.getMinimumSize());
        curFileText.setEnabled(false);
        valueUpdated(getCurWidgetValue());
        button.addActionListener(this);
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(curFileText, BorderLayout.CENTER);
        jp.add(button, BorderLayout.EAST);
        add(jp, BorderLayout.CENTER);
    }

    @Override
    public AbstractFile getCurEditorValue() {
        return embeddedFile;
    }

    public void actionPerformed(ActionEvent e) {

        File f = folder() ? FileDialog.showOpenPathDialog("Choose Path") : FileDialog.showOpenDialog("Choose File", extensions);
        if (f != null) {
            try {
                embeddedFile = parent.getEmbeddedFileManager().loadFile(f, clazz);
                curFileText.setText(embeddedFile.toString());
                return;
            } catch (Exception ex) {
                Log.log(LogLevel.ERROR, this, ex);
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

    @Override
    protected void valueUpdated(AbstractFile t) {
        embeddedFile = t;
        if (embeddedFile != null) {
            curFileText.setText(embeddedFile.toString());
        } else {
            curFileText.setText("");
        }
    }
}
