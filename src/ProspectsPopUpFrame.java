import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProspectsPopUpFrame extends JFrame implements ActionListener {

private static final long serialVersionUID = 1L;
private String[] colNames = TableHeaders.getJTableHeaders();
private HashMap<String, JTextField> prospectData;
private JFrame frame;
private JFrame parentFrame;
private HashMap<String, String> rowData;
private boolean editUser;
private int selectedRow;

		
	public ProspectsPopUpFrame(JFrame parentFrame) {
		// Frame wurde geöffnet, um einen neuen Interessenten hinzuzufügen
		editUser = false;
		this.parentFrame = parentFrame;
		setUpFrame();
	}
	
	public ProspectsPopUpFrame(JFrame parentFrame, HashMap<String, String> rowData, int selectedRow) {
		// Frame wurde geöffnet, um einen existierenden Interessenten zu bearbeiten
		// rowData: Die Interessentendaten (wurden aus dem JTable gelesen)
		// selectedRow: Die im Tabellenmodell ausgewählte Reihe
		editUser = true;
		this.parentFrame = parentFrame;
		this.rowData = rowData;
		this.selectedRow = selectedRow;
		setUpFrame();
	}
	
	private void setUpFrame() {
		// prospectData: HashMap, bei der jede Spaltenüberschrift aus der Tabelle mit einem JTextfield verbunden wird.
		// Mir Hilfe dieser Map können die Usereingaben zu den verschiedenen Spalten eingegeben werden
		prospectData = new HashMap<>();
		
		// Frame aufsetzen und Grundeigenschaften festlegen
		frame = new JFrame();
		frame.setTitle("Interessenteninformation");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE); 
		frame.setResizable(false);
		frame.setLayout(new BorderLayout(10, 10));
		
		// Location setzen: Relativ zum MainFrame --> Dieser soll der parentFrame sein und wurde als Parameter übergeben
		frame.setLocationRelativeTo(parentFrame);
		
		// Icon hinzufügen
		ImageIcon image = new ImageIcon("MainIcon.png");
		frame.setIconImage(image.getImage());
		
		// JPanel für User Input erstellen
		JPanel userDataInputPanel = new JPanel(new GridLayout(0, 3, 15, 15));
		userDataInputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
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
		/* Input-Textfelder mit Überschriften zum Panel für den userInput hinzufügen */
		
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
			
			// Aktuellen Eintrag im prospectData dictionary abspeichern, Key: Spaltenüberschrift, Value: Das dazugehörige Textfeld
			prospectData.put(colNames[i], inputField);
			
			// Label mit Überchrift und Textfeld zum itemPanel hinzufügen
			itemPanel.add(new JLabel(colNames[i]));
			itemPanel.add(prospectData.get(colNames[i]));
			
			// itemPanel mit Überschrift und Textfeld zum userDataInputPanel hinzufügen
			userDataInputPanel.add(itemPanel);				
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		
		switch (command) {
		case "delete":
			deleteProspect();
			break;
		case "save":
			// Checken ob der User-Input valide ist. Wenn nicht: return
			if (!checkUserInputValidity()) {
				return;
			}

			// Values aus den Testfeldern im prospectData dict in Array speichern --> Größe: colCount - 1, weil die ID nicht ins Array soll
			int colCount = TableHeaders.getColCount() - 1;
			String[] prospectDataString = new String[colCount];
			for (int i = 0; i < colCount; i++) {
				prospectDataString[i] = prospectData.get(TableHeaders.getJTableColNameByColIndex(i + 1)).getText();
			}
			
			if (editUser) {
				// Existierenden Interessenten updaten --> Hierzu brauchen wir die UserID
				int userID = Integer.parseInt(prospectData.get(TableHeaders.getDBColNameByColIndex(0)).getText()); // 0: ID
				Database.updateExistingProspect(prospectDataString, selectedRow, userID);
			}
			else {
				// Neuen Interessenten hinzufügen
				Database.insertNewProspect(prospectDataString);
			}
			break;
		}
		
		// Frame schließen
		frame.dispose();
	}

	private boolean checkUserInputValidity() {
		/* Validität des User-Inputs checken */
		
		// Der Input ist nicht valide, wenn keine Interessentendaten eigegeben wurden
		int emptyTextFields = 0;
		
		// Wir starten bei 1, weil die ID automatisch generiert wird und nicht vom User eingegeben werden kann
		for (int i = 1; i < colNames.length; i++) {
			if (prospectData.get(colNames[i]).getText().equals("")) {
				emptyTextFields++;
			}
		}
		
		// Sind alle editierbaren Textfelder leer? --> PopUpNachricht anzeigen und return
		if (emptyTextFields == colNames.length - 1) {
			JOptionPane.showMessageDialog(frame, "Bitte geben Sie Interessentendaten ein", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		// Der Input im InputField "Priorität" ist nur dann valide, wenn ein int zwischen 1 und 5 eingeben wurde
		String priorityString = prospectData.get(TableHeaders.getJTableColNameByColIndex(1)).getText(); // 1: Priorität
		if (!priorityString.equals("")) {
			try {
				int priorityInt = Integer.parseInt(priorityString);
				if (priorityInt < 1 || priorityInt > 5) {
					throw new NumberFormatException();
				}
			}
			catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(frame, "Priorität muss eine Ganzzahl zwischen 1 und 5 sein", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		// Wenn der User gar keine Priorität eingegben hat, wird sie automatisch auf 1 gesetzt
		else {
			prospectData.get(TableHeaders.getJTableColNameByColIndex(1)).setText("1");
		}
		
		// Wenn eine Erinnerung gesetzt wurde, muss sie ein Zeitformat haben.
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		String dateString = prospectData.get(TableHeaders.getJTableColNameByColIndex(17)).getText(); // 17: Erinnerung
		
		// Hat der User etwas in das Feld "Erinnerung" eingetragen? --> Wenn ja...
		if (!dateString.equals("")) {
			try {
				if (dateString.length() != 10) {
					throw new Exception();
				}				
				Date date = dateFormat.parse(dateString);
				
				// Das Erinnerungsdatum muss in der Zukunft liegen
				Date currentDate = Calendar.getInstance().getTime();
				if (!date.after(currentDate)) {
					JOptionPane.showMessageDialog(frame, "Erinnerungsdatum muss in der Zukunft liegen", "Error", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(frame, "Erinnerung bitte im Format TT.MM.JJJJ eintragen", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		
		return true;
	}
	
	private void deleteProspect() {
		// Prospect aus DB löschen
		int userID = Integer.parseInt(prospectData.get(TableHeaders.getDBColNameByColIndex(0)).getText());
		Database.deleteProspectFromDB(userID);
		
		// Reihe aus dem JTable löschen
		MyTableModel.deleteRow(selectedRow);
	}

}
