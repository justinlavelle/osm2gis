package ch.hsr.osminabox.importing.xml;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;

public class CreateTagHandlerTest extends EasyMockSupport{

	private ApplicationContext context;
	private DBService service;
	private CreateTagHandler handler;
	private OSMTagHandler delegate;
	@Before
	public void setUp() throws Exception {
		service = createMock(DBService.class);
		context = createMock(ApplicationContext.class);
		XMLTagHandler parentHandler = createMock(XMLTagHandler.class);
		
		expect(parentHandler.getApplicationContext()).andReturn(context);
		expect(parentHandler.getApplicationContext()).andReturn(context);
		expect(context.getDBService()).andReturn(service);
		expect(context.getConfigParameter(ConfigConstants.CONF_BUFFERSIZE_NODE)).andReturn("2");
		expect(context.getConfigParameter(ConfigConstants.CONF_BUFFERSIZE_WAY)).andReturn("2");
		expect(context.getConfigParameter(ConfigConstants.CONF_BUFFERSIZE_RELATION)).andReturn("2");
		expect(context.getConfigParameter(ConfigConstants.CONF_BUFFERSIZE_AREA)).andReturn("2");
		replayAll();
		handler = new CreateTagHandler(parentHandler);
		verifyAll();
		resetAll();
		delegate = createMock(OSMTagHandler.class);
		handler.delegate = delegate;
	}

	@Test
	public void testGetSubHandler() throws InvalidHandlerStateException {
		expect(delegate.getSubHandler(XMLTag.NODE)).andReturn(null);
		replayAll();
		XMLTagHandler h = handler.getSubHandler(XMLTag.NODE);
		verifyAll();
		assertNull(h);
	}

	@Test
	public void testGetTag() {
		assertEquals(XMLTag.CREATE, handler.getTag());
	}

	@Test
	public void testHandleBeginTag() throws InvalidHandlerStateException {
		delegate.handleBeginTag("tagname", null);
		replayAll();
		handler.handleBeginTag("tagname", null);
		verifyAll();
	}

	@Test
	public void testHandleEndTag() throws InvalidHandlerStateException {
		delegate.handleEndTag("tagname");
		replayAll();
		handler.handleEndTag("tagname");
		verifyAll();
	}

}
