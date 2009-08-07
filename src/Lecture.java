//
//  Lecture.java
//  Gypsum
//
//  Created by DLP on 7/28/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

public class Lecture {
	public String[] images;
	
	public Lecture (String[] theImages) {
		images = new String[6];
		for (int i = 0; i < theImages.length; i++) {
			images[i] = theImages[i];
		}
		for (int i = theImages.length; i < 6; i++) {
			images[i] = "";
		}
	}
}
