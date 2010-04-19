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

import java.io.File;
import java.util.List;

import javax.swing.tree.TreeModel;

import org.finroc.gui.abstractbase.DataModelBase;
import org.finroc.gui.abstractbase.DataModelListener;
import org.finroc.gui.abstractbase.UIBase;
import org.finroc.gui.util.PackageContentEnumerator;
import org.finroc.gui.util.treemodel.InterfaceTreeModel;
import org.finroc.gui.util.treemodel.PortWrapper;

import org.finroc.core.plugin.ConnectionListener;
import org.finroc.core.plugin.ExternalConnection;

public abstract class GUIUiWithInterfaces<P extends UIBase<?,?,?,?>, C extends UIBase<?,?,?,?>> extends GUIUiBase<P,C> implements ConnectionListener {

    /** Interface of MCAGUI */
    protected transient InterfaceTreeModel ioInterface = new InterfaceTreeModel();

//  /** GUI's InputInterfaces */
//  protected transient List<RobotInterface> ioInterfaces;

    /** PlugIn directory */
    public static final File PLUGIN_DIR = getPluginDir();

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


    public void dataModelChanged(DataModelBase<?,?,?> caller, DataModelListener.Event event, Object param) {
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

    public String getConnectionAddress() {
        String result = "";
        for (ExternalConnection io : getActiveInterfaces()) {
            if (io.isConnected()) {
                result += io.getConnectionAddress() + "; ";
            }
        }
        if (result.equals("")) {
            return "";
        }
        return result.substring(0, result.length() - 2);
    }

    public void connectionEvent(ExternalConnection source, int e) {
        fireConnectionEvent(e);
    }

    public PortWrapper getInput(String uid) {
        getTreeModel();
        return ioInterface.getInputPort(uid);
    }

    public PortWrapper getOutput(String uid) {
        getTreeModel();
        return ioInterface.getOutputPort(uid);
    }

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
        String interfaceName = address.substring(0, address.indexOf(":"));
        String actualAddress = address.substring(interfaceName.length() + 1);
        for (ExternalConnection io : getActiveInterfaces()) {
            if (io.getClass().getSimpleName().equalsIgnoreCase(interfaceName)) {
                try {
                    io.connect(actualAddress);
                } catch (Exception e) {
                    throw new Exception("Couldn't connect to " + address + ".", e);
                }
                return;
            }
        }
        throw new Exception("Couldn't find robot interface " + interfaceName);
    }
}
