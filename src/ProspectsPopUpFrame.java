import java.awt.EventQueue;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.RowSet;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

public class ProspectsPopUpFrame extends JFrame implements ActionListener {

private static final long serialVersionUID = 1L;
private String[] colNames;
private HashMap<String, JTextField> userData;
private JFrame frame;
private Statement statement;
private JTextField[] dataFields;

		
	public ProspectsPopUpFrame(String[] colNames) {
		Database base = new Database();
		base.connectToDatabase();
		this.statement = base.getStatement();
		
		this.colNames = colNames;
		this.userData = new HashMap<>();
		this.frame = new JFrame();
		
		frame.setTitle("Interessenteninformation");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
		frame.setResizable(false);
		frame.setLayout(new BorderLayout(10, 10));
		
		ImageIcon image = new ImageIcon("MainIcon.png");
		frame.setIconImage(image.getImage());
		
		
		JPanel userDataInputPanel = new JPanel(new GridLayout(0, 3, 15, 15));
		userDataInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		// Eingabefelder zur Usereingabe hinzufügen
		addInputFields(userDataInputPanel);
		
		JPanel actionPanel = new JPanel();
		JButton saveBtn = new JButton("Speichern");
		JButton abortBtn = new JButton("Abbrechen");
		saveBtn.setActionCommand("save");
		abortBtn.setActionCommand("abort");
		saveBtn.addActionListener(this);
		abortBtn.addActionListener(this);
		actionPanel.add(saveBtn);
		actionPanel.add(abortBtn);
		actionPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
		
		
		
		
		frame.add(actionPanel, BorderLayout.SOUTH);
		frame.add(userDataInputPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
			
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

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		if (command.equals("abort")) {
			frame.dispose();
		}
		else if (command.equals("save")) {
			String insertQuery = generateInsertQuery();
			try {
				statement.executeUpdate(insertQuery);
				MyTableModel model = MyTableModel.getModel();
				
				// Die soeben hinzugefügte Zeile in der Datenbank auswählen
				String getInsertQuery = "SELECT * FROM prospects ORDER BY id DESC LIMIT 1";
				ResultSet rs = statement.executeQuery(getInsertQuery);
				
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
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			frame.dispose();
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

}
