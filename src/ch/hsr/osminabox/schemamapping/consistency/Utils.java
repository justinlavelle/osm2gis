package ch.hsr.osminabox.schemamapping.consistency;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	public String getActualDateForLogFileName(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'_'HHmmss");
		Date currentTime = new Date();
		return formatter.format(currentTime);
	}
	
	public String getActualDateTimeForLogEntry(){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss ");
		Date currentTime = new Date();
		return formatter.format(currentTime);
	}
}
