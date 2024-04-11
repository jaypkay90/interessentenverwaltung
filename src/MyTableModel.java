import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class MyTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;
	private static MyTableModel model;
	private static TableRowSorter<DefaultTableModel> myTableRowSorter;

    // Statische Initialisierung, sobald das Programm startet
    static {
        model = new MyTableModel();
    }

    // Privater Konstruktor, der nur innerhalb dieser Klasse aufgerufen werden kann
    private MyTableModel() {
    	new DefaultTableModel();
    }

    // Das Tabellenmodell kann von jeder anderen Klasse abgerufen werden
    public static MyTableModel getModel() {
        return model;
    }
    
    public static TableRowSorter<DefaultTableModel> getMyTableRowSorter() {
    	return myTableRowSorter;
    }
    
    public static List<String> getTableData() {
		// TableModel aus der MyTableModel Klasse bekommen
		model = MyTableModel.getModel();
		
		// Überschriften zum TableModel hinzufügen
		model.setColumnIdentifiers(TableHeaders.getJTableHeaders());
		
		// ArrayList mit Reminder-Nachrichten --> Die Nachrichten, die in dieser Liste gespeichert werden, werden nach dem Start des Programms in JOptionPanes angezeigt
		// In diese Liste kommen kommen Strings mit Reminder-Nachrichten für alle Interessenten, bei denen für das aktuelle Datum eine Erinnerung gesetzt wurde
		List<String> reminderPopupMessages = new ArrayList<>();
					
		// Daten aus der DB auslesen und zum TableModel hinzufügen		
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = Database.createStatement();
			rs = statement.executeQuery("SELECT * FROM prospects");
			
			// Spaltenanzahl entpricht der Anzahl von Spaltenüberschriften
			int colCount = TableHeaders.getColCount();
			
			// Daten aus der Datenbank auslesen, solange es noch welche gibt...
			while (rs.next()) {
				
				// Für jedes ResultSet: String Array erstellen
			    String[] row = new String[colCount];
			    for (int i = 0; i < colCount; i++) {
			    	
			    	// Daten aus akt. ResultSet in String abspeichern und zum Array hinzufügen
			    	String currentRsValue = rs.getString(i + 1);
			    	row[i] = currentRsValue == null ? "" : currentRsValue;
			    	
			    	// Daten für die Spalte "Erinnerung" für die Anzeige in der Tabelle formatieren und zum Array hinzufügen
			    	if (TableHeaders.getDBColNameByColIndex(i).equals("Erinnerung")) {
			    		if (!row[i].equals("")) {
			    			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			    			//dateFormat.setLenient(false);
			    			Date date = new Date(Long.parseLong(currentRsValue));
			    			String dateString = dateFormat.format(date);
			    			row[i] = dateString;
			    			
			    			// Checken ob die Erinnerung für das heutige Datum gesetzt ist. Falls ja: PopUp Message anzeigen.
			    			DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
			    			LocalDate parsedDate = LocalDate.parse(dateString, dateFormatter);
			    			LocalDate currentDate = LocalDate.now();
			    			//LocalDate currentDate = LocalDate.now().plusDays(1);
			    			
			    			if (currentDate.equals(parsedDate)) {
			    				int idCol = TableHeaders.getJTableColNumByJTableColName("ID");
			    				int vornameCol = TableHeaders.getJTableColNumByJTableColName("Vorname");
			    				int nachnameCol = TableHeaders.getJTableColNumByJTableColName("Nachname");
			    				String message = String.format("Erinnerung für heute gesetzt! Interessenten-ID: %s, Vorname: %s, Nachname: %s", row[idCol], row[vornameCol], row[nachnameCol]);			
			    				reminderPopupMessages.add(message);
			    			}   			
			    		}
			    	}
			    }
			    
			    // Daten aus dem akt. ResultSet (Reihe) zum Tabellenmodell hinzufügen
			    model.addRow(row);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			Database.closeResultSetAndStatement(rs);
		}
		
		// RowSorter setzen
		setRowSorter();
		
		// PopUpMessages zurückgeben
        return reminderPopupMessages;
	}
    
    private static void setRowSorter() {
    	// RowSorter mit Hilfe des Tabellenmodells erstellen
		myTableRowSorter = new TableRowSorter<>(model);
		
		// Die Spalten Priorität und ID sollen beim Sortieren Integers vergleichen, keine Strings. Nur so werden die Zahlen richtig sortiert
		// Bei den Spalten "PLZ" und "Hausnummer" habe ich die Textsortierung beibehalten, weil die Hausnummer auch andere Zeichen als Ziffern enthalten kann.
		// Dies gilt in einigen Ländern auch für die PLZ
		myTableRowSorter.setComparator(TableHeaders.getJTableColNumByJTableColName("ID"), Comparator.comparingInt(o ->  Integer.parseInt(o.toString())));
		myTableRowSorter.setComparator(TableHeaders.getJTableColNumByJTableColName("Priorität"), Comparator.comparingInt(o ->  Integer.parseInt(o.toString())));
		
		// Zur Spalte "Erinnerung" wird ein Comparator gesetzt, der die Daten der Erinnerungen vergleicht und die Spalteneinträge entsprechend sortiert
        Comparator<String> dateComparator = (date1, date2) -> {
        	// Leerstrings handeln --> Sie sollen in der Tabelle ganz oben/unten erscheinen
            if (date1.equals("")) {
            	// Sind die Spalteneinträge für "Erinnerung" in beiden Reihen leer? --> Gib 0 zurück (Gleicheit)
            	// Wenn d2 nicht leer ist --> gib -1 zurück --> das "leere" Datum steht in der Tabelle VOR dem validen Datum
                return (date2.equals("")) ? 0 : -1;
            }
            else if (date2.equals("")) {
            	// Datum 2 ist ein Leerstring, Datum 1 nicht --> gib 1 zurück --> das "leere" Datum (hier Datum 2) steht in der Tabelle VOR dem "validen" Datum (hier d1)
                return 1;
            }
        	
        	try {
                // Strings in Datum-Datentyp konvertieren
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                java.util.Date d1 = dateFormat.parse(date1);
                java.util.Date d2 = dateFormat.parse(date2);
                
                // Daten vergleichen und Ergebnis zurückgeben, wenn d1 vor d2 im Kalender ist, ist der Rückgabewert negativ
                return d1.compareTo(d2);
            } catch (ParseException e) {
            	e.printStackTrace();
            	return 0;
            }
        };

        // Comparator für Erinnerungsspalte im TableRowSorter setzen
        myTableRowSorter.setComparator(TableHeaders.getJTableColNumByJTableColName("Erinnerung"), dateComparator);
    }
    
    public static void updateExistingProspectInJTable(int selectedRow, int userID) {
		PreparedStatement prep = null;
		ResultSet rs = null;
		try {
			
			// Geänderte Zeile in der Datenbank auswählen
			String findInsertQuery = String.format("SELECT * FROM prospects WHERE %s = ?", TableHeaders.getDBColNameByColIndex(0)); // 0: "ID"
			
			Connection connect = Database.getConnection();
			prep = connect.prepareStatement(findInsertQuery);
			
			//prep.setInt(1, Integer.parseInt(userData.get(TableHeaders.getDBColNameByColIndex(0)).getText()));
			prep.setInt(1, userID);
			rs = prep.executeQuery();
			
			// Spaltenanzahl bekommen --> entspricht der Anzahl von Überschriften
			int colCount = TableHeaders.getColCount();
			
			// Zeile im JTable mit den neu eingegebenen Daten updaten
			MyTableModel model = MyTableModel.getModel();
			
			
			for (int i = 0; i < colCount; i++) {
				
				if (TableHeaders.getDBColNameByColIndex(i).equals("Erinnerung") && rs.getString(i + 1) != null) {
		    			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		    			//dateFormat.setLenient(false);
		    			Date date = new Date(Long.parseLong(rs.getString(i + 1)));
		    			String dateString = dateFormat.format(date);
		    			model.setValueAt(dateString, selectedRow, i);
		    		}
				else {			
					model.setValueAt(rs.getString(i + 1), selectedRow, i);
				}
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			Database.closeResultSetAndPreparedStatement(rs, prep);
		}
	}
	
	
	public static void addNewProspectToJTable() {
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
	
	public static void deleteRow(int selectedRow) {
		model.removeRow(selectedRow);
		model.fireTableDataChanged();
	}

}
