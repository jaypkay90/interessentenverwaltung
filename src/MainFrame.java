import javax.swing.table.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import java.awt.BorderLayout;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame implements KeyListener, ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JFrame frame;
	private JTable table;
	private String[] colNames = TableHeaders.getJTableHeaders();;
	private Map<String, JTextField> textFieldMap;
	private List<String> reminderPopupMessages;
	
	
	public MainFrame() {
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
		JPanel contentPane = new JPanel();
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
        
        // Die Textfelder und die Namen der Felder werden über eine HashMap "verbunden". So kann festgestellt werden, in welchen Tabellenspalten gefiltert werden soll.
        textFieldMap = new HashMap<>();
        
        for (String colName : colNames) {
        	// In jedes Item-Panel kommt eine Überschrift (diese entspricht je einer Spaltenüberschrift des JTables) und ein dazugehöriges Suchfeld
        	JPanel itemPanel = new JPanel(new GridLayout(0, 1, 5, 5)); // keine Reihen, 1 Spalte --> Das Label und das Textfeld werden untereinander in einer Spalte angezeigt
            JLabel label = new JLabel(colName); // Label mit der Überschrift als Text
            JTextField textField = new JTextField(15); // Breite des Textfeldes: 15px

            // Jedes Textfeld bekommt einen KeyListener
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
		/* Im CenterPanel werden die Daten aus der Datenbank in einem JTable angezeigt */
		
		// Daten aus der Datenbank auslesen und in einem Tabellenmodell speichern
		// Währendessen wird eine ArrayList mit allen Prospects erstellt, für die für das heutige Datum eine Erinnerung gesetzt ist
		reminderPopupMessages = MyTableModel.getTableData();
		
		// JTable mit dem zuvor erstellten Tabellenmodell initialisieren
		table = new JTable(MyTableModel.getModel());
		
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
					Object tableCellValue = MyTableModel.getModel().getValueAt(selectedRow, i);
					String cellValueToString = (tableCellValue != null) ? tableCellValue.toString() : "";
					rowData.put(colNames[i],cellValueToString);
				}
				
				// ProspectsPopUp mit Interessentendaten anzeigen
				new ProspectsPopUpFrame(frame, rowData, selectedRow);
			}
		});
		
		
		// Scrollbar und TableRowSorter zum JTable hinzufügen
		JScrollPane scrollPane = new JScrollPane(table);
		table.setRowSorter(MyTableModel.getMyTableRowSorter());
		
		// JPanel erstellen und JScrollPane mit JTable hinzufügen
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		centerPanel.add(scrollPane);
		
		return centerPanel;
	}
	
	private void showReminderPopups() {
		// Anzeigen von PopUpNachrichten für alle User, für die für den akt. Tag eine Erinnerung gesetzt wurde
		for (String message : reminderPopupMessages) {
			JOptionPane.showMessageDialog(frame, message, "Erinnerung", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	// ActionListener für Buttons
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		switch (command) {
		case "addNewUser":
			// Ein neuer User wird hinzugefügt
			new ProspectsPopUpFrame(frame);
			break;
		case "exportCSV":
			exportSelectionToCSV();
			break;
		case "importCSV":
			CSVImportExport.importCSV(frame);
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
		MyTableModel.getMyTableRowSorter().setRowFilter(compoundFilter);
	}
	
	private void exportSelectionToCSV() {
		// Anzahl aller Reihen im JTable
		int rowCount = table.getRowCount();
		
		// Indizes aller sichtbaren Reihen in Array speichern
		int[] visibleRows = new int[0];
		
		int visibleRowCount = 0;
		for (int i = 0; i < rowCount; i++) {
			// Checken, ob akt. Reihe sichtbar ist, -1 wird zurückgegeben, wenn die Reihe herausgefiltert wurde und somit nicht sichtbar ist
			int currentRowInModel = MyTableModel.getMyTableRowSorter().convertRowIndexToModel(i);
			if (currentRowInModel != -1) {
				visibleRowCount++;
				
				// akt. Reihe zum Array hinzufügen
				int[] tmpArray = Arrays.copyOf(visibleRows, visibleRowCount);
				visibleRows = tmpArray;
				visibleRows[visibleRowCount - 1] = currentRowInModel;
			}
		}
		
		// Auswahl als CSV exportieren
		CSVImportExport.exportCSV(frame, visibleRows);
	}
}
