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
package org.finroc.tools.gui.util;

import org.finroc.tools.gui.FinrocGUI;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * Utility class for getting frames per second
 */
public class FPSDetector {

    static long lastOutput;
    static int frameCount = 0;
    static long maxTime = 0;
    static long time1;

    public static void tick() {

        if (lastOutput == 0) { // init ?
            lastOutput = System.currentTimeMillis();
            time1 = System.currentTimeMillis();
        }

        maxTime = Math.max(maxTime, System.currentTimeMillis() - time1);
        time1 = System.currentTimeMillis();
        //w.setChanged();
        frameCount++;
        //System.out.println(time1 - System.currentTimeMillis());
        long diff = System.currentTimeMillis() - lastOutput;
        if (diff > 1000) {
            FinrocGUI.logDomain.log(LogLevel.USER, "FPSDetector", ((frameCount * 1000) / diff) + " fps   " + maxTime);
            frameCount = 0;
            lastOutput = System.currentTimeMillis();
            maxTime = 0;
        }
    }
}
