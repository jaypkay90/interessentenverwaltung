import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CSVImportExport {
	// Spaltenanzahl: Entspricht Anzahl von Überschriften im JTable/in der DB
	private static int colCount = TableHeaders.getColCount();
	private static JFrame frame;
	
	public static void exportCSV(JFrame mainFrame, int[] selectedRows) {
		frame = mainFrame;
		
		// selectedRows: int Array mit Indizes der selektierten Reihen im Tabellenmodell, die exportiert werden sollen
		int rowCount = selectedRows.length;
		
		// File Chooser für den Export öffnen
		String filePath = openFileChooser("export");
		
		// Wenn User keine Datei ausgewählt hat: return!
		if (filePath == "") {
			return;
		}
		
		// File zum Schreiben öffnen
		FileIO.openWriter(filePath);
		
		// Da die Überschrift der ersten Spalte "ID" ist, weist Excel der entstehenden Datei automatisch den Typ SYLK zu.
		// Um den CSV-Dateityp richtig zu identifizieren, wird daher an den Anfang ein BOM-Character gesetzt
		// "If \uFEFF appears at the beginning of a file, it typically indicates that the file is encoded in UTF-8"
		FileIO.print('\uFEFF');
		
		// Überschriften in CSV Datei schreiben
		printHeadersToCSV();
		
		// Selecktierte Reihen in CSV Datei schreiben
		for (int row = 0; row < rowCount; row++) {
			printRowToCSV(selectedRows[row]);
		}
		
		// Nach dem Export: Infomessage anzeigen
		JOptionPane.showMessageDialog(frame, "CSV Export erfolgreich!", "Information", JOptionPane.INFORMATION_MESSAGE);
		
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
			MyTableModel model = MyTableModel.getModel();
			
			// Wert in der aktuellen Zelle bekommen und in String umwandeln
			Object currentCellValue = model.getValueAt(row, col);
			String currentCellValueStr = String.valueOf(currentCellValue);
			
			// Alle Werte werden durch Semikolon getrennt. Ausnahme: Nach dem letzten Wert folgt ein Zeilenumbruch
			if (col != colCount - 1) {
				FileIO.printf("%s;", currentCellValueStr);				
			}
			else {
				FileIO.printf("%s\n", currentCellValueStr);
			}
		}
	}
	
	public static void importCSV(JFrame mainFrame) {
		frame = mainFrame;
		
		// File Chooser für den Import öffnen
		String filePath = openFileChooser("import");
		
		// Wenn User keine Datei ausgewählt hat: return!
		if (filePath == "") {
			return;
		}
		
		// Datei zum Lesen öffnen
		FileIO.openReader(filePath);
		
		// Überschriften checken: Die Überschriften im CSV-File sollten mit den Überschriften im JTable übereinstimmen
		boolean headersOkay = checkHeaders();
		if (!headersOkay) {
			return;
		}
		
		// Die Überschriften sind kompatibel. Jetzt können wir versuchen, die Daten in die Datenbank zu übertragen
		// Wenn showErrorMessage innerhalb der Schleife true wird, wird am Ende des Prozesses eine Fehlermeldung ausgegeben
		// Es konnten nicht alle Zeilen importiert werden
		boolean showErrorMessage = false;
		
		// Solange wir nicht am Ende des Files angekommen sind
		while (FileIO.hasNext()) {
			// Zu Beginn: Reihe aus CSV lesen
			String currentLine = FileIO.nextLine();
			
			// Werte aus den einzelnen Spalten der akt. Zeile in String-Array speichern
			String[] rowValues = getRowValues(currentLine);
			
			// Daten in der Zeile überprüfen --> Wenn checkRowData true zurückgibt, sind die Daten nicht mit der DB kompatibel
			boolean error = checkRowData(rowValues);
			
			// Inhalt wurde geprüft. Wenn checkRowData false zurückgegeben hat, kann die Zeile zur DB hinzugefügt werden
			if (!error) {
				// Wir müssen die ID (Index 0) aus dem Array löschen, damit die Methode funktioniert
				Database.insertNewProspect(Arrays.copyOfRange(rowValues, 1, rowValues.length));
			}
			// Wenn Kompatibilitätsproblem gefunden und dies die erste Zeile ist, bei der ein Problem aufgetreten ist...
			else if (!showErrorMessage) {
				showErrorMessage = true;
			}
			
		}
		
		// Zum Schluss: Wenn showErrorMessage true ist --> Errormessage anzeigen
		if (showErrorMessage) {
			JOptionPane.showMessageDialog(frame, "Eine oder mehrere Zeilen konnten nicht importiert werden, da sie inkompatible Daten enthalten.", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
	}
	
	private static boolean checkRowData(String[] rowValues) {
		// Checken, ob die Daten in der aktuellen Zeile mit der DB kompatibel sind
		// Der ID-Wert wird übersprungen und nicht in die Datenbank geschrieben. Daher müssen wir den Inhalt nicht prüfen
		// Check 1: Hat die Zeile zu viele Spalteneinträge? --> Falls ja, haben wir den ersten Index im rowValues Array auf null gesetzt
		if (rowValues[0] == null) {
			return true;
		}
		
		// Check 2: Priorität muss ein int sein und zwischen 1 und 5 liegen
		try {
			int priorityInt = Integer.parseInt(rowValues[TableHeaders.getJTableColNumByJTableColName("Priorität")]);
			if (priorityInt < 1 || priorityInt > 5) {
				throw new NumberFormatException();
			}
		}
		catch (NumberFormatException e)  {
			// Check nicht bestanden!
			return true;
		}
		
		// Check 3: Erinnerungsspalte prüfen
		// Wenn eine Erinnerung gesetzt wurde, muss sie in ein Zeitformat konvertierbar sein
		// Das Erinnerungsdatum muss hier nicht nach dem akt. Datum liegen
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		String dateString = rowValues[TableHeaders.getJTableColNumByJTableColName("Erinnerung")];
		if (!dateString.equals("")) {
			try {
				if (dateString.length() != 10) {
					throw new Exception();
				}
				
				dateFormat.parse(dateString);
			}
			catch (Exception e) {
				return true;
			}
		}
		
		// Alle Checks bestanden --> Zeile kann zur DB hinzugefügt werden
		return false;
	}
	
	private static String[] storeRowDataInArray(String currentLine) {
		/* Speichert die Werte der einzelnen Spalten aus der akt. Zeile im CSV-File in einem String-Array */
		
		// Größe des Arrays: Anzahl von Überschriften im JTable --> entspricht Spaltenanzahl
		String[] rowValues = new String[TableHeaders.getColCount()];
		int len = currentLine.length();
		
		// colOverflow: Wird innerhalb der Schleife true, wenn unsere Zeile zu viele Spalten hat --> zu wenig Platz im Array
		boolean colOverflow = false;
		
		int colNum = 0;
		String currentValue = String.valueOf(currentLine.charAt(0));
		for (int i = 1; i < len; i++) {
			char currentChar = currentLine.charAt(i);
			
			// Die akt. Spaltennummer ist gleich der Länge des Arrays --> Es ist kein Platz mehr im Array! --> Es gibt zu viele Spalten in der Zeile!
			// Zeile kann nicht in die DB eingefügt werden
			if (colNum == rowValues.length) {
				colOverflow = true;
				break;
			}
			
			// Wenn akt. Char ein Semikolon ist: "Spaltenstring" der akt. Spalte zum Array hinzufügen und neuen String für die nächste Spalte beginnen
			else if (currentChar == ';') {
				rowValues[colNum] = currentValue;
				currentValue = "";						
				colNum++;
			}
			
			// Am Ende der Zeile: Letzten Char zum akt. "Spaltenstring" hinzufügen und String ins Array packen
			// Dieser Pfad wird nur ausgeführt, wenn der letzte Char kein Semikolon ist
			// Wenn der letzte Char der Zeile ein Semikolon ist, ist die letzte Spalte der Zeile leer
			else if (i == len - 1) {
				currentValue = currentValue.concat(String.valueOf(currentChar));
				rowValues[colNum] = currentValue;
				colNum++;
			}
			
			// Wenn akt. Char kein Semikolon und nicht letzter Char der Zeile: Akt. char zum akt. "Spaltenstring" hinzufügen
			else {
				currentValue = currentValue.concat(String.valueOf(currentChar));
			}
			
		}
		
		// Hatte die Zeile zu viele Spalten? --> Setze den ersten Platz im Array auf null!
		if (colOverflow) {
			rowValues[0] = null;
		}
		
		return rowValues;
	}
	
	private static String[] getRowValues(String currentLine) {
		// rowValues: String-Array mit allen "Werten", die in der aktuellen Reihe der CSV-Datei stehen
		String[] rowValues = storeRowDataInArray(currentLine);
		
		// Wenn der erste Platz im Array null ist, ist die Zeile inkompatibel --> Wir wollen das Array aber so zurückgeben, wie es ist
		// Wenn noch kein String im letzten Arrayplatz für die akt. Zeile steht, ist die letzte Spalte leer --> Leerstring ins Array packen
		if (rowValues[0] != null && rowValues[rowValues.length - 1] == null) {
			rowValues[rowValues.length - 1] = ""; 
		}
		
		return rowValues;
	}
	
	private static boolean checkHeaders() {		
		// Überschriften stehen in der ersten Zeile der Datei. Wenn BOM Charakter (Byte Order Mark) vorhanden --> herausschneiden
		String headersStr = FileIO.nextLine().strip();
		if (headersStr.charAt(0) == '\uFEFF') {
			headersStr = headersStr.substring(1);
		}
		
		// Überschriften aus CSV-Datei in String-Array speichern
		String[] headers = storeRowDataInArray(headersStr);
		
		// Error-Message
		String message = "Inkompatible Datei. Es können nur Daten aus CSV-Dateien importiert werden, die mit diesem Programm erzeugt wurden.";
		
		// Es müssen genausoviele Überschriften in der CSV-Datei stehen, wie Platz im Array ist
		// Wenn der erste Platz im Array null ist, sind zu viele Überschriften vorhanden --> Wir haben willentlich den ersten Index im Array auf null gesetzt
		// Wenn der letzte Platz im Array null ist, ist die Anzahl an Überschriften zu klein
		if (headers[0] == null || headers[headers.length - 1] == null) {
			JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// Checken, ob die Headers aus der CSV mit den Headern im JTable übereinstimmen --> Wenn nicht: Fehlermeldung
		for (int i = 0; i < headers.length; i++) {
			if (!(headers[i].equals(TableHeaders.getJTableColNameByColIndex(i)))) {
				// Sobald ein Header nicht übereinstimmt, ist die Datei nicht kompatibel
				JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		// Wenn wir an dieser Stelle im Code angekommen sind, sind die Überschriften aus dem CSV-File und die Überschriften im JTable identisch 
		return true;
	}
	
	private static String openFileChooser(String operation) {
		/* JFileChooser öffnen, damit der User eine Datei auswählen kann */
		
		JFileChooser chooser = new JFileChooser();
		
		// Nur CSV-Dateien können ausgewählt werden
		FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Dateien (*.csv)", "csv", "CSV");
		chooser.setFileFilter(filter);
		
		String filePath = "";		
		int response;
		// operation: Ist entweder "export" oder "import"
		if (operation.equals("export")) {
			// operation ist "export" --> Speichermenü öffnen
			// chooser.showSaveDialog: Gibt 0 zurück, wenn eine Datei ausgewählt wurde und 1, wenn keine ausgewählt wurde
			response = chooser.showSaveDialog(frame);			
		}
		else {
			// operation ist "import" --> Öffnen-Menü öffnen
			response = chooser.showOpenDialog(frame);
		}
		
		// JFileChooser.APPROVE_OPTION ist gleich 0 --> if (response == 0) --> User hat Datei ausgewählt
		if (response == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			String filename = selectedFile.getName();
			
			// Checken, ob ausgewählte Datei csv-Datei ist
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
		
		// Wenn der User importieren will: Es kann nur eine Datei importiert werden, die exisitiert
		if (operation.equals("import")) {
			// Checken ob die ausgewählte Datei überhaupt existiert. Falls nicht: Error anzeigen und Leerstring zurückgeben
			 File file = new File(filePath);
			 if (!file.exists()) {		 
				JOptionPane.showMessageDialog(frame, "Import fehlgeschlagen! Die angegebene Datei existiert nicht", "Error", JOptionPane.ERROR_MESSAGE);
				return "";
			 }
		}
		
		// Dateipfad zurückgeben
		return filePath;
	}
	
}
