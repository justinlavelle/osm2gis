package ch.hsr.osminabox.test.schemamapping.xml2ddl;


import static org.junit.Assert.fail;


import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import ch.hsr.osminabox.schemamapping.exceptions.XmlFileInvalidException;
import ch.hsr.osminabox.schemamapping.xml2ddl.XmlValidation;

public class XMLValidationTest {
	
	@Test
	public void missingXMLFileTest(){
		XmlValidation xmlValid = new XmlValidation();
		
		try {
			xmlValid.startValidation("missing.xml");
			fail("Should have raised an IOException");
		} catch (SAXException e) {
			fail("Should not have raised an SAXException");
		} catch (IOException e) {
		} catch (XmlFileInvalidException e) {
			fail("Should not have raised an XmlFileInvalidException");
		}
	}
	
	@Test
	public void invalidXMLTest(){
		XmlValidation xmlValid = new XmlValidation();
		
		try {
			xmlValid.startValidation(new XmlFilesDummy().getInvalidXML());
			fail("Should have raised an XmlFileInvalidException");
		} catch (SAXException e) {
			fail("Should not have raised an SAXException");
		} catch (IOException e) {
			fail("Should not have raised an IOException");
		} catch (XmlFileInvalidException e) {
		}
	}
	

}
