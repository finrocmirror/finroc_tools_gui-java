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
package org.finroc.tools.gui.util.propertyeditor;

import java.awt.BorderLayout;

import javax.naming.OperationNotSupportedException;
import javax.swing.JCheckBox;

public class BooleanEditor extends PropertyEditComponent<Boolean> {

    /** UID */
    private static final long serialVersionUID = 7615759489323538266L;

    private JCheckBox chk;

    @Override
    protected void createAndShow() throws Exception {
        chk = new JCheckBox();
        chk.setEnabled(isModifiable());
        valueUpdated(getCurWidgetValue());
        add(chk, BorderLayout.WEST);
    }

    @Override
    public void createAndShowMinimal(Boolean b) throws OperationNotSupportedException {
        chk = new JCheckBox();
        chk.setEnabled(isModifiable());
        valueUpdated(b);
        add(chk);
    }

    @Override
    public Boolean getCurEditorValue() {
        return chk.isSelected();
    }

    @Override
    protected void valueUpdated(Boolean t) {
        chk.setSelected(t);
    }

}
