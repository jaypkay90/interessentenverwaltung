import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

public class CSVImportExport {
	private static DefaultTableModel model;
	private static int colCount;
	
	static {
		model = MyTableModel.getModel();
		colCount = TableHeaders.getColCount();
	}
	
	public static void exportCSV(int[] selectedRows) {
		int rowCount = selectedRows.length;
		String filePath = openFileChooser("export");
		
		// Wenn User keine Datei ausgewählt hat: return!
		if (filePath == "") {
			return;
		}
		
		FileIO.openWriter(filePath);
		
		// Da die Überschrift der ersten Spalte "ID" ist, weist Excel der entstehenden Datei automatisch den Typ SYLK zu. Die folgenden Zeichen helfen Excel, den CSV-Dateityp richtig zu identifizieren
		// "If \uFEFF appears at the beginning of a file, it typically indicates that the file is encoded in UTF-8"
		FileIO.print('\uFEFF');
		
		// Überschriften in CSV Datei schreiben
		printHeadersToCSV();
		
		// Selecktierte Reihen in CSV Datei schreiben
		for (int row = 0; row < rowCount; row++) {
			printRowToCSV(selectedRows[row]);
		}
		
		// Nach dem Export: Infomessage anzeigen
		JOptionPane.showMessageDialog(null, "CSV Export erfolgreich!", "Information", JOptionPane.INFORMATION_MESSAGE);
		
		FileIO.closeWriter();
	}
	
	private static void printHeadersToCSV() {
		// Druckt die Spaltenüberschriften in die erste Reihe der CSV Datei
		for (int col = 0; col < colCount; col++) {
			// Die Überschrift der aktuellen Spalte bekommen
			String currentHeader = TableHeaders.getJTableColNameByColIndex(col);
			
			// Alle Werte werden durch Semikolon getrennt. Ausnahme: Nach dem letzten Wert folgt ein Zeilenumbruch
			if (col != colCount - 1) {
				FileIO.printf("%s;", currentHeader);
			}
			else {
				FileIO.printf("%s\n", currentHeader);
			}
		}
	}
	
	private static void printRowToCSV(int row) {
		// Druckt die aktuelle Reihe in die CSV Datei
		for (int col = 0; col < colCount; col++) {
			// Wert in der aktuellen Zelle bekommen und in String umwandeln
			Object currentCellValue = model.getValueAt(row, col);
			String currentCellValueStr = String.valueOf(currentCellValue);
			System.out.println(currentCellValueStr);
			
			// Alle Werte werden durch Semikolon getrennt. Ausnahme: Nach dem letzten Wert folgt ein Zeilenumbruch
			if (col != colCount - 1) {
				FileIO.printf("%s;", currentCellValueStr);				
			}
			else {
				FileIO.printf("%s\n", currentCellValueStr);
			}
		}
	}
	
	public static void importCSV() {
		String filePath = openFileChooser("import");
		
		// Wenn User keine Datei ausgewählt hat: return!
		if (filePath == "") {
			return;
		}
		
		/*String filename = "Interessenten-Test.csv";
		// Dateiformat checken
		if (!(filename.toLowerCase().endsWith(".csv"))) {
			// Datei hat ein falsches Dateiformat
			JOptionPane.showMessageDialog(null, "Ungültiges Dateiformat! Bitte CSV-Datei auswählen.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}*/
		
		// Datei öffnen
		//FileIO.openReader(filename);
		FileIO.openReader(filePath);
		
		// Überschriften checken
		String[] headers = new String[TableHeaders.getColCount()];
		String headersStr = FileIO.nextLine().strip();
		if (headersStr.charAt(0) == '\uFEFF') {
			headersStr = headersStr.substring(1);
		}
		
		int strlen = headersStr.length();
		
		// Checken ob 18 Überschriften (Der JTable und die Datenbank haben 18 Überschriften)
		int headerNum = 0;
		String currentHeader = String.valueOf(headersStr.charAt(0));
		for (int i = 1; i < strlen; i++) {
			char currentChar = headersStr.charAt(i);
			if (currentChar == ';') {
				headerNum++;
				headers[headerNum - 1] = currentHeader;
				currentHeader = "";
			}
			else if (i == strlen - 1) {
				headerNum++;
				currentHeader = currentHeader.concat(String.valueOf(currentChar));
				headers[headerNum - 1] = currentHeader;
				break;
			}
			else {
				currentHeader = currentHeader.concat(String.valueOf(currentChar));
			}
		}
		
		System.out.println(headerNum);
		
		// Ist die Anzahl an Überschriften korrekt? --> Wenn nicht: Fehlermeldung
		if (headerNum != headers.length) {
			JOptionPane.showMessageDialog(null, "Inkompatible Datei. Es können nur Daten aus CSV-Dateien importiert werden, die mit diesem Programm erzeugt wurden.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Checken, ob die Headers aus der CSV mit den Headern im JTable übereinstimmen --> Wenn nicht: Fehlermeldung
		for (int i = 0; i < headerNum; i++) {
			if (!(headers[i].equals(TableHeaders.getJTableColNameByColIndex(i)))) {
				// Sobald ein Header nicht übereinstimmt, ist die Datei nicht kompatibel
				JOptionPane.showMessageDialog(null, "Inkompatible Datei. Es können nur Daten aus CSV-Dateien importiert werden, die mit diesem Programm erzeugt wurden.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		
		// Die Überschriften sind kompatibel. Jetzt können wir versuchen, die Daten in die Datenbank zu übertragen
		int errorCounter = 0;
		String[] rowValues = new String[TableHeaders.getColCount()];
		while (FileIO.hasNext()) {
			boolean error = false;
			String currentLine = FileIO.nextLine();
			int len = currentLine.length();
					
			int colNum = 0;
			String currentValue = String.valueOf(currentLine.charAt(0));
			for (int i = 1; i < len; i++) {
				char currentChar = currentLine.charAt(i);
				if (currentChar == ';') {
					// 2 Semikolons hintereinander...
					if (i < len - 1 && currentLine.charAt(i + 1) == ';') {
						colNum++;
						rowValues[colNum - 1] = "";
						currentValue = "";
					}
					else {
						colNum++;
						rowValues[colNum - 1] = currentValue;
						currentValue = "";						
					}
				}
				
				// Am Ende der Reihe
				else if (i == len - 1) {
					colNum++;
					currentValue = currentValue.concat(String.valueOf(currentChar));
					rowValues[colNum - 1] = currentValue;
					break;
				}
				else {
					currentValue = currentValue.concat(String.valueOf(currentChar));
				}
			}
			
			// Erinnerungsspalte ist leer
			if (colNum == TableHeaders.getColCount() - 1) {
				rowValues[TableHeaders.getColCount() - 1] = ""; 
			}
			
			// Reihe überprüfen --> Wenn error true ist: Nicht anzeigen
			// Der ID-Wert wird übersprungen und nicht in die Datenbank geschrieben. Daher müssen wir den Inhalt nicht prüfen
			// "Priorität" und "Datum" müssen richtig formatiert sein
			try {
				int priorityInt = Integer.parseInt(rowValues[TableHeaders.getJTableColNumByJTableColName("Priorität")]);
				if (priorityInt < 1 || priorityInt > 5) {
					throw new NumberFormatException();
				}
			}
			catch (NumberFormatException e)  {
				// Am Ende ErrorMessage anzeigen
				error = true;
				errorCounter++;
			}
			
			// Erinnerungsspalte prüfen
			// Wenn eine Erinnerung gesetzt wurde, muss sie ein Zeitformat haben.
			if (!error) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
				String dateString = rowValues[TableHeaders.getJTableColNumByJTableColName("Erinnerung")];
				if (!dateString.equals("")) {
					try {
						if (dateString.length() != 10) {
							throw new Exception();
						}
						Date date = dateFormat.parse(dateString);
						Date currentDate = Calendar.getInstance().getTime();
						
						if (!date.after(currentDate)) {
							error = true;
							errorCounter++;
						}
						
					}
					catch (Exception e) {
						error = true;
						errorCounter++;
					}
				}				
			}
			
			// Inhalt wurde geprüft. Jetzt kann die Zeile zur DB hinzugefügt werden
			if (!error) {
				insertNewProspect(rowValues);
			}
			
		}
		
		// Zum Schluss: Wenn der Error-Counter größer 0 ist, konnten nicht alle Reihen importiert werden --> Errormessage anzeigen
		if (errorCounter > 0) {
			JOptionPane.showMessageDialog(null, "Eine oder mehrere Zeilen konnten nicht importiert werden, da sie inkompatible Datenformate beinhalten.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	// Die GLEICHE METHODE GIBT ES AUCH IN PROSPECTSPOPUP
	private static void insertNewProspect(String[] rowValues) {
		// Neuen Interessenten hinzufügen
		String colNamesStr = TableHeaders.getInsertQueryHeadersString();
		String query = String.format("INSERT INTO prospects (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", colNamesStr);
		updateDatabaseTable(query, rowValues);
		addNewProspectToJTable();
	}
	
	// Die GLEICHE METHODE GIBT ES AUCH IN PROSPECTSPOPUP
	private static void updateDatabaseTable(String query, String[] rowValues) {
		PreparedStatement prep = null;
		try {
			Connection connect = Database.getConnection();
			prep = connect.prepareStatement(query);
			
			prep.setInt(1, Integer.parseInt(rowValues[1]));
			prep.setString(2, rowValues[2]);
			prep.setString(3, rowValues[3]);
			prep.setString(4, rowValues[4]);
			prep.setString(5, rowValues[5]);
			prep.setString(6, rowValues[6]);
			prep.setString(7, rowValues[7]);
			prep.setString(8, rowValues[8]);
			prep.setString(9, rowValues[9]);
			prep.setString(10, rowValues[10]);
			prep.setString(11, rowValues[11]);
			prep.setString(12, rowValues[12]);
			prep.setString(13, rowValues[13]);
			prep.setString(14, rowValues[14]);
			prep.setString(15, rowValues[15]);
			prep.setString(16, rowValues[16]);
			
			// Erinnerungsdatum einfügen
			String dateString = rowValues[17];
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			Date date = null;
			try {
				date = dateFormat.parse(dateString);
			}
			catch (Exception e) {
				
			}
			
			if (date != null) {
				prep.setDate(17, new java.sql.Date(date.getTime()));				
			}
			else {
				prep.setDate(17, null);
			}
			
			prep.executeUpdate();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			Database.closePreparedStatement(prep);
		}
			
	}
	
	// Die GLEICHE METHODE GIBT ES AUCH IN PROSPECTSPOPUP
	private static void addNewProspectToJTable() {
		PreparedStatement prep = null;
		ResultSet rs = null;
		try {
			
			// Die soeben hinzugefügte Zeile in der Datenbank auswählen
			String findInsertQuery = String.format("SELECT * FROM prospects ORDER BY %s DESC LIMIT ?", TableHeaders.getDBColNameByColIndex(0)); // 0: "ID"
			
			Connection connect = Database.getConnection();
			prep = connect.prepareStatement(findInsertQuery);
			prep.setInt(1, 1);
			rs = prep.executeQuery();
			
			// Spaltenanzahl bekommen
			/*ResultSetMetaData rsmetadata = rs.getMetaData();
			int colCount = rsmetadata.getColumnCount();*/
			int colCount = TableHeaders.getColCount();
			String[] row = new String[colCount];
			
			// Daten aus der letzten Tabellenzeile zum Tabellenmodell hinzufügen
			MyTableModel model = MyTableModel.getModel();
			for (int i = 0; i < colCount; i++) {
				// Daten aus akt. ResultSet in String abspeichern und zum Array hinzufügen
		    	String currentRsValue = rs.getString(i + 1);
		    	row[i] = currentRsValue == null ? "" : currentRsValue;
		    	
		    	// CODE DOPPELT VORHANDEN --> AUCH IN MAIN FRAME
		    	if (TableHeaders.getDBColNameByColIndex(i).equals("Erinnerung")) {
		    		if (!row[i].equals("")) {
		    			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		    			dateFormat.setLenient(false);
		    			Date date = new Date(Long.parseLong(currentRsValue));
		    			String dateString = dateFormat.format(date);
		    			row[i] = dateString;
		    		}
		    	}					
					//row[i] = rs.getString(i + 1);
				
			}	
			
			model.addRow(row);
			model.fireTableDataChanged();
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			Database.closeResultSetAndPreparedStatement(rs, prep);;
		}
	}
	
	private static String openFileChooser(String operation) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Dateien (*.csv)", "csv", "CSV");
		chooser.setFileFilter(filter);
		
		String filePath = "Interessenten.csv";
		
		int response;
		if (operation.equals("export")) {
			// Gibt 0 zurück, wenn eine Datei ausgewählt wurde und 1, wenn keine ausgewählt wurde
			response = chooser.showSaveDialog(null);			
		}
		else {
			response = chooser.showOpenDialog(null);
		}
		
		// JFileChooser.APPROVE_OPTION ist 0 --> if (response == 0) --> User hat Datei ausgewählt
		if (response == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			String filename = selectedFile.getName();
			if (!filename.toLowerCase().endsWith(".csv")) {
				// Datei ist keine CSV-Datei --> richtige Dateiendung anhängen
				filePath = selectedFile.getAbsolutePath() + ".csv";
			}
			else {
				filePath = selectedFile.getAbsolutePath();
			}
			
		}
		else if (response == JFileChooser.CANCEL_OPTION) {
			// Wenn der User gecancelt hat: Leerstring zurückgeben
			return "";
		}
		
		if (operation.equals("import")) {
			// Checken ob die ausgewählte Datei überhaupt existiert. Falls nicht: Erstellen!
			 File file = new File(filePath);
			 if (!file.exists()) {		 
				JOptionPane.showMessageDialog(null, "Import fehlgeschlagen! Die angegebene Datei existiert nicht", "Error", JOptionPane.ERROR_MESSAGE);
				return "";
			 }
		}
		
		// Dateipfad zurückgeben
		return filePath;
	}
	
}
