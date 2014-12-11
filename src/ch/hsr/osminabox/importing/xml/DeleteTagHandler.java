package ch.hsr.osminabox.importing.xml;

import org.xml.sax.Attributes;

import ch.hsr.osminabox.importing.InvalidHandlerStateException;
import ch.hsr.osminabox.importing.strategy.DeleteBufferStrategy;

/**
 * A Handler initiatiating the delete buffer strategy and delegating the occourence of
 * a tag to the Main handler
 * 
 * @author rhof
 *
 */
public class DeleteTagHandler extends XMLTagHandler {

	private final static XMLTag xmlTag = XMLTag.getTag("delete");
	
	private OSMTagHandler delegate;
	
	public DeleteTagHandler(XMLTagHandler parentHandler) {
		super(parentHandler);
		delegate = new OSMTagHandler(parentHandler, new DeleteBufferStrategy(parentHandler.getApplicationContext()));
	}
	
	@Override
	protected void initSubHandlers() {

	}
	
	@Override
	public XMLTagHandler getSubHandler(XMLTag tag)
			throws InvalidHandlerStateException {
		return delegate.getSubHandler(tag);
	}
	
	@Override
	public void closeHandler() throws InvalidHandlerStateException {
		delegate.closeHandler();
	}

	@Override
	public XMLTag getTag() {
		return xmlTag;
	}

	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		delegate.handleBeginTag(tagName, attributes);
	}

	@Override
	public void handleEndTag(String tagName)
			throws InvalidHandlerStateException {
		delegate.handleEndTag(tagName);
	}



}
