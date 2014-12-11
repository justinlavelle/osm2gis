package ch.hsr.osminabox.db.handlingstrategy;

import java.util.Map;

import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.util.DiffType;

public interface WayHandlingStrategy {

	
	public void addTemp(Way way, DiffType diffType, Map<String, StringBuffer> statements);
	
	public void addWay(Way way, Map<String, StringBuffer> statements);
}

