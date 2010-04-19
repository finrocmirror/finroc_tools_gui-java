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
package org.finroc.gui.util.embeddedfiles;

import java.io.File;

/**
 * @author max
 *
 */
public class VirtualFile extends File {

    /** UID */
    private static final long serialVersionUID = 7416146199514967291L;

    private long time;

    private byte[] data;

    public VirtualFile(String pathname) {
        super(pathname);
        time = System.currentTimeMillis();
    }

    public VirtualFile(String pathname, byte[] data) {
        this(pathname);
        setData(data);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public long length() {
        return data.length;
    }

    @Override
    public long lastModified() {
        return time;
    }

    public byte[] getData() {
        return data;
    }

}
