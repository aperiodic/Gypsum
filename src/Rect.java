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
	public long	lastObserved, firstObserved;
	public int x, y, width, height, label;
	
	public Rect (Rectangle rect, int lab) {
		rectangle = rect;
		firstObserved = new Date().getTime();
		lastObserved = new Date().getTime();
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
		x = rect.x;
		y = rect.y;
		width = rect.width;
		height = rect.height;
	}

}
