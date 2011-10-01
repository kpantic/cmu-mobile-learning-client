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

package com.glavsoft.rfb.protocol.state;

import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.rfb.protocol.ProtocolContext;

public class HandshakeState extends ProtocolState {

	private static final int PROTOCOL_STRING_LENGTH = 12;
	static final String PROTOCOL_STRING_3_3 = "RFB 003.003\n";
	static final String PROTOCOL_STRING_3_7 = "RFB 003.007\n";
	static final String PROTOCOL_STRING_3_8 = "RFB 003.008\n";

	public HandshakeState(ProtocolContext context) {
		super(context);
	}

	@Override
	public void handshake()
	throws TransportException, UnsupportedProtocolVersionException {
		String protocolName = reader.readString(PROTOCOL_STRING_LENGTH);
		logger.info("Protocol: " + protocolName);
		if (PROTOCOL_STRING_3_3.equals(protocolName)) {
			changeStateTo(new SecurityType33State(context));
			context.getSettings().setProtocolVersion("3.3");
		} else if (PROTOCOL_STRING_3_7.equals(protocolName)) {
			changeStateTo(new SecurityType37State(context));
			context.getSettings().setProtocolVersion("3.7");
		} else if (PROTOCOL_STRING_3_8.equals(protocolName)) {
			changeStateTo(new SecurityTypeState(context));
			context.getSettings().setProtocolVersion("3.8");
		} else
			throw new UnsupportedProtocolVersionException(
					"Unsupported protocol version: " + protocolName);
		writer.write(protocolName);
		logger.info("Protocol version: " + context.getSettings().getProtocolVersion());
	}

}
