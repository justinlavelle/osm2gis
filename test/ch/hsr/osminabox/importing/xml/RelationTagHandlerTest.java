package ch.hsr.osminabox.importing.xml;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.log4j.BasicConfigurator;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.db.XMLConstants;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.importing.EntityBuffer;
import ch.hsr.osminabox.importing.InvalidHandlerStateException;
import ch.hsr.osminabox.importing.util.RelationTagUtil;
import ch.hsr.osminabox.test.Util;

public class RelationTagHandlerTest extends EasyMockSupport{

	private RelationTagHandler handler;
	private OSMTagHandler parentHandler;
	private EntityBuffer<Relation> relationBuffer;
	private EntityBuffer<Area> areaBuffer;
	private ApplicationContext context;
	private RelationTagUtil util;
	@Before
	public void setUp() throws Exception {
		BasicConfigurator.configure();
		parentHandler = createMock(OSMTagHandler.class);
		relationBuffer = createMock(EntityBuffer.class);
		areaBuffer = createMock(EntityBuffer.class);
		util = createMock(RelationTagUtil.class);
		expect(parentHandler.getApplicationContext()).andReturn(context);
		replayAll();
		handler = new RelationTagHandler(parentHandler, relationBuffer, areaBuffer);
		handler.relationTagUtil = util;
		verifyAll();
		resetAll();
	}

	@Test
	public void testGetTag() {
		assertEquals(XMLTag.RELATION, handler.getTag());
	}

	@Test
	public void testHandleBeginTag() throws InvalidHandlerStateException {
		Attributes attrs = createMock(Attributes.class);
		expect(attrs.getLength()).andReturn(2);
		expect(attrs.getQName(0)).andReturn("tag1");
		expect(attrs.getValue(0)).andReturn("value1");
		expect(attrs.getLength()).andReturn(2);
		expect(attrs.getQName(1)).andReturn("tag2");
		expect(attrs.getValue(1)).andReturn("value2");
		expect(attrs.getLength()).andReturn(2);
		replayAll();
		handler.handleBeginTag("relation", attrs);
		verifyAll();
		Relation r = handler.currentRelation;
		assertNotNull(r);
		assertEquals("value1", r.attributes.get("tag1"));
		assertEquals("value2", r.attributes.get("tag2"));
	}

	@Test
	public void testHandleEndTagWithAreaCandidate() throws InvalidHandlerStateException {
		Relation r = new Relation();
		r.tags.put(XMLConstants.TAG_TYPE, Util.asSet(XMLConstants.TAG_TYPE_MULTIPOLYGON));
		handler.currentRelation = r;
		expect(util.isAreaCandidate(r)).andReturn(true);
		Capture<Area> capture = new Capture<Area>();
		areaBuffer.put(capture(capture));
		relationBuffer.put(r);
		replayAll();
		handler.handleEndTag("relation");
		verifyAll();
		Area a = capture.getValue();
		assertEquals(r.attributes, a.attributes);
		assertEquals(r.tags, a.originalTags);
	}
	
	@Test
	public void testHandleEndTagWithoutAreaCandidate() throws InvalidHandlerStateException {
		Relation r = new Relation();
		handler.currentRelation = r;
		expect(util.isAreaCandidate(r)).andReturn(false);
		relationBuffer.put(r);
		replayAll();
		handler.handleEndTag("relation");
		verifyAll();
	}

	@Test
	public void testCloseHandler() throws InvalidHandlerStateException {
		areaBuffer.flush();
		relationBuffer.flush();
		replayAll();
		handler.closeHandler();
		verifyAll();
	}

	@Test
	public void testAddTag() {
		handler.currentRelation = new Relation();
		handler.addTag("test", "value");
		assertEquals(Util.asSet("value"), handler.currentRelation.tags.get("test"));
	}

	@Test
	public void testAddRelationMember() {
		handler.currentRelation = new Relation();
		RelationMember member = new RelationMember();
		handler.addRelationMember(member);
		assertEquals(member, handler.currentRelation.members.get(0));
		assertEquals(1, handler.currentRelation.members.size());
	}

}
