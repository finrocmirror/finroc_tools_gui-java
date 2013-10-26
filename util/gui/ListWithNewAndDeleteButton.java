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
package org.finroc.tools.gui.util.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public abstract class ListWithNewAndDeleteButton<T> extends JPanel implements ActionListener, ListSelectionListener {

    /** UID */
    private static final long serialVersionUID = 5158799572411302270L;

    List<T> curList;
    private JButton newButton, delButton, editButton;
    private JButton upButton, downButton;
    private JList listUI;

    public ListWithNewAndDeleteButton(List<T> liste, String title, boolean multiselect, String buttonPosition, String editButtonText, boolean upDownButtons) {
        newButton = new JButton("Add...");
        delButton = new JButton("Delete");
        newButton.addActionListener(this);
        delButton.addActionListener(this);
        listUI = new JList();
        if (!multiselect) {
            listUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        setList(liste);
        setBorder(BorderFactory.createTitledBorder(title));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, ((buttonPosition == BorderLayout.WEST) || (buttonPosition == BorderLayout.EAST)) ? BoxLayout.PAGE_AXIS : BoxLayout.LINE_AXIS));
        buttonPanel.add(newButton);
        buttonPanel.add(delButton);
        if (editButtonText != null) {
            editButton = new JButton("Edit...");
            editButton.addActionListener(this);
            buttonPanel.add(editButton);
        }
        if (upDownButtons) {
            upButton = new JButton("Move up");
            downButton = new JButton("Move down");
            upButton.addActionListener(this);
            downButton.addActionListener(this);
            buttonPanel.add(upButton);
            buttonPanel.add(downButton);
        }
        for (JButton jb : createAdditionalButtons()) {
            buttonPanel.add(jb);
        }
        setLayout(new BorderLayout());
        add(new JScrollPane(listUI), BorderLayout.CENTER);
        add(buttonPanel, buttonPosition);
        listUI.addListSelectionListener(this);
    }

    protected JButton[] createAdditionalButtons() {
        return new JButton[0];
    }

    void setList(List<T> liste) {
        curList = liste;
        if (curList != null) {
            listUI.setListData(new Vector<T>(liste));
        } else {
            listUI.setListData(new Object[0]);
        }
        delButton.setEnabled(liste != null);
        newButton.setEnabled(liste != null);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == newButton) {
            T newElem = newPressed();
            if (newElem != null) {
                curList.add(newElem);
                setList(curList);
                listUI.setSelectedValue(newElem, true);
            }
        } else if (ae.getSource() == delButton) {
            for (Object elem : listUI.getSelectedValues()) {
                curList.remove(elem);
            }
            setList(curList);
        } else if (ae.getSource() == editButton) {
            editPressed(getSelectedItem());
        } else if (ae.getSource() == upButton) {
            T item = getSelectedItem();
            if (item == null) {
                return;
            }
            int index = curList.indexOf(item);
            if (index > 0) {
                curList.remove(index);
                curList.add(index - 1, item);
                setList(curList);
                listUI.setSelectedValue(item, true);
            }
        } else if (ae.getSource() == downButton) {
            T item = getSelectedItem();
            if (item == null) {
                return;
            }
            int index = curList.indexOf(item);
            if (index < curList.size() - 1) {
                curList.remove(index);
                curList.add(index + 1, item);
                setList(curList);
                listUI.setSelectedValue(item, true);
            }
        }
    }


    @SuppressWarnings("unchecked")
    public T getSelectedItem() {
        return (T)listUI.getSelectedValue();
    }

    public void valueChanged(ListSelectionEvent lse) {
        elementSelected(getSelectedItem());
    }

    public abstract T newPressed();
    public abstract void elementSelected(T t);
    public abstract void editPressed(T t);

    public void setControlsEnabled(boolean e) {
        delButton.setEnabled(e);
        downButton.setEnabled(e);
        editButton.setEnabled(e);
        listUI.setEnabled(e);
        newButton.setEnabled(e);
        upButton.setEnabled(e);
    }
}
