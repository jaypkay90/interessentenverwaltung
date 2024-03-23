import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;

public class TableHeaders {
	private static HashMap<String, String> headers = new HashMap<>();
	
	public static void buildMap() {
		headers.put("ID", "ID");
		headers.put("Priorität", "Prioritaet");
		headers.put("Vorname", "Vorname");
		headers.put("Nachname", "Nachname");
		headers.put("Firma", "Firma");
		headers.put("Branche", "Branche");
		headers.put("Abteilung", "Abteilung");
		headers.put("E-Mail", "E_Mail");
		headers.put("Telefon", "Telefon");
		headers.put("Bevorzugter Social Media Kanal", "Social_Media");
		headers.put("Sprache", "Sprache");
		headers.put("Land", "Land");
		headers.put("Bundesland", "Bundesland");
		headers.put("PLZ", "PLZ");
		headers.put("Stadt", "Stadt");
		headers.put("Hausnummer", "Hausnummer");
		headers.put("Interesse an", "Interesse_an");
		headers.put("Erinnerung", "Erinnerung");
	}

	public static int getColCount() {
		return headers.size();
	}
	
	public static String getJTableColName(String dbColName) {
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			if (entry.getValue().equals(dbColName)) {
				return entry.getKey();
			}
		}
		return null;
	}
	
}
