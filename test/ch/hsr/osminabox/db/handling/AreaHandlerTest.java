package ch.hsr.osminabox.db.handling;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.AreaComposition;
import ch.hsr.osminabox.db.sql.area.GeomNOuterStrategy;
import ch.hsr.osminabox.db.sql.area.GeomOneOuterStrategy;
import ch.hsr.osminabox.db.util.AreaCompositionDetector;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.test.Util;

public class AreaHandlerTest extends HandlerTestTemplate {

	private AreaHandler handler;
	private AreaCompositionDetector detector;
	private List<Area> areas;
	private Area area1;
	private Area area51;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		detector = createMock(AreaCompositionDetector.class);
		handler = new AreaHandler(context, dbStructure);
		handler.insertStrategy = areaInsertStrategy;
		handler.updateStrategy = areaUpdateStrategy;
		handler.dbUtil = util;
		handler.composition = detector;
		area1 = new Area(1);
		area1.setOsmId(1);
		area51 = new Area(2);
		area51.setOsmId(2);
		areas = Util.asList(area1, area51);
	}

	@Test
	public void testCreateInsertStatements() {
		// because additional objects are involved in the sql query generation,
		// we cannot use the methods defined in HandlerTest --> do it manually.
		expectTablesOfGeomTypeCall(MappingType.MULTIPOLYGON);
		expect(detector.detect(area1)).andReturn(
				AreaComposition.N_INNER_N_OUTER);
		Capture<Map<String, StringBuffer>> statements = new Capture<Map<String, StringBuffer>>();
		areaInsertStrategy.addArea(eq(area1), capture(statements),
				anyObject(GeomNOuterStrategy.class));
		expect(detector.detect(area51)).andReturn(
				AreaComposition.ONE_OUTER_N_INNER);
		Capture<Map<String, StringBuffer>> statements2 = new Capture<Map<String, StringBuffer>>();
		areaInsertStrategy.addArea(eq(area51), capture(statements2),
				anyObject(GeomOneOuterStrategy.class));
		final Capture<HashMap<String, StringBuffer>> statements3 = new Capture<HashMap<String, StringBuffer>>();
		expect(util.addEndTags(capture(statements3))).andAnswer(
				new IAnswer<HashMap<String, StringBuffer>>() {

					@Override
					public HashMap<String, StringBuffer> answer()
							throws Throwable {
						return statements3.getValue();
					}
				});
		replayAll();
		handler.createInsertStatements(areas);
		verifyAll();
		assertSame(statements.getValue(), statements2.getValue());
		assertSame(statements.getValue(), statements3.getValue());
	}

	@Test
	public void testCreateUpdateStatements() {
		// because additional objects are involved in the sql query generation,
		// we cannot use the methods defined in HandlerTest --> do it manually.
		expectTablesOfGeomTypeCall(MappingType.MULTIPOLYGON);
		expect(detector.detect(area1)).andReturn(
				AreaComposition.N_INNER_N_OUTER);
		Capture<Map<String, StringBuffer>> statements = new Capture<Map<String, StringBuffer>>();
		areaUpdateStrategy.addArea(eq(area1), capture(statements),
				anyObject(GeomNOuterStrategy.class));
		expect(detector.detect(area51)).andReturn(
				AreaComposition.ONE_OUTER_N_INNER);
		Capture<Map<String, StringBuffer>> statements2 = new Capture<Map<String, StringBuffer>>();
		areaUpdateStrategy.addArea(eq(area51), capture(statements2),
				anyObject(GeomOneOuterStrategy.class));	
		replayAll();
		handler.createUpdateStatements(areas);
		verifyAll();
		assertSame(statements.getValue(), statements2.getValue());
	}

}
