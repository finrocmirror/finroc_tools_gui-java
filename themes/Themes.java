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
package org.finroc.tools.gui.themes;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.UIManager;

import org.finroc.tools.gui.util.PackageContentEnumerator;

public class Themes {

    private static List<Theme> themes = initThemes();

    // temporary
    private static Theme SHINY = new BrushedMetalBlue();
    private static Theme CLASSIC = new Default();
    private static Boolean NIMBUS;

    public static Theme getCurTheme() {
        return nimbusLookAndFeel() ? SHINY : CLASSIC;

        /*for (Theme t : themes) {
            if (t instanceof Default) {
                return t;
            }
        }
        return null;*/
    }

    /**
     * @return Are we using Nimbus Look And Feel instead of default
     */
    public static boolean nimbusLookAndFeel() {
        if (NIMBUS == null) {
            NIMBUS = UIManager.getLookAndFeel().getName().equals("Nimbus");
        }
        return NIMBUS.booleanValue();
    }

    private static List<Theme> initThemes() {
        List<Theme> result = new ArrayList<Theme>();
        try {
            for (String s : new PackageContentEnumerator(new Themes(), "")) {
                if (s.endsWith(".class")) {
                    String name = s.substring(0, s.lastIndexOf("."));
                    Class<?> c = Themes.class.getClassLoader().loadClass(Themes.class.getPackage().getName() + "." + name);
                    if (Modifier.isAbstract(c.getModifiers())) {
                        continue;
                    }
                    if (!(Theme.class.isAssignableFrom(c))) {
                        continue;
                    }
                    result.add((Theme)c.newInstance());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
