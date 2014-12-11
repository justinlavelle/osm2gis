package ch.hsr.osminabox.importing.xml;

import ch.hsr.osminabox.db.entities.RelationMember;

/**
 * An Interface which should be used by XMLTagHandlers to retrieve relation members from
 * a sub XMLTagHandler.
 * @author rhof
 *
 */
public interface RelationMemberListener {

	public void addRelationMember(RelationMember member);
	
}
