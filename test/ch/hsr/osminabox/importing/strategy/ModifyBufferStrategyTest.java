package ch.hsr.osminabox.importing.strategy;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

public class ModifyBufferStrategyTest extends BufferStrategyTest{

	@Test
	public void testModifyBufferStrategy() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		testStrategyCreation(ModifyBufferStrategy.class);
	}

}
