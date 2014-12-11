package ch.hsr.osminabox.db.util;

/**
 * Defines the xml tag an Osm Entity is contained by within a differential update file.
 * On initial import, use "create"
 * 
 * @author Joram
 *
 */

public enum DiffType {
	create,
	modify,
	delete
}
