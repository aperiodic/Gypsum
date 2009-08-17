//
//  Rectangle.java
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

import java.util.Date;
import java.awt.Rectangle;
import java.awt.Image;

public class Rect {
	public Rectangle rectangle;
	public long	lastObserved, firstObserved, labelObserved;
	public int x, y, width, height, label;
	
	public Rect (Rectangle rect, int lab) {
		rectangle = rect;
		firstObserved = new Date().getTime();
		lastObserved = new Date().getTime();
		labelObserved = new Date().getTime();
		label = lab;
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}
	
	public Rect (Rectangle rect) {
		rectangle = rect;
		firstObserved = new Date().getTime();
		lastObserved = new Date().getTime();
		// set the label change time to 0, so adding the first label is instant
		labelObserved = 0; // the dawn of time!
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}

}
