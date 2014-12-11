package ch.hsr.osminabox.updating;

/**
 * This Enums are used by the DateStringParser. They represent specific values
 * within a date string
 * @author rhof
 *
 */
public enum DateStringFields {

	YEAR(0, 4, 4), MONTH(4, 6, 2), DAY(6, 8, 2), HOUR(8, 10, 2), MINUTE(10, 12, 2);
	
	private final int startIndex;
	private final int endIndex;
	private final int length;
	
	private DateStringFields(int startIndex, int endIndex, int length) {
		this.startIndex = startIndex;
		this.endIndex = endIndex;
		this.length = length;
	}
	
	public int getStartIndex(){
		return this.startIndex;
	}
	
	public int getEndIndex(){
		return this.endIndex;
	}
	
	public int length(){
		return this.length;
	}
}
