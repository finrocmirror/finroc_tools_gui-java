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

import java.awt.Dimension;

import javax.swing.JFrame;

import org.rrlib.finroc_core_utils.log.LogLevel;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.util.JConsole;

public class DebugConsole extends JFrame {

    /** UID */
    private static final long serialVersionUID = 13457365983L;

    public DebugConsole(GUIWindowUI windowUI) {
        super("Debug Console");
        setPreferredSize(new Dimension(640, 480));
        JConsole console = new JConsole();
        add(console);
        pack();
        setVisible(true);
        Interpreter ip = new Interpreter(console);
        GUIUiBase <? , ? > instance = windowUI.getModel().getParent().getFingui();
        //ip.setConsole(console);
        try {
            ip.set("fingui", instance);
            ip.eval("import org.finroc.tools.gui.*");
            ip.eval("import org.finroc.core.*");
            ip.eval("setAccessibility(true);");
            ip.eval("mcapart = fingui.getActiveInterfaces().get(0)");
        } catch (EvalError e) {
            FinrocGUI.logDomain.log(LogLevel.LL_ERROR, "DebugConsole", e);
        }
        new Thread(ip).start();
    }
}
