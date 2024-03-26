import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	private static Connection connect;
	private static Statement statement;
	private static PreparedStatement preparedStatement;
	
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
	
	public static Connection getConnection() {
		// Falls aus irgendeinem Grund keine Verbindung mehr zur DB besteht --> Wiederherstellen, dann connect zurückgeben
		connectToDatabase();
		return connect;
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
	
	////////////////////////////////////////////////////////////////////////////
	public static PreparedStatement createPreparedStatement(String query) {
		try {
        	// Wenn Verbindung zur DB besteht: Statement Instanz erstellen
            if (connect != null) {
                preparedStatement = connect.prepareStatement(query);
                return preparedStatement;
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
	
	
	public static void closeResultSetAndStatement(ResultSet rs) {
		closeResultSet(rs);
		closeStatement();
	}

}
