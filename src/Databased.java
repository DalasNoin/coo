
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private String pwd = LoginData.pwd;
    private String login = LoginData.login;
    private String dbUrl = LoginData.dbUrl;
    
    private Statement stmt;
    private Connection openConnection = null; 
    
    public static void main(String[] args) throws SQLException{
        Databased base = new Databased();
        String q = "Select * From Vorstellung";
        for(String s : base.executeQuery(q))
            System.out.println(s);
        base.close();
    }
    
    public Databased(){
        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
            openConnection = DriverManager.getConnection(dbUrl, login, pwd);
        } catch (SQLException ex) {
            Logger.getLogger(Databased.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public void execute(String s) throws SQLException{
        stmt = openConnection.createStatement();
        stmt.executeUpdate(s);
    }
    
    public ArrayList<String> executeQuery(String q) throws SQLException{
        ArrayList<String> s = new ArrayList<>();
        
        ResultSet rs;
        stmt = openConnection.createStatement();
        rs = stmt.executeQuery(q);
        
        ResultSetMetaData md = rs.getMetaData();
        while (rs.next())
        {   
            String row ="";
            for(int i = 1;i<=md.getColumnCount();i++){
                 row += rs.getString(md.getColumnName(i))+" ";
            }
           s.add(row);
        }

        return s;
    }
    
    public void close(){
        if (openConnection == null)
            return;
        try {
            openConnection.close();
        } catch (SQLException ex) {
            Logger.getLogger(Databased.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
