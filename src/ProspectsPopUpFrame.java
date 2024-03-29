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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
		
		// userData: HashMap, bei der jede Spaltenüberschrift aus der Tabelle mit einem JTextfield verbunden wird.
		// Mir Hilfe dieser Map können die Usereingaben zu den verschiedenen Spalten eingegeben werden
		userData = new HashMap<>();
		
		// Frame aufsetzen und Grundeigenschaften festlegen
		frame = new JFrame();
		frame.setTitle("Interessenteninformation");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
		frame.setResizable(false);
		frame.setLayout(new BorderLayout(10, 10));
		
		// Icon hinzufügen
		ImageIcon image = new ImageIcon("MainIcon.png");
		frame.setIconImage(image.getImage());
		
		// JPanel für User Input erstellen
		JPanel userDataInputPanel = new JPanel(new GridLayout(0, 3, 15, 15));
		userDataInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		
		/*if (editUser) {
			addInputFieldsEdit(userDataInputPanel);
		}
		else {
			addInputFields(userDataInputPanel);
		}*/
		
		// Überschriften und Eingabefelder zum Panel für den User-Input hinzufügen
		addInputFields(userDataInputPanel);
		
		// Panel für Aktionsbutton (Interessent speichern, löschen oder Operation abbrechen) hinzufügen
		JPanel actionPanel = new JPanel();
		
		// Speichern-Button zum Aktionspanel hinzufügen
		JButton saveBtn = createBtn("Speichern", "save");
		actionPanel.add(saveBtn);
		
		// NUR wenn ein existierender User bearbeitet werden soll --> Löschen-Button zum Aktionspanel hinzufügen
		if (editUser) {
			JButton deleteBtn = createBtn("Löschen", "delete");
			actionPanel.add(deleteBtn);
		}
		
		// Abbrechen-Button zum Aktionspanel hinzufügen
		JButton abortBtn = createBtn("Abbrechen", "abort");
		actionPanel.add(abortBtn);	
		actionPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		
		// Panel zum Frame hinzufügen und Frame sichtbar machen
		frame.add(actionPanel, BorderLayout.SOUTH);
		frame.add(userDataInputPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
	private JButton createBtn(String btnName, String actionCommand) {
		// Diese Methode kreiiert einen JButton mit dem Text btnName und dem ActionCommand actionCommand
		JButton button = new JButton(btnName);
		button.setActionCommand(actionCommand);
		button.addActionListener(this);
		return button;
	}
	
	
	private void addInputFields(JPanel userDataInputPanel) {
		// Input-Textfelder mit Überschriften zum Panel für den userInput hinzufügen
		
		// Durch alle Spaltenüberschriften loopen
		for (int i = 0; i < colNames.length; i++) {
			// Für jede Spalte im JTable wird ein eigenes Panel mit der Spaltenüberschrift und einem dazugehörigen Textfeld erstellt
			JPanel itemPanel = new JPanel(new GridLayout(0, 1, 8, 8));
			JTextField inputField = new JTextField(15);
			
			// NUR wenn ein existierender User editiert werden soll, wird der Text des InputFields gesetzt
			if (editUser) {
				// Text im InputField entspricht dem Text in der Spalte mit dem aktuellen Spaltennamen der angeklickten Reihe im JTable
				// Beispiel: In der Spalte "Vorname" steht "Harry" --> rowData: Key = "Vorname", Value = "Harry"
				inputField.setText(rowData.get(colNames[i]));
			}
			
			// Das Textfeld für die Interessenten-ID soll nicht editierbar sein
			if (colNames[i].equals("ID")) {
				inputField.setEnabled(false);
			}
			
			// Aktuellen Eintrag im UserData dictionary abspeichern, Key: Spaltenüberschrift, Value: Das dazugehörige Textfeld
			userData.put(colNames[i], inputField);
			
			// Label mit Überchrift und Textfeld zum itemPanel hinzufügen
			itemPanel.add(new JLabel(colNames[i]));
			itemPanel.add(userData.get(colNames[i]));
			
			// itemPanel mit Überschrift und Textfeld zum userDataInputPanel hinzufügen
			userDataInputPanel.add(itemPanel);				
		}
	}
	
	/*public void addInputFieldsEdit(JPanel panel) {
		for (int i = 0; i < colNames.length; i++) {
			JPanel itemPanel = new JPanel(new GridLayout(0, 1, 8, 8));
			
			JTextField inputField = new JTextField(rowData.get(colNames[i]), 15);
			
			// Das Textfeld für die Interessenten-ID soll nicht editierbar sein
			if (colNames[i].equals("ID")) {
				inputField.setEnabled(false);
			}
			
			userData.put(colNames[i], inputField);
			
			itemPanel.add(new JLabel(colNames[i]));
			itemPanel.add(userData.get(colNames[i]));
			
			panel.add(itemPanel);				
		}
	}*/

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		switch (command) {
		case "delete":
			deleteUser();
			break;
		case "save":
			// Checken ob der User-Input valide ist. Wenn nicht: return
			if (!checkUserInputValidity()) {
				return;
			}
			
			if (editUser) {
				// Existierenden Interessenten updaten
				// Validität des Inputs sicherstellen
				updateExistingProspect();
				/*query = generateUpdateQuery();
				updateDatabaseTable(query);
				updateExistingProspectInJTable();*/
			}
			else {
				// Neuen Interessenten hinzufügen
				insertNewProspect();
				/*String colNamesStr = buildColStringForInsertQuery();
				query = String.format("INSERT INTO prospects (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", colNamesStr);
				updateDatabaseTable(query);
				addNewProspectToJTable();*/
			}
			break;
		}
		
		// Frame schließen
		frame.dispose();
	}

	private boolean checkUserInputValidity() {
		// Der Input ist nicht valide, wenn keine Interessentendaten eigegeben wurden
		int emptyTextFields = 0;
		
		// Wir starten bei 1, weil die ID automatisch generiert wird und nicht vom User eingegeben werden kann
		for (int i = 1; i < colNames.length; i++) {
			if (userData.get(colNames[i]).getText().equals("")) {
				emptyTextFields++;
			}
		}
		
		if (emptyTextFields == colNames.length - 1) {
			JOptionPane.showMessageDialog(null, "Bitte geben Sie Interessentendaten ein", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// Der Input im InputField "Priorität" ist nur dann valide, wenn ein int zwischen 1 und 5 eingeben wurde
		String priorityString = userData.get(TableHeaders.getJTableColNameByColIndex(1)).getText();
		if (!priorityString.equals("")) {
			try {
				int priorityInt = Integer.parseInt(priorityString);
				if (priorityInt < 1 || priorityInt > 5) {
					throw new NumberFormatException();
				}
			}
			catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Priorität muss eine Ganzzahl zwischen 1 und 5 sein", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// Wenn der User keine Priorität eingegben hat, wird sie automatisch auf 1 gesetzt
		else {
			userData.get(TableHeaders.getJTableColNameByColIndex(1)).setText("1");
		}
		
		// Wenn eine Erinnerung gesetzt wurde, muss sie ein Zeitformat haben.
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		String dateString = userData.get(TableHeaders.getJTableColNameByColIndex(17)).getText();
		if (!dateString.equals("")) {
			try {
				if (dateString.length() != 10) {
					throw new Exception();
				}
				Date date = dateFormat.parse(dateString);
				Date currentDate = Calendar.getInstance().getTime();
				
				if (!date.after(currentDate)) {
					JOptionPane.showMessageDialog(null, "Erinnerungsdatum muss in der Zukunft liegen", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Erinnerung bitte im Format TT.MM.JJJJ eintragen", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		return true;
	}
	
	private void updateExistingProspect() {
		// Existierenden Interessenten updaten
		String query = TableHeaders.getUpdateQueryHeadersString();
		updateDatabaseTable(query);
		updateExistingProspectInJTable();
	}
	
	private void insertNewProspect() {
		// Neuen Interessenten hinzufügen
		String colNamesStr = TableHeaders.getInsertQueryHeadersString();
		String query = String.format("INSERT INTO prospects (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", colNamesStr);
		updateDatabaseTable(query);
		addNewProspectToJTable();
	}
	
	private void updateExistingProspectInJTable() {
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
			int colCount = TableHeaders.getColCount();
			
			// Zeile im JTable mit den neu eingegebenen Daten updaten
			MyTableModel model = MyTableModel.getModel();
			for (int i = 0; i < colCount; i++) {
				
				if (TableHeaders.getDBColNameByColIndex(i).equals("Erinnerung")) {
		    			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		    			dateFormat.setLenient(false);
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
	
	private void updateDatabaseTable(String query) {
		PreparedStatement prep = null;
		try {
			Connection connect = Database.getConnection();
			System.out.println(query);
			System.out.println(userData.get(TableHeaders.getJTableColNameByColIndex(2)).getText());
			prep = connect.prepareStatement(query);
			
			prep.setInt(1, Integer.parseInt(userData.get(TableHeaders.getJTableColNameByColIndex(1)).getText()));
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
			
			// Erinnerungsdatum einfügen
			String dateString = userData.get(TableHeaders.getJTableColNameByColIndex(17)).getText();
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
			
			// Wenn ein existierender Interessent bearbeitet wird, müssen die Daten dieses Interessenten mithilfe der ID in der Datenbank selektiert werden 
			if(editUser) {
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

}
