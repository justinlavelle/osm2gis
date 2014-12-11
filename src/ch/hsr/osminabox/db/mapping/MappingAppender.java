package ch.hsr.osminabox.db.mapping;

import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.schemamapping.xml.Mapping;

public interface MappingAppender {
	public void appendMapping(OSMEntity entity, Mapping mapping);
}
