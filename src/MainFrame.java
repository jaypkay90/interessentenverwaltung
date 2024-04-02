import java.sql.*;
import javax.swing.table.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import java.awt.Panel;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.ScrollPaneConstants;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JTextField;
import javax.swing.RowFilter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Color;

public class MainFrame extends JFrame implements KeyListener, ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JFrame frame;
	private JPanel contentPane;
	private JTable table;
	private DefaultTableModel model;
	private String[] colNames = TableHeaders.getJTableHeaders();
	private Map<String, JTextField> textFieldMap;
	private TableRowSorter<DefaultTableModel> myTableRowSorter;
	private List<String> reminderPopupMessages;
	
	
	public MainFrame() {
		// Mit Database verbinden
		Database.connectToDatabase();
		
		// Frame erstellen, Icon hinzufügen und andere Grundeigenschaften festlegen
		frame = new JFrame();
		ImageIcon icon = new ImageIcon("MainIcon.png");
		frame.setIconImage(icon.getImage());
		frame.setTitle("Interessentenverwaltung");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Frame beim Starten der App maximiert anzeigen (BOTH: Maximierte Höhe und Breite in Abhängigkeit von der Bildschirmgröße)
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		// Initiale Größe des Frames beim Minimieren
		frame.setBounds(100, 100, 800, 500);
		
		// Panel für den Content erstellen
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		frame.setContentPane(contentPane);

		// Menü erstellen und zur North-Region hinzufügen
		JMenuBar menuBar = setupMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);
		
		// Panel zum Anzeigen der Daten aus der DB zur Center-Region hinzufügen
		JPanel centerPanel = setupCenterPanel();
		contentPane.add(centerPanel, BorderLayout.CENTER);
		
		// Panel für Suche und Filterung der Daten zur East-Region hinzufügen
		JScrollPane scrollSearchPane = setupSearchPanel();
        contentPane.add(scrollSearchPane, BorderLayout.EAST);
        
        // Frame sichtbar machen
		frame.setVisible(true);
		
		// Popup(s) für Erinnerungen anzeigen
		showReminderPopups();
	}
	
	private JScrollPane setupSearchPanel() {
        JPanel searchPanel = new JPanel(new GridLayout(0, 1, 10, 10)); // keine Reihen, 1 Spalte
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // 10 px "Padding" an allen Seiten
        searchPanel.add(new JLabel("Suche", SwingConstants.CENTER)); // Text "Suche" wird ganz oben im Panel mittig zentriert angezeigt
        
        // Die Textfelder und der Name des Feldes werden über eine HashMap "verbunden". So kann festgestellt werden, in welchen Tabellenspalten gefiltert werden soll.
        textFieldMap = new HashMap<>();
        
        for (String colName : colNames) {
        	// In jedes Item-Panel kommt eine Überschrift (diese entspricht je einer Spaltenüberschrift des JTables) und ein dazugehöriges Suchfeld
        	JPanel itemPanel = new JPanel(new GridLayout(0, 1, 5, 5)); // keine Reihen, 1 Spalte --> Das Label und das Textfeld werden untereinander in einer Spalte angezeigt
            JLabel label = new JLabel(colName);
            JTextField textField = new JTextField(15); // Breite des Textfeldes: 15px
            textField.addKeyListener(this);
            
            // Label mit Überschrift und Textfeld zum Panel hinzufügen
            itemPanel.add(label);
            itemPanel.add(textField);
            
            // Panel mit Überschrift und Textfeld zum SearchPanel hinzufügen
            searchPanel.add(itemPanel);
            
            // Spaltenname (Key) und dazugehöriges Textfeld (Value) zur Hashmap hinzufügen
            textFieldMap.put(colName, textField);
        }
        
        // Scrollbar zum SearchPanel hinzufügen
        JScrollPane scrollSearchPane = new JScrollPane(searchPanel);
        scrollSearchPane.setPreferredSize(new Dimension(400, 0));
        
        return scrollSearchPane;
	}
	
	private JMenuBar setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		// Datei-Menü erstellen und zur Menübar hinzufügen
		JMenu fileMenu = new JMenu("Datei");
		fileMenu.add(createMenuItem("Schließen", "exit"));
		fileMenu.add(createMenuItem("Benutzer hinzufügen", "addNewUser"));
		fileMenu.add(createMenuItem("Auwahl als CSV exportieren", "exportCSV"));
		fileMenu.add(createMenuItem("Interessenten aus CSV importieren", "importCSV"));
		menuBar.add(fileMenu);
		
		// HilfeMenü erstellen und zur Menübar hinzufügen
		JMenu helpMenu = new JMenu("Hilfe");
		menuBar.add(helpMenu);
		
		return menuBar;
	}
	
	private JMenuItem createMenuItem(String label, String command) {
		// Fügt einem Menuitem einen Anzeigetext, einen ActionCommand und einen ActionListener hinzu
		JMenuItem item = new JMenuItem(label);
		item.setActionCommand(command);
		item.addActionListener(this);
		return item;
	}
	
	private JPanel setupCenterPanel() {
		// Im CenterPanel werden die Daten aus der Datenbank in einem JTable angezeigt
		
		// Daten aus der Datenbank auslesen und in einem TableModel speichern
		getTableData();
		
		// JTable mit dem zuvor erstellten Tabellenmodell initialisieren
		table = new JTable(model);
		
		// MouseListener zum JTable hinzufügen
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// Nur bei einem Linksklick soll sich ein PopUp-Fenster öffnen, in dem die Interessentendaten aus der Reihe, auf die geklickt wurde, angezeigt werden
				if (e.getButton() != MouseEvent.BUTTON1) { 
		            return;
		        }
				
				// table.getSelectedRow() liest den viewIndex der Reihe aus, auf die geklickt wurde. Im viewIndex sind die Daten so sortiert, wie sie initial zum JTable hinzugefügt wurden
				// Bei Sortierung oder Filterung ändert sich der viewIndex nicht, sondern nur der index im Tabellenmodell. Daher muss der der viewIndex hier in den model Index konvertiert werden
				int selectedRow = table.convertRowIndexToModel(table.getSelectedRow());
				
				// Daten aus der selektierten Reihe auslesen und in einer HashMap abspeichern
				// Struktur der Map --> Key: Spaltenüberschrift, Value: Text in der Zelle der entsprechenden Spalte innerhalb der angeklickten Reihe
				HashMap<String, String> rowData = new HashMap<>();
				for (int i = 0; i < colNames.length; i++) {
					Object tableCellValue = model.getValueAt(selectedRow, i);
					String cellValueToString = (tableCellValue != null) ? tableCellValue.toString() : "";
					rowData.put(colNames[i],cellValueToString);
				}
				
				// ProspectsPopUp mit Interessentendaten anzeigen
				new ProspectsPopUpFrame(rowData, selectedRow);
			}
		});
		
		
		// Scrollbar und TableRowSorter zum JTable hinzufügen
		JScrollPane scrollPane = new JScrollPane(table);
		table.setRowSorter(myTableRowSorter);
		
		// JPanel erstellen und JScrollPane mit JTable hinzufügen
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		centerPanel.add(scrollPane);
		
		return centerPanel;
	}
	
	private void showReminderPopups() {
		for (String message : reminderPopupMessages) {
			JOptionPane.showMessageDialog(null, message, "Erinnerung", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	private void getTableData() {
		// TableModel aus der MyTableModel Klasse bekommen
		model = MyTableModel.getModel();
		
		// Überschriften zum TableModel hinzufügen
		model.setColumnIdentifiers(TableHeaders.getJTableHeaders());
		
		// ArrayList mit Reminder-Nachrichten --> Die Nachrichten, die in dieser Liste gespeichert werden, werden nach dem Start des Programms in JOptionPanes angezeigt
		// In diese Liste kommen kommen Strings mit Reminder-Nachrichten für alle Interessenten, bei denen für das aktuelle Datum eine Erinnerung gesetzt wurde
		reminderPopupMessages = new ArrayList<>();
					
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
            if (date1 == "") {
            	// Sind die Spalteneinträge für "Erinnerung" in beiden Reihen leer? --> Gib 0 zurück (Gleicheit)
            	// Wenn d2 nicht leer ist --> gib -1 zurück --> das "leere" Datum steht in der Tabelle VOR dem validen Datum
                return (date2 == "") ? 0 : -1;
            }
            else if (date2 == "") {
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

        // Set the comparator for the "Date" column in the TableRowSorter
        myTableRowSorter.setComparator(TableHeaders.getJTableColNumByJTableColName("Erinnerung"), dateComparator);
	}
	
	// ActionListener für Buttons
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		switch (command) {
		case "addNewUser":
			// Ein neuer User wird hinzugefügt
			new ProspectsPopUpFrame();
			break;
		case "exportCSV":
			exportSelectionToCSV();
			break;
		case "importCSV":
			CSVImportExport.importCSV();
			break;
		case "exit":
			// App wird komplett geschlossen
			System.exit(0);
			break;
		}
		
	}
	
	// KeyListener
	@Override
	public void keyTyped(KeyEvent e) {
		
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
	
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// Ein KeyListener ist nur für die Such-Textfelder eingerichtet. Jeder Keystroke führt dazu, dass die Tabelle neu gefiltert wird
		// Diese Vorgehensweise ist User-freundlicher, da der User in Echtzeit die Ergebnisse seiner Suche sieht. Allerdings würde die Suche über
		// einen Button, der gedrückt wird, wenn alle Suchkriterien eingegeben wurden, deutlich weniger Rechenleistung erfordern.
		filterTable();
	}
	
	private void filterTable() {
		// In dieser ArrayList werden alle Filterkriterien abgespeichert, die bei der Filterung der Reihen beachtet werden sollen
		// Ein einzelner RowFilter filtert Reihen nach vorgegebenen Kriterien heraus. Wir wollen hier mehrere Filterkriterien auf einmal beachten
		// Daher erstellen wir zunächst eine ArrayListe, in der jedes dieser Kriterien als RowFilter abgespeichert wird. Am Ende können wir die Einzelfilter kombinieren
		// Die Einzelfilter sind in unserem Fall regex-Filter. Das heißt: Wir filtern Reihen mit den Kriterien heraus, dass gewisse Spalten innerhalb der Reihe einen Suchtext enthalten
		ArrayList<RowFilter<TableModel, Object>> colFilters = new ArrayList<>();
		
		// Durch Hashmap mit den JTextfields loopen
		for (Map.Entry<String, JTextField> entry : textFieldMap.entrySet()) {		
			// Überschrift (Key) und Textfeld (value) des akt. Entrys bekmmen --> Die Überschrift entspricht dem dazugehörigen Spaltennamen im JTable
			String colName = entry.getKey();
			JTextField currentField = entry.getValue();
			
			// searchText: Der Text, den der User in das Textfield eingegeben hat
			String searchText = currentField.getText();
			
			// Filter für die enstprechende Spalte in der Tabelle zum Filter-Array hinzufügen, wenn der User Suchtext eingegeben hat
			if (!searchText.equals("")) {
				// mit Hilfe der Überschrift über dem JTextfield können wir die Spaltennummer im JTable finden
				int colNum = TableHeaders.getJTableColNumByJTableColName(colName);
				
				// regex-Filter: Filtert Reihen mit dem Kriterium, dass die Spalte mit der Spaltennummer colNum den Suchtext erhalten
				colFilters.add(RowFilter.regexFilter(currentField.getText(), colNum));
			}
		}
		
		// An dieser Stelle haben wir jetzt alle Einzelfilter im Array abgespeichert.
		// Damit alle Filterkriterien GEMEINSAM bei der Filterung der Reihen beachtet werden, wird daraus nun ein kombinierter Filter erstellt
		RowFilter<TableModel, Object> compoundFilter = RowFilter.andFilter(colFilters);
		
		// Kombinierten Filter zum RowSorter hinzufügen
		myTableRowSorter.setRowFilter(compoundFilter);
	}
	
	private void exportSelectionToCSV() {
		int rowCount = table.getRowCount();
		
		// Indizes aller sichtbaren Reihen in Array speichern
		int[] visibleRows = new int[0];
		
		int visibleRowCount = 0;
		for (int i = 0; i < rowCount; i++) {
			// Checken, ob akt. Reihe sichtbar ist, -1 wird zurückgegeben, wenn die Reihe herausgefiltert wurde
			int currentRowInModel = myTableRowSorter.convertRowIndexToModel(i);
			if (currentRowInModel != -1) {
				visibleRowCount++;
				
				// akt. Reihe zum Array hinzufügen
				int[] tmpArray = Arrays.copyOf(visibleRows, visibleRowCount);
				visibleRows = tmpArray;
				visibleRows[visibleRowCount - 1] = currentRowInModel;
			}
		}
		
		// Auswahl als CSV exportieren
		CSVImportExport.exportCSV(visibleRows);
	}
		
	private void importCSV() {
		// Checken, ob File valide
		// Korrekte Spaltenüberschriften, korrekte Datentypen
		
	}
}
