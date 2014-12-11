package ch.hsr.osminabox.db.downloading;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.sql.Constants;

/**
 * An Implementation of the API Service using the Version 0.6 of the OSM API
 * @author rhof
 */
public class APIServiceImpl06 implements APIService {

	private final static Logger logger = Logger.getLogger(APIServiceImpl06.class);
	
	private final static String urlRoot = "http://api.openstreetmap.org/api/0.6/";
	
	protected XMLResponseParser xmlResponseParser;
	
	public APIServiceImpl06() {
		xmlResponseParser = new XMLResponseParser();
	}
	
	@Override
	public List<Node> retrieveNodes(List<Node> nodes){
		String apiCall = urlRoot + "nodes?nodes=" + listToCSVString(nodes);
		byte[] response = doAPICall(apiCall);
		if(response != null)
			return xmlResponseParser.parseNodes(new String(response));
		return null;
	}		
	
	@Override
	public Way retrieveWayFull(long osmId) {
		String apiCall = urlRoot + "way/" + osmId + "/full";
		byte[] response = doAPICall(apiCall);
		if(response != null)
			return xmlResponseParser.parseCompleteWay(new String(response, Charset.forName("UTF-8")));
		
		return null;
	}
	
	protected byte[] doAPICall(String url){

		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);
		
		try {
			int statusCode = client.executeMethod(method);
			
			if(statusCode != HttpStatus.SC_OK){
		        logger.error("HTTP GET Failed: " + url);
		        return null;
			}
			
			return method.getResponseBody();
			
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Converts a List with OSM Ids to a Comma Seperated Value String to be used for fetching multiple OSM entities.
	 * @param entities
	 * @return
	 */
	private String listToCSVString(List<? extends OSMEntity> entities){
		StringBuffer result = new StringBuffer();
		for(OSMEntity entity : entities){
			result.append(entity.getOsmId());
			result.append(Constants.COMMA);
		}
		if(result.length() > 0)
			result.deleteCharAt(result.length() - 1);
		
		return result.toString();
	}
}
