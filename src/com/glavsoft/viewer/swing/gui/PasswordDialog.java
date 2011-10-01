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

import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.EmptyBorder;

import com.glavsoft.viewer.swing.Utils;

/**
 * Dialog to ask password
 */
@SuppressWarnings("serial")
public class PasswordDialog extends JDialog {

	private String password = "";

	private static final int PADDING = 4;
	private final JLabel messageLabel;
	public PasswordDialog(Frame owner, final WindowListener onClose) {
		super(owner, "Login", true);
		addWindowListener(onClose);

		JPanel pane = new JPanel(new GridLayout(0, 1, PADDING, PADDING));
		add(pane);
		pane.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

		messageLabel = new JLabel("Server requires password authentication");
		pane.add(messageLabel);

		JPanel passwordPanel = new JPanel();
		passwordPanel.add(new JLabel("Password:"));
		final JPasswordField passwordField = new JPasswordField("", 20);
		passwordPanel.add(passwordField);
		pane.add(passwordPanel);

		JPanel buttonPanel = new JPanel();
		JButton loginButton = new JButton("Login");
		buttonPanel.add(loginButton);
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				password = new String(passwordField.getPassword());
				setVisible(false);
			}
		});

		JButton closeButton = new JButton("Close");
		buttonPanel.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				onClose.windowClosing(null);
			}
		});

		pane.add(buttonPanel);

		getRootPane().setDefaultButton(loginButton);

		List<Image> icons = Utils.getIcons();
		if (icons.size() != 0) {
			setIconImages(icons);
		}

		// center dialog
		Point locationPoint = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		pack();
		Rectangle bounds = getBounds();
		locationPoint.setLocation(
				locationPoint.x - bounds.width/2, locationPoint.y - bounds.height/2);
		setLocation(locationPoint);
	}

	public void setServerHostName(String serverHostName) {
		messageLabel.setText("Server '" + serverHostName + "' requires password authentication");
		pack();
	}

	public String getPassword() {
		return password;
	}

}
