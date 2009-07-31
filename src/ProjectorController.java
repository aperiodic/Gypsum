//
//  ProjectorController.java
//  Gypsum
//
//  Created by DLP on 7/29/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import javax.imageio.ImageIO;
import java.io.File;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class ProjectorController extends JFrame {
	protected Lecture lecture;
	protected Gypsum app;
	protected Image[] images;
	
	public ProjectorController(Gypsum theApp) {
		app = theApp;
		
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
	
	public void newRect(Rectangle r, int label) {
		//
	}
	
	public void changeLabel(Rectangle r, int label) {
	}
	
	public void flushRect(Rectangle r, int label) {
		
	}
}
