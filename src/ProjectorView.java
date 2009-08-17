//
//  ProjectorView.java
//  Gypsum
//
//  Created by DLP on 7/29/09.
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

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class ProjectorView extends JFrame {
	protected Lecture lecture;
	protected Gypsum app;
	protected Image[] images;
	protected Image monitor;
	protected ArrayList rects, vidRects;
	
	public ProjectorView(Lecture theLecture, Gypsum theApp) {
		app = theApp;
		lecture = theLecture;
		rects = new ArrayList();
		vidRects = new ArrayList();
		images = lecture.images;
		
		Gypsum.fsWindowProperties fswp = app.new fsWindowProperties();
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
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
		
		setUndecorated(true);
		setLocation(fswp.x, fswp.y);
		setSize(fswp.width, fswp.height);
		setResizable(false);
		setBackground(new Color(0, 0, 0));
		setVisible(true);
	}
	
	public void paint(Graphics g) {
		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int vidXShift = getWidth()-330; int vidYShift = 10;
		
		if (monitor != null) {
			g.drawImage(monitor, vidXShift, vidYShift, 320, 240, this);
		}
		
		for (int i = 0; i < vidRects.size(); i++) {
			Rect r = (Rect) vidRects.get(i);
			g.setColor(new Color(0, 255, 0));
			g.drawRect(r.x/2 + vidXShift, r.y/2 + vidYShift, r.width/2, r.height/2);
		}
		
		for (int i = 0; i < rects.size(); i++) {
			Rect r = (Rect) rects.get(i);
			if (r.label > 0 && r.label <= images.length) {
				g.drawImage(images[r.label-1], r.x, r.y, r.width, r.height, this);
			}
		}
		
	}
	
	public void newRect(Rect nr) {
		rects.add(nr);
		repaint();
	}
	
	public void changeLabel(Rect cr) {
		for (int i = 0; i < rects.size(); i++) {
			Rect or = (Rect) rects.get(i);
			if (GeomUtils.doOverlap(or, cr)) {
				or.label = cr.label;
				break;
			}
		}
		
		repaint();
	}
	
	public void removeRect(Rect r) {
		for (int i = 0; i < rects.size(); i++) {
			Rect or = (Rect) rects.get(i);
			if (GeomUtils.doOverlap(r, or)) {
				rects.remove(i);
				break;
			}
		}
		
		repaint();
	}
	
	public void setMonitorImage(Image mon) {
		monitor = mon;
		
		repaint();
	}
	
	public void setVidRects(ArrayList vRects) {
		vidRects = vRects;
	}
	
	public Lecture getLecture() {
		return lecture;
	}
}
