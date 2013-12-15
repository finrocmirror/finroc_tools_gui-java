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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.finroc.tools.gui.abstractbase.DataModelBase;

import org.finroc.core.FrameworkElement;
import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortWrapperBase;
import org.finroc.core.portdatabase.CCType;
import org.finroc.core.portdatabase.FinrocTypeInfo;
import org.finroc.core.remote.HasUid;
import org.finroc.core.remote.PortWrapperTreeNode;
import org.finroc.core.remote.RemotePort;
import org.rrlib.serialization.NumericRepresentation;
import org.rrlib.xml.XMLNode;

public abstract class WidgetPort < P extends PortWrapperBase > extends DataModelBase < GUI, Widget, WidgetPort<? >> implements PortWrapperTreeNode, Serializable {

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

    public List<PortWrapperTreeNode> getConnectionPartners() {
        ArrayList<PortWrapperTreeNode> result = new ArrayList<PortWrapperTreeNode>();
        if (port != null) {
            ArrayList<AbstractPort> connectionPartners = new ArrayList<AbstractPort>();
            port.getWrapped().getConnectionPartners(connectionPartners, true, true, false);
            for (AbstractPort port : connectionPartners) {
                RemotePort[] remotePorts = RemotePort.get(port);
                if (remotePorts != null) {
                    for (RemotePort remotePort : remotePorts) {
                        result.add(remotePort);
                    }
                }
            }
        }

        return result;
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
//  public boolean connectionPossibleTo(PortWrapperTreeNode pw) {
//      if (isInputPort()) {
//          return (!pw.isInputPort() && pw.getPort().getType().isAssignableFrom(port.getType()));
//      } else {
//          return (pw.isInputPort() && port.getType().isAssignableFrom(pw.getPort().getType()));
//      }
//  }
//
//  public abstract void connectTo(PortWrapperTreeNode other);
//
//  public abstract void connectTo(String uid);
//
//  public abstract void interfaceUpdated();
//
//  public abstract PortWrapperTreeNode[] getConnectionPartners();
//
//  public abstract void clearConnections();
//
//  public abstract void removeConnection(PortWrapperTreeNode pw);
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

    public void connectTo(PortWrapperTreeNode other) {
        if (getPort().isConnectedTo(other.getPort())) {
            return;
        }
        if (getPort().isInputPort()) {
            getPort().connectTo(((HasUid)other).getUid(), AbstractPort.ConnectDirection.TO_SOURCE, false);
        } else {
            getPort().connectTo(((HasUid)other).getUid(), AbstractPort.ConnectDirection.TO_TARGET, false);
        }
        connectedTo.add(((HasUid)other).getUid());
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
        if (p.getFlag(FrameworkElementFlags.ACCEPTS_DATA) && (defaultFlags & FrameworkElementFlags.PUSH_STRATEGY) != 0) {
            p.setPushStrategy(push);
        }
        if ((defaultFlags & FrameworkElementFlags.PUSH_STRATEGY_REVERSE) != 0) {
            p.setReversePushStrategy(push);
        }
    }

    /**
     * @return Unmodifiable set of connection links
     */
    public Set<String> getConnectionLinks() {
        return Collections.unmodifiableSet(connectedTo);
    }

    @Override
    public void serialize(XMLNode node) throws Exception {
        XMLNode connectionNode = node.addChildNode("connectedTo");
        for (String connection : connectedTo) {
            connectionNode.addChildNode("string").setContent(connection);
        }
        node.addChildNode("description").setContent(description);
    }

    @Override
    public void deserialize(XMLNode node) throws Exception {
        connectedTo.clear();
        for (XMLNode child : node.children()) {
            if (child.getName().equals("connectedTo")) {
                for (XMLNode connectionNode : child.children()) {
                    connectedTo.add(connectionNode.getTextContent());
                }
            } else if (child.getName().equals("description")) {
                description = child.getTextContent();
            }
        }
    }
}
