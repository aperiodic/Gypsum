//
//  VideoMonitor.java
//  Gypsum
//
//  Created by DLP on 6/24/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;

import javax.swing.*;

import java.util.ArrayList;

import hypermedia.video.OpenCV;
import hypermedia.video.Blob;

public class VideoMonitor extends JPanel implements Runnable {
	static int WIDTH = 320;
	static int HEIGHT = 240;
	static int MAX_RECT_AREA = WIDTH*HEIGHT*3/4;
	static int MIN_RECT_AREA = WIDTH*HEIGHT/60;
	static int MAX_BLOB_AREA = WIDTH*HEIGHT/2;
	static int MIN_BLOB_AREA = 100;
	
	Thread t;
	Image frame;
	
	protected OpenCV cv;
	protected ArrayList rectangles;
	
	protected int contrast, threshold;
	protected boolean thresholded;
	
	VideoMonitor() {
		super();
		
		cv = new OpenCV();
		cv.capture(WIDTH, HEIGHT);
		contrast = 0;
		threshold = 150;
		thresholded = false;
		
		rectangles = new ArrayList();
		
		this.setBackground(Color.BLACK);
		this.setVisible(true);
		
		t = new Thread(this);
		t.start();
	}
	
	public void paint(Graphics g) {
		g.drawImage(frame, 0, 0, null);
		g.setColor(new Color(0, 255, 0));
		
		for (int i = 0; i < rectangles.size(); i++) {
			Blob rect = (Blob) rectangles.get(i);
			g.drawRect(rect.rectangle.x, rect.rectangle.y, 
					   rect.rectangle.width, rect.rectangle.height);
		}
		
	}
	
	public void run() {
		while (t != null && cv != null) {
			try {
				t.sleep(1000/20);
				
				cv.read();
				
				// convert the image to greyscale, apply contrast
				// and threshold filters
				cv.convert(OpenCV.GRAY);
				cv.contrast(contrast);
				
				if (thresholded) {
					// threshold, then save the image
					cv.threshold(threshold);
					
					MemoryImageSource mis = new MemoryImageSource(cv.width, cv.height, cv.pixels(), 0, cv.width);
					frame = createImage(mis);
				} else {
					// save the image, then threshold
					MemoryImageSource mis = new MemoryImageSource(cv.width, cv.height, cv.pixels(), 0, cv.width);
					frame = createImage(mis);
					
					cv.threshold(threshold);
				}
				
				// find the blobs in the image
				Blob[] blobs = cv.blobs(200, WIDTH*HEIGHT/2, 100, true, OpenCV.MAX_VERTICES*4);
				rectangles.clear();
				
				// scan through the blobs to find rects
				for (int i = 0; i < blobs.length; i++) {
					Blob blob = blobs[i];
					
					//-- make sure the blob is big enough to matter --//
					int containingRectArea = blob.rectangle.height * blob.rectangle.width;
					if (containingRectArea < MIN_RECT_AREA || containingRectArea > MAX_RECT_AREA) {
						continue;
					}
					
					//-- find TL, TR, BL, BR corners --//
					
					// TL and BR are the points which have lowest and 
					// highest value of y+x, respectively
					int maxYPX = 0;
					int minYPX = HEIGHT+WIDTH;
					int TLindex = 0;
					int BRindex = 0;
					for (int j = 0; j < blob.points.length; j++) {
						int ypx = blob.points[j].y + blob.points[j].x;
						
						if (ypx > maxYPX) {
							maxYPX = ypx;
							BRindex = j;
						}
						
						if (ypx < minYPX) {
							minYPX = ypx;
							TLindex = j;
						}
					}
					
					// TR and BL are the points which have the lowest and
					// highest values of y-x, respectively
					int maxYMX = -WIDTH;
					int minYMX = HEIGHT;
					int TRindex = 0;
					int BLindex = 0;
					for (int j = 0; j < blob.points.length; j++) {
						int ymx = blob.points[j].y - blob.points[j].x;
						
						if (ymx > maxYMX) {
							maxYMX = ymx;
							BLindex = j;
						}
						
						if (ymx < minYMX) {
							minYMX = ymx;
							TRindex = j;
						}
					}
					
					// make sure that one of the sides of the blob is not
					// up against the side of the screen
					if (blob.points[TLindex].x == 0 || blob.points[TLindex].y == 0 ||
						blob.points[TRindex].x == WIDTH-1 || blob.points[TRindex].y == 0 ||
						blob.points[BRindex].x == WIDTH-1 || blob.points[BRindex].y == HEIGHT-1 ||
						blob.points[BLindex].x == 0 || blob.points[BLindex].y == HEIGHT-1) {
						continue;
					}
					
					// find the difference in area between the TR-TL-BL-BR rectangle
					// and the containing rectangle
					int cornerRectArea = quadArea(blob.points[TLindex], blob.points[TRindex],
												  blob.points[BRindex], blob.points[BLindex]);
					int areaDiff = Math.abs(containingRectArea - cornerRectArea);
					
					if (areaDiff / (float)containingRectArea < 0.2) {
						rectangles.add(blob);
					}
				}
				
				repaint();
			} catch (InterruptedException e) {;}
		}
	}
	
	private int quadArea(Point a, Point b, Point c, Point d) {
		return Math.abs( (c.x - a.x)*(d.y - b.y) - (d.x - b.x)*(c.y - a.y) )/2;
	}
	
	public void setContrast(int newContrast) {
		contrast = newContrast;
	}
	
	public void setThreshold(int newThreshold) {
		threshold = newThreshold;
	}
	
	public void setThresholded(boolean enabled) {
		thresholded = enabled;
	}
}

