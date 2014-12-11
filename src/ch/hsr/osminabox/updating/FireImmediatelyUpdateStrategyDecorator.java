package ch.hsr.osminabox.updating;

import org.quartz.Trigger;
import org.quartz.TriggerUtils;

public class FireImmediatelyUpdateStrategyDecorator implements UpdateStrategy {

	private UpdateStrategy target;
	
	public FireImmediatelyUpdateStrategyDecorator(UpdateStrategy target){
		this.target = target;
	}
	@Override
	public String getNextUpdateFile(String lastUpdateFile) {
		return target.getNextUpdateFile(lastUpdateFile);
	}

	@Override
	public String getSubDirectoryConfigConstant() {
		return target.getSubDirectoryConfigConstant();
	}

	@Override
	public Trigger getTrigger() {
		Trigger trigger = TriggerUtils.makeImmediateTrigger(1, 1000);
		trigger.setName("FireImmediately:"+target.getTrigger().getName());
		return trigger;
	}

	@Override
	public String getUpdateFileAsUrl(String updateFile) {
		return target.getUpdateFileAsUrl(updateFile);
	}

}
