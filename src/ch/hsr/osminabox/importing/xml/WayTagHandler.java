package ch.hsr.osminabox.importing.xml;


import org.xml.sax.Attributes;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.importing.EntityBuffer;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;

/**
 * A XMlTagHandler handling the the "way" tag
 * @author rhof
 */
public class WayTagHandler extends XMLTagHandler implements TagListener, NodeReferenceListener{

	private final static XMLTag xmlTag = XMLTag.getTag("way");

	private EntityBuffer<Way> wayBuffer;
	protected Way currentWay;
	
	
	public WayTagHandler(XMLTagHandler parentHandler, EntityBuffer<Way> wayBuffer) {
		super(parentHandler);
		this.wayBuffer = wayBuffer;
	}
	
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#initSubHandlers()
	 */
	@Override
	protected void initSubHandlers() {
		addSubHandler(new TagTagHandler(this, this));
		addSubHandler(new NdTagHandler(this, this));
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#handleBeginTag(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		currentWay = new Way();
		
		for(int i=0; i<attributes.getLength(); i++){
			currentWay.attributes.put(attributes.getQName(i), attributes.getValue(i));
		}
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#handleEndTag(java.lang.String)
	 */
	@Override
	public void handleEndTag(String tagName)
			throws InvalidHandlerStateException {
		wayBuffer.put(currentWay);
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#closeHandler()
	 */
	@Override
	public void closeHandler() throws InvalidHandlerStateException {
		wayBuffer.flush();
	}

	@Override
	public XMLTag getTag() {
		return xmlTag;
	}

	@Override
	public void addTag(String key, String value) {
		currentWay.putTag(key, value);
	}

	@Override
	public void addNodeReference(Node node) {
		currentWay.nodes.add(node);
	}



}
