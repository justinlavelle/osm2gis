package ch.hsr.osminabox.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.BasicConfigurator;
import org.apache.tools.bzip2.CBZip2InputStream;
import org.junit.BeforeClass;
import org.junit.Test;

public class StreamFactoryTest {

	@BeforeClass
	public static void initialize() {
		BasicConfigurator.configure();
	}

	@Test
	public void testCreateInputStreamForPlainFile() {
		assertEquals(FileInputStream.class, StreamFactory
				.createInputStream("test/ch/hsr/osminabox/util/test.osm").getClass());
	}
	
	@Test
	public void testCreateInputStreamForGZipFile() {
		assertEquals(GZIPInputStream.class, StreamFactory
				.createInputStream("test/ch/hsr/osminabox/util/test.txt.gz").getClass());
	}
	
	@Test
	public void testCreateInputStreamForBZ2File() {
		assertEquals(CBZip2InputStream.class, StreamFactory
				.createInputStream("test/ch/hsr/osminabox/util/test.txt.bz2").getClass());
	}
	
	@Test
	public void testCreateInputStreamInvalidBZ2File() {
		assertNull( StreamFactory
				.createInputStream("test/ch/hsr/osminabox/util/invalid.bz2"));
	}
	@Test
	public void testCreateInputStreamForNonexistentFile() {
		assertNull( StreamFactory
				.createInputStream("test/ch/hsr/osminabox/util/nonexistent.zip"));
	}
}
