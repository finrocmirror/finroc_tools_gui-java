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
package org.finroc.gui.util;

import org.finroc.gui.FinrocGUI;
import org.finroc.log.LogLevel;

/**
 * @author max
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
            FinrocGUI.logDomain.log(LogLevel.LL_USER, "FPSDetector", ((frameCount * 1000) / diff) + " fps   " + maxTime);
            frameCount = 0;
            lastOutput = System.currentTimeMillis();
            maxTime = 0;
        }
    }
}
