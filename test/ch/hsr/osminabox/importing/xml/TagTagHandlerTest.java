package ch.hsr.osminabox.importing.xml;

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;

public class TagTagHandlerTest extends EasyMockSupport {

	private TagTagHandler handler;
	private XMLTagHandler parentHandler;
	private TagListener listener;
	private ApplicationContext context;
	@Before
	public void setUp() throws Exception {
		context = createMock(ApplicationContext.class);
		parentHandler = createMock(XMLTagHandler.class);
		listener = createMock(TagListener.class);
		expect(parentHandler.getApplicationContext()).andReturn(context);
		replayAll();
		handler = new TagTagHandler(parentHandler, listener);
		verifyAll();
		resetAll();
	}

	@Test
	public void testHandleBeginTag() throws InvalidHandlerStateException {
		Attributes attrs = createMock(Attributes.class);
		expect(attrs.getValue("k")).andReturn("key");
		expect(attrs.getValue("v")).andReturn("value");
		listener.addTag("key", "value");
		replayAll();
		handler.handleBeginTag("tag", attrs);
		verifyAll();
	}

}
