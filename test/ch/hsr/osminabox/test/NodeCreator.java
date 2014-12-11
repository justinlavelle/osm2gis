package ch.hsr.osminabox.test;

import java.util.List;
import java.util.Set;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.schemamapping.xml.Column;

public class NodeCreator {

	private Node node;

	private NodeCreator(long osmId) {
		node = new Node();
		node.setOsmId(osmId);
	}

	public static NodeCreator create(long osmId) {
		return new NodeCreator(osmId);
	}
	
	public static NodeCreator create(long osmId, double lat, double lon) {
		NodeCreator nodeCreator = new NodeCreator(osmId);
		nodeCreator.attr(Node.NODE_LATITUDE, "" + lat);
		nodeCreator.attr(Node.NODE_LONGITUDE, "" + lon);
		return nodeCreator;
	}

	public NodeCreator attr(String key, String value) {
		node.attributes.put(key, value);
		return this;
	}

	public Node finish() {
		return node;
	}

	public NodeCreator dbMapping(String id, List<Column> columns) {
		node.dbMappings.put(id, columns);
		return this;
	}

	public NodeCreator tag(String key, Set<String> values) {
		node.tags.put(key, values);
		return this;
	}
}
