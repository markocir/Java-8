/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package filematching;

import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Scanner;
import java.io.IOException;
/**
 *
 * @author markoc
 */
public class Log {
    HashMap<Integer, Integer> idList = new HashMap<>(10);
    Scanner trans;
    Formatter log;
    String tPath, lPath;
    
    /**
     * Constructor saves paths of files to local variables
     * @param logPath stores a path were to create or locate a log file
     * @param transPath stores a path were to create or locate a transactions file
     */
    public Log(String logPath, String transPath){
        this.tPath = transPath;
        this.lPath = logPath;
    }
    
    /**
     *
     * @param id (master)that is going to be saved into a list and
     * compared with master account IDs (it does not save duplicates)
     */
    public void addId(int id){
        if(!this.idList.containsKey(id))
            this.idList.put(id, id);
    }
    
    /**
     * This method runs through the transactions file and compares IDs from that
     * file with IDs from IDs added to the list. If there is no match a line 
     * of text with that ID is stored to a log file.
     */
    public void compareList(){
        openTrans(); //we open an existing file, it must exist
        openLog(); //we open or create a file
        
        while(this.trans.hasNext()){
            int accNumber = this.trans.nextInt();
            if(!this.idList.containsKey(accNumber))
                log.format("%s %d%n","Unmatched transaction record for number: ", accNumber);
            this.trans.nextLine();
        }
        //we wont use the files anymore, so we close them
        closeLog();
        closeTrans();
    }
    
    public void openLog(){
        try{
            log = new Formatter(this.lPath);
        }
        catch(SecurityException e){
            System.err.println("Can Not Write To File. Terminating.");
            System.exit(1);
        }
        catch(FileNotFoundException e){
            System.err.println("File Not Found. Terminating.");
            System.exit(1);
        }
    }
    
    public void openTrans(){
        try{
            this.trans = new Scanner(Paths.get(this.tPath));
        }
        catch(IOException e){
            System.err.println("Error Opening File. Terminating.");
            System.exit(1);
        }
    }
    
    public void closeLog(){
        if(this.log!=null)
            this.log.close();
    }
    
    public void closeTrans(){
        if(this.trans!=null)
            this.trans.close();
    }
}