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
 * Created on Feb 19, 2008
 *
 * $Log:$
 */

package eu.novi.ponder2.inspector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * TODO Description
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
class BooleanPanel extends JPanel {

	private JRadioButton trueButton = null;
	private JRadioButton falseButton = null;
	private ButtonGroup buttonGroup = null;

	public BooleanPanel() {
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 2;
		gridBagConstraints.gridy = 0;
		JLabel jLabel1 = new JLabel("False");
		JLabel jLabel = new JLabel("True");
		this.setLayout(new GridBagLayout());
		this.add(jLabel, new GridBagConstraints());
		this.add(getTrueButton(), new GridBagConstraints());
		this.add(getFalseButton(), gridBagConstraints);
		this.add(jLabel1, new GridBagConstraints());
		getButtonGroup().add(getFalseButton());
		getButtonGroup().add(getTrueButton());
	}

	void setValue(Boolean value) {
		JRadioButton button = value ? getTrueButton() : getFalseButton();
		button.setSelected(true);
	}

	Boolean getValue() {
		return getTrueButton().isSelected();
	}

	/**
	 * This method initializes trueButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getTrueButton() {
		if (trueButton == null) {
			trueButton = new JRadioButton();
		}
		return trueButton;
	}

	/**
	 * This method initializes falseButton
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getFalseButton() {
		if (falseButton == null) {
			falseButton = new JRadioButton();
		}
		return falseButton;
	}

	/**
	 * This method initializes buttonGroup
	 * 
	 * @return javax.swing.ButtonGroup
	 */
	private ButtonGroup getButtonGroup() {
		if (buttonGroup == null) {
			buttonGroup = new ButtonGroup();
		}
		return buttonGroup;
	}
}
