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
package org.finroc.tools.gui.widgets;

import java.awt.BorderLayout;

import org.finroc.tools.gui.FinrocGUI;
import org.finroc.tools.gui.GUIUiBase;
import org.finroc.tools.gui.Widget;
import org.finroc.tools.gui.WidgetPort;
import org.finroc.tools.gui.WidgetUI;
import org.rrlib.finroc_core_utils.log.LogLevel;

import org.finroc.core.port.PortCreationInfo;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.util.JConsole;


public class BeanShell extends Widget {

    /** UID */
    private static final long serialVersionUID = 5572282903982373246L;

    @Override
    protected WidgetUI createWidgetUI() {
        return new BeanShellUI();
    }

    @Override
    protected PortCreationInfo getPortCreationInfo(PortCreationInfo suggestion, WidgetPort<?> forPort) {
        return null;
    }

    class BeanShellUI extends WidgetUI {

        /** UID */
        private static final long serialVersionUID = 1027858005093725792L;

        public BeanShellUI() {
            super(RenderMode.Swing);
            setLayout(new BorderLayout());
            JConsole console = new JConsole();
            add(console, BorderLayout.CENTER);
            Interpreter ip = new Interpreter(console);

            GUIUiBase <? , ? > instance = BeanShell.this.getRoot().getFingui();
            try {
                ip.set("mcagui", instance);
                ip.eval("public void wait(long ms) { Integer dummy = new Integer(5); synchronized(dummy) { dummy.wait(ms); }}");
                ip.eval("public org.finroc.core.port.AbstractPort getInput(String s) { return mcagui.getInput(s).getPort(); }");
                ip.eval("public org.finroc.core.port.AbstractPort getOutput(String s) { return mcagui.getOutput(s).getPort(); }");
                ip.eval("public void main() { System.out.println(\"main not defined yet\"); }");
                ip.eval("public class XYZ extends org.finroc.jc.thread.LoopThread { " +
                        "    public static XYZ instance;" +
                        "    public XYZ(long ms) {" +
                        "        super(ms, false);" +
                        "        start();" +
                        "    }" +
                        "    public void mainLoopCallback() {" +
                        "        main(); " +
                        "    } " +
                        "}");
                ip.eval("public void stop() { " +
                        "   if (XYZ.instance != null) {" +
                        "      XYZ.instance.stopLoop();" +
                        "      XYZ.instance = null;" +
                        "    }" +
                        "}");
                ip.eval("public void start(long ms) { " +
                        "   stop();" +
                        "   XYZ.instance = new XYZ(ms); " +
                        "}");
            } catch (EvalError e) {
                FinrocGUI.logDomain.log(LogLevel.LL_ERROR, toString(), e);
            }
            new Thread(ip).start();
        }

        @Override
        protected boolean isWidgetFocusable() {
            return true;
        }
    }

}
