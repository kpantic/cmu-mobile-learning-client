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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.glavsoft.rfb.encoding.EncodingType;

/**
 * Decoders container class
 */
public class DecodersContainer {
	private static Map<EncodingType, Class<? extends Decoder>> knownDecoders =
		new HashMap<EncodingType, Class<? extends Decoder>>();
	static {
		knownDecoders.put(EncodingType.TIGHT, TightDecoder.class);
		knownDecoders.put(EncodingType.HEXTILE, HextileDecoder.class);
		knownDecoders.put(EncodingType.ZRLE, ZRLEDecoder.class);
		knownDecoders.put(EncodingType.ZLIB, ZlibDecoder.class);
		knownDecoders.put(EncodingType.RRE, RREDecoder.class);
		knownDecoders.put(EncodingType.COPY_RECT, CopyRectDecoder.class);
//		knownDecoders.put(EncodingType.RAW_ENCODING, RawDecoder.class);
	}
	private final Map<EncodingType, Decoder> decoders =
		new HashMap<EncodingType, Decoder>();

	public DecodersContainer() {
		addMandatoryDecoders();
	}

	private void addMandatoryDecoders() {
		decoders.put(EncodingType.RAW_ENCODING, RawDecoder.getInstance());
	}

	/**
	 * Init decoders for those encodings that's needed to be initialized.
	 *
	 * @param encodings encodings we need to handle
	 */
	public void initDecodersWhenNeeded(Collection<EncodingType> encodings) {
		for (EncodingType enc : encodings) {
			if (EncodingType.ordinaryEncodings.contains(enc) &&
					! decoders.containsKey(enc)) {
				try {
					decoders.put(enc, knownDecoders.get(enc).newInstance());
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public Decoder getDecoderByType(EncodingType type) {
		return decoders.get(type);
	}

	public void resetDecoders() {
		for (Decoder decoder : decoders.values()) {
			if (decoder != null) {
				decoder.reset();
			}
		}
	}

}
