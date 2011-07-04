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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.finroc.tools.gui.abstractbase.DataModelBase;
import org.finroc.tools.gui.abstractbase.DataModelListener;
import org.finroc.tools.gui.util.embeddedfiles.AbstractFile;
import org.finroc.tools.gui.util.embeddedfiles.EmbeddedFile;
import org.finroc.tools.gui.util.embeddedfiles.FileManager;
import org.finroc.tools.gui.util.gui.IconManager;
import org.finroc.tools.gui.util.gui.MToolBar;
import org.finroc.tools.gui.util.propertyeditor.gui.PropertiesDialog;
import org.rrlib.finroc_core_utils.log.LogLevel;

import org.finroc.core.RuntimeEnvironment;
import org.finroc.core.plugin.ConnectionListener;
import org.finroc.core.plugin.CreateExternalConnectionAction;
import org.finroc.core.plugin.ExternalConnection;
import org.finroc.core.plugin.Plugins;
import org.finroc.core.port.ThreadLocalCache;


/**
 * @author max
 *
 */
public class GUIWindowUI extends GUIWindowUIBase<FinrocGUI> implements ActionListener, KeyListener, WindowListener, MenuListener, ChangeListener, MouseListener, ConnectionPanel.Owner {

    /** UID */
    private static final long serialVersionUID = 754612157890759400L;

    /** Minimum Size for Widgets */
    private static final int WIDGET_MIN_SIZE = 10;

    /** Standard size for widget (if invalid size occurs during creation) */
    private static final Dimension WIDGET_STANDARD_SIZE = new Dimension(100, 100);

    /** default window title */
    private static final String TITLE = "FinGUI";

    /** Reference to ConnectionPanel */
    protected transient ConnectionPanel connectionPanel;

    /** reference to undo buffer */
    private transient UndoBuffer<GUIWindow> undoBuffer;

    /** MenuItems */
    private transient JMenuItem /*miConnect, miReconnect,*/ miDisconnectDiscard, miNew, miLoad, miSave, miExit, miSaveAs, miNewTab, miNewWindow, miCloseTab, miCloseWindow, miTest;
    transient JMenuItem miUndo, miRedo, miCut, miCopy, miPaste, miDelete, miChangePanelName;
    private transient JCheckBoxMenuItem miConnectionPanel, miSnapToGrid;
    private transient JMenuItem miDebugConsole, miGuiSettings, miGlobalSettings, miFrameworkElementDump;
    private transient JMenu miConnectMenu, /*miDisconnectMenu, miReconnectMenu,*/ miRecentFiles;
    private transient JRadioButtonMenuItem miEditOriginal, miEditCtrl, miUseOnly;

    /** Tab-Popupmenu */
    private transient int clickedOnTab = 0; // tab that was clicked on
    private transient JMenuItem pmiChangePanelName; // change tab name
    private transient List<JMenuItem> pmiMoveTo = new ArrayList<JMenuItem>(); // move to commands

    /** Toolbar */
    private transient MToolBar toolBar;

    /** Toolbar-Buttons */
    private transient JButton tbNew, tbLoad, tbSave, tbCut, tbCopy, tbPaste, tbUndo, tbRedo;

    /** Temporary object that stores, which type of Widget the user wants to create */
    private transient Class <? extends Widget > objectToCreate;

    /** Is Control-Key currently pressed? */
    private transient boolean ctrlPressed = false;

    /** the appication's clipboard */
    private static Clipboard clipboard;

    /** timer to update status bar */
    private Timer statusBarTimer;

    public GUIWindowUI(FinrocGUI parent, GUIWindow model) {
        super(parent, new JFrame(), model);
        ThreadLocalCache.get();
        this.setMinimumSize(new Dimension(640, 480));
        JFrame jframe = (JFrame)ui;

        // menu
        JMenuBar menuBar = new JMenuBar();

        // (dis-)connect-menu
        miConnectMenu = new JMenu("Connect");
        //miDisconnectMenu = new JMenu("Disconnect");
        //miReconnectMenu = new JMenu("Reconnect");
        for (CreateExternalConnectionAction ioi : Plugins.getInstance().getExternalConnections().getBackend()) {
            if ((ioi.getFlags() & CreateExternalConnectionAction.REMOTE_EDGE_INFO) == 0) {
                miConnectMenu.add(new ConnectAction(ioi, false, false));
            }
            //miDisconnectMenu.add(new ConnectAction(ioi, true, false));
            //miReconnectMenu.add(new ConnectAction(ioi, false, true));
        }

        // file menu
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic(KeyEvent.VK_F);
        menuFile.addMenuListener(this);
        menuBar.add(menuFile);
        //miConnect = createMenuEntry("Connect All", menuFile, KeyEvent.VK_C);
        //miReconnect = createMenuEntry("Reconnect All", menuFile, KeyEvent.VK_R);
        miDisconnectDiscard = createMenuEntry("Disconnect & Discard All", menuFile, KeyEvent.VK_D);
        menuFile.add(miConnectMenu);
        //menuFile.add(miReconnectMenu);
        //menuFile.add(miDisconnectMenu);
        menuFile.addSeparator();
        miNew = createMenuEntry("New GUI", menuFile, KeyEvent.VK_N);
        miLoad = createMenuEntry("Open...", menuFile, KeyEvent.VK_O);
        miSave = createMenuEntry("Save", menuFile, KeyEvent.VK_S);
        miSaveAs = createMenuEntry("Save As...", menuFile, KeyEvent.VK_A);
        miRecentFiles = new JMenu("Recent Files");
        miRecentFiles.setMnemonic(KeyEvent.VK_E);
        menuFile.add(miRecentFiles);
        menuFile.addSeparator();
        miNewTab = createMenuEntry("New Tab", menuFile, KeyEvent.VK_T);
        miNewWindow = createMenuEntry("New Window", menuFile, KeyEvent.VK_W);
        miCloseTab = createMenuEntry("Close Tab", menuFile, KeyEvent.VK_B);
        miCloseWindow = createMenuEntry("Close Window", menuFile, KeyEvent.VK_L);
        menuFile.addSeparator();
        miExit = createMenuEntry("Exit", menuFile, KeyEvent.VK_X);
        //miTest = createMenuEntry("Test", menuFile, KeyEvent.VK_F16);
        menuBar.setVisible(true);

        // edit menu
        ButtonGroup editModes = new ButtonGroup();
        JMenu menuEdit = new JMenu("Edit");
        menuEdit.setMnemonic(KeyEvent.VK_E);
        menuEdit.addMenuListener(this);
        menuBar.add(menuEdit);
        miUndo = createMenuEntry("Undo", menuEdit, KeyEvent.VK_U);
        miRedo = createMenuEntry("Redo", menuEdit, KeyEvent.VK_R);
        menuEdit.addSeparator();
        miCut = createMenuEntry("Cut", menuEdit, KeyEvent.VK_T);
        miCopy = createMenuEntry("Copy", menuEdit, KeyEvent.VK_C);
        miPaste = createMenuEntry("Paste", menuEdit, KeyEvent.VK_P);
        menuEdit.addSeparator();
        miSelectAll = createMenuEntry("Select All", menuEdit, KeyEvent.VK_A);
        miSelectNone = createMenuEntry("Select None", menuEdit, KeyEvent.VK_N);
        menuEdit.addSeparator();
        miDelete = createMenuEntry("Delete", menuEdit, KeyEvent.VK_D);
        menuEdit.addSeparator();
        miEditOriginal = createRadioButtonMenuEntry("Edit Mode (Office-like)", menuEdit, KeyEvent.VK_E, editModes);
        miEditCtrl = createRadioButtonMenuEntry("Edit Mode (Hold Ctrl)", menuEdit, KeyEvent.VK_M, editModes);
        miUseOnly = createRadioButtonMenuEntry("Use GUI only", menuEdit, KeyEvent.VK_O, editModes);
        miEditOriginal.setSelected(true);
        menuEdit.addSeparator();
        miSnapToGrid = new JCheckBoxMenuItem("Snap to Grid", false);
        miSnapToGrid.addActionListener(this);
        menuEdit.add(miSnapToGrid);
        menuEdit.addSeparator();
        miChangePanelName = createMenuEntry("Change Panel Name...", menuEdit, KeyEvent.VK_H);
        miGuiSettings = createMenuEntry("Current GUI Settings...", menuEdit, KeyEvent.VK_S);
        miGlobalSettings = createMenuEntry("Global Settings...", menuEdit, KeyEvent.VK_G);

        // view menu
        JMenu menuView = new JMenu("View");
        menuView.setMnemonic(KeyEvent.VK_V);
        menuBar.add(menuView);
        miConnectionPanel = new JCheckBoxMenuItem("Connection Panel", false);
        miConnectionPanel.addActionListener(this);
        menuView.add(miConnectionPanel);

        // widget menu
        JMenu menuWidget = new JMenu("Widgets");
        menuWidget.setMnemonic(KeyEvent.VK_W);
        menuBar.add(menuWidget);

        // debug menu
        if (FinrocGUI.SHOWDEBUGMENU) {
            JMenu menuDebug = new JMenu("Debug");
            menuDebug.setMnemonic(KeyEvent.VK_D);
            menuBar.add(menuDebug);
            miDebugConsole = createMenuEntry("Console", menuDebug, KeyEvent.VK_C);
            miFrameworkElementDump = createMenuEntry("Dump FrameworkElement info", menuDebug, KeyEvent.VK_D);
        }

        // popup menu
        pmiChangePanelName = new JMenuItem("Change Name...");
        pmiChangePanelName.addActionListener(this);

        // create Toolbar
        toolBar = new MToolBar("Standard");
        toolBar.setFloatable(false);
        tbNew = toolBar.createButton("document-new.png", "New GUI", this);
        tbLoad = toolBar.createButton("document-open.png", "Load GUI", this);
        tbSave = toolBar.createButton("document-save.png", "Save current GUI", this);
        toolBar.addSeparator();
        tbCut = toolBar.createButton("edit-cut.png", "Cut", this);
        tbCopy = toolBar.createButton("edit-copy.png", "Copy", this);
        tbPaste = toolBar.createButton("edit-paste.png", "Paste", this);
        tbUndo = toolBar.createButton("edit-undo.png", "Undo last action", this);
        tbRedo = toolBar.createButton("edit-redo.png", "Redo", this);
        toolBar.addSeparator();

        // create and add Create-Widget actions
        for (Class <? extends Widget > widgetClass : WidgetAndInterfaceRegister.getInstance()) {
            Action action = new CreateWidgetAction(widgetClass);
            JMenuItem menuitem = new JMenuItem(action);
            menuitem.setToolTipText(null);
            menuitem.setIcon(null);
            menuWidget.add(menuitem);
            if (action.getValue(Action.SMALL_ICON) != null) {
                toolBar.add(action);
            }
        }

        // create and show GUI
        statusBar = new StatusBar();
        connectionPanel = new ConnectionPanel(this, getModel().getParent().getTreeFont());
        jframe.setJMenuBar(menuBar);
        jframe.pack();
        setVisible(true);
        setFocusTraversalKeysEnabled(false);
        arrangePanels(null);
        jframe.addWindowListener(this);
        addKeyListener(this);
        jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // set window to screen size
        Rectangle r = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setSize(r.width, r.height - 70);
        repaint();

        // set editmode
        editMode = getEditModeFromMenu();
        undoBuffer = new UndoBuffer<GUIWindow>();
        undoBuffer.addUndoComponent(miUndo);
        undoBuffer.addUndoComponent(tbUndo);
        undoBuffer.addRedoComponent(miRedo);
        undoBuffer.addRedoComponent(tbRedo);

        // register connection Listener
        getParent().addConnectionListener(this);
        updateToolBarState();
        connectionEvent(null, ConnectionListener.NOT_CONNECTED);  // update Menu item and toolbar state

        statusBarTimer = new Timer(2000, this);
        statusBarTimer.setInitialDelay(2000);
        statusBarTimer.start();
    }

    /**
     * Arrange panels (in tabs when there are multiple panels)
     * @param selectedPanel panelToSelect (if null first one will be selected)
     */
    private void arrangePanels(GUIPanelUI selectedPanel) {
        JFrame jframe = (JFrame)ui;
        Container root = jframe.getContentPane();
        root.removeAll();
        root.setLayout(new BorderLayout());
        root.add(toolBar, BorderLayout.PAGE_START);
        root.add(statusBar, BorderLayout.SOUTH);
        if (model == null) {
            jframe.setTitle("error: no model");
            return;
        }
        updateTitle();

        // remove old UIs
        removeObsoleteUIs();

        // create new UIs
        for (GUIPanel gp : getModel().getChildren()) {
            if (getChild(gp) == null) {
                children.add(new GUIPanelUI(this, gp));
            }
        }

        // GUIPanel(s)
        Component guipanels = null;
        switch (model.getChildren().size()) {
        case 0:
            model.add(new GUIPanel(model));
            return;
        case 1:
            guipanels = new JScrollPane(children.get(0).asComponent());
            break;
        default:
            tabbedPane = new JTabbedPane();
            tabbedPane.addChangeListener(this);
            for (GUIPanelUI panel : children) {
                JScrollPane temp = new JScrollPane(panel.asComponent());
                tabbedPane.addTab(panel.toString(), temp);
                if (selectedPanel == panel) {
                    tabbedPane.setSelectedComponent(temp);
                }
            }
            guipanels = tabbedPane;
            tabbedPane.addMouseListener(this);
        }
        for (GUIPanelUI panel : children) {
            panel.setParent(this);
        }

        // update strategies
        GUIPanel gp = getCurPanel().getModel();
        for (GUIPanel gp2 : getModel().getChildren()) {
            updatePortStrategies(gp2, gp == gp2);
        }

        // Connection Panel?
        if (miConnectionPanel.isSelected()) {
            JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, connectionPanel, guipanels);
            sp.setDividerSize(3);
            root.add(sp, BorderLayout.CENTER);
            connectionPanel.setRightTree(getCurPanel().getModel().getTreeModel());
            connectionPanel.setLeftTree(getParent().getTreeModel());
        } else {
            root.add(guipanels, BorderLayout.CENTER);
        }

        root.validate();
        repaint();
    }

    /**
     * Update port strategies of specified panel
     *
     * @param gp GUIPanel of which to update ports of
     * @param push Push values if this is default?
     */
    private void updatePortStrategies(GUIPanel gp, boolean push) {
        FinrocGUI.logDomain.log(LogLevel.LL_DEBUG_VERBOSE_1, "GUIWindowUI", "Setting strategies of panel " + gp.toString() + " to " + push);
        for (Widget w : gp.getChildren()) {
            for (WidgetPort<?> wp : w.getChildren()) {
                wp.updateStrategy(push);
            }
        }
    }

    /**
     * Convenient method the create menu entries and add this Window as listener
     *
     * @param string Text of menu entry
     * @param menuFile Menu to add menu entry to
     * @return Create menu entry
     */
    private JMenuItem createMenuEntry(String string, JMenu menuFile, int mnemonic) {
        JMenuItem item = new JMenuItem(string, mnemonic);
        item.addActionListener(this);
        menuFile.add(item);
        return item;
    }

    /**
     * Convenient method the create radion button menu entries and add this Window as listener
     *
     * @param string Text of menu entry
     * @param menuFile Menu to add menu entry to
     * @return Create menu entry
     */
    private JRadioButtonMenuItem createRadioButtonMenuEntry(String string, JMenu menu, int mnemonic, ButtonGroup bg) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(string);
        item.setMnemonic(mnemonic);
        item.addActionListener(this);
        menu.add(item);
        bg.add(item);
        return item;
    }

    public void actionPerformed(ActionEvent ae) {
        FinrocGUI guiCore = getParent();
        try {
            Object src = ae.getSource();
            if (src == miNew || src == tbNew) {
                guiCore.clearGUI(this, true);
//          } else if (src == miConnect) {
//              guiCore.connect();
            } else if (src == miDisconnectDiscard) {
                guiCore.disconnectDiscard();
//          } else if (src == miReconnect) {
//              guiCore.reconnect();
            } else if (src == miLoad || src == tbLoad) {
                guiCore.loadGUI(this, null);
                resetUndoBuffer();
            } else if (src == miSave || src == tbSave) {
                guiCore.saveGUI();
            } else if (src == miSaveAs) {
                guiCore.saveGUIAs();
            } else if (src == miExit) {
                guiCore.exit();
            } else if (src == miNewTab) {
                GUIPanel p = new GUIPanel(model);
                getModel().add(p);
                addUndoBufferEntry("Create Tab");
            } else if (src == miNewWindow) {
                guiCore.addNewWindow();
            } else if (src == miCloseTab) {
                getModel().remove(getCurPanel().getModel());
                addUndoBufferEntry("Close Tab");
            } else if (src == miCloseWindow) {
                windowClosing(null);
            } else if (src == miTest) {
                //BufferTest2.test(children.get(0));
            } else if (src == miCut || src == tbCut) {
                copy(new ArrayList<Widget>(getCurPanel().getModel().getSelection()), true);
            } else if (src == miCopy || src == tbCopy) {
                copy(new ArrayList<Widget>(getCurPanel().getModel().getSelection()), false);
            } else if (src == miDelete) {
                connectionPanel.rightTree.storeExpandedElements();
                getCurPanel().getModel().remove(new ArrayList<Widget>(getCurPanel().getModel().getSelection()));
                connectionPanel.rightTree.restoreExpandedElements();
                addUndoBufferEntry("Delete Widgets");
            } else if (src == miSelectAll) {
                getCurPanel().getModel().selectAll();
            } else if (src == miSelectNone) {
                getCurPanel().getModel().setSelection(null);
            } else if (src == miPaste || src == tbPaste) {
                paste(null);
            } else if (src == miConnectionPanel) {
                arrangePanels(getCurPanel());
            } else if (src == miUndo || src == tbUndo) {
                GUIWindow newModel = undoBuffer.undo();
                model.getParent().replaceChild(model, newModel);
                setModel(newModel);
            } else if (src == miRedo || src == tbRedo) {
                GUIWindow newModel = undoBuffer.redo();
                model.getParent().replaceChild(model, newModel);
                setModel(newModel);
            } else if (src == miDebugConsole) {
                new DebugConsole(this);
            } else if (src == miFrameworkElementDump) {
                RuntimeEnvironment.getInstance().printStructure();
            } else if (src == miChangePanelName || src == pmiChangePanelName) {
                String name = JOptionPane.showInputDialog("Please enter new name for panel", getCurPanel().getModel().toString());
                if (name != null) {
                    GUIPanel panel = (src == miChangePanelName) ? getCurPanel().getModel() : getModel().getChildAt(clickedOnTab);
                    panel.setName(name);
                    arrangePanels(getCurPanel());
                }
            } else if (src == miGuiSettings) {
                new PropertiesDialog((JFrame)ui, getModel().getParent(), getModel().getRoot().getEmbeddedFileManager(), false);
                addUndoBufferEntry("Change GUI Settings");
                getModel().getParent().fireDataModelEvent(DataModelListener.Event.WidgetPropertiesChanged, getModel().getParent());
                //connectionPanel.setTreeFont(getModel().getRoot().getTreeFont());
            } else if (src == miGlobalSettings) {
                new PropertiesDialog((JFrame)ui, getParent().getPersistentSettings(), getModel().getRoot().getEmbeddedFileManager(), false);
                getModel().getRoot().getEmbeddedFileManager().update();
            } else if (src == miEditOriginal || src == miEditCtrl || src == miUseOnly) {
                setEditMode(getEditModeFromMenu());
                getModel().getParent().setEditMode(getEditMode().ordinal());
            } else if (src == statusBarTimer) {
                connectionEvent(null, 0);
                return;
            } else if (pmiMoveTo.contains(src)) {
                int moveTo = pmiMoveTo.indexOf(src);
                GUIPanelUI current = getCurPanel();
                getModel().moveChildTo(getModel().getChildAt(clickedOnTab), moveTo); // move in model
                GUIPanelUI remove = children.remove(clickedOnTab); // move in ui
                children.add(moveTo, remove);
                arrangePanels(current);
            }
            requestFocus();
        } catch (Exception e) {
            guiCore.showErrorMessage(e);
        }
        updateToolBarState();
    }

    public void updateToolBarState() {
        // conditions identical to menuSelected... definetely possible to make things nicer here...
        tbPaste.setEnabled(getClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor));
        try {
            tbCut.setEnabled(getCurPanel().getModel().getSelection().size() > 0);
            tbCopy.setEnabled(tbCut.isEnabled());
        } catch (Exception e) {
            tbCut.setEnabled(false);
            tbCopy.setEnabled(false);
        }
    }


    public void paste(Point pos) {
        try {
            GUIPanel panel = getCurPanel().getModel();
            ClipboardContent cc = new ClipboardContent(this);
            List<Widget> elemData = cc.selection;
            if (elemData.size() == 0) {
                return;
            }
            for (Widget w : elemData) {
                w.restore(panel);
            }

            if (pos == null) { // an freiem Ort einfügen
                // If components at stored coordinates already exist, place somewhere else
                while (getCurPanel().widgetsWithSamePosExist(elemData)) {
                    for (Widget w : elemData) {
                        w.setLocation(w.getBounds().x + 30, w.getBounds().y + 30);
                    }
                }
            } else { // an Position einfügen

                // find topleft of clipboard
                Point p = new Point(1000000, 1000000);
                for (Widget w : elemData) {
                    p.x = Math.min(p.x, w.getBounds().x);
                    p.y = Math.min(p.y, w.getBounds().y);
                }

                // value to add to each position
                Point diff = new Point(pos.x - p.x, pos.y - p.y);

                for (Widget w : elemData) {
                    w.setLocation(w.getBounds().x + diff.x, w.getBounds().y + diff.y);
                }
                //System.out.println(pos + " " + p + " " + diff);
            }

            for (Widget w : elemData) {
                panel.add(w);
            }
            panel.setSelection(elemData);
            addUndoBufferEntry("Paste");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui, "Cannot import current clipboard contents", "Paste Operation failed", JOptionPane.ERROR_MESSAGE);
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, "GUIWindowUI", e);
        }
    }

    @Override
    public void connectionEvent(ExternalConnection source, int e) {
        statusBar.setStatus(getParent().getActiveInterfaces());
        /*if (connectionPanel != null && source != null) {
            connectionPanel.setLeftTree(((JMCAGUI)source).getTreeModel());
        }*/
        /*if (miConnectMenu != null) {
            boolean oneConnected = false;
            boolean allConnected = true;
            boolean allReconnectable = true;
            for (int i = 0; i < miConnectMenu.getItemCount(); i++) {
                RobotInterface ioi = parent.getActiveInterfaces().get(i);
                boolean connected = ioi.isConnected();
                oneConnected |= connected;
                allConnected &= connected;
                boolean reconnectable = (ioi.getConnectionAddress() != null && ioi.getConnectionAddress().length() > 0);
                allReconnectable &= reconnectable;
                miConnectMenu.getItem(i).getAction().setEnabled(!connected);
                miDisconnectMenu.getItem(i).getAction().setEnabled(connected);
                miReconnectMenu.getItem(i).getAction().setEnabled(reconnectable);
            }
            miConnect.setEnabled(!allConnected);
            miDisconnect.setEnabled(oneConnected);
            miReconnect.setEnabled(!allConnected && allReconnectable);
        }*/
    }

    public void windowClosing(WindowEvent e) {
        getParent().closeWindow(model);
    }

    public void areaSelected(Rectangle createRectangle) {
        GUIPanel panel = getCurPanel().getModel();
        List<Widget> selection = new ArrayList<Widget>();
        if (editMode == EditMode.createObject) {
            try {
                Widget w = (Widget)objectToCreate.newInstance();
                w.restore(panel);
                createRectangle.width = (int)((createRectangle.width < WIDGET_MIN_SIZE) ? WIDGET_STANDARD_SIZE.getWidth() : createRectangle.width);
                createRectangle.height = (int)((createRectangle.height < WIDGET_MIN_SIZE) ? WIDGET_STANDARD_SIZE.getHeight() : createRectangle.height);
                w.setBounds(createRectangle);
                panel.add(w);
                selection.add(w);
                panel.setSelection(selection);
                addUndoBufferEntry("Create " + w.toString());
            } catch (Exception e) {
                parent.showErrorMessage(e);
            }
            setEditMode(getEditModeFromMenu());
        } else if (editMode == EditMode.editObject || editMode == EditMode.ctrlEditObject) {
            for (Widget w : panel.getChildren()) {
                if (createRectangle.intersects(w.getBounds())) {
                    selection.add(w);
                }
            }
            panel.setSelection(selection);
        }
        updateToolBarState();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlPressed = false;
        }

        // update state of menu items
        menuSelected(null);

        int ctrlMask = KeyEvent.CTRL_DOWN_MASK;
        int shiftAltMask = KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK;

        // Control pressed?
        if ((e.getModifiersEx() & (ctrlMask | shiftAltMask)) == ctrlMask) {
            if (e.getKeyCode() == KeyEvent.VK_X && miCut.isEnabled()) {
                actionPerformed(new ActionEvent(miCut, 0, ""));
            }
            if (e.getKeyCode() == KeyEvent.VK_C && miCopy.isEnabled()) {
                actionPerformed(new ActionEvent(miCopy, 0, ""));
            }
            if (e.getKeyCode() == KeyEvent.VK_V && miPaste.isEnabled()) {
                actionPerformed(new ActionEvent(miPaste, 0, ""));
            }
            if (e.getKeyCode() == KeyEvent.VK_Z && miUndo.isEnabled()) {
                actionPerformed(new ActionEvent(miUndo, 0, ""));
            }
            if (e.getKeyCode() == KeyEvent.VK_Y && miRedo.isEnabled()) {
                actionPerformed(new ActionEvent(miRedo, 0, ""));
            }
            if (e.getKeyCode() == KeyEvent.VK_A) {
                actionPerformed(new ActionEvent(miSelectAll, 0, ""));
            }
        }

        if (e.getModifiersEx() == 0) {
            if (e.getKeyCode() == KeyEvent.VK_DELETE && miDelete.isEnabled()) {
                actionPerformed(new ActionEvent(miDelete, 0, ""));
            }
            if (e.getKeyCode() == KeyEvent.VK_TAB) {
                miConnectionPanel.setSelected(!miConnectionPanel.isSelected());
                actionPerformed(new ActionEvent(miConnectionPanel, 0, ""));
            }
        }
        requestFocus();
    }

    public void keyTyped(KeyEvent e) {
    }

    /**
     * @return Is control button currently pressed?
     */
    public boolean isCtrlPressed() {
        return ctrlPressed;
    }

    public Clipboard getClipboard() {
        if (clipboard == null) {
            if (FinrocGUI.USE_SYSTEM_CLIPBOARD == true) {
                clipboard = ui.getToolkit().getSystemClipboard();
            } else {
                clipboard = new Clipboard("fingui clipboard");
            }
        }
        return clipboard;
    }

    @Override
    public void copy(List<Widget> sel, boolean cutElements) {
        new ClipboardContent(sel, this).toClipboard(this);
        if (cutElements) {
            connectionPanel.rightTree.storeExpandedElements();
            getCurPanel().getModel().remove(new ArrayList<Widget>(sel));
            connectionPanel.rightTree.restoreExpandedElements();
            addUndoBufferEntry("Cut");
        }
    }

    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}


    @SuppressWarnings("rawtypes")
    public void dataModelChanged(DataModelBase caller, org.finroc.tools.gui.abstractbase.DataModelListener.Event event, Object param) {
        if (caller != model) {
            caller.removeDataModelListener(this);
            return;
        }

        // restore edit mode from gui file
        int em = getParent().getModel().getEditMode();
        if (em == EditMode.editObject.ordinal()) {
            miEditOriginal.setSelected(true);
        } else if (em == EditMode.ctrlEditObject.ordinal()) {
            miEditCtrl.setSelected(true);
        } else {
            miUseOnly.setSelected(true);
        }
        setEditMode(getEditModeFromMenu());

        if (event == DataModelListener.Event.CompleteChange || event == DataModelListener.Event.ChildAdded || event == DataModelListener.Event.ChildRemoved) {
            GUIPanelUI gpu = null;
            try {
                gpu = getCurPanel();
            } catch (Exception e) {}
            arrangePanels(gpu);
        }
    }

    public void menuCanceled(MenuEvent e) {}
    public void menuDeselected(MenuEvent e) {}
    public void menuSelected(MenuEvent e) {
        FinrocGUI guiCore = getParent();

        // cut/copy/delete enabled?
        miCut.setEnabled(getCurPanel().getModel().getSelection().size() > 0);
        miCopy.setEnabled(miCut.isEnabled());
        miDelete.setEnabled(miCut.isEnabled());
        // paste enabled?
        miPaste.setEnabled(getClipboard().isDataFlavorAvailable(DataFlavor.stringFlavor));
        // close tab enabled?
        miCloseTab.setEnabled(children.size() > 1);
        // close window enabled?
        miCloseWindow.setEnabled(model.getParent().getChildCount() > 1);
        // save enabled?
        miSave.setEnabled(guiCore.hasChanged());
        // connection stuff
        //miConnect.setEnabled(!guiCore.isConnected());
        //miDisconnect.setEnabled(guiCore.isConnected());
        //miReconnect.setEnabled(!guiCore.isConnected() && !guiCore.getConnectionAddress().equals(""));

        // create and add Recent-File actions
        miRecentFiles.removeAll();
        for (File f : getParent().getRecentFiles()) {
            Action a = new LoadRecentFileAction(f);
            miRecentFiles.add(a);
        }
    }

    public void addUndoBufferEntry(String description) {
        undoBuffer.addEntry(model, description);
    }

    public void resetUndoBuffer() {
        if (undoBuffer != null) {
            undoBuffer.clear();
        }
    }

    public void stateChanged(ChangeEvent e) {

        // from TabbedPane
        updateToolBarState();
        connectionPanel.setRightTree(getCurPanel().getModel().getTreeModel());
    }

    class CreateWidgetAction extends AbstractAction {

        /** UID */
        private static final long serialVersionUID = 5229170166031859951L;

        private Class <? extends Widget > widgetClass;

        public CreateWidgetAction(Class <? extends Widget > widgetClass2) {
            this.widgetClass = widgetClass2;
            putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon(widgetClass2.getSimpleName() + ".png"));
            putValue(Action.NAME, widgetClass2.getSimpleName());
            putValue(Action.SHORT_DESCRIPTION, "Create " + widgetClass2.getSimpleName());
        }


        public void actionPerformed(ActionEvent e) {
            setEditMode(EditMode.createObject);
            objectToCreate = widgetClass;
        }
    }

    class LoadRecentFileAction extends AbstractAction {

        /** UID */
        private static final long serialVersionUID = 7651942164395391202L;

        private File f;

        public LoadRecentFileAction(File f) {
            this.f = f;
            putValue(Action.NAME, f.getName());
            putValue(Action.SHORT_DESCRIPTION, f.getAbsolutePath());
        }

        public void actionPerformed(ActionEvent e) {
            try {
                getParent().loadGUI(GUIWindowUI.this, f);
                resetUndoBuffer();
            } catch (Exception ex) {
                getParent().showErrorMessage(ex);
            }
        }
    }

    class ConnectAction extends AbstractAction {

        /** UID */
        private static final long serialVersionUID = 8268564574563185951L;

        private CreateExternalConnectionAction ioInterface;
        private boolean disconnect, reconnect;

        public ConnectAction(CreateExternalConnectionAction ioInterface, boolean disconnect, boolean reconnect) {
            this.ioInterface = ioInterface;
            this.disconnect = disconnect;
            this.reconnect = reconnect;
            putValue(Action.NAME, ioInterface.getName());
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (disconnect) {
                    //ioInterface.disconnect();
                } else if (reconnect) {
                    //ioInterface.reconnect();
                } else {
                    //ioInterface.connect(null);
                    ExternalConnection ec = ioInterface.createExternalConnection();
                    GUIWindowUI.this.getParent().ioInterface.getRootFrameworkElement().addChild(ec);
                    ec.init();
                    ec.addConnectionListener(GUIWindowUI.this);
                    if (getParent().getPersistentSettings().lastConnectionAddress != null) {
                        ec.setDefaultAddress(getParent().getPersistentSettings().lastConnectionAddress);
                    }
                    ec.connect(null);
                    if (ec.isConnected()) {
                        getParent().getPersistentSettings().lastConnectionAddress = ec.getConnectionAddress();
                        getParent().getModel().addConnectionAddress(ioInterface.getName() + ":" + ec.getConnectionAddress());
                    }
                    //parent.ioInterface.addModule(ioInterface.createModule());
                }
                parent.updateInterface();
            } catch (Exception ex) {
                parent.showErrorMessage(ex);
            }
        }
    }

    public boolean snapToGrid() {
        return miSnapToGrid.isSelected();
    }

    public void setTreeFont(Font treeFont) {
        if (connectionPanel != null) {
            connectionPanel.setTreeFont(treeFont);
        }
    }

    public void updateTitle() {
        ((JFrame)ui).setTitle(TITLE + ((getParent().toString() != "") ? (" - " + getParent().toString()) : ""));
    }

    private EditMode getEditModeFromMenu() {
        if (miEditOriginal.isSelected()) {
            return EditMode.editObject;
        } else if (miEditCtrl.isSelected()) {
            return EditMode.ctrlEditObject;
        } else {
            return EditMode.none;
        }
    }

    private static class ClipboardContent {
        private List<Widget> selection;
        private Map<EmbeddedFile, byte[]> files = new HashMap<EmbeddedFile, byte[]>();

        public ClipboardContent(List<Widget> content, GUIWindowUI thizz) {
            selection = content;

            // copy embedded files, if any
            FileManager efm = thizz.getModel().getRoot().getEmbeddedFileManager();
            for (Widget w : content) {
                for (AbstractFile af : efm.getEmbeddedFiles(w)) {
                    if (af instanceof EmbeddedFile) {
                        EmbeddedFile ef = (EmbeddedFile)af;
                        files.put(ef, ef.getData(efm));
                    }
                }
            }
        }

        /**
         * Restore clipboard contents from clipboard
         */
        public ClipboardContent(GUIWindowUI thizz) throws Exception {
            String ssel = (String)thizz.getClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor);
            ClipboardContent cc = (ClipboardContent)FinrocGuiXmlSerializer.getInstance().fromXML(ssel.toString());
            files = cc.files;
            selection = cc.selection;

            // restore embedded files
            FileManager efm = thizz.getModel().getRoot().getEmbeddedFileManager();
            for (Map.Entry<EmbeddedFile, byte[]> entry : files.entrySet()) {
                efm.importFile(entry.getKey(), entry.getValue());
            }
        }

        public void toClipboard(GUIWindowUI thizz) {
            thizz.getClipboard().setContents(new StringSelection(toString()), null);
        }

        public String toString() {
            return FinrocGuiXmlSerializer.getInstance().toXML(this);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent me) {

        // from JTabbedPane
        JTabbedPane tabs = (JTabbedPane)me.getSource();

        if (me.getButton() == MouseEvent.BUTTON3 && (getEditMode() == GUIWindowUI.EditMode.editObject || (getEditMode() == GUIWindowUI.EditMode.ctrlEditObject && isCtrlPressed()))) {

            // show right-click-menu?
            Point p = me.getPoint();
            int tabIdx = tabs.indexAtLocation(p.x, p.y);
            if (tabIdx >= 0) {
                clickedOnTab = tabIdx;

                JPopupMenu pmenu = new JPopupMenu();
                pmenu.add(pmiChangePanelName);
                pmenu.addSeparator();
                for (int i = 0; i < getModel().getChildCount(); i++) {
                    if (i >= pmiMoveTo.size()) {
                        JMenuItem item = new JMenuItem("Move to position " + (i + 1));
                        item.addActionListener(this);
                        pmiMoveTo.add(item);
                    }
                    JMenuItem item = pmiMoveTo.get(i);
                    pmenu.add(item);
                    item.setEnabled(i != clickedOnTab);
                }
                pmenu.show(tabs, me.getX(), me.getY());
            }
        }

        // update strategies
        GUIPanel gp = getCurPanel().getModel();
        for (GUIPanel gp2 : getModel().getChildren()) {
            updatePortStrategies(gp2, gp == gp2);
        }
    }

    @Override
    public void dispose() {
        statusBarTimer.stop();
    }

    @Override
    public void refreshConnectionPanelModels() {
        getParent().updateInterface();
    }
}
