package ch.hsr.osminabox.db.handlingstrategy;

import java.util.Map;

import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.util.DiffType;

public interface RelationHandlingStrategy {
	
	public void addRelation(Relation relation, Map<String, StringBuffer> statements);
	
	public void addTemp(Relation relation, DiffType diffType, Map<String, StringBuffer> statements);
	
	public void addTempMembers(Relation relation, Map<String, StringBuffer> statements);
	
	public void addJoins(Relation relation, Map<String, StringBuffer> statements);

}
