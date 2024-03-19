import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel {


	private static final long serialVersionUID = 1L;
	private static MyTableModel model;

    // Static initialization block to create the shared instance
    static {
        model = new MyTableModel();
    }

    // Private constructor to prevent external instantiation
    private MyTableModel() {
        super();
    }

    // Static method to access the shared instance
    public static MyTableModel getModel() {
        return model;
    }

}
