package ch.hsr.osminabox.importing.strategy;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.importing.EntityBuffer;
import ch.hsr.osminabox.importing.listener.EntityBufferListener;

public class BufferStrategyTest extends EasyMockSupport {

	protected ApplicationContext context;
	protected BufferStrategy strategy;

	@Before
	public void setUp() {
		context = createMock(ApplicationContext.class);
	}

	@Test
	public void testGetConfigParameter() throws Exception {
		strategy = new BufferStrategy(context) {

			@Override
			protected void initBuffers() {

			}

			@Override
			protected EntityBufferListener<Way> createWayListener(
					DBService dbService, int biffersize) {
				return null;
			}

			@Override
			protected EntityBufferListener<Relation> createRelationListener(
					DBService dbService, int biffersize) {
				return null;
			}

			@Override
			protected EntityBufferListener<Node> createNodeListener(
					DBService dbService, int biffersize) {
				return null;
			}

			@Override
			protected EntityBufferListener<Area> createAreaListener(
					DBService dbService, int biffersize) {
				return null;
			}
		};
		expect(context.getConfigParameter("test")).andReturn("10");
		expect(context.getConfigParameter("test2")).andReturn(null);
		replayAll();
		assertEquals(10, strategy.loadConfigParameter("test", 5));
		assertEquals(5, strategy.loadConfigParameter("test2", 5));
		verifyAll();
	}

	@Test
	public void testInitBuffers() throws Exception {
		setExpectations();
		final List<Integer> buffersizeCalls = new ArrayList<Integer>();
		replayAll();
		strategy = new BufferStrategy(context) {

			@Override
			protected EntityBufferListener<Way> createWayListener(
					DBService dbService, int biffersize) {
				buffersizeCalls.add(biffersize);
				return null;
			}

			@Override
			protected EntityBufferListener<Relation> createRelationListener(
					DBService dbService, int biffersize) {
				buffersizeCalls.add(biffersize);
				return null;
			}

			@Override
			protected EntityBufferListener<Node> createNodeListener(
					DBService dbService, int biffersize) {
				buffersizeCalls.add(biffersize);
				return null;
			}

			@Override
			protected EntityBufferListener<Area> createAreaListener(
					DBService dbService, int biffersize) {
				buffersizeCalls.add(biffersize);
				return null;
			}
		};
		verifyAll();
		assertEquals(4, buffersizeCalls.size());
		for (int i = 0; i < 4; i++) {
			assertEquals(new Integer(5), buffersizeCalls.get(i));
		}
	}

	protected void assertCreatedBuffer(EntityBuffer<?> areaBuffer) {
		assertEquals(1, areaBuffer.getBufferListeners().size());
		assertEquals(5, areaBuffer.getBufferListeners().get(0)
				.getWakeupEventNotificationSize());
	}

	protected void expectConfigCall(String configField, String returnValue) {
		expect(context.getConfigParameter(configField)).andReturn(returnValue);
	}

	protected <T extends BufferStrategy> void testStrategyCreation(
			Class<T> bufferStrategy) throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		setExpectations();
		replayAll();
		strategy = bufferStrategy.getConstructor(ApplicationContext.class)
				.newInstance(context);
		verifyAll();
		assertCreatedBuffer(strategy.getAreaBuffer());
		assertCreatedBuffer(strategy.getNodeBuffer());
		assertCreatedBuffer(strategy.getRelationBuffer());
		assertCreatedBuffer(strategy.getWayBuffer());
	}

	private void setExpectations() {
		expectConfigCall(ConfigConstants.CONF_BUFFERSIZE_NODE, "5");
		expectConfigCall(ConfigConstants.CONF_BUFFERSIZE_WAY, "5");
		expectConfigCall(ConfigConstants.CONF_BUFFERSIZE_RELATION, "5");
		expectConfigCall(ConfigConstants.CONF_BUFFERSIZE_AREA, "5");
		expect(context.getDBService()).andReturn(null);
	}
}
