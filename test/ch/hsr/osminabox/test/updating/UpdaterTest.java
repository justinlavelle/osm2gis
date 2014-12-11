package ch.hsr.osminabox.test.updating;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sun.org.apache.xerces.internal.impl.dv.xs.DayDV;

import ch.hsr.osminabox.updating.DailyUpdateStrategy;
import ch.hsr.osminabox.updating.HourlyUpdateStrategy;
import ch.hsr.osminabox.updating.MinutelyUpdateStrategy;
import ch.hsr.osminabox.updating.UpdateStrategy;

public class UpdaterTest {

	private UpdateStrategy updateStrategy;

	@Test
	public void updateStrategyMinutelyTestNormal() {
		updateStrategy = new MinutelyUpdateStrategy();
		testUpdateStrategy("000000000.osc.gz", "000000001.osc.gz");
		testUpdateStrategy("000034999.osc.gz", "000035000.osc.gz");
		testUpdateStrategy("023999999.osc.gz", "024000000.osc.gz");
	}

	@Test
	public void testUpdateStrategyDaily() {
		updateStrategy = new DailyUpdateStrategy(13, 15);
		testUpdateStrategy("20090522-20090523.osc.gz",
				"20090523-20090524.osc.gz");
		testUpdateStrategy("20091230-20091231.osc.gz",
				"20091231-20100101.osc.gz");
	}

	@Test
	public void minutelyFileUrlTest() {
		updateStrategy = new MinutelyUpdateStrategy();
		testFileUrl("000034999.osc.gz", "000/034/999.osc.gz");
	}

	@Test
	public void dailyFileUrlTest() {
		updateStrategy = new DailyUpdateStrategy(13, 15);
		testFileUrl("20091230-20091231.osc.gz", "20091230-20091231.osc.gz");
	}

	private void testFileUrl(String currentFileUrl, String expectedNextFileUrl) {
		assertEquals(expectedNextFileUrl, updateStrategy
				.getUpdateFileAsUrl(currentFileUrl));
	}
	
	private void testUpdateStrategy(String currentFileUrl,
			String expectedNextFileUrl) {
		String actualNextFileUrl = updateStrategy
				.getNextUpdateFile(currentFileUrl);
		assertEquals(expectedNextFileUrl, actualNextFileUrl);
	}

}
