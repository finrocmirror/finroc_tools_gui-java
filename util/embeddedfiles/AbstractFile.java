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
import java.io.Serializable;

import org.finroc.tools.gui.util.propertyeditor.NotInPropertyEditor;
import org.rrlib.serialization.XMLSerializable;
import org.rrlib.xml.XMLNode;

/**
 * @author Max Reichardt
 *
 * This is the abstract base class for files that are used in GUIs
 */
public abstract class AbstractFile implements Serializable, XMLSerializable {

    /** UID */
    private static final long serialVersionUID = 9030547614693056384L;

    /** The filename - when last seen/loaded */
    @NotInPropertyEditor
    private String filename;

    /** The file modification date - when last seen/loaded */
    @NotInPropertyEditor
    private long date;

    /** The file size - when last seen/loaded */
    @NotInPropertyEditor
    private long size;

    @Deprecated // only for deserialization
    public AbstractFile() {}

    /**
     * Creates EmbeddedFile from some file
     */
    protected AbstractFile(File file) {
        filename = file.getName();
        date = file.lastModified();
        size = file.length();
    }

    /**
     * Dummy constructor for loading from zip
     */
    protected AbstractFile(long size) {
        filename = "Dummy";
        date = 0;
        this.size = size;
    }

    /**
     * Called after loading
     *
     * @return True if file was successfully initialized and can be used
     */
    abstract boolean init(FileManager efm);

    /** @return The filename - when last seen/loaded */
    public String getFileName() {
        return filename;
    }

    public String toString() {
        return getFileName();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!o.getClass().equals(this.getClass())) {
            return false;
        }
        if (o instanceof AbstractFile) {
            AbstractFile ef = (AbstractFile)o;
            return (this.getClass().equals(o.getClass()) && date == ef.date && filename.equals(ef.filename) && size == ef.size);
        }
        return false;
    }

    @Override
    public void serialize(XMLNode node) throws Exception {
        node.addChildNode("filename").setContent(filename);
        node.addChildNode("date").setContent("" + date);
        node.addChildNode("size").setContent("" + size);
    }

    @Override
    public void deserialize(XMLNode node) throws Exception {
        for (XMLNode child : node.children()) {
            if (child.getName().equals("filename")) {
                filename = child.getTextContent();
            } else if (child.getName().equals("date")) {
                date = Long.parseLong(child.getTextContent());
            } else if (child.getName().equals("size")) {
                size = Long.parseLong(child.getTextContent());
            }
        }
    }
}
