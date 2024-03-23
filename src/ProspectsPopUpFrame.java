import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProspectsPopUpFrame extends JFrame implements ActionListener {

private static final long serialVersionUID = 1L;
private String[] colNames;
private HashMap<String, JTextField> userData;
private JFrame frame;
private JTextField[] dataFields;
private HashMap<String, String> rowData;
private boolean editUser;
private int selectedRow;

		
	public ProspectsPopUpFrame(String[] colNames) {
		editUser = false;
		this.colNames = colNames;
		
		setUpFrame();
	}
	
	public ProspectsPopUpFrame(String[] colNames, HashMap<String, String> rowData, int selectedRow) {
		editUser = true;
		this.rowData = rowData;
		this.selectedRow = selectedRow;
		this.colNames = colNames;
		
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
			
			if (colNames[i] != "id") {
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
			if (editUser) {
				// Existierenden Interessenten updaten
				updateExistingProspect();
			}
			else {
				// Neuen Interessenten hinzufügen
				addNewProspect();
			}
			break;
		}
		
		// Frame schließen
		frame.dispose();
			
		/*if (command.equals("abort")) {
			// Nichts tun --> kann am Ende evtl. gel
		}
		else if (command.equals("delete")) {
			deleteUser();
			frame.dispose();
		}
		else if (command.equals("save")) {
			// Wenn neuer Interessent hinzugefügt wird
			if (editUser == false) {
				addNewProspect();
			}
			
			// Wenn existierender User upgedated wird.
			else {
				updateExistingProspect();
			}
		}
		
		// Nach Klicken des Buttons: Frame schließen
		frame.dispose();*/
	}
	
	private void updateExistingProspect() {
		String insertQuery = generateUpdateQuery();
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = Database.createStatement();
			statement.executeUpdate(insertQuery);
			MyTableModel model = MyTableModel.getModel();
			
			// Geänderte Zeile in der Datenbank auswählen
			String getInsertQuery = String.format("SELECT * FROM prospects WHERE id = %s", rowData.get("id"));
			rs = statement.executeQuery(getInsertQuery);
			
			// Spaltenanzahl bekommen
			ResultSetMetaData rsmetadata = rs.getMetaData();
			int colCount = rsmetadata.getColumnCount();
			
			// Zeile im JTable mit den neu eingegebenen Daten updaten
			for (int i = 0; i < colCount; i++) {
				model.setValueAt(rs.getString(i + 1), selectedRow, i);
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			Database.closeResultSet(rs);
			Database.closeStatement();
		}
	}
	
	private void addNewProspect() {
		String insertQuery = generateInsertQuery();
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = Database.createStatement();
			statement.executeUpdate(insertQuery);
			
			MyTableModel model = MyTableModel.getModel();
			
			// Die soeben hinzugefügte Zeile in der Datenbank auswählen
			String getInsertQuery = "SELECT * FROM prospects ORDER BY id DESC LIMIT 1";
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
			Database.closeResultSet(rs);
			Database.closeStatement();
		}
	}
	
	private void deleteUser() {
		Statement statement = null;
		try {
			MyTableModel model = MyTableModel.getModel();
			
			// Eintrag aus der Datenbank löschen
			statement = Database.createStatement();
			statement.executeUpdate(String.format("DELETE FROM prospects WHERE id = %s", rowData.get("id")));
			
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
	
	private String generateInsertQuery() {
		String query = "INSERT INTO prospects (Status, Interesse_an, Vorname, Nachname, Telefon, E_Mail, Sprache, Kanal, Firma, Abteilung, Branche, Land, Strasse, Hausnummer, PLZ, Stadt, Werbemassnahmen) " +
                "VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');";
		
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
                userData.get("Werbemassnahmen").getText());
	}
	
	private String generateUpdateQuery() {
		String query = "UPDATE prospects SET " +
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
                userData.get("id").getText());
	}

}
