//
//  VideoAdjustor.java
//  Gypsum
//
//  Created by DLP on 6/26/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import java.util.Properties;

public class VideoAdjustor extends JFrame implements ChangeListener, MouseListener {
	protected VideoMonitor vidMon;
	protected Properties config;
	protected Gypsum app;
	
	VideoAdjustor(Gypsum theApp, Properties cfg, String theTitle) {
		super("");
		setTitle(theTitle);
		
		app = theApp;
		config = cfg;
		
		this.setBounds(0, 0, 740, 502);
		this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
		
		JPanel contrastSliderPanel = new JPanel();
		contrastSliderPanel.setLayout(new BoxLayout(contrastSliderPanel, BoxLayout.Y_AXIS));
		JSlider contrastSlider = new JSlider(JSlider.VERTICAL, -128, 128, 0);
		contrastSlider.setName("contrastSlider");
		contrastSlider.addChangeListener(this);
		contrastSliderPanel.add(contrastSlider);
		
		JLabel contrastLabel = new JLabel("Contrast");
		contrastLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		contrastSliderPanel.add(contrastLabel);
		
		this.add(contrastSliderPanel);
		
		if (app.monitoring()) {
			vidMon = app.getVideoMonitor();
		} else {
			vidMon = app.newVideoMonitor(640, 480);
		}
		
		vidMon.setName("video");
		vidMon.addMouseListener(this);
		this.add(vidMon);
		
		JPanel thresholdSliderPanel = new JPanel();
		thresholdSliderPanel.setLayout(new BoxLayout(thresholdSliderPanel, BoxLayout.Y_AXIS));
		JSlider thresholdSlider = new JSlider(JSlider.VERTICAL, 0, 255, 150);
		thresholdSlider.setName("thresholdSlider");
		thresholdSlider.addChangeListener(this);
		thresholdSlider.addMouseListener(this);
		thresholdSliderPanel.add(thresholdSlider);
		
		JLabel thresholdLabel = new JLabel("Threshold");
		thresholdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		thresholdSliderPanel.add(thresholdLabel);
		
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
		
		this.add(thresholdSliderPanel);
		
		
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
		
}
