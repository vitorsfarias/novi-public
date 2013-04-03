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

package eu.novi.ponder2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;


import eu.novi.ponder2.apt.Ponder2op;
import eu.novi.ponder2.exception.Ponder2ArgumentException;
import eu.novi.ponder2.exception.Ponder2OperationException;
import eu.novi.ponder2.inspector.InspectorWindow;
import eu.novi.ponder2.objects.P2Hash;
import eu.novi.ponder2.objects.P2Object;

import eu.novi.ponder2.ManagedObject;

/**
 * Creates a window making available for editing the values of a Hash.
 * Understands, strings, numbers and booleans. If objects are in the hash, they
 * will be displayed but will not be editable.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class HashInspector implements ManagedObject {

	private InspectorWindow inspector;

	private Map<String, P2Hash> profile = new HashMap<String, P2Hash>();

	private P2Hash currentHash = null;

	private String currentName;

	/**
	 * Creates a HashInspector window with the default title of "Hash Inspector"
	 * 
	 */
	@Ponder2op("create")
	public HashInspector() {
		initialise("Hash Inspector");
	}

	/**
	 * Creates a HashInspector window with the title aString
	 * 
	 * @param aString
	 *            the title of the window
	 */
	@Ponder2op("create:")
	public HashInspector(String aString) {
		initialise(aString);
	}

	private void initialise(final String title) {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				inspector = new InspectorWindow(title);
				inspector.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				inspector.setVisible(true);
				inspector.addPropertyChangeListener(
						InspectorWindow.ProfileChange, profileChange);
				inspector.addPropertyChangeListener(
						InspectorWindow.AttributeChange, attributeChange);
				inspector.addPropertyChangeListener(
						InspectorWindow.NewValueChange, newValue);
			}
		});
	}

	/**
	 * manages aHash using the identifier aName for its display
	 * 
	 * @param aHash
	 *            the hash to be displayed
	 * @param aName
	 *            the name to be given in the window for the hash
	 * @throws Ponder2ArgumentException
	 * @throws Ponder2OperationException
	 */
	@Ponder2op("manage:as:")
	public void manage(P2Object aHash, final String aName)
			throws Ponder2ArgumentException, Ponder2OperationException {
		profile.put(aName, aHash.asHash());
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				inspector.manage(aName);
			}
		});
	}

	private void showValues(String name) {
		currentName = name;
		currentHash = profile.get(name);
		Map<String, P2Object> list = new HashMap<String, P2Object>();
		for (String key : currentHash.keySet()) {
			list.put(key, currentHash.get(key));
		}
		inspector.setList(list);
	}

	private PropertyChangeListener profileChange = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent evt) {
			String name = (String) evt.getNewValue();
			showValues(name);
		}

	};

	private PropertyChangeListener newValue = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent evt) {
			showValues(currentName);
		}

	};

	private PropertyChangeListener attributeChange = new PropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent evt) {
			System.out.println("Attribute " + evt.getOldValue()
					+ " changed to " + evt.getNewValue());
			if (currentHash != null) {
				P2Object value = (P2Object) evt.getNewValue();
				currentHash.put((String) evt.getOldValue(), value);
			}
		}
	};

}
