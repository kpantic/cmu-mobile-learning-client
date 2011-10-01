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

import javax.swing.JApplet;

import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.LocalPointer;
import com.glavsoft.rfb.protocol.ProtocolSettings;
import com.glavsoft.utils.Strings;
import com.glavsoft.viewer.Viewer;
import com.glavsoft.viewer.cli.Parser;

public class ParametersHandler {
	public static class ConnectionParams {
		public String hostName;
		public int portNumber = Viewer.DEFAULT_PORT;
		public boolean isHostNameEmpty() {
			return Strings.isTrimmedEmpty(hostName);
		}
		void parsePortNumber(String port) {
			try {
				portNumber = Integer.parseInt(port);
			} catch (NumberFormatException e) { /*nop*/ }
		}
	}

	public static boolean showControls;
	public static boolean isSeparateFrame;

	public static void completeParserOptions(Parser parser) {
		parser.addOption(Viewer.ARG_HELP, null, "Print this help");
		parser.addOption(Viewer.ARG_HOST, null, "Server host name");
		parser.addOption(Viewer.ARG_PORT, "5900", "Port number");
		parser.addOption(Viewer.ARG_PASSWORD, null, "Password to the server");
		parser.addOption(Viewer.ARG_SHOW_CONTROLS, null, "Set to \"No\" if you want to get rid of that " +
				"button panel at the top. Default: \"Yes\"");
		parser.addOption(Viewer.ARG_VIEW_ONLY, null, "When set to \"Yes\", then all keyboard and mouse " +
				"events in the desktop window will be silently ignored and will not be passed " +
				"to the remote side. Default: \"No\"");
		parser.addOption(Viewer.ARG_SHARE_DESKTOP, null, "Share the connection with other clients " +
				"on the same VNC server. The exact behaviour in each case depends on the server " +
				"configuration. Default: \"Yes\"");
		parser.addOption(Viewer.ARG_ALLOW_COPY_RECT, null, "The \"CopyRect\" encoding saves bandwidth " +
				"and drawing time when parts of the remote screen are moving around. " +
				"Most likely, you don't want to change this setting. Default: \"Yes\"");
		parser.addOption(Viewer.ARG_ENCODING, null, "The preferred encoding. Possible values: \"Tight\", " +
				"\"Hextile\", \"ZRLE\", and \"Raw\". Default: \"Tight\"");
		parser.addOption(Viewer.ARG_COMPRESSION_LEVEL, null, "Use specified compression level for " +
				"\"Tight\" and \"Zlib\" encodings. Values: 1-9. Level 1 uses minimum of CPU " +
				"time on the server but achieves weak compression ratios. Level 9 offers best " +
				"compression but may be slow");
		parser.addOption(Viewer.ARG_JPEG_IMAGE_QUALITY, null, "Use the specified image quality level " +
				"in \"Tight\" encoding. Values: 1-9. When not specified, the server will not use " +
				"lossy JPEG compression in \"Tight\" encoding");
		parser.addOption(Viewer.ARG_LOCAL_POINTER, null, "Possible values: on/yes/true (draw pointer locally), off/no/false (let server draw pointer), hide). " +
		"Default: \"On\"");
		parser.addOption(Viewer.ARG_COLOR_DEPTH, null, "Possible values: 6, 8, 16, 24, 32 (equals to 24). " +
				"Bits per pixel color format. Only 24/32 is supported now");
		parser.addOption(Viewer.ARG_SCALING_FACTOR, null, "Scale local representation of the remote desktop. " +
				"The value is interpreted as scaling factor in percents. The default value of 100% " +
				"corresponds to the original framebuffer size.");
	}

	public static void completeSettingsFromCLI(Parser parser, ConnectionParams connectionParams, ProtocolSettings settings) {
		completeSettings(parser.getValueFor(Viewer.ARG_HOST), parser.getValueFor(Viewer.ARG_PORT),
				parser.getValueFor(Viewer.ARG_SHOW_CONTROLS), parser.getValueFor(Viewer.ARG_VIEW_ONLY),
				parser.getValueFor(Viewer.ARG_ALLOW_COPY_RECT), parser.getValueFor(Viewer.ARG_SHARE_DESKTOP),
				parser.getValueFor(Viewer.ARG_ENCODING), parser.getValueFor(Viewer.ARG_COMPRESSION_LEVEL),
				parser.getValueFor(Viewer.ARG_JPEG_IMAGE_QUALITY), parser.getValueFor(Viewer.ARG_COLOR_DEPTH),
				parser.getValueFor(Viewer.ARG_SCALING_FACTOR), parser.getValueFor(Viewer.ARG_LOCAL_POINTER),
				connectionParams, settings);
		// when hostName == a.b.c.d:3 where :3 is display num (X Window) we need add display num to port number
		if ( ! Strings.isTrimmedEmpty(connectionParams.hostName)) {
			splitConnectionParams(connectionParams, connectionParams.hostName);
		}
		if (parser.isSetPlainOptions()) {
			splitConnectionParams(connectionParams, parser.getPlainOptionAt(0));
			if (parser.getPlainOptionsNumber() > 1) {
				connectionParams.parsePortNumber(parser.getPlainOptionAt(1));
			}
		}
	}


	/**
	 * Split host string into hostName, display number and port number and set ConnectionParans.
	 * a.b.c.d:3:5000 -> hostName == a.b.c.d, portNumber == 5003 (3 + 5000)
	 * a.b.c.d::5000 -> hostName == a.b.c.d, portNumber == 5000
	 * a.b.c.d:3 -> hostName == a.b.c.d, portNumber == 5903 (3 + 5900/default/)
	 *
	 * @param connectionParams
	 * @param host
	 */
	public static void splitConnectionParams(final ConnectionParams connectionParams, String host) {
		int displayNum = 0;
		int indexOfColon = host.indexOf(':');
		if (indexOfColon > 0) {
			String[] splited = host.split(":");
			connectionParams.hostName = splited[0];
			if (splited.length > 1) {
				try {
					displayNum = Integer.parseInt(splited[1]);
				} catch (NumberFormatException e) { /*nop*/ }
			}
			if (splited.length > 2) {
				connectionParams.parsePortNumber(splited[2]);
			}
			connectionParams.portNumber+= displayNum;
		} else {
			connectionParams.hostName = host;
		}
	}

	private static void completeSettings(String hostName, String portNumber,
			String showControlsParam, String viewOnlyParam, String allowCopyRectParam,
			String shareDesktopParam, String encodingParam, String compressionLevelParam,
			String jpegQualityParam, String colorDepthParam, String scaleFactorParam,
			String localPointerParam,
			ConnectionParams connectionParams, ProtocolSettings settings) {
		connectionParams.hostName = hostName;
		try {
			connectionParams.portNumber = Integer.parseInt(portNumber);
		} catch (NumberFormatException e) { /* nop */ }

		showControls = parseBooleanOrDefault(showControlsParam, true);
		settings.setViewOnly(parseBooleanOrDefault(viewOnlyParam, false));
	    settings.setAllowCopyRect(parseBooleanOrDefault(allowCopyRectParam, true));
		settings.setSharedFlag(parseBooleanOrDefault(shareDesktopParam, true));
		if (EncodingType.TIGHT.getName().equalsIgnoreCase(encodingParam)) {
			settings.setPreferredEncoding(EncodingType.TIGHT);
		}
		if (EncodingType.HEXTILE.getName().equalsIgnoreCase(encodingParam)) {
			settings.setPreferredEncoding(EncodingType.HEXTILE);
		}
		if (EncodingType.ZRLE.getName().equalsIgnoreCase(encodingParam)) {
			settings.setPreferredEncoding(EncodingType.ZRLE);
		}
		if (EncodingType.RAW_ENCODING.getName().equalsIgnoreCase(encodingParam)) {
			settings.setPreferredEncoding(EncodingType.RAW_ENCODING);
		}
		try {
			int compLevel = Integer.parseInt(compressionLevelParam);
			if (compLevel > 0 && compLevel <= 9) {
				settings.setCompressionLevel(compLevel);
			}
		} catch (NumberFormatException e) { /* nop */ }
		try {
			int jpegQuality = Integer.parseInt(jpegQualityParam);
			if (jpegQuality > 0 && jpegQuality <= 9) {
				settings.setJpegQuality(jpegQuality);
			}
		} catch (NumberFormatException e) { /* nop */ }
		try {
			int colorDepth = Integer.parseInt(colorDepthParam);
			switch (colorDepth) {
			case 6: case 8: case 16: case 32:
				// nop - use colorDepth as is
				break;
			default:
				colorDepth = 32;
				break;
			}
			// TODO set color depth
		} catch (NumberFormatException e) { /* nop */ }
		if (scaleFactorParam != null) {
			try {
				double scalingFactor = Double.parseDouble(scaleFactorParam);
				// 1..1000?
				if (scalingFactor >= 1 && scalingFactor <= 1000) {
					settings.setScaling(scalingFactor);
				}
			} catch (NumberFormatException e) { /* nop */ }
		}

		if ("on".equalsIgnoreCase(localPointerParam) ||
			"true".equalsIgnoreCase(localPointerParam) ||
			"yes".equalsIgnoreCase(localPointerParam)) {
				settings.setMouseCursorTrack(LocalPointer.ON);
		}
		if ("off".equalsIgnoreCase(localPointerParam) ||
			"no".equalsIgnoreCase(localPointerParam) ||
			"false".equalsIgnoreCase(localPointerParam)) {
				settings.setMouseCursorTrack(LocalPointer.OFF);
		}
		if ("hide".equalsIgnoreCase(localPointerParam) ||
			"hidden".equalsIgnoreCase(localPointerParam)) {
				settings.setMouseCursorTrack(LocalPointer.HIDE);
		}
	}

	static boolean parseBooleanOrDefault(String param, boolean defaultValue) {
		return defaultValue ?
				! ("no".equalsIgnoreCase(param) || "false".equalsIgnoreCase(param)) :
				"yes".equalsIgnoreCase(param) || "true".equalsIgnoreCase(param);
	}

	public static void completeSettingsFromApplet(JApplet applet,
			ConnectionParams connectionParams, ProtocolSettings settings) {

		String host = applet.getParameter(Viewer.ARG_HOST);
		if (Strings.isTrimmedEmpty(host)) {
			host = applet.getCodeBase().getHost();
		}
		completeSettings(host,
				applet.getParameter(Viewer.ARG_PORT),
				applet.getParameter(Viewer.ARG_SHOW_CONTROLS),
				applet.getParameter(Viewer.ARG_VIEW_ONLY),
				applet.getParameter(Viewer.ARG_ALLOW_COPY_RECT),
				applet.getParameter(Viewer.ARG_SHARE_DESKTOP),
				applet.getParameter(Viewer.ARG_ENCODING),
				applet.getParameter(Viewer.ARG_COMPRESSION_LEVEL),
				applet.getParameter(Viewer.ARG_JPEG_IMAGE_QUALITY),
				applet.getParameter(Viewer.ARG_COLOR_DEPTH),
				applet.getParameter(Viewer.ARG_SCALING_FACTOR),
				applet.getParameter(Viewer.ARG_LOCAL_POINTER),
				connectionParams, settings);
		isSeparateFrame = parseBooleanOrDefault(applet.getParameter(Viewer.ARG_OPEN_NEW_WINDOW), true);
	}


}
