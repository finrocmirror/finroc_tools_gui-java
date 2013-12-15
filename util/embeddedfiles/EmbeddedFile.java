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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.finroc.tools.gui.util.propertyeditor.NotInPropertyEditor;
import org.rrlib.xml.XMLNode;


/**
 * @author Max Reichardt
 *
 * This class represents an embedded file in the GUI-File
 * Once loaded it's immutable
 *
 * Annahme: Wenn Dateiname, Änderungsdatum und Größe übereinstimmen, dann handelt es sich
 * um dieselbe Datei.
 */
public class EmbeddedFile extends AbstractFile {

    /** UID */
    private static final long serialVersionUID = 5523351982209183670L;

    /** UID of this file */
    @NotInPropertyEditor
    private long uid;

    /**
     * Creates EmbeddedFile from some file
     */
    protected EmbeddedFile(File file) {
        super(file);
        uid = System.currentTimeMillis();
    }

    /**
     * Dummy file for loading
     */
    protected EmbeddedFile(long uid, long size) {
        super(size);
        this.uid = uid;
    }

    @Deprecated // only for deserialization
    public EmbeddedFile() {}

    @Override
    boolean init(FileManager efm) {
        return true;
    }

    public byte[] getData(FileManager efm) {
        if (efm == null) {
            throw new RuntimeException("EmbeddedFileManager not set");
        }
        return efm.getData(getUid());
    }

    public InputStream getInputStream(FileManager efm) {
        return new ByteArrayInputStream(getData(efm));
    }

    public String getUniqueZipFilename() {
        return "embeddedfiles/" + uid;
    }

    long getUid() {
        return uid;
    }

    void setUid(long uid2) {
        uid = uid2;
    }

    @Override
    public void serialize(XMLNode node) throws Exception {
        super.serialize(node);
        node.addChildNode("uid").setContent("" + uid);
    }

    @Override
    public void deserialize(XMLNode node) throws Exception {
        super.deserialize(node);
        for (XMLNode child : node.children()) {
            if (child.getName().equals("uid")) {
                uid = Long.parseLong(child.getTextContent());
            }
        }
    }
}
