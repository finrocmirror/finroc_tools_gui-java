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
import java.util.Comparator;
import java.util.List;

import org.finroc.tools.gui.commons.fastdraw.CompressedImage;
import org.finroc.tools.gui.plugin.GUIPlugin;
import org.finroc.tools.gui.util.PackageContentEnumerator;

import org.finroc.core.plugin.CreateExternalConnectionAction;
import org.finroc.core.plugin.Plugin;
import org.finroc.core.plugin.Plugins;
import org.rrlib.finroc_core_utils.log.LogLevel;


/**
 * @author Max Reichardt
 *
 * Register for Widgets and IO-interfaces
 */
public class WidgetAndInterfaceRegister extends ArrayList < Class <? extends Widget >> implements Comparator < Class<? >> {

    /** UID */
    private static final long serialVersionUID = -8979927965402899643L;

    /** widget types (read in during initialization) */
    private static WidgetAndInterfaceRegister widgetTypes;

    /** relative package name of package that contains Widgets */
    private static final String WIDGETPACKAGENAME = "widgets";

//  /** GUI's InputInterfaces */
//  private static List<CreateExternalConnectionAction> ioInterfaces = new ArrayList<CreateExternalConnectionAction>();
//  private static final String IO_INTERFACE_PACKAGE_NAME = "interfaces";

    /** GUI's file codecs */
    private static List<GUICodec> guiCodecs = new ArrayList<GUICodec>();

    /** Variables to globally query in which mode Jmcagui is running */
    public static boolean appletMode = false;
    public static boolean serverMode = false;

    public static void init(GUIUiBase <? , ? > base) {
        widgetTypes = new WidgetAndInterfaceRegister(base);
    }

    public static WidgetAndInterfaceRegister getInstance() {
        if (widgetTypes == null) {
            throw new RuntimeException("Not initialized yet");
        }
        return widgetTypes;
    }

    private WidgetAndInterfaceRegister(GUIUiBase <? , ? > fingui) {
        try {
            initWidgetTypes(fingui);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Initialize widget types, if not done already
     * @param fingui
     */
    @SuppressWarnings("unchecked")
    private void initWidgetTypes(GUIUiBase <? , ? > fingui) throws Exception {

        // load core widgets
        for (String s : new PackageContentEnumerator(this, WIDGETPACKAGENAME)) {
            if (s.endsWith(".class")) {
                try {
                    Class <? extends Widget > c = (Class <? extends Widget >)Class.forName(getClass().getPackage().getName() + "." + WIDGETPACKAGENAME + "." + s.substring(0, s.length() - 6));
                    if (Widget.class.isAssignableFrom(c)) {
                        this.add(c);
                    }
                } catch (NoClassDefFoundError e) {
                    FinrocGUI.logDomain.log(LogLevel.WARNING, "WidgetAndInterfaceRegister", "Error loading widget " + s + ": " + e.getMessage() + " . Deleting 'build/java' might solve this issue.");
                }
            }
        }

        // JavaOnlyBlock
        //JavaPlugins.loadAllDataTypesInPackage(StringList.class);
        Plugins.loadAllDataTypesInPackage(CompressedImage.class);

        ArrayList<Plugin> plugins = Plugins.getInstance().getPlugins();
        for (Plugin plugin : plugins) {
            if (plugin instanceof GUIPlugin) {
                Class <? extends Widget > [] cs = ((GUIPlugin)plugin).getWidgets();
                if (cs != null) {
                    for (Class <? extends Widget > c : cs) {
                        this.add(c);
                    }
                }
            }
        }

        if (!appletMode) {
            for (Class<?> c : this) {
                FinrocGuiXmlSerializer.getInstance().alias(c.getSimpleName(), c);
                for (Class<?> inner : c.getClasses()) {
                    if ((!WidgetUI.class.isAssignableFrom(inner)) && Serializable.class.isAssignableFrom(inner)) {
                        FinrocGuiXmlSerializer.getInstance().alias(inner.getSimpleName(), inner);
                    }
                }
                FinrocGuiXmlSerializer.getInstance().processAnnotations(c);
            }
        }

//      Plugins.getInstance().getExternalConnections();
//      SimpleList<Plugin> pl

//      // load core interfaces
//      for (String s : new PackageContentEnumerator(this, IO_INTERFACE_PACKAGE_NAME)) {
//          if (s.endsWith(".class")) {
//              Class<? extends ExternalConnection> c = (Class<? extends ExternalConnection>)Class.forName(getClass().getPackage().getName() + "." + IO_INTERFACE_PACKAGE_NAME + "." + s.substring(0, s.length() - 6));
//              if (ExternalConnection.class.isAssignableFrom(c)) {
//                  try {
//                      ioInterfaces.add(c);
//                  } catch (Exception e) {
//                      e.printStackTrace();
//                  }
//              }
//          }
//      }

        // load plugin interfaces
//      ioInterfaces.addAll(Plugins.getInstance().getExternalConnections().getBackend());

//      // load plugins
//      for (Plugin p : jmcagui.getPlugIns()) {
//          // widgets...
//          for (String w : p.getWidgets()) {
//              try {
//                  Class c = p.getClassLoader().loadClass(w);
//                  if (Widget.class.isAssignableFrom(c)) {
//                      this.add(c);
//                  } else {
//                      throw new Exception("class is no widget");
//                  }
//              } catch (Exception e) {
//                  e.printStackTrace();
//              }
//          }
//
//          // and interfaces
//          for (String w : p.getInterfaces()) {
//              try {
//                  Class c = p.getClassLoader().loadClass(w);
//                  if (RobotInterface.class.isAssignableFrom(c)) {
//                      ioInterfaces.add(c);
//                  } else {
//                      throw new Exception("class is no RobotInterface");
//                  }
//              } catch (Exception e) {
//                  e.printStackTrace();
//              }
//          }
//
//          // and codecs
//          for (String w : p.getFileFilters()) {
//              try {
//                  Class c = p.getClassLoader().loadClass(w);
//                  if (GUICodec.class.isAssignableFrom(c)) {
//                      guiCodecs.add((GUICodec)c.newInstance());
//                  } else {
//                      throw new Exception("class is no GuiCodec");
//                  }
//              } catch (Exception e) {
//                  e.printStackTrace();
//              }
//          }
//      }

        // sort widgets alphabetically
        Collections.sort(this, this);

        ClassLoader cl = Plugins.getInstance().getPluginClassLoader();
        if (cl != null) {
            FinrocGuiXmlSerializer.getInstance().setClassLoader(cl);
        }
    }

    public static List<CreateExternalConnectionAction> getIOInterfaces() {
        return Plugins.getInstance().getExternalConnections();
    }

    public static List < Class <? extends Widget >> getWidgets() {
        return getInstance();
    }

    public static List<GUICodec> getGUICodecs() {
        return guiCodecs;
    }

    public int compare(Class<?> o1, Class<?> o2) {
        return o1.getSimpleName().compareTo(o2.getSimpleName());
    }
}
