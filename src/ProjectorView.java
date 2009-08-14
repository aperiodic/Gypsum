//
//  ProjectorView.java
//  Gypsum
//
//  Created by DLP on 7/29/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
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
		images = new Image[6];
		
		for (int i = 0; i < 6; i++) {
			if (lecture.images[i].equals("")) {
				continue;
			}
			File imageFile = new File(lecture.images[i]);
			try {
				images[i] = (Image) ImageIO.read(imageFile);
			} catch (java.io.IOException e) {
				System.out.println("there was an IO error while trying to load the image " + lecture.images[i] + ". Please make sure the file is readable and try again");
			}
		}
		
		Gypsum.fsWindowProperties fswp = app.new fsWindowProperties();
		
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
			g.drawRect(r.x/2 + vidYShift, r.y/2 + vidYShift, r.width/2, r.height/2);
		}
		
		for (int i = 0; i < rects.size(); i++) {
			Rect r = (Rect) rects.get(i);
			if (r.label > 0) {
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
}
