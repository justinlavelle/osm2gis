package ch.hsr.osminabox.db.boundingbox;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;

/**
 * A Null object for when no boundingbox is specified
 * The visit method always returns true
 * @author jzimmerm
 *
 */
public class BoundingBoxStrategyNull implements BoundingBoxStrategy {

	@Override
	public boolean visit(Node node) {
		return true;
	}

	@Override
	public boolean visit(Way way) {
		return true;
	}

	@Override
	public boolean visit(Area area) {
		return true;
	}

}
