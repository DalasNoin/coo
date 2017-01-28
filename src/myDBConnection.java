/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.*;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class myDBConnection {

	
	//Statement and Connection
	private Statement stmt;
	private Connection con;
	
	//replace USER and PWD with your login data (Matrikelnummer)
    private String USER = LoginData.login;
    private String PWD = LoginData.pwd;
    private String URL = LoginData.dbUrl;
    
    //5a)
    public myDBConnection() throws SQLException{
    	
    	//load the oracle driver
    	DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
    	
    	//open the db connection
    	con = DriverManager.getConnection(URL, USER, PWD);
    }
    
    //close the db connection
    public void close() throws SQLException{
    	con.close();
    }
    
    //5b) - CREATE TABLE
    public void createRelations() throws SQLException{
    	
    	//Vorstellung
    	String v = "CREATE TABLE Vorstellung ("
    			 + "Name VARCHAR(100) PRIMARY KEY,"
    			 + "Datum DATE NOT NULL)";
    	
    	//Buchung
    	String b = "CREATE TABLE Buchung ("
    			 + "Name VARCHAR(100) PRIMARY KEY REFERENCES Vorstellung DEFERRABLE,"
    			 + "Sitzplaetze_Max INTEGER NOT NULL,"
    			 + "Sitzplaetze_Gebucht INTEGER)";
    	
    	//cyclical foreign key
    	String fk = "ALTER TABLE Vorstellung ADD CONSTRAINT vorstFK "
    			  + "FOREIGN KEY(Name) REFERENCES Buchung(Name) DEFERRABLE";
    	
    	try{
    		//transaction, set autocommit to "false"
    		con.setAutoCommit(false);
    		stmt = con.createStatement();
    		
    		stmt.executeUpdate(v);
    		stmt.executeUpdate(b);
    		stmt.executeUpdate(fk);
    		
    		con.commit();
    		
    	} catch (SQLException e){
    		
    		con.rollback();
    		throw e;
    	}
    	finally {
    		
    		con.setAutoCommit(true);
        	stmt.close();
        	
    	}
    	
    }
    
    //5c)
    //INSERT VALUES
    //java.sql.Date is differ to java.util.Date
    public void insertValues(String Name, java.sql.Date Datum, int Sitzplaetze_Max, int Sitzplaetze_Gebucht) throws SQLException{
    	
    	//PreparedStatement for "INSERT INTO Vorstellung"
    	PreparedStatement insV = con.prepareStatement("INSERT INTO Vorstellung VALUES (?, ?)");
    	
    	insV.setString(1, Name);
    	insV.setDate(2, Datum);
    	
    	//Statement for "INSERT INTO Buchung"
    	String insB = "INSERT INTO Buchung VALUES('" + Name + "', " + Sitzplaetze_Max + ", " + Sitzplaetze_Gebucht + ")";
    	
    	try{
    		//transaction
    		con.setAutoCommit(false);
    		stmt = con.createStatement();
    		
    		//set CONSTRAINT to DEFERRED
    		stmt.executeUpdate("SET CONSTRAINTS ALL DEFERRED");
    		
    		insV.executeUpdate();
    		stmt.executeUpdate(insB);
    		
    		//set CONSTRAINTS to default
    		stmt.executeUpdate("SET CONSTRAINTS ALL IMMEDIATE");
    		
    		//end the transaction
    		con.commit();
    		
    	} catch (SQLException e){
    		
    		con.rollback();
    		throw e;
    	}
    	finally{
    		con.setAutoCommit(true);
        	stmt.close();
    	}
    }
    
    //6a) alle Vorstellungsnamen ausgeben
    public ArrayList<String> getShowNames() throws SQLException{
    	
    	String q = "SELECT Name FROM Vorstellung";
    	ResultSet rs;
    	
    	//createStatement
    	stmt = con.createStatement();
    	rs = stmt.executeQuery(q);

    	ArrayList<String> names = new ArrayList<String>();

    	while(rs.next()){
    		names.add(rs.getString("Name"));
    	}
    	
    	//close all
    	rs.close();
    	stmt.close();
    	
    	return names;
    }
    
    //6b)
    //compute the number of free places
    public int getFreeSeats(String name) throws SQLException {
        ResultSet rs;
        
        //freie Plaetze = Sitzplaetze_Max - Sitzplaetze_Gebucht
        String q = "SELECT Sitzplaetze_Max, Sitzplaetze_Gebucht FROM Buchung WHERE Name = '" + name + "'";
        int seats_max = 0;  
 	    int seats_booked = 0; 
                       
        stmt = con.createStatement();
        rs = stmt.executeQuery(q);
       
        if(rs.next()) {
            seats_max = rs.getInt("Sitzplaetze_Max");
            seats_booked = rs.getInt("Sitzplaetze_Gebucht");            
        }
        
        rs.close();
        stmt.close();
        
        return seats_max - seats_booked;                   
    }
    
    //6c
    //book the seats
    public boolean bookSeats(String v_name, int nr) throws SQLException {
        
        try {
            //con.setAutoCommit(false);
            
            //there is no enough free seats
            if(getFreeSeats(v_name) < nr){
            	return false;
            }
            else{
            	// update statement
                String s = "UPDATE Buchung SET Sitzplaetze_Gebucht = " + nr + " + (SELECT Sitzplaetze_Gebucht "
                         + "FROM Buchung WHERE Name = '" + v_name + "')"  + " WHERE Name = '" + v_name + "'";
                stmt = con.createStatement();
                stmt.executeUpdate(s);
                
              //  con.commit();
            }
                       
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } 
        finally{
        	
        	con.setAutoCommit(true);
            stmt.close();
        }
        
        return true;
    }
    
    //6d)
    //give the date 
    public java.sql.Date getDate(String name) throws SQLException {
        ResultSet rs;
        
        String s = "SELECT Datum FROM Vorstellung WHERE Name='" + name + "'";
        java.sql.Date data = null;
        
        stmt = con.createStatement();
        rs = stmt.executeQuery(s);
         
        if(rs.next()) {
            data = rs.getDate("Datum");          
        }
        
        rs.close();
        stmt.close();
        
        return data;
    }
    
/************************************************************************** Blatt 8 *************************************************************************************/
    
    //3a) Anzahl der Sitzplaetze aendern
    public void setSeatNumber(int number) throws SQLException{
    	
    	String q = "SELECT Sitzplaetze_Max FROM Buchung";
    	ResultSet rs;
    	
    	//createStatement with TYPE_SCROLL_SENSITIVE and CONCURE_UPDATABLE
    	stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	
    	rs = stmt.executeQuery(q);
    	rs.last();
    	int plaetze = rs.getInt("Sitzplaetze_Max");
    	rs.updateInt("Sitzplaetze_Max", plaetze+number);
    	rs.updateRow();
    	
    	rs.close();
    	stmt.close();
    }
    
    //3b) Tupel loeschen
    public void deleteTupel(int number) throws SQLException{  
    	
    	//Es darf kein SELECT * verwendet werden, nur Auflisten von Attributen funktioniert!!!
    	String q1 = "SELECT Name, Datum FROM Vorstellung";
    	String q2 = "SELECT Name, Sitzplaetze_Max, Sitzplaetze_Gebucht FROM Buchung";
    	ResultSet rs1;
    	ResultSet rs2;
    	
    	con.setAutoCommit(false);
    	stmt = con.createStatement();
    	//createStatement with TYPE_SCROLL_SENSITIVE and CONCURE_UPDATABLE
    	Statement stmt1 = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	Statement stmt2 = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	
    	//set CONSTRAINT to DEFERRED
    	stmt.executeUpdate("SET CONSTRAINTS ALL DEFERRED");
    	
    	rs1 = stmt1.executeQuery(q1);
    	rs2 = stmt2.executeQuery(q2);
    	
    	rs1.absolute(number);
    	rs2.absolute(number);
    	
    	rs1.deleteRow();
	    rs2.deleteRow();
		
		//set CONSTRAINTS to default
		stmt.executeUpdate("SET CONSTRAINTS ALL IMMEDIATE");
		
		//end the transaction
		con.commit();
		rs1.close();
		rs2.close();
		
		stmt.close();
		stmt1.close();
		stmt2.close();
    }
    
    //3c) Cursor updatable?
    public boolean isCursorUpdatable() throws SQLException{
    	
    	String q = "SELECT v.Name, Datum, b.Name, Sitzplaetze_Max, Sitzplaetze_Gebucht "
    			 + "FROM Vorstellung v, Buchung b "
    			 + "WHERE v.Name = b.Name";
    	
    	ResultSet rs;
    	
    	//createStatement with TYPE_SCROLL_INSENSITIVE and CONCURE_READ_ONLY
    	stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    	rs = stmt.executeQuery(q);
    	
    	boolean isUpdatable = (rs.getConcurrency() == ResultSet.CONCUR_UPDATABLE);
    	 	
    	//close all
    	rs.close();
    	stmt.close();
    	
    	return isUpdatable;
    }
/************************************************************************************************************************************************************************/
    /*
    public static void main(String args[]){
    	
    	try{
    		myDBConnection dbcon = new myDBConnection();
    		
    		//5d)
    		dbcon.createRelations();
    		
    		//insert data 
    		//Achtung! Die Monate in Java werden von 0 bis 11 gezaehlt.
    		dbcon.insertValues("Schneewittchen", new java.sql.Date(new GregorianCalendar(2005, 10, 23).getTimeInMillis()), 80, 23);
    		dbcon.insertValues("Aschenputtel", new java.sql.Date(new GregorianCalendar(2006, 00, 06).getTimeInMillis()), 100, 20);
            dbcon.insertValues("Dornroeschen", new java.sql.Date(new GregorianCalendar(2006, 00, 11).getTimeInMillis()), 60, 30);
            dbcon.insertValues("DB1-Klausur", new java.sql.Date(new GregorianCalendar(2017, 01, 07).getTimeInMillis()), 244, 0);
            
            //6a)
            ArrayList<String> s = dbcon.getShowNames();
            
            System.out.println("Namen der Vorstellungen:");
            for(int i = 0; i < s.size(); i++){
            	
            	System.out.println(s.get(i));
            }
            
            
            //6b)
            System.out.println("Anzahl der freien Sitzplaetze fuer Dornroeschen = " + dbcon.getFreeSeats("Dornroeschen"));
            
            //6c)
            if(!dbcon.bookSeats("Schneewittchen", 2)){
            	System.out.println("Anzahl freier Sitzplaetze ueberschritten");
            }
                
            //6d)
            java.sql.Date data = dbcon.getDate("DB1-Klausur");
            System.out.println("Datum DB1-Klausur: " + data.toString());
            
            //3a)
            dbcon.setSeatNumber(6);
            
            //3b)
            dbcon.deleteTupel(2);
            
           
            System.out.println(dbcon.isCursorUpdatable());
            
    	} catch(SQLException e) {
            System.out.println("error: " + e.getMessage());
            e.printStackTrace();
        }   
    }    */
}

