package ch.hsr.osminabox.importing.xml;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.importing.EntityBuffer;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;
import ch.hsr.osminabox.test.Util;

public class NodeTagHandlerTest extends EasyMockSupport{

	private NodeTagHandler handler;
	private ApplicationContext context;
	private EntityBuffer<Node> nodeBuffer;
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		context = createMock(ApplicationContext.class);
		XMLTagHandler parentHandler = createMock(XMLTagHandler.class);
		nodeBuffer = createMock(EntityBuffer.class);
		expect(parentHandler.getApplicationContext()).andReturn(context);
		replayAll();
		handler = new NodeTagHandler(parentHandler, nodeBuffer);
		verifyAll();
		resetAll();
	}

	@Test
	public void testGetTag() {
		assertEquals(XMLTag.NODE, handler.getTag());
	}

	@Test
	public void testHandleBeginTag() throws InvalidHandlerStateException {
		Attributes attrs = createMock(Attributes.class);
		expect(attrs.getLength()).andReturn(2);
		expect(attrs.getQName(0)).andReturn("attr1");
		expect(attrs.getValue(0)).andReturn("value1");
		expect(attrs.getLength()).andReturn(2);
		expect(attrs.getQName(1)).andReturn("attr2");
		expect(attrs.getValue(1)).andReturn("value2");
		expect(attrs.getLength()).andReturn(2);
		replayAll();
		handler.handleBeginTag("node", attrs);
		verifyAll();
		assertEquals("value1", handler.currentNode.attributes.get("attr1"));
		assertEquals("value2", handler.currentNode.attributes.get("attr2"));
	}

	@Test
	public void testHandleEndTag() throws InvalidHandlerStateException {
		testHandleBeginTag();
		resetAll();
		nodeBuffer.put(handler.currentNode);
		replayAll();
		handler.handleEndTag("node");
		verifyAll();
	}

	@Test
	public void testCloseHandler() throws InvalidHandlerStateException {
		nodeBuffer.flush();
		replayAll();
		handler.closeHandler();
		verifyAll();
	}

	@Test
	public void testAddTag() {
		handler.currentNode = new Node();
		handler.addTag("test", "value");
		assertEquals(1, handler.currentNode.tags.size());
		assertEquals(Util.asSet("value"), handler.currentNode.tags.get("test"));
	}

}
