package ch.hsr.osminabox.util;

// License: GPL. Copyright 2007-2008 by Brett Henderson and other contributors.

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;


//1970-01-12T13:46:40.000Z
//1970-01-12T13:46:40Z

/**
 * Handles a number of different date formats encountered in OSM. This is built
 * based on similar code in JOSM. This class is not threadsafe, a separate
 * instance must be created per thread.
 * 
 * @author Brett Henderson
 */
public class DateParser {
	private DatatypeFactory datatypeFactory;
	private FallbackDateParser fallbackDateParser;
	private Calendar calendar;
	
	
	/**
	 * Creates a new instance.
	 */
	public DateParser() {
		// Build an xml data type factory.
		try {
			datatypeFactory = DatatypeFactory.newInstance();
			
		} catch (DatatypeConfigurationException e) {
			return;
		}
		
		fallbackDateParser = new FallbackDateParser();
		
		calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
	}
	
	
	private boolean isDateInShortStandardFormat(String date) {
		char[] dateChars;
		// eg. 2007-09-23T08:25:43Z
		
		if (date.length() != 20) {
			return false;
		}
		
		dateChars = date.toCharArray();
		
		if(!checkFixedCharacterPositions(dateChars, true)){
			return false;
		}
		
		if(!charactersAreNumbers(dateChars)){
			return false;
		}
		
		return true;
	}


	private boolean charactersAreNumbers(char[] dateChars) {

		if(!checkIfCharactersRangeAreNumbers(dateChars, 0, 4)){return false;}
		if(!checkIfCharactersRangeAreNumbers(dateChars, 5, 7)){return false;}
		if(!checkIfCharactersRangeAreNumbers(dateChars, 8, 10)){return false;}
		if(!checkIfCharactersRangeAreNumbers(dateChars, 11, 13)){return false;}
		if(!checkIfCharactersRangeAreNumbers(dateChars, 14, 16)){return false;}
		if(!checkIfCharactersRangeAreNumbers(dateChars, 17, 19)){return false;}
		
		return true;
	}
	
	private boolean checkIfCharactersRangeAreNumbers(char[] chars, int begin, int end){
		for (int i = begin; i < end; i++) {
			if (chars[i] < '0' || chars[i] > '9') {
				return false;
			}
		}
		return true;
	}


	private boolean checkFixedCharacterPositions(char[] dateChars, boolean shortStandard) {
		if (dateChars[4] != '-') {return false;}
		if (dateChars[7] != '-') {return false;}
		if (dateChars[10] != 'T') {return false;}
		if (dateChars[13] != ':') {return false;}
		if (dateChars[16] != ':') {return false;}
		
		if(shortStandard){
			if (dateChars[19] != 'Z') {return false;}
		} else {
			if (dateChars[19] != '.') {return false;}
			if (dateChars[23] != 'Z') {return false;}
		}
		
		return true;
	}
	
	
	private boolean isDateInLongStandardFormat(String date) {
		char[] dateChars;
		// eg. 2007-09-23T08:25:43.000Z
		
		if (date.length() != 24) {
			return false;
		}
		
		dateChars = date.toCharArray();
		checkFixedCharacterPositions(dateChars, false);
		
		charactersAreNumbers(dateChars);
		for (int i = 20; i < 23; i++) {
			if (dateChars[i] < '0' || dateChars[i] > '9') {
				return false;
			}
		}
		
		// No problems found so it is in the special case format.
		return true;
	}
	
	
	private Date parseShortStandardDate(String date) {
		int year;
		int month;
		int day;
		int hour;
		int minute;
		int second;
		
		year = Integer.parseInt(date.substring(0, 4));
		month = Integer.parseInt(date.substring(5, 7));
		day = Integer.parseInt(date.substring(8, 10));
		hour = Integer.parseInt(date.substring(11, 13));
		minute = Integer.parseInt(date.substring(14, 16));
		second = Integer.parseInt(date.substring(17, 19));
		
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		
		return calendar.getTime();
	}
	
	
	private Date parseLongStandardDate(String date) {
		int year;
		int month;
		int day;
		int hour;
		int minute;
		int second;
		int millisecond;
		
		year = Integer.parseInt(date.substring(0, 4));
		month = Integer.parseInt(date.substring(5, 7));
		day = Integer.parseInt(date.substring(8, 10));
		hour = Integer.parseInt(date.substring(11, 13));
		minute = Integer.parseInt(date.substring(14, 16));
		second = Integer.parseInt(date.substring(17, 19));
		millisecond = Integer.parseInt(date.substring(20, 23));
		
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, second);
		calendar.set(Calendar.MILLISECOND, millisecond);
		
		return calendar.getTime();
	}
	
	
	/**
	 * Attempts to parse the specified date.
	 * 
	 * @param date
	 *            The date to parse.
	 * @return The date.
	 */
	public Date parse(String date) {
		
		if(date == null){
			return new Date();
		}
		
		try {
			if (isDateInShortStandardFormat(date)) {
				return parseShortStandardDate(date);
			} else if (isDateInLongStandardFormat(date)) {
				return parseLongStandardDate(date);
			} else {
				return datatypeFactory.newXMLGregorianCalendar(date).toGregorianCalendar().getTime();
			}
			
		} catch (IllegalArgumentException e) {
			return fallbackDateParser.parse(date);
		}
	}
}
