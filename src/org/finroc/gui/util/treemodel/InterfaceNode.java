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
package org.finroc.gui.util.treemodel;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.finroc.core.FrameworkElement;

public class InterfaceNode extends DefaultMutableTreeNode implements TreeNode, Uid {

    /** UID */
    private static final long serialVersionUID = 4521408365192647987L;

    protected final FrameworkElement.Link wrapped;

    public InterfaceNode(FrameworkElement.Link wrapped) {
        super(wrapped.getDescription());
        this.wrapped = wrapped;
    }

    @Override
    public String getUid() {
        StringBuilder sb = new StringBuilder();
        wrapped.getChild().getQualifiedLink(sb, wrapped);
        return sb.toString();
    }

    // If there are links to the same framework element: Points to node that represents next link
    InterfaceNode next = null;

//  protected String uid = null;
//
//  public InterfaceNode(Object name) {
//      super(name);
//  }
//
//  public void createUid() {
//      uid = userObject.toString();
//      for (TreeNode tn = getParent(); tn != null; tn = tn.getParent()) {
//          uid = tn.toString() + "." + uid;
//      }
//  }
//
//  public String getUid() {
//      if (uid == null) {
//          createUid();
//      }
//      return uid;
//  }
//
//  public InterfaceNode addNode(String name) {
//      InterfaceNode in = new InterfaceNode(name);
//      add(in);
//      return in;
//  }
//
//  public void addInput(InputPort<?> ip) {
//      add(new InterfaceNodePort(ip, true));
//  }
//
//  public void addOutput(OutputPort<?> op) {
//      add(new InterfaceNodePort(op, false));
//  }
}
