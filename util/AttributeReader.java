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
package org.finroc.tools.gui.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.management.Attribute;

public class AttributeReader extends BufferedReader {

    public AttributeReader(Reader in) {
        super(in);
    }

    public String[] readSeperatedAttribute(String attrName, String separator) throws IOException {
        String s = readAttribute(attrName);
        if (s.trim().length() <= 0) {
            return new String[0];
        }
        String[] result = s.split(",");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].trim();
        }
        return result;
    }

    public String readAttribute(String attrName) throws IOException {
        Attribute a = readNextAttribute();
        if (!a.getName().equalsIgnoreCase(attrName)) {
            throw new IOException("Wrong attribute name: " + a.getName());
        }
        return a.getValue().toString();
    }

    public Attribute readNextAttribute() throws IOException {
        String s = readLine();
        String name = s.substring(0, s.indexOf(":")).trim();
        String value = s.substring(s.indexOf(":") + 1).trim();
        return new Attribute(name, value);
    }
}
