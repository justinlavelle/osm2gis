package ch.hsr.osminabox.importing.util;

import java.util.HashSet;
import java.util.Set;

import ch.hsr.osminabox.db.entities.Relation;

public class RelationTagUtil {
	
	private static final String AREA_CANDIDATE_TAG_TYPE = "type";
	
	private static final String AREA_CANDIDATE_TAG_TYPE_MULTIPOLYGON = "multipolygon";
	private static final String AREA_CANDIDATE_TAG_TYPE_BOUNDARY = "boundary";
	
	private Set<String> areaTypes = new HashSet<String>();
	
	public RelationTagUtil(){
		areaTypes.add(AREA_CANDIDATE_TAG_TYPE_MULTIPOLYGON);
		areaTypes.add(AREA_CANDIDATE_TAG_TYPE_BOUNDARY);
	}
	
	/**
	 * Checks if a relation has the needed type-tags to be treated as an area.
	 * @param relation
	 * @return true if this relation should be converted into an area object.
	 */
	public boolean isAreaCandidate(Relation relation) {
		if(relation.tags.containsKey(AREA_CANDIDATE_TAG_TYPE)){
			if(areaTypes.containsAll(relation.tags.get(AREA_CANDIDATE_TAG_TYPE))){
				return true;
			}
		}
		return false;
	}

}
