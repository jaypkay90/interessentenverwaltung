import javax.swing.JOptionPane;
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
		FileIO.openWriter("Interessenten.csv");
		
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
			
			String currentCellValueStr;
			if (currentCellValue == null) {
				currentCellValueStr = "";							
			}
			else {
				currentCellValueStr = String.valueOf(currentCellValue);				
			}
			
			// Alle Werte werden durch Semikolon getrennt. Ausnahme: Nach dem letzten Wert folgt ein Zeilenumbruch
			if (col != colCount - 1) {
				FileIO.printf("%s;", currentCellValueStr);				
			}
			else {
				FileIO.printf("%s\n", currentCellValueStr);
			}
		}
	}
	
}
