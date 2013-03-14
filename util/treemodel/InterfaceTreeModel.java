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
package org.finroc.tools.gui.util.treemodel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

import org.finroc.core.FrameworkElement;
import org.finroc.core.RuntimeEnvironment;
import org.finroc.core.RuntimeListener;
import org.finroc.core.plugin.ExternalConnection;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.PortFlags;
import org.finroc.core.port.net.RemoteCoreRegister;
import org.finroc.tools.gui.FinrocGUI;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * Tree Model for interfaces
 */
public class InterfaceTreeModel extends DefaultTreeModel implements RuntimeListener {

    /** UID */
    private static final long serialVersionUID = -5379710469604864477L;

    /** Root node */
    private final InterfaceNode root;

    /** Global register of all ports. Allows accessing ports with simple handle. */
    private final RemoteCoreRegister<InterfaceNodePort> ports = new RemoteCoreRegister<InterfaceNodePort>();

    /** Global register of all framework elements (except of ports) (negative handle) */
    private final RemoteCoreRegister<InterfaceNode> elements = new RemoteCoreRegister<InterfaceNode>();

    public InterfaceTreeModel() {
        super(new InterfaceNode(new FrameworkElement(RuntimeEnvironment.getInstance(), "Interfaces").getLink(0)));
        root = (InterfaceNode)this.getRoot();
        RuntimeEnvironment.getInstance().addListener(this);
        elements.put(-getRootFrameworkElement().getHandle(), root);
        root.wrapped.getChild().init();
    }

    public FrameworkElement getRootFrameworkElement() {
        return root.wrapped.getChild();
    }

    public InterfaceNode getInterfaceNode(FrameworkElement element) {
        return getInterfaceNode(element, 0);
    }

    /**
     * Get Node for element. If it doesn't exist yet - create it
     *
     * @param element Framework Element to get node for
     * @return Node
     */
    public InterfaceNode getInterfaceNode(FrameworkElement element, int count) {
        if (element.isDeleted()) {
            return null;
        }

        assert(element.isInitialized());
        InterfaceNode node = element.isPort() ? ports.get(element.getHandle()) : elements.get(-element.getHandle());
        if (node != null) {
            return node;
        }

        InterfaceNode primary = null;
        for (int i = 0; i < element.getLinkCount(); i++) {
            FrameworkElement.Link l = element.getLink(i);
            if (element.isPort()) {
                InterfaceNodePort in = new InterfaceNodePort(l);
                InterfaceNode parent = getInterfaceNode(l.getParent(), count + 1);
                parent.add(in);
                if (i == 0) {
                    primary = in;
                    ports.put(element.getHandle(), in);
                } else {
                    in.next = primary.next;
                    primary.next = in;
                }
                //in.next = ports.get(element.getHandle());
                //ports.put(element.getHandle(), in);
            } else {
                InterfaceNode in = new InterfaceNode(l);
                InterfaceNode parent = getInterfaceNode(l.getParent(), count + 1);
                parent.add(in);
                if (i == 0) {
                    primary = in;
                    elements.put(-element.getHandle(), in);
                } else {
                    in.next = primary.next;
                    primary.next = in;
                }
                //in.next = elements.get(-element.getHandle());
                //elements.put(-element.getHandle(), in);
            }
        }

        return getInterfaceNode(element, count + 1);
    }

    @Override
    public void runtimeChange(final byte changeType, final FrameworkElement element) {
        if (!element.isChildOf(root.wrapped.getChild(), true)) {
            return; // not of interest
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (RuntimeEnvironment.getInstance().getRegistryLock()) {
                    if (changeType == ADD && element.isReady()) {
                        final InterfaceNode in = getInterfaceNode(element);
                        InterfaceTreeModel.this.nodeStructureChanged(in.getParent());
                    } else if (changeType == REMOVE) {
                        if (element.isPort()) {
                            InterfaceNode in = ports.get(element.getHandle());
                            ports.remove(element.getHandle());
                            while (in != null) { // remove all links
                                final InterfaceNode parent = (InterfaceNode)in.getParent();
                                final int idx = parent.getIndex(in);
                                final InterfaceNode inCopy = in;
                                parent.remove(in);
                                InterfaceTreeModel.this.nodesWereRemoved(parent, new int[] {idx}, new Object[] {inCopy});
                                in = in.next;
                            }
                        } else {
                            InterfaceNode in = elements.get(-element.getHandle());;
                            elements.remove(-element.getHandle());
                            while (in != null) { // remove all links
                                final InterfaceNode parent = (InterfaceNode)in.getParent();
                                final int idx = parent.getIndex(in);
                                final InterfaceNode inCopy = in;
                                parent.remove(in);
                                InterfaceTreeModel.this.nodesWereRemoved(parent, new int[] {idx}, new Object[] {inCopy});
                                in = in.next;
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public void runtimeEdgeChange(byte changeType, AbstractPort source, AbstractPort target) {
        // do nothing at the moment
    }

    public PortWrapper getInputPort(String uid) {
        PortWrapper in = getPort(uid);
        return (in != null && in.getPort().getFlag(PortFlags.EMITS_DATA)) ? in : null;
    }

    public PortWrapper getOutputPort(String uid) {
        PortWrapper in = getPort(uid);
        return (in != null && in.getPort().getFlag(PortFlags.ACCEPTS_DATA)) ? in : null;
    }

    public PortWrapper getPort(String uid) {
        StringBuilder tmp = new StringBuilder();
        FrameworkElement el = RuntimeEnvironment.getInstance().getPort(uid);
        if (el != null && el.isPort()) {
            InterfaceNodePort inp = ports.get(el.getHandle());
            while (inp != null) {
                inp.getPort().getQualifiedLink(tmp, inp.wrapped);
                if (tmp.toString().equals(uid)) {
                    return inp;
                }
                inp = (InterfaceNodePort)inp.next;
            }
            FinrocGUI.logDomain.log(LogLevel.LL_WARNING, "InterfaceTreeModel", "Strange... could not find " + uid + " - although runtime returned something for it");
        }
        return null;
    }

    public int childEntryCount() {
        return root.getChildCount();
    }

    public List<ExternalConnection> getActiveInterfaces() {
        List<ExternalConnection> list = new ArrayList<ExternalConnection>();
        //list.add(ioInterfaces.get(0));
        for (int i = 0; i < root.getChildCount(); i++) {
            list.add((ExternalConnection)((InterfaceNode)root.getChildAt(i)).wrapped.getChild());
        }
        return list;
    }
}
