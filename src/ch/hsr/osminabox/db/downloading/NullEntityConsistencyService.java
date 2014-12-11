package ch.hsr.osminabox.db.downloading;

import java.util.LinkedList;
import java.util.List;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;

/**
 * An implementation of EntityConsistencyService, which doesn't do anything.
 * @author tstaehli
 *
 */
public class NullEntityConsistencyService implements EntityConsistencyService {

	@Override
	public void addMissingNodes(List<Node> nodes) {
		// don't care about additions, because we will never download them with this null implementation.
	}

	@Override
	public LinkedList<Node> fetchMissingNodes() {
		return new LinkedList<Node>(); // only return an empty list, which means no Node could be retireved
	}

	@Override
	public Way fetchWayFull(long osmId) {
		return null; //return null, which means way could not have been recieved
	}

}
