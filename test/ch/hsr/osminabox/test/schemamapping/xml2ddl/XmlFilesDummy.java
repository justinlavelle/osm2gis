package ch.hsr.osminabox.test.schemamapping.xml2ddl;

import java.io.BufferedReader;
import java.io.FileReader;

public class XmlFilesDummy {
	
	public String loadXMLFile(String filepath)throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();

	}
	
	public String getValidXML(){

			return "./test/xmlfiles/mappingconfig_network.xml";

	}
	
	public String getInvalidXML(){
		return "./test/xmlfiles/mappingconfig_network_invalid.xml";
	}
}
