package ch.hsr.osminabox.updating;

import java.util.HashMap;
import java.util.Map;

/**
 * This enums represent update frequencys used by UpdateStrategys
 * @author rhof
 *
 */
public enum UpdateFrequency {

	DAILY("daily"), HOURLY("hourly"), MINUTELY("minutely");
	
	private String strRepresentation;
	
	private static Map<String, UpdateFrequency> frequencys;
	
	static{
		frequencys = new HashMap<String, UpdateFrequency>();
		for(UpdateFrequency frequency: UpdateFrequency.values()){
			frequencys.put(frequency.getStringRepresentation(), frequency);
		}
	}
	
	private UpdateFrequency(String strRepresentation){
		this.strRepresentation = strRepresentation;
	}
	
	public String getStringRepresentation(){
		return strRepresentation;
	}
	
	public static UpdateFrequency getFrequency(String strRepresentation){
		return frequencys.get(strRepresentation);
	}
	
}
