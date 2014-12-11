package ch.hsr.osminabox.db.boundingbox;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.db.entities.Area.WayRole;
import ch.hsr.osminabox.db.entities.exceptions.InvalidWayException;

public class BoundingBoxStrategyImplTest {

	private static final Way EMPTY_WAY = createWay(new Node[] {});
	private static final Node HALF_OUT_LOWER = createNode(-1, 5);
	private static final Node HALF_OUT_UPPER = createNode(5, 11);
	private static final Node IN_BORDERS = createNode(5, 5);
	private static final Node OVER_UPPER_BORDER = createNode(11, 11);
	private static final Node OVER_LOWER_BORDER = createNode(-1, -1);
	private static final Way TWO_NODES_OUT = createWay(new Node[] {
			OVER_LOWER_BORDER, OVER_UPPER_BORDER });
	private static final Way ONE_OUT_AND_ONE_IN = createWay(new Node[] {
			OVER_LOWER_BORDER, IN_BORDERS });
	private static final Node NEAR_UPPER_BORDER = createNode(10, 10);
	private static final Node NEAR_LOWER_BORDER = createNode(0, 0);
	private static final Way NODES_NEAR_BORDER = createWay(new Node[] {
			NEAR_LOWER_BORDER, NEAR_UPPER_BORDER });
	private static final Way ONE_NODE_IN = createWay(new Node[] { NEAR_LOWER_BORDER });
	private BoundingBoxStrategy strategy;

	@Before
	public void setUp() throws Exception {
		int latMax = 10;
		int lonMin = 0;
		int latMin = 0;
		int lonMax = 10;
		strategy = new BoundingBoxStrategyImpl(latMax, lonMin, latMin, lonMax);
	}

	@Test
	public void testVisitNode() throws Exception {
		assertTrue(strategy.visit(NEAR_LOWER_BORDER));
		assertTrue(strategy.visit(NEAR_UPPER_BORDER));
		assertFalse(strategy.visit(OVER_LOWER_BORDER));
		assertFalse(strategy.visit(OVER_UPPER_BORDER));
		assertFalse(strategy.visit(HALF_OUT_UPPER));
		assertFalse(strategy.visit(HALF_OUT_LOWER));
	}

	@Test
	public void testVisitWay() throws Exception {
		assertFalse(strategy.visit(EMPTY_WAY));
		assertTrue(strategy.visit(ONE_NODE_IN));
		assertTrue(strategy.visit(NODES_NEAR_BORDER));
		assertTrue(strategy.visit(ONE_OUT_AND_ONE_IN));
		assertFalse(strategy.visit(TWO_NODES_OUT));
	}

	@Test
	public void testVisitArea() throws Exception {
		assertTrue(strategy.visit(createArea(createWay(new Node[] {
				NEAR_LOWER_BORDER, NEAR_UPPER_BORDER, IN_BORDERS,
				NEAR_LOWER_BORDER }))));
		assertFalse(strategy.visit(createArea(createWay(new Node[] {
				OVER_LOWER_BORDER, OVER_UPPER_BORDER, createNode(30, 30),
				OVER_LOWER_BORDER }))));
	}

	private static Area createArea(Way way) throws InvalidWayException {
		Area a = new Area(way);
		a.ways.clear();
		a.ways.put(way, WayRole.inner);
		return a;
	}

	private static Way createWay(Node[] nodes) {
		Way w = new Way();
		for (Node n : nodes) {
			w.nodes.add(n);
		}
		return w;
	}

	private static Node createNode(float lat, float lon) {
		Node n = new Node();
		n.attributes.put(Node.NODE_LATITUDE, "" + lat);
		n.attributes.put(Node.NODE_LONGITUDE, "" + lon);
		return n;
	}

}
