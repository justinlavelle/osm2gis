package ch.hsr.osminabox.importing.xml;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.importing.EntityBuffer;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;

public class WayTagHandlerTest extends EasyMockSupport {

	private WayTagHandler handler;
	private XMLTagHandler parentHandler;
	private ApplicationContext context;
	private EntityBuffer<Way> wayBuffer;

	@Before
	public void setUp() throws Exception {
		context = createMock(ApplicationContext.class);
		parentHandler = createMock(XMLTagHandler.class);
		wayBuffer = createMock(EntityBuffer.class);
		expect(parentHandler.getApplicationContext()).andReturn(context);
		replayAll();
		handler = new WayTagHandler(parentHandler, wayBuffer);
		verifyAll();
		resetAll();
	}

	@Test
	public void testGetTag() {
		assertEquals(XMLTag.WAY, handler.getTag());
	}

	@Test
	public void testHandleBeginTag() throws InvalidHandlerStateException {
		Attributes attrs = createMock(Attributes.class);
		expect(attrs.getLength()).andReturn(2);
		expect(attrs.getQName(0)).andReturn("k1");
		expect(attrs.getValue(0)).andReturn("v1");
		expect(attrs.getLength()).andReturn(2);
		expect(attrs.getQName(1)).andReturn("k2");
		expect(attrs.getValue(1)).andReturn("v2");
		expect(attrs.getLength()).andReturn(2);
		replayAll();
		handler.handleBeginTag("way", attrs);
		verifyAll();
		assertEquals("v1", handler.currentWay.attributes.get("k1"));
		assertEquals("v2", handler.currentWay.attributes.get("k2"));
	}

	@Test
	public void testHandleEndTag() throws InvalidHandlerStateException {
		handler.currentWay = new Way();
		wayBuffer.put(handler.currentWay);
		replayAll();
		handler.handleEndTag("way");
		verifyAll();
	}

	@Test
	public void testCloseHandler() throws InvalidHandlerStateException {
		wayBuffer.flush();
		replayAll();
		handler.closeHandler();
		verifyAll();
	}

	@Test
	public void testAddTag() {
		handler.currentWay = new Way();
		handler.addTag("tag", "value");
		assertEquals(1, handler.currentWay.tags.size());
		assertEquals(Util.asSet("value"), handler.currentWay.tags.get("tag"));
	}

	@Test
	public void testAddNodeReference() {
		handler.currentWay = new Way();
		Node node = NodeCreator.create(1, 45, 8).finish();
		handler.addNodeReference(node);
		assertTrue(handler.currentWay.nodes.contains(node));
	}

}
