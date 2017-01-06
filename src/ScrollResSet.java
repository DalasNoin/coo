import java.sql.*;
import oracle.jdbc.driver.*;

public class ScrollResSet {
	
	private static String dbUrl=LoginData.dbUrl; 
	//set user
	private static String login = LoginData.login; //your username
	//set password
	private static String pwd = LoginData.pwd; //your password
	private static Connection openConnection;

	private static boolean establishConnection(){
		//load driver
		try{
			DriverManager.registerDriver(new OracleDriver());
			openConnection = DriverManager.getConnection(dbUrl,login,pwd);
			return !openConnection.isClosed();
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean executeStatement(String statement){
		Statement stmt;
		try {
			stmt = openConnection.createStatement();
			stmt.executeUpdate(statement);
			return true;
		} 
		catch (SQLException e){
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean executeQuery(String query) throws SQLException{
		Statement stmt;
		try {
			stmt = openConnection.createStatement();
			ResultSet result = stmt.executeQuery(query);
			displayResults(result);
			return true;
		} 
		catch (SQLException e){
			e.printStackTrace();
			return false;
		}
	}
	
	private static void displayResults(ResultSet result){
		try {
			int cols = result.getMetaData().getColumnCount();
			while(result.next()){
				String row = new String(" | ");
				for(int i=1;i<=cols;i++){
					row = row.concat(result.getString(i)+ " | ");
				}
				System.out.println(row);
			}
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	private static boolean closeConnection(){
		try{
			openConnection.close();
			openConnection = null;
			return true;
		}
		catch(SQLException e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args){
		
	   establishConnection();
	   
	   String create = "CREATE TABLE inventory ("+
						"partno INTEGER PRIMARY KEY, "+
						"name VARCHAR(100), "+
						"qonhand INTEGER )";
		//create the relation
		executeStatement(create);		

		String insert = "INSERT INTO inventory VALUES"+
						"(300, 'SCREW', 40)";
		String insert2 = "INSERT INTO inventory VALUES"+
				         "(317, 'SCREW2', 32)";
		//insert tuple into relation
		executeStatement(insert);
		executeStatement(insert2);
			
		String getData = "SELECT * FROM inventory";
		//show the data
		try {
			executeQuery(getData);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String drop = "DROP TABLE inventory";
		//drop the relation
		executeStatement(drop);
				
		//close the connection
		closeConnection();
	}

}