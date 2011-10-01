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

package com.glavsoft.transport;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import com.glavsoft.exceptions.ClosedConnectionException;
import com.glavsoft.exceptions.TransportException;

public class Transport {
	private static final Logger logger = Logger.getLogger("com.glavsoft.transport");
	private final static Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

	public static class Reader {
		private DataInputStream is;

		public Reader(InputStream is) {
			this.is = new DataInputStream(new BufferedInputStream(is));
		}

		public byte readByte() throws TransportException {
			try {
				byte readByte = is.readByte();
				return readByte;
			} catch (EOFException e) {
				ClosedConnectionException closedConnectionException = new ClosedConnectionException(
						e);
				logger.throwing(this.getClass().getName(),
						"public byte readByte()", closedConnectionException);
				throw closedConnectionException;
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot read byte", e);
				logger.throwing(this.getClass().getName(),
						"public byte readByte()", transportException);
				throw transportException;
			}

		}

		public int readUInt8() throws TransportException {
			return (readByte() & 0x0ff);
		}

		public int readUInt16() throws TransportException {
			return readInt16() & 0x0ffff;
		}

		public short readInt16() throws TransportException {
			try {
				short readShort = is.readShort();
				return readShort;
			} catch (EOFException e) {
				ClosedConnectionException closedConnectionException = new ClosedConnectionException(
						e);
				logger.throwing(this.getClass().getName(),
						"public short readInt16()", closedConnectionException);
				throw closedConnectionException;
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot read int16", e);
				logger.throwing(this.getClass().getName(),
						"public short readInt16()", transportException);
				throw transportException;
			}
		}

		public long readUInt32() throws TransportException {
			return readInt32() & 0xffffffffL;
		}

		public int readInt32() throws TransportException {
			try {
				int readInt = is.readInt();
				return readInt;
			} catch (EOFException e) {
				ClosedConnectionException closedConnectionException = new ClosedConnectionException(
						e);
				logger.throwing(this.getClass().getName(),
						"public int readInt32()", closedConnectionException);
				throw closedConnectionException;
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot read int16", e);
				logger.throwing(this.getClass().getName(),
						"public int readInt32()", transportException);
				throw transportException;
			}
		}

		/**
		 * Read string by it length
		 *
		 * @return String read
		 */
		public String readString(int length) throws TransportException {
			String stringReceived = new String(readBytes(length), ISO_8859_1);
			return stringReceived;
		}

		/**
		 * Read 32-bit string length and then string themself by it length
		 *
		 * @return String read
		 * @throws TransportException
		 */
		public String readString() throws TransportException {
			// unset most sighificant (sign) bit 'cause InputStream#readFully
			// reads
			// [int] length bytes from stream. Change when realy need read sthing more
			// than
			// 2147483647 bytes lenght
			int length = readInt32() & Integer.MAX_VALUE;
			return readString(length);
		}

		public byte[] readBytes(int length) throws TransportException {
			byte b[] = new byte[length];
			return readBytes(b, 0, length);
		}

		public byte[] readBytes(byte[] b, int offset, int length) throws TransportException {
			try {
				is.readFully(b, offset, length);
				return b;
			} catch (EOFException e) {
				ClosedConnectionException closedConnectionException = new ClosedConnectionException(
						e);
				logger.throwing(this.getClass().getName(),
						"public byte[] readBytes(int length)",
						closedConnectionException);
				throw closedConnectionException;
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot read " + length + " bytes array", e);
				logger.throwing(this.getClass().getName(),
						"public byte[] readBytes(int length)",
						transportException);
				throw transportException;
			}
		}
	}

	public static class Writer {
		private DataOutputStream os;

		public Writer(OutputStream os) {
			this.os = new DataOutputStream(os);
		}

		public void flush() throws TransportException {
			try {
				os.flush();
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot flush output stream", e);
				logger.throwing(this.getClass().getName(),
						"public void flush()", transportException);
				throw transportException;
			}
		}

		public void writeByte(int b) throws TransportException {
			write((byte) (b & 0xff));
		}

		public void write(byte b) throws TransportException {
			try {
				os.writeByte(b);
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot write byte", e);
				logger.throwing(this.getClass().getName(),
						"public void write(byte b)", transportException);
				throw transportException;
			}
		}

		public void writeInt16(int sh) throws TransportException {
			write((short) (sh & 0xffff));
		}

		public void write(short sh) throws TransportException {
			try {
				os.writeShort(sh);
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot write short", e);
				logger.throwing(this.getClass().getName(),
						"public void write(short sh)", transportException);
				throw transportException;
			}
		}

		public void writeInt32(int i) throws TransportException {
			write(i);
		}

		public void write(int i) throws TransportException {
			try {
				os.writeInt(i);
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot write int", e);
				logger.throwing(this.getClass().getName(),
						"public void write(int i)", transportException);
				throw transportException;
			}
		}

		public void write(byte[] b) throws TransportException {
			write(b, 0, b.length);
		}

		/** Writes string as byte array (ISO_8859_1 encoded)
		 * (No any length prefix, string characters only!)
		 */
		public void write(String s) throws TransportException {
			write(s.getBytes(ISO_8859_1));
		}

		public void write(byte[] b, int length) throws TransportException {
			write(b, 0, length);
		}

		public void write(byte[] b, int offset, int length)
				throws TransportException {
			try {
				os.write(b, offset, length <= b.length ? length : b.length);
			} catch (IOException e) {
				TransportException transportException = new TransportException(
						"Cannot write " + length + " bytes", e);
				logger.throwing(this.getClass().getName(),
						"public void write(byte[] b, int offset, int length)",
						transportException);
				throw transportException;
			}
		}
	}
}
