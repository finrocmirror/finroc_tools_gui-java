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
package org.finroc.tools.gui.util.embeddedfiles;

import java.io.File;

import org.rrlib.xml.XMLNode;

/**
 * @author Max Reichardt
 *
 * File or directory that lies in file system... if it cannot be embedded in GUI file
 */
public class ExternalFile extends AbstractFile {

    /** UID */
    private static final long serialVersionUID = -949313366936519301L;

    /** absolute file name when file was loaded */
    private String absFilename;

    /** relative file name (to some resource directory) when file was loaded */
    String relFilename;

    /** File in current file system */
    transient private File file;

    @Deprecated // only for deserialization
    public ExternalFile() {}

    protected ExternalFile(File file) {
        super(file);
        this.file = file;
        absFilename = file.getAbsolutePath();
    }

    @Override
    boolean init(FileManager efm) {
        updateRelativeFilename(efm);
        return true;
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

    @Override
    public void serialize(XMLNode node) throws Exception {
        super.serialize(node);
        node.addChildNode("absFilename").setContent(absFilename);
        node.addChildNode("relFilename").setContent(relFilename);
    }

    @Override
    public void deserialize(XMLNode node) throws Exception {
        super.deserialize(node);
        for (XMLNode child : node.children()) {
            if (child.getName().equals("absFilename")) {
                absFilename = child.getTextContent();
            } else if (child.getName().equals("relFilename")) {
                relFilename = child.getTextContent();
            }
        }
    }

}
