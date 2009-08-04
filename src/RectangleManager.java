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
	public ArrayList rects;
	private VideoMonitor vidmon;
	private ProjectorController projector;
	
	public RectangleManager(ProjectorController theProjector) {
		rects = new ArrayList();
		projector = theProjector;
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
				if (doOverlap(fr, or)) {
					if (rectArea(fr) >= rectArea(or)) {
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
		
		// find all of the reported rectangles which do not
		// overlap known rectangles
		for (int i = 0; i < rects.size(); i++) {
			Rect kr = (Rect) rects.get(i);
			
			for (int j = 0; j < foundRects.size(); j++) {
				Rect fr = (Rect) foundRects.get(j);
				
				if (doOverlap(fr, kr)) {
					if (fr.label != kr.label) {
						// notify projector controller of label change
					}
					
					foundRects.remove(j);
					j--;
					kr.lastObserved = new Date();
				}
			}
		}
		
		// for each new rect, notify the projector controller
		for (int i = 0; i < foundRects.size(); i++) {
			Rect nr = (Rect) foundRects.get(i);
			projector.newRect(nr);
			rects.add(nr);
		}
		
		if (vidmon != null) {
			vidmon.setRects(rects);
		}
	}
	
	// get rid of rectangles that haven't been seen in over 5 seconds
	private void flushOldRects() {
		long now = new Date().getTime();
		
		for (int i = 0; i < rects.size(); i++) {
			Rect r = (Rect) rects.get(i);
			long lastSeen = r.lastObserved.getTime();
			if (now - lastSeen > 5000) {
				projector.removeRect(r);
				rects.remove(i);
				i--;
			}
		}
	}
	
	private boolean doOverlap(Rect rectA, Rect rectB) {
		int A1 = rectA.x; int A2 = rectA.x + rectA.width;
		int B1 = rectB.x; int B2 = rectB.x + rectB.width;
		
		if (!( (A1 >= B1 && A1 <= B2) || (B1 >= A1 && B1 <= A2))) {
			return false;
		}
		
		A1 = rectA.y; A2 = rectA.y + rectA.height;
		B1 = rectB.y; B2 = rectB.y + rectB.height;
		
		if (!( (A1 >= B1 && A1 <= B2) || (B1 >= A1 && B1 <= A2))) {
			return false;
		}
		
		return true;
	}
	
	private int rectArea(Rect rect) {
		return rect.width * rect.height;
	}
	
	public void setVideoMonitor(VideoMonitor theVidMon) {
		vidmon = theVidMon;
	}
}
