package ch.hsr.osminabox.util;

import java.util.Date;
/**
 * StopWatch Class for Timemeasurement
 * @author m2huber
 *
 */
public class StopWatch {
	
	Date date_start;
	Date date_stop;
	
	public StopWatch () {
		
	}
	
	/**
	 * Start the stopwatch
	 */
	public void start() {
		date_start = new Date();
	}
	
	/**
 	* Stop the Time Measurement 
 	* @return
 	*		Time in Seconds with comm
 	*/
	public float stop() {
		date_stop = new Date();

		return Float.parseFloat(""+(date_stop.getTime() - date_start.getTime()))/1000f;
		
	}
	
	/**
	 * reset Stopwatch
	 */
	public void reset()  {
		date_start = null;
		date_stop = null;
	}
	

}
