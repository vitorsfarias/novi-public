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
 * Created on Oct 16, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.inspector;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.inspector.Value;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public abstract class ValuePanel extends JPanel implements Value {

	private JLabel attName = null;
	private JTextField noAttName = null;
	private JComponent value;

	public ValuePanel(JComponent value) {
		noAttName = new JTextField();
		initialize(noAttName, value);
	}

	public ValuePanel(String name, JComponent value) {
		attName = new JLabel();
		attName.setText(name);
		initialize(attName, value);
	}

	private void initialize(JComponent attField, JComponent value) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.setRows(1);
		gridLayout.setHgap(10);
		this.setLayout(gridLayout);
		this.setSize(360, 25);
		this.add(attField, null);
		this.value = value;
		this.add(value, null);
	}

	public abstract boolean changed();

	public String getAttributeName() {
		return attName != null ? attName.getText() : noAttName.getText();
	}

	public abstract P2Object getNewValue();

	protected JComponent getValue() {
		return value;
	}

}
