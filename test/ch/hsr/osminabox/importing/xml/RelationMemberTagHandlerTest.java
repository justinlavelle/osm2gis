package ch.hsr.osminabox.importing.xml;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.RelationMemberType;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;

public class RelationMemberTagHandlerTest extends EasyMockSupport{

	private RelationMemberTagHandler handler;
	private RelationMemberListener listener;
	private OSMTagHandler parentHandler;
	private ApplicationContext context;
	@Before
	public void setUp() throws Exception {
		context = createMock(ApplicationContext.class);
		parentHandler = createMock(OSMTagHandler.class);
		listener = createMock(RelationMemberListener.class);
		expect(parentHandler.getApplicationContext()).andReturn(context);
		replayAll();
		handler = new RelationMemberTagHandler(parentHandler, listener);
		verifyAll();
		resetAll();
	}

	@Test
	public void testGetTag() {
		assertEquals(XMLTag.RELATION_MEMBER, handler.getTag());
	}

	@Test
	public void testHandleBeginTag() throws InvalidHandlerStateException {
		testRelationMemberHandling("way", RelationMemberType.WAY);
		testRelationMemberHandling("node", RelationMemberType.NODE);
		testRelationMemberHandling("relation", RelationMemberType.RELATION);
		testRelationMemberHandling("role_is_not_known", RelationMemberType.UNKNOWN);
	}

	private void testRelationMemberHandling(String typeName, RelationMemberType type)
			throws InvalidHandlerStateException {
		Attributes attrs = createMock(Attributes.class);
		expect(attrs.getValue("ref")).andReturn("2");
		expect(attrs.getValue("type")).andReturn(typeName);
		expect(attrs.getValue("role")).andReturn("testrole");
		Capture<RelationMember> capture = new Capture<RelationMember>();
		listener.addRelationMember(capture(capture));
		replayAll();
		handler.handleBeginTag("tagname", attrs);
		verifyAll();
		RelationMember member = capture.getValue();
		assertEquals(2, member.osmId);
		assertEquals(type, member.type);
		assertEquals("testrole", member.role);
		resetAll();
	}

}
