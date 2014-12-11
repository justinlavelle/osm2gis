package ch.hsr.osminabox.db.differentialupdate;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.easymock.EasyMockSupport;

import ch.hsr.osminabox.db.sql.util.DBUtil;
import ch.hsr.osminabox.db.util.ValueConverter;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.test.Util;

public class UpdateXXXHandlingStrategyTestTemplate extends EasyMockSupport {

	protected DBUtil dbutil;
	protected ValueConverter valueConverter;

	protected Column createColumn(String name, String value) {
		Column c = new Column();
		c.setName(name);
		c.setValue(value);
		return c;
	}

	protected void createMocks() {
		dbutil = createMock(DBUtil.class);
		valueConverter = createMock(ValueConverter.class);
	}

	protected void expectSQLGeneration(StringBuffer stringBuffer) {
		expect(dbutil.createUpdateBegin("testtable")).andReturn(new StringBuffer("INSERT INTO testtable VALUES "));
		expect(dbutil.addUpdateValues("testtable", Util.asMap("v1", "v2c", "v3", "v4c"))).andReturn(new StringBuffer("(v1 = v2c, v3 = v4c)"));
		expect(dbutil.addWhere(1)).andReturn(" WHERE osm_id = 1 ");
		dbutil.checkAndAppendSpacer(stringBuffer, ";");
	}

	protected void assertSQLGenerated(Map<String, StringBuffer> statements) {
		assertEquals(
				"INSERT INTO testtable VALUES (v1 = v2c, v3 = v4c) WHERE osm_id = 1 ",
				statements.get("testtable").toString());
	}
	
}
