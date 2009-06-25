//
//  ConfigPage1.java
//  Gypsum
//
//  Created by DLP on 6/9/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Properties;

import java.io.FileInputStream;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class Configuration extends JFrame implements ActionListener, ChangeListener, MouseListener {
	protected JPanel buttonPane, deck;
	protected JPanel[] configCards;
	protected JButton cancel, previous, next;
	protected VideoMonitor vidMon;
	protected static int configWidth = 500;
	protected static int configHeight = 400;
	protected static int configTop = Toolkit.getDefaultToolkit().getScreenSize().height/2 - (configHeight/2) - 20;
	protected static int configLeft = Toolkit.getDefaultToolkit().getScreenSize().width/2 - (configWidth/2);
	protected int panel;
	protected Font titleFont, bodyFont;
	protected ResourceBundle strings;
	protected Properties config;
	
	public Configuration() {
		super("");
		this.setResizable(false);
		strings = ResourceBundle.getBundle("strings", Locale.getDefault());
		setTitle(strings.getString("configIntroTitle"));
		
		// attempt to load lucida grande for the body font,
		// but if loading fails, use the generic sans serif
		bodyFont = new Font("Lucida Grande", Font.PLAIN, 14);
		if (bodyFont == null) {
			bodyFont = new Font("SansSerif", Font.PLAIN, 14);
		}
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		deck = new JPanel(new CardLayout());
		configCards = new JPanel[5];
		
		// build a pane to hold the three buttons at the bottom
		buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		cancel = new JButton(strings.getString("cancelButton"));
		cancel.setActionCommand("cancel");
		cancel.addActionListener(this);
		buttonPane.add(cancel);
		
		buttonPane.add(Box.createHorizontalGlue());
		
		previous = new JButton(strings.getString("previousButton"));
		previous.setActionCommand("previous");
		previous.addActionListener(this);
		previous.setEnabled(false);
		buttonPane.add(previous);
		
		next = new JButton(strings.getString("nextButton"));
		next.setActionCommand("next");
		next.addActionListener(this);
		buttonPane.add(next);
		
		createConfigCard(0);
		
		this.add(deck);
		this.add(Box.createVerticalGlue());
		this.add(buttonPane);
	}
	
	public void startConfiguration(Properties _config) {
		config = _config;
		panel = 0;
		
		this.getRootPane().setDefaultButton(next);
		
		this.pack();
		this.setBackground(new Color(232, 232, 232));
		this.setLocation(configLeft, configTop);
		this.setSize(configWidth, configHeight);
		this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if ("next".equals(e.getActionCommand())) {
			panel++;
			
			if (panel == 1) {
				previous.setEnabled(true);
				next.setEnabled(false);
				this.setTitle(strings.getString("configTitle"));
			} else if (panel == 2) {
				
			} else if (panel == configCards.length - 1) {
				// change "next" to "finish"
			}
			
			if (configCards[panel] == null) {
				createConfigCard(panel);
			}
			
			CardLayout deckLayout = (CardLayout) deck.getLayout();
			deckLayout.next(deck);
		} else if ("previous".equals(e.getActionCommand())) {
			if (panel > 0) {
				panel--;
			}
			
			if (panel == 0) {
				previous.setEnabled(false);
				next.setEnabled(true);
			}
			
			CardLayout deckLayout = (CardLayout) deck.getLayout();
			deckLayout.previous(deck);
		} else if ("extended".equals(e.getActionCommand())) {
			next.setEnabled(true);
			config.setProperty("projectorMode", "extended");
			
		} else if ("mirrored".equals(e.getActionCommand())) {
			next.setEnabled(true);
			config.setProperty("projectorMode", "mirrored");
			
		} else if ("cancel".equals(e.getActionCommand())) {
			
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		JComponent theComponent = (JComponent) e.getSource();
		
		if ("contrastSlider".equals(theComponent.getName())) {
			JSlider cSlider = (JSlider) theComponent;
			vidMon.setContrast(cSlider.getValue());
			config.setProperty("contrast", "" + cSlider.getValue());
		} else if ("thresholdSlider".equals(theComponent.getName())) {
			JSlider tSlider = (JSlider) theComponent;
			vidMon.setThreshold(tSlider.getValue());
			config.setProperty("threshold", "" + tSlider.getValue());
		}
	}
	
	public void mousePressed(MouseEvent e) {
		if ("thresholdSlider".equals(e.getComponent().getName())) {
			vidMon.setThresholded(true);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if ("thresholdSlider".equals(e.getComponent().getName())) {
			vidMon.setThresholded(false);
		}
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	
	public void createConfigCard(int index) {
		if (index == 0) {
			// -- FIRST CONFIGURATION CARD -- //
			configCards[0] = new JPanel();
			configCards[0].setLayout(new BoxLayout(configCards[0], BoxLayout.Y_AXIS));
			
			java.net.URL crystalIconURL = Gypsum.class.getResource("images/configCrystal.png");
			ImageIcon appIcon = new ImageIcon(crystalIconURL);
			JLabel iconLabel = new JLabel(appIcon);
			iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
			iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			configCards[0].add(iconLabel);
			
			JLabel introMessage = new JLabel(strings.getString("configIntroMessage"));
			introMessage.setFont(bodyFont);
			introMessage.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
			introMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
			configCards[0].add(introMessage);
			deck.add(configCards[0], "introCard");
			
		} else if (index == 1) {
			// -- SECOND CONFIGURATION CARD -- //
			configCards[1] = new JPanel();
			configCards[1].setLayout(new BoxLayout(configCards[1], BoxLayout.Y_AXIS));
			
			JLabel projectorMessage = new JLabel(strings.getString("configProjectorMessage"));
			projectorMessage.setFont(bodyFont);
			projectorMessage.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			projectorMessage.setAlignmentX(Component.CENTER_ALIGNMENT);		
			configCards[1].add(projectorMessage);
			
			ButtonGroup projectorButtonGroup = new ButtonGroup();
			
			// create a horizontal box to hold the row of projector mode buttons
			JPanel projectorButtonPane = new JPanel();
			projectorButtonPane.setLayout(new BoxLayout(projectorButtonPane, BoxLayout.X_AXIS));
			projectorButtonPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			projectorButtonPane.add(Box.createHorizontalGlue());
			
			// create vertical boxes for each mode button and label
			JPanel mirrorModePanel = new JPanel();
			mirrorModePanel.setLayout(new BoxLayout(mirrorModePanel, BoxLayout.Y_AXIS));
			
			java.net.URL mirrorIconURL = Gypsum.class.getResource("images/mirrorModeIcon.png");
			ImageIcon mirrorIcon = new ImageIcon(mirrorIconURL);
			JLabel mirroredLabel = new JLabel(mirrorIcon);
			mirroredLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			mirrorModePanel.add(mirroredLabel);
			
			JRadioButton mirroredRadioButton = new JRadioButton(strings.getString("mirroredModeLabel"));
			mirroredRadioButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			mirroredRadioButton.setActionCommand("mirrored");
			mirroredRadioButton.addActionListener(this);
			
			projectorButtonGroup.add(mirroredRadioButton);
			mirrorModePanel.add(mirroredRadioButton);
			projectorButtonPane.add(mirrorModePanel);
			
			projectorButtonPane.add(Box.createHorizontalGlue());
			
			JPanel extendedModePanel = new JPanel();
			extendedModePanel.setLayout(new BoxLayout(extendedModePanel, BoxLayout.Y_AXIS));
			
			java.net.URL extendedIconURL = Gypsum.class.getResource("images/extendedModeIcon.png");
			ImageIcon extendedIcon = new ImageIcon(extendedIconURL);
			JLabel extendedLabel = new JLabel(extendedIcon);
			extendedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			extendedModePanel.add(extendedLabel);
			
			JRadioButton extended = new JRadioButton(strings.getString("extendedModeLabel"));
			extended.setAlignmentX(Component.CENTER_ALIGNMENT);
			extended.setActionCommand("extended");
			extended.addActionListener(this);
			
			projectorButtonGroup.add(extended);
			extendedModePanel.add(extended);
			projectorButtonPane.add(extendedModePanel);
			
			projectorButtonPane.add(Box.createHorizontalGlue());
			configCards[1].add(projectorButtonPane);
			
			deck.add(configCards[1], "projectorCard");			
			
		} else if (index == 2) {
			// -- THIRD CONFIGURATION CARD -- //
			configCards[2] = new JPanel();
			configCards[2].setLayout(new BoxLayout(configCards[2], BoxLayout.Y_AXIS));
			
			JLabel imageAdjustMessage = new JLabel(strings.getString("configImageMessage"));
			imageAdjustMessage.setFont(bodyFont);
			imageAdjustMessage.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
			imageAdjustMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
			configCards[2].add(imageAdjustMessage);
			
			JPanel videoPanel = new JPanel();
			videoPanel.setLayout(new BoxLayout(videoPanel, BoxLayout.X_AXIS));
			videoPanel.add(Box.createHorizontalStrut(22));
			
			JPanel contrastSliderPanel = new JPanel();
			contrastSliderPanel.setLayout(new BoxLayout(contrastSliderPanel, BoxLayout.Y_AXIS));
			JSlider contrastSlider = new JSlider(JSlider.VERTICAL, -128, 128, 0);
			contrastSlider.setName("contrastSlider");
			contrastSlider.addChangeListener(this);
			contrastSliderPanel.add(contrastSlider);
			
			JLabel contrastLabel = new JLabel(strings.getString("contrastLabel"));
			contrastLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			contrastSliderPanel.add(contrastLabel);
			
			videoPanel.add(contrastSliderPanel);
			videoPanel.add(Box.createHorizontalStrut(13));
			
			vidMon = new VideoMonitor();
			vidMon.setAlignmentX(Component.CENTER_ALIGNMENT);
			videoPanel.add(vidMon);
			videoPanel.add(Box.createHorizontalStrut(13));
			
			JPanel thresholdSliderPanel = new JPanel();
			thresholdSliderPanel.setLayout(new BoxLayout(thresholdSliderPanel, BoxLayout.Y_AXIS));
			JSlider thresholdSlider = new JSlider(JSlider.VERTICAL, 0, 255, 150);
			thresholdSlider.setName("thresholdSlider");
			thresholdSlider.addChangeListener(this);
			thresholdSlider.addMouseListener(this);
			thresholdSliderPanel.add(thresholdSlider);
			
			JLabel thresholdLabel = new JLabel(strings.getString("thresholdLabel"));
			thresholdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			thresholdSliderPanel.add(thresholdLabel);
			
			videoPanel.add(thresholdSliderPanel);
			videoPanel.add(Box.createHorizontalStrut(22));
			configCards[2].add(videoPanel);
			
			deck.add(configCards[2], "imageAdjustCard");
		}
	}
	
	
	
	
}
