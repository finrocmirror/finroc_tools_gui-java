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
package org.finroc.gui;

import java.util.ArrayList;

import org.finroc.gui.commons.EventRouter;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.cc.CCPortData;
import org.finroc.core.port.cc.CCPortListener;
import org.finroc.core.port.std.PortBase;
import org.finroc.core.port.std.PortData;
import org.finroc.core.port.std.PortListener;


/**
 * @author max
 *
 */
@SuppressWarnings("unchecked")
public class WidgetPorts<P extends WidgetPort<?>> extends ArrayList<P> implements PortListener, CCPortListener {

    /** UID */
    private static final long serialVersionUID = 3502191793248052616L;

    private final Widget parent;
    private String portNamePrefix;
    private Class<? extends WidgetPort> type;
    private transient Object hashDelegate;  // necessary that this can be used with EventRouter
    transient boolean initialized;

    public WidgetPorts(String portNamePrefix, int initialSize, Class<? extends WidgetPort> type, Widget parent) {
        this.portNamePrefix = portNamePrefix;
        this.type = type;
        this.parent = parent;
        initialized = true;
        setSize(initialSize);
    }

    public int hashCode() {  // necessary that this can be used with EventRouter
        if (hashDelegate == null) {
            hashDelegate = new Object();
        }
        return hashDelegate.hashCode();
    }

    public void setSize(int newSize) {

        // re-register as listener after loading
        if (!initialized) {
            for (P p : this) {
                addChangeListener(p);
            }
            initialized = true;
        }

        if (newSize < size()) {
            removeRange(newSize, size());
        } else if (newSize > size()) {
            while (newSize > size()) {
                try {
                    P wp = (P)type.getConstructor().newInstance();
                    wp.description = portNamePrefix + " " + (size() + 1);
                    wp.restore(parent);
                    addChangeListener(wp);
                    add(wp);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void addChangeListener(P p) {
        if (p.getPort() instanceof PortBase || p.getPort() instanceof CCPortBase) {
            EventRouter.addListener(p.getPort(), "addPortListenerRaw", this);
        }
    }

    public void addChangeListener(WidgetPortsListener l) {
        EventRouter.addListener(this, l, WidgetPortsListener.class);
    }

    public void removeChangeListener(WidgetPortsListener l) {
        EventRouter.removeListener(this, l, WidgetPortsListener.class);
    }

    @Override
    public void portChanged(PortBase origin, PortData value) {
        EventRouter.fireChangeEvent(this, origin, value);
    }

    @Override
    public void portChanged(CCPortBase origin, CCPortData value) {
        EventRouter.fireChangeEvent(this, origin, value);
    }

    public int indexOf(AbstractPort ap) {
        for (int i = 0; i < size(); i++) {
            if (get(i).getPort() == ap) {
                return i;
            }
        }
        return -1;
    }

//  public void valueChanged(Port source) {
//      EventRouter.fireChangeEvent(this, source);
//  }
}
