package ch.hsr.osminabox.updating;

import java.util.Date;

import org.quartz.Trigger;
import org.quartz.TriggerUtils;

import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.util.PathUtil;

/**
 * Holds the Information for a daily scheduled update
 * @author rhof
 *
 */
public class DailyUpdateStrategy implements UpdateStrategy {

	private int updateHour = 13;
	private int updateMinute = 15;
	
	public DailyUpdateStrategy(int updateHour, int updateMinute) {
		this.updateHour = updateHour;
		this.updateMinute = updateMinute;
	}

	protected String getNextFileString(DateStringHandler handler) {
			   handler.addDay();
		return handler.getDateString(DateStringFields.DAY);
	}

	@Override
	public Trigger getTrigger() {
		Trigger trigger = TriggerUtils.makeDailyTrigger(updateHour, updateMinute);
			    trigger.setStartTime(new Date());
			    trigger.setMisfireInstruction(Trigger.INSTRUCTION_NOOP);
			    trigger.setName("DailyTrigger");
			    
		return trigger;
	}

	@Override
	public String getSubDirectoryConfigConstant() {
		return ConfigConstants.CONF_DIFF_DIRECTORY_DAILY;
	}

	@Override
	public String getNextUpdateFile(String lastUpdateFile) {
		String extension = PathUtil.getExtension(lastUpdateFile);
		String lastDateString = getCurrentDateString(lastUpdateFile);
		
		DateStringHandler handler = new DateStringHandler(lastDateString);
		String nextDateString = getNextFileString(handler);
		
		return lastDateString + "-" + nextDateString + extension;
	}
	
	/**
	 * @param fileName
	 * @return the string holding the date for a filename
	 */
	public String getCurrentDateString(String fileName) {
		int dotIndex = fileName.indexOf('.');
		String filenameWithoutDots = fileName.substring(0, dotIndex);
		int strichIndex = filenameWithoutDots.indexOf('-');
		String currentDateInFileName = filenameWithoutDots.substring(strichIndex + 1);
		return currentDateInFileName;
	}

	@Override
	public String getUpdateFileAsUrl(String updateFile) {
		return updateFile;
	}
}
