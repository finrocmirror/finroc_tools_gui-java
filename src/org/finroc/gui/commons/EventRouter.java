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
package org.finroc.gui.commons;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

import org.finroc.gui.WidgetPorts;
import org.finroc.gui.WidgetPortsListener;

import org.finroc.core.plugin.ConnectionListener;
import org.finroc.core.plugin.ExternalConnection;
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
 * Class to add/remove listeners from objects and fire events
 *
 * Since FinGUI has lots of observables and observers and managing them all got
 * quite complex (especially removing all listeners), this class was introduced
 * to do that task.
 *
 * And since everything is stored in one place, more advanced stuff can be done, too :-)
 *
 * Note: Only WeakHashMaps are used in this class, to prevent memory leaks.
 * Furthermore objects are automatically removed :-)
 *
 *
 * IMPORTANT: Only use objects as observers and observables that have a constant hash code.
 * This is usually the case, but for example not with (classes extending) Java Collection
 * classes.
 */
@SuppressWarnings("rawtypes")
public class EventRouter implements PortListener, CCPortListener, ConnectionListener {

    /** Map for storing connections: Observable, listener, Listener type */
    private static Map < Object, Map < EventListener, List < Class <? extends EventListener >>> > listeners = new WeakHashMap < Object, Map < EventListener, List < Class <? extends EventListener >>> > ();

    /** Cache for ordinary add***Listener methods */
    private static Map<String, Method> methodCache;

    /** singleton instance */
    private static EventRouter instance;

    public static EventRouter getInstance() {
        if (instance == null) {
            instance = new EventRouter();
        }
        return instance;
    }

    public synchronized static void addListener(Object observable, EventListener observer, Class <? extends EventListener > type) {
        if (observable == null || observer == null || type == null) {
            return;
        }

        List < Class <? extends EventListener >> list = getListenerTypes(observable, observer);

        // add listener
        if (!list.contains(type)) {
            list.add(type);
        }
    }

    public synchronized static void addListener(Object observable, String addMethodName, EventListener observer) {
        if (observable == null || observer == null || addMethodName == null) {
            return;
        }
        assert(addMethodName.startsWith("add") && (addMethodName.endsWith("Listener") || addMethodName.endsWith("ListenerRaw")));
        String key = observable.getClass().getName() + "." + addMethodName;

        // make sure cache has been initialized
        if (methodCache == null) {
            methodCache = new WeakHashMap<String, Method>();
        }

        // get addMethod-Object (hopefully from cache)
        Method addMethod = methodCache.get(key);
        if (addMethod == null) {
            for (Method m : observable.getClass().getMethods()) {
                if (m.getName() == addMethodName) {
                    addMethod = m;
                    break;
                }
            }
            if (addMethod == null) {
                throw new RuntimeException(addMethodName + " not found in class " + observable.getClass());
            }
            methodCache.put(key, addMethod);
        }

        // register Router as listener
        try {
            addMethod.invoke(observable, getInstance());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        addListener(observable, observer, (Class <? extends EventListener >)addMethod.getParameterTypes()[0]);
    }

    public synchronized static void removeListener(Object observable, EventListener observer, Class <? extends EventListener > type) {
        if (observable != null && observer != null && type != null) {
            getListenerTypes(observable, observer).remove(type);
        }
    }

    public synchronized static void objectDisposed(Object o) {

        // remove listeners
        listeners.remove(o);

        // remove as listener
        if (o instanceof EventListener) {
            removeAsListener((EventListener)o);
        }
    }

    public synchronized static void removeAsListener(EventListener observer) {
        for (Entry < Object, Map < EventListener, List < Class <? extends EventListener >>> > entry : listeners.entrySet()) {
            entry.getValue().remove(observer);
        }
    }

    private synchronized static List < Class <? extends EventListener >> getListenerTypes(Object observable, EventListener observer) {
        // make sure 'listeners' has been initialized
        /*if (listeners == null) {
            listeners =
        }*/
        Map < EventListener, List < Class <? extends EventListener >>> mapOfObj = listeners.get(observable);
        if (mapOfObj == null) {
            mapOfObj = new WeakHashMap < EventListener, List < Class <? extends EventListener >>> ();
            listeners.put(observable, mapOfObj);
        }
        List < Class <? extends EventListener >> list = mapOfObj.get(observer);
        if (list == null) {
            list = new ArrayList < Class <? extends EventListener >> ();
            mapOfObj.put(observer, list);
        }
        return list;
    }


    public synchronized static <T extends EventListener> Collection<T> getListeners(Object caller, Class<T> listenerType) {
        List<T> result = new ArrayList<T>();
        try {
            for (Entry < EventListener, List < Class <? extends EventListener >>> entry : listeners.get(caller).entrySet()) {
                if (entry.getValue().contains(listenerType)) {
                    result.add((T)entry.getKey());
                }
            }
        } catch (Exception e) {}
        return result;
    }

    public synchronized static void removeListeners(Object observable, Class <? extends EventListener > listenerType) {

        for (Entry < EventListener, List < Class <? extends EventListener >>> entry : listeners.get(observable).entrySet()) {
            try {
                entry.getValue().remove(listenerType);
            } catch (Exception e) {}
        }
    }

    public synchronized static boolean hasListeners(Object observable, Class <? extends EventListener > listenerType) {
        if (!listeners.containsKey(observable)) {
            return false;
        }
        for (Entry < EventListener, List < Class <? extends EventListener >>> entry : listeners.get(observable).entrySet()) {
            if (entry.getValue().contains(listenerType)) {
                return true;
            }
        }
        return false;
    }

    // Below: only stuff for specific Listeners

    @Override
    public void portChanged(PortBase origin, PortData value) {
        fireChangeEvent(origin, value);
    }

    @Override
    public void portChanged(CCPortBase origin, CCPortData value) {
        fireChangeEvent(origin, value);
    }

    public void connectionEvent(ExternalConnection source, int e) {
        fireConnectionEvent(source, e);
    }

    public static void fireConnectionEvent(ExternalConnection source, int e) {
        for (ConnectionListener el : getListeners(source, ConnectionListener.class)) {
            el.connectionEvent(source, e);
        }
    }

    public static void fireChangeEvent(PortBase source, PortData value) {
        for (PortListener el : getListeners(source, PortListener.class)) {
            el.portChanged(source, value);
        }
    }

    public static void fireChangeEvent(CCPortBase source, CCPortData value) {
        for (CCPortListener el : getListeners(source, CCPortListener.class)) {
            el.portChanged(source, value);
        }
    }

    public static void fireChangeEvent(WidgetPorts wi, AbstractPort source, Object value) {
        for (WidgetPortsListener el : getListeners(wi, WidgetPortsListener.class)) {
            el.portChanged(wi, source, value);
        }
    }

//  public static void fireChangeEvent(Object observable, Port source) {
//      for (PortListener el : getListeners(observable, PortListener.class)) {
//          el.valueChanged(source);
//      }
//  }


//  public static void fireReverseChangeEvent(Object source) {
//      for (OutputPortListener el : getListeners(source, OutputPortListener.class)) {
//          el.outputValueChanged(source);
//      }
//  }

    public static void fireMouseClickedEvent(Object source, MouseEvent e) {
        for (MouseListener ml : getListeners(source, MouseListener.class)) {
            ml.mouseClicked(e);
        }
    }
    public static void fireMouseEnteredEvent(Object source, MouseEvent e) {
        for (MouseListener ml : getListeners(source, MouseListener.class)) {
            ml.mouseEntered(e);
        }
    }
    public static void fireMouseExitedEvent(Object source, MouseEvent e) {
        for (MouseListener ml : getListeners(source, MouseListener.class)) {
            ml.mouseExited(e);
        }
    }
    public static void fireMousePressedEvent(Object source, MouseEvent e) {
        for (MouseListener ml : getListeners(source, MouseListener.class)) {
            ml.mousePressed(e);
        }
    }
    public static void fireMouseReleasedEvent(Object source, MouseEvent e) {
        for (MouseListener ml : getListeners(source, MouseListener.class)) {
            ml.mouseReleased(e);
        }
    }
    public static void fireMouseDraggedEvent(Object source, MouseEvent e) {
        for (MouseMotionListener ml : getListeners(source, MouseMotionListener.class)) {
            ml.mouseDragged(e);
        }
    }
    public static void fireMouseMovedEvent(Object source, MouseEvent e) {
        for (MouseMotionListener ml : getListeners(source, MouseMotionListener.class)) {
            ml.mouseMoved(e);
        }
    }
}
