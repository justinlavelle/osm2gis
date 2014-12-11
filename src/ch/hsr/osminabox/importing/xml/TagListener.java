package ch.hsr.osminabox.importing.xml;

/**
 * An Interface used by XMLTagHandlers to retrive Tags from a sub XMLTagHandler
 * @author rhof
 *
 */
public interface TagListener {

	public void addTag(String key, String value);
	
}
