package ch.hsr.osminabox.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;
import org.apache.tools.bzip2.CBZip2InputStream;

/**
 * This Factory creates an inputstream depending on the Compression method
 * @author rhof
 *
 */
public class StreamFactory {

	private static Logger logger = Logger.getLogger(StreamFactory.class);
	
	public static InputStream createInputStream(String file){
		
		try{
			
			InputStream stream = new FileInputStream(file);	
			CompressionMethod compressionMethod = getCompressionMethod(file);
			if(logger.isDebugEnabled()){logger.debug("Set Compression Method: " + compressionMethod.toString());}
			
			if(compressionMethod == CompressionMethod.NONE){
				return new FileInputStream(file);
			}
			
			if(compressionMethod == CompressionMethod.GZIP){
				return new GZIPInputStream(new FileInputStream(file));
			}
			
			if(compressionMethod == CompressionMethod.BZIP2){
			    if (stream.read() != 'B' || stream.read() != 'Z') {
			    	logger.error("The source stream must start with the characters BZ if it is to be read as a BZip2 stream.");
			    	return null;
			    }
			    return new CBZip2InputStream(stream);
			}
			
		} catch (FileNotFoundException e){
			logger.error("File not found: " + file);
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			logger.error("IOException while trying to create InputStream for file: " + file);
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	private static CompressionMethod getCompressionMethod(String file){
		if(file.endsWith("gz")){return CompressionMethod.GZIP;}
		if(file.endsWith("bz2")){return CompressionMethod.BZIP2;}
		return CompressionMethod.NONE;
	}
	
}
