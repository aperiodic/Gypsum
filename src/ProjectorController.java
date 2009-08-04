//
//  ProjectorController.java
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

public class ProjectorController extends JFrame {
	protected Lecture lecture;
	protected Gypsum app;
	protected Image[] images;
	protected ArrayList rects;
	
	public ProjectorController(Lecture theLecture, Gypsum theApp) {
		app = theApp;
		lecture = theLecture;
		rects = new ArrayList();
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
		
		setLocation(fswp.x, fswp.y);
		setSize(fswp.width, fswp.height);
		
		setVisible(true);
	}
	
	public void paint(Graphics g) {
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
			if (or.x == cr.x && or.y == cr.y) {
				or.label = cr.label;
				break;
			}
		}
		
		repaint();
	}
	
	public void removeRect(Rect r) {
		for (int i = 0; i < rects.size(); i++) {
			Rect or = (Rect) rects.get(i);
			if (or.x == r.x && or.y == r.y) {
				rects.remove(i);
				break;
			}
		}
		
		repaint();
	}
}
