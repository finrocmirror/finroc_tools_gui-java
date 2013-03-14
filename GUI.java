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

}
