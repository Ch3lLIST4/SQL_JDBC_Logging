/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package mssql_api_testing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;


/**
 *
 * @author ch3l
 */
public class MSSQL_API_testing {
   
    
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_GREEN = "\u001B[32m";
    
    public static final String DATABASE_USER = "user";
    public static final String DATABASE_PASSWORD = "password";
    public static final String MSSQL_RECONNECT = "autoReconnect";
    public static final String MSSQL_MAX_RECONNECTS = "maxReconnects";
    
    public static final int TIME_OUT = 5;
    public static final int RECONNECTION_TIME_OUT = 2;
    
    public static JSONObject obj_main = new JSONObject();
    
//    https://ff855c5b-7d87-4035-a588-d444d913a96d.mock.pstmn.io/data
    public static String SCHEMA = "https";
    public static String HOSTNAME = "ff855c5b-7d87-4035-a588-d444d913a96d.mock.pstmn.io";
//    public static String HOSTNAME = "ff855c5b-7d87-4035-a588-d444d913a96dWRONG.mock.pstmn.io";
    public static String PATH = "data";
    
    public static String API_URL = SCHEMA + "://" + HOSTNAME + "/" + PATH;
            
    public static int MAX_QUERIES_IN_FILE = 10_000;
    
    public static String lastest_file_name = new String();
    
    public static int last_sent_file_index = 0;
    public static int last_sent_line_index = 0;
    
    public static int RESENDING_DATA_TIMEOUT = 1;
    
    public static String DATABASE_NAME = "MSSQL";
    
    public static int MAX_TRACE_SIZE = 5_000_000; // 5,00MB
    
    
    /**
     * @param args the command line arguments 
     */
    
    
    public static String getCurrentTime(){
        // 2020-11-05 15:37:00.884583
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime now = LocalDateTime.now();
        return(dtf.format(now));
    }    
    
    
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
        System.out.println("trace_path = " + trace_path);
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

    
    public static void create_folder(String folder_path) {
        File dir = new File(folder_path);
        boolean checkDirCreated = dir.mkdir();
        if (checkDirCreated) {
            System.out.println("Directory created successfully : " + folder_path);
        }
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
    
    
    public static boolean checkFileExisted(String file_path) {
        boolean already_existed = false;
        try {
            file_path = file_path + ".trc";
            
            File f = new File(file_path);
            already_existed = f.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return already_existed;
    }
    
    
    public static boolean checkFileSizeExceeds(String file_path) {
        try {
            file_path = file_path + ".trc";
            
            File f = new File(file_path);
            long file_size = f.length();
            
            if (file_size >= MAX_TRACE_SIZE) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    public static String runTrace(Connection conn, String trace_fn) throws Exception {
        String TraceID = new String();
        
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
                    "EXEC sp_trace_setevent @TraceID, 13, 14, @on", trace_fn);

            Statement create_statement = conn.createStatement();

            ResultSet result = create_statement.executeQuery(create_sql);

            //get TraceID        
            if (result.next()) {
                TraceID = result.getString("TraceID");
                System.out.println(ANSI_PURPLE + trace_fn + " is created - TraceID: " + TraceID + ANSI_RESET);
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

        return TraceID;
    }
    
    
    public static void writePropertiesFile(String ip_address, String port_number, 
            String username, String password, String log_path, String trace_path, String last_TraceID) {
        try {
            Properties properties = new Properties();
            properties.setProperty("ip_address", ip_address);
            properties.setProperty("port_number", port_number);
            properties.setProperty("username", username);
            properties.setProperty("password", password);
            properties.setProperty("log_path", log_path);
            properties.setProperty("trace_path", trace_path);
            properties.setProperty("last_TraceID", last_TraceID);

            OutputStream output  = new FileOutputStream(trace_path + "info.properties");
            properties.store(output , "Info Properties");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static Properties loadPropertiesFile(String trace_path) throws Exception {
        Properties prop = new Properties();

        InputStream input = new FileInputStream(trace_path + "info.properties");          

        prop.load(input);
        
        return prop;
    }
    
    
    public static String readTrace(Connection conn, 
            String trace_fn, String last_exec_time) throws Exception {
        
        trace_fn = trace_fn + ".trc";
        // LAST_EXEC_TIME temp var for retrieving most current exec time 
        String LAST_EXEC_TIME = new String();
        
        String readTrace_sql = String.format("SELECT TextData, LoginName, StartTime, EventClass FROM fn_trace_gettable('%s', DEFAULT) \n", trace_fn) +
                "WHERE (TextData LIKE '%INSERT%' OR TextData LIKE '%UPDATE%' OR TextData LIKE '%DELETE%' OR TextData LIKE '%TRUNCATE%' OR TextData LIKE '%ALTER%') \n" + 
                "AND NOT TextData LIKE '%SELECT TextData, LoginName, StartTime, EventClass FROM fn_trace_gettable%' \n" +
                String.format("AND StartTime > '%s' \n", last_exec_time) +
                "ORDER BY StartTime ASC";

        Statement readTrace_statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        ResultSet result = readTrace_statement.executeQuery(readTrace_sql);

        if (result.last()) {
            LAST_EXEC_TIME = result.getString("StartTime");
            result.beforeFirst();
        }
        
        // creating var
        List<JSONObject> obj_queriesArray = new ArrayList<JSONObject>();
            
        while (result.next()) {
            JSONObject obj_query = new JSONObject();
            
            String StartTime = result.getString("StartTime");
            String TextData = result.getString("TextData");
            String LoginName = result.getString("LoginName");
            String EventClass = result.getString("EventClass");
            
            obj_query.put("StartTime", StartTime);
            obj_query.put("TextData", TextData);
            obj_query.put("LoginName", LoginName);
            obj_query.put("EventClass", EventClass);
            obj_queriesArray.add(obj_query);
                
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
        
        // putting queries obj to main obj
        obj_main.put("queries", obj_queriesArray);
            
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
    
    
    public static void prettyPrintJSON(JSONObject obj_main) {
        try {
            System.out.println("");
            System.out.println(ANSI_RED + getCurrentTime() + ANSI_RESET);
            System.out.println(obj_main.toString(4));
            System.out.println("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void sendMockData(JSONObject obj_main, StringBuilder response) throws Exception{
        URL url = new URL (API_URL);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        String jsonInputString = obj_main.toString(4);

        // Create the Request Body
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);			
        }
        // Read the Response from Input Stream
        try(BufferedReader br = new BufferedReader(
            new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            System.out.println(response.toString());
        }
    }
    
    
    public static int count_lines_in_file(String file_name) {
        int lines = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file_name));
            while (reader.readLine() != null) lines++;
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }
    
    
    public static String create_log_file(String folder_path, String databaseName) {
        long file_index = 0;
        String file_name = new String();
        try {
            while(true) {
                file_name = folder_path + databaseName + "_" + file_index + ".txt";

                File fn = new File(file_name);
                if (fn.createNewFile()) {
                    System.out.println("File created: " + file_name);
                    break;
                } else {
                    System.out.println("File " + file_name + " already exists.");
                    file_index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file_name;
    }
    
    
    public static void write_to_file(String file_name, JSONObject log_obj) {
        int count = count_lines_in_file(file_name) + 1;
        try {
            FileWriter writer = new FileWriter(file_name, true);
            
            writer.write(count + " | " + obj_main.toString() + "\n");
            
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void skipLines(Scanner s,int lineNum){
        for(int i = 0; i < lineNum;i++){
            if(s.hasNextLine())s.nextLine();
        }
    }
    
    
    public static boolean check_mock_connection() {
        try {
            URL url = new URL (API_URL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.getInputStream();
            
            return true;
        } catch (Exception ignore){
            return false;
        }
    }
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        String ip_address = "localhost";
        String port_number = "1433";
        String username = "sa";
        String password = "123456";
        String log_path = ".\\MSSQL_logs\\";
        String trace_path = "D:\\MSSQL_traces\\";
        String last_TraceID = "";
        
        try {
            Scanner sc = new Scanner(System.in);
            
            //Load properties file
            try {
                Properties prop = loadPropertiesFile(trace_path);
                
                ip_address = prop.getProperty("ip_address");
                port_number = prop.getProperty("port_number");
                username = prop.getProperty("username");
                password = prop.getProperty("password");
                log_path = prop.getProperty("log_path");
                last_TraceID = prop.getProperty("last_TraceID");
                
                System.out.println("Properties file loaded!");
            } catch (FileNotFoundException e) {
                System.out.println("No properties file found! Using default properties..");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
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
                        System.out.print("\nEnter Log Path (blank for .\\MSSQL_logs\\): ");
                        log_path = new String(sc.nextLine());
                        if (log_path.equals("")) {
                            log_path = ".\\MSSQL_logs\\";
                        }
                        break;
                    case '6':
                        //Enter trace path
                        System.out.print("\nEnter Trace Path (blank for D:\\MSSQL_traces\\): ");
                        trace_path = new String(sc.nextLine());
                        if (trace_path.equals("")) {
                            trace_path = "D:\\MSSQL_traces\\";
                        }
                        break;
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
            
            // create trace folder if not existed
            create_folder(trace_path);
            
            //end old TraceID if existed in properties file
            if (!("".equals(last_TraceID) || last_TraceID.isEmpty()) ) {
                endTrace(conn, last_TraceID);
                System.out.println("last_TraceID found: " + last_TraceID +". Terminating old trace.");
            }
            
            System.out.println("\nBegin monitoring.\n-----------------------------------------------------\n");
            
            String last_exec_time = getCurrentTime();
            StringBuilder response = new StringBuilder();
            
            int file_index = 1;
            while(true) {
                
                try {
                    //1. create Trace File
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
                    LocalDateTime now = LocalDateTime.now();  
                    String current_date = dtf.format(now);

                    //delete outdated traces
                    delete_outdated_traces(trace_path, current_date);
                    
                    //2. run Trace file
                    String trace_fn  = trace_path + DATABASE_NAME + "-log-" + file_index + "-" + current_date;

                    while (checkFileExisted(trace_fn)) {
                        file_index++;
                        trace_fn = trace_path + DATABASE_NAME + "-log-" + file_index + "-" + current_date;
                    }
                    last_TraceID = runTrace(conn, trace_fn);

                    //3. Consistently read Trace File
                    while (!checkFileSizeExceeds(trace_fn)) {
    //                    System.out.println(ANSI_PURPLE + last_exec_time + ANSI_RESET);
                        last_exec_time = readTrace(conn, trace_fn, last_exec_time);
                        
                        prettyPrintJSON(obj_main);
                        sendMockData(obj_main, response);
                        response = new StringBuilder();
                        
                        writePropertiesFile(ip_address, port_number, username, password, log_path, trace_path, last_TraceID);
                        TimeUnit.SECONDS.sleep(TIME_OUT);
                    }
                    if (checkFileSizeExceeds(trace_fn)) {
                        last_exec_time = readTrace(conn, trace_fn, last_exec_time);
                        
                        prettyPrintJSON(obj_main);
                        sendMockData(obj_main, response);
                        response = new StringBuilder();
                        
                        writePropertiesFile(ip_address, port_number, username, password, log_path, trace_path, last_TraceID);
                        TimeUnit.SECONDS.sleep(TIME_OUT);
                    }
                    
                    endTrace(conn, last_TraceID);
                } catch (SQLException e) {
                    if (conn != null) {
                        conn.close();
                    }
                    System.out.println("Lost connection to DB. Trying to reconnect..");
                    try {
                        conn = getConnection(ip_address, port_number, username, password);
                    } catch (Exception ignore) {
                    }
                    TimeUnit.SECONDS.sleep(RECONNECTION_TIME_OUT);
                } catch (Exception e) {
                    if (response.toString().equals("")){
                        System.out.println("Lost connection to Mock Server. Saving queries as log files for later transfer..");
                        
                        // Logging queries
                        
                        // create log folder if not existed
                        create_folder(log_path);
                        
                        // if exceeds log size -> create new log file
                        if (lastest_file_name.isEmpty() || count_lines_in_file(lastest_file_name) >= MAX_QUERIES_IN_FILE) {
                            String file_name = create_log_file(log_path, DATABASE_NAME);
                            lastest_file_name = file_name;
                        }             
                        
                        // write log to file
                        write_to_file(lastest_file_name, obj_main);
                        
                    } else {
                        System.out.println(e);
                    }
                    TimeUnit.SECONDS.sleep(RECONNECTION_TIME_OUT);
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
