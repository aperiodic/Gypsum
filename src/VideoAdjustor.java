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

public class VideoAdjustor extends JPanel implements ChangeListener, MouseListener {
	protected VideoMonitor vidMon;
	
	VideoAdjustor() {
		super();
		this.setBounds(0, 0, 740, 480);
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
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
		
		vidMon = new VideoMonitor(640, 480);
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
		
		this.add(thresholdSliderPanel);
	}
	
	public void stateChanged(ChangeEvent e) {
		JComponent theComponent = (JComponent) e.getSource();
		
		if ("contrastSlider".equals(theComponent.getName())) {
			JSlider cSlider = (JSlider) theComponent;
			vidMon.setContrast(cSlider.getValue());
		} else if ("thresholdSlider".equals(theComponent.getName())) {
			JSlider tSlider = (JSlider) theComponent;
			vidMon.setThreshold(tSlider.getValue());
		}
	}
	
	public void mousePressed(MouseEvent e) {
		if ("thresholdSlider".equals(e.getComponent().getName())) {
			vidMon.setThresholded(true);
		} else if ("video".equals(e.getComponent().getName())) {
			vidMon.setEdgeDetection(true);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if ("thresholdSlider".equals(e.getComponent().getName())) {
			vidMon.setThresholded(false);
		} else if ("video".equals(e.getComponent().getName())) {
			vidMon.setEdgeDetection(false);
		}
	}
	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
		
}
