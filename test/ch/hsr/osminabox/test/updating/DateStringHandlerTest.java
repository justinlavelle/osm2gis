package ch.hsr.osminabox.test.updating;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.updating.DateStringFields;
import ch.hsr.osminabox.updating.DateStringHandler;

public class DateStringHandlerTest {

	private DateStringHandler parser;

	@Before
	public void setUp() {
		parser = new DateStringHandler();
	}

	@Test
	public void testConstructor() throws Exception {
		String minutes = "200905202222";
		assertEquals(minutes, new DateStringHandler(minutes)
				.getDateString(DateStringFields.MINUTE));
	}

	@Test
	public void testMinuteString() {
		String minuteString = "200905212057";
		parser.setDateString(minuteString);
		assertDateStrings(parser, "2009", "05", "21", "20", "57");
		assertDateIntegers(parser, 2009, 5, 21, 20, 57);
		assertEquals(minuteString, parser
				.getDateString(DateStringFields.MINUTE));

	}

	@Test
	public void testAddMinute() {
		testMinuteAddition("200905312258", "200905312259");
		testMinuteAddition("200905312359", "200906010000");
		testMinuteAddition("200912312359", "201001010000");
	}

	@Test
	public void testAddHour() {
		testHourAddition("2009053122", "2009053123");
		testHourAddition("2009053123", "2009060100");
		testHourAddition("2009123123", "2010010100");
	}

	@Test
	public void testAddDay() throws Exception {
		testDayAddition("20090515", "20090516");
		testDayAddition("20090531", "20090601");
		testDayAddition("20091231", "20100101");
	}

	@Test
	public void testHourString() {
		String minuteString = "2009052120";
		parser.setDateString(minuteString);
		assertYearMonthDayHourStrings(parser, "2009", "05", "21", "20");
		assertYearMonthDayHourIntegers(parser, 2009, 5, 21, 20);
		assertEquals("2009052120", parser.getDateString(DateStringFields.HOUR));
	}

	@Test
	public void testDayString() {
		String dayString = "20090521";
		parser.setDateString(dayString);
		assertYearMonthDayStrings(parser, "2009", "05", "21");
		assertYearMonthDayIntegers(parser, 2009, 5, 21);
		assertEquals("20090521", parser.getDateString(DateStringFields.DAY));
	}

	private void assertDateStrings(DateStringHandler parser, String yearString,
			String monthString, String dayString, String hourString,
			String minuteString) {
		assertYearMonthDayHourStrings(parser, yearString, monthString,
				dayString, hourString);
		assertEquals(minuteString, parser
				.getStringValue(DateStringFields.MINUTE));
	}

	private void assertYearMonthDayHourStrings(DateStringHandler parser,
			String yearString, String monthString, String dayString,
			String hourString) {
		assertYearMonthDayStrings(parser, yearString, monthString, dayString);
		assertEquals(hourString, parser.getStringValue(DateStringFields.HOUR));
	}

	private void assertYearMonthDayStrings(DateStringHandler parser,
			String yearString, String monthString, String dayString) {
		assertEquals(yearString, parser.getStringValue(DateStringFields.YEAR));
		assertEquals(monthString, parser.getStringValue(DateStringFields.MONTH));
		assertEquals(dayString, parser.getStringValue(DateStringFields.DAY));
	}

	private void assertDateIntegers(DateStringHandler parser, int year,
			int month, int day, int hour, int minute) {
		assertYearMonthDayHourIntegers(parser, year, month, day, hour);
		assertEquals(minute, parser.getIntegerValue(DateStringFields.MINUTE));
	}

	private void assertYearMonthDayHourIntegers(DateStringHandler parser,
			int year, int month, int day, int hour) {
		assertYearMonthDayIntegers(parser, year, month, day);
		assertEquals(hour, parser.getIntegerValue(DateStringFields.HOUR));
	}

	private void assertYearMonthDayIntegers(DateStringHandler parser, int year,
			int month, int day) {
		assertEquals(year, parser.getIntegerValue(DateStringFields.YEAR));
		assertEquals(month, parser.getIntegerValue(DateStringFields.MONTH));
		assertEquals(day, parser.getIntegerValue(DateStringFields.DAY));
	}

	private void testMinuteAddition(String minuteString, String oneMinuteLater) {
		testAddition(minuteString, oneMinuteLater, new Runnable() {

			@Override
			public void run() {
				parser.addMinute();
			}
		}, DateStringFields.MINUTE);
	}

	private void testHourAddition(String hourString, String oneHourLater) {
		testAddition(hourString, oneHourLater, new Runnable() {

			@Override
			public void run() {
				parser.addHour();
			}
		}, DateStringFields.HOUR);
	}

	private void testDayAddition(String dayString, String oneDayLater) {
		testAddition(dayString, oneDayLater, new Runnable() {

			@Override
			public void run() {
				parser.addDay();
			}
		}, DateStringFields.DAY);
	}

	private void testAddition(String stringBeforeAddition,
			String stringAfterAddition, Runnable incrementation,
			DateStringFields lastField) {
		parser.setDateString(stringBeforeAddition);
		incrementation.run();
		assertEquals(stringAfterAddition, parser.getDateString(lastField));
	}
}
