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
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.cc.CCPort;
import org.finroc.core.port.cc.CCPortData;
import org.finroc.core.port.cc.CCPortListener;
import org.finroc.core.port.cc.PortNumeric;
import org.finroc.core.port.std.Port;
import org.finroc.core.port.std.PortData;
import org.finroc.core.port.std.PortListener;

/**
 * @author max
 *
 * Classes that can be used for private attributes that are automatically recognized
 * as widget input ports.
 */
public class WidgetInput {

    public static class Std<T extends PortData> extends WidgetInputPort<Port<T>> {

        /** UID */
        private static final long serialVersionUID = 4446496337106684704L;

        @Override
        protected Port<T> createPort() {
            return new Port<T>(getPci());
        }

        public void addChangeListener(PortListener<T> listener) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", listener);
        }

        public T getAutoLocked() {
            if ((super.defaultFlags & PortFlags.PUSH_STRATEGY) != 0 && (!getPort().pushStrategy())) {
                getPort().setPushStrategy(true); // we still/soon seem to need push strategy
            }
            return asPort().getAutoLocked();
        }
    }

    public static class CC<T extends CCPortData> extends WidgetInputPort<CCPort<T>> {

        /** UID */
        private static final long serialVersionUID = 2195466520164567898L;

        @Override
        protected CCPort<T> createPort() {
            return new CCPort<T>(getPci());
        }

        public void addChangeListener(CCPortListener<T> listener) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", listener);
        }

        public T getAutoLocked() {
            return asPort().getAutoLocked();
        }
    }

    public static class Numeric extends WidgetInputPort<PortNumeric> {

        /** UID */
        private static final long serialVersionUID = 2771906075250045196L;

        @Override
        protected PortNumeric createPort() {
            return new PortNumeric(getPci());
        }

        public void addChangeListener(CCPortListener<CoreNumber> listener) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", listener);
        }

        public int getInt() {
            return asPort().getIntRaw();
        }

        public double getDouble() {
            return asPort().getDoubleRaw();
        }

        public CoreNumber getAutoLocked() {
            return asPort().getAutoLocked();
        }
    }

//  public static class Function extends WidgetInputPort<org.mca.commons.datatype.Function> {
//      /** UID */
//      private static final long serialVersionUID = -3359424921287800979L;
//      public Function(String description) {
//          super(org.mca.commons.datatype.Function.class, new PartWiseLinearFunction(), description);
//      }
//  }
//  public static class Strings extends WidgetInputPort<ContainsStrings> {
//      public Strings(String description) {
//          super(ContainsStrings.class, null, description);
//      }
//      /** UID */
//      private static final long serialVersionUID = -1247348356903465247L;
//  }
//  public static class BehaviourInfo extends WidgetInputPort<org.mca.commons.datatype.BehaviourInfo.List> {
//      public BehaviourInfo(String description) {
//          super(org.mca.commons.datatype.BehaviourInfo.List.class, null, description);
//      }
//      /** UID */
//      private static final long serialVersionUID = -1163735849469456363L;
//  }
}
