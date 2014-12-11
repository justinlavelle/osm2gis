package ch.hsr.osminabox.db;

/**
* This Constants are used for SQL Syntax. Do not change unless you also
* change the Database schema
* @author jzimmerm
*
*/
public class XMLConstants {
	
	public static final String ATTRIBUTE_ID = "%attribute_id%";
	public static final String ATTRIBUTE_TIMESTAMP = "%attribute_timestamp%";
	public static final String ATTRIBUTE_LATITUDE = "%attribute_lat%";
	public static final String ATTRIBUTE_LONGITUDE = "%attribute_lon%";
	public static final String TAGS_ALL = "%tags_all%";
	public static final String ND_ALL = "%nd_all%";
	public static final String MEMBERS_ALL = "%members_all%";
	
	public static final String TAG_TYPE = "type";
	public static final String TAG_TYPE_MULTIPOLYGON = "multipolygon";
	public static final String TAG_TYPE_BOUNDARY = "boundary";
	
	public static final String MEMBER_REF = "%member_ref%";
	public static final String MEMBER_TYPE = "%member_type%";
	public static final String MEMBER_ROLE = "%member_role%";

}
