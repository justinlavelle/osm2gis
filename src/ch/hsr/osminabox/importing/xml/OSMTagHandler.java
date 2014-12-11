package ch.hsr.osminabox.importing.xml;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;
import ch.hsr.osminabox.importing.strategy.BufferStrategy;

/**
 * The Main XMLTagHandler for the OSM Tag
 * @author rhof
 */
public class OSMTagHandler extends XMLTagHandler {

	private final static XMLTag xmlTag = XMLTag.getTag("osm");
	private final static Logger logger = Logger.getLogger(OSMTagHandler.class);
	
	private XMLTagHandler currentSubHandler;
	
	public OSMTagHandler(ApplicationContext context, BufferStrategy bufferStrategy) {
		super(context, bufferStrategy);	
	}
	
	public OSMTagHandler(XMLTagHandler parentHandler, BufferStrategy bufferStrategy){
		super(parentHandler, bufferStrategy);
	}
	
	@Override
	protected void initSubHandlers(){
		addSubHandler(new IgnoreTagHandler(this, IGNORETAG_BOUND));
		addSubHandler(new IgnoreTagHandler(this, IGNORETAG_BOUNDS));
		addSubHandler(new ChangesetTagHandler(this));
		addSubHandler(new NodeTagHandler(this, getBufferStrategy().getNodeBuffer()));
		addSubHandler(new WayTagHandler(this, getBufferStrategy().getWayBuffer()));
		addSubHandler(new RelationTagHandler(this, getBufferStrategy().getRelationBuffer(), getBufferStrategy().getAreaBuffer()));
	}
	
	@Override
	public void closeHandler() throws InvalidHandlerStateException {
		currentSubHandler.closeHandler();
	}


	@Override
	public XMLTagHandler getSubHandler(XMLTag tag) {
		try{
		
			if(currentSubHandler == null){
				currentSubHandler = super.getSubHandler(tag);
			} else {
				
				XMLTagHandler nextSubHandler = super.getSubHandler(tag);
				if(nextSubHandler.getTag() != currentSubHandler.getTag()){
					currentSubHandler.closeHandler();
					currentSubHandler = nextSubHandler;
				}
			}
		
		} catch(InvalidHandlerStateException e){
			
		}
		
		return currentSubHandler;
	}

	@Override
	public void handleBeginTag(String tagName, Attributes attributes)
			throws InvalidHandlerStateException {
		logger.debug("Beginn OSM Tag Handling");
	}

	@Override
	public void handleEndTag(String tagName)
			throws InvalidHandlerStateException {

		currentSubHandler.closeHandler();
		logger.debug("End OSM Tag Handling");
	}
	
	
	@Override
	public XMLTag getTag() {
		return xmlTag;
	}

}
