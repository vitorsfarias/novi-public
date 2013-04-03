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

import javax.swing.JTextField;

import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.inspector.ValuePanel;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class ValueP2Object extends ValuePanel {

	private JTextField field;

	private P2Object oldValue;

	/**
	 * Creates a new string attribute
	 */
	public ValueP2Object(String name, P2Object value) {
		super(name, new JTextField());
		field = (JTextField) getValue();
		field.setText(value.toString());
		oldValue = value;
	}

	@Override
	public boolean changed() {
		return false;
	}

	@Override
	public P2Object getNewValue() {
		return oldValue;
	}

}
