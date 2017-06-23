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
package org.finroc.tools.gui;

import java.util.ArrayList;

import org.finroc.tools.gui.commons.EventRouter;

import org.finroc.core.datatype.CoreNumber;
import org.rrlib.serialization.BinarySerializable;
import org.rrlib.serialization.Serialization;
import org.rrlib.serialization.StringInputStream;
import org.rrlib.serialization.StringSerializable;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.Port;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;
import org.finroc.core.port.ThreadLocalCache;
import org.finroc.core.port.cc.PortNumeric;
import org.finroc.core.port.std.PortBase;
import org.finroc.core.port.std.PortDataManager;

/**
 * @author Max Reichardt
 *
 * Classes that can be used for private attributes that are automatically recognized
 * as widget output ports.
 */
public class WidgetOutput {

    public static class Std<T extends BinarySerializable> extends WidgetOutputPort<Port<T>> {

        /** UID */
        private static final long serialVersionUID = 6646515051948353004L;

        @Override
        protected Port<T> createPort() {
            return new Port<T>(getPci());
        }

        public void addChangeListener(PortListener<T> listener) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", listener);
        }

        public T getUnusedBuffer() {
            return asPort().getUnusedBuffer();
        }

        public void publish(T buffer) {
            asPort().publish(buffer);
        }
    }

    /**
     * Deprecated: Don't use this in new widgets
     */
    public static class CC<T extends BinarySerializable> extends WidgetOutputPort<Port<T>> {

        /** UID */
        private static final long serialVersionUID = -6522086680079332096L;

        @Override
        protected Port<T> createPort() {
            return new Port<T>(getPci());
        }

        public void addChangeListener(PortListener<T> listener) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", listener);
        }

        public void publish(T t) {
            asPort().publish(t);
        }
    }

    @SuppressWarnings("rawtypes")
    public static class Numeric extends WidgetOutputPort<Port<CoreNumber>> {

        /** UID */
        private static final long serialVersionUID = 8765896513368994897L;

        @Override
        protected PortNumeric createPort() {
            return new PortNumeric(getPci());
        }

        public void addChangeListener(PortListener<CoreNumber> listener) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", listener);
        }

        public void publish(int val) {
            CoreNumber cn = asPort().getUnusedBuffer();
            cn.setValue(val);
            asPort().publish(cn);
        }

        public void publish(double val) {
            CoreNumber cn = asPort().getUnusedBuffer();
            cn.setValue(val);
            asPort().publish(cn);
        }

        public int getInt() {
            int result = asPort().getAutoLocked().intValue();
            ThreadLocalCache.getFast().releaseAllLocks();
            return result;
        }

        public double getDouble() {
            double result = asPort().getAutoLocked().doubleValue();
            ThreadLocalCache.getFast().releaseAllLocks();
            return result;
        }

        public CoreNumber getAutoLocked() {
            return (CoreNumber)asPort().getAutoLocked();
        }
    }

    public static class Blackboard<T> { /*extends WidgetOutputPort<RawBlackboardClient.WritePort>*/

//        /** UID */
//        private static final long serialVersionUID = 2712886077657464267L;
//
//        private transient BlackboardClient<T> c;
//
//        @Override
//        protected RawBlackboardClient.WritePort createPort() {
//            PortCreationInfo def = RawBlackboardClient.getDefaultPci().derive(getDescription());
//            PortCreationInfo pci = getParent().getPortCreationInfo(def, this);
//            c = new BlackboardClient<T>(pci.toString(), null, pci.getFlag(FrameworkElementFlags.PUSH_STRATEGY), pci.dataType);
//            //c = new BlackboardClient<T>(pci == null ? def : pci, false, -1);
//            return c.getWrapped().getWritePort();
//        }
//
//        public void addChangeListener(PortListener<T> listener) {
//            EventRouter.addListener(getClient().getWrapped().getReadPort(), "addPortListenerRaw", listener);
//        }
//
//        public BlackboardClient<T> getClient() {
//            return c;
//        }
//
//        public PortDataList<T> readAutoLocked() {
//            PortDataList<T> result = c.read();
//            ThreadLocalCache.get().addAutoLock(PortDataManager.getManager(result));
//            return result;
//        }
    }

    /** Custom string serialiazable output */
    @SuppressWarnings("rawtypes")
    public static class Custom extends WidgetOutputPort<Port<BinarySerializable>> {

        /** UID */
        private static final long serialVersionUID = -3991768387448158703L;

        @Override
        protected Port createPort() {
            return Port.wrap(new CustomPort(getPci()));
        }

        @SuppressWarnings("unchecked")
        public synchronized void publishFromString(String s) throws Exception {
            ArrayList<AbstractPort> result = new ArrayList<>();
            getPort().getConnectionPartners(result, true, false, false);

            // prepare buffers
            StringSerializable[] buffers = new StringSerializable[result.size()];
            try {
                for (int i = 0; i < result.size(); i++) {
                    Port destinationPort = Port.wrap(result.get(i));
                    buffers[i] = (StringSerializable)destinationPort.getUnusedBuffer();
                    Serialization.deepCopy(destinationPort.getAutoLocked(), buffers[i]);
                    StringInputStream stream = new StringInputStream(s);
                    buffers[i].deserialize(stream);
                }
            } catch (Exception e) {
                for (StringSerializable buffer : buffers) {
                    if (buffer != null) {
                        ((PortDataManager)PortDataManager.getManager(buffer)).recycleUnused();
                    }
                }
                getPort().releaseAutoLocks();
                throw e;
            }
            getPort().releaseAutoLocks();

            // disconnect all ports but the first
            for (int i = 1; i < result.size(); i++) {
                getPort().disconnectFrom(result.get(i));
            }

            // now publish to each port
            Port thisPort = Port.wrap(getPort());
            for (int i = 0; i < result.size(); i++) {

                if (i > 0) {
                    getPort().connectTo(result.get(i)); // connecting first should prevent initial pushing
                    getPort().disconnectFrom(result.get(i - 1));
                }

                thisPort.publish((BinarySerializable)buffers[i]);
            }

            // re-establish connections to all ports
            for (int i = 0; i < result.size(); i++) {
                getPort().connectTo(result.get(i));
            }
        }

        public StringSerializable getAutoLocked() {
            return (StringSerializable)asPort().getAutoLocked();
        }

        public synchronized void addChangeListener(PortListener l) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", l);
        }
    }


    private static class CustomPort extends PortBase {

        @Override
        public boolean mayConnectTo(AbstractPort target, boolean warnIfImpossible) {
            if (StringSerializable.class.isAssignableFrom(target.getDataType().getJavaClass())) {
                return true;
            }
            return super.mayConnectTo(target, warnIfImpossible);
        }

        public CustomPort(PortCreationInfo pci) {
            super(adjustPci(pci));
        }

        private static PortCreationInfo adjustPci(PortCreationInfo pci) {
            pci.dataType = StringSerializable.TYPE;
            return pci;
        }
    }



//  public static class Strings extends WidgetOutputPort<ContainsStrings> {
//      /** UID*/
//      private static final long serialVersionUID = 3572458245634667L;
//
//      public Strings(String description) {
//          super(ContainsStrings.class, new StringList(), description);
//      }
//  }
//
//  public static class BehaviourInfo extends WidgetOutputPort<org.mca.commons.datatype.BehaviourInfo.List> {
//      public BehaviourInfo(String description) {
//          super(org.mca.commons.datatype.BehaviourInfo.List.class, null, description);
//      }
//
//      /** UID */
//      private static final long serialVersionUID = -1862270888947923567L;
//  }
//
//  public static class Number extends WidgetOutputPort<java.lang.Number> {
//      /** UID*/
//      private static final long serialVersionUID = -7133853211750656509L;
//
//      public Number(String description) {
//          super(java.lang.Number.class, 0, description);
//      }
//
//      public java.lang.Number getValue() {
//          java.lang.Number n = super.getValue();
//          if (n == null) {
//              return 0;
//          } else {
//              return n;
//          }
//      }
//  }
}
