package ch.hsr.osminabox.downloading;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

public class Downloader {

	private static Logger logger = Logger.getLogger(Downloader.class);
	
	private static int size = 1024;

	private static int nextSecond, byteRead, byteWritten = 0;
	
	public static void download(String src, String dst) throws FileNotFoundException {

		logger.info("Downloading file from URL: " + src);
		logger.info("Destination: " + dst);
		
		byteRead = 0;
		byteWritten = 0;
		
		
		try{
			URL fileURL = new URL(src);
			OutputStream os = new BufferedOutputStream(new FileOutputStream(dst));
			
			URLConnection conn = fileURL.openConnection();
			InputStream is = conn.getInputStream();
			
			byte[] buf = new byte[size];

			nextSecond = new GregorianCalendar().get(Calendar.SECOND);
			
			while((byteRead = is.read(buf)) != -1){
					os.write(buf, 0, byteRead);
					byteWritten += byteRead;
					if(logger.isTraceEnabled()){
						traceDownload();}
			}
			
			is.close();
			os.close();

			logger.info("Download completed!");
		} catch (FileNotFoundException e){
		   throw e;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			byteWritten = 0;
			byteRead = 0;		
		}
		
		
		
	}

	private static void traceDownload() {
		if(new GregorianCalendar().get(Calendar.SECOND) > nextSecond){
			logger.trace("Bytes written: " + byteWritten);
			nextSecond++;
			if(nextSecond > 60){nextSecond = 0;}
		}
	}
	
	
	
}
