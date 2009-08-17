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

public class Configuration extends JFrame implements ActionListener, ChangeListener, MouseListener, MouseMotionListener {
	private Gypsum app;
	private JPanel buttonPane, deck;
	private JPanel[] configCards;
	private JButton cancel, previous, next;
	private VideoMonitor vidMon;
	private Calibration cal; 
	private Point[] calPoints;
	private int numCalPoints, cpIndex;
	private boolean movingCalPoint;
	protected static int configWidth = 500;
	protected static int configHeight = 400;
	protected static int configTop = Toolkit.getDefaultToolkit().getScreenSize().height/2 - (configHeight/2) - 20;
	protected static int configLeft = Toolkit.getDefaultToolkit().getScreenSize().width/2 - (configWidth/2);
	protected int panel;
	protected Font titleFont, bodyFont;
	protected ResourceBundle strings;
	protected Properties config;
	
	public Configuration(Gypsum _app) {
		super("");
		setResizable(false);
		app = _app;
		strings = ResourceBundle.getBundle("strings", Locale.getDefault());
		setTitle(strings.getString("configIntroTitle"));

		
		calPoints = new Point[4];
		numCalPoints = 0;
		movingCalPoint = false;
		cpIndex = 0;
		
		// attempt to load lucida grande for the body font,
		// but if loading fails, use the generic sans serif
		bodyFont = new Font("Lucida Grande", Font.PLAIN, 14);
		if (bodyFont == null) {
			bodyFont = new Font("SansSerif", Font.PLAIN, 14);
		}
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		deck = new JPanel(new CardLayout());
		configCards = new JPanel[4];
		
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
		
		vidMon = app.newVideoMonitor(320, 240);
		vidMon.setName("vidmon");
		vidMon.addMouseListener(this);
		vidMon.addMouseMotionListener(this);
		vidMon.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		createConfigCard(0);
		
		app.attachMenu(this);
		
		this.add(deck);
		this.add(Box.createVerticalGlue());
		this.add(buttonPane);
		
		addWindowListener(new WindowAdapter() {
							public void windowClosing(WindowEvent e) {
								app.handleClosing(e);
							}
							public void windowClosed(WindowEvent e) {
								app.handleClosed(e);
							}
							public void windowActivated(WindowEvent e) {
								app.handleActivated(e);
							}
							public void windowDeactivated(WindowEvent e) {
								app.handleDeactivated(e);
							}
							public void windowOpened(WindowEvent e) {
								app.handleOpened(e);
							}
						  });
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
				numCalPoints = 0;
				calPoints = new Point[4];
				vidMon.setCalPoints(calPoints);
				next.setEnabled(false);
				cal = new Calibration(app, this, vidMon);
				toFront();
				
			} else if (panel == 3) {
				vidMon.calibrate(config, app.new fsWindowProperties());
				cal.setVisible(false);
				
				app.releaseVideoMonitor();
			}
			
			if (panel == configCards.length - 1) {
				next.setText("Finish");
				next.setActionCommand("finish");
			}
			
			if (configCards[panel] == null) {
				createConfigCard(panel);
			}
			
			CardLayout deckLayout = (CardLayout) deck.getLayout();
			deckLayout.next(deck);
			deck.remove(0);
			configCards[panel-1] = null;
			
		} else if ("previous".equals(e.getActionCommand())) {
			if (panel > 0) {
				panel--;
			}
			
			if (panel == 0) {
				previous.setEnabled(false);
				next.setEnabled(true);
			}
			
			if (panel == 1) {
				app.releaseVideoMonitor();
				configCards[2] = null;
				if (cal != null) {
					cal.setVisible(false);
				}
			}
			
			if (panel == 2) {
				numCalPoints = 0;
				calPoints = new Point[4];
				vidMon.setCalPoints(calPoints);
				next.setEnabled(false);
				cal.setVisible(true);
				toFront();
				next.setText("Next");
				next.setActionCommand("next");
			}
			
			if (configCards[panel] == null) {
				createConfigCard(panel);
			}
			
			CardLayout deckLayout = (CardLayout) deck.getLayout();
			deckLayout.next(deck);
			deck.remove(0);
			configCards[panel+1] = null;
			
		} else if ("finish".equals(e.getActionCommand())) {
			config.setProperty("configured", "yes");
			String userDataDir = app.getUserDataDirectory();
			String configPath = userDataDir + "/Gypsum.config";
			
			try {
				java.io.FileOutputStream configFile = new java.io.FileOutputStream(configPath);
				
				try {
					config.store(configFile, "");
					configFile.close();
				} catch (java.io.IOException ioe) {
					app.showError("An error ocurred while trying to write to the configuration file.\nPlease restart Gypsum", ioe);
				}
				
			} catch (java.io.FileNotFoundException fnf) {
				// should never happen, because if the config file couldn't be created,
				// we would have bailed by now.
			}
			
			this.setVisible(false);
			cal.setVisible(false);
			cal.dispose();
			cal = null;
			app.releaseVideoMonitor();
			vidMon = null;
			app.configurationFinished(config);
			
		} else if ("extendedL".equals(e.getActionCommand())) {
			next.setEnabled(true);
			config.setProperty("projectorMode", "extendedL");
			
		} else if ("extendedR".equals(e.getActionCommand())) {
			next.setEnabled(true);
			config.setProperty("projectorMode", "extendedR");
			
		} else if ("mirrored".equals(e.getActionCommand())) {
			next.setEnabled(true);
			config.setProperty("projectorMode", "mirrored");
			
		} else if ("cancel".equals(e.getActionCommand())) {
			setVisible(false);
			app.newLecture();
		}
	}
	
	public void stateChanged(ChangeEvent e) {
	}
	
	public void mousePressed(MouseEvent e) {
		if ("thresholdSlider".equals(e.getComponent().getName())) {
			vidMon.setThresholded(true);
		}
		
		if ("vidmon".equals(e.getComponent().getName()) && panel == 2) {
			if (numCalPoints < 4) {
				calPoints[numCalPoints] = new Point(e.getX(), e.getY());
				numCalPoints++;
				
				if (numCalPoints == 4) {
					next.setEnabled(true);
				}
				
				vidMon.setCalPoints(calPoints);
			} else if (numCalPoints == 4) {
				int mx = e.getX(); int my = e.getY();
				for (int i = 0; i < 4; i++) {
					if (Math.abs(mx - calPoints[i].x) < 4 && Math.abs(my - calPoints[i].y) < 4) {
						movingCalPoint = true;
						cpIndex = i;
						
						calPoints[i] = new Point(mx, my);
						vidMon.setCalPoints(calPoints);
						
						break;
					}
				}
			}
		}
	}
	
	
	public void mouseDragged(MouseEvent e) {
		if (movingCalPoint) {
			calPoints[cpIndex] = new Point(e.getX(), e.getY());
			vidMon.setCalPoints(calPoints);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if ("thresholdSlider".equals(e.getComponent().getName())) {
			//vidMon.setThresholded(false);
		}
		
		movingCalPoint = false;
		cpIndex = 0;
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	
	// -- A METRIC ASSLOAD OF BORING SWING CODE TO LAY OUT EACH CONFIGURATION CARD -- //
	// man do i hate swing
	
	public void createConfigCard(int index) {
		if (index == 0) {
			// -- FIRST CONFIGURATION CARD - INTRO MESSAGE -- //
			configCards[0] = new JPanel();
			configCards[0].setLayout(new BoxLayout(configCards[0], BoxLayout.Y_AXIS));
			
			java.net.URL crystalIconURL = Gypsum.class.getResource("images/configAppIcon.png");
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
			// -- SECOND CONFIGURATION CARD - PROJECTOR MODE -- //
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
			projectorButtonPane.setBorder(BorderFactory.createEmptyBorder(-10,20,0,20));
			projectorButtonPane.add(Box.createHorizontalGlue());
			
			// create vertical boxes for each mode button and label
			JPanel extendedLModePanel = new JPanel();
			extendedLModePanel.setLayout(new BoxLayout(extendedLModePanel, BoxLayout.Y_AXIS));
			
			java.net.URL extendedLIconURL = Gypsum.class.getResource("images/extendedLModeIcon.png");
			ImageIcon extendedLIcon = new ImageIcon(extendedLIconURL);
			JLabel extendedLLabel = new JLabel(extendedLIcon);
			extendedLLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			extendedLModePanel.add(extendedLLabel);
			
			JRadioButton extendedL = new JRadioButton(strings.getString("extendedLModeLabel"));
			extendedL.setAlignmentX(Component.CENTER_ALIGNMENT);
			extendedL.setActionCommand("extendedL");
			extendedL.addActionListener(this);
			
			projectorButtonGroup.add(extendedL);
			extendedLModePanel.add(extendedL);
			projectorButtonPane.add(extendedLModePanel);
			projectorButtonPane.add(Box.createHorizontalGlue());
			
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
			
			JPanel extendedRModePanel = new JPanel();
			extendedRModePanel.setLayout(new BoxLayout(extendedRModePanel, BoxLayout.Y_AXIS));
			
			java.net.URL extendedRIconURL = Gypsum.class.getResource("images/extendedRModeIcon.png");
			ImageIcon extendedRIcon = new ImageIcon(extendedRIconURL);
			JLabel extendedRLabel = new JLabel(extendedRIcon);
			extendedRLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			extendedRModePanel.add(extendedRLabel);
			
			JRadioButton extendedR = new JRadioButton(strings.getString("extendedRModeLabel"));
			extendedR.setAlignmentX(Component.CENTER_ALIGNMENT);
			extendedR.setActionCommand("extendedR");
			extendedR.addActionListener(this);
			
			projectorButtonGroup.add(extendedR);
			extendedRModePanel.add(extendedR);
			projectorButtonPane.add(extendedRModePanel);
			
			projectorButtonPane.add(Box.createHorizontalGlue());
			configCards[1].add(projectorButtonPane);
			
			if ("extendedL".equals(config.getProperty("projectorMode"))) {
				extendedL.setSelected(true);
				next.setEnabled(true);
			} else if ("mirrored".equals(config.getProperty("projectorMode"))) {
				mirroredRadioButton.setSelected(true);
				next.setEnabled(true);
			} else if ("extendedR".equals(config.getProperty("projectorMode"))) {
				extendedR.setSelected(true);
				next.setEnabled(true);
			}
			
			deck.add(configCards[1], "projectorCard");			
			
		/*} else if (index == 2) {
			// -- THIRD CONFIGURATION CARD - VIDEO ADJUSTMENT -- //
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
			videoPanel.add(app.getVideoMonitor());
			videoPanel.add(Box.createHorizontalStrut(13));
			
			JPanel thresholdSliderPanel = new JPanel();
			thresholdSliderPanel.setLayout(new BoxLayout(thresholdSliderPanel, BoxLayout.Y_AXIS));
			JSlider thresholdSlider = new JSlider(JSlider.VERTICAL, 0, 255, 120);
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
			
			if (config.getProperty("contrast") != null) {
				int contrast = java.lang.Integer.parseInt(config.getProperty("contrast"));
				contrastSlider.setValue(contrast);
				vidMon.setContrast(contrast);
				config.setProperty("contrast", "" + contrast);
				next.setEnabled(true);
			}
			
			if (config.getProperty("threshold") != null) {
				int threshold = java.lang.Integer.parseInt(config.getProperty("threshold"));
				thresholdSlider.setValue(threshold);
				vidMon.setThreshold(threshold);
				config.setProperty("threshold", "" + threshold);
				next.setEnabled(true);
			}
			
			deck.add(configCards[2], "imageAdjustCard");*/
		} else if (index == 2) {
			// -- THIRD CONFIGURATION CARD - CALIBRATION -- //
			configCards[2] = new JPanel();
			configCards[2].setLayout(new BoxLayout(configCards[2], BoxLayout.Y_AXIS));
			
			JLabel calibrationMessage = new JLabel(strings.getString("calibrationMessage"));
			calibrationMessage.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
			calibrationMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
			configCards[2].add(calibrationMessage);
			
			JPanel vidPanel = new JPanel();
			vidPanel.setLayout(new BoxLayout(vidPanel, BoxLayout.X_AXIS));
			vidPanel.add(Box.createHorizontalStrut(90));
			vidPanel.add(app.getVideoMonitor());
			vidPanel.add(Box.createHorizontalStrut(90));
			
			configCards[2].add(vidPanel);
			
			deck.add(configCards[2], "calibrationCard");
		} else if (index == 3) {
			// -- FOURTH CARD - FINISHED -- //
			configCards[3] = new JPanel();
			configCards[3].setLayout(new BoxLayout(configCards[3], BoxLayout.Y_AXIS));
			
			JLabel imageMessage = new JLabel(strings.getString("finishedMessage"));
			imageMessage.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
			imageMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
			configCards[3].add(imageMessage);
			
			deck.add(configCards[3], "finishedCard");
		}
	}
	
	
	
	
}
