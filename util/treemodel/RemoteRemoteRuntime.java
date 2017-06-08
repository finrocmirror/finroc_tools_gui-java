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
package org.finroc.tools.gui.util.treemodel;

import java.util.HashMap;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.finroc.core.FrameworkElement;
import org.finroc.core.FrameworkElementFlags;
import org.finroc.core.RuntimeEnvironment;
import org.finroc.core.admin.AdminClient;
import org.finroc.core.net.generic_protocol.RemoteRuntime;
import org.finroc.core.port.AbstractPort;
import org.finroc.core.port.Port;
import org.finroc.core.port.PortCreationInfo;
import org.finroc.core.port.PortListener;
import org.finroc.core.remote.BufferedModelChanges;
import org.finroc.core.remote.FrameworkElementInfo;
import org.finroc.core.remote.ModelHandler;
import org.finroc.core.remote.ModelNode;
import org.finroc.core.remote.RemoteConnector;
import org.finroc.core.remote.RemoteFrameworkElement;
import org.finroc.core.remote.RemotePort;
import org.rrlib.logging.Log;
import org.rrlib.logging.LogLevel;
import org.rrlib.serialization.BinaryInputStream;
import org.rrlib.serialization.MemoryBuffer;
import org.rrlib.serialization.SerializationInfo;

/**
 * @author Max Reichardt
 *
 * Remote runtime environment that finstruct cannot access directly
 * (e.g. running on embedded hardware).
 * This class provides access - using the interfaces.
 *
 * TODO: We have some code duplication from RemotePart. Maybe this could be refactored.
 */
public class RemoteRemoteRuntime extends RemoteRuntime implements PortListener<MemoryBuffer>, TreeModelListener {

    /** Framework element that is parent of all framework elements to access ports of remote runtime */
    private static FrameworkElement accessPortParent;

    /** Framework element that contains all elements for managing and accessing this runtime */
    private final FrameworkElement frameworkElement;

    /** Port receiving structure updates from remote runtime */
    private Port<MemoryBuffer> structureUpdatesPort;

    /** Node in parent interface; admin and structure updates ports should be located here */
    private final ModelNode nodeInParentInterface;

    /** Reference to global tree model */
    private final InterfaceTreeModel treeModel;

    /** Handler for remote model */
    private final ModelHandler handler;

    /** Id of protocol that this runtime is connected by */
    private final String protocolId;

    /** Lookup for parent model nodes for every protocol id */
    private static final HashMap<String, ModelNode> protocolParentNodeRegister = new HashMap<String, ModelNode>();

    /**
     * Lookup for remote framework elements (currently not ports) - similar to remote CoreRegister
     * (should only be accessed by reader thread of management connection)
     */
    //private HashMap<Integer, NetPort> remotePortRegister = new HashMap<Integer, NetPort>();

    public RemoteRemoteRuntime(String name, String uuid, AdminClient adminClient, String protocolId, ModelNode nodeInParentInterface, InterfaceTreeModel treeModel, ModelHandler handler) {
        super(getCommonParent(), uuid, adminClient == null, 12);
        if (adminClient != null) {
            this.adminInterface = adminClient;
        }
        this.frameworkElement = this.getAdminInterface().getParent();
        this.nodeInParentInterface = nodeInParentInterface;
        this.handler = handler;
        this.protocolId = protocolId;
        checkForPorts();
        this.treeModel = treeModel;
        treeModel.addTreeModelListener(this);
    }

    public void delete() {
        super.delete();
        treeModel.removeTreeModelListener(this);
        if (currentModelNode != null && currentModelNode.getParent() != null) {
            BufferedModelChanges changes = new BufferedModelChanges();
            changes.removeNode(currentModelNode);
            getModelHandler().applyModelChanges(changes);
        }
    }

    /**
     * Checks for ports in parent interface to connect to.
     * Is called whenever children are added to nodeInParentInterface'.
     */
    private void checkForPorts() {
        if (structureUpdatesPort == null) {
            ModelNode portNode = nodeInParentInterface.getChildByName("Structure");
            if (portNode != null && (portNode instanceof RemotePort)) {
                PortCreationInfo pci = new PortCreationInfo("Structure Updates", frameworkElement, MemoryBuffer.TYPE, FrameworkElementFlags.INPUT_PORT | FrameworkElementFlags.HAS_AND_USES_QUEUE);
                pci.maxQueueSize = -1;
                structureUpdatesPort = new Port<MemoryBuffer>(pci);
                structureUpdatesPort.addPortListener(this);
                structureUpdatesPort.connectTo(((RemotePort)portNode).getPort());
                structureUpdatesPort.init();
            }
        }
    }

    /**
     * (only to be called in constructor)
     *
     * @return Framework element that contains all elements for managing and accessing this remote runtimes
     */
    private static synchronized FrameworkElement getCommonParent() {
        if (accessPortParent == null) {
            accessPortParent = new FrameworkElement(RuntimeEnvironment.getInstance(), "Remote Runtime Access");
            accessPortParent.init();
        }
        return accessPortParent;
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        Object node = e.getTreePath().getLastPathComponent();
        if (node == nodeInParentInterface || ((node instanceof ModelNode) && nodeInParentInterface.isNodeAncestor(((ModelNode)node)))) {
            checkForPorts();
        }
        if (!nodeInParentInterface.isNodeAncestor(treeModel.getRoot())) {
            delete();
        }
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        Object node = e.getTreePath().getLastPathComponent();
        if (node == nodeInParentInterface || ((node instanceof RemoteFrameworkElement) && ((RemoteFrameworkElement)node).isNodeAncestor(nodeInParentInterface))) {
            checkForPorts();
        }
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        if (!nodeInParentInterface.isNodeAncestor(treeModel.getRoot())) {
            delete();
        }
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        Object node = e.getTreePath().getLastPathComponent();
        if (node == nodeInParentInterface || ((node instanceof ModelNode) && nodeInParentInterface.isNodeAncestor(((ModelNode)node)))) {
            checkForPorts();
        }
        if (!nodeInParentInterface.isNodeAncestor(treeModel.getRoot())) {
            delete();
        }
    }

    /**
     * @param handler Model handler
     * @param protocolId Id of protocol
     * @return Parent model nodes for specified protocol id
     */
    private synchronized static ModelNode getProtocolParentNode(ModelHandler handler, ModelNode treeModelRootNode, String protocolId) {
        ModelNode parent = protocolParentNodeRegister.get(protocolId);
        if (parent == null) {
            parent = new ModelNode(protocolId);
            protocolParentNodeRegister.put(protocolId, parent);

            BufferedModelChanges changes = new BufferedModelChanges();
            changes.addNode(treeModelRootNode, parent);
            handler.applyModelChanges(changes);
        }
        return parent;
    }

    @Override
    public void portChanged(AbstractPort origin, MemoryBuffer value) {
        if (origin == structureUpdatesPort.getWrapped()) {
            // Interpret remote structure
            boolean initial = (this.getParent() == null);
            try {
                processStructurePacket(value);

                if (initial) {
                    ModelNode parent = getProtocolParentNode(handler, treeModel.getRoot(), protocolId);
                    BufferedModelChanges changes = new BufferedModelChanges();
                    changes.addNode(parent, currentModelNode);
                    getModelHandler().applyModelChanges(changes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes buffer containing change events regarding remote runtime.
     * Must be executed from model thread.
     *
     * @param structureBufferToProcess Buffer containing serialized structure changes
     */
    public void processStructurePacket(MemoryBuffer structureBufferToProcess) throws Exception {
        FrameworkElementInfo info = new FrameworkElementInfo();
        BinaryInputStream stream = new BinaryInputStream(structureBufferToProcess, new SerializationInfo());
        while (stream.moreDataAvailable()) {
            byte opcode = stream.readByte();
            if (opcode == 0) {
                info.deserialize(stream, true);
                //Log.log(LogLevel.DEBUG, info.getLink(0).name);
                addRemoteStructure(info);
                int x = stream.readInt();
                if (x != 0x58585858) {
                    Log.log(LogLevel.ERROR, Integer.toHexString(x));
                }

            }/* else if (opcode == TCP.OpCode.STRUCTURE_CHANGE) {
                int handle = stream.readInt();
                int flags = stream.readInt();
                short strategy = stream.readShort();
                short updateInterval = stream.readShort();
                ProxyPort port = remotePortRegister.get(handle);
                if (port != null) {
                    info.deserializeConnections(stream);
                    port.update(flags, strategy, updateInterval, info.copyConnections(), info.copyNetworkConnections());

                    RemotePort[] modelElements = RemotePort.get(port.getPort());
                    for (RemotePort modelElement : modelElements) {
                        modelElement.setFlags(flags);
                    }
                } else {
                    RemoteFrameworkElement element = getRemoteElement(handle);
                    if (element != null) {
                        element.setFlags(flags);
                    }
                }
            } else if (opcode == TCP.OpCode.STRUCTURE_DELETE) {
                int handle = stream.readInt();
                ProxyPort port = remotePortRegister.get(handle);
                if (port != null) {
                    RemotePort[] modelElements = RemotePort.get(port.getPort());
                    port.managedDelete();
                    for (RemotePort modelElement : modelElements) {
                        handler.removeNode(modelElement);
                    }
                    elementLookup.remove(handle);
                    uninitializedRemotePorts.remove(port);
                } else {
                    RemoteFrameworkElement element = getRemoteElement(handle);
                    if (element != null) {
                        handler.removeNode(element);
                        elementLookup.remove(handle);
                    }
                }
            } else {
                Log.log(LogLevel.WARNING, this, "Received corrupted structure info. Skipping packet");
                return;
            }*/
        }

        // Initialize ports whose links are now complete
//        synchronized (frameworkElement.getRegistryLock()) {
//            for (int i = 0; i < uninitializedRemotePorts.size(); i++) {
//                ProxyPort port = uninitializedRemotePorts.get(i);
//                RemotePort[] remotePorts = RemotePort.get(port.getPort());
//                boolean complete = true;
//                for (RemotePort remotePort : remotePorts) {
//                    complete |= remotePort.isNodeAncestor(this);
//                }
//                if (complete) {
//                    for (int j = 0; j < remotePorts.length; j++) {
//                        port.getPort().setName(createPortName(remotePorts[j]), j);
//                    }
//                    port.getPort().init();
//                    uninitializedRemotePorts.remove(i);
//                    i--;
//                }
//            }
//        }
    }

    final char SEPARATOR = 1;

    /**
     * Called during initial structure exchange
     *
     * @param info Info on another remote framework element
     */
    public void addRemoteStructure(FrameworkElementInfo info) {
        Log.log(LogLevel.DEBUG_VERBOSE_1, this, "Adding element: " + info.toString());
        org.finroc.core.remote.RemoteRuntime remoteRuntime = newModelNode;
        if (info.isPort()) {
            ModelNode portNode = nodeInParentInterface.getChildByQualifiedName("Internal Ports" + SEPARATOR + (((long)info.getHandle()) & 0xFFFFFFFFL), SEPARATOR);
            if (portNode == null && getFrameworkElement(info.getLink(0).parent) != null) {
                portNode = nodeInParentInterface.getChildByQualifiedName(getFrameworkElement(info.getLink(0).parent).getName() + SEPARATOR + info.getLink(0).name, SEPARATOR);
            }
            if (portNode instanceof RemotePort) {
                //NetPort netport = ((RemotePort)portNode).getPort().asNetPort();
                //remotePortRegister.put(info.getHandle(), netport);
                //ProxyPort port = new ProxyPort(info);

                for (int i = 0; i < info.getLinkCount(); i++) {
                    RemoteFrameworkElement remoteElement = new RemotePort(info.getHandle(), info.getLink(i).name, ((RemotePort)portNode).getPort(), i, info.getDataType());
                    if (i == 0) {
                        remoteRuntime.setElementLookupEntry(info.getHandle(), remoteElement);
                    }
                    remoteElement.setName(info.getLink(i).name);
                    remoteElement.setTags(info.getTags());
                    remoteElement.setFlags(info.getFlags());
                    ModelNode parent = getFrameworkElement(info.getLink(0).parent);
                    parent.add(remoteElement);
                }
                Log.log(LogLevel.DEBUG, this, "Found port for: " + info.toString());

                // add edges
                if (info.getOwnedConnectors() != null) {
                    for (RemoteConnector connector : info.getOwnedConnectors()) {
                        remoteRuntime.addConnector(connector);
                    }
                }
            } else {
                Log.log(LogLevel.WARNING, this, "Could not find the port for: " + info.toString());
            }
        } else {
            super.addRemoteStructure(info, true, null);
        }
    }

    /**
     * Returns framework element with specified handle.
     * Creates one if it doesn't exist.
     *
     * @param handle Remote handle of framework element
     * @param remoteRuntime Remote runtime model to use for lookup
     * @return Framework element.
     */
    public RemoteFrameworkElement getFrameworkElement(int handle) {
        return currentModelNode.obtainFrameworkElement(handle);
    }

    @Override
    public void createNewModel() {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    @Override
    public ModelHandler getModelHandler() {
        // TODO
        return handler;
    }


}
