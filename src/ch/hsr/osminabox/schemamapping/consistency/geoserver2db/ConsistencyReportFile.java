package ch.hsr.osminabox.schemamapping.consistency.geoserver2db;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.hsr.osminabox.schemamapping.xml.Column;
import ch.hsr.osminabox.schemamapping.xml.Mapping;
import ch.hsr.osminabox.schemamapping.xml.RelatedTable;
import ch.hsr.osminabox.schemamapping.xml.Tag;

/**
 * The Class StatisticFile.
 * 
 * @author ameier
 */
public class ConsistencyReportFile {
	
	/** The statistic file. */
	private String statisticFile=""; 
	
	/** The statistic file summary. */
	private String statisticFileSummary="";
	
	private SimpleDateFormat dateFormatterFileName;
	private SimpleDateFormat dateFormatterTitle;
	private Date currentTime;
	
	private boolean hasErrors = false;
	
	public ConsistencyReportFile(){
		this.dateFormatterFileName = new SimpleDateFormat("yyyyMMdd'_'HHmmss");
		this.dateFormatterTitle = new SimpleDateFormat("dd.MM.yyyy HH:mm");
		this.currentTime = new Date();
	}
	
	/**
	 * Write to statistic.
	 * 
	 * @param text the text
	 */
	public void writeToStatistic(Object text){
		statisticFile=statisticFile+text;
	}
	
	/**
	 * Write to statistic summary.
	 * 
	 * @param text the text
	 */
	private void writeToStatisticSummary(Object text){
		statisticFileSummary=statisticFileSummary+text;
	}

	/**
	 * Adds the line.
	 * 
	 * @param lineNumbers the line numbers
	 */
	public void addLine(int lineNumbers){
		for (int i = 0;i<lineNumbers;i++){
			statisticFile=statisticFile+"\n----------------------------------------------------------------";
		}
	}
	
	/**
	 * Adds the short line.
	 * 
	 * @param lineNumbers the line numbers
	 */
	public void addShortLine(int lineNumbers){
		for (int i = 0;i<lineNumbers;i++){
			statisticFile=statisticFile+"\n--------------------------------";
		}
	}
	
	
	/**
	 * Save statistic file.
	 * 
	 * @param dateFormatterFileName the formatter
	 * @param currentTime the current time
	 */
	public void saveStatisticFile() {
		try {
			writeToStatisticSummary("\n\n\n");
			statisticFile=
				statisticFileSummary+
				"\nDetails"+
				"\n=======\n\n"+				
				statisticFile;

			Writer fw_Backup;
			String filename = "Consistency_Report_"+dateFormatterFileName.format(currentTime)+".txt";
			fw_Backup = new FileWriter("./"+filename);
			fw_Backup.write(statisticFile);
			fw_Backup.close();
			System.out.println("Consistency check file has been written in "+filename+" in the osm2gis folder.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeSummary(String column1, String column2){
		writeToStatisticSummary("\n\t"+String.format("%1$-28s%2$8s", column1,column2));
	}
	
	
	public void writeHeadSummaryDstTablesToDstMappings(){
		writeToStatisticSummary("Consistency report from "+dateFormatterTitle.format(currentTime)+
				"\n################################################"+
				"\n"+
				"\nSummary"+
				"\n======="+
				"\n\n\t1 src_to_dst_mappings to dst_schema_def in mappingconfiguration"+
				"\n\t--------------------------------------------------"+
				"\n\tOnly the mappings with errors are listed below!");
		writeSummary("mapping","error(s)");
		writeSummary("----------","-------------");
	}
	
	public void writeHeadSummaryTdToDb(){
		writeToStatisticSummary("\n\n\n"+
				"\n\n\t2 dst_schema_def in mappingconfiguration to DB"+
				"\n\t--------------------------------------------------");
		writeSummary("table name","difference(s)");
		writeSummary("----------","-------------");
	}

	
	public void writeHeadSummaryFtToDb(){
		writeToStatisticSummary("\n\n\n"+
				"\n\n\t3 GeoServer feature type (inc. SLD) to DB"+
				"\n\t--------------------------------------------------");
		writeSummary("feature type","error(s)");
		writeSummary("------------","--------");
	}
	
	
	
	public void writeHeadSummaryMcToSLDTable(){
		writeToStatisticSummary("\n\n\n"+
				"\n\n\t4 mapping configurations to GeoServer (*.SLD)"+
				"\n\t--------------------------------------------------");
		writeSummary("table name","hint(s)");
		writeSummary("----------","-------");
	}
	
	public void writeHeadSummaryMcToSLDView(){
		
		writeToStatisticSummary("\n");
		writeSummary("view name","hint(s)");
		writeSummary("---------","-------");
	}
	
	/**
	 * Adds the mapping key value to statistic.
	 * 
	 * @param m the m
	 */
	public void addMappingKeyValueToStatistic(Mapping m) {
		writeToStatistic("\n\t<mapping type=\""+m.getType().value()+"\">"+
									   "\n\t\t<and_ed_conditions>");
		for (Tag nt : m.getAndEdConditions().getTag()){
			writeToStatistic("\n\t\t\t<tag key=\""+nt.getK()+"\" value=\""+nt.getV()+"\"/>");
		}
		writeToStatistic("\n\t\t</and_ed_conditions>"+
									   "\n\t\t<dst_table name=\""+m.getDstTable().getName()+"\" />"+
									   "\n\t\t<dst_columns>");
		for (ch.hsr.osminabox.schemamapping.xml.Column c : m.getDstColumns().getColumn()){
			writeToStatistic("\n\t\t\t<column name=\""+c.getName()+"\" value=\""+c.getValue()+"\"/>");
		}
		writeToStatistic("\n\t\t</dst_columns>");
		if (m.getMembers() != null){
			writeToStatistic("<member>");
			for (RelatedTable rt : m.getMembers().getRelatedTable()){
				writeToStatistic("<related_table name=\""+rt.getName()+"\">");
				writeToStatistic("<joint_table name=\""+rt.getJoinTable().getName()+"\"/>");
				if (rt.getJoinTableColumns() != null){
					writeToStatistic("<join_table_columns>");
					for (Column c : rt.getJoinTableColumns().getColumn()){
						writeToStatistic("<column name=\""+c.getName()+"\" value=\""+c.getValue()+"\" />");
					}
					writeToStatistic("</join_table_columns>");
				}
				
				writeToStatistic("</related_table>");
			}
			writeToStatistic("</member>");
		}
		writeToStatistic("\n\t</mapping>");
	}

	public boolean isHasErrors() {
		return hasErrors;
	}

	public void setHasErrors(boolean hasErrors) {
		this.hasErrors = hasErrors;
	}
	
	public String getDetailText(){
		return statisticFile;
	}
}
