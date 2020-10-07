/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Admin
 */
public class JdbcSQLServerConnection {
    
    public static Connection getConnection(String ip_address, String port_number, String instanceName, String databaseName, String username, String password) {
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
    
    public static void searchDB(String query, String params, 
            String ip_address, String port_number, String instanceName, String databaseName, String username, String password) {
        try {
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                System.out.println("Connected to the Database!");
                
                String sql = query;
                
                Statement statement = conn.createStatement();
                ResultSet result = statement.executeQuery(sql);
                
                String[] param_list = null;
                if (!params.equals("*")){
                    param_list = params.split(",");
                } else {
                    ResultSetMetaData rsmd = result.getMetaData();
                    int columns_number = rsmd.getColumnCount();  
          
                    while (result.next()) {      
                        for(int i = 1 ; i <= columns_number; i++){
                              System.out.print(result.getString(i));
                        }
                        System.out.println();       
                    }
                    return;
                }
                
                while (result.next()){
                    for (int i = 1; i <= param_list.length; i++) {
                        System.out.print(result.getString(i));
                    }
                    System.out.println("");
                }
            } else {
                System.out.println("Couldn't connect to the Database!");
                return;
            }
            System.out.println("\nQuery successfully executed!");
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Something doesn't seem right in your query!");            
        }
    }
    
    
    public static void insertDB(String query, 
            String ip_address, String port_number, String instanceName, String databaseName, String username, String password) {
        try {
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                System.out.println("Connected to the Database!");
                
                String sql = query;
 
                PreparedStatement statement = conn.prepareStatement(sql);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted == 0) {
                    System.out.println("Query executed but no changes were made!");
                } else if (rowsInserted > 0) {
                    System.out.println("Query executed successfully!");
                }
            } else {
                System.out.println("Couldn't connect to the Database!");
                return;
            }   
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Something doesn't seem right in your query!");
        }
    }
    
    
    public static void updateDB(String query, 
            String ip_address, String port_number, String instanceName, String databaseName, String username, String password) {
        try {
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                System.out.println("Connected to the Database!");
                
                String sql = query;
 
                PreparedStatement statement = conn.prepareStatement(sql);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("Query executed but no changes were made!");
                } else if (rowsUpdated > 0) {
                    System.out.println("Query executed successfully!");
                }
            } else {
                System.out.println("Couldn't connect to the Database!");
                return;
            }   
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Something doesn't seem right in your query!");
        }
    }
    
    
    public static void deleteDB(String query, 
            String ip_address, String port_number, String instanceName, String databaseName, String username, String password) {
        try {
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                System.out.println("Connected to the Database!");
                
                String sql = query;
 
                PreparedStatement statement = conn.prepareStatement(sql);

                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted == 0) {
                    System.out.println("Query executed but no changes were made!");
                } else if (rowsDeleted > 0) {
                    System.out.println("Query executed successfully!");
                }
            } else {
                System.out.println("Couldn't connect to the Database!");
                return;
            }   
        } catch (Exception e) {
//            e.printStackTrace();
            System.out.println("Something doesn't seem right in your query!");
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner sc = new Scanner(System.in);
        
        //Enter ip_address
        System.out.print("Enter IP Address/Domain Name (blank for localhost): ");
        String ip_address = new String(sc.nextLine());
        if (ip_address.equals("")) {
            ip_address = "localhost";
        }
        //Enter port_number
        System.out.print("Enter Port Number (blank for \'1433\'): ");
        String port_number = new String(sc.nextLine());
        if (port_number.equals("")) {
            port_number = "1433";
        }
        //Enter instanceName
        System.out.print("Enter Instance Name (blank for \'MSSQLSERVER\'): ");
        String instanceName = new String(sc.nextLine());
        if (instanceName.equals("")) {
            instanceName = "MSSQLSERVER";
        }
        //Enter databaseName
        System.out.print("Enter Database Name (blank for \'sampledb\'): ");
        String databaseName = new String(sc.nextLine());
        if (databaseName.equals("")) {
            databaseName = "sampledb";
        }
        //Enter username
        System.out.print("Enter Username (blank for \'sa\'): ");
        String username = new String(sc.nextLine());
        if (username.equals("")) {
            username = "sa";
        }
        //Enter password
        System.out.print("Enter Password (blank for \'123456\'): ");
        String password = new String(sc.nextLine());
        if (password.equals("")) {
            password = "123456";
        }
        
        char key_input = 'Y';
        while (key_input == 'Y') {
            //1. Enter Query
            System.out.println("Enter Query:");
            String query = sc.nextLine().trim();
            //2. Extract Query Type
            String query_type = query.split(" ", 2)[0];
            //3. Determine Query 
            if (query_type.equals("SELECT") || query_type.equals("INSERT") || query_type.equals("UPDATE") || query_type.equals("DELETE")) {
                System.out.println("Executing Query..");
                try {
                    switch (query_type) {
                        case ("SELECT"):
                            String params = "";
                            Pattern pattern = Pattern.compile("SELECT(.*?)FROM", Pattern.DOTALL);
                            Matcher matcher = pattern.matcher(query);
                            if (matcher.find()) {
                                params = matcher.group(1).trim();
                            }
                            searchDB(query, params, ip_address, port_number, instanceName, databaseName, username, password);
                            break;
                        case ("INSERT"):
                            insertDB(query, ip_address, port_number, instanceName, databaseName, username, password);
                            break;
                        case ("UPDATE"):
                            updateDB(query, ip_address, port_number, instanceName, databaseName, username, password);
                            break;
                        case ("DELETE"):
                            deleteDB(query, ip_address, port_number, instanceName, databaseName, username, password);
                            break;
                    }
                } catch (Exception e) {
//                    e.printStackTrace();
                    System.out.println("Something is wrong in your query! Please re-check!");
                }
            } else {
                System.out.println("Failed to execute!\nOnly SELECT/INSERT/UPDATE/DELETE queries supported.");
                System.out.println("Your entered command: " + query_type);
            }
            
            System.out.print("Do you want to continue the program (Y/N): ");
            key_input = sc.nextLine().toUpperCase().trim().charAt(0);
            System.out.println("");
        }
    }
    
}
