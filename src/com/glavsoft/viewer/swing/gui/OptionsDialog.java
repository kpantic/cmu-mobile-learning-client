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

package com.glavsoft.viewer.swing.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;

import com.glavsoft.rfb.encoding.EncodingType;
import com.glavsoft.rfb.protocol.LocalPointer;
import com.glavsoft.rfb.protocol.ProtocolSettings;

/**
 * Options dialog
 */
@SuppressWarnings("serial")
public class OptionsDialog extends JDialog {

	private static final String SCALE_25 = "25";
	private static final String SCALE_50 = "50";
	private static final String SCALE_75 = "75";
	private static final String SCALE_90 = "90";
	private static final String SCALE_100 = "100";
	private static final String SCALE_125 = "125";
	private static final String SCALE_150 = "150";

	private JSlider jpegQuality;
	private JSlider compressionLevel;
	private JCheckBox viewOnlyCheckBox;
	private ProtocolSettings settings;
	private boolean isOkPressed = false;
	private JComboBox scaleCombo;
	private JCheckBox sharedSession;
	private RadioButtonSelectedState<LocalPointer> mouseCursorTrackSelected;
	private Map<LocalPointer, JRadioButton> mouseCursorTrackMap;
	private JCheckBox useCompressionLevel;
	private JCheckBox useJpegQuality;
	private JLabel jpegQualityPoorLabel;
	private JLabel jpegQualityBestLabel;
	private JLabel compressionLevelFastLabel;
	private JLabel compressionLevelBestLabel;
	private JCheckBox allowCopyRect;
	private JComboBox encodings;
	private JCheckBox disableClipboardTransfer;

	public OptionsDialog(JFrame owner) {
		super(owner, "Connection Options", true);
		final WindowAdapter onClose = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				isOkPressed = false;
				setVisible(false);
			}
		};
		addWindowListener(onClose);

		JPanel optionsPane = new JPanel(new GridLayout(0, 2));
		add(optionsPane, BorderLayout.CENTER);

		optionsPane.add(createLeftPane());
		optionsPane.add(createRightPane());

		addButtons(onClose);

		pack();
	}

	public void initControlsFromSettings(ProtocolSettings settings, boolean isOnConnect) {
		isOkPressed = false;
		this.settings = settings;

		viewOnlyCheckBox.setSelected(settings.isViewOnly());

//		String scaling = String.valueOf((int)(settings.scaling * 100));
//		int i = 0; boolean isNotSetScale = true;
//		while ( scaleCombo.getItemAt(i) != null) {
//			String item = (String)scaleCombo.getItemAt(i);
//			if (item.equals(scaling)) {
//				scaleCombo.setSelectedIndex(i);
//				isNotSetScale = false;
//				break;
//			}
//			++i;
//		}
//		if (isNotSetScale) {
//			scaleCombo.setSelectedItem(SCALE_100);
//		}

		int i = 0; boolean isNotSetEncoding = true;
		while ( encodings.getItemAt(i) != null) {
			EncodingType item = ((EncodingSelectItem)encodings.getItemAt(i)).type;
			if (item.equals(settings.getPreferredEncoding())) {
				encodings.setSelectedIndex(i);
				isNotSetEncoding = false;
				break;
			}
			++i;
		}
		if (isNotSetEncoding) {
			encodings.setSelectedItem(0);
		}

		sharedSession.setSelected(settings.isShared());
		sharedSession.setEnabled(isOnConnect);

		mouseCursorTrackMap.get(settings.getMouseCursorTrack()).setSelected(true);
		mouseCursorTrackSelected.setSelected(settings.getMouseCursorTrack());

		useCompressionLevel.setSelected(settings.getCompressionLevel() > 0);
		compressionLevel.setValue(Math.abs(settings.getCompressionLevel()));
		setCompressionLevelPaneEnable();

		useJpegQuality.setSelected(settings.getJpegQuality() > 0);
		jpegQuality.setValue(Math.abs(settings.getJpegQuality()));
		setJpegQualityPaneEnable();

		allowCopyRect.setSelected(settings.isAllowCopyRect());
		disableClipboardTransfer.setSelected( ! settings.isAllowClipboardTransfer());
}

	private void setSettingsFromControls() {
		settings.setViewOnly(viewOnlyCheckBox.isSelected());
//		try {
//			settings.scaling = Double.parseDouble((String)scaleCombo.getSelectedItem()) / 100.;
//		} catch (NumberFormatException nfe) {
//			settings.scaling = 1.;
//		}
		settings.setPreferredEncoding(((EncodingSelectItem)encodings.getSelectedItem()).type);

		settings.setSharedFlag(sharedSession.isSelected());
		settings.setMouseCursorTrack(mouseCursorTrackSelected.getSelected());

		settings.setCompressionLevel(compressionLevel.getValue());
		if ( ! useCompressionLevel.isSelected()) {
			settings.setCompressionLevel(- settings.getCompressionLevel());
		}
		settings.setJpegQuality(jpegQuality.getValue());
		if ( ! useJpegQuality.isSelected()) {
			settings.setJpegQuality(- settings.getJpegQuality());
		}
		settings.setAllowCopyRect(allowCopyRect.isSelected());
		settings.setAllowClipboardTransfer(! disableClipboardTransfer.isSelected());
	}

	private Component createLeftPane() {
		Box box = Box.createVerticalBox();
		box.setAlignmentX(LEFT_ALIGNMENT);

		box.add(createEncodingsPanel());

		box.add(Box.createVerticalGlue());
		return box;
	}

	private Component createRightPane() {
		Box box = Box.createVerticalBox();
		box.setAlignmentX(LEFT_ALIGNMENT);

		box.add(createRestrictionsPanel());
//		box.add(createDisplayPanel());
		box.add(createMouseCursorPanel());
//		box.add(createLocalShapePanel());

		sharedSession = new JCheckBox("Request shared session");
		sharedSession.setAlignmentX(LEFT_ALIGNMENT);
		box.add(sharedSession);

		box.add(Box.createVerticalGlue());
		return box;
	}

	private JPanel createRestrictionsPanel() {
		JPanel restrictionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		restrictionsPanel.setAlignmentX(LEFT_ALIGNMENT);
		restrictionsPanel.setBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(), "Restrictions"));

		Box restrictionsBox = Box.createVerticalBox();
		restrictionsBox.setAlignmentX(LEFT_ALIGNMENT);
		restrictionsPanel.add(restrictionsBox);
		viewOnlyCheckBox = new JCheckBox("View only (inputs ignored)");
		viewOnlyCheckBox.setAlignmentX(LEFT_ALIGNMENT);
		restrictionsBox.add(viewOnlyCheckBox);

		disableClipboardTransfer = new JCheckBox("Disable clipboard transfer");
		disableClipboardTransfer.setAlignmentX(LEFT_ALIGNMENT);
		restrictionsBox.add(disableClipboardTransfer);

		return restrictionsPanel;
	}

	private JPanel createEncodingsPanel() {
		JPanel encodingsPanel = new JPanel();
		encodingsPanel.setAlignmentX(LEFT_ALIGNMENT);
		encodingsPanel.setLayout(new BoxLayout(encodingsPanel, BoxLayout.Y_AXIS));
		encodingsPanel.setBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(), "Format and Encodings"));

		JPanel encPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		encPane.setAlignmentX(LEFT_ALIGNMENT);
		encPane.add(new JLabel("Preferred encoding: "));

		encodings = new JComboBox();
		encodings.addItem(new EncodingSelectItem(EncodingType.TIGHT));
		encodings.addItem(new EncodingSelectItem(EncodingType.HEXTILE));
//		encodings.addItem(new EncodingSelectItem(EncodingType.RRE));
//		encodings.addItem(new EncodingSelectItem(EncodingType.ZLIB));
		encodings.addItem(new EncodingSelectItem(EncodingType.ZRLE));
		encodings.addItem(new EncodingSelectItem(EncodingType.RAW_ENCODING));
		encPane.add(encodings);
		encodingsPanel.add(encPane);

//		JCheckBox colorDepth8BitCheckBox = new JCheckBox("Use 8-bit color");
//		colorDepth8BitCheckBox.setAlignmentX(LEFT_ALIGNMENT);
//		encodingsPanel.add(colorDepth8BitCheckBox);
		// TODO uncomment when ready

		addCompressionLevelPane(encodingsPanel);

		addJpegQualityLevelPane(encodingsPanel);

		allowCopyRect = new JCheckBox("Allow CopyRect encoding");
		allowCopyRect.setAlignmentX(LEFT_ALIGNMENT);
		encodingsPanel.add(allowCopyRect);

		return encodingsPanel;
	}

	private static class EncodingSelectItem {
		final EncodingType type;
		public EncodingSelectItem(EncodingType type) {
			this.type = type;
		}
		@Override
		public String toString() {
			return type.getName();
		}
	}

	private void addJpegQualityLevelPane(JPanel encodingsPanel) {
		useJpegQuality = new JCheckBox("Allow JPEG, set quality level:");
		useJpegQuality.setAlignmentX(LEFT_ALIGNMENT);
		encodingsPanel.add(useJpegQuality);

		JPanel jpegQualityPane = new JPanel();
		jpegQualityPane.setAlignmentX(LEFT_ALIGNMENT);
		jpegQualityPoorLabel = new JLabel("poor");
		jpegQualityPane.add(jpegQualityPoorLabel);
		jpegQuality = new JSlider(1, 9, 9);
		jpegQualityPane.add(jpegQuality);
		jpegQuality.setPaintTicks(true);
		jpegQuality.setMinorTickSpacing(1);
		jpegQuality.setMajorTickSpacing(1);
		jpegQuality.setPaintLabels(true);
		jpegQuality.setSnapToTicks(true);
		jpegQuality.setFont(
				jpegQuality.getFont().deriveFont((float) 8));
		jpegQualityBestLabel = new JLabel("best");
		jpegQualityPane.add(jpegQualityBestLabel);
		encodingsPanel.add(jpegQualityPane);

		jpegQualityPoorLabel.setFont(jpegQualityPoorLabel.getFont().deriveFont((float) 10));
		jpegQualityBestLabel.setFont(jpegQualityBestLabel.getFont().deriveFont((float) 10));

		useJpegQuality.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setJpegQualityPaneEnable();
			}
		});
		setJpegQualityPaneEnable();
	}

	protected void setJpegQualityPaneEnable() {
		setEnabled(useJpegQuality.isSelected(),
				jpegQuality, jpegQualityPoorLabel, jpegQualityBestLabel);
	}

	private void addCompressionLevelPane(JPanel encodingsPanel) {
		useCompressionLevel = new JCheckBox("Custom compression level:");
		useCompressionLevel.setAlignmentX(LEFT_ALIGNMENT);
		encodingsPanel.add(useCompressionLevel);

		JPanel compressionLevelPane = new JPanel();
		compressionLevelPane.setAlignmentX(LEFT_ALIGNMENT);
		compressionLevelFastLabel = new JLabel("fast");
		compressionLevelPane.add(compressionLevelFastLabel);
		compressionLevel = new JSlider(1, 9, 1);
		compressionLevelPane.add(compressionLevel);
		compressionLevel.setPaintTicks(true);
		compressionLevel.setMinorTickSpacing(1);
		compressionLevel.setMajorTickSpacing(1);
		compressionLevel.setPaintLabels(true);
		compressionLevel.setSnapToTicks(true);
		compressionLevel.setFont(compressionLevel.getFont().deriveFont((float) 8));
		compressionLevelBestLabel = new JLabel("best");
		compressionLevelPane.add(compressionLevelBestLabel);
		encodingsPanel.add(compressionLevelPane);

		compressionLevelFastLabel.setFont(compressionLevelFastLabel.getFont().deriveFont((float) 10));
		compressionLevelBestLabel.setFont(compressionLevelBestLabel.getFont().deriveFont((float) 10));

		useCompressionLevel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setEnabled(useCompressionLevel.isSelected(),
						compressionLevel, compressionLevelFastLabel, compressionLevelBestLabel);
			}
		});
		setCompressionLevelPaneEnable();
	}

	protected void setCompressionLevelPaneEnable() {
		setEnabled(useCompressionLevel.isSelected(),
				compressionLevel, compressionLevelFastLabel, compressionLevelBestLabel);
	}
	private void setEnabled(boolean isEnabled, JComponent ... comp) {
		for (JComponent c : comp) {
			c.setEnabled(isEnabled);
		}
	}

	private JPanel createDisplayPanel() {
		JPanel displayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		displayPanel.setBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(), "Display"));

		Box displayBox = Box.createVerticalBox();
		displayPanel.add(displayBox);

		displayBox.setAlignmentX(LEFT_ALIGNMENT);
		JPanel scalePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		scalePane.setAlignmentX(LEFT_ALIGNMENT);
		scalePane.add(new JLabel("Scale by: "));

		scaleCombo = new JComboBox();
//		scaleCombo.addItem(SCALE_25);
//		scaleCombo.addItem(SCALE_50);
//		scaleCombo.addItem(SCALE_75);
//		scaleCombo.addItem(SCALE_90);
		scaleCombo.addItem(SCALE_100);
//		scaleCombo.addItem(SCALE_125);
//		scaleCombo.addItem(SCALE_150);
		scalePane.add(scaleCombo);
		scalePane.add(new JLabel("%"));
		displayBox.add(scalePane);

//		JCheckBox deiconifyOnBellCheckBox = new JCheckBox("Deiconify on remote Bell event");
//		deiconifyOnBellCheckBox.setAlignmentX(LEFT_ALIGNMENT);
//		displayBox.add(deiconifyOnBellCheckBox);
		return displayPanel;
	}

	private JPanel createLocalShapePanel() {
		JPanel localCursorShapePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
//		localCursorShapePanel.setLayout(new BoxLayout(localCursorShapePanel, BoxLayout.Y_AXIS));
		localCursorShapePanel.setBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(), "Local cursor shape"));
		Box localCursorShapeBox = Box.createVerticalBox();
		localCursorShapePanel.add(localCursorShapeBox);

		JRadioButton dotCursorRadio = new JRadioButton("Dot cursor");
		JRadioButton smallDotCursorRadio = new JRadioButton("Small dot cursor");
		JRadioButton arrowCursorRadio = new JRadioButton("Default cursor");
		JRadioButton noCursorRadio = new JRadioButton("No local cursor");
		localCursorShapeBox.add(dotCursorRadio);
		localCursorShapeBox.add(smallDotCursorRadio);
		localCursorShapeBox.add(arrowCursorRadio);
		localCursorShapeBox.add(noCursorRadio);
		ButtonGroup localCursorButtonGroup = new ButtonGroup();
		localCursorButtonGroup.add(dotCursorRadio);
		localCursorButtonGroup.add(smallDotCursorRadio);
		localCursorButtonGroup.add(arrowCursorRadio);
		localCursorButtonGroup.add(noCursorRadio);
		return localCursorShapePanel;
	}

	private JPanel createMouseCursorPanel() {
		JPanel mouseCursorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		mouseCursorPanel.setBorder(
				BorderFactory.createTitledBorder(
						BorderFactory.createEtchedBorder(), "Mouse Cursor"));
		Box mouseCursorBox = Box.createVerticalBox();
		mouseCursorPanel.add(mouseCursorBox);

		ButtonGroup mouseCursorTrackGroup = new ButtonGroup();

		mouseCursorTrackSelected = new RadioButtonSelectedState<LocalPointer>();
		mouseCursorTrackMap = new HashMap<LocalPointer, JRadioButton>();

		addRadioButton("Track remote cursor locally", LocalPointer.ON,
				mouseCursorTrackSelected, mouseCursorTrackMap, mouseCursorBox,
				mouseCursorTrackGroup);
		addRadioButton("Let remote server deal with mouse cursor",
				LocalPointer.OFF,
				mouseCursorTrackSelected, mouseCursorTrackMap, mouseCursorBox,
				mouseCursorTrackGroup);
		addRadioButton("Don't show remote cursor", LocalPointer.HIDE,
				mouseCursorTrackSelected, mouseCursorTrackMap, mouseCursorBox,
				mouseCursorTrackGroup);
		return mouseCursorPanel;
	}

	private static class RadioButtonSelectedState<T> {
		private T state;

		public void setSelected(T state) {
			this.state = state;
		}

		public T getSelected() {
			return state;
		}

	}

	private <T> void addRadioButton(String text, final T state,
			final RadioButtonSelectedState<T> selected,
			Map<T, JRadioButton> state2buttonMap, JComponent component, ButtonGroup group) {
		JRadioButton radio = new JRadioButton(text);
		radio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selected.setSelected(state);
			}
		});
		component.add(radio);
		group.add(radio);
		state2buttonMap.put(state, radio);
	}

	private void addButtons(final WindowListener onClose) {
		JPanel buttonPanel = new JPanel();
		JButton loginButton = new JButton("Ok");
		buttonPanel.add(loginButton);
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isOkPressed = true;
				setSettingsFromControls();
				setVisible(false);
			}
		});

		JButton closeButton = new JButton("Cancel");
		buttonPanel.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onClose.windowClosing(null);
			}
		});
		add(buttonPanel, BorderLayout.SOUTH);
	}

	public boolean isOkPressed() {
		return isOkPressed;
	}

}
