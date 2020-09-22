/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author ASUS
 */
public class JavaApplication5 {

    /**
     * @param args the command line arguments
     */
    
    public static Connection getConnection(String ip_address, String port_number, 
            String instanceName, String databaseName, String username, String password) {
        Connection conn = null;
        try {
            //1. Load Driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            //2. Create String
            String url = String.format("jdbc:sqlserver://%s:%s;databaseName=%s;instanceName=%s", ip_address, port_number, databaseName, instanceName);
            //3. Connect Database
            conn = DriverManager.getConnection(url, username, password);
            
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Something is wrong in your url!");
            System.out.println("Please restart the program and re-enter the components!");
        }
        return conn;
    }
    
    public static void testRandom(String ip_address,String port_number,String instanceName,String databaseName,String username,String password) {
        try {
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                while (true) {
                    int randomNum1 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    int randomNum2 = ThreadLocalRandom.current().nextInt(1, 50 + 1);
                    int randomNum3 = ThreadLocalRandom.current().nextInt(1, 10 + 1);

                    String sql1 = "INSERT INTO Users (id, name, password, mail) " +
                        "VALUES ('7', " + randomNum1 + ", " + randomNum2 + ", " + randomNum3 +")";
                    String sql3 = "UPDATE Users SET password = " + randomNum1 + " WHERE id = '7'";
                    String sql2 = "DELETE FROM Users WHERE id = '7'";

                    PreparedStatement statement1 = conn.prepareStatement(sql1);
                    PreparedStatement statement2 = conn.prepareStatement(sql2);
                    PreparedStatement statement3 = conn.prepareStatement(sql3);

                    statement1.executeUpdate();
                    statement1.executeUpdate();
                    statement1.executeUpdate();
                    statement2.executeUpdate();
                    statement3.executeUpdate();
                    
                    TimeUnit.SECONDS.sleep(5); 
                }
            } else {
                System.out.println("Couldn't connect to the Database!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    public static void main(String[] args) {
        // TODO code application logic here
        String ip_address = new String("localhost");
        String port_number = new String("1433");
        String instanceName = new String("MSSQLSERVER");
        String databaseName = new String("sampledb");
        String username = new String("sa");
        String password = new String("123456");
        
        try {
            testRandom(ip_address, port_number, instanceName, databaseName, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
