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
package org.finroc.tools.gui.commons.fastdraw;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.rrlib.serialization.ArrayBuffer;

/**
 * Converts mono channel (image) to color image by using mono channel for interpolation
 * between an arbitrary number of colors.
 * This is commonly used for heat map visualization of mono channel.
 */
public class HeatMapColorTransformation {

    /**
     * Calls set with values/colors equally distributed between minValue and maxValue
     *
     * @param minValue Value for first color
     * @param maxValue Value for last color
     * @param colors Colors
     */
    public void set(double minValue, double maxValue, Color... colors) {
        if (values != null && values[0] == minValue && values[values.length - 1] == maxValue && this.colors != null && Arrays.equals(colors, this.colors)) {
            return;
        }
        double values[] = new double[colors.length];
        for (int i = 0; i < colors.length; i++) {
            values[i] = minValue + i * ((maxValue - minValue) / (colors.length - 1));
        }
        set(values, colors);
    }

    /**
     * Calls set with values/colors equally distributed between minValue and maxValue
     *
     * @param minValue Value for first color
     * @param maxValue Value for last color
     * @param colors Colors
     * @param colors colorCount The number of colors to use from array
     * @return Whether any values have been changed
     */
    public void set(double minValue, double maxValue, Color[] colors, int colorCount) {
        if (values != null && values[0] == minValue && values[values.length - 1] == maxValue && this.colors != null && this.colors.length == colorCount) {
            boolean colorsEqual = true;
            for (int i = 0; i < colorCount; i++) {
                colorsEqual &= colors[i].equals(this.colors[i]);
            }
            if (colorsEqual) {
                return;
            }
        }
        double values[] = new double[colorCount];
        for (int i = 0; i < colorCount; i++) {
            values[i] = minValue + i * ((maxValue - minValue) / (colorCount - 1));
        }
        Color colorsCopy[] = new Color[colorCount];
        System.arraycopy(colors, 0, colorsCopy, 0, colorCount);
        set(values, colorsCopy);
    }

    /**
     * Sets which color should be assigned to which value (in ascending order)
     * In between values, linear color interpolation is used.
     * Values below and above are assigned the min and max color.
     *
     * @param values Values
     * @param colors Colors assigned to values with same index
     */
    private void set(double[] values, Color[] colors) {
        if (values.length != colors.length) {
            throw new RuntimeException("Arrays need the same size");
        }
        cachedColorLookupUint8 = null;
        cachedColorLookupUint16 = null;
        cachedColorLookupFloat = null;
        this.values = values;
        this.colors = colors;
    }

    /**
     * Transforms selected array buffer channel to RGB data in destination buffer
     *
     * @param destination Destination buffer
     * @param source Source buffer
     * @param channel Selected channel in source buffer
     */
    public void transform(byte[] destination, ArrayBuffer source, ArrayBuffer.Channel channel) {
        int[] dimensions = source.getArrayDimensions();
        int entries = 1;
        for (int i = 0; i < dimensions.length; i++) {
            entries *= dimensions[i];
        }
        int currentOffset = channel.getOffset();
        int stride = channel.getStride();
        ByteBuffer buffer = source.getByteBuffer();

        if (channel.getDataType() == ArrayBuffer.AttributeType.UNSIGNED_BYTE) {
            if (cachedColorLookupUint8 == null) {
                int values[] = new int[this.values.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = Math.max(0, Math.min(255, (int)this.values[i]));
                }
                byte[] newLookup = new byte[256 * 4];
                newLookup[0] = (byte)colors[0].getRed();
                newLookup[1] = (byte)colors[0].getGreen();
                newLookup[2] = (byte)colors[0].getBlue();
                for (int i = 1; i < values[0]; i++) {
                    System.arraycopy(newLookup, 0, newLookup, i * 4, 3);
                }
                for (int color = 0; color < colors.length - 1; color++) {
                    Color c1 = colors[color];
                    Color c2 = colors[color + 1];
                    int value1 = values[color];
                    int value2 = values[color + 1];

                    for (int i = value1; i < value2; i++) {
                        double a = (i - value1) / ((double)(value2 - value1));
                        newLookup[i * 4] = (byte)((1 - a) * c1.getRed() + a * c2.getRed());
                        newLookup[i * 4 + 1] = (byte)((1 - a) * c1.getGreen() + a * c2.getGreen());
                        newLookup[i * 4 + 2] = (byte)((1 - a) * c1.getBlue() + a * c2.getBlue());
                    }
                }
                int lastValue = values[values.length - 1];
                Color lastColor = colors[colors.length - 1];
                for (int i = lastValue; i < 256; i++) {
                    newLookup[i * 4] = (byte)lastColor.getRed();
                    newLookup[i * 4 + 1] = (byte)lastColor.getGreen();
                    newLookup[i * 4 + 2] = (byte)lastColor.getBlue();
                }
                cachedColorLookupUint8 = newLookup;
            }

            int destinationIndex = 0;
            for (int i = 0; i < entries; i++) {
                int index = 4 * (buffer.get(currentOffset) & 0xFF);
                destination[destinationIndex] = cachedColorLookupUint8[index];
                destination[destinationIndex + 1] = cachedColorLookupUint8[index + 1];
                destination[destinationIndex + 2] = cachedColorLookupUint8[index + 2];
                destinationIndex += 3;
                currentOffset += stride;
            }
        } else if (channel.getDataType() == ArrayBuffer.AttributeType.UNSIGNED_SHORT) {
            int lastValue = Math.max(0, Math.min(0xFFFF, (int)this.values[values.length - 1]));
            if (cachedColorLookupUint16 == null) {
                int values[] = new int[this.values.length];
                for (int i = 0; i < values.length; i++) {
                    values[i] = Math.max(0, Math.min(0xFFFF, (int)this.values[i]));
                }

                byte[] newLookup = new byte[(lastValue + 1) * 4];
                newLookup[0] = (byte)colors[0].getRed();
                newLookup[1] = (byte)colors[0].getGreen();
                newLookup[2] = (byte)colors[0].getBlue();
                for (int i = 1; i < values[0]; i++) {
                    System.arraycopy(newLookup, 0, newLookup, i * 4, 3);
                }
                for (int color = 0; color < colors.length - 1; color++) {
                    Color c1 = colors[color];
                    Color c2 = colors[color + 1];
                    int value1 = values[color];
                    int value2 = values[color + 1];

                    for (int i = value1; i < value2; i++) {
                        double a = (i - value1) / ((double)(value2 - value1));
                        newLookup[i * 4] = (byte)((1 - a) * c1.getRed() + a * c2.getRed());
                        newLookup[i * 4 + 1] = (byte)((1 - a) * c1.getGreen() + a * c2.getGreen());
                        newLookup[i * 4 + 2] = (byte)((1 - a) * c1.getBlue() + a * c2.getBlue());
                    }
                }
                Color lastColor = colors[colors.length - 1];
                newLookup[lastValue * 4] = (byte)lastColor.getRed();
                newLookup[lastValue * 4 + 1] = (byte)lastColor.getGreen();
                newLookup[lastValue * 4 + 2] = (byte)lastColor.getBlue();
                cachedColorLookupUint16 = newLookup;
            }

            int destinationIndex = 0;
            for (int i = 0; i < entries; i++) {
                int index = 4 * Math.min(lastValue, buffer.getShort(currentOffset) & 0xFFFF);
                destination[destinationIndex] = cachedColorLookupUint16[index];
                destination[destinationIndex + 1] = cachedColorLookupUint16[index + 1];
                destination[destinationIndex + 2] = cachedColorLookupUint16[index + 2];
                destinationIndex += 3;
                currentOffset += stride;
            }
        } else if (channel.getDataType() == ArrayBuffer.AttributeType.FLOAT || channel.getDataType() == ArrayBuffer.AttributeType.DOUBLE) {
            double firstValue = this.values[0];
            double lastValue = this.values[values.length - 1];
            double colorStep = (lastValue - firstValue) / (256 * (this.values.length - 1));

            if (cachedColorLookupFloat == null) {
                byte[] newLookup = new byte[((colors.length - 1) * 256 + 1) * 4];
                for (int color = 0; color < colors.length - 1; color++) {
                    Color c1 = colors[color];
                    Color c2 = colors[color + 1];
                    int offset = color * 256;
                    for (int i = 0; i < 256; i++) {
                        double a = i / 255.;
                        newLookup[(offset + i) * 4] = (byte)((1 - a) * c1.getRed() + a * c2.getRed());
                        newLookup[(offset + i) * 4 + 1] = (byte)((1 - a) * c1.getGreen() + a * c2.getGreen());
                        newLookup[(offset + i) * 4 + 2] = (byte)((1 - a) * c1.getBlue() + a * c2.getBlue());
                    }
                }
                Color lastColor = colors[colors.length - 1];
                int lastEntry = newLookup.length - 4;
                newLookup[lastEntry] = (byte)lastColor.getRed();
                newLookup[lastEntry + 1] = (byte)lastColor.getGreen();
                newLookup[lastEntry + 2] = (byte)lastColor.getBlue();
                cachedColorLookupFloat = newLookup;
            }

            int destinationIndex = 0;
            int maxIndex = (cachedColorLookupFloat.length / 4) - 1;
            if (channel.getDataType() == ArrayBuffer.AttributeType.FLOAT) {
                for (int i = 0; i < entries; i++) {
                    int index = 4 * Math.max(0, Math.min(maxIndex, (int)((buffer.getFloat(currentOffset) - firstValue) / colorStep)));
                    destination[destinationIndex] = cachedColorLookupFloat[index];
                    destination[destinationIndex + 1] = cachedColorLookupFloat[index + 1];
                    destination[destinationIndex + 2] = cachedColorLookupFloat[index + 2];
                    destinationIndex += 3;
                    currentOffset += stride;
                }
            } else {
                for (int i = 0; i < entries; i++) {
                    int index = 4 * Math.max(0, Math.min(maxIndex, (int)((buffer.getDouble(currentOffset) - firstValue) / colorStep)));
                    destination[destinationIndex] = cachedColorLookupFloat[index];
                    destination[destinationIndex + 1] = cachedColorLookupFloat[index + 1];
                    destination[destinationIndex + 2] = cachedColorLookupFloat[index + 2];
                    destinationIndex += 3;
                    currentOffset += stride;
                }
            }
        }
    }

//    /**
//     * @param wrapped Array buffer with data to wrap as blittable
//     * @return Blittable wrapper that does color transformation while blitting
//     */
//    public Blittable getWrappedArrayBuffer(ArrayBuffer wrapped) {
//
//    }
//
//    /** Blittable wrapper that does color transformation while blitting */
//    public class BlittableWrapper extends Blittable {
//
//
//
//        /** Wrapped blittable */
//        private ArrayBuffer wrapped;
//    }

    /** Color lookups - created lazily */
    private byte[] cachedColorLookupUint8;
    private byte[] cachedColorLookupUint16;
    private byte[] cachedColorLookupFloat;

    private double[] values;
    private Color[] colors;
}
