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
package org.finroc.tools.gui.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * @author Max Reichardt
 *
 */
public class PackageContentEnumerator extends ArrayList<String> {

    /** UID */
    private static final long serialVersionUID = -4250220886730849533L;

    public PackageContentEnumerator(Object caller, String folder) throws Exception {
        URL folder2 = caller.getClass().getResource(folder);
        if (folder2 != null && !folder2.toString().startsWith("jar:")) {
            BufferedReader br = new BufferedReader(new InputStreamReader(folder2.openStream()));
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                this.add(s);
            }
            br.close();
        } else {
            // open jar
            String folder3 = caller.getClass().getPackage().getName().replace(".", "/") + "/" + folder;
            URL classurl = caller.getClass().getResource(caller.getClass().getSimpleName() + ".class");
            URL jar = ((JarURLConnection)classurl.openConnection()).getJarFileURL();
            JarInputStream jis = new JarInputStream(new BufferedInputStream(jar.openStream()));
            while (true) {
                JarEntry je = jis.getNextJarEntry();
                if (je == null) {
                    break;
                }
                if (je.getName().startsWith(folder3)) {
                    add(je.getName().substring(je.getName().lastIndexOf("/") + 1));
                }
            }
            jis.close();
        }
    }

    public static String getRootDir(Class<?> caller) {

        String dirName = caller.getResource(caller.getSimpleName() + ".class").toString();
        String packageName = caller.getName().replaceAll("[.]", "/");
        dirName = dirName.substring(0, dirName.indexOf(packageName));
        if (dirName.contains(".jar!")) {
            dirName = dirName.substring(0, dirName.indexOf(".jar!"));
            dirName = dirName.substring(dirName.indexOf("jar:/") + 5, dirName.lastIndexOf("/") + 1);
        }

        if (File.separator.equals("\\")) { // Windows
            dirName = dirName.substring(dirName.indexOf("file:/") + 6);
            dirName = dirName.replaceAll("/", "\\\\");
        } else { // Unix/Linux
            dirName = dirName.substring(dirName.indexOf("file:/") + 5);
        }

        return dirName;
    }
}
