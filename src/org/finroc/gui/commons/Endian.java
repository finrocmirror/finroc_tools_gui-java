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
 * this class is used to convert numbers to correct endian mode
 */
public class Endian {

    /** is data transferred in big endian format ? */
    public final static boolean MCA_BIG_ENDIAN = false;

    public static int correct(int i) {
        if (!MCA_BIG_ENDIAN) {
            return i;
        }
        return Integer.reverseBytes(i);
    }

    public static short correct(short i) {
        if (!MCA_BIG_ENDIAN) {
            return i;
        }
        return Short.reverseBytes(i);
    }

    public static int correctInv(int i) {
        if (MCA_BIG_ENDIAN) {
            return i;
        }
        return Integer.reverseBytes(i);
    }

    public static short correctInv(short i) {
        if (MCA_BIG_ENDIAN) {
            return i;
        }
        return Short.reverseBytes(i);
    }

    /*public static short turn(short i) {
        short result = (short)((i & 0xFF) << 8);
        result |= (i & 0xFF00) >> 8;
        return result;
    }

    public static int turn(int i) {
        int result = (i & 0xFF) << 24;
        result |= (i & 0xFF00) << 8;
        result |= (i & 0xFF0000) >> 8;
        result |= ((i & 0xFF000000) >> 24);
        return result;
    }*/

}
