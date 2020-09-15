/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tracelogmssql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;
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
                    System.out.println("Table created successfully!\n");
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
//                System.out.println("Connected to the Database!");
                
                String sql = "SELECT * FROM MyTraceTable ORDER BY StartTime ASC";
                
                Statement statement = conn.createStatement();
                ResultSet result = statement.executeQuery(sql);
                
                final Object[][] table = new String[4][];
                table[0] = new String[] { "StartTime", "TextData", "LoginName", "ApplicationName" };
                
                int row_index = 1; 
                while (result.next()) {
                    if (result.getString("TextData") != null) {     
                        String test = result.getString("TextData").trim();
                        if ( (test.contains("INSERT") || test.contains("UPDATE") || test.contains("DELETE") ) 
                                && (result.getString("EventClass").equals("13")) ) { 
                            System.out.println(test + " (" + result.getString("StartTime") + ")\n");
                        }     
                    }                       
                }
                
            } else {
                System.out.println("Coulnd't connect to the Database!");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something doesn't seem right in your query!");            
        }
    }
    
    
    public static void initMenu(String ip_address, String port_number, String instanceName, String databaseName, String username, String password, String folder_path){
        System.out.println("====================");
        System.out.println("ip_address = " + ip_address);
        System.out.println("port_number = " + port_number);
        System.out.println("instanceName = " + instanceName);
        System.out.println("databaseName = " + databaseName);
        System.out.println("username = " + username);
        System.out.println("password = " + password);
        System.out.println("folder_path = " + folder_path);
        System.out.println("====================");
    }
    
    
    public static void printChangeMenu() {
        System.out.println("\nWhich one do you want to change ?");
        System.out.println("1. ip_address");
        System.out.println("2. port_number");
        System.out.println("3. instanceName");
        System.out.println("4. databaseName");
        System.out.println("5. username");
        System.out.println("6. password");
        System.out.println("7. folder_path");
        System.out.print("Insert the number: ");
    }
    
    
    public static File[] list_files(String folder_path){      
        File[] files = null;
        try {
            File dir = new File(folder_path);
            files = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".trc");
                }
            });
            for (File file : files) {
                System.out.println(file);  
            }
        } catch (Exception e) {
            e.printStackTrace();
        }      
        return files;
    }
    
    
    public static void writePropertiesFile(String ip_address, String port_number, String instanceName, String databaseName, String username, String password, String folder_path) {
        try {
                Properties properties = new Properties();
                properties.setProperty("ip_address", ip_address);
                properties.setProperty("port_number", port_number);
                properties.setProperty("instanceName", instanceName);
                properties.setProperty("databaseName", databaseName);
                properties.setProperty("username", username);
                properties.setProperty("password", password);
                properties.setProperty("folder_path", folder_path);

                OutputStream output  = new FileOutputStream("./info.properties");
                properties.store(output , "Info Properties");
                
                System.out.println("Components saved at /info.properties!\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static Properties loadPropertiesFile() throws IOException {
        Properties prop = new Properties();

        InputStream input = new FileInputStream("./info.properties");          

        prop.load(input);

        return prop;
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            Scanner sc = new Scanner(System.in);
            
            String ip_address = "localhost";
            String port_number = "1433";
            String instanceName = "MSSQLSERVER"; 
            String databaseName = "master";
            String username = "sa"; 
            String password = "123456";
            String folder_path = "C:\\Program Files\\Microsoft SQL Server\\MSSQL15.MSSQLSERVER\\MSSQL\\Log";
            
            //Load properties file
            try {
                Properties prop = loadPropertiesFile();
                
                ip_address = prop.getProperty("ip_address");
                port_number = prop.getProperty("port_number");
                instanceName = prop.getProperty("instanceName");
                databaseName = prop.getProperty("databaseName");
                username = prop.getProperty("username");
                password = prop.getProperty("password");
                folder_path = prop.getProperty("folder_path");
                
                System.out.println("Properties file loaded!");
            } catch (FileNotFoundException e) {
                System.out.println("No properties file found! Using default properties..");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            initMenu(ip_address, port_number, instanceName, databaseName, username, password, folder_path);

            String key_inputs = new String();
            System.out.print("Do you want to make a change ? (Y/N):");
            key_inputs = sc.nextLine().toUpperCase().trim();
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
                    case '3':
                        //Enter instanceName
                        System.out.print("\nEnter Instance Name (blank for \'MSSQLSERVER\'): ");
                        instanceName = new String(sc.nextLine());
                        if (instanceName.equals("")) {
                            instanceName = "MSSQLSERVER";
                        }
                    case '4':
                        //Enter databaseName
                        System.out.print("\nEnter Database Name (blank for \'master\'): ");
                        databaseName = new String(sc.nextLine());
                        if (databaseName.equals("")) {
                            databaseName = "master";
                        }
                    case '5':
                        //Enter username
                        System.out.print("\nEnter Username (blank for \'sa\'): ");
                        username = new String(sc.nextLine());
                        if (username.equals("")) {
                            username = "sa";
                        }
                    case '6':
                        //Enter password
                        System.out.print("\nEnter Password (blank for \'******\'): ");
                        password = new String(sc.nextLine());
                        if (password.equals("")) {
                            password = "123456";
                        }
                    case '7':
                        //Enter folder_path
                        System.out.print("\nEnter the path of the file (blank for default):");
                        folder_path = sc.nextLine();
                        if (folder_path.equals("")) {
                            folder_path = "C:\\Program Files\\Microsoft SQL Server\\MSSQL15.MSSQLSERVER\\MSSQL\\Log";
                        }
                }

                System.out.println("Successfully updated the component!\n");
                initMenu(ip_address, port_number, instanceName, databaseName, username, password, folder_path);
                System.out.print("Do you still want to make a change ? (Y/N):");
                key_inputs = sc.nextLine().trim();
            } 
            
            key_inputs = "Y";
            while (key_inputs.startsWith("Y")) {

                //List files in folder_path
                System.out.println("\nList of trace files in the folder:");
                File[] files = list_files(folder_path);

                //Enter file_name
                System.out.print("\nEnter target file name (blank to analyze all log files):");
                String file_name = sc.nextLine();
                if (file_name.equals("")) {
                    for (File file : files) {
                        String path = file.toString();
                        
                        if (createTable(path, ip_address, port_number, instanceName, databaseName, username, password)) {
                            searchTable(ip_address, port_number, instanceName, databaseName, username, password);
                        }
                    }
                } else {
                    //Create full path
                    String path = (folder_path + "\\" + file_name);

                    if (createTable(path, ip_address, port_number, instanceName, databaseName, username, password)) {
                        searchTable(ip_address, port_number, instanceName, databaseName, username, password);
                    }
                }                

                System.out.print("Do you want to continue the program on that folder? (Y/N): ");
                key_inputs = sc.nextLine().toUpperCase().trim();
                System.out.println("");
            }

            System.out.print("Do you want to save the components for next time use ? (Y/N): ");
            key_inputs = sc.nextLine().toUpperCase(); 
            if (key_inputs.startsWith("Y")) {
                writePropertiesFile(ip_address, port_number, instanceName, databaseName, username, password, folder_path);         
            }
            System.out.println("Quitting Program.");            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
