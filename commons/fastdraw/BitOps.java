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
package org.finroc.tools.gui.commons.fastdraw;

public class BitOps {

    public static long unpack3(int i) {
        long l = i;
        return ((l & 0xFF0000) << 16) | ((l & 0xFF00) << 8) | l & 0xFF;
    }

    public static int merge(int b, int c, int d) {
        return ((cleanByte(b) << 16) | (cleanByte(c) << 8) | cleanByte(d));
    }

    public static int merge(int i, int j, int k, int l) {
        return ((cleanByte(i) << 24) | (cleanByte(j) << 16) | (cleanByte(k) << 8) | cleanByte(l));
    }

    public static long unpack3b(int b) {
        long l = b;
        return (l << 32) | (l << 16) | b;
    }

    public static long restore(long l) {
        long result = l & 0x0000FF00FF00FF00L;
        return result >> 8;
    }

    public static int pack(long l) {
        return (int)(((l & 0xFF00000000L) >> 16) | ((l & 0xFF0000) >> 8) | (l & 0xFF));
    }

    public static int cleanByte(int b) {
        return b >= 0 ? b : b + 256;
    }

    public static int getByte(int i, int index) {
        switch (index) {
        case 0:
            return cleanByte(i >> 24);
        case 1:
            return (i >> 16) & 0xFF;
        case 2:
            return (i >> 8) & 0xFF;
        case 3:
            return i & 0xFF;
        }
        return 0;
    }

    /**
     * Inverse to merge
     *
     * @param i
     * @return
     */
    public static byte[] intToBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i & 0xFF000000) >> 24);
        result[1] = (byte)((i & 0xFF0000) >> 16);
        result[2] = (byte)((i & 0xFF00) >> 8);
        result[3] = (byte)(i & 0xFF);
        return result;
    }
}
