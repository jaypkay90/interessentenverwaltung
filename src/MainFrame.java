import java.sql.*;


import java.awt.EventQueue;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.BoxLayout;
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

public class MainFrame extends JFrame implements KeyListener, ActionListener {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel centerPanel;
	private JTable table;
	private Statement statement;
	private DefaultTableModel model;
	private String[] colNames;
	private Map<JTextField, String> textFieldMap;
	private TableRowSorter<DefaultTableModel> myTableRowSorter;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.pack();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		// Datenbankverbindung trennen
		
	}

	/**
	 * Create the frame.
	 */
	
	
	
	public MainFrame() {
		// Mit Database verbinden
		connectToDatabase();
		
		setIconImage(Toolkit.getDefaultToolkit().getImage("D:\\Beruf\\Ausbildung\\Eclipse Workspace\\JP Interessentenverwaltung\\MainIcon.png"));
		setTitle("Interessentenverwaltung");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 803, 507);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JMenuBar menuBar = new JMenuBar();
		contentPane.add(menuBar, BorderLayout.NORTH);
		
		JMenu fileMenu = new JMenu("Datei");
		menuBar.add(fileMenu);
		
		JMenuItem closeItem = new JMenuItem("Schließen");
		fileMenu.add(closeItem);
		
		JMenu helpMenu = new JMenu("Hilfe");
		menuBar.add(helpMenu);
		
		centerPanel = new JPanel();
		contentPane.add(centerPanel, BorderLayout.CENTER);
		
		createDataTable("SELECT * FROM prospects");
		
		// Create a panel for the east region with GridBagLayout
        JPanel searchPanel = new JPanel(new GridBagLayout());

        // Create constraints for GridBagLayout
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.insets = new Insets(5, 5, 5, 5);

        // Create multiple rows with JLabel and JTextField
        textFieldMap = new HashMap<>();
        
        for (String colName : colNames) {
            JLabel label = new JLabel(colName);
            JTextField textField = new JTextField(15); // Breite: 15px
            textField.addKeyListener(this);
            textFieldMap.put(textField, colName);

            // Add components to the panel using constraints
            searchPanel.add(label, constraints);
            searchPanel.add(textField, constraints);
        }
        
        JScrollPane scrollSearchPane = new JScrollPane(searchPanel);
        scrollSearchPane.setPreferredSize(new Dimension(400, 0));
        contentPane.add(scrollSearchPane, BorderLayout.EAST);
	}
	
	private void createDataTable(String query) {	
		table = new JTable();
		getTableData(query);		
		
		
		// Scrollbar und Tablesorter zum JTable hinzufügen
		JScrollPane scrollPane = new JScrollPane(table);
		myTableRowSorter = new TableRowSorter(model);
		table.setRowSorter(myTableRowSorter);
		
		centerPanel.setLayout(new BorderLayout(0, 0));
		
		centerPanel.add(scrollPane);
		
		contentPane.add(centerPanel, BorderLayout.CENTER);
	}
	
	private void getTableData(String query) {
		//query = "SELECT * FROM prospects WHERE Status is 'Interessiert'";
		ResultSet rs;
		try {
			rs = statement.executeQuery(query);
			
			model = (DefaultTableModel) table.getModel();
			
			// Spaltenanzahl bekommen
			ResultSetMetaData rsmetadata = rs.getMetaData();
			int colCount = rsmetadata.getColumnCount();
			
			// Überschriften der Spalten aus den Metadaten auslesen
			colNames = new String[colCount];
			for (int i = 0; i < colCount; i++) {
				colNames[i] = rsmetadata.getColumnLabel(i + 1);
			}
			
			// Überschriften zum JTable hinzufügen
			model.setColumnIdentifiers(colNames);
			
			// Daten aus der Datenbank auslesen, solange es noch welche gibt...
			while (rs.next()) {
			    String[] row = new String[colCount];
			    for (int i = 0; i < colCount; i++) {
			        row[i] = rs.getString(i + 1);
			    }
			    
			    // Aktuelle Reihe zum JTable hinzufügen
			    model.addRow(row);
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void connectToDatabase() {
		// JDBC URL
	    String url = "jdbc:sqlite:prospectsData.db";
	   
	    try {
	    	Class.forName("org.sqlite.JDBC");
	    	Connection conn = DriverManager.getConnection(url);
	    	statement = conn.createStatement();
	        

	    } catch (SQLException | ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	}
	
	 private int findColumnNumber(String columnName) {
        TableColumnModel columnModel = table.getColumnModel();
        int columnCount = columnModel.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            TableColumn column = columnModel.getColumn(i);
            String currentColumnName = (String) column.getHeaderValue();
            if (currentColumnName.equals(columnName)) {
                return i; // Return the column number if the names match
            }
        }
        return -1; // Return -1 if the column is not found
    }
	
	// ActionListener für Buttons
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
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
		// Text Field in der HashMap suchen
		JTextField selectedField = (JTextField) e.getSource();
		String colName = textFieldMap.get(selectedField);
		int colNum = findColumnNumber(colName);
		filterTable();
	}
	
	public void filterTable() {				
		RowFilter<TableModel, Object>[] colFilters = new RowFilter[textFieldMap.size()];
		int i = 0;
		
		// Durch Hashmap mit JTextfields loopen
		for (Map.Entry<JTextField, String> entry : textFieldMap.entrySet()) {			
			// Textfield und column Name des akt. Entrys bekmmen
			JTextField currentField = entry.getKey();
			String colName = entry.getValue();
			String searchText = currentField.getText();
			int colNum = findColumnNumber(colName);
			
			// Filter für akt. Col. zum Filter-Array hinzufügen, wenn Suchtext nicht leer ist
			if (searchText != "") {
				colFilters[i] = RowFilter.regexFilter(currentField.getText(), colNum);
				i++;
			}
		}
		
		// Array kopieren und alle leeren Plätze rauswerfen
		RowFilter<TableModel, Object>[] validFilters = Arrays.copyOf(colFilters, i);
		
		// Filter von allen Cols kombinieren
		RowFilter<TableModel, Object> compoundFilter = RowFilter.andFilter(Arrays.asList(validFilters));
		
		// Kombinierten Filter zum RowSorter hinzufügen
		myTableRowSorter.setRowFilter(compoundFilter);
			
		}
		
	
}
