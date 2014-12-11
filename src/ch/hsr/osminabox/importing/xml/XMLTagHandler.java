package ch.hsr.osminabox.importing.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.xml.sax.Attributes;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;
import ch.hsr.osminabox.importing.strategy.BufferStrategy;

/**
 * An XML Tag handler handles the occurence of an xml tag in an osm File
 * @author rhof
 *
 */
public abstract class XMLTagHandler {
	
	protected final static String IGNORETAG_BOUND = "bound";
	protected final static String IGNORETAG_BOUNDS = "bounds";
	
	private Map<XMLTag, XMLTagHandler> subHandlers = new HashMap<XMLTag, XMLTagHandler>();
	private ApplicationContext context;
	private XMLTagHandler parentHandler;
	private BufferStrategy bufferStrategy;
	
	public XMLTagHandler(XMLTagHandler parentHandler) {
		this.context = parentHandler.getApplicationContext();
		this.parentHandler = parentHandler;
		initSubHandlers();
	}
	
	public XMLTagHandler(ApplicationContext context, BufferStrategy bufferStrategy) {
		this.context = context;
		this.parentHandler = null;
		this.bufferStrategy = bufferStrategy;
		initSubHandlers();
	}
	
	public XMLTagHandler(XMLTagHandler parentHandler, BufferStrategy bufferStrategy){
		this.context = parentHandler.context;
		this.parentHandler = parentHandler;
		this.bufferStrategy = bufferStrategy;
		initSubHandlers();
	}
	
	protected BufferStrategy getBufferStrategy(){
		return bufferStrategy;
	}
	
	protected ApplicationContext getApplicationContext(){
		return this.context;
	}
	
	/**
	 * Adds a subhandler for a sub xml tag
	 * @param handler
	 */
	protected void addSubHandler(XMLTagHandler handler){
		subHandlers.put(handler.getTag(), handler);
	}
	
	public XMLTagHandler getSubHandler(XMLTag tag) throws InvalidHandlerStateException{
		XMLTagHandler subHandler = subHandlers.get(tag);
		
		if(subHandler == null){
			throw new InvalidHandlerStateException("XMLTagHandler for Tag: " 
					+ getTag().getXMLRepresentation() 
					+ " has no child tag named: " 
					+ tag.getXMLRepresentation());
		}
		return subHandler;
	}
	
	public List<XMLTagHandler> getSubHandlers(){
		return new Vector<XMLTagHandler>(subHandlers.values());
	}
	
	/**
	 * @return the Parent XMLTagHandler
	 */
	public XMLTagHandler getParentHandler(){
		return parentHandler;
	}

	/**
	 * An Implementation of this method should initialise all sub tag handlers
	 */
	protected abstract void initSubHandlers();
	public abstract XMLTag getTag();
	
	/**
	 * An Implementation of the method should handle the begin of a specific tag
	 * @param tagName Name of the Tag
	 * @param attributes The Attributes of this tag
	 * @throws InvalidHandlerStateException
	 */
	public abstract void handleBeginTag(String tagName, Attributes attributes) throws InvalidHandlerStateException;
	
	/**
	 * An Implementation of the method should handle the end of a specific tag
	 * @param tagName Name of the Tag
	 * @throws InvalidHandlerStateException
	 */
	public abstract void handleEndTag(String tagName) throws InvalidHandlerStateException;
	
	/**
	 * Handles clean up functionality after if the handler wont be used anymore
	 * @throws InvalidHandlerStateException
	 */
	public abstract void closeHandler() throws InvalidHandlerStateException;
	
	
}
