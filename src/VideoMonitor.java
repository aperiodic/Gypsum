//
//  VideoMonitor.java
//  Gypsum
//
//  Created by DLP on 6/24/09.
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
	
	int  S = WIDTH/8;
	float T = 0.17f;
	
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
	private AffineTransform vidTranslate, vidScale;
	private Point[] calPoints;
	
	private boolean thresholded, shouldStop, calibrated, failedCal, blur;
	
	VideoMonitor() {
		super();
		
		WIDTH = 320;
		HEIGHT = 240;
		
		rectManager = new RectangleManager(this);
		calibrated = false;
		
		init();
	}
	
	VideoMonitor(int w, int h) {
		super();
		
		WIDTH = w;
		HEIGHT = h;
		
		rectManager = new RectangleManager(this);
		calibrated = false;
		
		init();
	}
	
	VideoMonitor(int w, int h, Properties cfg) {
		super();
		
		WIDTH = w;
		HEIGHT = h;
		
		rectManager = new RectangleManager(this);
		config = cfg;
		calibrated = false;
		
		init();
	}
	
	VideoMonitor(int w, int h, Properties cfg, Gypsum theApp, RectangleManager theRectManager) {
		super();
		
		WIDTH = w;
		HEIGHT = h;
		
		app = theApp;
		rectManager = theRectManager;
		
		init();
		
		Gypsum.fsWindowProperties fswp = theApp.new fsWindowProperties();
		
		if (cfg.getProperty("perspTLx") != null) {
			Point TL = new Point(java.lang.Integer.parseInt(cfg.getProperty("perspTLx")), 
								 java.lang.Integer.parseInt(cfg.getProperty("perspTLy")));
			Point TR = new Point(java.lang.Integer.parseInt(cfg.getProperty("perspTRx")), 
								 java.lang.Integer.parseInt(cfg.getProperty("perspTRy")));
			Point BR = new Point(java.lang.Integer.parseInt(cfg.getProperty("perspBRx")), 
								 java.lang.Integer.parseInt(cfg.getProperty("perspBRy")));
			Point BL = new Point(java.lang.Integer.parseInt(cfg.getProperty("perspBLx")), 
								 java.lang.Integer.parseInt(cfg.getProperty("perspBLy")));
			
			double unit = fswp.height/3.0;
			int widthMargin = (fswp.width - fswp.height)/2;
			
			// create a perspective transform that maps the projector coordinates of the 
			// calibration squares to the found quadrilateral. the inverse of this transform
			// will be used to correct the perspective distortion on the video input
			perspCorrect = PerspectiveTransform.getQuadToQuad(unit + widthMargin, unit,
															  unit*2 + widthMargin, unit,
															  unit*2 + widthMargin, unit*2,
															  unit + widthMargin, unit*2,
															  
																TL.x, TL.y,
																TR.x, TR.y,
																BR.x, BR.y,	
																BL.x, BL.y);
			
			calibrated = true;
		}
		
		config = cfg;
	}
	
	protected void init() {
		makeRectBounds();
		
		cv = new OpenCV();
		cv.capture(WIDTH, HEIGHT);
		thresholded = false;
		calibrated = false;
		failedCal = false;
		blur = false;
		
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
		S = WIDTH/8;
	}
	
	public void setRects(ArrayList someRects) {
		rectangles = new ArrayList();
		
		// copy the new rects over to the video monitor's
		// rectangles list, so we can transform them back
		// to video coordinates without interfering with
		// the rectangle manager.
		for (int i = 0; i < someRects.size(); i++) {
			Rect sr = (Rect) someRects.get(i);
			Rect nr = new Rect(new Rectangle(sr.rectangle), sr.label);
			rectangles.add(nr);
		}
		
		if (calibrated) {
			convertRectsToVideoCoords(rectangles);
			rectManager.setVidRects(rectangles);
		}
	}
	
	public void paint(Graphics g) {
		
		g.drawImage(frame, 0, 0, null);
		
		if (rectangles != null) {
			for (int i = 0; i < rectangles.size(); i++) {
				Rect rect = (Rect) rectangles.get(i);
				
				g.setColor(new Color(0, 255, 0));
				g.drawRect(rect.x, rect.y, 
						   rect.width, rect.height);
				
				g.setColor(new Color(0, 0, 255));
				for (int j = 0; j < rect.label; j++) {
					g.fillRect(rect.x + (10*j), rect.y-15, 10, 10);
				}
			}
		}
		
		if (calPoints != null) {
			g.setColor(new Color(255, 255, 0));
			for (int i = 0; i < calPoints.length; i++) {
				if (calPoints[i] == null) break;
				
				g.fillRect(calPoints[i].x - 2, calPoints[i].y - 2, 4, 4);
				
				int next = (i+1) % calPoints.length;
				if (calPoints[next] == null) break;
				
				g.drawLine(calPoints[i].x, calPoints[i].y, 
						   calPoints[next].x, calPoints[next].y);
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
				
				cv.read();
				
				// convert the image to grayscale, apply contrast
				// and threshold filters
				cv.convert(OpenCV.GRAY);
				
				if (thresholded) {
					// threshold, then save the image
					//cv.threshold(threshold);
					adaptiveThreshold();
					
					MemoryImageSource mis = new MemoryImageSource(cv.width, cv.height, cv.pixels(), 0, cv.width);
					frame = createImage(mis);
				} else {
					// save the image, then threshold
					MemoryImageSource mis = new MemoryImageSource(cv.width, cv.height, cv.pixels(), 0, cv.width);
					frame = createImage(mis);
					
					//cv.threshold(threshold);
					adaptiveThreshold();
				}
				
				// find the rectangles in the image
				Blob[] blobs = cv.blobs(100, WIDTH*HEIGHT/2, 100, true, OpenCV.MAX_VERTICES*4);
				ArrayList rawRects = findRectangles(blobs);
				ArrayList report = new ArrayList();
				
				for (int i = 0; i < rawRects.size(); i++) {
					Blob rectBlob = (Blob) rawRects.get(i);
					report.add(new Rect(rectBlob.rectangle));
				}
				
				findLabels(report);
				
				
				// if calibrated, transform each rect into screen coordinates
				if (calibrated) {
					convertRectsToScreenCoords(report);
				}
				
				rectManager.report(report);
				
				repaint();
			} catch (InterruptedException e) {;}
		}
	}
		
	public void setCalPoints(Point[] theCalPoints) {
		calPoints = theCalPoints;
	}
	
	public void calibrate(Properties theConfig, Gypsum.fsWindowProperties fswp) {
		// first, scale all the points by 2, since it will be used to
		// correct a 640x480 input, and it's being calibrated with a
		// 320x240 input
		for (int i = 0; i < 4; i++) {
			calPoints[i] = new Point(calPoints[i].x*2, calPoints[i].y*2);
		}
		
		// find the topleft, topright, bottomright, and bottomleft points
		// of the calibration quad (we can't assume a consistent winding)
		Point TL, TR, BL, BR;
		TL = TR = BL = BR = new Point(0, 0);
		int mTL = 640+480; int mTR = 480; 
		int mBL = -640; int mBR = 0;
		
		for (int i = 0; i < 4; i++) {
			Point p = calPoints[i];
			
			// figure out the blob's y+x and y-x "altitude"
			int YpX = p.y + p.x;
			int YmX = p.y - p.x;
			
			// see if either altitude is an extrema, and
			// update the values accordingly
			if (YpX < mTL) {
				TL = p;
				mTL = YpX;
			}
			if (YpX > mBR) {
				BR = p;
				mBR = YpX;
			}
			if (YmX < mTR) {
				TR = p;
				mTR = YmX;
			}
			if (YmX > mBL) { 
				BL = p;
				mBL = YmX;
			}
		}
		
		double unit = fswp.height/3.0;
		int widthMargin = (fswp.width - fswp.height)/2;
		
		// create a perspective transform that maps the projector coordinates of the 
		// calibration squares to the found quadrilateral. the inverse of this transform
		// will be used to correct the perspective distortion on the video input
		perspCorrect = PerspectiveTransform.getQuadToQuad(unit + widthMargin, unit,
														  unit*2 + widthMargin, unit,
														  unit*2 + widthMargin, unit*2,
														  unit + widthMargin, unit*2,
														  
															TL.x, TL.y,
															TR.x, TR.y,
															BR.x, BR.y,	
															BL.x, BL.y);
		
		// save the perspective transform parameters in the given config
		theConfig.setProperty("perspTLx", "" + TL.x); 
		theConfig.setProperty("perspTLy", "" + TL.y);
		
		theConfig.setProperty("perspTRx", "" + TR.x); 
		theConfig.setProperty("perspTRy", "" + TR.y);
		
		theConfig.setProperty("perspBRx", "" + BR.x);
		theConfig.setProperty("perspBRy", "" + BR.y);
		
		theConfig.setProperty("perspBLx", "" + BL.x);
		theConfig.setProperty("perspBLy", "" + BL.y);
		
		calibrated = true;		
	}
	
	private void adaptiveThreshold() {
		cv.invert();
		long sum = 0;
		int count = 0;
		int index;
		int x1, y1, x2, y2;
		int s2 = S/2;
		
		int[] input = cv.pixels();
		long[] integral = new long[WIDTH * HEIGHT];
		int[] output = new int[WIDTH * HEIGHT];
		
		// find the integral values
		for (int i = 0; i < WIDTH; i++) {
			sum = 0;
			
			for (int j = 0; j < HEIGHT; j++) {
				index = j*WIDTH + i;
				sum += input[index] & 0xff;
				if (i == 0) {
					integral[index] = sum;
				} else {
					integral[index] = integral[index-1] + sum;
				}
			}
		}
		
		// do the thresholding
		for (int i = 0; i < WIDTH; i++) {
			for (int j = 0; j < HEIGHT; j++) {
				index = j*WIDTH+i;
				
				// set up an S x S region
				x1 = i-s2; x2 = i+s2;
				y1 = j-s2; y2 = j+s2;
				
				// check border
				if (x1 < 0) x1 = 0;
				if (x2 >= WIDTH) x2 = WIDTH-1;
				if (y1 < 0) y1 = 0;
				if (y2 >= HEIGHT) y2 = HEIGHT-1;
				
				count = (x2-x1)*(y2-y1);
				
				sum = integral[y2*WIDTH + x2] -
				integral[y1*WIDTH + x2] -
				integral[y2*WIDTH + x1] +
				integral[y1*WIDTH + x1];
				
				if ((long)((input[index] & 0xff)*count) < (long)(sum*(1.0-T))) {
					output[index] = 0;
				} else {
					output[index] = 0xffffff;
				}
			}
		}
		
		cv.copy(output, WIDTH, 0, 0, WIDTH, HEIGHT, 0, 0, WIDTH, HEIGHT);
		cv.invert();
	}
	
	private ArrayList findRectangles(Blob[] blobs) {
		ArrayList rects = new ArrayList();
		
		ArrayList mBlobs = new ArrayList();
		
		// add all blobs to the mBlobs ArrayList
		for (int i = 0; i < blobs.length; i++) {
			mBlobs.add(blobs[i]);
		}
		
		// merge "close" blobs
		for (int i = 0; i < mBlobs.size(); i++) {
			boolean didmerge = false;
			Blob b = (Blob) mBlobs.get(i);
			Rectangle br = b.rectangle;
			br = new Rectangle(br.x - 2, br.y - 2, br.width + 4, br.height + 4);
			
			for (int j = 0; j < mBlobs.size(); j++) {
				// don't compare blobs to themselves
				if (i == j) {
					continue;
				}
				
				Blob o = (Blob) mBlobs.get(j);
				Rectangle or = o.rectangle;
				or = new Rectangle(or.x - 2, or.y - 2, or.width + 4, or.height + 4);
				
				if (GeomUtils.doOverlap(br, or) && !GeomUtils.doContain(br, or)) {
					// create a new Point array to hold all the points of b & o
					Point[] newPoints = new Point[b.points.length + o.points.length];
					
					// copy the points from b & o into the new array
					for (int k = 0; k < b.points.length; k++) {
						newPoints[k] = b.points[k];
					}
					for (int k = b.points.length; k < newPoints.length; k++) {
						newPoints[k] = o.points[k - b.points.length];
					}
					
					b.points = newPoints;
					b.rectangle = GeomUtils.rectMerge(b.rectangle, o.rectangle);
					
					mBlobs.remove(j);
					j--;
					didmerge = true;
				}
			}
			if (didmerge) i--;
		}
		
		blobs = new Blob[mBlobs.size()];
		
		for (int i = 0; i < mBlobs.size(); i++) {
			Blob b = (Blob) mBlobs.get(i);
			blobs[i] = b;
		}
		
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
			int cornerRectArea = GeomUtils.quadArea(blob.points[TLindex], blob.points[TRindex],
										            blob.points[BRindex], blob.points[BLindex]);
			int areaDiff = Math.abs(containingRectArea - cornerRectArea);
			
			if (areaDiff / (float)containingRectArea < 0.2) {
				rects.add(blob);
			}
		}
		
		return rects;
	}
	
	private void findLabels(ArrayList theRects) {
		// blur the image so small gaps in the label are closed
		cv.blur(OpenCV.BLUR, 3);
		
		Blob[] lablobs = cv.blobs(100, 6000, 100, true, OpenCV.MAX_VERTICES*4);
		
		for (int i = 0; i < theRects.size(); i++) {
			Rect r = (Rect) theRects.get(i);
			int minX = r.rectangle.x; int maxX = r.rectangle.x + r.rectangle.width;
			int minY = r.rectangle.y - (r.rectangle.height*3)/4; int maxY = r.rectangle.y;
			
			ArrayList candidateBlobs = new ArrayList();
			
			for (int j = 0; j < lablobs.length; j++) {
				
				if (lablobs[j].rectangle.x < minX || 
					lablobs[j].rectangle.x + lablobs[j].rectangle.width > maxX) {
					continue;
				}
				
				if (lablobs[j].rectangle.y < minY ||
					lablobs[j].rectangle.y + lablobs[j].rectangle.height > maxY) {
					continue;
				}
				
				candidateBlobs.add(lablobs[j]);
			}
			
			if (candidateBlobs.size() == 0 || candidateBlobs.size() == 1) {
				r.label = 0;
			} else {
				// first find the biggest blob
				int maxSize = 0; int maxSizeIndex = 0;
				for (int j = 0; j < candidateBlobs.size(); j++) {
					Blob b = (Blob) candidateBlobs.get(j);
					int blobSize = b.rectangle.width * b.rectangle.height;
					if (blobSize > maxSize) {
						maxSize = blobSize;
						maxSizeIndex = j;
					}
				}
				
				// now see how many blobs are contained in that one
				Blob biggest = (Blob) candidateBlobs.get(maxSizeIndex);
				int biggestContains = 0;
				
				for (int j = 0; j < candidateBlobs.size(); j++) {
					if (j == maxSizeIndex) continue;
					
					Blob b = (Blob) candidateBlobs.get(j);
					if (GeomUtils.doContain(biggest.rectangle, b.rectangle)) {
						biggestContains++;
					}
				}
				
				r.label = biggestContains;
			}
		}
	}
	
	private void convertRectsToScreenCoords(ArrayList theRects) {
		for (int i = 0; i < theRects.size(); i++) {
 			Rect r = (Rect) theRects.get(i);
			Point origin, botright;
			origin = r.rectangle.getLocation();
			botright = new Point(r.x + r.width, r.y + r.height);
			
			try {
				perspCorrect.inverseTransform(origin, origin);
				perspCorrect.inverseTransform(botright, botright);
			} catch (java.awt.geom.NoninvertibleTransformException e) {}
			
			
			r.rectangle = new Rectangle(origin.x, origin.y, botright.x-origin.x, botright.y-origin.y);
			r.x = origin.x; r.y = origin.y;
			r.width = r.rectangle.width; r.height = r.rectangle.height;
		}
	}
	
	private void convertRectsToVideoCoords(ArrayList theRects) {
		for (int i = 0; i < theRects.size(); i++) {
			Rect r = (Rect) theRects.get(i);
			Point origin, botright;
			origin = r.rectangle.getLocation();
			botright = new Point(r.x + r.width, r.y + r.height);
			
			perspCorrect.transform(origin, origin);
			perspCorrect.transform(botright, botright);
			
			r.rectangle = new Rectangle(origin.x, origin.y, botright.x-origin.x, botright.y-origin.y);
			r.x = origin.x; r.y = origin.y;
			r.width = r.rectangle.width; r.height = r.rectangle.height;
		}
	}
	
	public void setThresholded(boolean enabled) {
		thresholded = enabled;
	}
	
	public Image getImage() {
		return frame;
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
            // there's no point in running this application headlessly. ignore!
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

