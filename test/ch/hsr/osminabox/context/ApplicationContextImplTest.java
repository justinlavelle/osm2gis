package ch.hsr.osminabox.context;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ApplicationContextImplTest {

	private ApplicationContext context;
	@Before
	public void setUp() throws Exception {
		context = new ApplicationContextImpl("config/osm2gis.properties", new String[]{});
	}

	@Test
	public void testForTheExistanceOfTimeForDailyUpdate() {
		String configParameter = context.getConfigParameter("osm2gis.scheduler.daily.time");
		assertNotNull(configParameter);
		String[] split = configParameter.split(":");
		String hour = split[0];
		String minute = split[1];
		assertInRange(0,23, Integer.valueOf(hour));
		assertInRange(0,59, Integer.valueOf(minute));
	}

	private void assertInRange(int lower, int upper, int value) {
		if(!(value >=lower && value <= upper)){
			throw new Error("'"+value + "' is not within the goven bounds ["+lower+";"+upper+"]");
		}
	}

}
