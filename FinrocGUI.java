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

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;
import org.finroc.tools.gui.util.gui.FileDialog;
import org.finroc.tools.gui.util.propertyeditor.NotInPropertyEditor;
import org.finroc.plugins.data_types.StringList;
import org.finroc.plugins.tcp.Peer;

import org.finroc.core.RuntimeEnvironment;
import org.finroc.core.RuntimeSettings;
import org.finroc.core.plugin.CreateExternalConnectionAction;
import org.finroc.core.plugin.Plugins;
import org.finroc.core.util.Files;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;

/**
 * @author Max Reichardt
 *
 * This is the main class and contains the top-level control functions.
 */
public class FinrocGUI extends GUIUiWithInterfaces<FinrocGUI, GUIWindowUI> implements KeyEventDispatcher { /*implements RobotInterface*/

    /** Constants that may be changed */
    public static final boolean USE_SYSTEM_CLIPBOARD = true;
    public static final boolean SHOWDEBUGMENU = false;

    /** File under which the GUI was saved, null if it hasn't been saved yet */
    private transient File guifile = null;

    /** GUI-Data as XML from last save (used to determine whether something changed) */
    private transient String lastGUIData;

    /** Persistent GUI Settings */
    private Settings persistentSettings;

    /** Is Control-Key currently pressed? */
    private transient boolean ctrlPressed = false;

    /** Dummy variable in order to create dependency to TCP plugin - at least as long as there is no other network transport available */
    static final Peer TCP_DEPENDENCY = null;

    /** Contains information about connect tasks specified via command line */
    static class ConnectTask {

        /** Type of connection */
        String connectionType;

        /** Address of connection */
        String address;

        public ConnectTask(String connectionType, String address) {
            this.connectionType = connectionType;
            this.address = address;
        }
    }

    public FinrocGUI() throws Exception {

        // install keyboard hook
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);

        // restore last settings
        persistentSettings = Settings.restore();

        // set file dialog start directory to convenient choice
        if (System.getenv("FINROC_PROJECT_HOME") != null) {
            FileDialog.setCurrentDirectory(new File(System.getenv("FINROC_PROJECT_HOME")));
        } else if (System.getenv("FINROC_HOME") != null) {
            FileDialog.setCurrentDirectory(new File(System.getenv("FINROC_HOME")));
        } else {
            FileDialog.setCurrentDirectory(new File("."));
        }

        RuntimeEnvironment.getInstance().setProgramName("fingui");

        setModel(new GUI(this));
        arrangeWindows();
        updateLastGUIData();
    }


    public void dataModelChanged(DataModelBase <? , ? , ? > caller, DataModelListener.Event event, Object param) {
        super.dataModelChanged(caller, event, param);
        for (GUIWindowUI wui : children) {
            wui.setTreeFont(getModel().getTreeFont());
        }
    }

    public void arrangeWindows() {

        // Determine which GUIWindow-GUIWindowUI sets are incomplete
        List<GUIWindowUI> withoutWindow = new ArrayList<GUIWindowUI>();
        List<GUIWindow> withoutUI = new ArrayList<GUIWindow>();
        for (GUIWindow w : getModel().getChildren()) {
            boolean hasWindow = false;
            for (GUIWindowUI wui : children) {
                if (wui.getModel() == w) {
                    hasWindow = true;
                }
            }
            if (!hasWindow) {
                withoutUI.add(w);
            }
        }
        for (GUIWindowUI wui : children) {
            if (!getModel().getChildren().contains(wui.getModel())) {
                withoutWindow.add(wui);
            }
        }

        // assign Windows to spare WindowsUI
        while (withoutWindow.size() > 0 && withoutUI.size() > 0) {
            withoutWindow.get(0).setModel(withoutUI.get(0));
            withoutWindow.remove(0);
            withoutUI.remove(0);
        }

        for (GUIWindowUI wui : withoutWindow) {
            wui.dispose();
            children.remove(wui);
        }

        for (GUIWindow w : withoutUI) {
            children.add(new GUIWindowUI(this, w));
        }
    }


//  /**
//   * Initialize widget types, if not done already
//   */
//
//  private void initIOInterfaces() throws Exception {
//      if (ioInterfaces == null) {
//          ioInterfaces = new ArrayList<RobotInterface>();
//          for (Class<? extends RobotInterface> c : WidgetAndInterfaceRegister.getIOInterfaces()) {
//              RobotInterface ioi = (RobotInterface)c.newInstance();
//              ioInterfaces.add(ioi);
//              //EventRouter.addListener(ioi, "addConnectionListener", this);
//              ioi.addConnectionListener(this);
//          }
//      }
//  }

    /**
     * Update lastGUIData variable with data of current GUI
     */
    private void updateLastGUIData() {
        lastGUIData = FinrocGuiXmlSerializer.getInstance().toXML(getModel());
    }

    /**
     * Create new GUI (delete all open windows)
     * @param caller
     */
    public void clearGUI(GUIWindowUI caller, boolean askForSave) {
        if (!askForSave || askForSave()) {

            guifile = null;

            // clear panels
            getModel().dispose();
            setModel(new GUI(this));
            if (caller != null) {
                caller.setModel(getModel().getChildren().get(0));
            }
            for (GUIWindowUI gwu : children) {
                gwu.resetUndoBuffer();
            }
            arrangeWindows();

            updateLastGUIData();
        }
    }

    /**
     * Add new empty window to GUI
     */
    public void addNewWindow() {
        try {
            getModel().add(new GUIWindow(getModel()));
            arrangeWindows();
        } catch (Exception e) {
            showErrorMessage(e);
        }
    }

    public void showErrorMessage(Exception e) {
        Log.log(LogLevel.ERROR, this, e);
        JOptionPane.showMessageDialog(null, e.getClass().getName() + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showErrorMessage(String string) {
        RuntimeException re = new RuntimeException(string);
        showErrorMessage(re);
        throw re;
    }


    /**
     * Ask user whether changes should be saved
     *
     * @return Returns whether action should continue (= user didn't press abort)
     */
    private boolean askForSave() {
        if (hasChanged()) {
            int answer = JOptionPane.showConfirmDialog(null, "Would you like to save your changes?", "GUI was modified", JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                return saveGUI();
            }
            return (answer != JOptionPane.CANCEL_OPTION);
        }
        return true;
    }

    /**
     * @return Has GUI changed since last save
     */
    public boolean hasChanged() {
        //long start = System.currentTimeMillis();
        String newGUIData = FinrocGuiXmlSerializer.getInstance().toXML(getModel());
        //System.out.println(System.currentTimeMillis() - start);
        return (!newGUIData.equals(lastGUIData));
    }

    /**
     * Save or export GUI using specified filename.
     *
     * @param f File to write GUI to. If null then current filename is used.
     * @return Has GUI been saved
     */
    public boolean saveGUI() {

        // Ask for filename?
        if (guifile == null) {
            return saveGUIAs();
        }

        // save GUI
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                exportGUI(guifile);
            } catch (UnsupportedEncodingException e) {
                super.saveGUI(getModel(), baos, false);
            }

            guifile.delete();
            FileOutputStream fos = new FileOutputStream(guifile);
            baos.writeTo(fos);
            fos.close();
            persistentSettings.addToRecentFiles(guifile);
        } catch (Exception e) {
            showErrorMessage(e);
        }

        updateLastGUIData();
        return true;
    }

    /**
     * Save GUI asking for filename
     * @return saved Has GUI been saved?
     */
    public boolean saveGUIAs() {
        File f = FileDialog.showSaveDialog("Save GUI As...", GUI_FILE_EXTENSION);
        if (f != null) {
            if (f.exists()) {
                int answer = JOptionPane.showConfirmDialog(null, "File exists. Would you like to overwrite it?", "Warning", JOptionPane.YES_NO_CANCEL_OPTION);
                if (answer == JOptionPane.NO_OPTION) {
                    saveGUIAs();
                    return false;
                } else if (answer == JOptionPane.CANCEL_OPTION) {
                    return false;
                }
            }
            guifile = f;
            saveGUI();
            for (GUIWindowUI gwu : children) {
                gwu.updateTitle();
            }
            return true;
        }
        return false;
    }

    /**
     * Load GUI from HDD or from an URL
     */
    public void loadGUI(GUIWindowUI caller, File f, URL url) throws Exception {
        if (!askForSave()) {
            return;
        }
        if (f == null && url == null) {
            f = FileDialog.showOpenDialog("Open GUI...", getSupportedExtensions());
        }
        if (f != null || url != null) {

            GUI newGui = null;
            if (url != null) {
                newGui = super.loadGUI(url.openStream());
                guifile = null;
            } else if (f.getAbsolutePath().toLowerCase().endsWith(GUI_FILE_EXTENSION.toLowerCase())) {
                newGui = super.loadGUI(new FileInputStream(f));
                guifile = f;
            } else {
                // import GUI
                newGui = importGUI(f);
            }
            persistentSettings.addToRecentFiles(f);

            if (newGui == null) {
                return;
            }

            getModel().dispose();
            setModel(newGui);

            // put first window in calling window
            caller.setModel(newGui.getChildren().get(0));

            arrangeWindows();

            // adjust window sizes to content
            for (GUIWindowUI wui : children) {
                wui.adjustSizeToContent();
            }

            // update last GUIData
            updateLastGUIData();
        }
    }

    public void exit() {
        if (askForSave()) {
            persistentSettings.save();
            System.exit(0);
        }
    }

    public String toString() {
        return (guifile != null) ? guifile.getName() : "";
    }

    public static void main(String[] args) {

        RuntimeSettings.setUseCCPorts(false);
        RuntimeSettings.setMaxCoreRegisterIndexBits(19);
        Plugins.getInstance().addPrefixForPluginsToLoad("finroc_tools_gui_plugins_");

//      // Server mode ?
//      if (args.length > 0 && args[0].toLowerCase().contains("-serve")) {
//          try {
//              int port = Integer.parseInt(args[1]);
//              String guifile = null;
//              if (args.length > 1) {
//                  guifile = args[2];
//              }
//              new Server(port, guifile, args.length > 3);
//          } catch (Exception e) {
//              e.printStackTrace();
//              System.out.println("Syntax: fingui -server <port> <guifile>");
//          }
//          return;
//        }

        // normal mode

        // parse command line arguments
        final List<String> loadTasks = new ArrayList<String>();
        final List<ConnectTask> connectTasks = new ArrayList<ConnectTask>();
        boolean shiny = true;
        for (String arg : args) {
            if (arg.startsWith("--connect=")) {
                connectTasks.add(new ConnectTask(null, arg.substring("--connect=".length())));
            } else if (arg.startsWith("--connect-with=")) {
                String trimmedArg = arg.substring("--connect-with=".length());
                if (!trimmedArg.contains(":")) {
                    connectTasks.add(new ConnectTask(trimmedArg, ""));
                } else {
                    connectTasks.add(new ConnectTask(trimmedArg.substring(0, trimmedArg.indexOf(":")), trimmedArg.substring(trimmedArg.indexOf(":") + 1)));
                }
            } else if (arg.equals("-h") || arg.equals("--help")) {
                printHelp();
                return;
            } else if (arg.equals("--classic")) {
                shiny = false;
            } else if (arg.equals("--list-connection-types")) {
                RuntimeEnvironment.getInstance();
                System.out.println("\nAvailable connection types:\n");
                for (CreateExternalConnectionAction ceca : Plugins.getInstance().getExternalConnections()) {
                    if ((ceca.getFlags() & CreateExternalConnectionAction.REMOTE_EDGE_INFO) == 0) {
                        System.out.println("  " + ceca.getName());
                    }
                }
                System.out.println();
                return;
            } else if (arg.equals("-open")) {
                // ignore (this is added by Windows when double-clicking on .fingui files; simply ignoring it should work)
            } else if (arg.startsWith("-")) {
                System.out.println("Unsupported option: " + arg);
                printHelp();
                return;
            } else { // gui file to load
                if (loadTasks.size() >= 1) {
                    System.out.println("Syntax: fingui <options> guifile");
                    printHelp();
                    return;
                }
                loadTasks.add(arg);
            }
        }

        if (shiny) {
            try {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {}
        }


        // start Fingui in separate Thread (recommended in Java Tutorials)
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    FinrocGUI fingui = new FinrocGUI();

                    // load GUI file at startup?
                    if (!loadTasks.isEmpty()) {
                        File file = new File(loadTasks.get(0));
                        if (file.exists()) {
                            fingui.loadGUI(fingui.children.get(0), file, null);
                        } else if (loadTasks.get(0).contains("://")) {
                            fingui.loadGUI(fingui.children.get(0), file, new URL(loadTasks.get(0)));
                        } else {
                            Log.log(LogLevel.ERROR, "Cannot open '" + loadTasks.get(0) + "'");
                        }
                    }

                    // connect at startup?
                    boolean connected = false;
                    for (ConnectTask ct : connectTasks) {
                        String connectionType = ct.connectionType;

                        // Determine default connection type
                        if (connectionType == null && (!loadTasks.isEmpty())) {
                            connectionType = fingui.getModel().getDefaultConnectionType();
                            if (connectionType == null && fingui.getModel().getConnectionList().size() > 0) {

                                // if all past connections used the same connection type, use this
                                boolean allSame = true;
                                String first = fingui.getModel().getConnectionList().get(0);
                                String type = first.substring(0, first.indexOf(":"));
                                for (int i = 1; i < fingui.getModel().getConnectionList().size(); i++) {
                                    String entry = fingui.getModel().getConnectionList().get(i);
                                    allSame &= entry.substring(0, entry.indexOf(":")).equalsIgnoreCase(type);
                                }
                                if (allSame) {
                                    connectionType = type;
                                    System.out.println("Provided GUI file does not provide default connection type, but all past connections used: " + connectionType);
                                }
                            }
                        }

                        if (connectionType == null) {
                            // Show dialog so that user can select
                            connectionType = new ConnectionTypeDialog((JFrame)fingui.children.get(0).asComponent(), fingui).showDialog();
                        }

                        if (connectionType != null) {
                            fingui.connect(connectionType, ct.address);
                            connected = true;
                        } else {
                            System.out.println("Could determine default connection type. Not connecting to " + ct.address);
                        }
                    }

                    // Show dialog for choosing connection?
                    if (connectTasks.size() == 0 && loadTasks.size() > 0 && fingui.getModel().getConnectionList().size() > 0 && (fingui.children.get(0).asComponent() instanceof JFrame)) {
                        String connect = new ConnectDialog((JFrame)fingui.children.get(0).asComponent(), true, false, "Would you like to connect now?").show(fingui.getModel().getConnectionList(), null);
                        if (connect != null) {
                            fingui.connect(connect);
                            connected = true;
                        }
                    }

                    if (connected) {
                        fingui.updateInterface();
                    }
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().length() > 0) {
                        JOptionPane.showMessageDialog(null, e.getMessage());
                    } else {
                        JOptionPane.showMessageDialog(null, e.getClass().getSimpleName() + " at " + e.getStackTrace()[0].toString());
                    }
                    Log.log(LogLevel.ERROR, this, e);
                    System.exit(-1);
                }
            }
        });
    }

    /**
     * Prints help
     */
    private static void printHelp() {
        try {
            for (String s : Files.readLines(FinrocGUI.class.getResourceAsStream("help.txt"))) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void closeWindow(GUIWindow window) {
        if (children.size() <= 1) {
            exit();
            return;
        }
        getModel().remove(window);
        arrangeWindows();
    }

    /**
     * @author Max Reichardt
     *
     * Persistent settings in Jmcagui
     */
    public static class Settings implements Serializable {

        /** UID */
        private static final long serialVersionUID = -3731170717599189682L;

        private static final String SETTINGSFILE = System.getProperty("user.home") + File.separator + ".fingui";
        private static final int MAXRECENTFILES = 8;

        @NotInPropertyEditor
        private List<File> recentFiles;

        private StringList resourceFolders;

        public String lastConnectionAddress;

        public boolean detachConnectionPanel;

        private Settings() {
            recentFiles = new ArrayList<File>();
            resourceFolders = new StringList("$MCAHOME\n.");
        }

        public static Settings restore() {
            try {
                return (Settings)FinrocGuiXmlSerializer.getInstance().fromXML(new BufferedReader(new FileReader(SETTINGSFILE)));
            } catch (Exception e) {
                // file doesn't exist - return default settings
                return new Settings();
            }
        }

        public void save() {
            try {
                FinrocGuiXmlSerializer.getInstance().toXML(this, new BufferedWriter(new FileWriter(SETTINGSFILE)));
            } catch (Exception e) {
                Log.log(LogLevel.ERROR, this, e);
            }
        }

        public void addToRecentFiles(File file) {
            file = file.getAbsoluteFile();
            if (recentFiles.contains(file)) {
                recentFiles.remove(file);
            }
            recentFiles.add(0, file);
            while (recentFiles.size() > MAXRECENTFILES) {
                recentFiles.remove(recentFiles.size() - 1);
            }
        }

        public ArrayList<String> getResourceFolders() {
            if (resourceFolders == null) {
                resourceFolders = new StringList("$MCAHOME\n.");
            }
            return resourceFolders;
        }
    }

    public List<File> getRecentFiles() {
        return Collections.unmodifiableList(persistentSettings.recentFiles);
    }

    /**
     * @return Resource folders
     */
    public List<File> getResourcePaths() {
        List<File> result = new ArrayList<File>();
        for (String s : persistentSettings.getResourceFolders()) {
            s = Files.resolveEnvironmentVariables(s);
            if (s != null && s.length() > 0) {
                File f = new File(s);
                if (f.exists() && f.isDirectory()) {
                    result.add(new File(s));
                }
            }
        }
        return result;
    }


    public Settings getPersistentSettings() {
        return persistentSettings;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL && e.getID() == KeyEvent.KEY_PRESSED) {
            ctrlPressed = true;
        } else if (e.getKeyCode() == KeyEvent.VK_CONTROL && e.getID() == KeyEvent.KEY_RELEASED) {
            ctrlPressed = false;
        }

        return false;
    }

    public boolean isCtrlPressed() {
        return ctrlPressed;
    }
}
