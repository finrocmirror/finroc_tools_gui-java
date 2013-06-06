//
// You received this file as part of Finroc
// A Framework for intelligent robot control
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.finroc.tools.gui;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JMenuItem;



/**
 * @author Max Reichardt
 *
 * UndoBuffer for GUI-Window
 */
public class UndoBuffer<T> implements Runnable {

    /** Maximum entries in buffer */
    private static final int MAX_BUFFER_SIZE = 10;

    /** Default State of buffered object */
    private static final String DEFAULT_STATE = "  <Window>\n    <Panel/>\n  </Window>";

    /** Undo-Buffer */
    private LinkedList<UndoBufferEntry> buffer = new LinkedList<UndoBufferEntry>();

    /** Undo-Buffer */
    private LinkedList<UndoBufferEntry> redoBuffer = new LinkedList<UndoBufferEntry>();

    /** Info about running operation */
    private boolean running, valid, abort;
    private String description;

    /** Reference to object to serialize */
    private Object objToSerialize;

    /** Components to enable/disable when Undo is possible/impossible */
    private List<Component> undoComponents = new ArrayList<Component>();
    private List<Component> redoComponents = new ArrayList<Component>();

    public UndoBuffer() {
        clear();
    }

    public void addEntry(T newState, String description) {
        if (!running) {
            abort = false;
            this.description = description;
            this.objToSerialize = newState;
            new Thread(this, "Undo-Buffer").start();
        } else {
            valid = false;
            abort = false;
            this.objToSerialize = newState;
            this.description = description;
        }
        updateComponentEnable();
    }

    public void abortOperation() {
        abort = true;
    }

    // do serialization in seperate Thread (to not slow done application)
    public void run() {
        running = true;
        UndoBufferEntry ube = new UndoBufferEntry();
        do {
            valid = true;
            if (abort) {
                return;
            }
            try {
                ube.state = FinrocGuiXmlSerializer.getInstance().toXML(objToSerialize);
            } catch (Exception e) {
                valid = false;
            }
            if (abort) {
                return;
            }
            ube.lastOperation = description;
        } while (!valid);

        // Operation sucessful -> Save entry
        buffer.add(ube);
        redoBuffer.clear();
        if (buffer.size() > MAX_BUFFER_SIZE) {
            buffer.removeFirst();
        }
        running = false;
        updateComponentEnable();
    }


    private class UndoBufferEntry {
        private String state; // as XML
        private String lastOperation; // as description for menu
    }

    private boolean undoEnabled() {
        return buffer.size() > 1;
    }

    public String getUndoString() {
        if (undoEnabled()) {
            return "Undo " + buffer.getLast().lastOperation;
        }
        return "Undo";
    }

    private boolean redoEnabled() {
        return redoBuffer.size() > 0 && !running;
    }

    public String getRedoString() {
        if (redoEnabled()) {
            return "Redo " + redoBuffer.getLast().lastOperation;
        }
        return "Redo";
    }


    @SuppressWarnings("unchecked")
    public T undo() {
        if (!undoEnabled()) {
            throw new RuntimeException("Undo not enabled");
        }
        UndoBufferEntry ube = buffer.removeLast();
        redoBuffer.add(ube);
        updateComponentEnable();
        return (T)(FinrocGuiXmlSerializer.getInstance().fromXML(buffer.getLast().state));
    }


    @SuppressWarnings("unchecked")
    public T redo() {
        if (!redoEnabled()) {
            throw new RuntimeException("Redo not enabled");
        }
        UndoBufferEntry ube = redoBuffer.removeLast();
        buffer.add(ube);
        updateComponentEnable();
        return (T)(FinrocGuiXmlSerializer.getInstance().fromXML(ube.state));
    }

    public void clear() {
        buffer.clear();
        redoBuffer.clear();
        UndoBufferEntry ube = new UndoBufferEntry();
        ube.state = DEFAULT_STATE;
        buffer.add(ube);
        updateComponentEnable();
    }

    private void updateComponentEnable() {
        setUndoEnabled(undoEnabled());
        setRedoEnabled(redoEnabled());
    }

    private void setUndoEnabled(boolean enabled) {
        for (Component c : undoComponents) {
            c.setEnabled(enabled);
            if (c instanceof JMenuItem) {
                ((JMenuItem)c).setText(getUndoString());
            } else if (c instanceof JButton) {
                ((JButton)c).setToolTipText(getUndoString());
            }
        }
    }

    private void setRedoEnabled(boolean enabled) {
        for (Component c : redoComponents) {
            c.setEnabled(enabled);
            if (c instanceof JMenuItem) {
                ((JMenuItem)c).setText(getRedoString());
            } else if (c instanceof JButton) {
                ((JButton)c).setToolTipText(getRedoString());
            }
        }
    }

    public void addUndoComponent(Component c) {
        undoComponents.add(c);
        updateComponentEnable();
    }

    public void addRedoComponent(Component c) {
        redoComponents.add(c);
        updateComponentEnable();
    }
}
