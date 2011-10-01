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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.rfb.IRepaintController;
import com.glavsoft.rfb.IUserInputController;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.rfb.protocol.LocalPointer;
import com.glavsoft.rfb.protocol.MessageQueue;
import com.glavsoft.rfb.protocol.ProtocolSettings;

@SuppressWarnings("serial")
public class Surface extends JPanel implements IRepaintController, IUserInputController {

	private int width;
	private int height;
	private Image offscreenImage;
	private SoftCursorImpl cursor;
	private Renderer renderer;
	private MouseEventListener mouseEventListener;
	private KeyEventListener keyEventListener;
	private final MessageQueue messageQueue;
	private boolean showCursor;
	private JScrollPane scroller;
	private ModifierButtonEventListener modifierButtonListener;

	@Override
	public boolean isDoubleBuffered() {
		// TODO returning false in some reason may speed ups drawing, but may
		// not. Needed in challenging.
		return false;
	}

	public Surface(int width, int height, MessageQueue messageQueue, ProtocolSettings settings) {
		this.messageQueue = messageQueue;
		init(width, height);

		if ( !settings.isViewOnly()) {
			enableUserInput(true);
		}
		showCursor = settings.getMouseCursorTrack() != LocalPointer.HIDE;
	}

	@Override
	public void enableUserInput(boolean enable) {
		if (enable) {
			if (null == mouseEventListener) {
				mouseEventListener = new MouseEventListener(this, messageQueue);
			}
			addMouseListener(mouseEventListener);
			addMouseMotionListener(mouseEventListener);
			addMouseWheelListener(mouseEventListener);

			setFocusTraversalKeysEnabled(false);
			if (null == keyEventListener) {
				keyEventListener = new KeyEventListener(messageQueue);
				if (modifierButtonListener != null) {
					keyEventListener.addModifierListener(modifierButtonListener);
				}
			}
			addKeyListener(keyEventListener);
		} else {
			removeMouseListener(mouseEventListener);
			removeMouseMotionListener(mouseEventListener);
			removeMouseWheelListener(mouseEventListener);
			removeKeyListener(keyEventListener);
		}
	}

	@Override
	public void init(Renderer renderer) {
		this.renderer = renderer;
		synchronized (renderer) {
			// TODO avoid from cast
			cursor = ((RendererImpl) renderer).getCursor();
		}
		init(renderer.getWidth(), renderer.getHeight());
	}

	private void init(int width, int height) {
		this.width = width;
		this.height = height;
		setSize(width, height);
		if (scroller != null) {
			scroller.updateUI();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		synchronized (renderer) {
			// TODO avoid from cast
			offscreenImage = ((RendererImpl) renderer).getOffscreanImage();
			if (offscreenImage != null) {
				g.drawImage(offscreenImage, 0, 0, null);
			}
		}
		synchronized (cursor) {
			Image cursorImage = cursor.getImage();
			if (showCursor && cursorImage != null &&
				g.getClipBounds().intersects(cursor.rX, cursor.rY, cursor.width, cursor.height)) {
					g.drawImage(cursorImage, cursor.rX, cursor.rY, null);
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(width, height);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(width, height);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(width, height);
	}

	/**
	 * Saves context and simply invokes native JPanel repaint method which
	 * asyncroniously register repaint request using invokeLater to repaint be
	 * runned in Swing event dispatcher thread. So may be called from other
	 * threads.
	 */
	@Override
	public void repaintBitmap(FramebufferUpdateRectangle rect) {
		repaintBitmap(rect.x, rect.y, rect.width, rect.height);
	}

	@Override
	public void repaintBitmap(int x, int y, int width, int height) {
		repaint(x, y, width, height);
	}

	@Override
	public void repaintCursor() {
		synchronized (cursor) {
			repaint(cursor.oldRX, cursor.oldRY, cursor.oldWidth, cursor.oldHeight);
			repaint(cursor.rX, cursor.rY, cursor.width, cursor.height);
		}
	}

	@Override
	public void updateCursorPosition(short x, short y) {
		synchronized (cursor) {
			cursor.updatePosition(x, y);
			repaint(cursor.oldRX, cursor.oldRY, cursor.oldWidth, cursor.oldHeight);
			repaint(cursor.rX, cursor.rY, cursor.width, cursor.height);
		}
	}

	public void showCursor(boolean show) {
		synchronized (cursor) {
			showCursor = show;
		}
	}

	public void registerScroller(JScrollPane scroller) {
		this.scroller = scroller;

	}

	public void addModifierListener(ModifierButtonEventListener modifierButtonListener) {
		this.modifierButtonListener = modifierButtonListener;
		if (keyEventListener != null) {
			keyEventListener.addModifierListener(modifierButtonListener);
		}
	}

}
