package ch.hsr.osminabox.db.util;

import java.util.HashMap;
import java.util.Set;

public class DataInitialisation {
	
	public HashMap<String, StringBuffer> initiateHashMap(Set<String> tables) {
		HashMap<String, StringBuffer> statements = new HashMap<String, StringBuffer>();
		for(String table: tables) {
			statements.put(table, new StringBuffer());
		}
		return statements;
	}
}
