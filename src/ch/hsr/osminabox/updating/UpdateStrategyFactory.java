package ch.hsr.osminabox.updating;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.context.ConfigConstants;

/**
 * This factory instantiates an UpdateStrategy by a given parameter.
 * 
 * @author rhof
 * 
 */
public class UpdateStrategyFactory {

	public static UpdateStrategy createUpdateStrategy(ApplicationContext context) {

		UpdateStrategy strategy;
		String parameter = context
				.getConfigParameter(ConfigConstants.CONF_DIFF_FREQUENCY);
		UpdateFrequency frequency = UpdateFrequency.getFrequency(parameter);
		switch (frequency) {
		case DAILY:
			strategy = new DailyUpdateStrategy(getHour(context), getMinute(context));
			break;
		case HOURLY:
			strategy = new HourlyUpdateStrategy();
			break;
		case MINUTELY:
			strategy = new MinutelyUpdateStrategy();
			break;
		default:
			strategy = null;
		}
		
		return strategy;

	}

	private static int getHour(ApplicationContext context) {
		return readIntParam(context, true, 13);
	}

	private static int getMinute(ApplicationContext context) {
		return readIntParam(context, false, 15);
	}

	private static int readIntParam(ApplicationContext context, boolean isHour,
			int defaultValue) {
		try {
			return Integer.valueOf(context.getConfigParameter(
					"osm2gis.scheduler.daily.time").split(":")[isHour ? 0 : 1]);
		} catch (Exception e) {
			return defaultValue;
		}
	}

}
