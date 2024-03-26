import java.util.LinkedHashMap;
import java.util.Map;

public class TableHeaders {
	//private static HashMap<String, Object[]> headers;
	private static LinkedHashMap<Integer, String[]> headers;
	private static int colCount;
	private static String[] jTableHeaders;
	private static String[] dbHeaders;
	
	static {
		headers = new LinkedHashMap<>();
		headers.put(0, new String[]{"ID", "ID"});
		headers.put(1, new String[]{"Priorität", "Prioritaet"});
		headers.put(2, new String[]{"Vorname", "Vorname"});
		headers.put(3, new String[]{"Nachname", "Nachname"});
		headers.put(4, new String[]{"Firma", "Firma"});
		headers.put(5, new String[]{"Branche", "Branche"});
		headers.put(6, new String[]{"Abteilung", "Abteilung"});
		headers.put(7, new String[]{"E-Mail", "E_Mail"});
		headers.put(8, new String[]{"Telefon", "Telefon"});
		headers.put(9, new String[]{"Bevorzugter Social Media Kanal", "Social_Media"});
		headers.put(10, new String[]{"Sprache", "Sprache"});
		headers.put(11, new String[]{"Land", "Land"});
		headers.put(12, new String[]{"Bundesland", "Bundesland"});
		headers.put(13, new String[]{"PLZ", "PLZ"});
		headers.put(14, new String[]{"Stadt", "Stadt"});
		headers.put(15, new String[]{"Hausnummer", "Hausnummer"});
		headers.put(16, new String[]{"Interesse an", "Interesse_an"});
		headers.put(17, new String[]{"Erinnerung", "Erinnerung"});
		
		colCount = headers.size();
		storeColNamesInArrays();
		
		
		/*headers.put("ID", new Object[]{"ID", 0});
		headers.put("Priorität", new Object[]{"Prioritaet", 1});
		headers.put("Vorname", new Object[]{"Vorname", 2});
		headers.put("Nachname", new Object[]{"Nachname", 3});
		headers.put("Firma", new Object[]{"Firma", 4});
		headers.put("Branche", new Object[]{"Branche", 5});
		headers.put("Abteilung", new Object[]{"Abteilung", 6});
		headers.put("E-Mail", new Object[]{"E_Mail", 7});
		headers.put("Telefon", new Object[]{"Telefon", 8});
		headers.put("Bevorzugter Social Media Kanal", new Object[]{"Social_Media", 9});
		headers.put("Sprache", new Object[]{"Sprache", 10});
		headers.put("Land", new Object[]{"Land", 11});
		headers.put("Bundesland", new Object[]{"Bundesland", 12});
		headers.put("PLZ", new Object[]{"PLZ", 13});
		headers.put("Stadt", new Object[]{"Stadt", 14});
		headers.put("Hausnummer", new Object[]{"Hausnummer", 15});
		headers.put("Interesse an", new Object[]{"Interesse_an", 16});
		headers.put("Erinnerung", new Object[]{"Erinnerung", 17});*/
	}
	
	/*public static  LinkedHashMap<Integer, String[]> getHeaders() {
		return headers;
	}*/
	
	public static String getDBColNameByColIndex(int colIndex) {
		return dbHeaders[colIndex];
	}
	
	public static String getJTableColNameByColIndex(int colIndex) {
		return jTableHeaders[colIndex];
	}

	public static int getColCount() {
		return colCount;
	}
	
	public static String[] getJTableHeaders() {
		return jTableHeaders;
	}
	
	public static String[] getDBHeaders() {
		return dbHeaders;
	}
	
	public static int getJTableColNumByDbColName(String dbColName) {
		for (Map.Entry<Integer, String[]> entry : headers.entrySet()) {
			if (entry.getValue()[1].equals(dbColName)) {
				return entry.getKey().intValue();
			}
		}
		return -1;
	}
	
	public static int getJTableColNumByJTableColName(String tableColName) {
		for (Map.Entry<Integer, String[]> entry : headers.entrySet()) {
			if (entry.getValue()[0].equals(tableColName)) {
				return entry.getKey().intValue();
			}
		}
		return -1;
	}
	
	private static void storeColNamesInArrays() {
		jTableHeaders = new String[colCount];
		dbHeaders = new String[colCount];
		
		int i = 0;
		for (Map.Entry<Integer, String[]> entry : headers.entrySet()) {
			jTableHeaders[i] = entry.getValue()[0];
			dbHeaders[i] = entry.getValue()[1];
			i++;
		}
	}
}
