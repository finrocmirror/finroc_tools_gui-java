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
package org.finroc.gui;

import org.finroc.gui.commons.EventRouter;

import org.finroc.core.datatype.CoreNumber;
import org.finroc.plugin.blackboard.BlackboardClient;
import org.finroc.plugin.blackboard.RawBlackboardClient;
import org.finroc.serialization.PortDataList;
import org.finroc.serialization.RRLibSerializable;
import org.finroc.core.port.Port;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.PortListener;
import org.finroc.core.port.ThreadLocalCache;
import org.finroc.core.port.cc.PortNumeric;
import org.finroc.core.port.std.PortDataManager;

/**
 * @author max
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
    }

    @SuppressWarnings("rawtypes")
    public static class Numeric extends WidgetOutputPort<PortNumeric<Double>> {

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
            asPort().publish(val);
        }

        public void publish(double val) {
            asPort().publish(val);
        }

        public int getInt() {
            return asPort().getIntRaw();
        }

        public double getDouble() {
            return asPort().getDoubleRaw();
        }

        public CoreNumber getAutoLocked() {
            return (CoreNumber)asPort().getAutoLocked();
        }
    }

    public static class Blackboard<T> extends WidgetOutputPort<RawBlackboardClient.WritePort> {

        /** UID */
        private static final long serialVersionUID = 2712886077657464267L;

        private transient BlackboardClient<T> c;

        @Override
        protected RawBlackboardClient.WritePort createPort() {
            PortCreationInfo def = RawBlackboardClient.getDefaultPci().derive(getDescription());
            PortCreationInfo pci = getParent().getPortCreationInfo(def, this);
            c = new BlackboardClient<T>(pci.toString(), null, pci.getFlag(PortFlags.PUSH_STRATEGY), pci.dataType);
            //c = new BlackboardClient<T>(pci == null ? def : pci, false, -1);
            return c.getWrapped().getWritePort();
        }

        public void addChangeListener(PortListener<T> listener) {
            EventRouter.addListener(getClient().getWrapped().getReadPort(), "addPortListenerRaw", listener);
        }

        public BlackboardClient<T> getClient() {
            return c;
        }

        public PortDataList<T> readAutoLocked() {
            PortDataList<T> result = c.read();
            ThreadLocalCache.get().addAutoLock(PortDataManager.getManager(result));
            return result;
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
