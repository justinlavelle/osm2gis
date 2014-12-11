package ch.hsr.osminabox.context;

/**
 * This Class holds the String constants for the config property file.
 * 
 * @author rhof
 */
public class ConfigConstants {

	public static final String CONF_PLANET_FILE = "osm.defaults.planetfile";
	public static final String CONF_PLANET_URL = "osm.defaults.host";
	public static final String CONF_DIFF_FREQUENCY = "diff.scheduler.updatefrequency";
	public static final String CONF_DIFF_UPDATEROOT = "diff.scheduler.updateroot";
	public static final String CONF_DIFF_CURRENT_FILE = "diff.scheduler.currentfile";
	public static final String CONF_DIFF_CURRENT_FILE_REPLICATE = "diff.scheduler.currentfilereplicate";
	public static final String CONF_DIFF_DIRECTORY_DAILY = "diff.scheduler.directory.daily";
	public static final String CONF_DIFF_DIRECTORY_HOURLY = "diff.scheduler.directory.hourly";
	public static final String CONF_DIFF_DIRECTORY_MINUTELY = "diff.scheduler.directory.minutely";
	public static final String CONF_LAT_MIN = "osm2gis.importer.boundingboxlat.min";
	public static final String CONF_LAT_MAX = "osm2gis.importer.boundingboxlat.max";
	public static final String CONF_LON_MIN = "osm2gis.importer.boundingboxlong.min";
	public static final String CONF_LON_MAX = "osm2gis.importer.boundingboxlong.max";
	public static final String CONF_DOWNLOAD_DIR = "osm2gis.download.folder";
	
	public static final String CONF_OSM2GIS_STATEFILE = "osm2gis.statefile";
	public static final String CONF_OSM2GIS_STATEFILE_DATE_FROMAT = "osm2gis.statefile.dateformat";
	
	public static final String CONF_MAPPING_CONFIGFILE = "conf.mapping.file";
	
	public static final String CONF_GEOSERVER_LOCATION = "osm2gis.geoserver.location";
	
	public static final String CONF_DB_PW = "db.password";
	public static final String CONF_DB_PORT = "db.port";
	public static final String CONF_DB_DATABASE = "db.name";
	public static final String CONF_DB_USERNAME = "db.username";
	public static final String CONF_DB_HOST = "db.host";
	
	public static final String CONF_BUFFERSIZE_NODE = "buffersize.node";
	public static final String CONF_BUFFERSIZE_WAY = "buffersize.way";
	public static final String CONF_BUFFERSIZE_RELATION = "buffersize.relation";
	public static final String CONF_BUFFERSIZE_AREA = "buffersize.area";
	
}
