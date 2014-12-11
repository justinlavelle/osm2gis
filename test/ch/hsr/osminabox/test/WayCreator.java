package ch.hsr.osminabox.test;

import java.util.List;
import java.util.Set;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.schemamapping.xml.Column;

public class WayCreator {

	private Way way;

	private WayCreator(long osmId) {
		way = new Way();
		way.setOsmId(osmId);
	}

	public static WayCreator create(long osmId) {
		return new WayCreator(osmId);
	}

	public WayCreator attr(String key, String value) {
		way.attributes.put(key, value);
		return this;
	}

	public Way finish() {
		return way;
	}

	public WayCreator dbMapping(String id, List<Column> columns) {
		way.dbMappings.put(id, columns);
		return this;
	}

	public WayCreator node(Node n) {
		way.nodes.add(n);
		return this;
	}

	public WayCreator tag(String key, Set<String> values) {
		way.tags.put(key, values);
		return this;
	}
}
