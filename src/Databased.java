
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.driver.OracleDriver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author simon
 */
public class Databased {
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
        
        Scanner s = new Scanner(System.in);
        String in;
        while(!(in = s.nextLine()).equals("close")){
            System.out.println(in);
            if(in.equals("s")){
                String st = s.nextLine();
                executeStatement(st);
            }
            else
                executeQuery(s.nextLine());
        }

        closeConnection();
    }
}
