//
//  Calibration.java
//  Gypsum
//
//  Created by DLP on 7/1/09.
//  Copyright (c) 2009, Dan Lidral Porter
//  All rights reserved.

//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:

//  * Redistributions of source code must retain the above copyright notice, this list
//    of conditions and the following disclaimer.
//  * Redistributions in binary form must reproduce the above copyright notice, this list
//    of conditions and the following disclaimer in the documentation and/or other materials
//    provided with the distribution.
//  * Neither the name of "Gypsum" nor the names of its contributors may be used to endorse
//    or promote products derived from this software without specific prior written permission.

//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
//  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
//  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
//  SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
//  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
//  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
//  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
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
		
	}
	
	
}
