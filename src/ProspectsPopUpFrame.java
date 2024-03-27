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
private HashMap<String, String> rowData;
private boolean editUser;
private int selectedRow;

		
	public ProspectsPopUpFrame() {
		editUser = false;
		setUpFrame();
	}
	
	public ProspectsPopUpFrame(HashMap<String, String> rowData, int selectedRow) {
		editUser = true;
		this.rowData = rowData;
		this.selectedRow = selectedRow;
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
		for (int i = 0; i < colNames.length; i++) {
			JPanel itemPanel = new JPanel(new GridLayout(0, 1, 8, 8));
				
			JTextField inputField = new JTextField(15);
			
			// Das Textfeld für die Interessenten-ID soll nicht editierbar sein
			if (colNames[i].equals("ID")) {
				System.out.println(colNames[i]);
				inputField.setEnabled(false);
			}
			
			userData.put(colNames[i], inputField);
			
			itemPanel.add(new JLabel(colNames[i]));
			itemPanel.add(userData.get(colNames[i]));
			
			panel.add(itemPanel);				
		}
	}
	
	public void addInputFieldsEdit(JPanel panel) {
		//dataFields = new JTextField[colNames.length];
		for (int i = 0; i < colNames.length; i++) {
			JPanel itemPanel = new JPanel(new GridLayout(0, 1, 8, 8));
			
			JTextField inputField = new JTextField(rowData.get(colNames[i]), 15);
			
			// Das Textfeld für die Interessenten-ID soll nicht editierbar sein
			if (colNames[i].equals("ID")) {
				System.out.println(colNames[i]);
				inputField.setEnabled(false);
			}
			
			userData.put(colNames[i], inputField);
			
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
				String colNamesStr = buildColStringForInsertQuery();
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
		//String insertQuery = generateUpdateQuery();
		PreparedStatement prep = null;
		ResultSet rs = null;
		try {
			
			// Geänderte Zeile in der Datenbank auswählen
			String findInsertQuery = String.format("SELECT * FROM prospects WHERE %s = ?", TableHeaders.getDBColNameByColIndex(0)); // 0: "ID"
			
			Connection connect = Database.getConnection();
			prep = connect.prepareStatement(findInsertQuery);
			
			System.out.println(userData.get(TableHeaders.getDBColNameByColIndex(0)).getText());
			
			prep.setInt(1, Integer.parseInt(userData.get(TableHeaders.getDBColNameByColIndex(0)).getText()));
			rs = prep.executeQuery();
			
			// Spaltenanzahl bekommen --> entspricht der Anzahl von Überschriften
			/*ResultSetMetaData rsmetadata = rs.getMetaData();
			int colCount = rsmetadata.getColumnCount();*/
			int colCount = TableHeaders.getColCount();
			
			// Zeile im JTable mit den neu eingegebenen Daten updaten
			MyTableModel model = MyTableModel.getModel();
			for (int i = 0; i < colCount; i++) {
				model.setValueAt(rs.getString(i + 1), selectedRow, i);
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			Database.closeResultSetAndPreparedStatement(rs, prep);
		}
	}
	
	private void addNewProspectToJTable() {
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
				row[i] = rs.getString(i + 1);
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
	
	private void deleteUser() {
		PreparedStatement prep = null;
		try {
			MyTableModel model = MyTableModel.getModel();
			
			// Eintrag aus der Datenbank löschen
			Connection connect = Database.getConnection();
			prep = connect.prepareStatement(String.format("DELETE FROM prospects WHERE %s = ?", TableHeaders.getDBColNameByColIndex(0))); // 0: "ID"
			prep.setInt(1, Integer.parseInt(userData.get(TableHeaders.getDBColNameByColIndex(0)).getText()));
			prep.executeUpdate();
			
			// Reihe aus dem JTable löschen
			model.removeRow(selectedRow);
			model.fireTableDataChanged();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			Database.closePreparedStatement(prep);
		}
	}
	
	private String buildColStringForInsertQuery() {
		// Diese Methode baut einen String mit allen Spaltennamen in der Datenbank, um diesen String für Queries verwenden zu können
		StringBuilder builder = new StringBuilder();
		String dbHeaders[] = TableHeaders.getDBHeaders();
		
		for (String header : dbHeaders) {
			// Die ID soll nicht vom User eingegeben werden
			if (header != TableHeaders.getDBColNameByColIndex(0)) {
				builder.append(header + ", ");				
			}
		}
		
		String colNamesStr = builder.toString();
		colNamesStr = colNamesStr.substring(0, builder.length() - 2);
		System.out.println(colNamesStr);
		
		return colNamesStr.toString();
	}
	
	private void updateDatabaseTable(String query) {
		PreparedStatement prep = null;
		try {
			Connection connect = Database.getConnection();
			System.out.println(query);
			System.out.println(userData.get(TableHeaders.getJTableColNameByColIndex(2)).getText());
			prep = connect.prepareStatement(query);
			
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
			
			if(editUser) {
				// Nur wenn der User bearbeitet werden soll, müssen die Daten des Users mit der spezifizierten ID in der Datenbank selektiert werden 
				prep.setInt(18, Integer.parseInt(userData.get(TableHeaders.getJTableColNameByColIndex(0)).getText()));
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
	
	private String generateUpdateQuery() {
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE prospects SET ");
		int colCount = TableHeaders.getColCount();
		for (int i = 1; i < colCount; i++) {
			builder.append(String.format("%s = ?, ", TableHeaders.getDBColNameByColIndex(i)));
		}
		
		builder.setLength(builder.length() - 2);
		builder.append(" WHERE " + TableHeaders.getDBColNameByColIndex(0) + " = ?");
		return builder.toString();
	}

}
