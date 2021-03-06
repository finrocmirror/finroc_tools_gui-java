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

import java.io.File;
import java.util.List;

import javax.swing.tree.TreeModel;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;
import org.finroc.tools.gui.abstractbase.UIBase;
import org.finroc.tools.gui.util.PackageContentEnumerator;
import org.finroc.tools.gui.util.treemodel.InterfaceTreeModel;

import org.finroc.core.plugin.ConnectionListener;
import org.finroc.core.plugin.CreateExternalConnectionAction;
import org.finroc.core.plugin.ExternalConnection;
import org.finroc.core.plugin.Plugins;

public abstract class GUIUiWithInterfaces < P extends UIBase <? , ? , ? , ? >, C extends UIBase <? , ? , ? , ? >> extends GUIUiBase<P, C> implements ConnectionListener {

    /** Contains all interfaces widgets can be connected to */
    protected transient InterfaceTreeModel ioInterface = new InterfaceTreeModel();

//  /** GUI's InputInterfaces */
//  protected transient List<RobotInterface> ioInterfaces;

    public static File getPluginDir() {
        File f = new File(PackageContentEnumerator.getRootDir(GUIUiWithInterfaces.class) + "dist");
        if (!f.exists()) {
            String path = f.getParentFile().getParentFile() + File.separator + "dist";
            // replace "%20" with spaces
            while (path.contains("%20")) {
                path = path.replace("%20", " ");
            }
            f = new File(path);
        }
        return f;
    }

    public GUIUiWithInterfaces() {
        WidgetAndInterfaceRegister.init(this);
    }

//  public List<Plugin> getPlugIns() {
//      return getPlugInsStatic();
//  }
//
//  public static List<Plugin> getPlugInsStatic() {
//      List<Plugin> result = Plugin.getPlugIns(PLUGIN_DIR);
//
//      // set ClassLoaders for plugins
//      for (Plugin p : result) {
//          String[] jars = p.getJars();
//          URL[] urls = new URL[jars.length];
//          for (int i = 0; i < jars.length; i++) {
//              try {
//                  urls[i] = new URL(PLUGIN_DIR.toURI().toURL() + jars[i]);
//              } catch (Exception e) {
//                  e.printStackTrace();
//              }
//          }
//          p.setClassLoader(new URLClassLoader(urls));
//      }
//
//      return result;
//  }


    public void dataModelChanged(DataModelBase <? , ? , ? > caller, DataModelListener.Event event, Object param) {
        setLoopTime(getModel().getLoopTime());
    }

//  // below: connection related stuff
//  public void connect() throws Exception {
//      try {
//          for (RobotInterface io : getActiveInterfaces()) {
//              io.connect(null);
//          }
//      } catch (Exception e) {
//          disconnect();
//          throw e;
//      }
//      updateInterface();
//  }

    public void disconnectDiscard() throws Exception {
        for (ExternalConnection io : getActiveInterfaces()) {
            if (io.isConnected()) {
                try {
                    io.disconnect();
                } catch (Exception e) {}
            }
            io.managedDelete();
        }
        updateInterface();
    }

    public TreeModel getTreeModel() {
        if (ioInterface == null) {
            updateInterface();
        }
        return ioInterface;
    }

//  public void reconnect() throws Exception {
//      try {
//          for (RobotInterface io : getActiveInterfaces()) {
//              io.reconnect();
//          }
//      } catch (Exception e) {
//          disconnect();
//          throw e;
//      }
//      updateInterface();
//  }

    public void updateInterface() {
        /*ioInterface.removeAllChildren();
        for (RobotInterface io : getActiveInterfaces()) {
            TreeNode model = io.getTreeModel();
            if (model != null) {
                ioInterface.add(model);
            }
        }*/
        fireConnectionEvent(ConnectionListener.INTERFACE_UPDATED);
    }

    public List<ExternalConnection> getActiveInterfaces() {
        return ioInterface.getActiveInterfaces();
    }

//    public String getConnectionAddress() {
//        String result = "";
//        for (ExternalConnection io : getActiveInterfaces()) {
//            if (io.isConnected()) {
//                result += io.getConnectionAddress() + "; ";
//            }
//        }
//        if (result.equals("")) {
//            return "";
//        }
//        return result.substring(0, result.length() - 2);
//    }

    public void connectionEvent(ExternalConnection source, int e) {
        fireConnectionEvent(e);
    }

//    public PortWrapper getInput(String uid) {
//        getTreeModel();
//        return ioInterface.getInputPort(uid);
//    }
//
//    public PortWrapper getOutput(String uid) {
//        getTreeModel();
//        return ioInterface.getOutputPort(uid);
//    }

    public void setLoopTime(long ms) {
        for (ExternalConnection io : getActiveInterfaces()) {
            io.setLoopTime(ms);
        }
    }


    /**
     * Connect interface (Syntax: <interface class name>:<address>)
     *
     * @param address <interface class name>:<address>
     */
    public void connect(String address) throws Exception {
        connect(address.substring(0, address.indexOf(":")), address.substring(address.indexOf(":") + 1));
    }

    /**
     * Connect interface
     *
     * @param connectionType Connection type (CreateExternalConnectionAction.getName())
     * @param address Connection address (whatever connection type requires
     */
    public void connect(String connectionType, String address) throws Exception {
        for (CreateExternalConnectionAction io : Plugins.getInstance().getExternalConnections()) {
            if (io.getName().equalsIgnoreCase(connectionType)) {
                try {
                    connectImpl(io, address);
                } catch (Exception e) {
                    throw new Exception("Couldn't connect to " + address + ".", e);
                }
                return;
            }
        }

        /*
        // try default interface
        for (CreateExternalConnectionAction io : Plugins.getInstance().getExternalConnections()) {
            if (io.getName().equalsIgnoreCase(TCP.TCP_PORTS_ONLY_NAME)) {
                try {
                    connectImpl(io, address);
                } catch (Exception e) {
                    throw new Exception("Couldn't connect to " + address + ".", e);
                }
                return;
            }
        }*/

        throw new Exception("Couldn't find robot interface to connect to '" + address + "'");
    }

    /**
     * Connect to robot control
     *
     * @param action CreateExternalConnectionAction to instantiate connection with
     * @param address Address to connect to
     */
    public void connectImpl(CreateExternalConnectionAction action, String address) throws Exception {
        ExternalConnection ec = action.createExternalConnection();
        ioInterface.getRootFrameworkElement().addChild(ec);
        ec.init();
        ec.addConnectionListener(this);
        ec.connect(address, ioInterface.getNewModelHandlerInstance());
        if (ec.isConnected()) {
            if (this instanceof FinrocGUI) {
                ((FinrocGUI)this).getPersistentSettings().lastConnectionAddress = ec.getConnectionAddress();
            }
            getModel().addConnectionAddress(action.getName() + ":" + ec.getConnectionAddress());
        }
    }
}
