// Copyright (C) 2010, 2011 GlavSoft LLC.
// All rights reserved.
//
//-------------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
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
//-------------------------------------------------------------------------
//

package com.glavsoft.viewer.swing.gui;

import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.Viewer;
import com.glavsoft.viewer.swing.Utils;

/**
 * Dialog window for connection parameters get from.
 */
@SuppressWarnings("serial")
public class ConnectionDialog extends JDialog {
	private static final int PADDING = 4;
	private String serverNameString;
	private int serverPort;

	public ConnectionDialog(final JFrame owner, final WindowListener appWindowListener, String serverNameString,
			int serverPort, final OptionsDialog optionsDialog, final ProtocolSettings settings) {
		super(owner, "New TightVNC Connection", true);
		this.serverNameString = serverNameString;
		this.serverPort = serverPort;

		JPanel pane = new JPanel(new GridBagLayout());
		add(pane);
		pane.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

		setLayout(new GridBagLayout());
		GridBagConstraints cServerLabel = new GridBagConstraints();
		cServerLabel.gridx = 0; cServerLabel.gridy = 0;
		cServerLabel.weightx = 100; cServerLabel.weighty = 100;
		cServerLabel.gridwidth = 1; cServerLabel.gridheight = 1;
		cServerLabel.anchor = GridBagConstraints.LINE_END;
		cServerLabel.ipadx = PADDING;
		pane.add(new JLabel("TightVNC Server:"), cServerLabel);

		final JTextField serverNameField = new JTextField(
				null == serverNameString ? "" : serverNameString, 20);
		GridBagConstraints cServerName = new GridBagConstraints();
		cServerName.gridx = 1; cServerName.gridy = 0;
		cServerName.weightx = 0; cServerName.weighty = 100;
		cServerName.gridwidth = 1; cServerName.gridheight = 1;
		cServerName.anchor = GridBagConstraints.LINE_START;
		pane.add(serverNameField, cServerName);

		GridBagConstraints cPortLabel = new GridBagConstraints();
		cPortLabel.gridx = 0; cPortLabel.gridy = 1;
		cPortLabel.weightx = 100; cPortLabel.weighty = 100;
		cPortLabel.gridwidth = 1; cPortLabel.gridheight = 1;
		cPortLabel.anchor = GridBagConstraints.LINE_END;
		cPortLabel.ipadx = PADDING;
		cPortLabel.ipady = 10;
		pane.add(new JLabel("Port:"), cPortLabel);

		GridBagConstraints cPort = new GridBagConstraints();
		cPort.gridx = 1; cPort.gridy = 1;
		cPort.weightx = 0; cPort.weighty = 100;
		cPort.gridwidth = 1; cPort.gridheight = 1;
		cPort.anchor = GridBagConstraints.LINE_START;
		final JTextField serverPortField = new JTextField(
				String.valueOf(0 == serverPort ? Viewer.DEFAULT_PORT : serverPort), 10);
		pane.add(serverPortField, cPort);

		JPanel buttonPanel = new JPanel();

		JButton connectButton = new JButton("Connect");
		buttonPanel.add(connectButton);
		connectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setServerNameString(serverNameField.getText());
				setPort(serverPortField.getText());
				if (validateFields()) {
					setVisible(false);
				} else {
					serverNameField.requestFocusInWindow();
				}
			}
		});

		JButton optionsButton = new JButton("Options...");
		buttonPanel.add(optionsButton);
		optionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsDialog od = null == optionsDialog ?
						new OptionsDialog(owner) :
						optionsDialog;
				od.initControlsFromSettings(settings, true);
				od.setVisible(true);
			}
		});

		JButton closeButton = new JButton("Close");
		buttonPanel.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				appWindowListener.windowClosing(null);
			}
		});

		GridBagConstraints cButtons = new GridBagConstraints();
		cButtons.gridx = 0; cButtons.gridy = 2;
		cButtons.weightx = 100; cButtons.weighty = 100;
		cButtons.gridwidth = 2; cButtons.gridheight = 1;
		pane.add(buttonPanel, cButtons);

		getRootPane().setDefaultButton(connectButton);

		List<Image> icons = Utils.getIcons();
		if (icons.size() != 0) {
			setIconImages(icons);
		}
		addWindowListener(appWindowListener);

		// center dialog
		Point locationPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		pack();
		setResizable(false);
		Rectangle bounds = getBounds();
		locationPoint.setLocation(
				locationPoint.x - bounds.width/2, locationPoint.y - bounds.height/2);
		setLocation(locationPoint);
	}

	protected boolean validateFields() {
		return ! Strings.isTrimmedEmpty(serverNameString);
	}

	protected void setServerNameString(String text) {
		serverNameString = text;
	}

	public String getServerNameString() {
		return serverNameString;
	}

	public void setPort(String text) {
		try {
			serverPort = Integer.parseInt(text);
		} catch (NumberFormatException e) {
			serverPort = Viewer.DEFAULT_PORT;
		}
	}

	public int getPort() {
		return serverPort;
	}

}
