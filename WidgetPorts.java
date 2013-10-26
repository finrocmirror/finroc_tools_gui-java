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
package org.finroc.tools.gui;

import java.util.ArrayList;

import org.finroc.tools.gui.commons.EventRouter;

import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortListener;
import org.finroc.core.port.cc.CCPortBase;
import org.finroc.core.port.std.PortBase;

/**
 * @author Max Reichardt
 *
 */
@SuppressWarnings("rawtypes")
public class WidgetPorts < P extends WidgetPort<? >> extends ArrayList<P> implements PortListener {

    /** UID */
    private static final long serialVersionUID = 3502191793248052616L;

    private final Widget parent;
    private String portNamePrefix;
    private Class <? extends WidgetPort > type;
    private transient Object hashDelegate;  // necessary that this can be used with EventRouter
    private transient boolean initialized;

    public WidgetPorts(String portNamePrefix, int initialSize, Class <? extends WidgetPort > type, Widget parent) {
        this.portNamePrefix = portNamePrefix;
        this.type = type;
        this.parent = parent;
        initialized = true;
        //setSize(initialSize);
    }

    public int hashCode() {  // necessary that this can be used with EventRouter
        if (hashDelegate == null) {
            hashDelegate = new Object();
        }
        return hashDelegate.hashCode();
    }

    public void initialize() {
        for (P p : this) {
            p.restore(parent);
            addChangeListener(p);
        }
        initialized = true;
    }

    @SuppressWarnings("unchecked")
    public void setSize(int newSize) {

        // re-register as listener after loading
        if (!initialized) {
            initialize();
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
    public void portChanged(AbstractPort origin, Object value) {
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
