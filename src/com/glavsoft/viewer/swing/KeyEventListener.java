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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import com.glavsoft.rfb.client.KeyEventMessage;
import com.glavsoft.rfb.protocol.MessageQueue;

public class KeyEventListener implements KeyListener {

	private static final int K_F1 = 0xffbe;
	private static final int K_F2 = 0xffbf;
	private static final int K_F3 = 0xffc0;
	private static final int K_F4 = 0xffc1;
	private static final int K_F5 = 0xffc2;
	private static final int K_F6 = 0xffc3;
	private static final int K_F7 = 0xffc4;
	private static final int K_F8 = 0xffc5;
	private static final int K_F9 = 0xffc6;
	private static final int K_F10 = 0xffc7;
	private static final int K_F11 = 0xffc8;
	private static final int K_F12 = 0xffc9;
	private static final int K_INSERT = 0xff63;
	public static final int K_DELETE = 0xffff;
	private static final int K_HOME = 0xff50;
	private static final int K_END = 0xff57;
	private static final int K_PAGE_DOWN = 0xff56;
	private static final int K_PAGE_UP = 0xff55;
	private static final int K_DOWN = 0xff54;
	private static final int K_RIGHT = 0xff53;
	private static final int K_UP = 0xff52;
	private static final int K_LEFT = 0xff51;
	public static final int K_ESCAPE = 0xff1b;
	private static final int K_ENTER = 0xff0d;
	private static final int K_TAB = 0xff09;
	private static final int K_BACK_SPACE = 0xff08;
	public static final int K_ALT_LEFT = 0xffe9;
	private static final int K_META_LEFT = 0xffe7;
	private static final int K_SHIFT_LEFT = 0xffe1;
	public static final int K_CTRL_LEFT = 0xffe3;
	public static final int K_SUPER_LEFT = 0xffeb;
	public static final int K_HYPER_LEFT = 0xffed;

	private final MessageQueue queue;
	private ModifierButtonEventListener modifierButtonListener;

	public KeyEventListener(MessageQueue messageQueue) {
		queue = messageQueue;
	}

	private void processKeyEvent(KeyEvent e) {
		if (processModifierKeys(e))
			return;

		int keyCode = e.getKeyCode();

		if (processActionKey(keyCode, e))
			return;

		int keyChar = e.getKeyChar();
		if (0xffff == keyChar) { keyChar = 0; }
		if (keyChar < 0x20) {
			if (e.isControlDown()) {
				keyChar += 0x60; // TODO: From legacy code. What's this?
			} else {
				switch (keyChar) {
					case KeyEvent.VK_BACK_SPACE: keyChar = K_BACK_SPACE; break;
					case KeyEvent.VK_TAB: keyChar = K_TAB; break;
					case KeyEvent.VK_ENTER: keyChar = K_ENTER; break;
					case KeyEvent.VK_ESCAPE: keyChar = K_ESCAPE; break;
				}
			}
		} else if (KeyEvent.VK_DELETE == keyChar) {
			keyChar = K_DELETE;
		}

		sendKeyEvent(keyChar, e);
	}

	private boolean processActionKey(int keyCode, KeyEvent e) {
		if (e.isActionKey()) {
			switch (keyCode) {
				case KeyEvent.VK_HOME: keyCode = K_HOME; break;
				case KeyEvent.VK_LEFT: keyCode = K_LEFT; break;
				case KeyEvent.VK_UP: keyCode = K_UP; break;
				case KeyEvent.VK_RIGHT: keyCode = K_RIGHT; break;
				case KeyEvent.VK_DOWN: keyCode = K_DOWN; break;
				case KeyEvent.VK_PAGE_UP: keyCode = K_PAGE_UP; break;
				case KeyEvent.VK_PAGE_DOWN: keyCode = K_PAGE_DOWN; break;
				case KeyEvent.VK_END: keyCode = K_END; break;
				case KeyEvent.VK_INSERT: keyCode = K_INSERT; break;
				case KeyEvent.VK_F1: keyCode = K_F1; break;
				case KeyEvent.VK_F2: keyCode = K_F2; break;
				case KeyEvent.VK_F3: keyCode = K_F3; break;
				case KeyEvent.VK_F4: keyCode = K_F4; break;
				case KeyEvent.VK_F5: keyCode = K_F5; break;
				case KeyEvent.VK_F6: keyCode = K_F6; break;
				case KeyEvent.VK_F7: keyCode = K_F7; break;
				case KeyEvent.VK_F8: keyCode = K_F8; break;
				case KeyEvent.VK_F9: keyCode = K_F9; break;
				case KeyEvent.VK_F10: keyCode = K_F10; break;
				case KeyEvent.VK_F11: keyCode = K_F11; break;
				case KeyEvent.VK_F12: keyCode = K_F12; break;
				default: return false; // ignore other 'action' keys
			}
			sendKeyEvent(keyCode, e);
			return true;
		}
		return false;
	}

	private boolean processModifierKeys(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch (keyCode) {
			case KeyEvent.VK_CONTROL: keyCode = K_CTRL_LEFT; break;
			case KeyEvent.VK_SHIFT: keyCode = K_SHIFT_LEFT; break;
			case KeyEvent.VK_ALT: keyCode = K_ALT_LEFT; break;
			case KeyEvent.VK_META: keyCode = K_META_LEFT; break;
			// follow two are 'action' keys in java terms but modifier keys actualy
			case KeyEvent.VK_WINDOWS: keyCode = K_SUPER_LEFT; break;
			case KeyEvent.VK_CONTEXT_MENU: keyCode = K_HYPER_LEFT; break;
			default: return false;
		}
		if (modifierButtonListener != null) {
			modifierButtonListener.fireEvent(e);
		}
		sendKeyEvent(keyCode +
				(e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT ? 1 : 0), // "Right" Ctrl/Alt/Shift/Meta deffers frim "Left" ones by +1
				e);
		return true;
	}

	private void sendKeyEvent(int keyChar, KeyEvent e) {
		queue.put(new KeyEventMessage(keyChar, e.getID() == KeyEvent.KEY_PRESSED));
	}

	@Override
	public void keyTyped(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		processKeyEvent(e);
		e.consume();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		processKeyEvent(e);
		e.consume();
	}

	public void addModifierListener(ModifierButtonEventListener modifierButtonListener) {
		this.modifierButtonListener = modifierButtonListener;
	}

}
