package ch.hsr.osminabox.db.differentialupdate;

import static org.easymock.EasyMock.expect;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;
import ch.hsr.osminabox.db.sql.area.GeomStrategy;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;
import ch.hsr.osminabox.test.WayCreator;

public class UpdateAreaHandlingStrategyTest extends
		UpdateXXXHandlingStrategyTestTemplate {

	private UpdateAreaHandlingStrategy update;
	protected GeomStrategy geomStrategy;

	@Before
	public void setUp() throws Exception {
		createMocks();
		geomStrategy = new GeomStrategy() {

			@Override
			public StringBuffer getGeom(ch.hsr.osminabox.db.entities.Area area)
					throws NoWayValuesException {
				throw new RuntimeException("Should never get invoked!");
			}
		};

		update = new UpdateAreaHandlingStrategy(null); // is mocked!
		update.dbUtil = dbutil;
		update.valueConverter = valueConverter;
	}

	@Test
	public void testAddArea() throws InvalidWayException, NoWayValuesException {
		Node startEndNode = NodeCreator.create(2, 2, 2).finish();
		StringBuffer stringBuffer = new StringBuffer();
		Area a = new Area(WayCreator.create(1).node(startEndNode).node(
				NodeCreator.create(3, 3, 3).finish()).node(
				NodeCreator.create(4, 4, 4).finish()).node(startEndNode)
				.finish());
		a.dbMappings.put("testtable", Util.asList(createColumn("v1", "v2"),
				createColumn("v3", "v4")));
		expect(valueConverter.convertValue("v2", a, geomStrategy)).andReturn(
				"v2c");
		expect(valueConverter.convertValue("v4", a, geomStrategy)).andReturn(
				"v4c");
		expectSQLGeneration(stringBuffer);
		replayAll();
		Map<String, StringBuffer> statements = Util.asMap("testtable",
				stringBuffer);
		update.addArea(a, statements, geomStrategy);
		verifyAll();
		assertSQLGenerated(statements);
	}

}
