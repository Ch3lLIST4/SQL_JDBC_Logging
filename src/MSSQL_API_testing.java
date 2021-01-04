/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package mssql_api_testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;


/**
 *
 * @author ch3l
 */
public class MSSQL_API_testing {
   
    public static final String DATABASE_USER = "user";
    public static final String DATABASE_PASSWORD = "password";
    public static final String MSSQL_RECONNECT = "autoReconnect";
    public static final String MSSQL_MAX_RECONNECTS = "maxReconnects";
    
    /**
     * @param args the command line arguments 
     */
    
    public static Connection getConnection(String ip_address, String port_number,
            String username, String password) throws Exception {
        Connection conn = null;
        
        //1. Load Driver
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        //2. Create String
        String url = String.format("jdbc:sqlserver://%s:%s", ip_address, port_number);
        //3. Create Properties
        java.util.Properties connProperties = new java.util.Properties();
        connProperties.put(DATABASE_USER, username);
        connProperties.put(DATABASE_PASSWORD, password);
        
        connProperties.put(MSSQL_RECONNECT, "true");
        
        connProperties.put(MSSQL_MAX_RECONNECTS, "1");
        //4. Connect Database
        conn = DriverManager.getConnection(url, connProperties);
        
        return conn;
    }
    
    
    public static void initMenu(String ip_address, String port_number, 
            String username, String password, String log_path, String trace_path) {
        System.out.println("====================");
        System.out.println("ip_address = " + ip_address);
        System.out.println("port_number = " + port_number);
        System.out.println("username = " + username);
        System.out.println("password = " + password);
        System.out.println("log_path = " + log_path);
        System.out.println("====================");
    }
    
    
    public static void printChangeMenu() {
        System.out.println("\nWhich one do you want to change ?");
        System.out.println("1. ip_address");
        System.out.println("2. port_number");
        System.out.println("3. username");
        System.out.println("4. password");
        System.out.println("5. log_path");
        System.out.println("6. trace_path");
        System.out.print("Insert the number: ");
    }

    
    public static void main(String[] args) {
        // TODO code application logic here
        String ip_address = "localhost";
        String port_number = "1433";
        String username = "sa";
        String password = "123456";
        String log_path = ".\\tmp\\";
        String trace_path = ".\\traces\\";
        
        try {
            Scanner sc = new Scanner(System.in);
            
            //Load properties file - Not yet
            
            initMenu(ip_address, port_number, username, password, log_path, trace_path);
            
            System.out.print("Do you want to make any change? (Y/N) : ");
            String key_inputs = sc.nextLine().toUpperCase().trim();
            
            while (key_inputs.startsWith("Y")) {
                printChangeMenu();
                key_inputs = sc.nextLine().trim();
                
                char key_input = key_inputs.charAt(0);
                
                switch (key_input) {
                    case '1':
                        //Enter ip_address
                        System.out.print("\nEnter IP Address/Domain Name (blank for localhost): ");
                        ip_address = new String(sc.nextLine());
                        if (ip_address.equals("")) {
                            ip_address = "localhost";
                        }
                        break;
                    case '2':
                        //Enter port_number
                        System.out.print("\nEnter Port Number (blank for \'1433\'): ");
                        port_number = new String(sc.nextLine());
                        if (port_number.equals("")) {
                            port_number = "1433";
                        }
                        break;
                    case '3':
                        //Enter username
                        System.out.print("\nEnter Username (blank for \'sa\'): ");
                        username = new String(sc.nextLine());
                        if (username.equals("")) {
                            username = "sa";
                        }
                        break;
                    case '4':
                        //Enter password
                        System.out.print("\nEnter Password (blank for \'123456\'): ");
                        password = new String(sc.nextLine());
                        if (password.equals("")) {
                            password = "123456";
                        }
                        break;
                    case '5':
                        //Enter log path
                        System.out.println("\nEnter Log Path (blank for .\\\\tmp\\\\): ");
                        log_path = new String(sc.nextLine());
                        if (log_path.equals("")) {
                            log_path = ".\\tmp\\";
                        }
                    case '6':
                        //Enter log path
                        System.out.println("\nEnter Trace Path (blank for .\\\\traces\\\\): ");
                        log_path = new String(sc.nextLine());
                        if (log_path.equals("")) {
                            log_path = ".\\traces\\";
                        }
                }
                
                System.out.println("Successfully updated the component!\n");
                initMenu(ip_address, port_number, username, password, log_path, trace_path);
                System.out.print("Do you still want to make changes ? (Y/N):");
                key_inputs = sc.nextLine().toUpperCase().trim();
            }
            
            // monitor
            
            // connect to the Database
            Connection conn = null;
            try {
                conn = getConnection(ip_address, port_number, username, password);
            } catch (Exception e) {
                System.out.println("Could not connect to the database. Please re-check url");
                System.exit(0);
            }
            if (conn == null) {
                System.exit(0);
            }
            
            System.out.println("");
            
            
        } catch (Exception e) {
        }
    }
    
}
