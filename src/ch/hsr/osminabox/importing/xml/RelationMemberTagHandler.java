package ch.hsr.osminabox.importing.xml;

import org.xml.sax.Attributes;

import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.RelationMemberType;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;

/**
 * A XMlTagHandler handling the the "member" tag
 * @author rhof
 *
 */
public class RelationMemberTagHandler extends XMLTagHandler {

	private final static XMLTag xmlTag = XMLTag.getTag("member");
	
	private RelationMemberListener relationMemberListener;
	
	public RelationMemberTagHandler(XMLTagHandler parentHandler, RelationMemberListener relationMemberListener) {
		super(parentHandler);
		this.relationMemberListener = relationMemberListener;
	}
	
	@Override
	protected void initSubHandlers() {}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#handleBeginTag(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		RelationMember member = new RelationMember();
		member.osmId = Long.valueOf(attributes.getValue("ref"));
		member.type = getRelationMemberType(attributes.getValue("type"));
		member.role = attributes.getValue("role");
		relationMemberListener.addRelationMember(member);
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#handleEndTag(java.lang.String)
	 */
	@Override
	public void handleEndTag(String tagName)
			throws InvalidHandlerStateException {}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#closeHandler()
	 */
	@Override
	public void closeHandler() throws InvalidHandlerStateException {}


	@Override
	public XMLTag getTag() {
		return xmlTag;
	}

	private RelationMemberType getRelationMemberType(String type){
		if(type.equals("node")){
			return RelationMemberType.NODE;
		}
		if(type.equals("way")){
			return RelationMemberType.WAY;
		}
		if(type.equals("relation")){
			return RelationMemberType.RELATION;
		}
		return RelationMemberType.UNKNOWN;
	}

}
