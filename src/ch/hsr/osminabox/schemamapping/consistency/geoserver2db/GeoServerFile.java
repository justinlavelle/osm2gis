package ch.hsr.osminabox.schemamapping.consistency.geoserver2db;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBElement;

import ch.hsr.osminabox.context.ConfigConstants;
import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.workspace.Workspace;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.datastore.DataStore;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.featuretypes.FeatureType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.BinaryComparisonOpType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.BinaryLogicOpType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.FeatureTypeStyle;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.NamedLayer;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.PropertyNameType;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.Rule;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.StyledLayerDescriptor;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles.UserStyle;


/**
 * The Class GeoServerFile.
 * 
 * @author ameier
 */
class GeoServerFile {
	private String geoserverPath;

	private ApplicationContext context;

	private String geoserverDataStoreID;

	private final static String geoserverDataFolder = "/data";

	private final static String geoserverStylesFolder = "/styles/";

	private final static String geoserverFeatureTypesFolder = "/featureTypes/";

	private final static String geoserverStylesFileending = ".sld";

	private final static String geoserverFeatureTypeFilename = "info.xml";

	private final static String geoserverDatastoreFilename = "/datastore.xml";
	
	private final static String geoserverWorkspaceFolder = "/workspaces/";
	
	private final static String geoserverDefaultWorkspaceFile = "/default.xml";
	
	/**
	 * Instantiates a new geo server file.
	 * 
	 * @param context the context
	 */
	public GeoServerFile(ApplicationContext context){
		this.geoserverPath = context.getConfigParameter(ConfigConstants.CONF_GEOSERVER_LOCATION);
		this.context = context;
	}
	
	/**
	 * Load geoserver catalog.
	 * 
	 * @return the catalog
	 */
	public DataStore LoadGeoserverCatalog(){
		String XmlFilePath = geoserverPath+geoserverDataFolder+geoserverWorkspaceFolder+geoserverDefaultWorkspaceFile;
		Workspace w = (Workspace)context.getConfigService().loadXmlFile(XmlFilePath, "ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.workspace");
		
		setGeoserverDataStoreID(w.getName());
		
		XmlFilePath = geoserverPath+geoserverDataFolder+geoserverWorkspaceFolder+w.getName()+"/"+w.getName()+"_ds"+geoserverDatastoreFilename;
		return (DataStore)context.getConfigService().loadXmlFile(XmlFilePath,"ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.datastore");
	}
	
	/**
	 * Load geoserver feature types.
	 * 
	 * @param featureTypeFolder the feature type folder
	 * 
	 * @return the feature type
	 */
	private FeatureType LoadGeoserverFeatureTypes(String featureTypeFolder){
		String XmlFilePath=geoserverPath+geoserverDataFolder+geoserverFeatureTypesFolder+featureTypeFolder+geoserverFeatureTypeFilename;
		return (FeatureType)context.getConfigService().loadXmlFile(XmlFilePath,"ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.featuretypes");
	}
	
	/**
	 * Load geoserver styles.
	 * 
	 * @param sldFilePath the sld file path
	 * 
	 * @return the styled layer descriptor
	 */
	public StyledLayerDescriptor LoadGeoserverStyles(String sldFilePath){
		return (StyledLayerDescriptor)context.getConfigService().loadXmlFile(sldFilePath,"ch.hsr.osminabox.schemamapping.consistency.geoserver2db.xml.styles");
	}
	
	/**
	 * Gets the feature type from table.
	 * 
	 * @param tablename the tablename
	 * 
	 * @return the feature type from table
	 */
	public FeatureType getFeatureTypeFromTable(String tablename){
		FeatureType ft = LoadGeoserverFeatureTypes(getGeoserverDataStoreID()+tablename+"/");
		if (ft == null){
			return null;
		}else{
			return ft;
		}
	}
	
	/**
	 * Gets the all feature type name in namespace.
	 * 
	 * @return the all feature type name in namespace
	 */
	public List<String> getAllFeatureTypeNameInNamespace(){
		List<String> result = new ArrayList<String>();
		
		File dir = new File(geoserverPath+geoserverDataFolder+geoserverFeatureTypesFolder);
		File[] fileList = dir.listFiles();
		for(File f : fileList) {
			if (f.getName().indexOf(geoserverDataStoreID)>-1){
				result.add(f.getName().substring(geoserverDataStoreID.length()));
			}
		}
		Collections.sort(result);
		return result;
	}
	
	/**
	 * Gets the column name from feature type sld.
	 * 
	 * @param featureName the feature name
	 * 
	 * @return the column name from feature type sld
	 */
	public List<String> getColumnNameFromFeatureTypeSLD(String featureName){
		List<String> result = new ArrayList<String>();
		FeatureType ft = getFeatureTypeFromTable(featureName);
		StyledLayerDescriptor sld = LoadGeoserverStyles(getSldFilePathOfTable(ft));
		for (Object o : sld.getNamedLayerOrUserLayer()){
			if (o instanceof NamedLayer){
				for (Object o2 : ((NamedLayer) o).getNamedStyleOrUserStyle()){
					if (o2 instanceof UserStyle){
						for (FeatureTypeStyle fts : ((UserStyle)o2).getFeatureTypeStyle()){
							for (Rule r : fts.getRule()){
								if (r.getFilter() != null){
									if (r.getFilter().getComparisonOps() != null){
										if (r.getFilter().getComparisonOps().getValue() instanceof BinaryComparisonOpType){
											BinaryComparisonOpType bcot = (BinaryComparisonOpType)r.getFilter().getComparisonOps().getValue();
											result.add(getColumnFromExpression(bcot));
										}
									}else if (r.getFilter().getLogicOps() != null){					
										if (r.getFilter().getLogicOps().getValue() instanceof BinaryLogicOpType){
											BinaryLogicOpType blot = (BinaryLogicOpType)r.getFilter().getLogicOps().getValue();
											for (Object o3 : blot.getComparisonOpsOrSpatialOpsOrLogicOps()){
												if (((JAXBElement<?>)o3).getValue() instanceof BinaryComparisonOpType) {
													BinaryComparisonOpType bcot = (BinaryComparisonOpType)((JAXBElement<?>)o3).getValue();
													result.add(getColumnFromExpression(bcot));
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Gets the column from expression.
	 * 
	 * @param bcot the bcot
	 * 
	 * @return the column from expression
	 */
	private String getColumnFromExpression(BinaryComparisonOpType bcot) {
		for (Object e :bcot.getExpression()){
			if (((JAXBElement<?>)e).getValue() instanceof PropertyNameType){
				PropertyNameType pnt = (PropertyNameType)((JAXBElement<?>)e).getValue();
				return pnt.getContent();
			}
		}
		return null;
	}
	
	/**
	 * Gets the sld file path of table.
	 * 
	 * @param ft the ft
	 * 
	 * @return the sld file path of table
	 */
	public String getSldFilePathOfTable(FeatureType ft){
		if (ft != null){
			return geoserverPath+geoserverDataFolder+geoserverStylesFolder+ft.getStyles().getDefault()+geoserverStylesFileending;
		}else{
			return "not found";
		}
	}

	/**
	 * Sets the geoserver data store id.
	 * 
	 * @param geoserverDataStoreID the new geoserver data store id
	 */
	private void setGeoserverDataStoreID(String geoserverDataStoreID) {
		this.geoserverDataStoreID = geoserverDataStoreID+"_"+"ds_";
	}

	/**
	 * Gets the geoserver data store id.
	 * 
	 * @return the geoserver data store id
	 */
	public String getGeoserverDataStoreID() {
		
		String result = geoserverDataStoreID;
		return result;
	}

}
