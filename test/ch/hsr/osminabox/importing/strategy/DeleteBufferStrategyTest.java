package ch.hsr.osminabox.importing.strategy;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class DeleteBufferStrategyTest extends BufferStrategyTest{

	@Test
	public void testDeleteBufferStrategy() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		testStrategyCreation(DeleteBufferStrategy.class);
	}

}
