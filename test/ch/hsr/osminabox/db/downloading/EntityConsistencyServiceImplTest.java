package ch.hsr.osminabox.db.downloading;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.test.NodeCreator;
import ch.hsr.osminabox.test.Util;
import ch.hsr.osminabox.test.WayCreator;

public class EntityConsistencyServiceImplTest extends EasyMockSupport {

	public EntityConsistencyServiceImpl service;
	public APIService apiService;

	@Before
	public void setUp() throws Exception {
		service = new EntityConsistencyServiceImpl();
		apiService = createMock(APIService.class);
		service.apiservice = apiService;
	}

	@Test
	public void testFetchMissingNodes() {
		BasicConfigurator.configure();
		List<Node> nodelist = Util.asList(NodeCreator.create(1)
				.finish(), NodeCreator.create(2).finish());
		service.addMissingNodes(nodelist);
		List<Node> downloadedNodes = Util.asList(NodeCreator.create(1, 2, 3)
				.finish(), NodeCreator.create(2, 3, 4).finish());
		expect(apiService.retrieveNodes(nodelist)).andReturn(downloadedNodes);
		expect(apiService.retrieveNodes(new ArrayList<Node>())).andReturn(new ArrayList<Node>());
		replayAll();
		List<Node> node = service.fetchMissingNodes();
		assertEquals(downloadedNodes, node);
		service.fetchMissingNodes(); // assert that webservice is only contacted once
		verifyAll();
	}

	@Test
	public void testFetchWayFull() {
		Way downloadedWay = WayCreator.create(1).finish();
		expect(apiService.retrieveWayFull(1)).andReturn(downloadedWay);
		replayAll();
		Way w = service.fetchWayFull(1);
		verifyAll();
		assertEquals(downloadedWay, w);
	}

}
