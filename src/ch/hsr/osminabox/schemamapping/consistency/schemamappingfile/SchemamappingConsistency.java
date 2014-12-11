package ch.hsr.osminabox.schemamapping.consistency.schemamappingfile;

import java.util.LinkedList;
import java.util.List;

import ch.hsr.osminabox.context.ApplicationContext;
import ch.hsr.osminabox.schemamapping.consistency.geoserver2db.ConsistencyReportFile;
import ch.hsr.osminabox.schemamapping.exceptions.NoSchemaDefAndMappingConfigXMLException;
import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.DstColumn;
import ch.hsr.osminabox.schemamapping.xml.DstJoinTableDef;
import ch.hsr.osminabox.schemamapping.xml.DstSchemaDef;
import ch.hsr.osminabox.schemamapping.xml.DstTableDef;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.SrcToDstMappings;

public class SchemamappingConsistency {

	private ApplicationContext context;
	private ConsistencyReportFile reportFile;
	private Integer errorCounter;
	
	public SchemamappingConsistency(ApplicationContext context){
		this.context = context;
	}
	
	public ConsistencyReportFile startSchemamappingConsistency(){
		reportFile = new ConsistencyReportFile();
		
		
		reportFile.writeHeadSummaryDstTablesToDstMappings();
		reportFile.writeToStatistic("\n1 Consistency check, src_to_dst_mappings to dst_schema_def in mapping configuration");
		reportFile.addLine(1);
		try {
			checkMappingAgainstDstSchemaDef(context.getConfigService().getSrcToDstMappings(),context.getConfigService().getDstSchemaDef());
		} catch (NoSchemaDefAndMappingConfigXMLException e) {
			e.printStackTrace();
			reportFile.setHasErrors(true);
		}
		
		reportFile.addLine(1);
		reportFile.writeToStatistic("\n\n");
		
		return reportFile;
	}
	
	private void checkMappingAgainstDstSchemaDef(SrcToDstMappings stdm,DstSchemaDef dsd ){
		boolean tableFound;
		int counter = 0;
		for (Mapping m : stdm.getMapping()){
			counter++;
			System.out.println("Checking mapping "+counter+" ...");
			
			tableFound = false;
			List<String> columnsNotFound = new LinkedList<String>();
			errorCounter = 0;
			DstTableDef inheritedTable=null;
			//Checking TableDefs ignore Userdefined
			for (Object o : dsd.getDstTableDefOrDstTableDefUserDefined()){
				if (o instanceof DstTableDef){
					DstTableDef table = (DstTableDef)o;
					inheritedTable = context.getConfigService().getInheritedTable(dsd, table.getInherits());
					
					if (table.getName().toLowerCase().equals(m.getDstTable().getName().toLowerCase())){
						 tableFound = true;
						 List<DstColumn> dc = table.getDstColumn();
						 if (inheritedTable != null){
							 dc.addAll(inheritedTable.getDstColumn());
						 }
						 columnsNotFound = checkColumnNamesOfMapping(m ,dc);
						 break;
					}
				}
				
			}
			
			//Relationtables
			if (!tableFound){
				for (DstJoinTableDef table : dsd.getDstJoinTableDef()){
					if (table.getName().toLowerCase().equals(m.getDstTable().getName().toLowerCase())){
						 tableFound = true;
						 List<DstColumn> dc = table.getDstColumn();
						 if (inheritedTable != null){
							 dc.addAll(inheritedTable.getDstColumn());
						 }
						 columnsNotFound = checkColumnNamesOfMapping(m ,dc);
						 break;
					}
				}
			}
			
			if (!tableFound){
				errorCounter++;
			}
			
			if (errorCounter > 0){
				reportFile.writeSummary("k=\""+m.getAndEdConditions().getTag().get(0).getK()+"\" v=\""+m.getAndEdConditions().getTag().get(0).getV()+"\"", errorCounter.toString());
				
				if (!tableFound){
					reportFile.writeToStatistic("\nNo table ("+m.getDstTable().getName()+") found for mapping:\n");
				}else{
					if (columnsNotFound.size() > 0 ){
						reportFile.writeToStatistic("\nColumn(s):\n");
						for (String text :columnsNotFound){
							reportFile.writeToStatistic("\t"+text+"\n");
						}
						reportFile.writeToStatistic("not found, from mapping:\n");
					}
				}
				
				reportFile.addMappingKeyValueToStatistic(m);
				reportFile.addShortLine(1);
				
				reportFile.setHasErrors(true);
			}
		}
	}
	
	private List<String> checkColumnNamesOfMapping(Mapping m, List<DstColumn> dc) {
		List<String> result = new LinkedList<String>();
		boolean columnFound;
		for (Column dcM : m.getDstColumns().getColumn()){
			columnFound = false;
			for (DstColumn c: dc){
				if (c.getName().toLowerCase().equals(dcM.getName().toLowerCase())){
					columnFound = true;
					break;
				}
			}
			if (!columnFound){
				result.add(dcM.getName());
				errorCounter++;
			}
		}
		return result;
	}
	
}
