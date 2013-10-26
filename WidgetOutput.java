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

import org.finroc.tools.gui.commons.EventRouter;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.core.datatype.DataTypeReference;
import org.rrlib.finroc_core_utils.log.LogLevel;
import org.rrlib.finroc_core_utils.serialization.RRLibSerializable;
import org.rrlib.finroc_core_utils.serialization.StringInputStream;
import org.finroc.core.port.Port;
import org.finroc.core.port.PortListener;
import org.finroc.core.port.ThreadLocalCache;
import org.finroc.core.port.cc.CCPortDataManagerTL;
import org.finroc.core.port.cc.PortNumeric;
import org.finroc.core.port.std.PortDataManager;
import org.finroc.core.portdatabase.UnknownType;
import org.finroc.core.portdatabase.UnknownTypeListener;

/**
 * @author Max Reichardt
 *
 * Classes that can be used for private attributes that are automatically recognized
 * as widget output ports.
 */
public class WidgetOutput {

    public static class Std<T extends RRLibSerializable> extends WidgetOutputPort<Port<T>> {

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
    public static class CC<T extends RRLibSerializable> extends WidgetOutputPort<Port<T>> {

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

        /** UID */
        private static final long serialVersionUID = 2712886077657464267L;

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

    @SuppressWarnings("rawtypes")
    public static class Custom extends WidgetOutputPort<Port<RRLibSerializable>> implements UnknownTypeListener {

        /** UID */
        private static final long serialVersionUID = -3991768387448158703L;

        /** Port's data type (can be changed) */
        private DataTypeReference type = new DataTypeReference();

        @Override
        protected Port createPort() {
            if (type != null && type.get() == null) {
                UnknownType.addUnknownTypeListener(this);
            }
            return new Port(getPci().derive((type == null || type.get() == null) ? CoreNumber.TYPE : type.get()));
        }

        public void changeDataType(DataTypeReference type) {
            frameworkElement.managedDelete();
            frameworkElement = null;
            port = null;
            this.type = type;
            restore(getParent());
        }

        public synchronized void publishFromString(String s) {
            RRLibSerializable buffer = asPort().getUnusedBuffer();
            StringInputStream sis = new StringInputStream(s);
            try {
                buffer.deserialize(sis);
                asPort().publish(buffer);
                buffer = null;
            } catch (Exception ex) {
                if (asPort().hasCCType()) {
                    ((CCPortDataManagerTL)CCPortDataManagerTL.getManager(buffer)).recycleUnused();
                } else {
                    ((PortDataManager)PortDataManager.getManager(buffer)).recycleUnused();
                }
                logDomain.log(LogLevel.ERROR, getLogDescription(), "Cannot parse '" + s + "' for publishing (type " + asPort().getDataType().getName() + ").");
            }
        }

        /**
         * @return Port's data type
         */
        public DataTypeReference getType() {
            return type;
        }

        public void addChangeListener(PortListener l) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", l);
        }

        @Override
        public void unknownTypeAdded(UnknownType t) {
            if (t == type.get()) {
                changeDataType(type);
            }
        }

        @Override
        public void dispose() {
            super.dispose();
            UnknownType.removeUnknownTypeListener(this);
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
