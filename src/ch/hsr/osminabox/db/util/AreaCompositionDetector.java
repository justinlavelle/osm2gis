package ch.hsr.osminabox.db.util;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.AreaComposition;
import ch.hsr.osminabox.db.entities.Area.WayRole;

/**
 * This Class determines the composition of an Area.
 * 
 * As there is any String possible for a RelationMembers Role-Attribute, if the Relations is treated as a
 * Multipolygon (Area), only a defined amount of Roles are accepted.
 * 
 * @See also http://wiki.openstreetmap.org/wiki/Talk:Relation:multipolygon#Advanced_multipolygons
 * 
 * @author jzimmerm
 */
public class AreaCompositionDetector {
	
	/** These WayRoles define a Way as the outer Border of a Multipolygon. */
	public final static WayRole[] OUTER_WAYROLES = {WayRole.none, WayRole.outer, WayRole.exclave};
	
	/** These WayRoles define a Way as the inner Border (a hole) of a Multipolygon. */
	public final static WayRole[] INNER_WAYROLES = {WayRole.inner, WayRole.enclave};
	
	/**
	 * Detects the composition of an Area. WayCombining must be completed on this point.
	 * @param area
	 * @return
	 */
	public AreaComposition detect(Area area) {
		//An Area must have Ways...
		if(area.ways == null || area.ways.size() == 0)
			return AreaComposition.UNKNOWN;
		
		
		if(checkAreaForOuterOnly(area)) {
			return AreaComposition.ONLY_OUTER;
		}
		
		else if(checkAreaForOneOuterNInner(area)) {			
			return AreaComposition.ONE_OUTER_N_INNER;				 
		 }
		
		else if(checkAreaForNOuterNInner(area)) {
			return AreaComposition.N_INNER_N_OUTER;
		}
		
		else {
			return AreaComposition.UNKNOWN;
		}
	}
	
	/**
	 * Checks if an Area has at least two outer Borders and one+ inner
	 * @param area
	 * @return
	 * 		True if the Area has at least two outer Borders and one+ inner <br/>
	 * 		else false
	 */
	private boolean checkAreaForNOuterNInner(Area area) {
		int inner=0;
		int outer=0;
		
		for(WayRole role : area.ways.values()){
			if(contains(INNER_WAYROLES, role)) inner++;
			else if(contains(OUTER_WAYROLES, role)) outer ++;
		}
		
		if(inner + outer == area.wayIds.size() && inner > 0 && outer > 1)
			return true;
		else
			return false;
	}

	/**
	 * Checks if the Area consists of only one outer Border with any number of inner holes.
	 * 
	 * @param area
	 * @return
	 * 		true if an Area has 1 outer and n inner Borders<br/>
	 * 		else false
	 */
	private boolean checkAreaForOneOuterNInner(Area area) {
		int outer =0;
		int inner = 0;
		
		for(WayRole role : area.ways.values()){
			if(contains(INNER_WAYROLES, role)) inner++;
			else if(contains(OUTER_WAYROLES, role)) outer++;
			else return false;
		}
		
		if(outer == 1)
			return true;
		else
			return false;
		
	}
	/**
	 * Checks if the Area is Type 1: One or many Areas with NO holes.
	 * 
	 * @param area
	 * @return
	 */
	private boolean checkAreaForOuterOnly(Area area) {
		for(WayRole role : area.ways.values()){
			if(!contains(OUTER_WAYROLES, role)) return false;
		}
		return true;		
	}
	
	/**
	 * Checks weather the searched role is contained by the given roles array.
	 * @param roles The Array to be searched in
	 * @param role The searched role
	 * @return true if the role is contained by the Array.
	 */
	public boolean contains(WayRole[] roles, WayRole role){
		for(int i=0; i<roles.length; i++){
			if(roles[i].equals(role))
				return true;
		}
		return false;
	}

}
