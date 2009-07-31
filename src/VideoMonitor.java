//
//  VideoMonitor.java
//  Gypsum
//
//  Created by DLP on 6/24/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.MemoryImageSource;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.RenderedImage;

import javax.swing.*;
import javax.media.jai.*;

import java.util.ArrayList;
import java.util.Properties;

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
	
	private Gypsum app;
	private RectangleManager rectManager;
	
	private OpenCV cv;
	private ArrayList rectangles;
	
	private Properties config;
	
	private RenderedOp srcImage, dstImage;
	private PerspectiveTransform perspCorrect;
	private AffineTransform idTransform;
	private AffineTransform projectorTransform;
	
	private int contrast, threshold;
	private boolean thresholded, shouldStop, edgeDetect, calibrated, failedCal;
	
	VideoMonitor() {
		super();
		
		WIDTH = 320;
		HEIGHT = 240;
		
		init();
	}
	
	VideoMonitor(int w, int h) {
		super();
		
		WIDTH = w;
		HEIGHT = h;
		
		init();
	}
	
	VideoMonitor(int w, int h, Properties cfg) {
		super();
		
		WIDTH = w;
		HEIGHT = h;
		
		config = cfg;
		
		init();
	}
	
	VideoMonitor(int w, int h, Properties cfg, Gypsum theApp, RectangleManager theRectManager) {
		super();
		
		WIDTH = w;
		HEIGHT = h;
		
		app = theApp;
		rectManager = theRectManager;
		
		init();
		
		contrast = java.lang.Integer.parseInt(cfg.getProperty("contrast"));
		threshold = java.lang.Integer.parseInt(cfg.getProperty("threshold"));
		
		Gypsum.fsWindowProperties fswp = theApp.new fsWindowProperties();
		
		if (cfg.getProperty("perpTLx") != null) {
			Point TL = new Point(java.lang.Integer.parseInt(cfg.getProperty("perspTLx")), 
								 java.lang.Integer.parseInt(cfg.getProperty("perspTLy")));
			Point TR = new Point(java.lang.Integer.parseInt(cfg.getProperty("perspTRx")), 
								 java.lang.Integer.parseInt(cfg.getProperty("perspTRy")));
			Point BR = new Point(java.lang.Integer.parseInt(cfg.getProperty("perspBRx")), 
								 java.lang.Integer.parseInt(cfg.getProperty("perspBRy")));
			Point BL = new Point(java.lang.Integer.parseInt(cfg.getProperty("perspBLx")), 
								 java.lang.Integer.parseInt(cfg.getProperty("perspBLy")));
			int sideLength = java.lang.Integer.parseInt(cfg.getProperty("perspSideLength"));
			perspCorrect = PerspectiveTransform.getQuadToQuad(TL.x, TL.y,
															  TL.x + sideLength, TL.y,
															  TL.x + sideLength, TL.y + sideLength,
															  TL.x, TL.y + sideLength,
															  
															  TL.x, TL.y,
															  TR.x, TR.y,
															  BR.x, BR.y,	
															  BL.x, BL.y);
			projectorTransform = AffineTransform.getTranslateInstance(-TL.x, -TL.y);
			projectorTransform.scale(fswp.height/3.0, fswp.height/3.0);
		}
		
		config = cfg;
	}
	
	protected void init() {
		makeRectBounds();
		
		cv = new OpenCV();
		cv.capture(WIDTH, HEIGHT);
		contrast = 0;
		threshold = 150;
		thresholded = false;
		edgeDetect = false;
		calibrated = false;
		failedCal = false;
		
		idTransform = new AffineTransform();
		
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
	
	public void setRects(ArrayList theRects) {
		rectangles = theRects;
	}
	
	public void paint(Graphics g) {
		if (!calibrated) {
			g.drawImage(frame, 0, 0, null);
		} else {
			ParameterBlock pb = new ParameterBlock();
			pb.addSource(srcImage);
			try {
				pb.add(new WarpPerspective(perspCorrect.createInverse()));
				pb.add(Interpolation.getInstance(Interpolation.INTERP_BICUBIC));
			} catch (Exception e) {
				// after calibration, all perspective correction transforms
				// will be invertible, so ignore the error
			}
			
			dstImage = JAI.create("warp", pb, null);
			((Graphics2D)g).drawRenderedImage(dstImage, idTransform);
		}
		
		
		if (rectangles != null) {
			g.setColor(new Color(0, 255, 0));
			for (int i = 0; i < rectangles.size(); i++) {
				Rect rect = (Rect) rectangles.get(i);
				g.drawRect(rect.x, rect.y, 
						   rect.width, rect.height);
			}
		}
		
	}
	
	public void run() {
		if (shouldStop) {
			return;
		}
		
		// need to have a rectangle manager
		if (rectManager == null) {
			return;
		}
		
		while (t != null && cv != null) {
			try {
				t.sleep(1000/20);
				
				if (shouldStop) {
					return;
				}
				
				if (failedCal) {
					return;
				}
				
				cv.read();
				
				if (calibrated) {
					int foo = 3;
				}
				
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
								
				// find the rectangles in the image
				Blob[] blobs = cv.blobs(200, WIDTH*HEIGHT/2, 100, true, OpenCV.MAX_VERTICES*4);
				ArrayList rawRects = findRectangles(blobs);
				ArrayList report = new ArrayList();
				
				for (int i = 0; i < rawRects.size(); i++) {
					Blob rectBlob = (Blob) rawRects.get(i);
					report.add(new Rect(rectBlob.rectangle));
				}
				
				rectManager.report(report);
				
				repaint();
			} catch (InterruptedException e) {;}
		}
	}
	
	public void perspectiveCorrect(Gypsum.fsWindowProperties fswp) {
		// get the image, find the yellow blobs, create
		// a perspective transform matrix that maps
		// the rectangle defined by the red blobs to
		// a square
		cv.read();
		
		cv.contrast(contrast);
		
		MemoryImageSource mis = new MemoryImageSource(cv.width, cv.height, cv.pixels(), 0, cv.width);
		Raster pixels = toBufferedImage(createImage(mis)).getData();
		
		cv.convert(OpenCV.GRAY);
		cv.threshold(threshold);
		
		MemoryImageSource smis = new MemoryImageSource(cv.width, cv.height, cv.pixels(), 0, cv.width);
		frame = createImage(smis);
		
		// find the small blobs in the image
		Blob[] blobs = cv.blobs(80, 1500, 100, true, OpenCV.MAX_VERTICES*4);
		ArrayList yellowBlobs = new ArrayList();
		
		int numBlobs = blobs.length;
		
		// find the yellow blobs
		for (int i = 0; i < blobs.length; i++) {
			int[] blobSamples = new int[3];
			pixels.getPixel(blobs[i].centroid.x, blobs[i].centroid.y, blobSamples);
			int r = blobSamples[0];
			int g = blobSamples[1];
			int b = blobSamples[2];
			float yellowness = ((float) r + g) / ((float) r + g + b);
			
			if(yellowness > 0.75 && (r + g + b) > 200) {
				
				
				yellowBlobs.add(blobs[i]);
			}
		}
		
		if(yellowBlobs.size() != 4) {
			System.err.println("wrong amount of perspective transform calibration points, found " + yellowBlobs.size());
			failedCal = true;
		} else {
			Point TL, TR, BL, BR;
			TL = TR = BL = BR = new Point(0, 0);
			int mTL = cv.height+cv.width; int mTR = cv.height; 
			int mBL = -cv.width; int mBR = 0;
			
			for (int i = 0; i < yellowBlobs.size(); i++) {
				Blob b = (Blob) yellowBlobs.get(i);
				
				// figure out the blob's y+x and y-x "altitude"
				int YpX = b.centroid.y + b.centroid.x;
				int YmX = b.centroid.y - b.centroid.x;
				
				// see if either altitude is an extrema, and
				// update the values accordingly
				if (YpX < mTL) TL = b.centroid;
				if (YpX > mBR) BR = b.centroid;
				if (YmX < mTR) TR = b.centroid;
				if (YmX > mBL) BL = b.centroid;
			}
			
			int leftSideLength = BL.y - TL.y;
			int rightSideLength = BR.y - TR.y;
			int sideLength = (leftSideLength > rightSideLength) ? leftSideLength : rightSideLength;
			
			// create a perspective transform that maps a square to the found quadrilateral
			// the inverse of this transform will be used to correct the perspective distortion
			// on the video input
			perspCorrect = PerspectiveTransform.getQuadToQuad(TL.x, TL.y,
															  TL.x + sideLength, TL.y,
															  TL.x + sideLength, TL.y + sideLength,
															  TL.x, TL.y + sideLength,
															  
															  TL.x, TL.y,
															  TR.x, TR.y,
															  BR.x, BR.y,	
															  BL.x, BL.y);
			
			// create an affine transform that will map points in an image that has been corrected
			// by the above transform to projector coordinates
			projectorTransform = AffineTransform.getTranslateInstance(-TL.x, -TL.y);
			projectorTransform.scale(fswp.height/3.0, fswp.height/3.0);
			
			// save the perspective transform parameters in the config object
			config.setProperty("perspTLx", "" + TL.x); config.setProperty("perspTLy", "" + TL.y);
			config.setProperty("perspTRx", "" + TR.x); config.setProperty("perspTRy", "" + TR.y);
			config.setProperty("perspBRx", "" + BR.x); config.setProperty("perspBRy", "" + BR.y);
			config.setProperty("perspBLx", "" + BL.x); config.setProperty("perspBLy", "" + BL.y);
			config.setProperty("perspSideLength", "" + sideLength);
			
			
			calibrated = true;
		}
		
	}
	
	public void findProjectorRange() {
		// find out how far above & below the projected
		// colored lines the board boundary is, and
		// normalize that to figure out how much of
		// the board the projector can project on to
		// also create a function to map camera coordinates
		// into projector coordinates
	}
	
	// This method returns a buffered image with the contents of an image
	
	private ArrayList findRectangles(Blob[] blobs) {
		ArrayList rects = new ArrayList();
		
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
				rects.add(blob);
			}
		}
		
		return rects;
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
		cv.stop();
		frame = null;
	}
	
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
}

