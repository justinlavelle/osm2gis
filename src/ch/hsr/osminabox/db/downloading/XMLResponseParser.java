package ch.hsr.osminabox.db.downloading;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;

/**
 * This class parses an xml response from the OMS Api call and returns the OSMEntitys
 * @author rhof, jzimmerm
 *
 */
public class XMLResponseParser {

	private DOMParser parser = new DOMParser();
	
	private static String OSM_NODE = "node";
	private static String OSM_WAY = "way";
	private static int FIRST_ITEM_INDEX = 0;
	
	/**
	 * Parses the response and adds all Nodes in the correct order to the Way.
	 * 
	 * @param string
	 * @return Way The complete way
	 */
	public Way parseCompleteWay(String response) {
		try{
			Document result = prepareResponse(response);
			
			// Create the Nodes from the response in a Map<OsmId, Node>.
			NodeList xmlNodesNode = result.getElementsByTagName(OSM_NODE);
			Map<Long, Node> resultNodes = new HashMap<Long, Node>();
			
			for(int i=0; i<xmlNodesNode.getLength(); i++){		
				Node node = createNode(xmlNodesNode.item(i));
				resultNodes.put(node.getOsmId(), node);
			}
			
			// Create the Way and take the referenced Nodes from the Map so the Node order is guaranteed.
			NodeList xmlNodesWay = result.getElementsByTagName(OSM_WAY);
			org.w3c.dom.Node xmlWay = xmlNodesWay.item(FIRST_ITEM_INDEX);
			
			NamedNodeMap attributes = xmlWay.getAttributes();
			
			Way resultWay = new Way();
			resultWay.setOsmId(Long.parseLong(attributes.getNamedItem("id").getNodeValue()));
			
			NodeList xmlChildNodes = xmlWay.getChildNodes();
			
			for(int i=0; i<xmlChildNodes.getLength(); i++){
				org.w3c.dom.Node xmlChildNode = xmlChildNodes.item(i);
				if(xmlChildNode.getNodeName().equals("nd")){
					
					NamedNodeMap memberAttribs = xmlChildNode.getAttributes();
					resultWay.nodes.add(resultNodes.get(Long.parseLong(memberAttribs.getNamedItem("ref").getNodeValue())));
				}
				else if(xmlChildNode.getNodeName().equals("tag")){
					NamedNodeMap tag = xmlChildNode.getAttributes();
					resultWay.putTag(tag.getNamedItem("k").getNodeValue(), tag.getNamedItem("v").getNodeValue());
				}
			}
			
			return resultWay;
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Parses every Node from the response String.
	 * 
	 * @param response
	 * @return
	 */
	public List<Node> parseNodes(String response) {
		try {
			Document result = prepareResponse(response);
			
			NodeList xmlNodes = result.getElementsByTagName(OSM_NODE);
			List<Node> resultNodes = new LinkedList<Node>();
			
			for(int i=0; i<xmlNodes.getLength(); i++){				
				resultNodes.add(createNode(xmlNodes.item(i)));
			}
						 
			return resultNodes;		
		
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates and returns an OSM Node from the xmlNode
	 * 
	 * @param xmlNode
	 * @return
	 */
	private Node createNode(org.w3c.dom.Node xmlNode) {		
		NamedNodeMap attributes = xmlNode.getAttributes();

		Node resultNode = new Node();
		resultNode.setOsmId(Integer.parseInt(attributes.getNamedItem("id").getNodeValue()));	 
		resultNode.attributes.put(Node.NODE_LATITUDE, attributes.getNamedItem("lat").getNodeValue());
		resultNode.attributes.put(Node.NODE_LONGITUDE, attributes.getNamedItem("lon").getNodeValue());
		
		NodeList xmlChildNodes = xmlNode.getChildNodes();
		for(int i = 0; i<xmlChildNodes.getLength() -1; i++){
			org.w3c.dom.Node xmlChildNode = xmlChildNodes.item(i);
			if(xmlChildNode.getNodeName().equals("tag")){
				NamedNodeMap tag = xmlChildNode.getAttributes();
				resultNode.putTag(tag.getNamedItem("k").getNodeValue(), tag.getNamedItem("v").getNodeValue());
			}
		}
		
		return resultNode;
	}
	
	private Document prepareResponse(String response) throws SAXException, IOException {
		InputSource source = new InputSource(new StringReader(response));
		parser.parse(source);
		Document result = parser.getDocument();
		return result;
	}
}
