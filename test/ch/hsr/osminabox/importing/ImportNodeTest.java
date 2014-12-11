package ch.hsr.osminabox.importing;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;

public class ImportNodeTest {
	
	private EntityBuffer<Node> nodeStack;
	private long timestamp = new Date().getTime();
	
	@Before
	public void setUp() {
		nodeStack = new EntityBuffer<Node>();
	}

	@Test
	public void ListenerTest() {
		nodeStack.clear();
		MockEnityBufferListener listener = new MockEnityBufferListener(null, 1);
		nodeStack.addBufferListener(listener);
		
		//Add 25 times to nodeStack
		for(int i=0; i < 25; i++) {
			nodeStack.put(createNode(i, 47.312494f, 8.525556f));
		}
		nodeStack.flush();
		//Validate if both Listener were called
		
		assertEquals(true, listener.isHandleOnWakeUpCalled()); 
		assertEquals(true, listener.isHandleRestCalled());		
	}
	
	private Node createNode(int osmId, float lat, float lon) {
		Node node = new Node();
		node.setOsmId(osmId);
		node.attributes.put(OSMEntity.ATTRIBUTE_TIMESTAMP, String.valueOf(new Date(timestamp)));
		node.attributes.put(Node.NODE_LATITUDE, String.valueOf(lat));
		node.attributes.put(Node.NODE_LONGITUDE, String.valueOf(lon));
		return node;
	}

}
