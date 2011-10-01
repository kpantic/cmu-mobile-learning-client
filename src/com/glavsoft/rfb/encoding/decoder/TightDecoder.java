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

package com.glavsoft.rfb.encoding.decoder;

import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.glavsoft.drawing.Renderer;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.transport.Transport;

/**
 * Tight protocol extention decoder
 */
public class TightDecoder extends Decoder {
	private static Logger logger = Logger.getLogger("com.glavsoft.rfb.encoding.decoder");

    private static final int FILL_TYPE = 0x08;
    private static final int JPEG_TYPE = 0x09;

    private static final int FILTER_ID_MASK = 0x40;
    private static final int STREAM_ID_MASK = 0x30;

    private static final int BASIC_FILTER = 0x00;
    private static final int PALETTE_FILTER = 0x01;
    private static final int GRADIENT_FILTER = 0x02;
    private static final int MIN_SIZE_TO_COMPRESS = 12;

    static final int DECODERS_NUM = 4;
	Inflater[] decoders;

    private int decoderId;

    final static int tightZlibBufferSize = 512;

	public TightDecoder() {
		reset();
	}

	@Override
	public void decode(Transport.Reader reader, Renderer renderer,
			FramebufferUpdateRectangle rect) throws TransportException {
		int bytesPerPixel = renderer.getBytesPerPixelSignificant();

		/**
		 * bits
		 * 7 - FILL or JPEG type
		 * 6 - filter presence flag
		 * 5, 4 - decoder to use when Basic type (bit 7 not set)
		 *    or
		 * 4 - JPEG type when set bit 7
		 * 3 - reset decoder #3
		 * 2 - reset decoder #2
		 * 1 - reset decoder #1
		 * 0 - reset decoder #0
		 */
		int compControl = reader.readUInt8();
		resetDecoders(compControl);

		int compType = compControl >> 4 & 0x0F;
		switch (compType) {
		case FILL_TYPE:
			int color = renderer.readCompactPixelColor(reader, true);
			renderer.fillRect(color, rect);
			break;
		case JPEG_TYPE:
			if (bytesPerPixel != 3) {
//				throw new EncodingException(
//						"Tight doesn't support JPEG subencoding while depth not equal to 24bpp is used");
			}
			processJpegType(reader, renderer, rect);
			break;
		default:
			if (compType > JPEG_TYPE) {
//				throw new EncodingException(
//						"Compression control byte is incorrect!");
			} else {
				processBasicType(compControl, reader, renderer, rect);
			}
		}
	}

	private void processBasicType(int compControl, Transport.Reader reader,
			Renderer renderer, FramebufferUpdateRectangle rect) throws TransportException {
		decoderId = (compControl & STREAM_ID_MASK) >> 4;

		int filterId = 0;
		if ((compControl & FILTER_ID_MASK) > 0) { // filter byte presence
			filterId = reader.readUInt8();
		}
		int bytesPerCPixel = renderer.getBytesPerPixelSignificant();
		int lengthCurrentbpp = bytesPerCPixel * rect.width * rect.height;
		byte [] buffer;
		switch (filterId) {
		case BASIC_FILTER:
			buffer = readTightData(lengthCurrentbpp, reader);
			renderer.drawBytes(buffer, 0, rect.x, rect.y, rect.width, rect.height, true, true);
			break;
		case PALETTE_FILTER:
			int paletteSize = reader.readUInt8() + 1;
			int[] palette = readPalette(paletteSize, reader, renderer);
			int dataLength = paletteSize == 2 ?
				rect.height * ((rect.width + 7) / 8) :
				rect.width * rect.height;
			buffer = readTightData(dataLength, reader);
			renderer.drawBytesWithPalette(buffer, rect, palette);
			break;
		case GRADIENT_FILTER:
			buffer = readTightData(lengthCurrentbpp, reader);
			int stride = rect.width * bytesPerCPixel;
			for (int i = 0; i < rect.height; ++i) {
				for (int j = 0; j < rect.width * bytesPerCPixel; ++j) {
					int p = (i - 1 < 0 ? // first row?
								0 :
								(int) (0xff & buffer[(i - 1) * stride + j])) // pixel from prev row for next rows
							+ (j - bytesPerCPixel < 0 ? // first pixel in a row?
								0 :
								(int) (0xff & buffer[i * stride + j - bytesPerCPixel])) // prev pixel in a row
							- (j - bytesPerCPixel < 0 || i - 1 < 0 ? // not first row not first pixel in a row
								0 :
								(int) (0xff & buffer[(i - 1) * stride + j - bytesPerCPixel])); // prev by diagonal
					if (p < 0) {
						p = 0;
					}
					if (p > 0xff) {
						p = 0xff;
					}
					buffer[i * stride + j] += (byte) p;
				}
			}
			renderer.drawBytes(buffer, 0, rect.x, rect.y, rect.width, rect.height, true, true);
//			TranslateFrom8bppTo32bpp(buffer, 0, rect.width * rect.height);
			break;
		default:
			break;
		}
	}

	/**
	 * Read palette from reader
	 *
	 * @param paletteSize
	 * @param reader
	 * @param renderer
	 * @return
	 * @throws TransportException
	 */
	private int[] readPalette(int paletteSize, Transport.Reader reader, Renderer renderer) throws TransportException {
		/**
		 * When bytesPerPixel == 1 && paletteSize == 2 read 2 bytes of palette
		 * When bytesPerPixel == 1 && paletteSize != 2 - error
		 * When bytesPerPixel == 3 (4) read (paletteSize * 3) bytes of palette
		 * so use renderer.readPixelColor
		 */
		int[] palette = new int[paletteSize];
		for (int i = 0; i < palette.length; ++i) {
			palette[i] = renderer.readCompactPixelColor(reader, true);
		}
		return palette;
	}

	/**
	 * Reads compressed (expected length >= MIN_SIZE_TO_COMPRESS) or
	 * uncompressed data. When compressed decompresses it.
	 *
	 * @param expectedLength expected data length in bytes
	 * @param reader
	 * @return result data
	 * @throws TransportException
	 */
	private byte[] readTightData(int expectedLength, Transport.Reader reader) throws TransportException {
		if (expectedLength < MIN_SIZE_TO_COMPRESS) {
			byte [] buffer = ByteBuffer.getInstance().getBuffer(expectedLength);
			reader.readBytes(buffer, 0, expectedLength);
			return buffer;
		} else
			return readCompressedData(expectedLength, reader);
	}

	/**
	 * Reads compressed data length, then read compressed data into rawBuffer
     * and decompress data with expected length == length
     *
     * Note: returned data contains not only decompressed data but raw data at array tail
     * which need to be ignored. Use only first expectedLength bytes.
     *
	 * @param expectedLength expected data length
	 * @param reader
	 * @return decompressed data (length == expectedLength) / + followed raw data (ignore, please)
	 * @throws TransportException
	 */
	private byte[] readCompressedData(int expectedLength, Transport.Reader reader) throws TransportException {
		int rawDataLength = readCompactSize(reader);

		byte [] buffer = ByteBuffer.getInstance().getBuffer(expectedLength + rawDataLength);
		// read compressed (raw) data behind space allocated for decompressed data
		reader.readBytes(buffer, expectedLength, rawDataLength);
		if (null == decoders[decoderId]) {
			decoders[decoderId] = new Inflater();
		}
		Inflater decoder = decoders[decoderId];
		decoder.setInput(buffer, expectedLength, rawDataLength);
		try {
			decoder.inflate(buffer, 0, expectedLength);
		} catch (DataFormatException e) {
			logger.throwing("TightDecoder", "readCompressedData", e);
			throw new TransportException("cannot inflate tight compressed data", e);
		}
		return buffer;
	}

	private void processJpegType(Transport.Reader reader, Renderer renderer,
			FramebufferUpdateRectangle rect) throws TransportException {
		int jpegBufferLength = readCompactSize(reader);
		byte [] bytes = ByteBuffer.getInstance().getBuffer(jpegBufferLength);
		reader.readBytes(bytes, 0, jpegBufferLength);
		renderer.drawJpegImage(bytes, 0, jpegBufferLength, rect);
	}

	/**
	 * Read an integer from reader in compact representation (from 1 to 3 bytes).
	 * Highest bit of read byte set to 1 means next byte contains data.
	 * Lower 7 bit of each byte contains significant data. Max bytes = 3.
	 * Less significant bytes first order.
	 *
	 * @param reader
	 * @return int value
	 * @throws TransportException
	 */
	private int readCompactSize(Transport.Reader reader) throws TransportException {
		int b = reader.readUInt8();
		int size = b & 0x7F;
		if ((b & 0x80) != 0) {
			b = reader.readUInt8();
			size += (b & 0x7F) << 7;
			if ((b & 0x80) != 0) {
				size += reader.readUInt8() << 14;
			}
		}
		return size;
	}

	/**
	 * Flush (reset) zlib decoders when bits 3, 2, 1, 0 of compControl is set
	 * @param compControl
	 */
	private void resetDecoders(int compControl) {
		for (int i=0; i < DECODERS_NUM; ++i) {
			if ((compControl & 1) != 0 && decoders[i] != null) {
				decoders[i].reset();
			}
			compControl >>= 1;
		}

	}

	@Override
	public void reset() {
		decoders = new Inflater[DECODERS_NUM];
	}

}
