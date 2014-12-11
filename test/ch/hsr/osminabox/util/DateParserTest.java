package ch.hsr.osminabox.util;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

import ch.hsr.osminabox.util.DateParser;

public class DateParserTest {

	@Test
	public void dateConversionTest(){
		
		DateParser parser = new DateParser();
		
		String date_1 = "2008-11-01T15:10:04+00:00";
		String date_2 = "2008-12-10T13:42:47+00:00";
		String date_3 = "2008-12-09T13:08:02+00:00";
		String date_4 = "2008dsaf-12-09T13:08:02000000";
		
		Date d1 = parser.parse(date_1);
		assertNotNull(d1);
		assertEquals(1225552204000l,  d1.getTime());
		
		Date d2 = parser.parse(date_2);
		assertNotNull(d2);
		assertEquals(1228916567000l, d2.getTime());
		
		Date d3 = parser.parse(date_3);
		assertNotNull(d3);
		assertEquals(1228828082000l, d3.getTime());
		
		Date d4 = parser.parse(date_4);
		assertNull(d4);

	}
	
	
}
