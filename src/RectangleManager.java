//
//  RectangleManager.java
//  Gypsum
//
//  Created by DLP on 7/15/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.ArrayList;
import java.util.Date;

public class RectangleManager {
	public ArrayList rects, freshRects;
	private VideoMonitor vidmon;
	private ProjectorController projector;
	boolean projecting;
	
	public RectangleManager() {
		rects = new ArrayList();
		freshRects = new ArrayList();
		projecting = false;
	}
	
	public RectangleManager(VideoMonitor theVidMon) {
		vidmon = theVidMon;
		rects = new ArrayList();
		freshRects = new ArrayList();
		projecting = false;
	}
	
	public RectangleManager(ProjectorController theProjector) {
		rects = new ArrayList();
		freshRects = new ArrayList();
		projector = theProjector;
		projecting = true;
	}
	
	public void report(ArrayList foundRects) {
		// first, get rid of old rectangles which haven't been
		// seen in over a second
		flushOldRects();
		
		// get rid of all the rectangles in the report which overlap
		for (int i = 0; i < foundRects.size(); i++) {
			Rect fr = (Rect) foundRects.get(i);
			for (int j = 0; j < foundRects.size(); j++) {
				if (i == j) continue;
				
				Rect or = (Rect) foundRects.get(j);
				if (GeomUtils.doOverlap(fr, or)) {
					if (GeomUtils.rectArea(fr) >= GeomUtils.rectArea(or)) {
						foundRects.remove(j);
						j--;
					} else {
						foundRects.remove(i);
						i--;
						break;
					}
				}
			}
		}
		
		// see if any of the reported rectangles overlap
		// "fresh" rectangles (rectangles < 0.25 seconds old).
		// if so, update the fresh rect's geometric info
		// with the reported rect's. this is to avoid situations
		// where only half of a rectangle is projected onto,
		// because it was prematurely identified while something
		// was obscuring part of it.
		for (int i = 0; i < freshRects.size(); i++) {
			Rect fsr = (Rect) freshRects.get(i);
			
			for (int j = 0; j < foundRects.size(); j++) {
				Rect fr = (Rect) foundRects.get(j);
				
				if (GeomUtils.doOverlap(fsr, fr)) {
					fsr.rectangle = fr.rectangle;
					fsr.x = fr.x; fsr.y = fr.y;
					fsr.width = fr.width; fsr.height = fr.height;
					fsr.label = fr.label;
					fsr.lastObserved = new Date().getTime();
					
					foundRects.remove(j);
					j--;
				}
			}
		}
		
		// find all of the reported rectangles which do not
		// overlap known rectangles
		for (int i = 0; i < rects.size(); i++) {
			Rect kr = (Rect) rects.get(i);
			
			for (int j = 0; j < foundRects.size(); j++) {
				Rect fr = (Rect) foundRects.get(j);
				
				if (GeomUtils.doOverlap(fr, kr)) {
					if (fr.label != kr.label) {
						kr.label = fr.label;
						
						if (projecting) {
							projector.changeLabel(fr);
						}
					}
					kr.lastObserved = new Date().getTime();
					
					foundRects.remove(j);
					j--;
				}
			}
		}
		
		// move each new rect to the "fresh" rects arraylist
		for (int i = 0; i < foundRects.size(); i++) {
			Rect nr = (Rect) foundRects.get(i);
			freshRects.add(nr);
		}
		
		// move each fresh rect > 0.25 seconds old into rects
		// and notify projector (if projecting)
		for (int i = 0; i < freshRects.size(); i++) {
			Rect fsr = (Rect) freshRects.get(i);
			
			long now = new Date().getTime();
			if (now - fsr.firstObserved > 250) {
				rects.add(fsr);
				
				if (projecting) {
					projector.newRect(fsr);
				}
				
				freshRects.remove(i);
				i--;
			}
		}
		
		if (vidmon != null) {
			vidmon.setRects(rects);
		}
	}
	
	// get rid of rectangles that haven't been seen in over 3 seconds
	private void flushOldRects() {
		long now = new Date().getTime();
		
		for (int i = 0; i < rects.size(); i++) {
			Rect r = (Rect) rects.get(i);
			long lastSeen = r.lastObserved;
			if (now - lastSeen > 3000) {
				if (projecting) {
					projector.removeRect(r);
				}
				rects.remove(i);
				i--;
			}
		}
	}
	
	public void setVideoMonitor(VideoMonitor theVidMon) {
		vidmon = theVidMon;
	}
}
