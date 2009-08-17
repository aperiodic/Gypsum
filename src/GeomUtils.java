//
//  GeomUtils.java
//  Gypsum
//
//  Created by DLP on 8/4/09.
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

import java.awt.Rectangle;
import java.awt.Point;

public class GeomUtils {
	
	public static boolean doOverlap(Rect rectA, Rect rectB) {
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
	
	public static boolean doOverlap(Rectangle rectA, Rectangle rectB) {
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
	
	public static boolean doContain(Rectangle rectA, Rectangle rectB) {
		int A1 = rectA.x; int A2 = rectA.x + rectA.width;
		int B1 = rectB.x; int B2 = rectB.x + rectB.width;
		int A3 = rectA.y; int A4 = rectA.y + rectA.height;
		int B3 = rectB.y; int B4 = rectB.y + rectB.height;
		
		// if A is contained in B...
		if (A1 > B1 && A2 < B2 && A3 > B3 && A4 < B4) {
			return true;
		}
		
		// if B is contained in A...
		if (B1 > A1 && B2 < A2 && B3 > A3 && B4 < A4) {
			return true;
		}
		
		return false;
	}
	
	public static Rectangle rectMerge(Rectangle a, Rectangle b) {
		Rectangle merged = new Rectangle();
		merged.x = ((a.x < b.x) ? a.x : b.x);
		merged.width = ((a.x + a.width > b.x + b.width) ? a.x + a.width : b.x + b.width) - merged.x;
		merged.y = ((a.y < b.y) ? a.y : b.y);
		merged.height = ((a.y + a.height > b.y + b.height) ? a.y + a.height : b.y + b.height) - merged.y;
		
		return merged;
	}
	
	public static int rectArea(Rect rect) {
		return rect.width * rect.height;
	}
	
	public static int quadArea(Point a, Point b, Point c, Point d) {
		return Math.abs( (c.x - a.x)*(d.y - b.y) - (d.x - b.x)*(c.y - a.y) )/2;
	}
	

}
