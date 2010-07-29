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
import java.io.File;

import javax.swing.JOptionPane;

import org.finroc.gui.FinrocGUI;
import org.finroc.gui.util.embeddedfiles.AbstractFile;
import org.finroc.gui.util.embeddedfiles.AbstractFiles;
import org.finroc.gui.util.embeddedfiles.EmbeddedFile;
import org.finroc.gui.util.gui.FileDialog;
import org.finroc.gui.util.gui.ListWithNewAndDeleteButton;
import org.finroc.log.LogLevel;


public class AbstractFilesEditor extends PropertyEditComponent<AbstractFiles<AbstractFile>> {

    /** UID */
    private static final long serialVersionUID = -225852580853828747L;

    AbstractFiles<AbstractFile> list;

    @Override
    protected void createAndShow() {
        list = getCurWidgetValue();
        add(new EmbeddedFilesEditorList(list, "Edit...", list.getExtensions()));
    }

    @Override
    public AbstractFiles<AbstractFile> getCurEditorValue() {
        return list;
    }

    class EmbeddedFilesEditorList extends ListWithNewAndDeleteButton<AbstractFile> {

        private String[] extensions;


        EmbeddedFilesEditorList(AbstractFiles<AbstractFile> files, String editButtonText, String[] extensions) {
            super(files, getPropertyName(), false, BorderLayout.SOUTH, editButtonText, true);
            this.extensions = extensions;
        }

        /** UID */
        private static final long serialVersionUID = 3305138623569659819L;

        @Override
        public void elementSelected(AbstractFile t) {}


        @Override
        public EmbeddedFile newPressed() {
            File f = FileDialog.showOpenDialog("Choose File", extensions);
            if (f != null) {
                try {
                    EmbeddedFile ef = getParentDialog().getEmbeddedFileManager().loadFile(f, list.getFileClass());
                    return ef;
                } catch (Exception ex) {
                    FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), ex);
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
            return null;
        }

        @Override
        public void editPressed(AbstractFile t) {
            if (t != null) {
                new PropertiesDialog(AbstractFilesEditor.this.getParentDialog(), t, getParentDialog().getEmbeddedFileManager(), false);
                repaint();
            }
        }
    }
}
