package ch.hsr.osminabox.importing.xml;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.exceptions.InvalidRelationException;
import ch.hsr.osminabox.importing.EntityBuffer;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;
import ch.hsr.osminabox.importing.util.RelationTagUtil;

/**
 * A XMlTagHandler handling the the "relation" tag
 * @author rhof
 *
 */
public class RelationTagHandler extends XMLTagHandler implements TagListener, RelationMemberListener{

	private static Logger logger = Logger.getLogger(RelationTagHandler.class);
	
	private final static XMLTag xmlTag = XMLTag.getTag("relation");
	
	private EntityBuffer<Relation> relationBuffer;
	private EntityBuffer<Area> areaBuffer;
	protected Relation currentRelation;
	
	protected RelationTagUtil relationTagUtil;
	
	public RelationTagHandler(XMLTagHandler parentHandler, EntityBuffer<Relation> relationBuffer, EntityBuffer<Area> areaBuffer) {
		super(parentHandler);
		this.relationBuffer = relationBuffer;
		this.areaBuffer = areaBuffer;
		
		relationTagUtil = new RelationTagUtil();
	}
	
	@Override
	protected void initSubHandlers() {
		addSubHandler(new RelationMemberTagHandler(this, this));
		addSubHandler(new TagTagHandler(this, this));
	}
	
	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		currentRelation = new Relation();
		
		for(int i=0; i<attributes.getLength(); i++){
			currentRelation.attributes.put(attributes.getQName(i), attributes.getValue(i));
		}
	}

	@Override
	public void handleEndTag(String tagName) throws InvalidHandlerStateException {
		
		if(relationTagUtil.isAreaCandidate(currentRelation)){
			try {
				areaBuffer.put(new Area(currentRelation));
			} catch (InvalidRelationException e) {
				logger.error("Relation couldn't be converted into an Area.");
			}
		}
		relationBuffer.put(currentRelation);
	}
	
	@Override
	public void closeHandler() throws InvalidHandlerStateException {
		areaBuffer.flush();
		relationBuffer.flush();
	}

	@Override
	public XMLTag getTag() {
		return xmlTag;
	}


	@Override
	public void addTag(String key, String value) {
		currentRelation.putTag(key, value);
	}

	@Override
	public void addRelationMember(RelationMember member) {
		currentRelation.members.add(member);
	}
}
