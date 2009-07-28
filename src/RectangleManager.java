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
	
	public RectangleManager() {
		rects = new ArrayList();
	}
	
	public void report(ArrayList foundRects) {
		// first, get rid of old rectangles which haven't been
		// seen in over a second
		flushOldRects();
		
		// find all of the reported rectangles which do not
		// overlap known rectangles
		ArrayList toAdd = new ArrayList();
		
		for (int i = 0; i < foundRects.size(); i++) {
			Rect fr = (Rect) foundRects.get(i);
			boolean newRect = true;
			for (int j = 0; j < rects.size(); j++) {
				Rect kr = (Rect) rects.get(i);
				if (doOverlap(fr, kr)) {
					newRect = false;
					kr.lastObserved = new Date();
					break;
				}
			}
			
			if (newRect) {
				toAdd.add(fr);
			}
		}
		
		rects.addAll(toAdd);
	}
	
	// get rid of rectangles that haven't been seen in over a second
	private void flushOldRects() {
		long now = new Date().getTime();
		
		for (int i = 0; i < rects.size(); i++) {
			Rect r = (Rect) rects.get(i);
			long lastSeen = r.lastObserved.getTime();
			if (now - lastSeen > 1000) {
				rects.remove(i);
				i--;
			}
		}
	}
	
	private boolean doOverlap(Rect rectA, Rect rectB) {
		int A1 = rectA.x; int A2 = rectA.x + rectA.width;
		int B1 = rectB.x; int B2 = rectB.x + rectB.width;
		
		if (!( (A1 > B1 && A1 < B2) || (B1 > A1 && B1 < A2))) {
			return false;
		}
		
		A1 = rectA.y; A2 = rectA.y + rectA.height;
		B1 = rectB.y; B2 = rectB.y + rectB.height;
		
		if (!( (A1 > B1 && A1 < B2) || (B1 > A1 && B1 < A2))) {
			return false;
		}
		
		return true;
	}
}
