package ch.hsr.osminabox.updating;

import org.apache.log4j.Logger;

import ch.hsr.osminabox.util.PathUtil;

public abstract class ReplicateUpdateStrategy implements UpdateStrategy{
	
	private static Logger logger = Logger.getLogger(ReplicateUpdateStrategy.class);

	private static final String FOLDER_SEPERATOR = "/";

	@Override
	public String getNextUpdateFile(String lastUpdateFile) {
		
		String fileExtension = PathUtil.getExtension(lastUpdateFile);
		String file = PathUtil.removeExtension(lastUpdateFile);
		
		if(fileExtension.length() <= 0 || file.length() <= 0){
			logger.error("No valid Differential Update File: " + lastUpdateFile);
			System.exit(0);
		}
		
		int AAA = Integer.parseInt(file.substring(0, 3));
		int BBB = Integer.parseInt(file.substring(3, 6));
		int CCC = Integer.parseInt(file.substring(6, 9));
		
		if(CCC == 999){
			CCC = 0;
			if(BBB == 999){
				BBB = 0;
				if(AAA == 999){
					AAA = 0;
				}
				else
					{AAA += 1;}
			}
			else
				{BBB += 1;}
		}
		else
			{CCC += 1;}
		
		return  String.format("%03d", AAA) + String.format("%03d", BBB) + String.format("%03d", CCC) + fileExtension;
	}

	public String getUpdateFileAsUrl(String updateFile){
		String fileExtension = PathUtil.getExtension(updateFile);
		String file = PathUtil.removeExtension(updateFile);
		
		if(file.length() != 9)
			return updateFile;
		
		return file.substring(0, 3) + FOLDER_SEPERATOR + file.substring(3, 6) + FOLDER_SEPERATOR + file.substring(6, 9) + fileExtension;
	}
}
