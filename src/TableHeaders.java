import java.util.LinkedHashMap;
import java.util.Map;

public class TableHeaders {
	private static LinkedHashMap<Integer, String[]> headers;
	private static int colCount;
	private static String[] jTableHeaders;
	private static String[] dbHeaders;
	private static String insertQueryHeadersString;
	private static String updateQueryHeadersString;
	
	static {
		// headers: LinkedHashMap mit folgendem Aufbau --> Key: Spaltennummer, Value 1: Spaltenname im JTable, Value 2: Spaltenname in der Datenbank
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
		buildHeadersStringForInsertQuery();
		buildHeadersStringForUpdateQuery();
	}
	
	public static String getDBColNameByColIndex(int colIndex) {
		// Bekommt den Spaltenindex als Input und gibt den dazugehörigen Spaltennamen in der Datenbank zurück
		return dbHeaders[colIndex];
	}
	
	public static String getJTableColNameByColIndex(int colIndex) {
		// Bekommt den Spaltenindex als Input und gibt den dazugehörigen Spaltennamen im JTable zurück
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
	
	public static String getInsertQueryHeadersString() {
		return insertQueryHeadersString;
	}
	
	public static String getUpdateQueryHeadersString() {
		return updateQueryHeadersString;
	}
	
	public static int getJTableColNumByDbColName(String dbColName) {
		// Nimmt einen Spaltennamen aus der Datenbank als Input und gibt die Spaltennummer im JTable zurück
		for (Map.Entry<Integer, String[]> entry : headers.entrySet()) {
			if (entry.getValue()[1].equals(dbColName)) {
				return entry.getKey().intValue();
			}
		}
		return -1;
	}
	
	public static int getJTableColNumByJTableColName(String tableColName) {
		// Nimmt einen Spaltennamen aus dem JTable als Input und gibt die Spaltennummer im JTable zurück
		for (Map.Entry<Integer, String[]> entry : headers.entrySet()) {
			if (entry.getValue()[0].equals(tableColName)) {
				return entry.getKey().intValue();
			}
		}
		return -1;
	}
	
	private static void storeColNamesInArrays() {
		// Speichert alle Spaltennamen aus dem JTable und der Datenbank in Arrays
		jTableHeaders = new String[colCount];
		dbHeaders = new String[colCount];
		
		int i = 0;
		for (Map.Entry<Integer, String[]> entry : headers.entrySet()) {
			jTableHeaders[i] = entry.getValue()[0];
			dbHeaders[i] = entry.getValue()[1];
			i++;
		}
	}
	
	private static void buildHeadersStringForInsertQuery() {
		// Diese Methode baut einen String mit allen Spaltennamen in der Datenbank auf, um diesen String für Queries verwenden zu können
		StringBuilder builder = new StringBuilder();
		
		for (String header : dbHeaders) {
			// Die ID soll nicht vom User eingegeben werden, deswegen überspringen wir diesen header
			if (header != TableHeaders.getDBColNameByColIndex(0)) {
				builder.append(header + ", ");				
			}
		}
		
		String colNamesStr = builder.toString();
		
		// Das letzte Leerzeichen und Komma abschneiden
		colNamesStr = colNamesStr.substring(0, builder.length() - 2);
		
		insertQueryHeadersString = colNamesStr.toString();
	}
	
	private static void buildHeadersStringForUpdateQuery() {
		// Diese Methode baut einen String für eine Update Query auf --> ein existierender User wird per ID gesucht und dann seine Daten aktualisiert
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE prospects SET ");
		for (int i = 1; i < colCount; i++) {
			builder.append(String.format("%s = ?, ", TableHeaders.getDBColNameByColIndex(i)));
		}
		
		builder.setLength(builder.length() - 2);
		builder.append(" WHERE " + TableHeaders.getDBColNameByColIndex(0) + " = ?");
		updateQueryHeadersString = builder.toString();
	}
	
}
