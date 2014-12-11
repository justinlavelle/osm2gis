package ch.hsr.osminabox.importing.xml;

import org.xml.sax.Attributes;

import ch.hsr.osminabox.importing.InvalidHandlerStateException;

/**
 * A XMlTagHandler handling the the "tag" tag
 * @author rhof
 *
 */
public class TagTagHandler extends XMLTagHandler {

	private final static XMLTag xmlTag = XMLTag.getTag("tag");
	private TagListener tagListener;
	
	public TagTagHandler(XMLTagHandler parentHandler, TagListener tagListener) {
		super(parentHandler);
		this.tagListener = tagListener;
	}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#initSubHandlers()
	 */
	@Override
	protected void initSubHandlers() {}
	
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#closeHandler()
	 */
	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#closeHandler()
	 */
	@Override
	public void closeHandler() throws InvalidHandlerStateException {}

	@Override
	public XMLTag getTag() {
		return xmlTag;
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#handleBeginTag(java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		tagListener.addTag(attributes.getValue("k"), attributes.getValue("v"));
	}

	/* (non-Javadoc)
	 * @see ch.hsr.osminabox.importer.xml.XMLTagHandler#handleEndTag(java.lang.String)
	 */
	@Override
	public void handleEndTag(String tagName)
			throws InvalidHandlerStateException {}



}
