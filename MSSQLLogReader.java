/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mssqllogreader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 *
 * @author Admin
 */
public class MSSQLLogReader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        Scanner sc = new Scanner(System.in);
        
        //Enter folder_path
        System.out.print("Enter the path of the file (blank for default):");
        String folder_path = sc.nextLine();
        if (folder_path.equals("")) {
            // C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER\MSSQL\Log
            folder_path = "C:\\Program Files\\Microsoft SQL Server\\MSSQL15.MSSQLSERVER\\MSSQL\\Log";
        }
        
        char key_input = 'Y';
        while (key_input == 'Y') {
            //Enter file_name
            System.out.print("Enter file name (blank for '\'ERRORLOG\''):");
            String file_name = sc.nextLine();
            if (file_name.equals("")) {
                file_name = "ERRORLOG";
            }

            //Create full path
            String path = new String((folder_path + "\\" + file_name).replace("\\", "/"));
            
            //Read file
            try {
                FileInputStream file = new FileInputStream(path);
                BufferedReader reader = new BufferedReader(new InputStreamReader(file));

                //reading file content line by line
                String key_input_2 = new String("");
                String line = reader.readLine();
                while (key_input_2.equals("")) {
                    for (int i = 0; i < 10; i++) {
                        if (line != null){
                            System.out.println(line);
                            line = reader.readLine();
                        }          
                    }
                    key_input_2 = sc.nextLine();
                }
                
            } catch (Exception e) {
//                e.printStackTrace();
                System.out.println("Something's wrong please recheck!");
            }
            
            System.out.print("Do you want to continue the program on that folder? (Y/N): ");
            key_input = sc.nextLine().toUpperCase().trim().charAt(0);
            System.out.println("");
        }   
            
    }
    
}
