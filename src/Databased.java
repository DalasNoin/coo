
import java.sql.*;

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
    private Connection openConnection;
}
