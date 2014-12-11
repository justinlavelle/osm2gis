package ch.hsr.osminabox.importing;

import java.util.List;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.DBService;
import ch.hsr.osminabox.importing.listener.EntityBufferListener;

public class MockEnityBufferListener extends EntityBufferListener<Node> {
	
	public MockEnityBufferListener(DBService db, int bufferSize) {
		super(db, bufferSize);
	}

	private boolean handleRestCalled = false;

	private boolean handleOnWakeUpCalled = false;

	@Override
	public int getWakeupEventNotificationSize() {
		return 20;
	}

	@Override
	public void handleFlush(List<Node> entitys) {
		handleRestCalled = true;
	}

	@Override
	public void handleWakeupEvent(List<Node> entitys) {
		handleOnWakeUpCalled = true;

	}
	
	public boolean isHandleRestCalled() {
		return handleRestCalled;
	}

	public boolean isHandleOnWakeUpCalled() {
		return handleOnWakeUpCalled;
	}


}
