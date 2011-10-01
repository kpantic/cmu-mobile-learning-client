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

package com.glavsoft.rfb.protocol;

import java.util.logging.Logger;

import com.glavsoft.exceptions.AuthenticationFailedException;
import com.glavsoft.exceptions.FatalException;
import com.glavsoft.exceptions.TransportException;
import com.glavsoft.exceptions.UnsupportedProtocolVersionException;
import com.glavsoft.exceptions.UnsupportedSecurityTypeException;
import com.glavsoft.rfb.IPasswordRetriever;
import com.glavsoft.rfb.client.FramebufferUpdateRequestMessage;
import com.glavsoft.rfb.client.SetEncodingsMessage;
import com.glavsoft.rfb.client.SetPixelFormatMessage;
import com.glavsoft.rfb.encoding.PixelFormat;
import com.glavsoft.rfb.encoding.ServerInitMessage;
import com.glavsoft.rfb.protocol.state.HandshakeState;
import com.glavsoft.rfb.protocol.state.ProtocolState;
import com.glavsoft.transport.Transport;

public class Protocol implements ProtocolContext {
	private ProtocolState state;
	private final Logger logger = Logger.getLogger("com.glavsoft.rfb.protocol");
	private final IPasswordRetriever passwordRetriever;
	private final ProtocolSettings settings;
	private ServerInitMessage serverInitMessage;
	private int fbWidth;
	private int fbHeight;
	private PixelFormat pixelFormat;
	private final Transport.Reader reader;
	private final Transport.Writer writer;

	public void process() throws UnsupportedProtocolVersionException,
			TransportException, UnsupportedSecurityTypeException,
			AuthenticationFailedException, FatalException {
		handshake();
		negotiateAboutSecurityType();
		authenticate();
		clientAndServerInit();
	}

	public Protocol(Transport.Reader reader, Transport.Writer writer,
			IPasswordRetriever passwordRetriever, ProtocolSettings settings) {
		this.reader = reader;
		this.writer = writer;
		this.passwordRetriever = passwordRetriever;
		this.settings = settings;
		state = new HandshakeState(this);
	}

	@Override
	public void changeStateTo(ProtocolState state) {
		this.state = state;
	}

	public void handshake() throws TransportException,
			UnsupportedProtocolVersionException {
		state.handshake();
	}

	public void negotiateAboutSecurityType()
			throws UnsupportedSecurityTypeException, TransportException {
		state.negotiateAboutSecurityType();
	}

	public void authenticate() throws TransportException,
			AuthenticationFailedException, FatalException, UnsupportedSecurityTypeException {
		state.authenticate();
	}

	public void clientAndServerInit() throws TransportException {
		serverInitMessage = state.clientAndServerInit();
		logger.fine(serverInitMessage.toString());
		pixelFormat = serverInitMessage.getPixelFormat();
		fbWidth = serverInitMessage.getFrameBufferWidth();
		fbHeight = serverInitMessage.getFrameBufferHeight();
	}

	public void set32bppPixelFormat() {
		PixelFormat.set32bppPixelFormat(pixelFormat);
	}

	@Override
	public PixelFormat getPixelFormat() {
		return pixelFormat;
	}

	public String getRemoteDesktopName() {
		return serverInitMessage.getName();
	}

	@Override
	public int getFbWidth() {
		return fbWidth;
	}

	public void setFbWidth(int fbWidth) {
		this.fbWidth = fbWidth;
	}

	@Override
	public int getFbHeight() {
		return fbHeight;
	}

	public void setFbHeight(int fbHeight) {
		this.fbHeight = fbHeight;
	}

	@Override
	public IPasswordRetriever getPasswordRetriever() {
		return passwordRetriever;
	}

	@Override
	public ProtocolSettings getSettings() {
		return settings;
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public Transport.Writer getWriter() {
		return writer;
	}

	@Override
	public Transport.Reader getReader() {
		return reader;
	}

	/**
	 * Following the server initialisation message it's up to the client to send
	 * whichever protocol messages it wants.  Typically it will send a
	 * SetPixelFormat message and a SetEncodings message, followed by a
	 * FramebufferUpdateRequest.  From then on the server will send
	 * FramebufferUpdate messages in response to the client's
	 * FramebufferUpdateRequest messages.  The client should send
	 * FramebufferUpdateRequest messages with incremental set to true when it has
	 * finished processing one FramebufferUpdate and is ready to process another.
	 * With a fast client, the rate at which FramebufferUpdateRequests are sent
	 * should be regulated to avoid hogging the network.
	 * @param senderQueue
	 * @param receiverQueue
	 */
	public void startNormalHandling(
			MessageQueue senderQueue) {
		senderQueue.put(new SetPixelFormatMessage(pixelFormat));
		logger.fine("sent: "+pixelFormat.toString());
		SetEncodingsMessage encodingsMessage = new SetEncodingsMessage(settings.encodings);
		senderQueue.put(encodingsMessage);
		logger.fine("sent: "+encodingsMessage.toString());
		// initial full screen framebuffer update request
		FramebufferUpdateRequestMessage frambufferUpdateRequestMessage =
			new FramebufferUpdateRequestMessage(0, 0, fbWidth, fbHeight, false);
		senderQueue.put(frambufferUpdateRequestMessage);
		logger.fine("sent: " + frambufferUpdateRequestMessage);
	}

}
