//
//  RectangleManager.java
//  Gypsum
//
//  Created by DLP on 7/15/09.
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

import java.util.ArrayList;
import java.util.Date;

public class RectangleManager {
	public ArrayList rects, freshRects;
	private VideoMonitor vidmon;
	private ProjectorView projector;
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
	
	public RectangleManager(ProjectorView theProjector) {
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
				
				long now = new Date().getTime();
				
				// if it overlaps, we assume it's the same rectangle
				// a bit naive, perhaps, but the camera shouldn't move,
				// it makes a ton of stuff much easier & more robust,
				// and i can't think of any reason why someone would want
				// to overlap projected images
				if (GeomUtils.doOverlap(fr, kr)) {
					
					// the label is different, so see if this is indecision
					// or a real label change
					if (fr.label != kr.label) {
						// if it doesn't have a label, go ahead and add it
						if (kr.label == 0) {
							kr.label = fr.label;
							kr.labelObserved = now;
							if (projecting) {
								projector.changeLabel(kr);
							}
						} else {
							// if it does have a label, make sure that
							// label hasn't been seen for 1/4 of a second
							if (now - kr.labelObserved > 250) {
								kr.label = fr.label;
								kr.labelObserved = now;
								if (projecting) {
									projector.changeLabel(kr);
								}
							}
						}
						
					} else {
						// if the label is the same, update the labelObserved
						// time of the rect to reflect this
						kr.labelObserved = now;
					}
					kr.lastObserved = now;
					
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
		
		if (projecting) {
			projector.setMonitorImage(vidmon.getImage());
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
	
	public void setVidRects(ArrayList vrects) {
		if (projecting) {
			projector.setVidRects(vrects);
		}
	}
	
	public ProjectorView getProjectorView() {
		return projector;
	}
}
