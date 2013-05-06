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
package org.finroc.tools.gui.util.embeddedfiles;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.tree.TreeNode;

import org.finroc.tools.gui.FinrocGUI;
import org.finroc.tools.gui.util.propertyeditor.ObjectCloner;
import org.finroc.tools.gui.util.propertyeditor.gui.ResourcePathProvider;
import org.rrlib.finroc_core_utils.log.LogLevel;
import org.rrlib.finroc_core_utils.log.LogStream;

import org.finroc.core.util.Files;

/**
 * @author Max Reichardt
 *
 * This class manages embedded files (and makes sure no duplicates are loaded)
 */

public class FileManager {

    /** Currently loaded Embedded file raw data (UID->Data) */
    private Map<EmbeddedFile, byte[]> files = new HashMap<EmbeddedFile, byte[]>();

    /** Data model which contains the embedded files */
    private TreeNode model;

    /** Resource path provider - Object that says which resource paths we are currently using */
    private ResourcePathProvider resourcePathProvider;

    public FileManager(TreeNode dataModel) {
        setModel(dataModel);
    }

    public FileManager() {
    }

    public void setModel(TreeNode dataModel) {
        model = dataModel;
        if (dataModel instanceof ResourcePathProvider) {
            resourcePathProvider = (ResourcePathProvider)dataModel;
        }
    }

    /** Should be called when files are deleted */
    public synchronized List<AbstractFile> update() {
        //files.clear();
        return getEmbeddedFiles(model);
    }

    private void updateHelper(List<AbstractFile> result, TreeNode curNode) {

        if (curNode instanceof HasEmbeddedFiles) {
            result.addAll(((HasEmbeddedFiles)curNode).getEmbeddedFiles());
        }

        if (curNode.children() == null) {
            return;
        }
        for (Enumeration<?> e = curNode.children() ; e.hasMoreElements() ;) {
            TreeNode dmbi = (TreeNode)e.nextElement();
            updateHelper(result, dmbi);
        }
    }

    public synchronized List<AbstractFile> getEmbeddedFiles(TreeNode rootNode) {
        List<AbstractFile> result = new ArrayList<AbstractFile>();
        updateHelper(result, rootNode);
        for (AbstractFile f : result) { // update relative file names
            if (f instanceof ExternalFile) {
                ((ExternalFile)f).updateRelativeFilename(this);
            }
        }
        return result;
    }

    public synchronized void loadFile(ZipInputStream zis, ZipEntry ze) throws Exception {

        // load data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while (true) {
            int b = zis.read();
            if (b == -1) {
                break;
            }
            baos.write(b);
        }
        byte[] data = baos.toByteArray();

        EmbeddedFile ef = new EmbeddedFile(Long.parseLong(ze.getName().split("/")[1]), data.length);
        files.put(ef, data);

        ef.init(this);
        printStatus();
    }

    public synchronized void saveFiles(ZipOutputStream zos) throws Exception {
        List<AbstractFile> saveFiles = update();
        Set<Long> saved = new HashSet<Long>();
        for (AbstractFile af : saveFiles) {
            if (!(af instanceof EmbeddedFile)) {
                continue;
            }
            EmbeddedFile ef = (EmbeddedFile)af;
            if (!saved.contains(ef.getUid())) {

                // save data
                zos.putNextEntry(new ZipEntry(ef.getUniqueZipFilename()));
                zos.write(getData(ef.getUid()));

                saved.add(ef.getUid());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized <T extends AbstractFile> T loadFile(File f, Class<T> c) throws Exception {
        Constructor <? extends AbstractFile > con = c.getDeclaredConstructor(File.class);
        AbstractFile abstractNewfile = con.newInstance(f);

        if (abstractNewfile instanceof EmbeddedFile) {
            EmbeddedFile newfile = (EmbeddedFile)abstractNewfile;

            // load data
            byte[] data = null;
            if (f instanceof VirtualFile) {
                data = ((VirtualFile)f).getData();
            } else {
                data = new byte[(int)f.length()];
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
                bis.read(data);
                bis.close();
            }

            // Is file with same data already loaded?
            for (Entry<EmbeddedFile, byte[]> ef : files.entrySet()) {
                if (ObjectCloner.equal(ef.getValue(), data)) {
                    newfile.setUid(ef.getKey().getUid());
                    newfile.init(this);
                    return (T)newfile;
                }
            }

            files.put(newfile, data);
            printStatus();
        }

        abstractNewfile.init(this);
        return (T)abstractNewfile;
    }

    public synchronized void importFile(EmbeddedFile newfile, byte[] data) {
        // Is file with same data already loaded?
        for (Entry<EmbeddedFile, byte[]> ef : files.entrySet()) {
            if (ObjectCloner.equal(ef.getValue(), data)) {
                newfile.setUid(ef.getKey().getUid());
                newfile.init(this);
                return;
            }
        }

        files.put(newfile, data);
        printStatus();
    }

    private void printStatus() {
        int space = 0;
        for (byte[] data : files.values()) {
            space += data.length;
        }
        FinrocGUI.logDomain.log(LogLevel.LL_DEBUG, "FileManager", "File loaded. Currently " + files.size() + " files loaded (" + space + " bytes).");
    }

    byte[] getData(long uid) {
        for (EmbeddedFile ef : files.keySet()) {
            if (ef.getUid() == uid) {
                return files.get(ef);
            }
        }
        throw new RuntimeException("Data not loaded");
    }

    /**
     * Retrieve relative file name - for current set of resource paths
     *
     * @param file File
     * @param current Current relative file name
     * @return relative filename (as string)
     */
    public String getRelativeFilename(File file, String current) {
        if (file == null) {
            FinrocGUI.logDomain.log(LogLevel.LL_WARNING, "FileManager", "Cannot locate external file/folder: " + current);
            return current;
        }

        List<File> rpaths = resourcePathProvider.getResourcePaths();

        // does file still exist?
        if (current != null && current.length() > 0 && (!current.contains("../"))) {
            for (File p : rpaths) {
                if (new File(p.getAbsolutePath() + File.separator + current).exists()) {
                    return current;
                }
            }
        }

        // is file in sub-directory of a resource path?
        for (File p : rpaths) {
            if (Files.isParentOf(p, file)) {
                return file.getAbsolutePath().substring(p.getAbsolutePath().length() + 1);
            }
        }

        // evil MCA-specific hack... Is it in some $MCAHOME
        File parent = file.getParentFile();
        while (parent != null) {
            if (isMCADir(parent)) {
                return file.getAbsolutePath().substring(parent.getAbsolutePath().length() + 1);
            }
            parent = parent.getParentFile();
        }

        // no resource paths => absolute file
        if (rpaths.size() == 0) {
            return file.getAbsolutePath();
        }

        // return it relative to first resource path
        parent = file.getParentFile().getAbsoluteFile();
        File firstDir = rpaths.get(0).getAbsoluteFile();
        String prefix = "";
        while (parent != null && firstDir != null) {
            if (parent.equals(firstDir)) {
                return prefix + file.getAbsolutePath().substring(parent.getAbsolutePath().length() + 1);
            } else if (parent.getAbsolutePath().length() > firstDir.getAbsolutePath().length()) {
                parent = parent.getParentFile();
            } else {
                if (!firstDir.getName().equals(".")) {
                    prefix += "../";
                }
                firstDir = firstDir.getParentFile();
            }
        }

        return file.getAbsolutePath();
    }

    /**
     * Is directory an MCA directory?
     *
     * @param dir directory
     * @return Answer
     */
    private boolean isMCADir(File dir) {
        return new File(dir.getAbsolutePath() + "/script/mcasetenv.py").exists();
    }

    /**
     * If GUI file is loaded from HDD - this method is called when the program tries
     * to find an external file again.
     * (It might be on a different system and resource paths might have changed)
     *
     * @param absFilename Absolute filename when file was referenced
     * @param relFilename Relative filename when GUI file was saved
     * @return
     */
    public File findExternalFileAgain(String absFilename, String relFilename) {
        File result = null;

        // ok... look in resource paths
        List<File> rpaths = resourcePathProvider.getResourcePaths();
        for (File f : rpaths) {
            f = f.getAbsoluteFile();
            result = new File(f + File.separator + relFilename);
            if (result.exists()) {
                while (relFilename.startsWith(".." + File.separator)) {
                    relFilename = relFilename.substring(3);
                    while (f.getName().equals(".")) {
                        f = f.getParentFile();
                    }
                    f = f.getParentFile();
                }
                return new File(f + File.separator + relFilename);
            }
        }

        LogStream ls = FinrocGUI.logDomain.getLogStream(LogLevel.LL_WARNING, "FileManager");
        ls.append("warning: file " + relFilename + " not found in resource paths... ");

        // hmm... not found - maybe the absolute file name is still the same
        result = new File(absFilename);
        if (result.exists()) {
            ls.append("luckily " + absFilename + " is still there");
            ls.close();
            return result;
        }

        ls.append("not loading it");
        ls.close();
        return null;
    }

    /**
     * Resolve File Object for ExternalFile
     *
     * @param sceneFile External file
     * @return Resolved file object
     */
    public File getFile(ExternalFile sceneFile) {
        if (sceneFile == null) {
            return null;
        }
        File f = sceneFile.getFile(this);
        return f;
    }
}
