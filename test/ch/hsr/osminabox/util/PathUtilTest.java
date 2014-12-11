package ch.hsr.osminabox.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PathUtilTest {

	@Test
	public void testGetFileName() {
		assertEquals("testfile.test", PathUtil.getFileName("http://www.test.ch/testfolder/testfile.test"));
	}

	@Test
	public void testRemoveExtension() {
		assertEquals("test", PathUtil.removeExtension("test.jpg"));
	}

	@Test
	public void testGetExtension() {
		assertEquals(".jpg", PathUtil.getExtension("test.jpg"));
	}

	@Test
	public void testFileExists() {
		assertTrue(PathUtil.fileExists("."));
		assertFalse(PathUtil.fileExists("nonexistenmt file"));
	}

}
