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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.finroc.jc.container.SimpleList;

import org.finroc.gui.commons.fastdraw.CompressedImage;
import org.finroc.gui.plugin.GUIPlugin;
import org.finroc.gui.util.PackageContentEnumerator;

import org.finroc.core.plugin.CreateExternalConnectionAction;
import org.finroc.core.plugin.Plugin;
import org.finroc.core.plugin.Plugins;


/**
 * @author max
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
                Class <? extends Widget > c = (Class <? extends Widget >)Class.forName(getClass().getPackage().getName() + "." + WIDGETPACKAGENAME + "." + s.substring(0, s.length() - 6));
                if (Widget.class.isAssignableFrom(c)) {
                    this.add(c);

                    // for nicer XML output
                    if (!appletMode) {
                        FinrocGuiXmlSerializer.getInstance().alias(c.getSimpleName(), c);
                    }
                }
            }
        }

        // JavaOnlyBlock
        //JavaPlugins.loadAllDataTypesInPackage(StringList.class);
        Plugins.loadAllDataTypesInPackage(CompressedImage.class);

        SimpleList<Plugin> plugins = Plugins.getInstance().getPlugins();
        for (Plugin plugin : plugins.getBackend()) {
            if (plugin instanceof GUIPlugin) {
                Class <? extends Widget > [] cs = ((GUIPlugin)plugin).getWidgets();
                if (cs != null) {
                    for (Class <? extends Widget > c : cs) {
                        this.add(c);
                    }
                }
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
    }

    public static List<CreateExternalConnectionAction> getIOInterfaces() {
        return Plugins.getInstance().getExternalConnections().getBackend();
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
