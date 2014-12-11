package ch.hsr.osminabox.importing.strategy;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class CreateBufferStrategyTest extends BufferStrategyTest {

	@Test
	public void testCreateBufferStrategy() throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		testStrategyCreation(CreateBufferStrategy.class);
	}

}
