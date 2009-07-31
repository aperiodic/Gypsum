//
//  Rectangle.java
//  Gypsum
//
//  Created by DLP on 7/15/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.Date;
import java.awt.Rectangle;
import java.awt.Image;

public class Rect {
	public Rectangle rectangle;
	public Date	lastObserved;
	public int x, y, width, height, label;
	
	public Rect (Rectangle rect, int lab) {
		rectangle = rect;
		lastObserved = new Date();
		label = lab;
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}
	
	public Rect (Rectangle rect) {
		rectangle = rect;
		lastObserved = new Date();
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}

}
