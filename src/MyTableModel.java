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

}
