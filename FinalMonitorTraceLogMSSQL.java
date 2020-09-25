/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalmonitortracelogmssql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
public class FinalMonitorTraceLogMSSQL {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    
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
    
    public static void initMenu(String ip_address, String port_number, 
            String instanceName, String databaseName, String username, String password) {
        System.out.println("====================");
        System.out.println("ip_address = " + ip_address);
        System.out.println("port_number = " + port_number);
        System.out.println("instanceName = " + instanceName);
        System.out.println("databaseName = " + databaseName);
        System.out.println("username = " + username);
        System.out.println("password = " + password);
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
        System.out.print("Insert the number: ");
    }
    
    public static void writePropertiesFile(String ip_address, String port_number, 
            String instanceName, String databaseName, String username, String password, String folder_path) {
        try {
            Properties properties = new Properties();
            properties.setProperty("ip_address", ip_address);
            properties.setProperty("port_number", port_number);
            properties.setProperty("instanceName", instanceName);
            properties.setProperty("databaseName", databaseName);
            properties.setProperty("username", username);
            properties.setProperty("password", password);

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
    
    public static Connection runTrace(String ip_addess, String port_number, 
            String instanceName, String databaseName, String username, String password) {
        Connection conn = null;    
        try {
            conn = getConnection(ip_addess, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                System.out.println("\nConnected to the Database!");
                System.out.println("\nBegin monitoring.\n-----------------------------------------------------\n");
                
                //create Trace
                String create_sql = "DECLARE @RC int, @TraceID int, @on BIT\n" +
                        "EXEC @rc = sp_trace_create @TraceID output, 0, N'D:\\SampleTrace'  \n" +
                        "\n" +
                        "SELECT RC = @RC, TraceID = @TraceID  \n" +
                        "SELECT @on = 1\n" +
                        "\n" +
                        "EXEC sp_trace_setevent @TraceID, 11, 1, @on\n" +
                        "EXEC sp_trace_setevent @TraceID, 11, 11, @on\n" +
                        "EXEC sp_trace_setevent @TraceID, 11, 14, @on\n" +
                        " \n" +
                        "EXEC sp_trace_setevent @TraceID, 13, 1, @on   \n" +
                        "EXEC sp_trace_setevent @TraceID, 13, 11, @on   \n" +
                        "EXEC sp_trace_setevent @TraceID, 13, 14, @on";
                
                Statement create_statement = conn.createStatement();
                
                ResultSet result = create_statement.executeQuery(create_sql);
                
                //get TraceID
                String TraceID = new String();
                if (result.next()) {
                    TraceID = result.getString("TraceID");
                } else {
                    System.out.println("Couldn't retrieve TraceID.");
                    return null;
                }
                
                //run Trace 
                if (TraceID != null) {
                    String trace_sql = String.format("EXEC sp_trace_setstatus %s, 1", TraceID);
                    PreparedStatement exec_statement = conn.prepareStatement(trace_sql);
                    
                    exec_statement.execute();
                    
                } else {
                    System.out.println("There was result but couldn't retrieve TraceID.");
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
    
    public static String readTrace(Connection conn, String ip_addess, String port_number, 
            String instanceName, String databaseName, String username, String password, String last_exec_time) {
        
        String LAST_EXEC_TIME = new String();
        
        try {
            String readTrace_sql = String.format("SELECT TOP 10 TextData, LoginName, StartTime, EventClass FROM fn_trace_gettable('D:\\SampleTrace.trc', DEFAULT) \n" +
                    "WHERE TextData LIKE '%INSERT%' OR TextData LIKE '%UPDATE%' OR TextData LIKE '%DELETE%' \n" +
                    "StartTime > %s" +
                    "ORDER BY StartTime DESC", last_exec_time);
            
            Statement readTrace_statement = conn.createStatement();
                
            ResultSet result = readTrace_statement.executeQuery(readTrace_sql);
                       
            if (result != null) {
                LAST_EXEC_TIME = result.getString("StartTime");
            }
            while (result.next()) {
                String StartTime = result.getString("StartTime");
                String TextData = result.getString("TextData");
                String LoginName = result.getString("LoginName");
//                String EventClass = result.getString("EventClass");
                
                System.out.println(ANSI_RED + StartTime + ANSI_RESET + " - " 
                        + ANSI_PURPLE + LoginName + ANSI_RESET + " - " 
                        + TextData.replaceAll("INSERT", ANSI_BLUE + "INSERT" + ANSI_RESET)
                                .replaceAll("UPDATE", ANSI_BLUE + "UPDATE" + ANSI_RESET)
                                .replaceAll("DELETE", ANSI_BLUE + "DELETE" + ANSI_RESET)
                                .replaceAll("TRUNCATE", ANSI_BLUE + "TRUNCATE" + ANSI_RESET)
                                .replaceAll("ALTER", ANSI_BLUE + "ALTER" + ANSI_RESET));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return LAST_EXEC_TIME;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String ip_address = "localhost";
        String port_number = "1433";
        String instanceName = "MSSQLSERVER";
        String databaseName = "sampledb";
        String username = "sa";
        String password = "123456";
        
        // TODO code application logic here
        try {
            Scanner sc = new Scanner(System.in);
            
            //Load properties file
            try {
                Properties prop = loadPropertiesFile();
                
                ip_address = prop.getProperty("ip_address");
                port_number = prop.getProperty("port_number");
                instanceName = prop.getProperty("instanceName");
                databaseName = prop.getProperty("databaseName");
                username = prop.getProperty("username");
                password = prop.getProperty("password");
                
                System.out.println("Properties file loaded!");
            } catch (FileNotFoundException e) {
                System.out.println("No properties file found! Using default properties..");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            initMenu(ip_address, port_number, instanceName, databaseName, username, password);
            
            System.out.print("Do you want to make a change? (Y/N:)");
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
                        //Enter instanceName
                        System.out.print("\nEnter Instance Name (blank for \'MSSQLSERVER\'): ");
                        instanceName = new String(sc.nextLine());
                        if (instanceName.equals("")) {
                            instanceName = "MSSQLSERVER";
                        }
                        break;
                    case '4':
                        //Enter databaseName
                        System.out.print("\nEnter Database Name (blank for \'sampledb\'): ");
                        databaseName = new String(sc.nextLine());
                        if (databaseName.equals("")) {
                            databaseName = "sampledb";
                        }
                        break;
                    case '5':
                        //Enter username
                        System.out.print("\nEnter Username (blank for \'sa\'): ");
                        username = new String(sc.nextLine());
                        if (username.equals("")) {
                            username = "sa";
                        }
                        break;
                    case '6':
                        //Enter password
                        System.out.print("\nEnter Password (blank for \'******\'): ");
                        password = new String(sc.nextLine());
                        if (password.equals("")) {
                            password = "123456";
                        }
                        break;
                }
                
                System.out.println("Successfully updated the component!\n");
                initMenu(ip_address, port_number, instanceName, databaseName, username, password);
                System.out.print("Do you still want to make changes ? (Y/N):");
                key_inputs = sc.nextLine().trim();
            }
            
            System.out.print("Do you want to save the components for next time use ? (Y/N): ");
            key_inputs = sc.nextLine().toUpperCase();
            if (key_inputs.startsWith("Y")) {
                writePropertiesFile(ip_address, port_number, instanceName, databaseName, username, password, username);
            }
            
            Connection conn = runTrace(ip_address, port_number, instanceName, databaseName, username, password);
            
            String last_exec_time = new String("0");
            while (true) {
                last_exec_time = readTrace(conn, ip_address, port_number, instanceName, databaseName, username, password, last_exec_time);               
            }      
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
