package ch.hsr.osminabox.importing.xml;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;

/**
 * The Main XMLTagHandler if a OSMChange tag is beeing processed.
 * @author rhof
 *
 */
public class OSMChangeTagHandler extends XMLTagHandler {

	private final static XMLTag xmlTag = XMLTag.getTag("osmChange");

	private final static Logger logger = Logger.getLogger(OSMChangeTagHandler.class);
	
	private XMLTagHandler currentSubHandler=null;
	
	public OSMChangeTagHandler(ApplicationContext context) {
		super(context, null);
	}
	
	@Override
	protected void initSubHandlers() {
		addSubHandler(new IgnoreTagHandler(this, IGNORETAG_BOUND));
		addSubHandler(new IgnoreTagHandler(this, IGNORETAG_BOUNDS));
		addSubHandler(new CreateTagHandler(this));
		addSubHandler(new ModifyTagHandler(this));
		addSubHandler(new DeleteTagHandler(this));
	}
	
	@Override
	public XMLTagHandler getSubHandler(XMLTag tag) {
	
		try {
			currentSubHandler = super.getSubHandler(tag);
		} catch (InvalidHandlerStateException e) {

		}
		
		return currentSubHandler;
	}
	
	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		logger.debug("Begin OSM Change Tag Handling");
	}

	@Override
	public void handleEndTag(String tagName)
			throws InvalidHandlerStateException {
		logger.debug("End OSM Change Tag Handling");
	}
	
	@Override
	public void closeHandler() throws InvalidHandlerStateException {
		// Flush the current Subhandler.
		currentSubHandler.closeHandler();
		
		logger.debug("End OSM Change Tag Handling");
	}

	@Override
	public XMLTag getTag() {
		return xmlTag;
	}





}
