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

import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.finroc.tools.gui.abstractbase.UIBase;
import org.finroc.tools.gui.commons.EventRouter;
import org.finroc.tools.gui.util.embeddedfiles.FileManager;
import org.finroc.tools.gui.util.gui.IconManager;
import org.finroc.tools.gui.util.propertyeditor.gui.ResourcePathProvider;

import org.finroc.core.RuntimeSettings;
import org.finroc.core.plugin.ConnectionListener;
import org.rrlib.xml.XMLDocument;
import org.rrlib.xml.XMLNode;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.XStream;

public abstract class GUIUiBase < P extends UIBase <? , ? , ? , ? >, C extends UIBase <? , ? , ? , ? >> extends UIBase<P, Container, GUI, C> implements ResourcePathProvider {

    /** Constants for GUI files */
    public static final String GUI_FILE_EXTENSION = "fingui";
    public static final String GUI_MAIN_FILE_IN_ZIP = "gui.xml";

    public static final String GUI_MAIN_FILE_IN_BINARY_ZIP = "gui.bin";

    /** Temporary variable: Is GUI connected? */
    private boolean connected = false;

    public abstract void showErrorMessage(Exception e);
    public abstract void showErrorMessage(String string);

    public GUIUiBase() {
        super(null, null, null);
        IconManager.getInstance().addResourceFolder(GUIUiBase.class, "icons");
        IconManager.getInstance().addResourceFolder(GUIUiBase.class, "themes");
    }

    /**
     * Save or export GUI to specified stream. Stream is closed afterwards.
     *
     * @param f File to write GUI to. If null then current filename is used.
     * @param export Is GUI exported to web-browser?
     * @return Has GUI been saved
     */
    public void saveGUI(GUI g, OutputStream os, boolean binary) throws Exception {

        ObjectOutputStream oos;

        // save GUI
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
        g.getEmbeddedFileManager().saveFiles(zos);
        if (!binary) {
            zos.putNextEntry(new ZipEntry(GUI_MAIN_FILE_IN_ZIP));
            oos = FinrocGuiXmlSerializer.getInstance().createObjectOutputStream(new OutputStreamWriter(zos));
        } else {
            zos.putNextEntry(new ZipEntry(GUI_MAIN_FILE_IN_BINARY_ZIP));
            oos = new ObjectOutputStream(zos);
        }
        oos.writeObject(g);
        oos.close();
    }

    public GUI importGUI(File f) throws Exception {

        for (GUICodec gc : WidgetAndInterfaceRegister.getGUICodecs()) {
            if (gc.supportsImport() && f.getAbsolutePath().toLowerCase().endsWith(gc.getStandardExtension().toLowerCase())) {
                GUI newGui = gc.importGUI(this, new FileInputStream(f), f);
                newGui.setJmcagui(this);
                newGui.restore(null);
                return newGui;
            }
        }
        throw new UnsupportedEncodingException("Unsupported file format");
    }

    public void exportGUI(File f) throws Exception {

        for (GUICodec gc : WidgetAndInterfaceRegister.getGUICodecs()) {
            if (gc.supportsExport() && f.getAbsolutePath().toLowerCase().endsWith(gc.getStandardExtension().toLowerCase())) {
                gc.exportGUI(getModel(), new FileOutputStream(f));
            }
        }
        throw new UnsupportedEncodingException("Unsupported file format");
    }

    /**
     * Load GUI from HDD
     */
    public GUI loadGUI(InputStream is) throws Exception {

        // load GUI
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
        GUI newGui = null;
        FileManager newEfm = new FileManager();

        while (true) {
            ZipEntry ze = zis.getNextEntry();
            if (ze == null) {
                break;
            } else if (ze.getName().equals(GUI_MAIN_FILE_IN_ZIP)) {  // XML_GUI

                // import/transform old guis
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Reader r = new OldVersionReader(new InputStreamReader(zis));
                Writer w = new OutputStreamWriter(baos);
                while (true) {
                    int c = r.read();
                    if (c < 0) {
                        break;
                    }
                    w.write(c);
                }
                w.close();
                //r.close();  // Don't do this - although Eclipse warns: This will close the zip file

                // read gui
                if (!RuntimeSettings.isRunningInApplet()) {
                    FinrocGuiXmlSerializer.getInstance().setMode(XStream.XPATH_RELATIVE_REFERENCES);
                    ObjectInputStream ois = FinrocGuiXmlSerializer.getInstance().createObjectInputStream(new InputStreamReader(new ByteArrayInputStream(baos.toByteArray())));
                    newGui = (GUI)ois.readObject();
                    FinrocGuiXmlSerializer.getInstance().setMode(XStream.NO_REFERENCES);
                } else {
                    XMLDocument document = new XMLDocument(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false);
                    newGui = new GUI(this);
                    XMLNode root = document.getRootNode();
                    for (XMLNode child : root.children()) {
                        if (child.getName().equals("fingui")) {
                            newGui.deserialize(child);
                            break;
                        }
                    }
                }
                newGui.setEmbeddedFileManager(newEfm);
                newEfm.setModel(newGui);
            } else if (ze.getName().equals(GUI_MAIN_FILE_IN_BINARY_ZIP)) {  // Binary GUI
                ObjectInputStream ois = new ObjectInputStream(zis);
                newGui = (GUI)ois.readObject();
                newGui.setEmbeddedFileManager(newEfm);
                newEfm.setModel(newGui);
            } else {  // EmbeddedFile
                newEfm.loadFile(zis, ze);
            }
        }
        zis.close();

        newGui.setJmcagui(this);
        newGui.restore(null);

        return newGui;
    }

    public void addConnectionListener(ConnectionListener l) {
        EventRouter.addListener(this, l, ConnectionListener.class);
    }
    public void removeConnectionListener(ConnectionListener l) {
        EventRouter.removeListener(this, l, ConnectionListener.class);
    }
    public void fireConnectionEvent(int e) {
        if (e == ConnectionListener.CONNECTED) {
            connected = true;
        } else if (e == ConnectionListener.NOT_CONNECTED) {
            connected = false;
        }
        EventRouter.fireConnectionEvent(null, e);
    }
    public boolean isConnected() {
        return connected;
    }

    public String[] getSupportedExtensions() {
        List<String> result = new ArrayList<String>();
        result.add(GUI_FILE_EXTENSION);
        for (GUICodec gc : WidgetAndInterfaceRegister.getGUICodecs()) {
            if (gc.supportsImport()) {
                result.add(gc.getStandardExtension());
            }
        }
        return result.toArray(new String[0]);
    }


//    public abstract PortWrapper getInput(String uid);
//
//    public abstract PortWrapper getOutput(String uid);

    //public abstract List<Plugin> getPlugIns();
}

/**
 * Reader class that is used to read gui files that have been created with older versions
 * of Jmcagui.
 * Replaces class name strings of data types for instance.
 *
 * Use as reader (no BufferedReader specific methods)
 */
class OldVersionReader extends BufferedReader {

    /** Map with replacement strings */
    private Map<String, String> replace = initMap();

    private String bufferedLine;
    private int pos;

    private Map<String, String> initMap() {
        Map<String, String> r = new HashMap<String, String>();
        r.put("<type>org.mca.commons.ContainsStrings</type>", "<type>org.mca.commons.datatype.ContainsStrings</type>");
        r.put("<type>org.mca.commons.BehaviourInfo$List</type>", "<type>org.mca.commons.datatype.BehaviourInfo$List</type>");
        return r;
    }

    protected OldVersionReader(Reader in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        if (bufferedLine == null || pos >= bufferedLine.length()) {

            // read next line
            bufferedLine = super.readLine();
            if (bufferedLine == null) {
                //close();
                return -1;
            }
            for (Map.Entry<String, String> r : replace.entrySet()) {
                if (bufferedLine.contains(r.getKey())) {
                    bufferedLine = bufferedLine.replace(r.getKey(), r.getValue());
                }
            }
            pos = 0;
            //System.out.println(bufferedLine);
        }
        pos++;
        return bufferedLine.charAt(pos - 1);
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public String readLine() {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public void reset() throws IOException {
        bufferedLine = null;
        super.reset();
    }
}
