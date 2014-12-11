package ch.hsr.osminabox.updating;

import java.util.Date;

import org.quartz.Trigger;
import org.quartz.TriggerUtils;

import ch.hsr.osminabox.context.ConfigConstants;

/**
 * Holds the Information for a hourly scheduled update
 * @author jzimmerm
 *
 */
public class HourlyUpdateStrategy extends ReplicateUpdateStrategy {

	@Override
	public Trigger getTrigger() {
		Trigger trigger = TriggerUtils.makeHourlyTrigger();
				trigger.setStartTime(new Date());
				trigger.setMisfireInstruction(Trigger.INSTRUCTION_NOOP);
				trigger.setName("HourlyTrigger");
		
		return trigger;
	}

	@Override
	public String getSubDirectoryConfigConstant() {
		return ConfigConstants.CONF_DIFF_DIRECTORY_HOURLY;
	}
}
