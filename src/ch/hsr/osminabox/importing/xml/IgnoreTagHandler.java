package ch.hsr.osminabox.importing.xml;

import org.xml.sax.Attributes;

import ch.hsr.osminabox.importing.InvalidHandlerStateException;

/**
 * Handles the "bound" tag. This tag will be ignored.
 * @author rhof
 *
 */
public class IgnoreTagHandler extends XMLTagHandler {

	private XMLTag tag;
	
	public IgnoreTagHandler(XMLTagHandler parentHandler, String tag) {
		super(parentHandler);
		this.tag = XMLTag.getTag(tag);
	}

	@Override public XMLTag getTag() {return tag;}
	@Override public void closeHandler() throws InvalidHandlerStateException {}
	@Override public void handleBeginTag(String tagName, Attributes attributes) throws InvalidHandlerStateException {}
	@Override public void handleEndTag(String tagName) throws InvalidHandlerStateException {}
	@Override protected void initSubHandlers() {}

}
