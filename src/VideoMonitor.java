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
import java.awt.image.BufferedImage;

import javax.swing.*;

import java.util.ArrayList;

import hypermedia.video.OpenCV;
import hypermedia.video.Blob;

public class VideoMonitor extends JPanel implements Runnable {
	int WIDTH = 320;
	int HEIGHT = 240;
	int MAX_RECT_AREA;
	int MIN_RECT_AREA;
	int MAX_BLOB_AREA;
	int MIN_BLOB_AREA;
	
	Thread t;
	Image frame;
	
	protected OpenCV cv;
	protected ArrayList rectangles;
	
	protected int contrast, threshold;
	protected boolean thresholded, shouldStop, edgeDetect;
	
	VideoMonitor() {
		super();
		
		WIDTH = 320;
		HEIGHT = 240;
		
		makeRectBounds();
		init();
	}
	
	VideoMonitor(int width, int height) {
		super();
		
		WIDTH = width;
		HEIGHT = height;
		
		makeRectBounds();
		init();
	}
	
	protected void init() {
		cv = new OpenCV();
		cv.capture(WIDTH, HEIGHT);
		contrast = 0;
		threshold = 150;
		thresholded = false;
		edgeDetect = false;
		
		rectangles = new ArrayList();
		
		this.setBounds(0, 0, WIDTH, HEIGHT);
		this.setBackground(Color.BLACK);
		this.setVisible(true);
		
		shouldStop = false;
		t = new Thread(this);
		t.start();
	}
	
	protected void makeRectBounds() {
		MAX_RECT_AREA = WIDTH*HEIGHT*3/4;
		MIN_RECT_AREA = WIDTH*HEIGHT/60;
		MAX_BLOB_AREA = WIDTH*HEIGHT/2;
		MIN_BLOB_AREA = 100;
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
		if (shouldStop) {
			return;
		}
		
		while (t != null && cv != null) {
			try {
				t.sleep(1000/20);
				
				if (shouldStop) {
					return;
				}
				
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
				
				if (edgeDetect) {
					BufferedImage bufIm = toBufferedImage(frame);
					CannyEdgeDetector edged = new CannyEdgeDetector();
					edged.setHighThreshold(12.5f);
					edged.setLowThreshold(7.5f);
					edged.setSourceImage(bufIm);
					edged.process();
					frame = edged.getEdgesImage();
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
					if (blob.points[TLindex].x <= 1 || blob.points[TLindex].y <= 1 ||
						blob.points[TRindex].x >= WIDTH-2 || blob.points[TRindex].y <= 1 ||
						blob.points[BRindex].x >= WIDTH-2 || blob.points[BRindex].y >= HEIGHT-2 ||
						blob.points[BLindex].x <= 1 || blob.points[BLindex].y >= HEIGHT-2) {
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
	
	// This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
		
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
		
        // Create a buffered image with a format that's compatible with the screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.OPAQUE);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
		
        if (bimage == null) {
            // Create a buffered image using the default color model
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
        }
		
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
		
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
		
		return bimage;
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
	
	public void setEdgeDetection(boolean enabled) {
		edgeDetect = enabled;
	}
	
	public void start() {
		shouldStop = false;
	}
	
	public void stop() {
		shouldStop = true;
	}
}

