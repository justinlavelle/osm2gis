package ch.hsr.osminabox.db.mapping;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.MappingType;

/**
 * @author jzimmerm
 *	
 *	This Dictionary converts the JAXP generated SrcToDstMappings into a List of all Mapping's AndEdConditions Key and Values and stores them
 *	in an internal Map. This fastens the access when searching for mapping-candidates while processing OSMEntities. *	
 *
 */
public class MappingsDictionary {
	
	private ConfigService configService;
	private List<Map<String, Set<String>>> mappings;
	private final MappingType mappingType;
	
	public MappingsDictionary(MappingType mappingType, ConfigService configService){
		this.configService = configService;
		
		this.mappingType = mappingType;
		mappings = configService.getMappings(mappingType);
	}
	
	/**
	 * Finds all "AndEdConditions"-matching <mapping> entries from the mapping-file and returns them.
	 * @param keyValues all key-value-pairs which should be checked for a match.
	 * @return the best matching Mapping-entries.
	 */
	public List<Mapping> getMatches(Map<String, Set<String>> keyValues){
		return getBestMatches(getCandidates(keyValues));
	}
	
	/**
	 * Filters duplicated sub-matches
	 * @param candidates
	 * @return
	 */
	private List<Mapping> getBestMatches(List<Map<String, Set<String>>> candidates){
		
		
		removeDuplicated(candidates);

		List<Mapping> matches = new LinkedList<Mapping>();
		
		for(Map<String, Set<String>> candidate : candidates){
			
			boolean isSubMap = false;
			for(Map<String, Set<String>> other : candidates){
				if(other != candidate && isSubMap(other, candidate)){
					isSubMap = true;
					break;
				}
			}
			if(!isSubMap)
				try {
					ArrayList<Integer> matches_indices = getIndicesOfMatchesWithThisLeftSide(mappings, candidate);
					for (int i=0; i<matches_indices.size(); i++) {
						matches.add(configService.getXmlMapping(mappingType, matches_indices.get(i).intValue()));
					}
				} catch (NoSchemaDefAndMappingConfigXMLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		return matches;
	}


	/**
	 * Returns true if submap is a subset of map, that means:
	 * each Key in submap must exist in map
	 * every Value of a Key must exist in the value-list of that key in map.
	 * 
	 * Ex. submap[{amenity=[parking, fuel]},{author=[jzimmerm]}] is a subset of 
	 *     map[{amenity=[fuel, garage, parking]}, {author=[jzimmerm]}, {optional=[this, is, optional]}]
	 * 
	 * @param map
	 * @param submap
	 * @return true if the submap is a subset of map.
	 */
	private static boolean isSubMap(Map<String, Set<String>> map, Map<String, Set<String>> submap){
		
		Set<String> submapKeySet = submap.keySet();

        if (map.keySet().containsAll(submapKeySet)) {
              for (String key : submapKeySet) {
	              for (String value : submap.get(key)) {
	                    if (!map.get(key).contains(value))
	                          return false;
	              }
              }
              return true;
        }
        return false;
	}
	
	/**
	 * Returns a List of all matching mapping candidates from the configuration.
	 * A Mapping section from the config file is a candidate, if all its NodesTags (Key, Value) entries find a match in the given keyValues-Map.
	 * @param keyValues An entities Key-Value pairs.
	 * @return A List<Mapping> containing all candidates.
	 */
	private List<Map<String, Set<String>>> getCandidates(Map<String, Set<String>> keyValues){
		
		List<Map<String, Set<String>>> candidates = new LinkedList<Map<String, Set<String>>>();	
		
		for(Map<String, Set<String>> mapping : mappings){
			if(isSubMap(keyValues, mapping)){
				candidates.add(mapping);
			}
		}
		return candidates;
	}	
	
	
	
	
	
	/**
	 * Given a list of mapping conditions and a certain mapping condition, returns
	 * the indices of the elements in the list that are exactly the same as the
	 * indicated mapping.
	 * 
	 * @param mapps
	 * @param cand
	 * @return
	 */
	private ArrayList<Integer> getIndicesOfMatchesWithThisLeftSide(
			List<Map<String, Set<String>>> mapps,
			Map<String, Set<String>> cand) {
		
		ArrayList<Integer> resp = new ArrayList<Integer>();
		
		int mapps_len = mapps.size();
		for (int i=0; i<mapps_len; i++) {
			// if set A is subset of set B and set B is subset of set A, then A = B
			if ((isSubMap(cand, mapps.get(i))) && (isSubMap(mapps.get(i), cand))) {
				resp.add(new Integer(i));
			}
		}
		return resp;
	}

	/**
	 * Removes mappings that are exactly the same, leaves one occurrence.
	 * @param candidates
	 */
	private void removeDuplicated(List<Map<String, Set<String>>> candidates) {
		
		for(Map<String, Set<String>> candidate : candidates){
			for(Map<String, Set<String>> other : candidates){
				if(other != candidate && isSubMap(other,candidate) && isSubMap(candidate,other)){
					// remove one
					candidates.remove(other);
					// call method recursively and return 
					removeDuplicated(candidates);
					return;
				}
			}
		}
	}
}
