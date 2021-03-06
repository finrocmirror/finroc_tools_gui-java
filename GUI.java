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

import java.awt.Font;
import java.io.File;
import java.util.List;

import javax.swing.JLabel;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.util.embeddedfiles.FileManager;
import org.finroc.tools.gui.util.propertyeditor.NotInPropertyEditor;
import org.finroc.tools.gui.util.propertyeditor.gui.ResourcePathProvider;
import org.finroc.plugins.data_types.StringList;

import org.finroc.core.FrameworkElement;
import org.finroc.core.RuntimeEnvironment;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;
import org.rrlib.xml.XMLNode;

/**
 * @author Max Reichardt
 *
 * This class contains the GUI-(data-)model
 */
public class GUI extends DataModelBase<GUI, GUI, GUIWindow> implements ResourcePathProvider {

    /** UID */
    private static final long serialVersionUID = -6655795676216560061L;

    /** Loop Time for IO-Connections */
    private int loopTime = 100;

    /** Font for connection tree (in left panel) */
    private float fontSize = 12;
    //private Font treeFont = new JLabel().getFont().deriveFont(Font.PLAIN);

    /** reference to main class */
    private transient GUIUiBase <? , ? > fingui;

    /** Manager for embedded files */
    private transient FileManager embeddedFileManager;

    /** Edit Mode saved in GUI file */
    @NotInPropertyEditor
    private int editMode = GUIWindowUIBase.EditMode.editObject.ordinal();

    /** Default connection type to use (e.g. if only address is specified on command line) */
    private String defaultConnectionType;

    /** List of connections that should be displayed as options for connecting GUI - usually the list is filled with past connections */
    private StringList connectionList;

    public GUI(GUIUiBase <? , ? > fingui) {
        super(null);
        this.fingui = fingui;
        // create GUI-GUI ;-)
        // create one new panel
        //addNewWindow();
        add(new GUIWindow(this));
        restore(null);
    }

    public GUIUiBase <? , ? > getFingui() {
        return fingui;
    }

    public void setJmcagui(GUIUiBase <? , ? > fingui) {
        this.fingui = fingui;
    }

    public int getLoopTime() {
        return loopTime;
    }

    public Font getTreeFont() {
        return new JLabel().getFont().deriveFont(Font.PLAIN, fontSize);
    }

    public void setLoopTime(int loopTime) {
        this.loopTime = loopTime;
    }

    public FileManager getEmbeddedFileManager() {
        if (embeddedFileManager == null) {
            embeddedFileManager = new FileManager(this);
        }
        //embeddedFileManager.update();
        return embeddedFileManager;
    }

    public void setEmbeddedFileManager(FileManager newEfm) {
        embeddedFileManager = newEfm;
    }

    @Override
    protected FrameworkElement createFrameworkElement() {
        return new FrameworkElement(RuntimeEnvironment.getInstance(), "GUI");
    }

    @Override
    public List<File> getResourcePaths() {
        return getFingui().getResourcePaths();
    }

    @Override
    public void restore(GUI parent) {
        super.restore(parent);
        if (connectionList == null) {
            connectionList = new StringList();
        }
        frameworkElement.init();
    }

    public int getEditMode() {
        return editMode;
    }

    public void setEditMode(int editMode) {
        this.editMode = editMode;
    }

    /**
     * @return List of connections that should be displayed as options for connecting GUI
     */
    public StringList getConnectionList() {
        return connectionList;
    }

    /**
     * Add new connection address to list of connections that should be displayed as options for connecting GUI
     *
     * @param connectionAddress new connection address
     */
    public void addConnectionAddress(String connectionAddress) {
        if (!connectionList.contains(connectionAddress)) {
            connectionList.add(connectionAddress);
        }
    }

    /**
     * @return Default connection type to use (e.g. if only address is specified on command line)
     */
    public String getDefaultConnectionType() {
        return defaultConnectionType;
    }

    /**
     * @param defaultConnectionType Default connection type to use (e.g. if only address is specified on command line)
     */
    public void setDefaultConnectionType(String defaultConnectionType) {
        this.defaultConnectionType = defaultConnectionType;
    }

    @Override
    public void serialize(XMLNode node) throws Exception {
        serializeChildren(node, "Window");
        node.addChildNode("loopTime").setContent("" + loopTime);
        node.addChildNode("fontSize").setContent("" + fontSize);
        node.addChildNode("editMode").setContent("" + editMode);
        connectionList.serialize(node.addChildNode("connectionList"));
    }

    @Override
    public void deserialize(XMLNode node) throws Exception {
        super.children.clear();
        for (XMLNode child : node.children()) {
            try {
                if (child.getName().equals("Window")) {
                    GUIWindow window = new GUIWindow(this);
                    window.deserialize(child);
                    add(window);
                } else if (child.getName().equals("loopTime")) {
                    loopTime = Integer.parseInt(child.getTextContent());
                } else if (child.getName().equals("fontSize")) {
                    fontSize = Float.parseFloat(child.getTextContent());
                } else if (child.getName().equals("editMode")) {
                    editMode = Integer.parseInt(child.getTextContent());
                } else if (child.getName().equals("connectionList")) {
                    connectionList.deserialize(child);
                }
            } catch (Exception e) {
                Log.log(LogLevel.ERROR, e);
            }
        }
    }
}
