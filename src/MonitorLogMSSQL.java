/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monitorlogmssql;

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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javafx.util.Pair;

/**
 *
 * @author ASUS
 */
public class MonitorLogMSSQL {
    
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    // purple for testing random
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
        
    public static void monitorLog(String ip_addess, String port_number, 
            String instanceName, String databaseName, String username, String password) {
        try {
            Connection conn = getConnection(ip_addess, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                System.out.println("\nConnected to the Database!");
                System.out.println("\nBegin monitoring.\n-----------------------------------------------------\n");
                
                String sql = "SELECT deqs.sql_handle AS [Hash], deqs.last_execution_time AS [Time], dest.TEXT AS [Query]\n" +
                    "FROM sys.dm_exec_query_stats AS deqs\n" +
                    "CROSS APPLY sys.dm_exec_sql_text(deqs.sql_handle) AS dest\n" +
                    "ORDER BY deqs.last_execution_time ASC";
                
                Statement statement = conn.createStatement();
                
                boolean ran_once = false;
                HashMap<Pair<String,String>,String> query_list = new HashMap<>();
                
                // Infinite Loop monitor     
                while (true) {
                    ResultSet result = statement.executeQuery(sql);
                    
                    //first run
                    if (ran_once == false) { 
                        
                        //for each record
                        while (result.next()) {
                            String hash = result.getString("Hash").trim();
                            String time = result.getString("Time").trim();
                            String query = result.getString("Query").trim();
                            
                            //count types of queries in an instance
                            int type_count = 0;
                            if (query.contains("INSERT")) {
                                type_count++;        
                            }
                            if (query.contains("UPDATE")) {
                                type_count++;   
                            }
                            if (query.contains("DELETE")) {
                                type_count++;   
                            }
                            if (query.contains("TRUNCATE")) {
                                type_count++;   
                            }
                            if (query.contains("ALTER")) {
                                type_count++;   
                            }
                            
                            //detect types of query
                            if (query.contains("INSERT") || query.contains("UPDATE") || query.contains("DELETE") || 
                                    query.contains("TRUNCATE") || query.contains("ALTER")){
                                
                                Pair<String,String> pair = new Pair<>(hash, time);
                                
                                //if only one query instance -> remove weird pre-query
                                if (type_count == 1) {
                                    query = query.replaceFirst("\\(.*\\)INSERT", "INSERT")
                                            .replaceFirst("\\(.*\\)UPDATE", "UPDATE")
                                            .replaceFirst("\\(.*\\)DELETE", "DELETE")
                                            .replaceFirst("\\(.*\\)TRUNCATE", "TRUNCATE")
                                            .replaceFirst("\\(.*\\)ALTER", "ALTER");
                                }                         
                                
                                query_list.put(pair,query);
                                System.out.println(ANSI_RED + time + ANSI_RESET + " - " + query                   
                                        .replaceAll("INSERT", ANSI_BLUE + "INSERT" + ANSI_RESET)
                                        .replaceAll("UPDATE", ANSI_BLUE + "UPDATE" + ANSI_RESET)
                                        .replaceAll("DELETE", ANSI_BLUE + "DELETE" + ANSI_RESET)
                                        .replaceAll("TRUNCATE", ANSI_BLUE + "TRUNCATE" + ANSI_RESET)
                                        .replaceAll("ALTER", ANSI_BLUE + "ALTER" + ANSI_RESET));
                            }  
                        }
                        
                        ran_once = true;
                        System.out.println("\nThe end of existed cached logs.\n");
                        TimeUnit.SECONDS.sleep(1);          
                    } 
                    //after first run loop
                    else if (ran_once == true) {
                        while (result.next()) {
                            String hash = result.getString("Hash").trim();
                            String time = result.getString("Time").trim();
                            String query = result.getString("Query").trim();
                            
                            //count types of queries in an instance
                            int type_count = 0;
                            if (query.contains("INSERT")) {
                                type_count++;        
                            }
                            if (query.contains("UPDATE")) {
                                type_count++;   
                            }
                            if (query.contains("DELETE")) {
                                type_count++;   
                            }
                            if (query.contains("TRUNCATE")) {
                                type_count++;   
                            }
                            if (query.contains("ALTER")) {
                                type_count++;   
                            }                      
                            
                            Pair<String,String> pair = new Pair<>(hash, time);
                            // if time hasnt existed in the list
                            if (query_list.get(pair) == null) {
                                if (query.contains("INSERT") || query.contains("UPDATE") || query.contains("DELETE") || 
                                    query.contains("TRUNCATE") || query.contains("ALTER")){
                                    
                                    //if only one query instance -> remove weird pre-query
                                    if (type_count == 1) {
                                        query = query.replaceFirst("\\(.*\\)INSERT", "INSERT")
                                                .replaceFirst("\\(.*\\)UPDATE", "UPDATE")
                                                .replaceFirst("\\(.*\\)DELETE", "DELETE")
                                                .replaceFirst("\\(.*\\)TRUNCATE", "TRUNCATE")
                                                .replaceFirst("\\(.*\\)ALTER", "ALTER");
                                    }
                                    
                                    query_list.put(pair,query);
                                    System.out.println(ANSI_RED + time + ANSI_RESET + " - " + query
                                            .replaceAll("INSERT", ANSI_BLUE + "INSERT" + ANSI_RESET)
                                            .replaceAll("UPDATE", ANSI_BLUE + "UPDATE" + ANSI_RESET)
                                            .replaceAll("DELETE", ANSI_BLUE + "DELETE" + ANSI_RESET)
                                            .replaceAll("TRUNCATE", ANSI_BLUE + "TRUNCATE" + ANSI_RESET)
                                            .replaceAll("ALTER", ANSI_BLUE + "ALTER" + ANSI_RESET));
                                }
                            }
                        }
                        TimeUnit.SECONDS.sleep(3); 
                    }
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testRandom(String ip_address,String port_number,String instanceName,String databaseName,String username,String password) {
        try {
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            if (conn != null) {
                while (true) {
                    int randomNum1 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    int randomNum2 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    int randomNum3 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    
                    int randomNum4 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    int randomNum5 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    int randomNum6 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    
                    int randomNum7 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    int randomNum8 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    int randomNum9 = ThreadLocalRandom.current().nextInt(1, 500 + 1);
                    
                    //INSERT
                    String sql1 = "INSERT INTO Users (id, name, password, mail) " +
                        "VALUES ('7', " + randomNum1 + ", " + randomNum2 + ", " + randomNum3 +")";
                    String sql2 = "INSERT INTO Users (id, name, password, mail) " +
                        "VALUES ('7', " + randomNum4 + ", " + randomNum5 + ", " + randomNum6 +")";
                    String sql3 = "INSERT INTO Users (id, name, password, mail) " +
                        "VALUES ('7', " + randomNum7 + ", " + randomNum8 + ", " + randomNum9 +")";
                    
                    PreparedStatement statement1 = conn.prepareStatement(sql1);
                    PreparedStatement statement2 = conn.prepareStatement(sql2);
                    PreparedStatement statement3 = conn.prepareStatement(sql3);
                    
                    //UPDATE
                    String sql4 = "UPDATE Users SET password = " + randomNum1 + " WHERE id = '7'";
                    
                    PreparedStatement statement4 = conn.prepareStatement(sql4);
                    
                    //DELETE
                    String sql5 = "DELETE FROM Users WHERE id = '7'";

                    PreparedStatement statement5 = conn.prepareStatement(sql5);

                    
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");  
                    
                    //Statement 1 INSERT
                    LocalDateTime now = LocalDateTime.now();  
                    System.out.println(ANSI_PURPLE + dtf.format(now) + " - " + sql1 + ANSI_RESET);  
                    statement1.executeUpdate();
                    TimeUnit.SECONDS.sleep(2); 
                    
                    //Statement 2 INSERT
                    now = LocalDateTime.now();  
                    System.out.println(ANSI_PURPLE + dtf.format(now) + " - " + sql2 + ANSI_RESET); 
                    statement2.executeUpdate();
                    TimeUnit.SECONDS.sleep(1);       
                    
                    //Statement 4 UPDATE
                    now = LocalDateTime.now();  
                    System.out.println(ANSI_PURPLE + dtf.format(now) + " - " + sql4 + ANSI_RESET); 
                    statement4.executeUpdate();
                    TimeUnit.SECONDS.sleep(2); 
                    
                    //Statement 5 DELETE
                    now = LocalDateTime.now();  
                    System.out.println(ANSI_PURPLE + dtf.format(now) + " - " + sql5 + ANSI_RESET); 
                    statement5.executeUpdate();
                    TimeUnit.SECONDS.sleep(3); 
                    
                    //Statement 3 INSERT
                    now = LocalDateTime.now();  
                    System.out.println(ANSI_PURPLE + dtf.format(now) + " - " + sql3 + ANSI_RESET); 
                    statement3.executeUpdate();
                    TimeUnit.SECONDS.sleep(10); 
                }
            } else {
                System.out.println("Couldn't connect to the Database!");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            
            // Load properties file
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
            
            String key_inputs = new String();
            System.out.print("Do you want to make a change? (Y/N:)");
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
                             
            System.out.print("Do you want to run program without testing function ? (Y/N): ");
            key_inputs = sc.nextLine().toUpperCase();
            if (key_inputs.startsWith("Y")) {
                monitorLog(ip_address, port_number, instanceName, databaseName, username, password); 
            } else {
                // Multithreading     
                final String final_ip_address = ip_address;
                final String final_port_number = port_number;
                final String final_instanceName = instanceName;
                final String final_databaseName = databaseName;
                final String final_username = username;
                final String final_password = password;

                System.out.println("\n" + ANSI_RED + "Starting Main Thread" + ANSI_RESET);

                class Runnable1 implements Runnable{
                    public void run(){
                        monitorLog(final_ip_address, final_port_number, final_instanceName, final_databaseName, final_username, final_password); 
                    }
                }
                Runnable r1 = new Runnable1();
                Thread t1 = new Thread(r1);
                t1.start();

                // Waits 10 sec to start testing function
                TimeUnit.SECONDS.sleep(10); 

                System.out.println(ANSI_RED + "Starting random test." + ANSI_RESET);

                class Runnable2 implements Runnable{
                    public void run(){
                        testRandom(final_ip_address, final_port_number, final_instanceName, final_databaseName, final_username, final_password);
                    }
                }
                Runnable r2 = new Runnable2();
                Thread t2 = new Thread(r2);
                t2.start(); 
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
