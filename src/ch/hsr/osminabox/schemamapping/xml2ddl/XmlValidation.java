package ch.hsr.osminabox.schemamapping.xml2ddl;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import ch.hsr.osminabox.schemamapping.exceptions.XmlFileInvalidException;


/**
 * Implementation of XmlValidation
 * 
 * @author ameier
 * 
 */
public class XmlValidation {

	private static int errorCount = 0;
	
	private static final String schemapath = "config/mappingconfig.xsd";
	
	
	/**
	 * Start validation.
	 * 
	 * @param xmlName the xml name
	 * 
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws XmlFileInvalidException the xml file invalid exception
	 */
	public void startValidation(String xmlName) throws SAXException, IOException, XmlFileInvalidException{
		errorCount = 0;
		Schema schema = loadSchema(schemapath);
		validateXml(schema, xmlName);
	}
	
	/**
	 * Validate xml.
	 * 
	 * @param schema the schema
	 * @param xmlName the xml name
	 * 
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws XmlFileInvalidException the xml file invalid exception
	 */
	private void validateXml(Schema schema, String xmlName) throws SAXException, IOException, XmlFileInvalidException {

			// creating a Validator instance
			Validator validator = schema.newValidator();

			// setting my own error handler
			validator.setErrorHandler(new MyErrorHandler());

			// preparing the XML file as a SAX source
			SAXSource source = new SAXSource(new InputSource(
					new java.io.FileInputStream(xmlName)));

			// validating the SAX source against the schema
			validator.validate(source);
			
			if (errorCount > 0) {
				throw new XmlFileInvalidException("Failed with errors:"+errorCount);
			}

	}

	/**
	 * Load schema.
	 * 
	 * @param name the name
	 * 
	 * @return the schema
	 */
	private Schema loadSchema(String name) {
		Schema schema = null;
		try {
			String language = XMLConstants.W3C_XML_SCHEMA_NS_URI;
			SchemaFactory factory = SchemaFactory.newInstance(language);
			schema = factory.newSchema(new File(name));
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return schema;
	}

	/**
	 * The Class MyErrorHandler.
	 */
	private class MyErrorHandler implements ErrorHandler {
		
		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
		 */
		public void warning(SAXParseException e) throws SAXException {
			System.out.println("XML Warning: ");
			printException(e);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
		 */
		public void error(SAXParseException e) throws SAXException {
			System.out.println("XML Error: ");
			printException(e);
		}

		/* (non-Javadoc)
		 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
		 */
		public void fatalError(SAXParseException e) throws SAXException {
			System.out.println("XML Fattal error: ");
			printException(e);
		}

		/**
		 * Prints the exception.
		 * 
		 * @param e the e
		 */
		private void printException(SAXParseException e) {
			errorCount++;
			System.out.println("   Line number: " + e.getLineNumber());
			System.out.println("   Column number: " + e.getColumnNumber());
			System.out.println("   Message: " + e.getMessage());
		}
	}
}
