/**
 * Copyright 2007 Kevin Twidle, Imperial College, London, England.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 *
 * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
 *
 * Created on Oct 17, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.inspector;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import eu.novi.ponder2.exception.Ponder2Exception;
import eu.novi.ponder2.objects.P2Boolean;
import eu.novi.ponder2.objects.P2Number;
import eu.novi.ponder2.objects.P2Object;
import eu.novi.ponder2.objects.P2String;

import eu.novi.ponder2.inspector.InspectorWindow;
import eu.novi.ponder2.inspector.Value;
import eu.novi.ponder2.inspector.ValueBoolean;
import eu.novi.ponder2.inspector.ValueNew;
import eu.novi.ponder2.inspector.ValueNumber;
import eu.novi.ponder2.inspector.ValueP2Object;
import eu.novi.ponder2.inspector.ValueString;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class InspectorWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JComboBox profile = null;

	private JPanel list = null;

	public static final String ProfileChange = "InspectorProfile";
	public static final String AttributeChange = "InspectorAttribute";
	public static final String NewValueChange = "InspectorNewValue";

	/**
	 * This method initializes profile
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getProfile() {
		if (profile == null) {
			profile = new JComboBox();
			profile.addActionListener(new java.awt.event.ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("actionPerformed()"); // TODO
																// Auto-generated
																// Event
					// stub actionPerformed()
					firePropertyChange(ProfileChange, "",
							profile.getSelectedItem());
				}
			});
		}
		return profile;
	}

	/**
	 * This method initializes list
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getList() {
		if (list == null) {
			GridLayout gridLayout = new GridLayout();
			gridLayout.setRows(25);
			gridLayout.setHgap(2);
			gridLayout.setVgap(2);
			gridLayout.setColumns(1);
			list = new JPanel();
			list.setLayout(gridLayout);
		}
		return list;
	}

	/**
	 * This method initializes jPanel
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.add(getOk(), new GridBagConstraints());
		}
		return jPanel;
	}

	/**
	 * This method initializes ok
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getOk() {
		if (ok == null) {
			ok = new JButton();
			ok.setText("Set");
			ok.addActionListener(new java.awt.event.ActionListener() {

				public void actionPerformed(java.awt.event.ActionEvent e) {
					System.out.println("Set button actionPerformed()"); // TODO
					// Auto-generated
					// Event stub
					// actionPerformed()
					for (Component component : getList().getComponents()) {
						Value attr = (Value) component;
						if (attr.changed()) {
							firePropertyChange(AttributeChange,
									attr.getAttributeName(), attr.getNewValue());
						}
					}
					firePropertyChange(NewValueChange, null, null);
				}
			});
		}
		return ok;
	}

	/**
	 * This method initializes jScrollPane
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getList());
		}
		return jScrollPane;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				InspectorWindow thisClass = new InspectorWindow(
						"Test Inspector");
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public InspectorWindow(String title) {
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
		this.setTitle(title);
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getProfile(), BorderLayout.NORTH);
			jContentPane.add(getJPanel(), BorderLayout.SOUTH);
			jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	public void manage(String name) {
		getProfile().addItem(name);
	}

	public void setList(Map<String, P2Object> list2) {
		List<String> names = new Vector<String>(list2.keySet());
		Collections.sort(names);
		getList().removeAll();
		GridLayout gridLayout = (GridLayout) getList().getLayout();
		gridLayout.setRows(names.size() + 1);
		getList().setLayout(gridLayout);
		for (String name : names) {
			JPanel att;
			P2Object obj = list2.get(name);
			try {
				if (obj instanceof P2Boolean)
					att = new ValueBoolean(name, obj.asBoolean());
				else if (obj instanceof P2Number)
					att = new ValueNumber(name, obj.asNumber().longValue());
				else if (obj instanceof P2String)
					att = new ValueString(name, obj.asString());
				else
					att = new ValueP2Object(name, obj);
				getList().add(att);
			} catch (Ponder2Exception e) {
			}
		}
		getList().add(new ValueNew());
		getList().revalidate();
	}

	private JPanel jPanel = null;

	private JButton ok = null;

	private JScrollPane jScrollPane = null;

}
