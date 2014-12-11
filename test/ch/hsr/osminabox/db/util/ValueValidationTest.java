package ch.hsr.osminabox.db.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ValueValidationTest {

	private ValueValidation validation;
	@Before
	public void setUp() throws Exception {
		validation = new ValueValidation();
	}

	@Test
	public void testAddEscape() {
		assertNull(validation.addEscape(null));
		assertEquals("", validation.addEscape(""));
		assertEquals("test", validation.addEscape("test"));
		assertEquals("test''", validation.addEscape("test'"));
		assertEquals("test''", validation.addEscape("test\\'"));
		assertEquals("test", validation.addEscape("test\""));
		assertEquals("test''", validation.addEscape("test\"'"));
	}

}
