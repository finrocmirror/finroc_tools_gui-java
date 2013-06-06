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
package org.finroc.tools.gui.commons;

/**
 * @author Max Reichardt
 *
 * this class is used to convert numbers to correct endian mode
 */
public class Endian {

    /** is data transferred in big endian format ? */
    public final static boolean MCA_BIG_ENDIAN = false;

    @SuppressWarnings("unused")
    public static int correct(int i) {
        if (!MCA_BIG_ENDIAN) {
            return i;
        }
        return Integer.reverseBytes(i);
    }

    @SuppressWarnings("unused")
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
