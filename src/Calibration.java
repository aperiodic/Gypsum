//
//  Calibration.java
//  Gypsum
//
//  Created by DLP on 7/1/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class Calibration extends JFrame {
	protected Gypsum gypsum;
	protected Configuration configuration;
	protected VideoMonitor vidMon;
	protected int stage;
	public boolean shouldCalibrate;
	
	public Calibration (Gypsum gyp, Configuration conf, VideoMonitor vid) {
		gypsum = gyp;
		configuration = conf;
		vidMon = vid;
		
		stage = 0;
		shouldCalibrate = false;
		
		Gypsum.fsWindowProperties fsw = gypsum.new fsWindowProperties();
		setUndecorated(true);
		setBounds(fsw.x, fsw.y, fsw.width, fsw.height);
		setBackground(new Color(0, 0, 0));	
		repaint();
		setVisible(true);
	}
	
	public void paint(Graphics g) {
		
		Gypsum.fsWindowProperties fswp = gypsum.new fsWindowProperties();
		
		if (stage == 0) {
			g.setColor(new Color(255, 255, 0));
			
			int partitions = 3;
			int unit = fswp.height/partitions;
			int leftMargin = unit + (fswp.width-fswp.height)/2;
			int rectWidth = 24;
			
			g.fillRect(leftMargin - rectWidth/2, unit - rectWidth/2, 
					   rectWidth, rectWidth);
			g.fillRect(leftMargin + (partitions-2) * unit - rectWidth/2, unit - rectWidth/2, 
					   rectWidth, rectWidth);
			g.fillRect(leftMargin + (partitions-2) * unit - rectWidth/2, unit * (partitions-1) - rectWidth/2, 
					   rectWidth, rectWidth);
			g.fillRect(leftMargin - rectWidth/2, unit * (partitions-1) - rectWidth/2, 
					   rectWidth, rectWidth);
		} else if (stage == 1) {
			g.setColor(new Color(255, 0, 0));
			g.fillRect(0, fswp.height/4 - 3, fswp.width, 6);
			g.setColor(new Color(0, 255, 0));
			g.fillRect(0, fswp.height/2 - 3, fswp.width, 6);
			g.setColor(new Color(0, 0, 255));
			g.fillRect(0, fswp.height*3/4 - 3, fswp.width, 6);			
		}
		
	}
	
	
}
