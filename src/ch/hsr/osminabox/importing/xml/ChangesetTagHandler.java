package ch.hsr.osminabox.importing.xml;

import org.xml.sax.Attributes;

import ch.hsr.osminabox.importing.InvalidHandlerStateException;

/**
 * Handles the "changset" tag. This tag will be ignored.
 * @author rhof
 *
 */
public class ChangesetTagHandler extends XMLTagHandler {

	private static XMLTag tag = XMLTag.getTag("changeset");
	
	public ChangesetTagHandler(XMLTagHandler parentHandler) {
		super(parentHandler);
	}

	@Override public XMLTag getTag() {return tag;}
	@Override public void closeHandler() throws InvalidHandlerStateException {}
	@Override public void handleBeginTag(String tagName, Attributes attributes) throws InvalidHandlerStateException {}
	@Override public void handleEndTag(String tagName) throws InvalidHandlerStateException {}
	@Override protected void initSubHandlers() {
		addSubHandler(new TagTagHandler(this, new NullTagListener()));
	}


}
