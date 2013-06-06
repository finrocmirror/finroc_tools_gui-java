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
package org.finroc.tools.gui.util.embeddedfiles;

import java.io.File;

/**
 * @author Max Reichardt
 *
 * File or directory that lies in file system... if it cannot be embedded in GUI file
 */
public class ExternalFile extends AbstractFile {

    /** UID */
    private static final long serialVersionUID = -949313366936519301L;

    /** absolute file name when file was loaded */
    private final String absFilename;

    /** relative file name (to some resource directory) when file was loaded */
    String relFilename;

    /** File in current file system */
    transient private File file;

    protected ExternalFile(File file) {
        super(file);
        this.file = file;
        absFilename = file.getAbsolutePath();
    }

    @Override
    void init(FileManager efm) {
        updateRelativeFilename(efm);
    }

    /**
     * Update relative file name - in case resource paths have changed
     */
    public void updateRelativeFilename(FileManager fileManager) {
        relFilename = fileManager.getRelativeFilename(file, relFilename);
    }

    public String toString() {
        if (relFilename == null || relFilename.length() <= 0) {
            return absFilename;
        } else {
            return relFilename;
        }
    }

    public File getFile(FileManager fileManager) {
        if (file != null && file.exists()) {
            return file;
        }
        file = fileManager.findExternalFileAgain(absFilename, relFilename);
        return file;
    }
}
