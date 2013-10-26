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
package org.finroc.tools.gui.commons;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JOptionPane;


/**
 * @author Max Reichardt
 *
 */
public class Util {

    public static byte[] intToBytes(int i) {
        byte[] result = new byte[4];
        if (Endian.MCA_BIG_ENDIAN) {
            result[0] = (byte)((i & 0xFF000000) >> 24);
            result[1] = (byte)((i & 0xFF0000) >> 16);
            result[2] = (byte)((i & 0xFF00) >> 8);
            result[3] = (byte)(i & 0xFF);
        } else {
            result[3] = (byte)((i & 0xFF000000) >> 24);
            result[2] = (byte)((i & 0xFF0000) >> 16);
            result[1] = (byte)((i & 0xFF00) >> 8);
            result[0] = (byte)(i & 0xFF);
        }
        return result;
    }

    public static byte[] shortToBytes(short s) {
        byte[] result = new byte[2];
        if (Endian.MCA_BIG_ENDIAN) {
            result[0] = (byte)((s & 0xFF00) >> 8);
            result[1] = (byte)(s & 0xFF);
        } else {
            result[1] = (byte)((s & 0xFF00) >> 8);
            result[0] = (byte)(s & 0xFF);
        }
        return result;
    }

    public static String asWords(String name) {
        String result = "";
        boolean lastUpperCase = false;
        for (char c : name.toCharArray()) {
            if (result == "" && Character.isLetter(c)) {
                result += Character.toUpperCase(c);
                continue;
            }
            if (Character.isUpperCase(c)) {
                if (lastUpperCase) {
                    result += c;
                } else {
                    result += " " + c;
                }
                lastUpperCase = true;
            } else {
                result += c;
                lastUpperCase = false;
            }
        }
        return result;
    }

    public static InetAddress getLocalIPAdress() throws Exception {
        Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces();
        List<InetAddress> candidates = new ArrayList<InetAddress>();
        while (eni.hasMoreElements()) {
            NetworkInterface ni = eni.nextElement();
            Enumeration<InetAddress> ia = ni.getInetAddresses();
            while (ia.hasMoreElements()) {
                InetAddress ina = ia.nextElement();
                if (!candidates.contains(ina) && ina instanceof Inet4Address && ina.isReachable(200)) {
                    candidates.add(ina);
                }
            }
        }

        // Heuristik: die längste IP-Adresse ist die richtige (mir fällt gerade nichst besseres ein)
        /*InetAddress best = InetAddress.getByName("127.0.0.1");
        for (InetAddress ia : candidates) {
            if (ia.toString().length() >= best.toString().length()) {
                best = ia;
            }
        }
        return best;*/

        // Nutzer wählen lassen
        return (InetAddress)JOptionPane.showInputDialog(null, "Please select network interface", "IP address", JOptionPane.PLAIN_MESSAGE, null, candidates.toArray(new InetAddress[0]), candidates.get(0));
    }

    /**
     * Helper method that will time out when making a socket connection.
     * This is required because there is no way to provide a timeout value
     * when creating a socket and in some instances, they don't seem to
     * timeout at all.
     *
     * from: http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.team.cvs.core/src/org/eclipse/team/internal/ccvs/core/util/Util.java?revision=1.48&view=markup
     * slightly modified
     */
    public static Socket createSocket(final String host, final int port, int timeout) throws Exception {

        // Start a thread to open a socket
        final Socket[] socket = new Socket[] { null };
        final Exception[] exception = new Exception[] {null };
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    Socket newSocket = new Socket(host, port);
                    synchronized (socket) {
                        if (Thread.interrupted()) {
                            // we we're either cancelled or timed out so just close the socket
                            newSocket.close();
                        } else {
                            socket[0] = newSocket;
                        }
                    }
                } catch (UnknownHostException e) {
                    exception[0] = e;
                } catch (IOException e) {
                    exception[0] = e;
                }
            }
        });
        thread.start();

        // Wait the appropriate number of seconds
        for (int i = 0; i < timeout; i++) {
            try {
                // wait for the thread to complete or 1 second, which ever comes first
                thread.join(1000);
            } catch (InterruptedException e) {
                // I think this means the thread was interupted but not necessarily timed out
                // so we don't need to do anything
            }
            synchronized (socket) {
                // if the user cancelled, clean up before preempting the operation
            }
        }
        // If the thread is still running (i.e. we timed out) signal that it is too late
        synchronized (socket) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
        if (exception[0] != null) {
            if (exception[0] instanceof UnknownHostException)
                throw(UnknownHostException)exception[0];
            else
                throw(IOException)exception[0];
        }
        if (socket[0] == null) {
            throw new Exception("Create Socket timeout");
        }
        return socket[0];
    }

    /**
     * Get locacl IP address that can reach the specified address
     *
     * @param address Address that should be reachable
     * @return Returns suitable local address
     * @throws Exception If no local address could be found
     */
    public static InetAddress getLocalIPAdress(InetAddress address) throws Exception {
        Enumeration<NetworkInterface> eni = NetworkInterface.getNetworkInterfaces();
        while (eni.hasMoreElements()) {
            NetworkInterface ni = eni.nextElement();
            if (address.isReachable(ni, 0, 2000)) {
                Enumeration<InetAddress> eia = ni.getInetAddresses();
                while (eia.hasMoreElements()) {
                    InetAddress tmpadr = eia.nextElement();
                    if (tmpadr instanceof Inet4Address) {
                        if (!tmpadr.isLoopbackAddress()) {
                            return tmpadr;
                        }
                    }
                }
            }
        }
        throw new Exception("No network interface found, that can reach " + address.toString());
    }

    public static double toInterval(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int toInterval(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double posInInterval(double value, double min, double max) {
        return (value - min) / (max - min);
    }

    /**
     * from Java 6 SE by Sun Microsystems
     *
     * Sets this transform to the inverse of itself.
     * The inverse transform Tx' of this transform Tx
     * maps coordinates transformed by Tx back
     * to their original coordinates.
     * In other words, Tx'(Tx(p)) = p = Tx(Tx'(p)).
     * <p>
     * If this transform maps all coordinates onto a point or a line
     * then it will not have an inverse, since coordinates that do
     * not lie on the destination point or line will not have an inverse
     * mapping.
     * The <code>getDeterminant</code> method can be used to determine if this
     * transform has no inverse, in which case an exception will be
     * thrown if the <code>invert</code> method is called.
     * @see #getDeterminant
     * @exception NoninvertibleTransformException
     * if the matrix cannot be inverted.
     * @since 1.6
     */
    public static void invert(AffineTransform at) throws NoninvertibleTransformException {
        double[] matrix = new double[6];
        at.getMatrix(matrix);
        double m00 = matrix[0];
        double m10 = matrix[1];
        double m01 = matrix[2];
        double m11 = matrix[3];
        double m02 = matrix[4];
        double m12 = matrix[5];
        double M00, M01, M02;
        double M10, M11, M12;
        double det;
        // case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        M00 = m00;
        M01 = m01;
        M02 = m02;
        M10 = m10;
        M11 = m11;
        M12 = m12;
        det = M00 * M11 - M01 * M10;
        if (Math.abs(det) <= Double.MIN_VALUE) {
            throw new NoninvertibleTransformException("Determinant is " +
                    det);
        }
        m00 =  M11 / det;
        m10 = -M10 / det;
        m01 = -M01 / det;
        m11 =  M00 / det;
        m02 = (M01 * M12 - M11 * M02) / det;
        m12 = (M10 * M02 - M00 * M12) / det;

        at.setTransform(m00, m10, m01, m11, m02, m12);
    }

    /**
     * deletes a complete directory including subfolders and files
     *
     * @param pathname
     */
    public static void deltree(File dir) {
        if (!dir.exists()) {
            return;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                deltree(f.getAbsoluteFile());
            } else {
                f.delete();
            }
        }

        // delete folder itself
        dir.delete();
    }

    public static String readString(InputStream is) throws Exception {
        byte b = (byte)is.read();
        StringBuffer sb = new StringBuffer();
        while ((b != '\n') && (b != '\r')) {
            sb.append((char)b);
            b = (byte)is.read();
        }
        if (b == '\r') {
            is.read(); // skip \n
        }
        return sb.toString();
    }

    /**
     * Read content of stream and store it in byte array.
     *
     * @param is Stream
     * @return Byte Array
     */
    public static byte[] readStreamFully(InputStream is) throws Exception {
        return readStreamFully(is, true);
    }

    /**
     * Read content of stream and store it in byte array.
     *
     * @param is Stream
     * @param closeStream close Stream afterwards
     * @return Byte Array
     */
    public static byte[] readStreamFully(InputStream is, boolean closeStream) throws Exception {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(is, 10000);
        byte[] buffer = new byte[10000];
        while (true) {
            int read = bis.read(buffer);
            if (read < 0) { // stream finished
                break;
            }
            data.write(buffer, 0, read);
        }
        if (closeStream) {
            bis.close();
        }
        return data.toByteArray();
    }
}
