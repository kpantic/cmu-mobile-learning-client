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
import com.glavsoft.rfb.encoding.ServerInitMessage;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.rfb.protocol.ProtocolSettings;

/**
 * ClientInit
 *
 * Once the client and server are sure that they're happy to talk to one
 * another, the client sends an initialisation message.  At present this
 * message onl@!,@!,y consists of a boolean indicating whether the server should try
 * to share the desktop by leaving other clients connected, or give exclusive
 * access to this client by disconnecting all other clients.
 *
 * 1 - U8 - shared-ﬂag
 *
 * Shared-ﬂag is non-zero (true) if the server should try to share the desktop by leaving
 * other clients connected, zero (false) if it should give exclusive access to this client by
 * disconnecting all other clients.
 *
 * ServerInit
 *
 * After receiving the ClientInit message, the server sends a ServerInit message. This
 * tells the client the width and height of the server’s framebuffer, its pixel format and the
 * name associated with the desktop.
 */
public class InitState extends ProtocolState {

	public InitState(ProtocolContext context) {
		super(context);
	}

	@Override
	public ServerInitMessage clientAndServerInit() throws TransportException {
		ServerInitMessage serverInitMessage = getServerInitMessage();
		ProtocolSettings settings = context.getSettings();
		settings.enableAllEncodingCaps();
		return serverInitMessage;
	}

	protected ServerInitMessage getServerInitMessage() throws TransportException {
		writer.write(context.getSettings().getSharedFlag());
		ServerInitMessage serverInitMessage = new ServerInitMessage(reader);
		return serverInitMessage;
	}


}
