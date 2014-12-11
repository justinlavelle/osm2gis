package ch.hsr.osminabox.parsing;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.hsr.osminabox.importing.InvalidHandlerStateException;
import ch.hsr.osminabox.importing.xml.XMLTag;
import ch.hsr.osminabox.importing.xml.XMLTagHandler;

/**
 * The Main EventHandler. It receives the Events from the SAX Parser and delegates them
 * to the XMLTagHandlers.
 * @author rhof
 *
 */
public class SAXEventHandler extends DefaultHandler {

	private static Logger logger = Logger.getLogger(SAXEventHandler.class);
	
	private XMLTagHandler rootHandler;
	private XMLTagHandler activeHandler;
	
	public SAXEventHandler(XMLTagHandler rootHandler) {
		this.rootHandler = rootHandler;
		this.activeHandler = rootHandler;
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {		
		super.startElement(uri, localName, name, attributes);

		XMLTag tag = XMLTag.getTag(name);
		
		if(tag == null){
			//Tag is not recognized
			logger.warn("Tag not recognized: " + name);
			return;
		}
		
		try {
			
			if(activeHandler.getTag() != tag){			
				activeHandler = activeHandler.getSubHandler(tag);
			}
			activeHandler.handleBeginTag(name, attributes);
			
		} catch (InvalidHandlerStateException e) {
			logger.error("Error while retrieving sub tag!");
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(uri, localName, name);
		
		try {
			activeHandler.handleEndTag(name);
			if(activeHandler != rootHandler){
				activeHandler = activeHandler.getParentHandler();
			}
		} catch (InvalidHandlerStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
