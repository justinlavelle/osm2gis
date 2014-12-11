package ch.hsr.osminabox.importing.xml;

import org.xml.sax.Attributes;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.importing.EntityBuffer;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;

/**
 * A XMlTagHandler handling the the "node" tag
 * 
 * @author rhof
 * 
 */
public class NodeTagHandler extends XMLTagHandler implements TagListener {

	private final static XMLTag xmlTag = XMLTag.getTag("node");

	private EntityBuffer<Node> nodeBuffer;
	protected Node currentNode;

	// private BoundingBoxStrategy bboxStrategy;

	public NodeTagHandler(XMLTagHandler parentHandler,
			EntityBuffer<Node> nodeBuffer) {
		super(parentHandler);
		this.nodeBuffer = nodeBuffer;
	}

	@Override
	protected void initSubHandlers() {
		addSubHandler(new TagTagHandler(this, this));
	}

	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		currentNode = new Node();

		for (int i = 0; i < attributes.getLength(); i++) {
			currentNode.attributes.put(attributes.getQName(i), attributes
					.getValue(i));
		}
	}

	@Override
	public void handleEndTag(String tagName)
			throws InvalidHandlerStateException {
		nodeBuffer.put(currentNode);
	}

	@Override
	public void closeHandler() throws InvalidHandlerStateException {
		nodeBuffer.flush();
	}

	@Override
	public XMLTag getTag() {
		return xmlTag;
	}

	@Override
	public void addTag(String key, String value) {
		currentNode.putTag(key, value);
	}

}
