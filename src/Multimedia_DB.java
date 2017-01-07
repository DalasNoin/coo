
import java.io.File;
import java.io.FileInputStream;
import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.driver.*;

public class Multimedia_DB {

    private static String dbUrl = LoginData.dbUrl;
    private static String login = LoginData.login;
    private static String pwd = LoginData.pwd;

    private static Connection openConnection;

    private static String path;

    private static boolean establishConnection() {
        // load driver
        try {
            DriverManager.registerDriver(new OracleDriver());
            openConnection = DriverManager.getConnection(dbUrl, login, pwd);
            return !openConnection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean executeStatement(String statement) {
        Statement stmt;
        try {
            stmt = openConnection.createStatement();
            stmt.executeUpdate(statement);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean executeQuery(String query) {
        Statement stmt;
        try {
            stmt = openConnection.createStatement();
            ResultSet result = stmt.executeQuery(query);
            displayResults(result);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void displayResults(ResultSet result) {
        try {
            int cols = result.getMetaData().getColumnCount();
            while (result.next()) {
                String row = new String(" | ");
                for (int i = 1; i <= cols; i++) {
                    row = row.concat(result.getString(i) + " | ");
                }
                System.out.println(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean closeConnection() {
        try {
            openConnection.close();
            openConnection = null;
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        establishConnection();

        path = "C:\\Users\\simon\\Desktop\\Uni\\Datenbanken\\papers\\";
        String[] names = new String[]{"key_de.pdf", "key_es.pdf", "key_fr.pdf"};

        String statement = "Create Table Papers("
                + "id     Integer     Primary Key,"
                + "title  Char(*),"
                + "pdf    BLOB"
                + ");";
        
        executeStatement(statement);
        
        String title;
        
        for(int i = 0 ; i<names.length;i++){
            title=names[i];
            statement = "Insert Into papers Values(id, title, pdf)"+
                    "("+i+","+title+",EMPTY_BLOB())";
            executeStatement(statement);
        }
        
        String query = "";

        closeConnection();
    }
}
