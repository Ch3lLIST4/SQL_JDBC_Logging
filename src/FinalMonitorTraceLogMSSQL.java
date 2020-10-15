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
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 *
 * @author ASUS
 */
public class FinalMonitorTraceLogMSSQL {
    
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_GREEN = "\u001B[32m";
    
    public static int MAX_LOG_SIZE = 5_000_000; // 5,00MB
    
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
            String instanceName, String databaseName, String username, String password, String log_path, String last_exec_time, String last_TraceID) {
        System.out.println("====================");
        System.out.println("ip_address = " + ip_address);
        System.out.println("port_number = " + port_number);
        System.out.println("instanceName = " + instanceName);
        System.out.println("databaseName = " + databaseName);
        System.out.println("username = " + username);
        System.out.println("password = " + password);
        System.out.println("log_path = " + log_path);
        System.out.println("last_exec_time = " + last_exec_time);
        System.out.println("last_TraceID = " + last_TraceID);
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
        System.out.println("7. log_path");
        System.out.println("8. last_exec_time");
        System.out.println("9. last_TraceID");
        System.out.print("Insert the number: ");
    }
    
    public static void writePropertiesFile(String ip_address, String port_number, 
            String instanceName, String databaseName, String username, String password, String folder_path, String log_path, String last_exec_time, String last_TraceID) {
        try {
            Properties properties = new Properties();
            properties.setProperty("ip_address", ip_address);
            properties.setProperty("port_number", port_number);
            properties.setProperty("instanceName", instanceName);
            properties.setProperty("databaseName", databaseName);
            properties.setProperty("username", username);
            properties.setProperty("password", password);
            properties.setProperty("log_path", log_path);
            properties.setProperty("last_exec_time", last_exec_time);
            properties.setProperty("last_TraceID", last_TraceID);

            OutputStream output  = new FileOutputStream(log_path + "info.properties");
            properties.store(output , "Info Properties");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static Properties loadPropertiesFile(String log_path) throws IOException {
        Properties prop = new Properties();

        InputStream input = new FileInputStream(log_path + "info.properties");          

        prop.load(input);
        
        return prop;
    }
    
    public static String runTrace(Connection conn, String ip_addess, String port_number, 
            String instanceName, String databaseName, String username, String password, String log_path, String file_name) {
        String TraceID = new String();
        try {
            String file_path = log_path + file_name;
    
            if (conn != null) {          
                //create Trace
                String create_sql = String.format("DECLARE @RC int, @TraceID int, @on BIT\n" +
                        "EXEC @rc = sp_trace_create @TraceID output, 0, N'%s'  \n" +
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
                        "EXEC sp_trace_setevent @TraceID, 13, 14, @on", file_path);
                
                Statement create_statement = conn.createStatement();
                
                ResultSet result = create_statement.executeQuery(create_sql);
                
                //get TraceID        
                if (result.next()) {
                    TraceID = result.getString("TraceID");
                    System.out.println(ANSI_PURPLE + file_name + " is created - TraceID: " + TraceID + ANSI_RESET);
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
        return TraceID;
    }
    
    public static String readTrace(Connection conn, 
            String log_path, String file_name, String last_exec_time) {
        
        // LAST_EXEC_TIME temp var for retrieving most current exec time 
        String LAST_EXEC_TIME = new String();
        
        try {
            String file_path = log_path + file_name + ".trc";    
            
            String readTrace_sql = String.format("SELECT TOP 10 TextData, LoginName, StartTime, EventClass FROM fn_trace_gettable('%s', DEFAULT) \n", file_path) +
                    "WHERE (TextData LIKE '%INSERT%' OR TextData LIKE '%UPDATE%' OR TextData LIKE '%DELETE%' OR TextData LIKE '%TRUNCATE%' OR TextData LIKE '%ALTER%') \n" + 
                    "AND NOT TextData LIKE '%SELECT TOP 10 TextData, LoginName, StartTime, EventClass FROM fn_trace_gettable%' \n" +
                    String.format("AND StartTime > '%s' \n", last_exec_time) +
                    "ORDER BY StartTime ASC";
            
            Statement readTrace_statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

            ResultSet result = readTrace_statement.executeQuery(readTrace_sql);

            if (result.last()) {
                LAST_EXEC_TIME = result.getString("StartTime");
                result.beforeFirst();
            }
            while (result.next()) {
                String StartTime = result.getString("StartTime");
                String TextData = result.getString("TextData");
                String LoginName = result.getString("LoginName");
//                String EventClass = result.getString("EventClass");

                //count types of queries in an instance
                int type_count = 0;
                if (TextData.contains("INSERT")) {
                    type_count++;        
                }
                if (TextData.contains("UPDATE")) {
                    type_count++;   
                }
                if (TextData.contains("DELETE")) {
                    type_count++;   
                }
                if (TextData.contains("TRUNCATE")) {
                    type_count++;   
                }
                if (TextData.contains("ALTER")) {
                    type_count++;   
                }  
                
                if (type_count == 1) {
                    TextData = TextData.replaceFirst(".*INSERT", "INSERT")
                            .replaceFirst(".*UPDATE", "UPDATE")
                            .replaceFirst(".*DELETE", "DELETE")
                            .replaceFirst(".*TRUNCATE", "TRUNCATE")
                            .replaceFirst(".*ALTER", "ALTER");
                }
                
                System.out.println(ANSI_RED + StartTime + ANSI_RESET + " - " 
                        + ANSI_GREEN + LoginName + ANSI_RESET + " - " 
                        + TextData.trim().replaceAll("INSERT", ANSI_BLUE + "INSERT" + ANSI_RESET)
                                .replaceAll("UPDATE", ANSI_BLUE + "UPDATE" + ANSI_RESET)
                                .replaceAll("DELETE", ANSI_BLUE + "DELETE" + ANSI_RESET)
                                .replaceAll("TRUNCATE", ANSI_BLUE + "TRUNCATE" + ANSI_RESET)
                                .replaceAll("ALTER", ANSI_BLUE + "ALTER" + ANSI_RESET));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (LAST_EXEC_TIME == null || LAST_EXEC_TIME.isEmpty()){
            LAST_EXEC_TIME = last_exec_time;
        }
        return LAST_EXEC_TIME;
    }
    
    public static void endTrace(Connection conn, String TraceID) {
        try {
            String stop_trace_sql = String.format("EXEC sp_trace_setstatus %s, 0 \n"
                    + "EXEC sp_trace_setstatus %s, 2", TraceID, TraceID);
            PreparedStatement exec_statement = conn.prepareStatement(stop_trace_sql);
                    
            exec_statement.execute();      
        } catch (Exception e) {
            System.out.println("Coulnd't end the last TraceID. It might already be ended unexpectedly.");
        }
    }
    
    public static boolean checkFileSizeExceeds(String log_path, String file_name) {
        try {
            String file_path = log_path + file_name + ".trc";
            
            File f = new File(file_path);
            long file_size = f.length();
            
            if (file_size >= MAX_LOG_SIZE) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean checkFileExisted(String log_path, String file_name) {
        boolean already_existed = false;
        try {
            String file_path = log_path + file_name + ".trc";

            File f = new File(file_path);
            already_existed = f.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return already_existed;
    }
    
    public static File[] list_trace_files(String folder_path){      
        File[] files = null;
        try {
            File dir = new File(folder_path);
            files = dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".trc");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }      
        return files;
    }
    
    public static void delete_outdated_traces(String log_path, String current_date) {
        try {
            boolean is_Admin = true;
            File[] files = list_trace_files(log_path);
            int deleted_count = 0;
            for (File file : files) {
                String file_name = file.getName();
                String removed_extension = file_name.substring(0, file_name.lastIndexOf('.'));
                String date = removed_extension.substring(removed_extension.length() - 10);
                
                // delete if date is actual date and < current_date           
                if (date.matches("\\d{4}-\\d{2}-\\d{2}")) { 
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date dateDate = sdf.parse(date);
                    Date current_dateDate = sdf.parse(current_date);
                    
                    if (current_dateDate.compareTo(dateDate) > 0) {
                        if(file.delete()) {
                            deleted_count++;
                        }
                        else{
                            System.out.println(ANSI_PURPLE + file + " deleted failed" + ANSI_RESET);
                            is_Admin = false;
                        }
                    }
                }      
            }
            if (!is_Admin) {
                System.out.println(ANSI_PURPLE + "Please run this program as Administrator for auto cleaning outdated log files" + ANSI_RESET);
            } 
            if (deleted_count > 0){
                System.out.println(ANSI_PURPLE + deleted_count + " outdated files deleted." + ANSI_RESET);
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
        String log_path = "D:\\database_logs\\";
        String last_exec_time = "2020-09-24 00:00:00.000";
        String last_TraceID = "";
        
        // TODO code application logic here
        try {
            Scanner sc = new Scanner(System.in);
            
            //Load properties file
            try {
                Properties prop = loadPropertiesFile(log_path);
                
                ip_address = prop.getProperty("ip_address");
                port_number = prop.getProperty("port_number");
                instanceName = prop.getProperty("instanceName");
                databaseName = prop.getProperty("databaseName");
                username = prop.getProperty("username");
                password = prop.getProperty("password");
                log_path = prop.getProperty("log_path");
                last_exec_time = prop.getProperty("last_exec_time");
                last_TraceID = prop.getProperty("last_TraceID");
                
                System.out.println("Properties file loaded!");
            } catch (FileNotFoundException e) {
                System.out.println("No properties file found! Using default properties..");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            initMenu(ip_address, port_number, instanceName, databaseName, username, password, log_path, last_exec_time, last_TraceID);
            
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
                    case '7':
                        //Enter log_path
                        System.out.print("\nEnter Log Path (blank for \'D:\\database_logs\\\'): ");
                        log_path = new String(sc.nextLine());
                        if (log_path.equals("")) {
                            log_path = "D:\\database_logs";
                        }
                        break;
                    case '8':
                        //Enter last_exec_time
                        System.out.print("\nEnter Password (blank for \'2020-09-24 00:00:00.000\'): ");
                        last_exec_time = new String(sc.nextLine());
                        if (last_exec_time.equals("")) {
                            last_exec_time = "2020-09-24 00:00:00.000";
                        }
                        break;
                    case '9':
                        //Enter last_TraceID
                        System.out.print("\nEnter Last TraceID (blank for \'\'): ");
                        last_TraceID = new String(sc.nextLine());
                        break;
                }
                
                System.out.println("Successfully updated the component!\n");
                initMenu(ip_address, port_number, instanceName, databaseName, username, password, log_path, last_exec_time, last_TraceID);
                System.out.print("Do you still want to make changes ? (Y/N):");
                key_inputs = sc.nextLine().toUpperCase().trim();
            }
                  
            //monitor
            
            //connect to the Database
            Connection conn = getConnection(ip_address, port_number, instanceName, databaseName, username, password);
            
            //create folder if not existed
            File dir = new File(log_path);
            boolean checkDirCreated = dir.mkdir();
            if(checkDirCreated){
                System.out.println("Directory created successfully");
            }  
            
            //end old TraceID if existed in properties file
            if (!(last_TraceID == "" || last_TraceID.isEmpty()) ) {
                endTrace(conn, last_TraceID);
                System.out.println("last_TraceID found: " + last_TraceID +". Terminating old trace.");
            }
            
            System.out.println("\nBegin monitoring.\n-----------------------------------------------------\n");
            int file_index = 1;
            while(true) {

                //1. create & run Trace File
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
                LocalDateTime now = LocalDateTime.now();  
                String current_date = dtf.format(now);
                
                //delete outdated traces
                delete_outdated_traces(log_path, current_date);
                
                String file_name  = databaseName + "-log-" + file_index + "-" + current_date;
                
                while (checkFileExisted(log_path, file_name)) {
                    file_index++;
                    file_name = databaseName + "-log-" + file_index + "-" + current_date;
                }
                last_TraceID = runTrace(conn, ip_address, port_number, instanceName, databaseName, username, password, log_path, file_name);
                
                //2. Consistenly read Trace File
                while (!checkFileSizeExceeds(log_path, file_name)) {
//                    System.out.println(ANSI_PURPLE + last_exec_time + ANSI_RESET);
                    last_exec_time = readTrace(conn, log_path, file_name, last_exec_time);
                    writePropertiesFile(ip_address, port_number, instanceName, databaseName, username, password, log_path, log_path, last_exec_time, last_TraceID);
                    TimeUnit.SECONDS.sleep(5);
                }
                if (checkFileSizeExceeds(log_path, file_name)) {
                    last_exec_time = readTrace(conn, log_path, file_name, last_exec_time);
                    writePropertiesFile(ip_address, port_number, instanceName, databaseName, username, password, log_path, log_path, last_exec_time, last_TraceID);
                    TimeUnit.SECONDS.sleep(5);
                }
                
                endTrace(conn, last_TraceID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
