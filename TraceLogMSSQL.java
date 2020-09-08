/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tracelogmssql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.Scanner;

/**
 *
 * @author ASUS
 */
public class TraceLogMSSQL {
    
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
    
    
    public static boolean createTable(String path, String ip_address, String port_number, 
            String instanceName, String databaseName, String username, String password) {
        try {
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                System.out.println("Connected to the Database!");
                
                //Drop existing table
                String drop_sql = new String("IF OBJECT_ID('MyTraceTable', 'U') IS NOT NULL DROP TABLE MyTraceTable");
                PreparedStatement drop_statement = conn.prepareStatement(drop_sql);
                drop_statement.executeUpdate();
                //
                
                String sql = String.format("SELECT * INTO MyTraceTable FROM fn_trace_gettable('%s', DEFAULT)", path);
                PreparedStatement statement = conn.prepareStatement(sql);
                
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted == 0) {
                    System.out.println("Query executed but no changes were made!");
                    return false;
                } else if (rowsInserted > 0) {
                    System.out.println("Table created successfully!");
                    return true;
                }
            } else {
                System.out.println("Couldn't connect to the Database!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something doesn't seem right in your query!");
        }
        return false;
    }
    
    
    public static void searchTable(String ip_address, String port_number, 
            String instanceName, String databaseName, String username, String password) {
        try {
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                System.out.println("Connected to the Database!");
                
                String sql = "SELECT * FROM MyTraceTable";
                
                Statement statement = conn.createStatement();
                ResultSet result = statement.executeQuery(sql);
                
                ResultSetMetaData rsmd = result.getMetaData();
                int columns_number = rsmd.getColumnCount();
                
                while (result.next()) {
                    for (int i = 1; i <= columns_number; i++) {
                        System.out.print(result.getString(i) + " ");
                    }
                    System.out.println();
                }
                
            } else {
                System.out.println("Coulnd't connect to the Database!");
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
        System.out.print("Enter Database Name (blank for \'master\'): ");
        String databaseName = new String(sc.nextLine());
        if (databaseName.equals("")) {
            databaseName = "master";
        }
        //Enter username
        System.out.print("Enter Username (blank for \'sa\'): ");
        String username = new String(sc.nextLine());
        if (username.equals("")) {
            username = "sa";
        }
        //Enter password
        System.out.print("Enter Password (blank for \'******\'): ");
        String password = new String(sc.nextLine());
        if (password.equals("")) {
            password = "123456";
        }
        
        //Enter folder_path
        System.out.print("Enter the path of the file (blank for default):");
        String folder_path = sc.nextLine();
        if (folder_path.equals("")) {
            folder_path = "C:\\Program Files\\Microsoft SQL Server\\MSSQL15.MSSQLSERVER\\MSSQL\\Log";
        }
        
        char key_input = 'Y';
        while (key_input == 'Y') {
            //Enter file_name
            System.out.print("Enter file name (blank for \'log_10.trc\'):");
            String file_name = sc.nextLine();
            if (file_name.equals("")) {
//                file_name = "ERRORLOG";
                file_name = "log_10.trc";
            }
            
            //Create full path
            String path = new String((folder_path + "\\" + file_name));
            
            System.out.println(path);
            
            try {
                if (createTable(path, ip_address, port_number, instanceName, databaseName, username, password)) {
                    searchTable(ip_address, port_number, instanceName, databaseName, username, password);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            System.out.print("Do you want to continue the program on that folder? (Y/N): ");
            key_input = sc.nextLine().toUpperCase().trim().charAt(0);
            System.out.println("");
        }
    }
    
}
