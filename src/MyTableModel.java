import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel {


	private static final long serialVersionUID = 1L;
	private static MyTableModel model;

    // Statische Initialisierung, sobald das Programm startet
    static {
        model = new MyTableModel();
        //model = new DefaultTableModel();
    }

    // Privater Konstruktor, der nur innerhalb dieser Klasse aufgerufen werden kann
    private MyTableModel() {
        //super();
    	new DefaultTableModel();
    }

    // Das Tabellenmodell kann von jeder anderen Klasse abgerufen werden
    public static MyTableModel getModel() {
        return model;
    }
    
    public static void makeTableUneditable() {
    	int rowCount = model.getRowCount();
    	int colCount = model.getColumnCount();
    	
    	for (int row = 0; row < rowCount; row++) {
    		for (int col = 0; col < colCount; col++) {
    			model.isCellEditable(row, 0);
    		}
    	}
    }
    
    public static void makeInsertedRowUneditable() {
    	int row = model.getRowCount() - 1;
    	int colCount = model.getColumnCount();
    	
    	for (int col = 0; col < colCount; col++) {
			model.isCellEditable(row, col);
		}
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
       // Macht eine Zelle uneditierbar
       return false;
    }

}
