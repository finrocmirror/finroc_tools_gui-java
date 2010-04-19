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
package org.finroc.gui.commons;

/**
 * @author max
 *
 * A Thread that loop with a specified interval
 */
public abstract class LoopThread extends Thread {

    /** Signals for state change */
    private boolean stopSignal = false, pauseSignal = false;

    /** Loop Time */
    private long loopTime;

    /** Display warning, if loop times is exceeded? */
    private boolean warnOnLoopTimeExceed;

    /** Display warnings on console? */
    private static final boolean DISPLAYWARNINGS = false;

    public LoopThread(long defaultLoopTime, boolean warnOnLoopTimeExceed) {
        loopTime = defaultLoopTime;
        this.warnOnLoopTimeExceed = warnOnLoopTimeExceed;
        setName(getClass().getSimpleName() + " MainLoop");
    }

    public void run() {
        try {

            stopSignal = false;
            pauseSignal = false;

            // Start main loop
            mainLoop();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mainLoop() throws Exception {

        while (!stopSignal) {

            // remember start time
            long startTimeMs = System.currentTimeMillis();

            if (!pauseSignal) {
                mainLoopCallback();
            }

            // wait
            long waitFor = loopTime - (System.currentTimeMillis() - startTimeMs);
            if (waitFor < 0 && warnOnLoopTimeExceed && DISPLAYWARNINGS) {
                System.err.println("warning: Couldn't keep up loop time (" + (-waitFor) + " ms too long)");
            } else if (waitFor > 0) {
                Thread.sleep(waitFor);
            }
        }
    }

    public abstract void mainLoopCallback() throws Exception;

    public long getLoopTime() {
        return loopTime;
    }

    public void setLoopTime(long loopTime) {
        this.loopTime = loopTime;
    }

    public boolean isRunning() {
        return isAlive();
    }

    public void stopLoop() {
        stopSignal = true;
    }

    public void pauseLoop() {
        pauseSignal = true;
    }

    public void continueLoop() {
        pauseSignal = false;
    }

    public boolean isPausing() {
        return pauseSignal;
    }

    public boolean isStopSignalSet() {
        return stopSignal;
    }
}
