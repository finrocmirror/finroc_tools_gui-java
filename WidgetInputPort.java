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
 * This class is a input port for a widget
 */
public abstract class WidgetInputPort < P extends PortWrapperBase<? >> extends WidgetPort<P> { /*implements InputPort<T>, PortListener*/

    /** UID & protected empty constructor */
    private static final long serialVersionUID = 8596177899211743789L;

    static PortCreationInfo stdPci = new PortCreationInfo("", PortFlags.INPUT_PORT);

    public PortCreationInfo getPci() {
        PortCreationInfo def = stdPci.derive(getDescription());
        PortCreationInfo pci = getParent().getPortCreationInfo(def, this);
        return pci == null ? def : pci;
    }


//  public WidgetInputPort(Class<T> type, T defaultValue, String description) {
//      super(type, defaultValue, description);
//  }

//  @Override
//  public void interfaceUpdated() {
//      register();
//  }
//
//  public void dispose() {
//      EventRouter.objectDisposed(this);
//  }
//
//  private void register() {
//      PortWrapper ip = getInputPort();
//      EventRouter.removeAsListener(this);
//      if (ip != null) {
//          //EventRouter.addListener(ip, "addChangeListener", this);
//          ip.getPort().addChangeListener(this);
//      }
//      updateValue();
//  }
//
//  @SuppressWarnings("unchecked")
//  private void updateValue() {
//      PortWrapper ip = getInputPort();
//      if (ip != null) {
//          setValue((T)ip.getPort().getValue(), true);
//      }
//  }
//
//
//  @SuppressWarnings("unchecked")
//  public PortWrapper getInputPort() {
//      try {
//          return getParent().getRoot().getJmcagui().getInput(connectedTo);
//      } catch (Exception e) {
//          return null;
//      }
//  }
//
//  @Override
//  public void restore(Widget parent) {
//      super.restore(parent);
//      interfaceUpdated();
//  }
//
//
//  @Override
//  public void connectTo(PortWrapper pw) {
//      connectTo(pw.getUid());
//  }
//
//  public void valueChanged(Port source) {
//      updateValue();
//  }
//
//
//  @Override
//  public PortWrapper[] getConnectionPartners() {
//      PortWrapper p = getInputPort();
//      if (p == null) {
//          return new PortWrapper[0];
//      }
//      return new PortWrapper[]{p};
//  }
//
//  @Override
//  public void clearConnections() {
//      EventRouter.removeAsListener(this);
//      connectedTo = "";
//  }
//
//
//  @Override
//  public void removeConnection(PortWrapper pw) {
//      if (connectedTo.equals(pw.getUid())) {
//          pw.getPort().removeChangeListener(this);
//          connectedTo = "";
//      }
//  }
//
//  public boolean isInputPort() {
//      return false;
//  }
//
//  @Override
//  public void connectTo(String uid) {
//      if (uid != null) {
//          connectedTo = uid;
//          register();
//      }
//  }
}
