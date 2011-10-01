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

package com.glavsoft.drawing;

import java.util.Arrays;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.decoder.FramebufferUpdateRectangle;
import com.glavsoft.transport.Transport;

/**
 * Render bitmap data
 *
 * @author dime @ tightvnc.com
 */
public abstract class Renderer {

	protected Transport.Reader reader;

	public abstract void drawJpegImage(byte[] bytes, int offset,
			int jpegBufferLength, FramebufferUpdateRectangle rect);

	protected int width;
	protected int height;
	protected int bytesPerPixel;
	protected int bytesPerPixelSignificant;
	protected int[] pixels;
	protected SoftCursor cursor;
	protected PixelFormat pixelFormat;

	/**
	 * Draw byte array bitmap data
	 *
	 * @param bytes bitmap data
	 * @param x bitmap x position
	 * @param y bitmap y position
	 * @param width bitmap width
	 * @param height bitmap height
	 */
	public void drawBytes(byte[] bytes, int x, int y, int width, int height) {
		drawBytes(bytes, 0, x, y, width, height, false);
	}

	/**
	 * Draw byte array bitmap data
	 *
	 * @param bytes bitmap data
	 * @param offset start position in bytes from which real bitmap data located
	 * @param x bitmap x position
	 * @param y bitmap y position
	 * @param width bitmap width
	 * @param height bitmap height
	 * @param isCompressed when true use 3 bytes for single colour, or use 4 bytes otherwise (for 32bpp mode)
	 * @return
	 */
	public synchronized int drawBytes(byte[] bytes, int offset,
			int x, int y, int width, int height, boolean isCompressed) {
		return drawBytes(bytes, offset, x, y, width, height, isCompressed, false);
	}

	public synchronized int drawBytes(byte[] bytes, int offset,
			int x, int y, int width, int height, boolean isCompressed, boolean needSwap) {
		int i = offset;
		for (int ly = y; ly < y + height; ++ly) {
			int end = ly * this.width + x + width;
			for (int pixelsOffset = ly * this.width + x; pixelsOffset < end; ++pixelsOffset) {
				pixels[pixelsOffset] = readCompactPixelColor(bytes, i, needSwap);
				i += isCompressed ? bytesPerPixelSignificant : bytesPerPixel;
			}
		}
		return i - offset;
	}

	/**
	 * Draw paletted byte array bitmap data
	 *
	 * @param buffer bitmap data
	 * @param rect bitmap location and dimensions
	 * @param palette colour palette
	 */
	public synchronized void drawBytesWithPalette(byte[] buffer, FramebufferUpdateRectangle rect,
			int[] palette) {
				// 2 colors
				if (palette.length == 2) {
					int dx, dy, n;
					int i = rect.y * this.width + rect.x;
					int rowBytes = (rect.width + 7) / 8;
					byte b;

					for (dy = 0; dy < rect.height; dy++) {
						for (dx = 0; dx < rect.width / 8; dx++) {
							b = buffer[dy * rowBytes + dx];
							for (n = 7; n >= 0; n--) {
								pixels[i++] = palette[b >> n & 1];
							}
						}
						for (n = 7; n >= 8 - rect.width % 8; n--) {
							pixels[i++] = palette[buffer[dy * rowBytes + dx] >> n & 1];
						}
						i += this.width- rect.width;
					}
				} else {
					// 3..255 colors (assuming bytesPixel == 4).
					int i = 0;
					for (int ly =  rect.y; ly < rect.y + rect.height; ++ly) {
						for (int lx = rect.x; lx < rect.x + rect.width; ++lx) {
							int pixelsOffset = ly * this.width + lx;
							pixels[pixelsOffset] = palette[buffer[i++] & 0xFF];
						}
					}
				}

			}

	/**
	 * Copy rectangle region from one position to another. Regions may be overlapped.
	 *
	 * @param srcX source rectangle x position
	 * @param srcY source rectangle y position
	 * @param dstRect destination rectangle posions and rectangle dimensions
	 */
	public synchronized void copyRect(int srcX, int srcY, FramebufferUpdateRectangle dstRect) {
		int startSrcY, endSrcY, dstY, deltaY;
		if (srcY > dstRect.y) {
			startSrcY = srcY;
			endSrcY = srcY + dstRect.height;
			dstY = dstRect.y;
			deltaY = +1;
		} else {
			startSrcY = srcY + dstRect.height - 1;
			endSrcY = srcY -1;
			dstY = dstRect.y + dstRect.height - 1;
			deltaY = -1;
		}
		for (int y = startSrcY; y != endSrcY; y += deltaY) {
			System.arraycopy(pixels, y * width + srcX,
					pixels, dstY * width + dstRect.x, dstRect.width);
			dstY += deltaY;
		}
	}

	/**
	 * Fill rectangle region with specified colour
	 *
	 * @param color colour to fill with
	 * @param rect rectangle region posions and dimensions
	 */
	public void fillRect(int color, FramebufferUpdateRectangle rect) {
		fillRect(color, rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Fill rectangle region with specified colour
	 *
	 * @param color colour to fill with
	 * @param x rectangle x position
	 * @param y rectangle y position
	 * @param width rectangle width
	 * @param height rectangle height
	 */
	public synchronized void fillRect(int color, int x, int y, int width, int height) {
				int sy = y * this.width + x;
				int ey = sy + height * this.width;
				for (int i = sy; i < ey; i += this.width) {
					Arrays.fill(pixels, i, i + width, color);
				}
			}

	/**
	 * Reads color bytes (PIXEL) from reader, returns int combined RGB
	 * value consisting of the red component in bits 16-23, the green component
	 * in bits 8-15, and the blue component in bits 0-7. May be used directly for
	 * creation awt.Color object
	 */
	public int readPixelColor(Transport.Reader reader) throws TransportException {
		int color = readCompactPixelColor(reader);
		if (4 == bytesPerPixel) {
			reader.readByte(); // skip unused byte
		}
		return color;
	}

	/**
	 * Reads 24bpp color bytes (PIXEL) from reader, returns int combined RGB
	 * value consisting of the red component in bits 16-23, the green component
	 * in bits 8-15, and the blue component in bits 0-7. May be used directly for
	 * creation awt.Color object
	 */
	public int readCompactPixelColor(Transport.Reader reader) throws TransportException {
		return readCompactPixelColor(reader, false);
	}
	public int readCompactPixelColor(Transport.Reader reader, boolean needSwap) throws TransportException {
		int c = bytesPerPixelSignificant;
		int color = 0;
		do {
			color <<= 8;
			color |= reader.readUInt8();
		} while (--c > 0);
		if (needSwap && 0 == pixelFormat.bigEndianFlag) {
			color = swapColorBytes(color);
		}
		return convertColor(color);
	}

	public int readCompactPixelColor(byte[] bytes, int offset) {
		return readCompactPixelColor(bytes, offset, false);
	}
	public int readCompactPixelColor(byte[] bytes, int offset, boolean needSwap) {
		int c = bytesPerPixelSignificant;
		int color = 0;
		do {
			color <<= 8;
			color |= bytes[offset++] & 0x0ff;
		} while (--c > 0);
		if (needSwap && 0 == pixelFormat.bigEndianFlag) {
			color = swapColorBytes(color);
		}
		return convertColor(color);
	}

	private int swapColorBytes(int rawColor) {
		if (3 == bytesPerPixelSignificant)
			return rawColor >> 16 & 0x0ff |
				rawColor & 0x0ff00 |
				rawColor << 16 & 0x0ff0000;
		else if (2 == bytesPerPixelSignificant)
			return rawColor >> 8 & 0x0ff |
			rawColor << 8 & 0x0ff00;
		else
			return rawColor;
	}

	/**
	 * Swap color bits from server sent order into current color model order (0x0rrggbb)
	 *
	 * @param rawColor
	 * @return color
	 */
	private int convertColor(int rawColor) {
		return rawColor >> pixelFormat.redShift & pixelFormat.redMax |
				(rawColor >> pixelFormat.greenShift & pixelFormat.greenMax) << 8 |
				(rawColor >> pixelFormat.blueShift & pixelFormat.blueMax) << 16;
	}

	public int getBytesPerPixel() {
		return bytesPerPixel;
	}

	public int getBytesPerPixelSignificant() {
		return bytesPerPixelSignificant;
	}

	public int putPixelIntoByteArray(byte[] decodedBytes, int decodedOffset, int color) {
		putPixelsIntoByteArray(decodedBytes, decodedOffset, 1, color);
		return bytesPerPixelSignificant;
	}

	/**
	 * @param decodedBytes
	 * @param decodedOffset
	 * @param rlength
	 * @param color
	 */
	public void putPixelsIntoByteArray(byte[] decodedBytes, int decodedOffset,
			int rlength, int color) {
				byte r = (byte) (color >> 16 & 0x0ff);
				byte g = (byte) (color >> 8 & 0x0ff);
				byte b = (byte) (color & 0x0ff);
				if (3 == bytesPerPixelSignificant) {
					while (rlength-- > 0) {
						decodedBytes[decodedOffset++] = b;
						decodedBytes[decodedOffset++] = g;
						decodedBytes[decodedOffset++] = r;
					}
				} else {
					int newColor = (r & pixelFormat.redMax) << pixelFormat.redShift |
									(g & pixelFormat.greenMax) << pixelFormat.greenShift |
									(b & pixelFormat.blueMax) << pixelFormat.blueShift;
					while (rlength-- > 0) {
						if (2 == bytesPerPixelSignificant) {
							decodedBytes[decodedOffset++] =	(byte) (newColor >> 8 & 0x0ff);
						}
						decodedBytes[decodedOffset++] =	(byte) (newColor & 0x0ff);
					}
				}
			}

	/**
	 * Width of rendered image
	 *
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Height of rendered image
	 *
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Read and decode cursor image
	 *
	 * @param rect new cursor hot point position and cursor dimensions
	 * @throws TransportException
	 */
	public void createCursor(int[] cursorPixels, FramebufferUpdateRectangle rect)
		throws TransportException {
		synchronized (cursor) {
			cursor.createCursor(cursorPixels, rect.x, rect.y, rect.width, rect.height);
		}
	}

	/**
	 * Read and decode new cursor position
	 *
	 * @param rect cursor position
	 */
	public void decodeCursorPosition(FramebufferUpdateRectangle rect) {
		synchronized (cursor) {
			cursor.updatePosition(rect.x, rect.y);
		}
	}

}