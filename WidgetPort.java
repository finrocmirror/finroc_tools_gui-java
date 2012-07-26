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
package org.finroc.tools.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.util.treemodel.PortWrapper;
import org.finroc.tools.gui.util.treemodel.TreePortWrapper;

import org.finroc.core.FrameworkElement;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.PortWrapperBase;
import org.finroc.core.portdatabase.CCType;
import org.finroc.core.portdatabase.FinrocTypeInfo;
import org.rrlib.finroc_core_utils.serialization.NumericRepresentation;

public abstract class WidgetPort < P extends PortWrapperBase > extends DataModelBase < GUI, Widget, WidgetPort<? >> implements TreePortWrapper, Serializable {

    /** UID & protected empty constructor */
    private static final long serialVersionUID = 88243609872346L;

    /** UIDs of ports that this port is connected to */
    private Set<String> connectedTo = new HashSet<String>();

    /** name/description of port (redundant - for serialization) */
    protected String description;

    /** Default flags of port - stored to restore original strategies */
    protected transient int defaultFlags;

    /** Wrapped port */
    protected transient P port;

    @Override
    protected FrameworkElement createFrameworkElement() {
        if (port == null) {
            port = createPort();
        }
        return port.getWrapped();
    }

    /**
     * @return Created port
     */
    protected abstract P createPort();

    public P asPort() {
        return port;
    }

    @Override
    public AbstractPort getPort() {
        return (AbstractPort)frameworkElement;
    }

    public WidgetPort() {
        super(null);
    }

    @Override
    public boolean isInputPort() {
        return !getPort().isInputPort();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void restore(Widget parent) {
        super.restore(parent);
        parent.getFrameworkElement().addChild(frameworkElement);
        frameworkElement.init();
        for (String s : connectedTo) {
            if (getPort().isInputPort()) {
                getPort().connectTo(s, AbstractPort.ConnectDirection.TO_SOURCE, false);
            } else {
                getPort().connectTo(s, AbstractPort.ConnectDirection.TO_TARGET, false);
            }
        }
        if (defaultFlags == 0) {
            defaultFlags = getPort().getAllFlags();
        }
    }

    public void clearConnections() {
        connectedTo.clear();
        getPort().disconnectAll();
    }

    public void removeConnection(AbstractPort p, String uid) {
        if (connectedTo.remove(uid)) {
            getPort().disconnectFrom(p);
        }
    }

    public String getUid() {
        return "not relevant";
    }

    public List<PortWrapper> getConnectionPartners() {
        ArrayList<PortWrapper> r = new ArrayList<PortWrapper>();
        for (String s : connectedTo) {
            if (isInputPort()) {
                r.add(getRoot().getFingui().getOutput(s));
            } else {
                r.add(getRoot().getFingui().getInput(s));
            }
        }
        return r;
    }

//  public void addChangeListener(PortListener cl) {
//      port.addChangeListener(cl);
//  }
//
//  public boolean containsLargeData() {
//      return port.containsLargeData();
//  }
//
//  public Class<T> getType() {
//      return port.getType();
//  }
//
//  public T getValue() {
//      return port.getValue();
//  }
//
//  public boolean hasChanged() {
//      return port.hasChanged();
//  }
//
//  public void removeChangeListener(PortListener cl) {
//      port.removeChangeListener(cl);
//  }
//
//  public String getDescription() {
//      return port.getDescription();
//  }
//
//  public void setChanged(boolean changed) {
//      port.setChanged(changed);
//  }
//
//  public void setValue(T value, boolean forceChangeFlag) {
//      port.setValue(value, forceChangeFlag);
//  }
//
//  public void setValue(T value) {
//      port.setValue(value);
//  }
//
    public String toString() {
        return description;
    }
//
//  public WidgetPort(Class<T> type, T defaultValue, String description) {
//      super(null);
//      port = new IOPort<T>(type, defaultValue, description);
//  }
//
//  @SuppressWarnings("unchecked")
//  public boolean connectionPossibleTo(PortWrapper pw) {
//      if (isInputPort()) {
//          return (!pw.isInputPort() && pw.getPort().getType().isAssignableFrom(port.getType()));
//      } else {
//          return (pw.isInputPort() && port.getType().isAssignableFrom(pw.getPort().getType()));
//      }
//  }
//
//  public abstract void connectTo(PortWrapper other);
//
//  public abstract void connectTo(String uid);
//
//  public abstract void interfaceUpdated();
//
//  public abstract PortWrapper[] getConnectionPartners();
//
//  public abstract void clearConnections();
//
//  public abstract void removeConnection(PortWrapper pw);
//
//  public void setParent(Widget parent) {
//      this.parent = parent;
//  }
//
//  public String getUid() {
//      return null;
//  }
//
//  public Port<T> getPort() {
//      return port;
//  }
//
//  public void setDescription(String description) {
//      port.setDescription(description);
//  }

    public void connectTo(PortWrapper other) {
        if (getPort().isConnectedTo(other.getPort())) {
            return;
        }
        if (getPort().isInputPort()) {
            getPort().connectTo(other.getUid(), AbstractPort.ConnectDirection.TO_SOURCE, false);
        } else {
            getPort().connectTo(other.getUid(), AbstractPort.ConnectDirection.TO_TARGET, false);
        }
        connectedTo.add(other.getUid());
    }

    /**
     * update port strategy
     *
     * @param push Enable pushing if this is the default?
     */
    public void updateStrategy(boolean push) {
        AbstractPort p = getPort();
        if (FinrocTypeInfo.isCCType(p.getDataType()) || (p.getDataType().getJavaClass() != null && (CCType.class.isAssignableFrom(p.getDataType().getJavaClass()) || NumericRepresentation.class.isAssignableFrom(p.getDataType().getJavaClass())))) { // not worth changing strategies for cc types
            return;
        }
        if (p.getFlag(PortFlags.ACCEPTS_DATA) && (defaultFlags & PortFlags.PUSH_STRATEGY) != 0) {
            p.setPushStrategy(push);
        }
        if (p.getFlag(PortFlags.MAY_ACCEPT_REVERSE_DATA) && (defaultFlags & PortFlags.PUSH_STRATEGY_REVERSE) != 0) {
            p.setReversePushStrategy(push);
        }
    }

    /**
     * @return Unmodifiable set of connection links
     */
    public Set<String> getConnectionLinks() {
        return Collections.unmodifiableSet(connectedTo);
    }
}
