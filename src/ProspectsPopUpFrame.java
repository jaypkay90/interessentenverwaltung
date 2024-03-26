import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProspectsPopUpFrame extends JFrame implements ActionListener {

private static final long serialVersionUID = 1L;
private String[] colNames = TableHeaders.getJTableHeaders();
private HashMap<String, JTextField> userData;
private JFrame frame;
private JTextField[] dataFields;
private HashMap<String, String> rowData;
private boolean editUser;
private int selectedRow;

		
	public ProspectsPopUpFrame() {
		editUser = false;
		//this.colNames = colNames;
		
		setUpFrame();
	}
	
	public ProspectsPopUpFrame(HashMap<String, String> rowData, int selectedRow) {
		editUser = true;
		this.rowData = rowData;
		this.selectedRow = selectedRow;
		//this.colNames = colNames;
		
		setUpFrame();
	}
	
	private void setUpFrame() {
		// An dieser Stelle besteht normalerweise schon eine Connection zur DB --> man hätte theoretisch eine getterMethode verwenden können
		// Vorteil hierbei: Sollte die Connection aus irgendeinem Grund abgebrochen sein, wird sie neu erstellt, sollte sie bestehen, ändert sich nichts
		Database.connectToDatabase();
		
		userData = new HashMap<>();
		frame = new JFrame();
		
		frame.setTitle("Interessenteninformation");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
		frame.setResizable(false);
		frame.setLayout(new BorderLayout(10, 10));
		
		ImageIcon image = new ImageIcon("MainIcon.png");
		frame.setIconImage(image.getImage());
		
		
		JPanel userDataInputPanel = new JPanel(new GridLayout(0, 3, 15, 15));
		userDataInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		
		if (editUser) {
			addInputFieldsEdit(userDataInputPanel);
		}
		else {
			addInputFields(userDataInputPanel);
		}
		
		JPanel actionPanel = new JPanel();
		
		JButton saveBtn = createBtn("Speichern", "save");
		actionPanel.add(saveBtn);
		
		if (editUser) {
			JButton deleteBtn = createBtn("Löschen", "delete");
			actionPanel.add(deleteBtn);
		}
		
		JButton abortBtn = createBtn("Abbrechen", "abort");
		actionPanel.add(abortBtn);
		
		actionPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
				
		frame.add(actionPanel, BorderLayout.SOUTH);
		frame.add(userDataInputPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
	private JButton createBtn(String btnName, String actionCommand) {
		JButton button = new JButton(btnName);
		button.setActionCommand(actionCommand);
		button.addActionListener(this);
		return button;
	}
	
	
	private void addInputFields(JPanel panel) {
		dataFields = new JTextField[colNames.length - 1];
		for (int i = 0; i < colNames.length; i++) {
			JPanel itemPanel = new JPanel(new GridLayout(0, 1, 8, 8));
			
			if (colNames[i] != "ID") {
				userData.put(colNames[i], new JTextField(15));
				itemPanel.add(new JLabel(colNames[i]));
				itemPanel.add(userData.get(colNames[i]));
				panel.add(itemPanel);				
			}
		}
	}
	
	public void addInputFieldsEdit(JPanel panel) {
		dataFields = new JTextField[colNames.length];
		for (int i = 0; i < colNames.length; i++) {
			JPanel itemPanel = new JPanel(new GridLayout(0, 1, 8, 8));
			userData.put(colNames[i], new JTextField(rowData.get(colNames[i]), 15));
			itemPanel.add(new JLabel(colNames[i]));
			itemPanel.add(userData.get(colNames[i]));
			panel.add(itemPanel);				
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		switch (command) {
		case "delete":
			deleteUser();
			break;
		case "save":
			String query;
			if (editUser) {
				// Existierenden Interessenten updaten
				query = generateUpdateQuery();
				updateDatabaseTable(query);
				updateExistingProspectInJTable();
			}
			else {
				// Neuen Interessenten hinzufügen
				String colNamesStr = buildColStringForQuery();
				query = String.format("INSERT INTO prospects (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", colNamesStr);
				updateDatabaseTable(query);
				addNewProspectToJTable();
			}
			break;
		}
		
		// Frame schließen
		frame.dispose();
	}
	
	private void updateExistingProspectInJTable() {
		String insertQuery = generateUpdateQuery();
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = Database.createStatement();
			statement.executeUpdate(insertQuery);
			MyTableModel model = MyTableModel.getModel();
			
			// Geänderte Zeile in der Datenbank auswählen
			String getInsertQuery = String.format("SELECT * FROM prospects WHERE ID = %s", userData.get("ID"));
			rs = statement.executeQuery(getInsertQuery);
			
			// Spaltenanzahl bekommen --> entspricht der Anzahl von Überschriften
			/*ResultSetMetaData rsmetadata = rs.getMetaData();
			int colCount = rsmetadata.getColumnCount();*/
			int colCount = TableHeaders.getColCount();
			
			// Zeile im JTable mit den neu eingegebenen Daten updaten
			for (int i = 0; i < colCount; i++) {
				model.setValueAt(rs.getString(i + 1), selectedRow, i);
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			Database.closeResultSetAndStatement(rs);
		}
	}
	
	private void addNewProspectToJTable() {
		Statement statement = null;
		ResultSet rs = null;
		try {
			MyTableModel model = MyTableModel.getModel();
			
			// Die soeben hinzugefügte Zeile in der Datenbank auswählen
			statement = Database.createStatement();
			String getInsertQuery = "SELECT * FROM prospects ORDER BY ID DESC LIMIT 1";
			rs = statement.executeQuery(getInsertQuery);
			
			// Spaltenanzahl bekommen
			ResultSetMetaData rsmetadata = rs.getMetaData();
			int colCount = rsmetadata.getColumnCount();
			String[] row = new String[colCount];
			
			// Daten aus der letzten Tabellenzeile zum Tabellenmodell hinzufügen
			for (int i = 0; i < colCount; i++) {
				row[i] = rs.getString(i + 1);
			}	
			model.addRow(row);
			model.fireTableDataChanged();
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			Database.closeResultSetAndStatement(rs);
		}
	}
	
	private void deleteUser() {
		Statement statement = null;
		try {
			MyTableModel model = MyTableModel.getModel();
			
			// Eintrag aus der Datenbank löschen
			statement = Database.createStatement();
			statement.executeUpdate(String.format("DELETE FROM prospects WHERE ID = %s", rowData.get("ID")));
			
			// Reihe aus dem JTable löschen
			model.removeRow(selectedRow);
			model.fireTableDataChanged();
			
			// Statement schließen
			statement.close();
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			Database.closeStatement();
		}
	}
	
	private String buildColStringForQuery() {
		// Diese Methode baut einen String mit allen Spaltennamen in der Datenbank, um diesen String für Queries verwenden zu können
		StringBuilder builder = new StringBuilder();
		String dbHeaders[] = TableHeaders.getDBHeaders();
		
		for (String header : dbHeaders) {
			if (header != "ID") {
				builder.append(header + ", ");				
			}
		}
		
		String colNamesStr = builder.toString();
		colNamesStr = colNamesStr.substring(0, builder.length() - 2);
		System.out.println(colNamesStr);
		
		return colNamesStr.toString();
	}
	
	private void updateDatabaseTable(String query) {
		try {
			Connection connect = Database.getConnection();
			PreparedStatement prep = connect.prepareStatement(query);
			
			//prep.setInt(1, Integer.parseInt(userData.get(TableHeaders.getJTableColNameByColIndex(1)).getText()));
			prep.setInt(1, 0);
			prep.setString(2, userData.get(TableHeaders.getJTableColNameByColIndex(2)).getText());
			prep.setString(3, userData.get(TableHeaders.getJTableColNameByColIndex(3)).getText());
			prep.setString(4, userData.get(TableHeaders.getJTableColNameByColIndex(4)).getText());
			prep.setString(5, userData.get(TableHeaders.getJTableColNameByColIndex(5)).getText());
			prep.setString(6, userData.get(TableHeaders.getJTableColNameByColIndex(6)).getText());
			prep.setString(7, userData.get(TableHeaders.getJTableColNameByColIndex(7)).getText());
			prep.setString(8, userData.get(TableHeaders.getJTableColNameByColIndex(8)).getText());
			prep.setString(9, userData.get(TableHeaders.getJTableColNameByColIndex(9)).getText());
			prep.setString(10, userData.get(TableHeaders.getJTableColNameByColIndex(10)).getText());
			prep.setString(11, userData.get(TableHeaders.getJTableColNameByColIndex(11)).getText());
			prep.setString(12, userData.get(TableHeaders.getJTableColNameByColIndex(12)).getText());
			prep.setString(13, userData.get(TableHeaders.getJTableColNameByColIndex(13)).getText());
			prep.setString(14, userData.get(TableHeaders.getJTableColNameByColIndex(14)).getText());
			prep.setString(15, userData.get(TableHeaders.getJTableColNameByColIndex(15)).getText());
			prep.setString(16, userData.get(TableHeaders.getJTableColNameByColIndex(16)).getText());
			//prep.setDate(18, userData.get(TableHeaders.getDBColNameByColIndex(16)).getText());
			
			// DATE-PROBLEM		
			prep.setDate(17, null);
			prep.executeUpdate();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
			
	}
	
	private void executeQuery(String query) {
		//String query = String.format("INSERT INTO prospects (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", colNamesStr);
		
		/*for (Map.Entry<String, JTextField> entry : userData.entrySet()) {
			String key = entry.getKey();
			String text = entry.getValue().getText();
		}*/
		
		try {
			Connection connect = Database.getConnection();
			PreparedStatement prep = connect.prepareStatement(query);
		
			
			//prep.setInt(1, Integer.parseInt(userData.get(TableHeaders.getJTableColNameByColIndex(1)).getText()));
			prep.setInt(1, 0);
			prep.setString(2, userData.get(TableHeaders.getJTableColNameByColIndex(2)).getText());
			prep.setString(3, userData.get(TableHeaders.getJTableColNameByColIndex(3)).getText());
			prep.setString(4, userData.get(TableHeaders.getJTableColNameByColIndex(4)).getText());
			prep.setString(5, userData.get(TableHeaders.getJTableColNameByColIndex(5)).getText());
			prep.setString(6, userData.get(TableHeaders.getJTableColNameByColIndex(6)).getText());
			prep.setString(7, userData.get(TableHeaders.getJTableColNameByColIndex(7)).getText());
			prep.setString(8, userData.get(TableHeaders.getJTableColNameByColIndex(8)).getText());
			prep.setString(9, userData.get(TableHeaders.getJTableColNameByColIndex(9)).getText());
			prep.setString(10, userData.get(TableHeaders.getJTableColNameByColIndex(10)).getText());
			prep.setString(11, userData.get(TableHeaders.getJTableColNameByColIndex(11)).getText());
			prep.setString(12, userData.get(TableHeaders.getJTableColNameByColIndex(12)).getText());
			prep.setString(13, userData.get(TableHeaders.getJTableColNameByColIndex(13)).getText());
			prep.setString(14, userData.get(TableHeaders.getJTableColNameByColIndex(14)).getText());
			prep.setString(15, userData.get(TableHeaders.getJTableColNameByColIndex(15)).getText());
			prep.setString(16, userData.get(TableHeaders.getJTableColNameByColIndex(16)).getText());
			//prep.setDate(18, userData.get(TableHeaders.getDBColNameByColIndex(16)).getText());
			
			// DATE-PROBLEM		
			prep.setDate(17, null);
			prep.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private String generateUpdateQuery() {
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE prospects SET ");
		int colCount = TableHeaders.getColCount();
		for (int i = 1; i < colCount; i++) {
			builder.append(String.format("%s = ?, ", TableHeaders.getDBColNameByColIndex(i)));
		}
		
		builder.setLength(builder.length() - 2);
		builder.append(" WHERE ID = ?");
		return builder.toString();
				
				
				/*"UPDATE prospects SET " +
				"Status = '%s', " +
				"Interesse_an = '%s', " +
                "Vorname = '%s', " +
                "Nachname = '%s', " +
                "Telefon = '%s', " +
                "E_Mail = '%s', " +
                "Sprache = '%s', " +
                "Kanal = '%s', " +
                "Firma = '%s', " +
                "Abteilung = '%s', " +
                "Branche = '%s', " +
                "Land = '%s', " +
                "Strasse = '%s', " +
                "Hausnummer = '%s', " +
                "PLZ = '%s', " +
                "Stadt = '%s', " +
                "Werbemassnahmen = '%s' " +
                "WHERE id = %s";
		
		return String.format(query,
				userData.get("Status").getText(),
                userData.get("Interesse_an").getText(),
                userData.get("Vorname").getText(),
                userData.get("Nachname").getText(),
                userData.get("Telefon").getText(),
                userData.get("E_Mail").getText(),
                userData.get("Sprache").getText(),
                userData.get("Kanal").getText(),
                userData.get("Firma").getText(),
                userData.get("Abteilung").getText(),
                userData.get("Branche").getText(),
                userData.get("Land").getText(),
                userData.get("Strasse").getText(),
                userData.get("Hausnummer").getText(),
                userData.get("PLZ").getText(),
                userData.get("Stadt").getText(),
                userData.get("Werbemassnahmen").getText(),
                userData.get("id").getText());*/
	}

}
