package ch.hsr.osminabox.db.mapping;

import java.util.HashMap;

import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMappingEntry;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.Members;
import ch.hsr.osminabox.schemamapping.xml.RelatedTable;

public class RelationMappingAppender implements MappingAppender {

	@Override
	public void appendMapping(OSMEntity entity, Mapping mapping) {
		Relation relation = (Relation)entity;
		
		RelationMappingEntry mappingEntry = new RelationMappingEntry();
		mappingEntry.mappingColumns = mapping.getDstColumns().getColumn();
		mappingEntry.allMembersRequiered = false;
		
		Members members = mapping.getMembers();
		if(members != null){
			mappingEntry.relatedTables = members.getRelatedTable();
			
			for(RelatedTable relatedTable : mappingEntry.relatedTables){
				mappingEntry.dbIdsToMember.put(relatedTable.getName(), new HashMap<Integer, RelationMember>());
			}
			
			Boolean allRequired = members.isAllRequired();
			if(allRequired != null)
				mappingEntry.allMembersRequiered = allRequired;		
		}
		
		relation.dbMappings.put(mapping.getDstTable().getName(), mappingEntry);
	}

}
