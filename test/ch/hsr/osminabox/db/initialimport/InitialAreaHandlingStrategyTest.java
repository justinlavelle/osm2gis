package ch.hsr.osminabox.db.initialimport;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.sql.area.GeomStrategy;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;

public class InitialAreaHandlingStrategyTest extends InitialXXXHandlingStrategy {

	private InitialAreaHandlingStrategy strategy;
	private Area area;
	private GeomStrategy geom;
	@Before
	public void setUp() throws Exception {
		super.setUp();
		geom = createMock(GeomStrategy.class);
		strategy = new InitialAreaHandlingStrategy(database);
		strategy.dbUtil = util;
		strategy.valueConverter = converter;
		area = new Area(1);
		area.dbMappings.put("testtable", mappings);
	}

	@Test
	public void testAddArea() throws NoWayValuesException {
		expectConvertingCalls(area, geom);
		expectInsertBeginCall();
		expectInsertValuesCall();
		replayAll();
		strategy.addArea(area, statements, geom);
		verifyAll();
		assertGeneratedSQL();
		assertEquals(1, statements.size());
	}

}
