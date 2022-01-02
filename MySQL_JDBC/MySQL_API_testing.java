/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package mysql_api_testing;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Properties;


/**
 *
 * @author ch3l
 */


public class MySQL_API_testing {

    
    // init global vars
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001b[33m";
    public static final String ANSI_CYAN = "\u001b[36m";

    public static final String DATABASE_USER = "user";
    public static final String DATABASE_PASSWORD = "password";
    public static final String MYSQL_AUTO_RECONNECT = "autoReconnect";
    public static final String MYSQL_MAX_RECONNECTS = "maxReconnects";
    
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
    
    public static String DATABASE_NAME = "MYSQL";
    
    
    public static String getCurrentTime(){
        // 2020-11-05 15:37:00.884583
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime now = LocalDateTime.now();
        return(dtf.format(now));
    }
    
    
    public static Connection getConnection(String ip_address, String port_number, 
            String username, String password) throws Exception {
        Connection conn = null;
        
        //1. Load Driver
//            Class.forName("com.mysql.jdbc.Driver");
        Class.forName("com.mysql.cj.jdbc.Driver");
        //2. Create String
        String url = String.format("jdbc:mysql://%s:%s", ip_address, port_number);
        //3. Create Properties
        java.util.Properties connProperties = new java.util.Properties();
        connProperties.put(DATABASE_USER, username);
        connProperties.put(DATABASE_PASSWORD, password);

        connProperties.put(MYSQL_AUTO_RECONNECT, "true");

        connProperties.put(MYSQL_MAX_RECONNECTS, "1");
        //4. Connect Database
        conn = DriverManager.getConnection(url, connProperties);
        
//        System.out.println(conn);
        return conn;
    }
    
    
    public static String checkGeneralLog(Connection conn){
        String value = "OFF";
        try {
            String sql = "show variables like 'general_log';";
        
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);
            
            if(result.next()){
                String var_name = result.getString("Variable_name");
                value = result.getString("Value");

                String output = "%s : %s";
                System.out.println(String.format(output, var_name, value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    
    
    public static String checkLogOutput(Connection conn) {
        String value = "FILE";
        try {
            String sql = "show variables like 'log_output';";
            
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);
            
            if (result.next()) {
                String var_name = result.getString("Variable_name");
                value = result.getString("Value");
                
                String output = "%s : %s";
                System.out.println(String.format(output, var_name, value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
    
    
    public static void onGeneralLog(Connection conn) {
        try {
            String sql = "SET global general_log = 1;";
            
            Statement statement = conn.createStatement();
            statement.execute(sql);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void onLogOutput(Connection conn) {
        try {
            String sql = "SET global log_output = 'TABLE';";
            
            Statement statement = conn.createStatement();
            statement.execute(sql);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    public static boolean is_Valid_Double(String numberString) {
        try
        {
          Double.parseDouble(numberString);
          return true;
        }
        catch(NumberFormatException e)
        {
          return false;
        }
    }
    
    
    // cal functions
    public static String cal_Traffic_speed_Bytes_received(String Bytes_received, String Last_Bytes_received) {
        String value = "0";
        try {
            double Traffic_speed_Bytes_received = (Double.parseDouble(Bytes_received) - Double.parseDouble(Last_Bytes_received)) / TIME_OUT;
            Traffic_speed_Bytes_received = Math.floor(Traffic_speed_Bytes_received * 100) / 100;
            value = String.valueOf(Traffic_speed_Bytes_received);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }

    
    public static String cal_Traffic_speed_Bytes_sent(String Bytes_sent, String Last_Bytes_sent) {
        String value = "0";
        try {
            double Traffic_speed_Bytes_sent = (Double.parseDouble(Bytes_sent) - Double.parseDouble(Last_Bytes_sent)) / TIME_OUT;
            Traffic_speed_Bytes_sent = Math.floor(Traffic_speed_Bytes_sent * 100) / 100;
            value = String.valueOf(Traffic_speed_Bytes_sent);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }

    
    public static String cal_Innodb_data_reads_per_sec(String Innodb_data_read, String Last_Innodb_data_read) {
        String value = "0";
        try {
            double Innodb_data_reads_per_sec = (Double.parseDouble(Innodb_data_read) - Double.parseDouble(Last_Innodb_data_read)) / TIME_OUT;
            Innodb_data_reads_per_sec = Math.floor(Innodb_data_reads_per_sec * 100) / 100;
            value = String.valueOf(Innodb_data_reads_per_sec);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }

    
    public static String cal_Innodb_data_writes_per_sec(String Innodb_data_written, String Last_Innodb_data_written) {
        String value = "0";
        try {
            double Innodb_data_writes_per_sec = (Double.parseDouble(Innodb_data_written) - Double.parseDouble(Last_Innodb_data_written)) / TIME_OUT;
            Innodb_data_writes_per_sec = Math.floor(Innodb_data_writes_per_sec * 100) / 100;
            value = String.valueOf(Innodb_data_writes_per_sec);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }
    
    
    public static String cal_Key_read_efficiency(String Key_reads, String Key_read_requests) {
        String value = "0";
        try {
            double Key_read_efficiency = (1 - (Double.parseDouble(Key_reads)/Double.parseDouble(Key_read_requests)))*100;
            Key_read_efficiency = Math.floor(Key_read_efficiency * 100) / 100;
            value = String.valueOf(Key_read_efficiency);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }
    
    
    public static String cal_Key_write_efficiency(String Key_writes, String Key_write_requests) {
        String value = "0";
        try {
            double Key_write_efficiency = (1 - (Double.parseDouble(Key_writes)/Double.parseDouble(Key_write_requests)))*100;
            Key_write_efficiency = Math.floor(Key_write_efficiency * 100) / 100;
            value = String.valueOf(Key_write_efficiency);
            if((!is_Valid_Double(value)) || value.equals("NaN")){
                value = "0";
            }
        } catch (Exception e) {
        }
        return value;
    }
    
    
    public static String monitorLogTable(Connection conn, String last_exec_time/*, int count[]*/,String monitor_mode) throws Exception {
        // LAST_EXEC_TIME temp var for retrieving most current exec time 
        String LAST_EXEC_TIME = new String();
            
            String sql = String.format("SELECT * FROM mysqL.general_log WHERE event_time > '%s' ", last_exec_time)
                    + "AND NOT argument LIKE 'SELECT * FROM mysqL.general_log WHERE event_time > %' "
                    + "AND NOT argument LIKE 'SHOW GLOBAL STATUS WHERE Variable_name REGEXP %'";
            
            if (monitor_mode.equals("only_alter")) {
                sql = "SELECT * FROM mysqL.general_log WHERE"
                        + " (LOWER(convert(Binary argument using latin1)) like LOWER('insert%')"
                        + " or LOWER(convert(Binary argument using latin1)) like LOWER('delete%')"
                        + " or LOWER(convert(Binary argument using latin1)) like LOWER('update%')"
                        + " or LOWER(convert(Binary argument using latin1)) like LOWER('alter%')"
                        + " or LOWER(convert(Binary argument using latin1)) like LOWER('truncate%'))"
                        + String.format(" and event_time > '%s'", last_exec_time);
            }
            
            Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet result = statement.executeQuery(sql);
            
            if (result.last()) {
                LAST_EXEC_TIME = result.getString("event_time");
                result.beforeFirst();
            }
            
            // creating var
            List<JSONObject> obj_queriesArray = new ArrayList<JSONObject>();
            
            while (result.next()) {
                JSONObject obj_query = new JSONObject();
                
                String event_time = result.getString("event_time");
                String user_host = result.getString("user_host");
                String[] user_hostArray = user_host.split("@", 2); 
                String thread_id = result.getString("thread_id");
                String server_id = result.getString("server_id");
                String command_type = result.getString("command_type");
                String argument = result.getString("argument");
                
                // adding key-value pairs to JSON obj_query -> add obj to queries ArrayList for main obj                
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"); 
                LocalDateTime event_timeLocalDateTime = LocalDateTime.parse(event_time, dtf);
                
                Instant instant = event_timeLocalDateTime.atZone(ZoneId.systemDefault()).toInstant();    
                long timeInMillis = instant.toEpochMilli(); 
                
                obj_query.put("event_time", timeInMillis);
                obj_query.put("user", user_hostArray[0].trim());
                obj_query.put("host", user_hostArray[1].trim());
                obj_query.put("thread_id", Integer.parseInt(thread_id));
                obj_query.put("server_id", Integer.parseInt(server_id));
                obj_query.put("command_type", command_type);
                obj_query.put("argument", argument);
                obj_queriesArray.add(obj_query);
                
                String output = "| %s | %s | %s | %s | %s | %s |";
                
                if (!("".equals(event_time) || event_time.isEmpty())){
                    event_time = ANSI_RED + event_time + ANSI_RESET;
                } else {}
                if (!("".equals(user_host) || user_host.isEmpty())){
                    user_host = ANSI_GREEN + user_host + ANSI_RESET;
                } else {}
                if (!("".equals(thread_id) || thread_id.isEmpty())){
                    thread_id = ANSI_CYAN + thread_id + ANSI_RESET;
                } else {}
                if (!("".equals(server_id) || server_id.isEmpty())){
                    server_id = ANSI_YELLOW + server_id + ANSI_RESET;
                } else {}
                if (!("".equals(command_type) || server_id.isEmpty())){
                    command_type = ANSI_PURPLE + command_type + ANSI_RESET;
                } else {}
                if (!("".equals(argument) || server_id.isEmpty())){
                    argument = ANSI_BLUE + argument + ANSI_RESET;
                } else {}
                
                System.out.println(String.format(output, event_time, user_host, thread_id, server_id, command_type, argument));
//                count[0]++;
            }
            
            // putting queries obj to main obj
            obj_main.put("queries", obj_queriesArray);
        
        if (LAST_EXEC_TIME == null || LAST_EXEC_TIME.isEmpty()){
            LAST_EXEC_TIME = last_exec_time;
        }
        return LAST_EXEC_TIME;
    }
    
    
    public static HashMap<String, String> searchShowStatus(Connection conn, HashMap<String, String> NeededValues) throws Exception {
        HashMap<String, String> NEEDED_VALUES = new HashMap<String, String>();
        ArrayList<String> arr = new ArrayList<String>();
        // Uptime
        arr.add("Uptime");
        // Threads_connected
        arr.add("Threads_connected");
        // Traffic = total-last_total / TIME_OUT
        arr.add("Bytes_received");
        arr.add("Bytes_sent");
        // InnoDB Buffer Usage = (Innodb_buffer_pool_pages_data / Innodb_buffer_pool_pages_total)*100
        arr.add("Innodb_buffer_pool_pages_data");
        arr.add("Innodb_buffer_pool_pages_total");
        // InnoDB Data read write speed = total-last_total / TIME_OUT
        arr.add("Innodb_data_read");
        arr.add("Innodb_data_written");
        // Key Read Efficiency
        arr.add("Key_reads");
        arr.add("Key_read_requests");
        // Key Write Efficiency
        arr.add("Key_writes");
        arr.add("Key_write_requests");
        
        //^%$
        //SHOW GLOBAL STATUS WHERE Variable_name REGEXP '^Threads_connected$|^Bytes_received$|^Bytes_sent$|^Created_tmp_disk_tables$|^Handler_read_first$|^Innodb_buffer_pool_wait_free$|^Key_reads$|^Max_used_connections$|^Open_tables$|^Select_full_join$|^Slow_queries$|^Uptime$';   
        // convert to needed String
        String listString = "";
        for (String s : arr)
        {
            listString += "^" + s + "$|";
        }
        if(listString.endsWith("|")) {
            listString = listString.substring(0, listString.length() - 1);
        }
        
        try {
            String sql = String.format("SHOW GLOBAL STATUS WHERE Variable_name REGEXP '%s';", listString);
            
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery(sql);
            
            // initializing vars
            double Innodb_buffer_pool_pages_data = 0;
            double Innodb_buffer_pool_pages_total = 0;
            double Key_reads = 0;
            double Key_read_requests = 0;
            double Key_writes = 0;
            double Key_write_requests = 0;
            
            // creating var
            JSONObject obj_status = new JSONObject();
            
            while (result.next()) {
                boolean needed = false;
                
                String variable_name = result.getString("Variable_name");
                String value = result.getString("Value");
                
                if (variable_name.equals("Uptime")) {
                    needed = true;
                } 
                else if (variable_name.equals("Threads_connected")) {
                    variable_name = "Connections";
                    needed = true;
                }
                else if (variable_name.equals("Bytes_received")) {
                    variable_name = "Traffic in";
                    NEEDED_VALUES.put("Bytes_received", value);
                    value = cal_Traffic_speed_Bytes_received(value, NeededValues.get("Bytes_received"));
                    needed = true;
                }
                else if (variable_name.equals("Bytes_sent")) {
                    variable_name = "Traffic out";
                    NEEDED_VALUES.put("Bytes_sent", value);
                    value = cal_Traffic_speed_Bytes_received(value, NeededValues.get("Bytes_sent"));
                    needed = true;
                }
                
                else if (variable_name.equals("Innodb_buffer_pool_pages_data")) {
                    Innodb_buffer_pool_pages_data = Double.parseDouble(value);
                }
                else if (variable_name.equals("Innodb_buffer_pool_pages_total")) {
                    Innodb_buffer_pool_pages_total = Double.parseDouble(value);
                }
                
                else if (variable_name.equals("Innodb_data_read")) {
                    variable_name = "InnoDB Reads per Second";
                    NEEDED_VALUES.put("Innodb_data_read", value);
                    value = cal_Innodb_data_reads_per_sec(value, NeededValues.get("Innodb_data_read"));
                    needed = true;
                }
                else if (variable_name.equals("Innodb_data_written")) {
                    variable_name = "InnoDB Writes per Second";
                    NEEDED_VALUES.put("Innodb_data_written", value);
                    value = cal_Innodb_data_writes_per_sec(value, NeededValues.get("Innodb_data_written"));
                    needed = true;
                }
                
                else if (variable_name.equals("Key_reads")) {
                    Key_reads = Double.parseDouble(value);
                }
                else if (variable_name.equals("Key_read_requests")) {
                    Key_read_requests = Double.parseDouble(value);
                }
                else if (variable_name.equals("Key_writes")) {
                    Key_writes = Double.parseDouble(value);
                }
                else if (variable_name.equals("Key_write_requests")) {
                    Key_write_requests = Double.parseDouble(value);
                }
                
                if (needed == true) {
                    if (variable_name.equals("Connections")){
                        obj_status.put(variable_name, Integer.parseInt(value));
                    }
                    else {
                        obj_status.put(variable_name, Double.parseDouble(value));
                    }
                }
            }
            
            // cal Innodb_buffer_usage
            double Innodb_buffer_usage_value = (Innodb_buffer_pool_pages_data / Innodb_buffer_pool_pages_total) * 100;
            Innodb_buffer_usage_value = Math.floor(Innodb_buffer_usage_value * 100) / 100; 
            String Innodb_buffer_usage = String.valueOf(Innodb_buffer_usage_value);
            obj_status.put("InnoDB Buffer Usage", Double.parseDouble(Innodb_buffer_usage));
            // cal Key_write_efficiency
            String Key_write_efficiency = cal_Key_write_efficiency(String.valueOf(Key_writes), String.valueOf(Key_write_requests));
            obj_status.put("Key Write Efficiency", Double.parseDouble(Key_write_efficiency));
            // cal Key_read_efficiency
            String Key_read_efficiency = cal_Key_read_efficiency(String.valueOf(Key_reads), String.valueOf(Key_read_requests));
            obj_status.put("Key Read Efficiency", Double.parseDouble(Key_read_efficiency));
            
            //others
            obj_status.put("Server Status", "Running");
            
            // putting status vars obj to main obj
            obj_main.put("status", obj_status);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NEEDED_VALUES;
    }
    
    
    public static void initMenu(String ip_address, String port_number, 
            String username, String password, String log_path, String monitor_mode) {
        System.out.println("====================");
        System.out.println("ip_address = " + ip_address);
        System.out.println("port_number = " + port_number);
        System.out.println("username = " + username);
        System.out.println("password = " + password);
        System.out.println("log_path = " + log_path);
        System.out.println("monitor_mode = " + monitor_mode);
        System.out.println("====================");
    }
    
    
    public static void printChangeMenu() {
        System.out.println("\nWhich one do you want to change ?");
        System.out.println("1. ip_address");
        System.out.println("2. port_number");
        System.out.println("3. username");
        System.out.println("4. password");
        System.out.println("5. log_path");
        System.out.println("6. monitor_mode");
        System.out.print("Insert the number: ");
    }
    
    
    public static void create_log_folder(String folder_path) {
        File dir = new File(folder_path);
        boolean checkDirCreated = dir.mkdir();
        if(checkDirCreated){
            System.out.println("Directory created successfully : " + folder_path);
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
    
    
    public static boolean check_file_exised(String file_name) {
        File f = new File(file_name);
        if(f.exists() && !f.isDirectory()) { 
            return true;
        }
        return false;
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
    

    public static void writePropertiesFile(String ip_address, String port_number, 
            String username, String password, String log_path, String monitor_mode) {
        try {
            Properties properties = new Properties();
            properties.setProperty("ip_address", ip_address);
            properties.setProperty("port_number", port_number);
            properties.setProperty("username", username);
            properties.setProperty("password", password);
            properties.setProperty("log_path", log_path);
            properties.setProperty("monitor_mode", monitor_mode);

            OutputStream output  = new FileOutputStream(log_path + "info.properties");
            properties.store(output , "Info Properties");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static Properties loadPropertiesFile(String log_path) throws Exception {
        Properties prop = new Properties();

        InputStream input = new FileInputStream(log_path + "info.properties");          

        prop.load(input);
        
        return prop;
    }
    
    
    public static void main(String[] args) {
        // TODO code application logic here
        String ip_address = "localhost";
        String port_number = "3306";
        String username = "root";
        String password = "123456";
        String log_path = ".\\tmp\\";
        String monitor_mode = "only_alter";
        
        try {
            Scanner sc = new Scanner(System.in);
            
            //Load properties file
            try {
                Properties prop = loadPropertiesFile(log_path);

                ip_address = prop.getProperty("ip_address");
                port_number = prop.getProperty("port_number");
                username = prop.getProperty("username");
                password = prop.getProperty("password");
                log_path = prop.getProperty("log_path");
                monitor_mode = prop.getProperty("monitor_mode");

                System.out.println("Properties file loaded!");
            } catch (FileNotFoundException e) {
                System.out.println("No properties file found! Using default properties..");
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            initMenu(ip_address, port_number, username, password, log_path, monitor_mode);
            
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
                        System.out.print("\nEnter Port Number (blank for \'3306\'): ");
                        port_number = new String(sc.nextLine());
                        if (port_number.equals("")) {
                            port_number = "3306";
                        }
                        break;
                    case '3':
                        //Enter username
                        System.out.print("\nEnter Username (blank for \'root\'): ");
                        username = new String(sc.nextLine());
                        if (username.equals("")) {
                            username = "root";
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
                        System.out.print("\nEnter Log Path (blank for .\\\\tmp\\\\): ");
                        log_path = new String(sc.nextLine());
                        if (log_path.equals("")) {
                            log_path = ".\\tmp\\";
                        }
                        break;
                    case '6':
                        //Enter monitor mode
                        System.out.print("\nEnter Monitor Mode (blank for \'only_alter\'): ");
                        monitor_mode = new String(sc.nextLine());
                        if (monitor_mode.equals("")) {
                            monitor_mode = "only_alter";  
                        }
                        break;
                }
                
                System.out.println("Successfully updated the component!\n");
                initMenu(ip_address, port_number, username, password, log_path, monitor_mode);
                System.out.print("Do you still want to make changes ? (Y/N):");
                key_inputs = sc.nextLine().toUpperCase().trim();
            }
            
            // create log folder if not existed
            create_log_folder(log_path);
            
            // save user properties
            writePropertiesFile(ip_address, port_number, username, password, log_path, monitor_mode);
            
            // monitor
            
            //connect to the Database
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
            
            //1. Check general_log status 1 -> if 0 : Turn it on
            String general_log_status = checkGeneralLog(conn);
            switch (general_log_status) {
                case "OFF":
                    System.out.print("MySQL general_log found not turn ON yet. Do you want to turn it on ? (Y/N):");
                    key_inputs = sc.nextLine().toUpperCase().trim();
                    if (key_inputs.startsWith("Y")) {
                        onGeneralLog(conn);
                        general_log_status = checkGeneralLog(conn);
                        switch (general_log_status) {
                            case "OFF":
                                System.out.println("general_log status is still OFF. Please re-check");
                                return;
                            case "ON":
                                System.out.println("general_log status is now ON and ready to be monitored");
                                break;
                            default:
                                System.out.println("general_log status is either ON or OFF. Please re-check");
                                return;
                        }
                    } else {
                        System.out.println("Turning off. Make sure general_log is ON to use the program");
                        return;
                    }   break;
                case "ON":
                    System.out.println("MySQL general_log found ON and ready to be monitored");
                    break;
                default:
                    System.out.println("MySQL general_log found neither ON or OFF. Please re-check");
                    return;
            }
            
            //2. Check log_output status 'table' -> if 'file' : Turn it on
            String log_output_status = checkLogOutput(conn);
            switch (log_output_status) {
                case "FILE":
                    System.out.print("MySQL log_output found not switched to TABLE yet. Do you want to switch it now ? (Y/N):");
                    key_inputs = sc.nextLine().toUpperCase().trim();
                    if (key_inputs.startsWith("Y")) {
                        onLogOutput(conn);
                        log_output_status = checkLogOutput(conn);
                        switch (log_output_status) {
                            case "FILE":
                                System.out.println("log_output value is still FILE. Please re-check");
                                return;
                            case "TABLE":
                                System.out.println("log_output value is now TABLE and ready to be monitored");
                                break;
                            default:
                                System.out.println("log_output value is either FILE or TABLE. Please re-check");
                                return;
                        }
                    } else {
                        System.out.println("Turning off. Make sure log_output is switched to TABLE to use the program");
                        return;
                    }   break;
                case "TABLE":
                    System.out.println("MySQL log_output is already TABLE and ready to be monitored");
                    break;
                default:
                    System.out.println("MySQL log_output found neither FILE or TABLE. Please re-check");
                    return;
            }
            
            System.out.println("\nBegin monitoring.\n-----------------------------------------------------\n");
            
            //3. Monitor log table - server status
//            int count[] = new int[]{0};
            String last_exec_time = getCurrentTime();
            StringBuilder response = new StringBuilder();
            while(true) {
                try {
                    
                    // 3.0 check available logs -> send them logs
                    while(check_mock_connection()) {
                        
                        // đọc đi từ 0 lên -> nếu có file -> gửi dữ liệu tới hết
                        String file_name = log_path + DATABASE_NAME + "_" + last_sent_file_index + ".txt";
                        
                        if (check_file_exised(file_name)) {
                            
                            // gửi dữ liệu lên server theo từng hàng
                            FileInputStream fis = new FileInputStream(file_name);       
                            sc = new Scanner(fis); 
                            skipLines(sc, last_sent_line_index);
                            while (sc.hasNextLine()) {
                                
                                // extract dữ liệu json bằng regex
                                String line = sc.nextLine();
                                
                                String[] line_parts = line.split("\\s[|]\\s");
                                
                                String json_data_str = line_parts[1].trim();
                                
                                // send dữ liệu json lên server
                                JSONObject json_log_obj = new JSONObject(json_data_str);
                                
                                // sending mock data
                                prettyPrintJSON(json_log_obj);
                                sendMockData(json_log_obj, response);
                                response = new StringBuilder();
                                
                                TimeUnit.SECONDS.sleep(RESENDING_DATA_TIMEOUT);
                            }
                            
                            last_sent_line_index = 0;
                            last_sent_file_index++;
                            
                        }
                        else {
                            break;
                        }
                    }
                    
                    //3.1. monitor Log Queries
                    last_exec_time = monitorLogTable(conn, last_exec_time/*, count*/, monitor_mode);
//                    System.out.println("Executed");
    //                System.out.println(count[0]);
                    
                    //3.2. monitor Server Status
                    HashMap<String, String> Needed_Values = new HashMap<String, String>();
        
                    Needed_Values.put("Bytes_received", "0");
                    Needed_Values.put("Bytes_sent", "0");
                    Needed_Values.put("Innodb_data_read", "0");
                    Needed_Values.put("Innodb_data_written", "0");
                    Needed_Values.put("Key_reads", "0");
                    Needed_Values.put("Key_read_requests", "0");
                    Needed_Values.put("Key_writes", "0");
                    Needed_Values.put("Key_write_requests", "0");
                    
                    Needed_Values = searchShowStatus(conn, Needed_Values);
                    
                    // 3.3 send mock data
                    prettyPrintJSON(obj_main);
                    sendMockData(obj_main, response);
                    response = new StringBuilder();
                    
                    TimeUnit.SECONDS.sleep(TIME_OUT);
                } catch (SQLException e) {
                    if (conn != null) {
                        conn.close();
                    }
                    System.out.println("Lost connection to DB. Trying to reconnect..");
                    try {
                        conn = getConnection(ip_address, port_number, username, password);
                        onGeneralLog(conn);
                        onLogOutput(conn);
                    } catch (Exception ignore) {
                    }
                    TimeUnit.SECONDS.sleep(RECONNECTION_TIME_OUT);
                } catch (Exception e) {
                    if (response.toString().equals("")){
                        System.out.println("Lost connection to Mock Server. Saving queries as log files for later transfer..");
                        
                        // Logging queries
                        
                        // create log folder if not existed
                        create_log_folder(log_path);
                        
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
