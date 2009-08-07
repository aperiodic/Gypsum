//
//  CalibrationTimerTask.java
//  Gypsum
//
//  Created by DLP on 7/9/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

import java.util.TimerTask;

public class CalibrationTimerTask extends TimerTask {
	protected Calibration cal;
	
	CalibrationTimerTask (Calibration c) {
		super();
		cal = c;
	}
	
	public void run() {
		cal.calibrate();
	}
}
