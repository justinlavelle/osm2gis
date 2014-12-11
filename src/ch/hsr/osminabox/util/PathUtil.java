package ch.hsr.osminabox.util;

import java.io.File;

public class PathUtil {

	public static final String urlSeparator = "/";
	
	/**
	 * @param url
	 * @return	extracts the file name from the url
	 */
	public static String getFileName(String url) {
		return url.substring(url.lastIndexOf(urlSeparator) + 1);
	}
	
	/**
	 * 
	 * @param file
	 * @return The filename without its extensions.
	 */
	public static String removeExtension(String file){
		String fileName;
		try{
			fileName = file.substring(0, file.indexOf('.'));
		} catch( IndexOutOfBoundsException e){
			return file;
		}
		return fileName;
	}
	
	public static String getExtension(String fileName){
		try{
			int dotIndex = fileName.indexOf('.');
			return fileName.substring(dotIndex);
		} catch (Exception e){
			return "";
		}
	}

	/**
	 * @param file
	 * @return true if the file exists
	 */
	public static boolean fileExists(String file) {
		File downloadedFile = new File(file);
		if(!downloadedFile.exists()){
			return false;
		}
		return true;
	}
	
}
