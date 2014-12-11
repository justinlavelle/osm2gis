package ch.hsr.osminabox.updating;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This class handles the a datestring wrapping a calendar object
 * @author rhof
 *
 */
public class DateStringHandler {
	
	private Calendar calendar;
	
	public DateStringHandler() {
		calendar = new GregorianCalendar();
	}
	
	public DateStringHandler(String dateString){
		this();
		setDateString(dateString);
	}
	
	public void setDateString(String dateString){
		
		String year = dateString.substring(DateStringFields.YEAR.getStartIndex(), DateStringFields.YEAR.getEndIndex());
			setValue(DateStringFields.YEAR, year);
		String month = dateString.substring(DateStringFields.MONTH.getStartIndex(), DateStringFields.MONTH.getEndIndex());
			setValue(DateStringFields.MONTH, month);
		String day = dateString.substring(DateStringFields.DAY.getStartIndex(), DateStringFields.DAY.getEndIndex());
			setValue(DateStringFields.DAY, day);
		if(dateString.length() > DateStringFields.HOUR.getStartIndex()){
			String hour = dateString.substring(DateStringFields.HOUR.getStartIndex(), DateStringFields.HOUR.getEndIndex());
				setValue(DateStringFields.HOUR, hour);
		}
		if(dateString.length() > DateStringFields.MINUTE.getStartIndex()){
			String minute = dateString.substring(DateStringFields.MINUTE.getStartIndex(), DateStringFields.MINUTE.getEndIndex());
				setValue(DateStringFields.MINUTE, minute);
		}
	}
	
	public String getDateString(DateStringFields lastField){
		
		String baseValue = getStringValue(DateStringFields.YEAR)
		 				 + getStringValue(DateStringFields.MONTH)
		 				 + getStringValue(DateStringFields.DAY);
		
		if(lastField == DateStringFields.DAY){
			return baseValue;}
		
		if(lastField == DateStringFields.HOUR){
			return baseValue + getStringValue(DateStringFields.HOUR);}
		
		if(lastField == DateStringFields.MINUTE){
			return baseValue + getStringValue(DateStringFields.HOUR)
						     + getStringValue(DateStringFields.MINUTE);}
		
		return "";
	}
	
	public int getIntegerValue(DateStringFields field){
		
		switch(field){
			case YEAR:  return calendar.get(Calendar.YEAR);
			case MONTH: return calendar.get(Calendar.MONTH)+1;
			case DAY:   return calendar.get(Calendar.DAY_OF_MONTH);
			case HOUR:  return calendar.get(Calendar.HOUR_OF_DAY);
			case MINUTE:return calendar.get(Calendar.MINUTE);
			default:    return 0;
		}
		
	}
	
	public String getStringValue(DateStringFields field){
		
		String strRepresentation = String.valueOf(getIntegerValue(field));
		while(strRepresentation.length() < field.length()){
			strRepresentation = "0" + strRepresentation;
		}
		return strRepresentation;
	}
	
	public void setValue(DateStringFields field, int value){
		
		switch(field){
			case YEAR:   calendar.set(Calendar.YEAR, value);         break;
			case MONTH:  calendar.set(Calendar.MONTH, (value-1));	 break;
			case DAY:    calendar.set(Calendar.DAY_OF_MONTH, value); break;
			case HOUR:   calendar.set(Calendar.HOUR_OF_DAY, value);  break;
			case MINUTE: calendar.set(Calendar.MINUTE, value);      
		}
	}
	
	public void setValue(DateStringFields field, String value){
		setValue(field, Integer.valueOf(value));
	}
	
	public void addMinute(){
		calendar.add(Calendar.MINUTE, 1);
	}
	
	public void addHour(){
		calendar.add(Calendar.HOUR, 1);
	}
	
	public void addDay(){
		calendar.add(Calendar.DAY_OF_YEAR, 1);
	}
	
}
