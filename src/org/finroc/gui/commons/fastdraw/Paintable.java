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
package org.finroc.gui.commons.fastdraw;

import java.awt.Graphics2D;

import org.finroc.core.buffer.CoreInput;
import org.finroc.core.buffer.CoreOutput;
import org.finroc.core.port.std.PortData;
import org.finroc.core.port.std.PortDataImpl;
import org.finroc.core.portdatabase.DataType;
import org.finroc.core.portdatabase.DataTypeRegister;

/**
 * @author max
 *
 * Marks objects that are paintable through Graphics interface
 */
public interface Paintable extends PortData {

    static DataType TYPE = DataTypeRegister.getInstance().getDataType(Paintable.class);

    public void paint(Graphics2D g);

    /**
     * Empty Paintable
     */
    public class Empty extends PortDataImpl implements Paintable {

        static DataType TYPE = DataTypeRegister.getInstance().getDataType(Empty.class, "DummyPaintable");

        @Override
        public void paint(Graphics2D g) {}

        @Override
        public void deserialize(CoreInput is) {}

        @Override
        public void serialize(CoreOutput os) {}

    }
}
