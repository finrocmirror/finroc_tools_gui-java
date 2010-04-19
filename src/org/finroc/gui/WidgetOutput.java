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

import org.finroc.core.FrameworkElement;
import org.finroc.core.datatype.CoreNumber;
import org.finroc.plugin.blackboard.BlackboardBuffer;
import org.finroc.plugin.blackboard.RawBlackboardClient;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.cc.CCPort;
import org.finroc.core.port.cc.CCPortData;
import org.finroc.core.port.cc.CCPortListener;
import org.finroc.core.port.cc.NumberPort;
import org.finroc.core.port.std.Port;
import org.finroc.core.port.std.PortData;
import org.finroc.core.port.std.PortListener;

/**
 * @author max
 *
 * Classes that can be used for private attributes that are automatically recognized
 * as widget output ports.
 */
public class WidgetOutput {

    public static class Std<T extends PortData> extends WidgetOutputPort<Port<T>> {

        /** UID */
        private static final long serialVersionUID = 6646515051948353004L;

        @Override
        protected FrameworkElement createFrameworkElement() {
            return new Port<T>(getPci());
        }

        public void addChangeListener(PortListener<T> listener) {
            EventRouter.addListener(getPort(), "addPortListener", listener);
        }

        public T getUnusedBuffer() {
            return getPort().getUnusedBuffer();
        }

        public void publish(T buffer) {
            getPort().publish(buffer);
        }
    }

    public static class CC<T extends CCPortData> extends WidgetOutputPort<CCPort<T>> {

        /** UID */
        private static final long serialVersionUID = -6522086680079332096L;

        @Override
        protected FrameworkElement createFrameworkElement() {
            return new CCPort<T>(getPci());
        }

        public void addChangeListener(CCPortListener<T> listener) {
            EventRouter.addListener(getPort(), "addPortListener", listener);
        }
    }

    public static class Numeric extends WidgetOutputPort<NumberPort> {

        /** UID */
        private static final long serialVersionUID = 8765896513368994897L;

        @Override
        protected FrameworkElement createFrameworkElement() {
            return new NumberPort(getPci());
        }

        public void addChangeListener(CCPortListener<CoreNumber> listener) {
            EventRouter.addListener(getPort(), "addPortListener", listener);
        }

        public void publish(int val) {
            getPort().publish(val);
        }

        public void publish(double val) {
            getPort().publish(val);
        }

        public int getInt() {
            return getPort().getIntRaw();
        }

        public double getDouble() {
            return getPort().getDoubleRaw();
        }

        public CoreNumber getAutoLocked() {
            return getPort().getAutoLocked();
        }
    }

    public static class Blackboard<T extends BlackboardBuffer> extends WidgetOutputPort<RawBlackboardClient.WritePort> {

        /** UID */
        private static final long serialVersionUID = 2712886077657464267L;

        @Override
        protected FrameworkElement createFrameworkElement() {
            PortCreationInfo def = RawBlackboardClient.getDefaultPci().derive(getDescription());
            PortCreationInfo pci = getParent().getPortCreationInfo(def, this, null);
            RawBlackboardClient c = new RawBlackboardClient(pci == null ? def : pci, false, -1);
            return c.getWritePort();
        }

        public void addChangeListener(PortListener<T> listener) {
            EventRouter.addListener(getClient().getReadPort(), "addPortListenerRaw", listener);
        }

        public RawBlackboardClient getClient() {
            return getPort().getBBClient();
        }

        @SuppressWarnings("unchecked")
        public T readAutoLocked() {
            return (T)getClient().readAutoLocked();
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
