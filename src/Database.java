import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	private Statement statement;
	
	public void connectToDatabase() {
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

	public Statement getStatement() {
		return statement;
	}
}
