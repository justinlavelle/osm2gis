package ch.hsr.osminabox.db.boundingbox;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;

/**
 * Implementation for the Boundingbox Strategy
 * 
 * @author jzimmerm
 */
public class BoundingBoxStrategyImpl implements BoundingBoxStrategy {

	private static final Logger logger = Logger
			.getLogger(BoundingBoxStrategyImpl.class);

	private float latMax;
	private float lonMin;

	private float latMin;
	private float lonMax;

	public BoundingBoxStrategyImpl(float latMax, float lonMin, float latMin, float lonMax) {
		this.latMax = latMax;
		this.lonMin = lonMin;
		this.latMin = latMin;
		this.lonMax = lonMax;

		logger.info("Using BoundingBox with latMin:" + latMax + ", lonMin: " + lonMin + ", latMin: " + latMin
				+ ", lonMax:" + lonMax);
	}

	@Override
	public boolean visit(Node node) {
		try{
			if (Float.parseFloat(node.attributes.get(Node.NODE_LATITUDE)) > latMax
					|| Float.parseFloat(node.attributes.get(Node.NODE_LATITUDE)) < latMin) {
				return false;
			}
			if (Float.parseFloat(node.attributes.get(Node.NODE_LONGITUDE)) < lonMin
					|| Float.parseFloat(node.attributes.get(Node.NODE_LONGITUDE)) > lonMax) {
				return false;
			}
			return true;
		}
		catch(Exception e){
			logger.error("Couldn't convert Node lat/lon values into a Float. Node Attributes: " + node.attributes.toString() + ". (" + e.getClass() + ")");
			return false;
		}
	}

	@Override
	public boolean visit(Way way) {
		for (Node node : way.nodes) {
			if (visit(node))
				return true;
		}
		return false;
	}

	@Override
	public boolean visit(Area area) {
		for(Way way : area.ways.keySet()){
			if(visit(way))
				return true;
		}
		return false;
	}

}
