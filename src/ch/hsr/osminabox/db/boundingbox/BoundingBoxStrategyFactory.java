package ch.hsr.osminabox.db.boundingbox;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.context.ApplicationContext;

/**
 * Creates a Bounding box Strategy using the Parameters given to the Application
 * @author rhof, jzimmerm
 */
public class BoundingBoxStrategyFactory {
	
	private static Logger logger = Logger.getLogger(BoundingBoxStrategyFactory.class);

	public static BoundingBoxStrategy createBoundingBoxStrategy(ApplicationContext context){
		
		if(validBoundingBoxValues(context)){
				
			float latMax = Float.valueOf(context.getConfigParameter(ConfigConstants.CONF_LAT_MAX));
			float lonMin = Float.valueOf(context.getConfigParameter(ConfigConstants.CONF_LON_MIN)); 
			float latMin = Float.valueOf(context.getConfigParameter(ConfigConstants.CONF_LAT_MIN));
			float lonMax = Float.valueOf(context.getConfigParameter(ConfigConstants.CONF_LON_MAX));
		
			return new BoundingBoxStrategyImpl(latMax, lonMin, latMin, lonMax);
				
		} else 
			return new BoundingBoxStrategyNull();
	}
	
	private static boolean validBoundingBoxValues(ApplicationContext context){
		
		try{
			Float.valueOf(context.getConfigParameter(ConfigConstants.CONF_LAT_MAX));
			Float.valueOf(context.getConfigParameter(ConfigConstants.CONF_LON_MIN)); 
			Float.valueOf(context.getConfigParameter(ConfigConstants.CONF_LAT_MIN));
			Float.valueOf(context.getConfigParameter(ConfigConstants.CONF_LON_MAX));
		}
		catch(Exception e){
			logger.info("No valid BoundingBox values found. No BoundingBox will be used.");
			return false;
		}
		return true;
	}
	
}
