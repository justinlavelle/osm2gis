package ch.hsr.osminabox.db.mapping;

import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.schemamapping.xml.Mapping;

public class GeneralMappingAppender implements MappingAppender {

	@Override
	public void appendMapping(OSMEntity entity, Mapping mapping) {
		entity.dbMappings.put(mapping.getDstTable().getName(), mapping.getDstColumns().getColumn());
	}

}
