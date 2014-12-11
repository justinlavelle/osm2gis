package ch.hsr.osminabox.updating;

import org.quartz.Trigger;

/**
 * 
 * @author Joram
 *
 */
public interface UpdateStrategy {
	
	/**
	 * @param lastUpdateFile
	 * @return the next update file name
	 */
	String getNextUpdateFile(String lastUpdateFile);
	
	/**
	 * Converts the File AAABBBCCC.osm.bz2 into AAA/BBB/CCC.osm.bz2 used for Replicate downloads
	 * 
	 * @param updateFile
	 * @return
	 */
	String getUpdateFileAsUrl(String updateFile);
		
	/**
	 * @return a Trigger used by quartz to schedule the update process
	 */
	Trigger getTrigger();
	
	/**
	 * @return the name of the sub directory for the url
	 */
	String getSubDirectoryConfigConstant();

}
