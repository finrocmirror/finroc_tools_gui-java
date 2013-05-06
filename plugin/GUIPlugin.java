//
// You received this file as part of Finroc
// A Framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui.plugin;

import org.finroc.tools.gui.GUICodec;
import org.finroc.tools.gui.Widget;

import org.finroc.core.plugin.Plugin;

/**
 * contains data about a single Plugin
 *
 * plugin files end with .plugin
 *
 * They contain the following:
 *
 * jars: jar1.jar, jar2.jar
 * widgets: org.mca.xx.yy.widgetxy1, org.mca.xx.yy.widgetxy2
 * interfaces: org.mca.zz.interface1, org.mca.zz.interface2
 * file filters: org.mca.rr.bb.filter1, org.mca.rr.bb.filter2
 */
public interface GUIPlugin extends Plugin {

    /**
     * @return Widgets that this plugin provides
     */
    public Class <? extends Widget > [] getWidgets();

    /**
     * @return GUI Import/Export filters that this plugin provides
     */
    public GUICodec[] getGUICodecs();

//  /** UID */
//  private static final long serialVersionUID = 8496048778979648243L;
//
//  private String[] jars;  // accompanying jars
//  private String[] widgets;  // class-names
//  private String[] interfaces; // class-names
//  private String[] fileFilters; // class-names
//  private transient ClassLoader classLoader; // ClassLoader for plugin
//
//  private Plugin(File pluginFile) throws Exception {
//      // Parse
//      AttributeReader ar = new AttributeReader(new FileReader(pluginFile));
//      jars = ar.readSeperatedAttribute("jars", ",");
//      widgets = ar.readSeperatedAttribute("widgets", ",");
//      interfaces = ar.readSeperatedAttribute("interfaces", ",");
//      fileFilters = ar.readSeperatedAttribute("file filters", ",");
//      ar.close();
//
//      // add path name to jars
//      for (int i = 0; i < jars.length; i++) {
//          jars[i] = pluginFile.getParentFile().getName() + File.separator + jars[i];
//      }
//  }
//
//  public Plugin(String widgets) {
//      jars = new String[0];
//      if (widgets.trim().length() <= 0) {
//          this.widgets = new String[0];
//      } else {
//          this.widgets = widgets.split(",");
//      }
//      interfaces = new String[0];
//      fileFilters = new String[0];
//      classLoader = getClass().getClassLoader();
//  }
//
//  public static List<Plugin> getPlugIns(File plugInDir) {
//      System.out.println("PlugInDir: " + plugInDir.getAbsolutePath());
//      List<Plugin> result = new ArrayList<Plugin>();
//      for (File d : plugInDir.listFiles()) {
//          if (d.isDirectory()) {
//              for (File f : d.listFiles()) {
//                  try {
//                      if (f.getName().endsWith(".plugin")) {
//                          result.add(new Plugin(f));
//                      }
//                  } catch (Exception e) {
//                      System.err.println("Error loading Plugin " + f.getName());
//                      e.printStackTrace();
//                  }
//              }
//          }
//      }
//      return result;
//  }
//
//  public String[] getInterfaces() {
//      return interfaces;
//  }
//
//  public String[] getJars() {
//      return jars;
//  }
//
//  public String[] getWidgets() {
//      return widgets;
//  }
//
//  public String[] getFileFilters() {
//      return fileFilters;
//  }
//
//  public ClassLoader getClassLoader() {
//      return classLoader;
//  }
//
//  public void setClassLoader(ClassLoader classLoader) {
//      this.classLoader = classLoader;
//  }
//
//  public String toString() {
//      String result = "";
//      for (String s : widgets) {
//          result += s + ", ";
//      }
//      for (String s : interfaces) {
//          result += s + ", ";
//      }
//      return result;
//  }
}
