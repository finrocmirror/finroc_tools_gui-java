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
package org.finroc.tools.gui.util.propertyeditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.finroc.tools.gui.Widget;
import org.rrlib.serialization.XMLSerializable;
import org.rrlib.xml.XMLNode;

/**
 * @author Max Reichardt
 *
 * List of property entries (with possibly multiple attributes)
 */
public class PropertyList<T extends Serializable> extends ArrayList<T> implements PropertyListAccessor<T>, XMLSerializable {

    /** UID */
    private static final long serialVersionUID = 3604338104379425977L;

    /** Class of entries in this list */
    private Class<T> entryClass;

    /** Maximum entries in this list */
    private int maxEntries;

    /** Single instance of XML serialization class */
    private static final Widget.XMLSerializer XML_SERIALIZER = new Widget.XMLSerializer();

    public PropertyList(Class<T> entryClass, int maxEntries) {
        this.entryClass = entryClass;
        this.maxEntries = maxEntries;
    }

    @Override
    public int getMaxEntries() {
        return maxEntries;
    }

    @Override
    public Class<T> getElementType() {
        return entryClass;
    }

    @Override
    public List < PropertyAccessor<? >> getElementAccessors(T element) {
        return FieldAccessorFactory.getInstance().createAccessors(element);
    }

    @Override
    public void addElement() {
        try {
            add(entryClass.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeElement(int index) {
        remove(index);
    }

    @Override
    public void serialize(XMLNode node) throws Exception {
        for (T entry : this) {
            XMLNode childNode = node.addChildNode(entryClass.getSimpleName());
            XML_SERIALIZER.serialize(childNode, entry);
        }
    }

    @Override
    public void deserialize(XMLNode node) throws Exception {
        int deserialized = 0;
        for (XMLNode child : node.children()) {
            if (child.getName().equals(entryClass.getSimpleName())) {
                if (deserialized >= size()) {
                    addElement();
                }
                XML_SERIALIZER.deserialize(child, get(deserialized), true);
                deserialized++;
            }
        }
        while (deserialized < size()) {
            remove(size() - 1);
        }
    }
}
