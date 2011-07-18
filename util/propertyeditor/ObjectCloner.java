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
package org.finroc.tools.gui.util.propertyeditor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;

/**
 * @author max
 *
 * Utility class for creating complete, deep clones of objects
 */
public class ObjectCloner {

    /** Cloneable interface with public clone-method */
    public interface Cloneable {

        /** Clone object */
        public Object clone();
    }

    /** List with available cloners, in order they will be used - contains ObjectCloner by default */
    private static final LinkedList<CloneHandler> cloners = new LinkedList<CloneHandler>();

    static {
        cloners.add(new CloneHandler() {
            @Override
            public boolean handles(Object o) {
                return o == null || o instanceof Serializable || o instanceof Cloneable;
            }

            @Override
            public <T> T clone(T t) {
                return cloneImpl(t);
            }

        });
    }

    /**
     * Register additional handler
     *
     * @param handler Handler
     * @param addBeforeDefault Try this handler before default object cloner?
     */
    public static void registerCloner(CloneHandler handler, boolean addBeforeDefault) {
        if (addBeforeDefault) {
            cloners.addFirst(handler);
        } else {
            cloners.addLast(handler);
        }
    }

    /**
     * @param t Object to clone
     * @return Cloned object
     */
    public static <T> T clone(T t) {
        for (CloneHandler ch : cloners) {
            if (ch.handles(t)) {
                return ch.clone(t);
            }
        }
        throw new RuntimeException("No cloner found for type " + t.toString());
    }

    @SuppressWarnings("unchecked")
    private static <T> T cloneImpl(T t) {
        if (t == null) {
            return null;
        }
        if (t instanceof String || t instanceof Number) { // directly return immutable objects
            return t;
        }
        if (t instanceof Serializable) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(t);
                oos.close();
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
                T result = (T)ois.readObject();
                ois.close();
                return result;
            } catch (Exception e) {
                throw new RuntimeException("Cloning object failed", e);
            }
        } else {
            assert(t instanceof Cloneable);
            return (T)((Cloneable)t).clone();
        }

    }

    /**
     * Serialize Serializable object to byte array
     *
     * @param t Object
     * @return Byte array
     */
    public static byte[] toByteArray(Serializable t) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(t);
            oos.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Converting object to byte array failed", e);
        }
    }

    /**
     * Compare two byte array
     *
     * @param b1 Array 1
     * @param b2 Array 2
     * @return Are arrays equal?
     */
    public static boolean equal(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) {
            return false;
        }
        for (int i = 0 ; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }
}