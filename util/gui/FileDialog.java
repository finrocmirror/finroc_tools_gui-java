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
package org.finroc.tools.gui.util.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.finroc.tools.gui.util.ExtensionFilenameFilter;

/**
 * @author max
 *
 * File-Open/Save-Dialog
 */
public class FileDialog {

    private static File lastDir;

    public static File showSaveDialog(String title) {
        return showSaveDialog(title, null);
    }

    public static File showOpenDialog(String title, String[] extensions) {
        return showOpenDialog(title, extensions, false);
    }

    public static File showOpenDialog(String title, String[] extensions, boolean dirSelectable) {

        // init dialog
        JFileChooser instance = new JFileChooser();
        if (extensions != null && extensions.length > 0) {
            instance.setFileFilter(new ExtensionFilenameFilter(extensions));
        } else {
            instance.setFileFilter(null);
        }
        instance.setFileSelectionMode(dirSelectable ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
        instance.setDialogTitle(title);
        if (lastDir != null) {
            instance.setCurrentDirectory(lastDir);
        }

        // show dialog
        instance.showOpenDialog(null);

        // process result
        lastDir = instance.getCurrentDirectory();
        return instance.getSelectedFile();
    }

    public static File showOpenDialog(String title) {
        return showOpenDialog(title, new String[0]);
    }

    public static File showSaveDialog(String title, String stdExtension) {

        // init dialog
        JFileChooser instance = new JFileChooser();
        FileFilter ff = null;
        if (stdExtension != null) {
            ff = new ExtensionFilenameFilter(new String[] {stdExtension});
            instance.setFileFilter(ff);
        }
        instance.setDialogTitle(title);
        if (lastDir != null) {
            instance.setCurrentDirectory(lastDir);
        }

        // show dialog
        instance.showSaveDialog(null);

        // process result
        lastDir = instance.getCurrentDirectory();
        File f = instance.getSelectedFile();
        if (f == null) {
            return null;
        }
        if (ff != null) {
            if (!ff.accept(f)) {
                f = new File(f.getAbsolutePath() + "." + stdExtension);
            }
        }
        return f;
    }

    public static File showOpenDialog(String title, String extension) {
        return showOpenDialog(title, new String[] {extension});
    }

    public static File showOpenPathDialog(String title) {
        return showOpenDialog(title, new String[0], true);
    }
}
