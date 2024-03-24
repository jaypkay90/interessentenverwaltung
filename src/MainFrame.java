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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
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

public class MainFrame extends JFrame implements KeyListener, ActionListener {

	private static final long serialVersionUID = 1L;
	
	private JFrame frame;
	private JPanel contentPane;
	//private JPanel centerPanel;
	private JTable table;
	private DefaultTableModel model;
	private String[] colNames = TableHeaders.getJTableHeaders();
	private Map<JTextField, String> textFieldMap;
	private TableRowSorter<DefaultTableModel> myTableRowSorter;
	//private Connection base;
	
	
	public MainFrame() {
		// Mit Database verbinden
		Database.connectToDatabase();
		
		// HashMap mit TableHeaders aufbauen
		//TableHeaders.buildMap();
		
		
		// Frame erstellen, Icon hinzufügen und andere Grundeigenschaften setzen
		frame = new JFrame();
		ImageIcon icon = new ImageIcon("MainIcon.png");
		frame.setIconImage(icon.getImage());
		//frame.setIconImage(Toolkit.getDefaultToolkit().getImage("D:\\Beruf\\Ausbildung\\Eclipse Workspace\\JP Interessentenverwaltung\\MainIcon.png"));
		frame.setTitle("Interessentenverwaltung");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Frame maximiert anzeigen (BOTH: Maximierte Höhe und Breite in Abhängigkeit von der Bildschirmgröße)
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
        /*Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screenSize.width, screenSize.height);*/
		
		// Initiale Größe des Frames beim Minimieren
		frame.setBounds(100, 100, 800, 500);
		
		contentPane = new JPanel();
		//contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout());
		frame.setContentPane(contentPane);

		// Menü erstellen und zum North-Region hinzufügen
		//JMenuBar menuBar = new JMenuBar();
		JMenuBar menuBar = setupMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);
		
		/*JMenu fileMenu = new JMenu("Datei");
		menuBar.add(fileMenu);
		
		JMenuItem closeItem = new JMenuItem("Schließen");
		closeItem.setActionCommand("exit");
		closeItem.addActionListener(this);
		fileMenu.add(closeItem);
		
		JMenuItem addUser = new JMenuItem("Benutzer hinzufügen");
		addUser.setActionCommand("addNewUser");
		addUser.addActionListener(this);
		fileMenu.add(addUser);
		
		JMenu helpMenu = new JMenu("Hilfe");
		menuBar.add(helpMenu);*/
		
		// Panel zum Anzeigen der Daten aus der DB zur Center-Region hinzufügen
		JScrollPane scrollTablePane = setupCenterPanel();
		//JPanel centerPanel = setupCenterPanel();
		contentPane.add(scrollTablePane, BorderLayout.CENTER);
		
		// Panel für Suche und Filterung der Daten zur East-Region hinzufügen
		JScrollPane scrollSearchPane = setupSearchPanel();
        contentPane.add(scrollSearchPane, BorderLayout.EAST);
        
        //frame.pack();
		frame.setVisible(true);
	}
	
	private JScrollPane setupSearchPanel() {
        JPanel searchPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        searchPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        searchPanel.add(new JLabel("Suche", SwingConstants.CENTER));
        
        // Die Textfelder und der Name des Feldes werden über eine HashMap "verbunden". So kann festgestellt werden, in welchen Tabellenspalten gefiltert werden soll.
        textFieldMap = new HashMap<>();
        
        for (String colName : colNames) {
        	JPanel itemPanel = new JPanel(new GridLayout(0, 1, 5,5));
            JLabel label = new JLabel(colName);
            JTextField textField = new JTextField(15); // Breite: 15px
            textField.addKeyListener(this);
            
            itemPanel.add(label);
            itemPanel.add(textField);
            
            textFieldMap.put(textField, colName);

            searchPanel.add(itemPanel);
        }
        
        JScrollPane scrollSearchPane = new JScrollPane(searchPanel);
        scrollSearchPane.setPreferredSize(new Dimension(400, 0));
        
        return scrollSearchPane;
	}
	
	private JMenuBar setupMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		// Datei-Menü erstellen
		JMenu fileMenu = new JMenu("Datei");
		fileMenu.add(createMenuItem("Schließen", "exit"));
		fileMenu.add(createMenuItem("Benutzer hinzufügen", "addNewUser"));
		menuBar.add(fileMenu);
		
		// HilfeMenü erstellen
		JMenu helpMenu = new JMenu("Hilfe");
		menuBar.add(helpMenu);
		
		return menuBar;
	}
	
	private JMenuItem createMenuItem(String label, String command) {
		JMenuItem item = new JMenuItem(label);
		item.setActionCommand(command);
		item.addActionListener(this);
		return item;
	}
	
	private JScrollPane setupCenterPanel() {
		// Im CenterPanel werden die Daten aus der Datenbank in einem JTable angezeigt
		// Daten aus der Datenbank auslesen und in einem TableModel speichern
		getTableData();
		
		table = new JTable(model);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int rowInView = table.getSelectedRow();
				
				
				int selectedRow = table.convertRowIndexToModel(rowInView);
				
				// Daten aus der selektierten Reihe auslesen
				HashMap<String, String> rowData = new HashMap<>();
				for (int i = 0; i < colNames.length; i++) {
					rowData.put(colNames[i], model.getValueAt(selectedRow, i).toString());
				}
				
				// ProspectsPopUp öffnen und userdaten in die Felder einfügen
				new ProspectsPopUpFrame(rowData, selectedRow);
			}
		});
		
		
		// Scrollbar und Tablesorter zum JTable hinzufügen
		JScrollPane scrollPane = new JScrollPane(table);
		myTableRowSorter = new TableRowSorter<>(model);
		myTableRowSorter.setComparator(0, Comparator.comparingInt(o ->  Integer.parseInt(o.toString())));
		myTableRowSorter.setComparator(1, Comparator.comparingInt(o ->  Integer.parseInt(o.toString())));
		myTableRowSorter.setComparator(13, Comparator.comparingInt(o ->  Integer.parseInt(o.toString())));
		myTableRowSorter.setComparator(15, Comparator.comparingInt(o ->  Integer.parseInt(o.toString())));
		table.setRowSorter(myTableRowSorter);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(scrollPane);
		
		JScrollPane scrollTablePane = new JScrollPane(centerPanel);
		 scrollTablePane.setPreferredSize(new Dimension(400, 0));
		return scrollTablePane;
		//contentPane.add(centerPanel, BorderLayout.CENTER);
	}
	
	private void getTableData() {
		// TableModel aus der MyTableModel Klasse bekommen
		model = MyTableModel.getModel();
		
		// Überschriften zum TableModel hinzufügen
		model.setColumnIdentifiers(TableHeaders.getJTableHeaders());
					
		// Daten aus der DB auslesen und zum TableModel hinzufügen		
		Statement statement = null;
		ResultSet rs = null;
		
		try {
			
			
			// TableModel aus der MyTableModel Klasse bekommen
			//model = MyTableModel.getModel();
			
			// Überschriften zum JTable hinzufügen
			//model.setColumnIdentifiers(TableHeaders.getJTableHeaders());
			
			
			// Spaltenanzahl bekommen --> entspricht der Anzahl von Einträgen in der TableHeaders HashMap
			/*ResultSetMetaData rsmetadata = rs.getMetaData();
			int colCount = rsmetadata.getColumnCount();*/
			//int colCount = TableHeaders.getColCount();
			
			// Überschriften der Spalten aus den Metadaten auslesen
			/*colNames = new String[colCount];
			for (int i = 0; i < colCount; i++) {
				String colNameDB = rsmetadata.getColumnLabel(i + 1);
				System.out.println(colNameDB);
				
				// Überschrift für den JTable zum Array hinzufügen
				// Frage: Warum dieser Aufwand. Theoretisch könnte man auch einfach die Überschriften in eine LinkedList packen und sie einfach blind in den JTable schreiben
				// Vorteil hierbei: Wenn sich die Reihenfolge der Einträge in der Datenbank ändert, funktioniert diese Methode weiterhin, weil alles abgeglichen wird
				colNames[i] = TableHeaders.getJTableColName(colNameDB);
				
				
			}*/
			
			statement = Database.createStatement();
			rs = statement.executeQuery("SELECT * FROM prospects");
			
			// Spaltenanzahl entpricht der Anzahl von Spaltenüberschriften
			int colCount = TableHeaders.getColCount();
			
			// Daten aus der Datenbank auslesen, solange es noch welche gibt...
			while (rs.next()) {
			    String[] row = new String[colCount];
			    for (int i = 0; i < colCount; i++) {
			    	String currentRsValue = rs.getString(i + 1);
			    	row[i] = currentRsValue == null ? "" : currentRsValue; 
			    }
			    
			    // Aktuelle Reihe zum JTable hinzufügen
			    model.addRow(row);
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			Database.closeResultSetAndStatement(rs);
		}
	}
	
	/*private int findColumnNumber(String columnName) {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            TableColumn column = columnModel.getColumn(i);
            String currentColumnName = (String) column.getHeaderValue();
            if (currentColumnName.equals(columnName)) {
                return i; 
            }
        }
        return -1; // Return -1, wenn Spalte nicht gefunden
    }*/
	
	// ActionListener für Buttons
	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		switch (command) {
		case "addNewUser":
			new ProspectsPopUpFrame();
			break;
		case "exit":
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// User hat Suchtext eingegeben --> Tabelle filtern... --Field in der HashMap suchen
		/*JTextField selectedField = (JTextField) e.getSource();
		String colName = textFieldMap.get(selectedField);
		int colNum = findColumnNumber(colName);*/
		filterTable();
	}
	
	public void filterTable() {
		// In dieser ArrayList werden alle gefilterten Reihen angezeigt --> Grö
		ArrayList<RowFilter<TableModel, Object>> colFilters = new ArrayList<>();
		
		// Durch Hashmap mit JTextfields loopen
		for (Map.Entry<JTextField, String> entry : textFieldMap.entrySet()) {
			
			// Textfield und column Name des akt. Entrys bekmmen
			JTextField currentField = entry.getKey();
			String colName = entry.getValue();
			String searchText = currentField.getText();
			int colNum = TableHeaders.getJTableColNumByJTableColName(colName);
			
			// Filter für akt. Col. in der Tabelle zum Filter-Array hinzufügen, wenn Suchtext nicht leer ist
			if (searchText != "") {
				colFilters.add(RowFilter.regexFilter(currentField.getText(), colNum));
			}
		}
		
		// Filter von allen Cols kombinieren
		RowFilter<TableModel, Object> compoundFilter = RowFilter.andFilter(colFilters);
		
		// Kombinierten Filter zum RowSorter hinzufügen
		myTableRowSorter.setRowFilter(compoundFilter);		
	}
		
	
}
