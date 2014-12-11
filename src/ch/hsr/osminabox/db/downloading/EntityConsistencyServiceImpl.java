package ch.hsr.osminabox.db.downloading;

import java.util.LinkedList;
import java.util.List;
import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;


public class EntityConsistencyServiceImpl implements EntityConsistencyService {

	private static Logger logger = Logger.getLogger(EntityConsistencyServiceImpl.class);
	
	protected APIService apiservice;
	
	private List<Node> missingNodes;
	
	public EntityConsistencyServiceImpl() {
		apiservice = new APIServiceImpl06();
		missingNodes = new LinkedList<Node>();
	}

	@Override
	public void addMissingNodes(List<Node> nodes) {
		missingNodes.addAll(nodes);
	}

	@Override
	public LinkedList<Node> fetchMissingNodes() {
		LinkedList<Node> result = new LinkedList<Node>();
		
		try{
			result.addAll(apiservice.retrieveNodes(missingNodes));
		} catch(Exception e){
			logger.error("Could not retrieve " + missingNodes + " Nodes.");
		}
		
		logger.debug("Fetched " + result.size() + " out of " + missingNodes.size() + " missing Nodes.");
		missingNodes.clear();
		
		return result;
		
	}
	
	@Override
	public Way fetchWayFull(long osmId){
		return apiservice.retrieveWayFull(osmId);
	}
}
