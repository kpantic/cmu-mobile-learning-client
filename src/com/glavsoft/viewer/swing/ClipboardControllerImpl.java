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

package com.glavsoft.viewer.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.logging.Logger;

import com.glavsoft.rfb.ClipboardController;
import com.glavsoft.rfb.client.ClientCutTextMessage;
import com.glavsoft.rfb.protocol.MessageQueue;

public class ClipboardControllerImpl implements ClipboardController, Runnable {
	private static final long CLIPBOARD_UPDATE_CHECK_INTERVAL_MILS = 1000L;
	private Clipboard clipboard;
	private String clipboardText = null;
	private volatile boolean isRunning;
	private boolean isEnabled;
	private final MessageQueue messageQueue;
	public ClipboardControllerImpl(MessageQueue messageQueue) {
		this.messageQueue = messageQueue;
		try {
			clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			updateSavedClippoardContent(); // prevent onstart clipboard content sending
		} catch (AccessControlException e) { /*nop*/ }
	}

	@Override
	public void updateSystemClipboard(String text) {
		if (clipboard != null) {
			StringSelection stringSelection = new StringSelection(text);
			clipboard.setContents(stringSelection, null);
		}
	}

	/**
	 *	Callback for clipboard changes listeners
	 *  Retrives text content from system clipboard which then available
	 *  through getClipboardText().
	 */
	private void updateSavedClippoardContent() {
		if (clipboard != null && clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				clipboardText = (String)clipboard.getData(DataFlavor.stringFlavor);
			} catch (UnsupportedFlavorException e) {
				Logger.getLogger("com.glavsoft.viewer.swing").throwing("ClipboardControllerImpl", "updateSavedClippoardContent", e);
				// ignore
			} catch (IOException e) {
				Logger.getLogger("com.glavsoft.viewer.swing").throwing("ClipboardControllerImpl", "updateSavedClippoardContent", e);
				// ignore
			}
		} else {
			clipboardText = null;
		}
	}

	@Override
	public String getClipboardText() {
		return clipboardText;
	}

	/**
	 * Get text clipboard contens when needed send to remote, or null vise versa
	 *
	 * @return clipboad string contents if it is changed from last method call
	 * or null when clipboard contains non text object or clipboard contents didn't changed
	 */
	@Override
	public String getRenuedClipboardText() {
		String old = clipboardText;
		updateSavedClippoardContent();
		if (clipboardText != null && ! clipboardText.equals(old))
			return clipboardText;
		return null;
	}

	@Override
	public void setEnabled(boolean enable) {
		if (isEnabled && ! enable) {
			isRunning = false;
		}
		if (enable && ! isEnabled) {
			new Thread(this).start();
		}
		this.isEnabled = enable;


	}

	@Override
	public void run() {
		isRunning = true;
		while (isRunning) {
			String clipboardText = getRenuedClipboardText();
			if (clipboardText != null) {
				messageQueue.put(new ClientCutTextMessage(clipboardText));
			}
			try {
				Thread.sleep(CLIPBOARD_UPDATE_CHECK_INTERVAL_MILS);
			} catch (InterruptedException e) { /*nop*/ }
		}
	}

}
