package ch.hsr.osminabox.db.initialimport;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.easymock.EasyMockSupport;
import org.easymock.IAnswer;

import ch.hsr.osminabox.db.dbdefinition.Database;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.RelationMember;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.sql.area.GeomStrategy;
import ch.hsr.osminabox.db.sql.area.exceptions.NoWayValuesException;
import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.test.Util;

public class InitialXXXHandlingStrategy extends EasyMockSupport {

	protected Database database;
	protected DBUtil util;
	protected ValueConverter converter;
	protected Map<String, StringBuffer> statements;
	protected List<Column> mappings;

	public void setUp() throws Exception {
		database = createMock(Database.class);
		util = createMock(DBUtil.class);
		converter = createMock(ValueConverter.class);
		mappings = new ArrayList<Column>();
		mappings.add(createColumn("c1Name", "c1Value"));
		mappings.add(createColumn("c2Name", "c2Value"));
		statements = new HashMap<String, StringBuffer>();
		statements.put("testtable", new StringBuffer());
	}

	protected Column createColumn(String name, String value) {
		Column column = new Column();
		column.setName(name);
		column.setValue(value);
		return column;
	}

	protected void expectValueConverting(String value, Node entity,
			String convertedValue) {
		expect(converter.convertValue(value, entity)).andReturn(
				convertedValue);
	}

	protected void expectValueConverting(String value, Area entity, GeomStrategy geom,
			String convertedValue) throws NoWayValuesException {
		expect(converter.convertValue(value, entity, geom)).andReturn(
				convertedValue);
	}
	
	protected void expectValueConverting(String value, Way entity,
			String convertedValue) {
		expect(converter.convertValue(value,entity)).andReturn(
				convertedValue);
	}
	
	protected void expectValueConverting(String value, Relation entity,
			String convertedValue) {
		expect(converter.convertValue(value, entity)).andReturn(convertedValue);
	}
	
	protected void expectValueConverting(String value, RelationMember entity,
			String convertedValue) {
		expect(converter.convertValue(value, entity)).andReturn(convertedValue);
	}

	protected void expectInsertBeginCall() {
		expect(util.createInsertBegin("testtable")).andReturn(
				new StringBuffer("<INSERT START>"));
	}

	protected void assertGeneratedSQL() {
		assertGeneratedSQL("testtable");
	}

	protected void assertGeneratedSQL(String tablename) {
		assertEquals("<INSERT START><INSERT SQL>", statements.get(tablename)
				.toString());
	}

	protected void expectConvertingCalls(Node osmEntity) {
		expectValueConverting("c1Value", osmEntity, "c1ValueConverted");
		expectValueConverting("c2Value", osmEntity, "c2ValueConverted");
	}
	
	protected void expectConvertingCalls(Area osmEntity, GeomStrategy geom) throws NoWayValuesException {
		expectValueConverting("c1Value", osmEntity, geom, "c1ValueConverted");
		expectValueConverting("c2Value", osmEntity, geom, "c2ValueConverted");
	}
	
	protected void expectConvertingCalls(Way osmEntity) {
		expectValueConverting("c1Value", osmEntity, "c1ValueConverted");
		expectValueConverting("c2Value", osmEntity, "c2ValueConverted");
	}
	
	protected void expectConvertingCalls(Relation osmEntity) {
		expectValueConverting("c1Value", osmEntity, "c1ValueConverted");
		expectValueConverting("c2Value", osmEntity, "c2ValueConverted");
	}

	protected void expectInsertValuesCall() {
		expect(
				util.addInsertValues("testtable", Util.asMap("c2Name",
						"c2ValueConverted", "c1Name", "c1ValueConverted")))
				.andReturn(new StringBuffer("<INSERT SQL>"));
	}

	protected void expectInsertBeginCall(String table) {
		expect(util.createInsertBegin(table)).andReturn(
				new StringBuffer("<INSERT START>"));
	}

	protected void expectInsertCall(String tablename,
			final Map<String, String> insertValues) {

		expect(util.addInsertValues(eq(tablename), anyObject(Map.class)))
				.andAnswer(new IAnswer<StringBuffer>() {

					@SuppressWarnings("unchecked")
					@Override
					public StringBuffer answer() throws Throwable {
						Map<String, String> realInsertValues = (Map<String, String>) getCurrentArguments()[1];
						assertEquals(insertValues.size(), realInsertValues
								.size());
						for (Entry<String, String> entry : insertValues
								.entrySet()) {
							assertEquals(entry.getValue(), realInsertValues
									.get(entry.getKey()));
						}
						return new StringBuffer("<INSERT SQL>");
					}
				});
	}

}
