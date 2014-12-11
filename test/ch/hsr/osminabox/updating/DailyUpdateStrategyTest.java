package ch.hsr.osminabox.updating;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.quartz.Trigger;

public class DailyUpdateStrategyTest {

	private static final int FIRE_MINUTE = 47;
	private static final int FIRE_HOUR = 9;
	private DailyUpdateStrategy strategy;

	@Before
	public void setUp() throws Exception {
		strategy = new DailyUpdateStrategy(FIRE_HOUR, FIRE_MINUTE);
	}

	@Test
	public void testGetTrigger() {
		Trigger t = strategy.getTrigger();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(t.computeFirstFireTime(null));
		assertEquals(FIRE_HOUR, calendar.get(Calendar.HOUR_OF_DAY));
		assertEquals(FIRE_MINUTE, calendar.get(Calendar.MINUTE));
	}

}
