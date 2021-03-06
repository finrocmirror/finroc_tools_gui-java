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
import java.io.File;

import javax.swing.JOptionPane;

import org.finroc.tools.gui.util.embeddedfiles.AbstractFile;
import org.finroc.tools.gui.util.embeddedfiles.AbstractFiles;
import org.finroc.tools.gui.util.embeddedfiles.EmbeddedFile;
import org.finroc.tools.gui.util.gui.FileDialog;
import org.finroc.tools.gui.util.gui.ListWithNewAndDeleteButton;
import org.finroc.tools.gui.util.propertyeditor.PropertyEditComponent;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;


public class AbstractFilesEditor extends PropertyEditComponent<AbstractFiles<AbstractFile>> {

    /** UID */
    private static final long serialVersionUID = -225852580853828747L;

    AbstractFiles<AbstractFile> list;

    /** reference to properties dialog */
    private final PropertiesDialog parent;

    public AbstractFilesEditor(PropertiesDialog parent) {
        this.parent = parent;
    }

    @Override
    protected void createAndShow() throws Exception {
        valueUpdated(getCurWidgetValue());
    }

    @Override
    public AbstractFiles<AbstractFile> getCurEditorValue() {
        return list;
    }

    @Override
    protected void valueUpdated(AbstractFiles<AbstractFile> t) {
        list = t;
        removeAll();
        add(new EmbeddedFilesEditorList(list, "Edit...", list.getExtensions()));
    }


    class EmbeddedFilesEditorList extends ListWithNewAndDeleteButton<AbstractFile> {

        /** UID */
        private static final long serialVersionUID = 3305138623569659819L;

        private String[] extensions;

        EmbeddedFilesEditorList(AbstractFiles<AbstractFile> files, String editButtonText, String[] extensions) {
            super(files, ""/*getPropertyName()*/, false, BorderLayout.SOUTH, editButtonText, true);
            this.extensions = extensions;
            setControlsEnabled(AbstractFilesEditor.this.isModifiable());
        }

        @Override
        public void elementSelected(AbstractFile t) {}


        @Override
        public EmbeddedFile newPressed() {
            File f = FileDialog.showOpenDialog("Choose File", extensions);
            if (f != null) {
                try {
                    EmbeddedFile ef = parent.getEmbeddedFileManager().loadFile(f, list.getFileClass());
                    return ef;
                } catch (Exception ex) {
                    Log.log(LogLevel.ERROR, this, ex);
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
            return null;
        }

        @Override
        public void editPressed(AbstractFile t) {
            if (t != null) {
                new PropertiesDialog(parent, t, parent.getEmbeddedFileManager(), false);
                repaint();
            }
        }
    }

    @Override
    public boolean isResizable() {
        return true;
    }
}
