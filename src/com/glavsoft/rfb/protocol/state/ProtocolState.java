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

import java.util.logging.Logger;

import com.glavsoft.exceptions.AuthenticationFailedException;
import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.encoding.ServerInitMessage;
import com.glavsoft.rfb.protocol.ProtocolContext;
import com.glavsoft.transport.Transport;

abstract public class ProtocolState {
	protected ProtocolContext context;
	protected Logger logger;
	protected Transport.Reader reader;
	protected Transport.Writer writer;

	public ProtocolState(ProtocolContext context) {
		this.context = context;
		this.logger = context.getLogger();
		this.reader = context.getReader();
		this.writer = context.getWriter();

	}
	protected void changeStateTo(ProtocolState state) {
		context.changeStateTo(state);
	}
	public void handshake()
	throws TransportException, UnsupportedProtocolVersionException {
		throw new IllegalStateException();
	}
	public void negotiateAboutSecurityType()
	throws TransportException, UnsupportedSecurityTypeException {
		throw new IllegalStateException();
	}

	public void authenticate()
	throws TransportException, AuthenticationFailedException, FatalException, UnsupportedSecurityTypeException {
		throw new IllegalStateException();
	}

	public ServerInitMessage clientAndServerInit() throws TransportException {
		throw new IllegalStateException();
	};

}
