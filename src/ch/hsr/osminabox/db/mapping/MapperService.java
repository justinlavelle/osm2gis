package ch.hsr.osminabox.db.mapping;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.db.entities.Area;
import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.entities.Relation;
import ch.hsr.osminabox.db.entities.Way;
import ch.hsr.osminabox.schemamapping.ConfigService;
import ch.hsr.osminabox.schemamapping.xml.MappingType;
import ch.hsr.osminabox.schemamapping.xml.Mapping;

public class MapperService {
	
	private static Logger logger = Logger.getLogger(MapperService.class);
	
	private MappingsDictionary pointDictionary;
	private MappingsDictionary linestringDictionary;
	private MappingsDictionary multipolygonDictionary;
	private MappingsDictionary relationDictionary;
	
	private MappingAppender generalMappingAppender;
	private MappingAppender relationMappingAppender;
	
	public MapperService(ConfigService config){
			pointDictionary = new MappingsDictionary(MappingType.POINT, config);
			linestringDictionary = new MappingsDictionary(MappingType.LINESTRING, config);
			multipolygonDictionary = new MappingsDictionary(MappingType.MULTIPOLYGON, config);
			relationDictionary = new MappingsDictionary(MappingType.RELATION, config);
			
			generalMappingAppender = new GeneralMappingAppender();
			relationMappingAppender = new RelationMappingAppender();
	}
	
	/**
	 * Adds all applicable dbMappings to the Nodes.
	 * @param nodes
	 */
	public void addDbMappingsForNodes(List<Node> nodes){
		int i = addDBMappings(nodes, pointDictionary, generalMappingAppender);
		logger.info("Total " + i + " Mappings added to " + nodes.size() + " Nodes.");
	}
	
	/**
	 * Adds all applicable dbMappings to the Ways.
	 * @param ways
	 */
	public void addDbMappingsForWays(List<Way> ways){
		int i = addDBMappings(ways, linestringDictionary, generalMappingAppender);
		logger.info("Total " + i + " Mappings added to " + ways.size() + " Ways.");
	}
	
	/**
	 * Adds all applicable dbMappings to the Areas.
	 * @param areas
	 */
	public void addDbMappingsForAreas(List<Area> areas){
		int i = addDBMappings(areas, multipolygonDictionary, generalMappingAppender);
		logger.info("Total " + i + " Mappings added to " + areas.size() + " Areas.");
	}

	/**
	 * Adds all applicable dbMappings to the Relations.
	 * @param relations
	 */
	public void addDbMappingsForRelations(List<Relation> relations){
		int i = addDBMappings(relations, relationDictionary, relationMappingAppender);
		logger.info("Total " + i + " Mappings added to " + relations.size() + " Relations.");
	}

	/**
	 * Adds all applicable dbMappings from the mappingconfig File to the entities.
	 * @param entities
	 * @param dictionary
	 * @param mappingAppender
	 * @return
	 */
	private int addDBMappings(List<? extends OSMEntity> entities, MappingsDictionary dictionary, MappingAppender mappingAppender) {
		
		int i = 0;
		for(OSMEntity entity: entities){
			
			if(entity.getOsmId()==62072251)
				System.out.println("halt");
			
			if(entity.tags.size() <= 0){
				logger.trace("No Tags on Entity with Osm Id: " + entity.getOsmId() + ", don't search for Mappings.");
				continue;
			}
			
			if(logger.isTraceEnabled()){
				logger.trace("Searching Mapping for Entity with OSM Id: " + entity.getOsmId());
				traceMappings(entity.tags);
			}
			
			for(Mapping mapping : dictionary.getMatches(entity.tags)){
				i++;
				logger.trace("Found Mapping: " + mapping.getDstTable().getName() + "\n");	
				
				mappingAppender.appendMapping(entity, mapping);
			}
		}		
		return i;
	}
	
	private void traceMappings(Map<String, Set<String>> tags) {
		logger.trace("Mappings:");
		for(Map.Entry<String, Set<String>> entry : tags.entrySet()){
			logger.trace(entry.getKey()+"="+entry.getValue());
		}
	}

}
