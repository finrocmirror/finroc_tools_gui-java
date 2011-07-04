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
package org.finroc.tools.gui.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.finroc.tools.gui.GUIUiWithInterfaces;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.InteractiveCallback;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.SFTPv3Client;
import ch.ethz.ssh2.SFTPv3FileAttributes;

/**
 * @author Max Reichardt
 *
 * Utility class to synchronize files or directories over SSH
 */
public class SSHResourceSynchronizer implements FilenameFilter, InteractiveCallback {

    private File baseDir;
    private String destDir;
    private Connection sshConnection;
    private SFTPv3Client sftp;
    private SCPClient scp;
    private static final Pattern destPattern = Pattern.compile("(.*)@(.*):(.*)");
    private String pwd;

    public SSHResourceSynchronizer(String destination, String password) throws Exception {
        this(destination, password, 22);
    }

    public SSHResourceSynchronizer(String destination, String password, int port) throws Exception {
        Matcher match = destPattern.matcher(destination);
        if (!match.matches() || match.groupCount() != 3) {
            throw new Exception("Invalid destination address");
        }
        sshConnection = new Connection(match.group(2), port);
        sshConnection.connect();
        pwd = password;
        boolean isAuthenticated = false;
        if (sshConnection.isAuthMethodAvailable(match.group(1), "password")) {
            isAuthenticated = sshConnection.authenticateWithPassword(match.group(1), password);
        } else {
            isAuthenticated = sshConnection.authenticateWithKeyboardInteractive(match.group(1), null, this);
        }
        if (isAuthenticated == false) {
            throw new IOException("Authentication failed.");
        }
        sftp = new SFTPv3Client(sshConnection);
        scp = sshConnection.createSCPClient();
        destDir = match.group(3);
        baseDir = GUIUiWithInterfaces.getPluginDir().getParentFile();
    }

    public void syncFile(String file) throws Exception {
        File f = new File(baseDir.getAbsolutePath() + File.separator + file);
        if (!f.exists() || f.isDirectory()) {
            throw new Exception("File " + f.toString() + " doesn't exist");
        }
        System.out.print("Sychronizing " + file + ": ");
        long timeHere = f.lastModified();
        String destFile = destDir + "/" + file;
        String destDir = "";
        try {
            destDir = destFile.substring(0, destFile.lastIndexOf("/"));
        } catch (Exception e) { /* no "/" in filename */ }

        try {
            SFTPv3FileAttributes attr = sftp.stat(destFile);
            long timeThere = ((long)attr.mtime) * 1000;
            if (timeHere > timeThere) {
                System.out.print(" outdated. Replacing...");
                sftp.rm(destFile);
                scp.put(f.getAbsolutePath(), destDir, "0777");
                System.out.println(" success.");
            } else {
                System.out.println(" up to date. Keeping.");
                return;
            }
        } catch (Exception e) {
            // file does not exist on server
            System.out.print(" non-existent. Creating...");
            if (!dirExists(destDir)) {
                System.out.print("(");
                createDir(destDir);
                System.out.print(")");
            }
            scp.put(f.getAbsolutePath(), destDir, "0777");
            System.out.println(" success.");
        }

        // set file modified time to local modified time
        SFTPv3FileAttributes attr = sftp.stat(destFile);
        attr.mtime = (int)(timeHere / 1000);
        sftp.setstat(destFile, attr);
    }

    private boolean dirExists(String f) {
        try {
            SFTPv3FileAttributes attr = sftp.stat(f);
            return attr.isDirectory();
        } catch (Exception e) {
            return false;
        }
    }

    public void createDir(String destDir) throws Exception {
        try {
            sftp.mkdir(destDir, 0777);
            System.out.print("Created directory " + destDir + ".");
        } catch (Exception e) {
            createDir(destDir.substring(0, destDir.lastIndexOf("/")));
            sftp.mkdir(destDir, 0777);
            System.out.print("Created directory " + destDir + ".");
        }
    }

    public void syncDir(String dir) throws Exception {
        System.out.println("Sychronizing " + dir + ":");
        List<File> files = getAllFiles(new File(baseDir.getAbsolutePath() + File.separator + dir), this, false, false);
        for (File f : files) {
            syncFile(f.getAbsolutePath().substring(baseDir.getAbsolutePath().length() + File.separator.length()));
        }
    }

    public void close() {
        sftp.close();
        sshConnection.close();
    }

    /**
     * Gets all files in a directory and all of it's subdirectories
     * that are accepted by filter.
     *
     * @param path Path to get all files from
     * @param filter Filter that files have to pass to be accepted
     * @param includeDirs Include directories in returned list ?
     * @return Returns List with File objects
     */
    public static List<File> getAllFiles(File path, FilenameFilter filter, boolean includeDirs, boolean processHiddenFiles) {
        ArrayList<File> files = new ArrayList<File>();
        if (!path.isDirectory()) {
            return files;
        }
        for (File f : path.listFiles(filter)) {
            if (!processHiddenFiles && (f.getName().startsWith("."))) {
                continue;
            }
            if (f.isDirectory()) {
                if (includeDirs) {
                    files.add(f);
                }
                files.addAll(getAllFiles(f, filter, includeDirs, processHiddenFiles));
            } else {
                files.add(f);
            }
        }
        return files;
    }

    public boolean accept(File arg0, String arg1) {
        return true;
    }

    // only for debugging
    public static void main(String[] args) {
        try {
            org.finroc.tools.gui.util.SSHResourceSynchronizer syncher = new org.finroc.tools.gui.util.SSHResourceSynchronizer("x", "y");
            //syncher.syncFile("dist/jmcagui.jar");
            syncher.syncDir("lib");
            syncher.close();
            //print("Synchronizing finished.");
        } catch (Exception e) {
            e.printStackTrace();
            //print("Synchronizing failed.");
        }
    }

    public String[] replyToChallenge(String arg0, String arg1, int arg2, String[] arg3, boolean[] arg4) throws Exception {
        return arg2 > 0 ? new String[] {pwd} : new String[] {};
    }
}
