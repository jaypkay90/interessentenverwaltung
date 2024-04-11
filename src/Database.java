import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	
	public static void closePreparedStatement(PreparedStatement prep) {
		if (prep != null) {
			try {
				prep.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
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
	
	public static void closeResultSetAndPreparedStatement(ResultSet rs, PreparedStatement prep) {
		closeResultSet(rs);
		closePreparedStatement(prep);
	}
	
	public static void updateExistingProspect(String[] prospectData, int selectedRow, int userID) {
		// Existierenden Interessenten updaten
		String query = TableHeaders.getUpdateQueryHeadersString();
		updateDatabaseTable(query, prospectData, userID, true);
		MyTableModel.updateExistingProspectInJTable(selectedRow, userID);
	}
	
	public static void insertNewProspect(String[] prospectData) {
		// Neuen Interessenten hinzufügen
		String colNamesStr = TableHeaders.getInsertQueryHeadersString();
		String query = String.format("INSERT INTO prospects (%s) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", colNamesStr);
		updateDatabaseTable(query, prospectData, -1, false);
		MyTableModel.addNewProspectToJTable();
	}
	
	private static void updateDatabaseTable(String query, String[] values, int userID, boolean editUser) {
		PreparedStatement prep = null;
		try {
			prep = connect.prepareStatement(query);
			
			prep.setInt(1, Integer.parseInt(values[0]));
			prep.setString(2, values[1]);
			prep.setString(3, values[2]);
			prep.setString(4, values[3]);
			prep.setString(5, values[4]);
			prep.setString(6, values[5]);
			prep.setString(7, values[6]);
			prep.setString(8, values[7]);
			prep.setString(9, values[8]);
			prep.setString(10, values[9]);
			prep.setString(11, values[10]);
			prep.setString(12, values[11]);
			prep.setString(13, values[12]);
			prep.setString(14, values[13]);
			prep.setString(15, values[14]);
			prep.setString(16, values[15]);
			
			// Erinnerungsdatum einfügen
			String dateString = values[16];
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
				prep.setInt(18, userID);
			}
			
			prep.executeUpdate();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			closePreparedStatement(prep);
		}
			
	}
	
	public static void deleteProspectFromDB(int userID) {
		PreparedStatement prep = null;
		try {
			// Eintrag aus der Datenbank löschen
			prep = connect.prepareStatement(String.format("DELETE FROM prospects WHERE %s = ?", TableHeaders.getDBColNameByColIndex(0))); // 0: "ID"
			prep.setInt(1, userID);
			prep.executeUpdate();
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		finally {
			closePreparedStatement(prep);
		}
	}

}
