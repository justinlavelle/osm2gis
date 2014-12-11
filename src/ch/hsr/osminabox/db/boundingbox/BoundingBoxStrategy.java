package ch.hsr.osminabox.db.boundingbox;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;

/**
 * This Strategy determines if a Node is in a given bounding box
 * @author jzimmerm
 */
public interface BoundingBoxStrategy {
	
	/**
	 * @param node
	 * @return true if the Node is within a given bounding box
	 */
	public boolean visit(Node node);
	
	/**
	 * Node data from temp table must be applied to this way before
	 * 
	 * @param way
	 * @return true if at least one Node of the given Way lies inside the bounding box.
	 */
	public boolean visit(Way way);
	
	/**
	 * Way data from temp table must be applied to this area before.
	 * 
	 * @param area
	 * @return true if at least one Node of any Way in this Area lies inside the bounding box.
	 */
	public boolean visit(Area area);
	
}
