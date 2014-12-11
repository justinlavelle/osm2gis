package ch.hsr.osminabox.schemamapping;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.schemamapping.xml2ddl.Xml2ddl;

public class ConsistencyServiceTest extends EasyMockSupport{

	private ConsistencyService service;
	private Xml2ddl xml2ddl;
	private ApplicationContext context;
	@Before
	public void setUp() throws Exception {
		context = createNiceMock(ApplicationContext.class);
		xml2ddl = createMock(Xml2ddl.class);
		replay(context);
		service = new ConsistencyService(context){
			protected String getXMLConfigFilePath() {
				return "xmlFilePath";
			}
		};
		resetToDefault(context);
		service.context = context;
		service.xml2ddl = xml2ddl;
	}

	@Test
	public void testCheckConsistencyForInitialImportAndUpdateFailing() {
		expect(xml2ddl.startGeneration("xmlFilePath", false, true)).andReturn(false);
		replayAll();
		assertFalse(service.checkConsistencyForInitialImportAndUpdate(true));
		verifyAll();
	}
	
	@Test
	public void testCheckConsistencyForInitialImportAndUpdateSuccess() {
		expect(xml2ddl.startGeneration("xmlFilePath", false, true)).andReturn(true);
		expect(context.setConfigParameter("conf.mapping.file", "xmlFilePath")).andReturn(null);
		context.save();
		replayAll();
		assertTrue(service.checkConsistencyForInitialImportAndUpdate(true));
		verifyAll();
	}

}
