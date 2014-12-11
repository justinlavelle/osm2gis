package ch.hsr.osminabox;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class HelpHandlerTest {

	@Test
	public void testHandleHelpCall() {
		String userDir = System.getProperty("user.dir");
		System.setProperty("user.dir", ".");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		HelpHandler.out = ps;

		HelpHandler.handleHelpCall();
		String help = baos.toString();
		assertNotNull(help);
		System.setProperty("user.dir", userDir);
	}

}
