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

import java.util.Arrays;

import org.finroc.tools.gui.commons.EventRouter;
import org.rrlib.serialization.BinarySerializable;
import org.rrlib.serialization.NumericRepresentation;

import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.port.Port;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;
import org.finroc.core.port.ThreadLocalCache;
import org.finroc.core.port.std.PortBase;
import org.finroc.core.port.std.PortDataManager;
import org.finroc.core.port.std.PortQueueFragmentRaw;

/**
 * @author Max Reichardt
 *
 * Classes that can be used for private attributes that are automatically recognized
 * as widget input ports.
 */
public class WidgetInput {

    public static class Std<T extends BinarySerializable> extends WidgetInputPort<Port<T>> {

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
            if ((super.defaultFlags & FrameworkElementFlags.PUSH_STRATEGY) != 0 && (!getPort().pushStrategy())) {
                getPort().setPushStrategy(true); // we still/soon seem to need push strategy
            }
            return asPort().getAutoLocked();
        }
    }

    /**
     * Deprecated: Don't use this in new widgets
     */
    public static class CC<T extends BinarySerializable> extends WidgetInputPort<Port<T>> {

        /** UID */
        private static final long serialVersionUID = 2195466520164567898L;

        @Override
        protected Port<T> createPort() {
            return new Port<T>(getPci());
        }

        public void addChangeListener(PortListener<T> listener) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", listener);
        }

        public T getAutoLocked() {
            return asPort().getAutoLocked();
        }
    }

    public static class Numeric extends WidgetInputPort<Port<NumericRepresentation>> {

        /** UID */
        private static final long serialVersionUID = 2771906075250045196L;

        @Override
        protected Port<NumericRepresentation> createPort() {
            return new Port<NumericRepresentation>(getPci().derive(NumericRepresentation.TYPE));
        }

        public void addChangeListener(PortListener<NumericRepresentation> listener) {
            EventRouter.addListener(getPort(), "addPortListenerRaw", listener);
        }

        public int getInt() {
            int result = getAutoLocked().getNumericRepresentation().intValue();
            ThreadLocalCache.getFast().releaseAllLocks();
            return result;
        }

        public double getDouble() {
            double result = getAutoLocked().getNumericRepresentation().doubleValue();
            ThreadLocalCache.getFast().releaseAllLocks();
            return result;
        }

        public NumericRepresentation getAutoLocked() {
            return (NumericRepresentation)asPort().getAutoLocked();
        }
    }

    /**
     * Queued input (default queue size is 2)
     *
     * Contains additional functionality to get data with same timestamp from multiple queued inputs
     */
    public static class Queue<T extends BinarySerializable> extends Std<T> {

        @Override
        public PortCreationInfo getPci() {
            PortCreationInfo info = super.getPci();
            info.flags |= FrameworkElementFlags.HAS_QUEUE | FrameworkElementFlags.USES_QUEUE;
            info.maxQueueSize = 2;
            return info;
        }

        public PortQueueFragmentRaw dequeueAll() {
            ((PortBase)getPort()).dequeueAllRaw(queueFragment);
            return queueFragment;
        }

        /**
         * Get data with identical timestamps from all specified inputs
         * (caller needs not to worry about locks; data is locked until next call to this method - or until port disposal)
         * (note: does not work properly in conjunction with calling dequeueAll on any of the inputs)
         *
         * @param result Array to place result in (PortDataManger with data from every input on success, null entries otherwise). Must have sufficient length.
         * @param inputs Inputs to get consistent data from
         * @return Returns true if consistent data could be found; false if not
         */
        public static boolean getConsistentData(PortDataManager[] result, Queue<?> ... inputs) {
            for (Queue<?> input : inputs) {
                input.dequeueNewValues();
            }
            for (int i = 0; i < 2; i++) {
                boolean consistent = true;
                result[0] = i == 0 ? inputs[0].newestValue : inputs[0].secondNewestValue;
                if (result[0] == null) {
                    continue;
                }
                for (int j = 1; j < inputs.length; j++) {
                    result[j] = (inputs[j].newestValue != null && inputs[j].newestValue.getTimestamp().longValue() == result[0].getTimestamp().longValue()) ? inputs[j].newestValue : inputs[j].secondNewestValue;
                    if (result[j] == null || result[j].getTimestamp().longValue() != result[0].getTimestamp().longValue()) {
                        consistent = false;
                        break;
                    }
                }
                if (consistent) {
                    return true;
                }
            }
            Arrays.fill(result, null);
            return false;
        }

        @Override
        public void dispose() {
            if (newestValue != null) {
                newestValue.releaseLock();
            }
            if (secondNewestValue != null) {
                secondNewestValue.releaseLock();
            }
            super.dispose();
        }

        private transient PortQueueFragmentRaw queueFragment = new PortQueueFragmentRaw();
        private transient PortDataManager newestValue, secondNewestValue;

        @Override
        public void restore(Widget parent) {
            queueFragment = new PortQueueFragmentRaw();
            super.restore(parent);
        }

        private void dequeueNewValues() {
            ((PortBase)getPort()).dequeueAllRaw(queueFragment);
            PortDataManager dequeued;
            while ((dequeued = queueFragment.dequeueUnsafe()) != null) {
                if (secondNewestValue != null) {
                    secondNewestValue.releaseLock();
                }
                secondNewestValue = newestValue;
                newestValue = dequeued;
            }
            if (newestValue == null) {
                newestValue = ((PortBase)getPort()).getLockedUnsafeRaw(true);
            }
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
