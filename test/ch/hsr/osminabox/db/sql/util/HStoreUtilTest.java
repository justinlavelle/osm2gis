package ch.hsr.osminabox.db.sql.util;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.easymock.EasyMockSupport;
import org.easymock.IMockBuilder;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.DBConstants;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.WayCreator;

public class HStoreUtilTest extends EasyMockSupport {

	private HStoreUtil util;
	private Connection conn;

	@Before
	public void setUp() throws Exception {
		conn = createMock(Connection.class);
	}

	private IMockBuilder<HStoreUtil> createUtilMockBuilder() {
		return createMockBuilder(HStoreUtil.class).withConstructor(conn)
				.addMockedMethod("exec");
	}

	@Test
	public void testAddTagsFromTempWay() throws SecurityException,
			NoSuchMethodException {
		initMock();
		Way way = WayCreator.create(1).finish();
		util.addTagsFromTemp(way, DBConstants.WAY_TEMP);
		expectLastCall().times(1);
		replayAll();
		util.addTagsFromTemp(way);
		verifyAll();
	}

	private HStoreUtil initMock() throws NoSuchMethodException {
		util = createUtilMockBuilder().addMockedMethod(
				HStoreUtil.class.getDeclaredMethod("addTagsFromTemp",
						OSMEntity.class, String.class)).createMock();
		return util;
	}

	@Test
	public void testAddTagsFromTempRelation() throws NoSuchMethodException {
		initMock();
		Relation r = new Relation();
		util.addTagsFromTemp(r, DBConstants.RELATION_TEMP);
		expectLastCall().times(1);
		replayAll();
		util.addTagsFromTemp(r);
		verifyAll();
	}

	@Test
	public void testAddTagsFromTempWithNullResultSet() throws Exception {
		util = createUtilMockBuilder().createMock();
		expect(util.exec("SELECT * FROM each((SELECT keyvalue FROM testtable WHERE osm_id=1 LIMIT 1));")).andReturn(null);
		replayAll();
		util.addTagsFromTemp(NodeCreator.create(1).finish(), "testtable");
		verifyAll();
	}
	@Test
	public void testAddTagsFromTempWithResultSet() throws Exception {
		util = createUtilMockBuilder().createMock();
		expect(util.exec("SELECT * FROM each((SELECT keyvalue FROM testtable WHERE osm_id=1 LIMIT 1));")).andReturn(createResultSetMock());
		replayAll();
		Node finish = NodeCreator.create(1).finish();
		util.addTagsFromTemp(finish, "testtable");
		verifyAll();
		assertTrue(finish.tags.get("k1").contains("v1"));
		assertTrue(finish.tags.get("k2").contains("v2"));
	}

	private ResultSet createResultSetMock() throws SQLException {
		ResultSet rs = createMock(ResultSet.class);
		expect(rs.next()).andReturn(true);
		expect(rs.getString(HStoreUtil.HSTORE_KEY)).andReturn("k1");
		expect(rs.getString(HStoreUtil.HSTORE_VALUE)).andReturn("v1");
		expect(rs.next()).andReturn(true);
		expect(rs.getString(HStoreUtil.HSTORE_KEY)).andReturn("k2");
		expect(rs.getString(HStoreUtil.HSTORE_VALUE)).andReturn("v2");
		expect(rs.next()).andReturn(false);
		return rs;
	}
}
