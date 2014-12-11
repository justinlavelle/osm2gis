package ch.hsr.osminabox.db.util;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.test.Util;

public class DataInitialisationTest {

	DataInitialisation init;
	@Before
	public void setUp() throws Exception {
		init = new DataInitialisation();
	}

	@Test
	public void testInitiateHashMap() {
		HashMap<String, StringBuffer> res = init.initiateHashMap(Util.asSet("test1", "test2"));
		assertNotSame(res.get("test1"), res.get("test2"));
		assertEquals("", res.get("test1").toString());
		assertEquals("", res.get("test2").toString());
	}

}
