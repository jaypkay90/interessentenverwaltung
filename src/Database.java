import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	private static Connection connect;
	private static Statement statement;
	
	public static void connectToDatabase() {
		// JDBC URL
	    String url = "jdbc:sqlite:prospectsData.db";
	   
	    try {
	    	// Wenn noch keine DB-Verbindung besteht...
	    	if (connect == null || connect.isClosed()) {
	    		// JDBC Treiber initialisieren
	    		Class.forName("org.sqlite.JDBC");
	    		connect = DriverManager.getConnection(url);	        	    		
	    	}
	    } catch (SQLException | ClassNotFoundException e) {
	        e.printStackTrace();
	    }
	}


	public static Statement createStatement() {
        try {
        	// Wenn Verbindung zur DB besteht: Statement Instanz erstellen
            if (connect != null) {
                statement = connect.createStatement();
                return statement;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	
	public static void closeStatement() {
		try {
        	// Wenn Statement Instanz geöffnet: Schließen
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	
	public static void closeResultSet(ResultSet rs) {
		try {
	        if (rs != null) {
	            rs.close();
	        }
	    } catch (SQLException e1) {
	        e1.printStackTrace();
	    }
	}
	
	
	public static void closeConnection() {
		try {
			// Wenn eine Verbindung besteht --> trennen
			if (connect != null) {
				connect.close();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
