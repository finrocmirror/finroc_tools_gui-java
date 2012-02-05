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

import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.PortWrapperBase;


/**
 * @author max
 *
 * This is a output port of a widget
 */
public abstract class WidgetOutputPort < P extends PortWrapperBase > extends WidgetPort<P> { /*implements OutputPort<T>, PortListener*/

    /** UID */
    private static final long serialVersionUID = 4183113499724602127L;

    static PortCreationInfo stdPci = new PortCreationInfo("", PortFlags.OUTPUT_PORT);

    public PortCreationInfo getPci() {
        PortCreationInfo def = stdPci.derive(getDescription());
        PortCreationInfo pci = getParent().getPortCreationInfo(def, this);
        return pci == null ? def : pci;
    }

//  private Set<String> connections;
//
//  private boolean settingValue;
//
//  public WidgetOutputPort(Class<T> type, T defaultValue, String description) {
//      super(type, defaultValue, description);
//  }
//
//
//  public void addOutputPortListener(OutputPortListener opl) {
//      EventRouter.addListener(this, opl, OutputPortListener.class);
//  }
//
//
//  public void removeOutputPortListener(OutputPortListener opl) {
//      EventRouter.removeListener(this, opl, OutputPortListener.class);
//  }
//
//
//  public void restore(Widget parent) {
//      super.restore(parent);
//      checkInitialized();
//      addChangeListener(this);
//      interfaceUpdated();
//  }
//
//  private void checkInitialized() {
//      if (connections == null) {
//          connections = new HashSet<String>();
//      }
//  }
//
//  public void dispose() {
//      EventRouter.objectDisposed(this);
//      connections = null;
//  }
//
//  @Override
//  public void interfaceUpdated() {
//      register();
//  }
//
//
//  @SuppressWarnings("unchecked")
//  private synchronized void register() {
//      PortWrapper[] ip = getConnectionPartners();
//      EventRouter.removeAsListener(this);
//      addChangeListener(this);
//      for (PortWrapper p : ip) {
//          if (p != null) {
//              p.getPort().addChangeListener(this);
//          }
//      }
//      for (PortWrapper p : ip) {
//          if (p != null) {
//              setValueReverse((T)p.getPort().getValue());
//              return;
//          }
//      }
//  }
//
//  public void setValueReverse(T value) {
//      port.setValueQuiet(value);
//      EventRouter.fireReverseChangeEvent(this);
//  }
//
//
//  @SuppressWarnings("unchecked")
//  @Override
//  public synchronized void connectTo(PortWrapper pw) {
//      checkInitialized();
//      connections.add(pw.getUid());
//      pw.getPort().addChangeListener(this);
//      settingValue = true;
//      try {
//          setValueReverse((T)pw.getPort().getValue());
//      } finally {
//          settingValue = false;
//      }
//  }
//
//
//  @SuppressWarnings("unchecked")
//  public synchronized void valueChanged(Port source) {
//      if (settingValue) {
//          return;
//      }
//
//      if (source == getPort()) {  // Widget has changed value
//          for (PortWrapper p : getConnectionPartners()) {
//              if (p != null) {
//                  settingValue = true;
//                  try {
//                      ((OutputPort)p.getPort()).setValue(getValue());
//                  } finally {
//                      settingValue = false;
//                  }
//              }
//          }
//      } else {  // Ein OutputPort hat sich geändert
//          assert source instanceof OutputPort;
//          setValueReverse(((Port<T>)source).getValue());
//      }
//  }
//
//
//  @Override
//  public PortWrapper[] getConnectionPartners() {
//      checkInitialized();
//      List<PortWrapper> result = new ArrayList<PortWrapper>();
//      for (String uid : connections) {
//          try {
//              PortWrapper partner = getParent().getRoot().getJmcagui().getOutput(uid);
//              if (partner != null) {
//                  result.add(partner);
//              }
//          } catch (ClassCastException e) {
//              // not fully initialized yet ... no problem
//          } catch (Exception e) {
//              e.printStackTrace();
//          }
//      }
//      return result.toArray(new PortWrapper[0]);
//  }
//
//  @Override
//  public void clearConnections() {
//      checkInitialized();
//      connections.clear();
//      EventRouter.removeAsListener(this);
//      addChangeListener(this);
//  }
//
//
//  @Override
//  public void removeConnection(PortWrapper pw) {
//      checkInitialized();
//      connections.remove(pw.getUid());
//      pw.getPort().removeChangeListener(this);
//  }
//
//
//  public boolean isInputPort() {
//      return true;
//  }
//
//
//  @Override
//  public void connectTo(String uid) {
//      checkInitialized();
//      connections.clear();
//      if (uid != null) {
//          connections.add(uid);
//      }
//      interfaceUpdated();
//  }
}
