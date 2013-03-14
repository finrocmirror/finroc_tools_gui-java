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
package org.finroc.tools.gui.util.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * @author Max Reichardt
 *
 * Singleton Icon Loader. So no icons are loaded multiple times
 */
public class IconManager {

    private static IconManager instance;
    private Map<String, Icon> lookup = new HashMap<String, Icon>();
    private List < Class<? >> caller = new ArrayList < Class<? >> ();
    private List<String> folder = new ArrayList<String>();

    public static IconManager getInstance() {
        if (instance == null) {
            instance = new IconManager();
        }
        return instance;
    }

    public void addResourceFolder(Class<?> caller, String folder) {
        this.caller.add(caller);
        this.folder.add(folder);
    }

    public Icon getIcon(String filename) {
        if (filename == null) {
            return null;
        }

        if (lookup.containsKey(filename)) {
            return lookup.get(filename);
        }
        for (int i = 0; i < caller.size(); i++) {
            try {
                ImageIcon ii = new ImageIcon(caller.get(i).getResource(folder.get(i) + "/" + filename));
                lookup.put(filename, ii);
                return ii;
            } catch (Exception e) {
                // icon doesn't exist
            }
        }

        lookup.put(filename, null);
        return null;
    }
}
