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

public class Configuration extends JFrame implements ActionListener {
	protected JButton cancel, previous, next;
	protected JPanel buttonPane, deck;
	protected JPanel[] configCards;
	protected static int configWidth = 500;
	protected static int configHeight = 400;
	protected static int configTop = Toolkit.getDefaultToolkit().getScreenSize().height/2 - 250;
	protected static int configLeft = Toolkit.getDefaultToolkit().getScreenSize().width/2 - 250;
	protected int panel;
	protected Font titleFont, bodyFont;
	protected ResourceBundle strings;
	protected Properties config;
	
	public Configuration() {
		super("");
		this.setResizable(false);
		strings = ResourceBundle.getBundle("strings", Locale.getDefault());
		setTitle(strings.getString("configIntroTitle"));
		
		deck = new JPanel(new CardLayout());
		configCards = new JPanel[5];
		
		// attempt to load lucida grande for the body font,
		// but if loading fails, use the generic sans serif
		bodyFont = new Font("Lucida Grande", Font.PLAIN, 14);
		if (bodyFont == null) {
			bodyFont = new Font("SansSerif", Font.PLAIN, 14);
		}
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
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
	
	public void actionPerformed(ActionEvent e) {
		if ("next".equals(e.getActionCommand())) {
			
			panel++;
			
			if (panel == 1) {
				previous.setEnabled(true);
			}
			
			if (configCards[panel] == null) {
				createConfigCard(panel);
			}
			
			CardLayout deckLayout = (CardLayout) deck.getLayout();
			deckLayout.next(deck);
		} else if ("cancel".equals(e.getActionCommand())) {
			
		}
	}
	
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
			
			// create a horzintal box to hold the row of projector mode buttons
			JPanel projectorButtonPane = new JPanel();
			projectorButtonPane.setLayout(new BoxLayout(projectorButtonPane, BoxLayout.X_AXIS));
			projectorButtonPane.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
			projectorButtonPane.add(Box.createHorizontalGlue());
			
			// create vertical boxes for each mode button and label
			JPanel mirrorModePanel = new JPanel();
			mirrorModePanel.setLayout(new BoxLayout(mirrorModePanel, BoxLayout.Y_AXIS));
			
			java.net.URL mirrorIconURL = Gypsum.class.getResource("images/mirrorModeIcon.png");
			ImageIcon mirrorIcon = new ImageIcon(mirrorIconURL);
			JButton mirrorModeButton = new JButton(mirrorIcon);
			mirrorModeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			mirrorModePanel.add(mirrorModeButton);
			
			JLabel mirrorModeLabel = new JLabel(strings.getString("mirrorModeLabel"));
			mirrorModeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			mirrorModePanel.add(mirrorModeLabel);
			projectorButtonPane.add(mirrorModePanel);
			
			projectorButtonPane.add(Box.createHorizontalGlue());
			
			JPanel extendedModePanel = new JPanel();
			extendedModePanel.setLayout(new BoxLayout(extendedModePanel, BoxLayout.Y_AXIS));
			
			java.net.URL extendedIconURL = Gypsum.class.getResource("images/extendedModeIcon.png");
			ImageIcon extendedIcon = new ImageIcon(extendedIconURL);
			JButton extendedModeButton = new JButton(extendedIcon);
			extendedModeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			extendedModePanel.add(extendedModeButton);
			
			JLabel extendedModeLabel = new JLabel(strings.getString("extendedModeLabel"));
			extendedModeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			extendedModePanel.add(extendedModeLabel);
			projectorButtonPane.add(extendedModePanel);
			
			projectorButtonPane.add(Box.createHorizontalGlue());
			configCards[1].add(projectorButtonPane);
			
			deck.add(configCards[1], "projectorCard");			
		}
	}
	
	public void startConfiguration() {
		panel = 0;
		
		this.getRootPane().setDefaultButton(next);
		
		this.pack();
		this.setBackground(new Color(232, 232, 232));
		this.setLocation(configLeft, configTop);
		this.setSize(configWidth, configHeight);
		this.setVisible(true);
	}
}
