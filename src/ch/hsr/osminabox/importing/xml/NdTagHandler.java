package ch.hsr.osminabox.importing.xml;

import org.xml.sax.Attributes;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;

/**
 * A XMlTagHandler handling the the "nd" tag
 * @author rhof
 *
 */
public class NdTagHandler extends XMLTagHandler {

	private final static XMLTag xmlTag = XMLTag.getTag("nd");
	
	private NodeReferenceListener nodeReferenceListener;
	
	public NdTagHandler(XMLTagHandler parentHandler, NodeReferenceListener nodeReferenceListener) {
		super(parentHandler);
		this.nodeReferenceListener = nodeReferenceListener;
	}
	
	@Override
	protected void initSubHandlers() {}
	
	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		Node node = new Node();
		node.setOsmId(Long.parseLong(attributes.getValue("ref")));
		nodeReferenceListener.addNodeReference(node);
	}

	@Override
	public void handleEndTag(String tagName)
			throws InvalidHandlerStateException {}
	

	
	@Override
	public void closeHandler() throws InvalidHandlerStateException {}

	@Override
	public XMLTag getTag() {
		return xmlTag;
	}





}
