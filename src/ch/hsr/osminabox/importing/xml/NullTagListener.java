package ch.hsr.osminabox.importing.xml;

/**
 * A Null object for a TagListener. Doing nothing.
 * @author rhof
 *
 */
public class NullTagListener implements TagListener{

	@Override
	public void addTag(String key, String value) {}

}
