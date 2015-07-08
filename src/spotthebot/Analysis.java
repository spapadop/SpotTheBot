package spotthebot;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Analyses the data collected and provides as a result a text file filled with
 * all 5-hours-time-window data for all users.
 * 
 * @author Sokratis Papadopoulos
 */
public class Analysis {
    
    private static MongoDBHandler mongo;
    private static HashMap<Long, TimeChunk[]> userPerChunk; //stores the 5 time-window chunks for each user
    private final DBCursor cursor; //used for iterating through retweets in db
        
    private final PrintWriter writer; //writes results into file   
    private int recordCounter; //indicates in which record are we in
    private Date last; //used for changing chunks according to record hour
    private int globalCounter; // indicates the total time-chunk we are in
    private int winCounter; //indicates in which time-window are we in
    private int chunkCounter; //indicates in which time-chunk are we in a time-window [0-5]
    private final SimpleDateFormat format; // decodes the format of date of records
    
    public Analysis() throws UnknownHostException, ParseException, FileNotFoundException, UnsupportedEncodingException{
        mongo = new MongoDBHandler(); 
        cursor = mongo.getRetweetsColl().find(); //get all retweets in our database

        userPerChunk = new HashMap<>(); 
        writer = new PrintWriter("results.txt", "UTF-8");        
        
        chunkCounter=0;
        winCounter =0;
        recordCounter=0;
        last = new Date();
        last.setHours(20); //set as the first occurance
        format = new SimpleDateFormat("EEE MMM d HH:mm:ss Z YYYY"); //CHECK
        globalCounter=0;
        
        loadUsers();
        calculateChunk();
        
        writer.close();
    }
    
    /**
     * Loads all users we have on database into HashMap 
     * to initialize user chunks data.
     * 
     */
    private void loadUsers(){
        
        DBCursor userCursor = mongo.getUsersColl().find();
        while (userCursor.hasNext()) {
            DBObject user = userCursor.next();
            long id = Long.parseLong(user.get("id_str").toString());
            
            TimeChunk[] chunks = new TimeChunk[5];
            for(int i=0; i<chunks.length; i++){
                chunks[i] = new TimeChunk();
            }
            
            userPerChunk.put(id, chunks); 
        }
    }
    
    /**
     * Starts the calculation of first 5 chunks for each user.
     * @throws ParseException 
     */
    private void calculateChunk() throws ParseException{
        
        while (cursor.hasNext()) {
            recordCounter++;
            DBObject rt = cursor.next(); 
            long userO = Long.parseLong(rt.get("originalUserID").toString());
            long userRT = Long.parseLong(rt.get("retweetedUserID").toString());
            Date when = format.parse(rt.get("created_at").toString());
            
            if(when.getHours() != last.getHours()){
                System.out.println("chunk: " + globalCounter + " stopped at record: " + recordCounter);
                chunkCounter++;
                globalCounter++;
                if(chunkCounter == 5){
                    chunkCounter=4;
                    last = when;
                    calculateTimeWindow();
                    shiftOneRight(userO,userRT);
                    break;
                }
                last = when;
            }
            
            userPerChunk.get(userO)[chunkCounter].increaseRetweetsReceived();
            userPerChunk.get(userRT)[chunkCounter].increaseRetweets();
        }
    }
    
    /**
     * Calculates the data for the current time-window using the available
     * 5 chunks data for each user (existing in HashMap).
     */
    private void calculateTimeWindow(){    
        winCounter++;
        Iterator it = userPerChunk.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            Long id = (long) pair.getKey();
            TimeWindow win = new TimeWindow((TimeChunk[]) pair.getValue());
            printWindow(id,win);
        }
    }
    
    /**
     * Prints to file the data of a window for a specific user.
     * 
     * @param id
     * @param win 
     */
    private void printWindow(Long id, TimeWindow win){
        writer.println(winCounter + " " + id + " " + win.print());
    }
    
    /**
     * Shifts the chunks on the hashmap one position back in order to compute in 
     * the last position the new chunk data.
     * 
     * @param userO
     * @param userRT
     * @throws ParseException 
     */
    private void shiftOneRight(long userO, long userRT) throws ParseException{
        
        //shift all values one position back and set last chunk to 0 values
        for (TimeChunk[] value : userPerChunk.values()) {
            for(int i=1; i<value.length; i++){
                value[i-1].setRetweets(value[i].getRetweets());
                value[i-1].setRetweetsReceived(value[i].getRetweetsReceived());
            }
            value[value.length-1].setRetweets(0);
            value[value.length-1].setRetweetsReceived(0);
        }       
        
        //putting the first record of the new chunk
        userPerChunk.get(userO)[4].increaseRetweetsReceived();
        userPerChunk.get(userRT)[4].increaseRetweets();
        
        if(cursor.hasNext()){
            calculateChunk();
        } 
    }
    
}



    
//    /**
//     * Prints the final results for each user in style of:
//     * 'user_id' [tab] 'chunk1-RTs' [space] 'chunk1-RTrec' [space] 'chunk2-RTs' [space] 'chunk2-RTrec' ...
//     */
//    private void printResults() throws FileNotFoundException, UnsupportedEncodingException{
//        PrintWriter writer = new PrintWriter("results.txt", "UTF-8");                
//        userDataString.values().stream().forEach((value) -> {
//            writer.println(value);
//        });
//        writer.close();
//    }


//    /**
//     * 
//     */
//    private void firstPrinting(){
//        //System.out.println("first printing... ");
//        Iterator it = userPerChunk.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            Long id = (long) pair.getKey();
//            TimeChunk[] chunks = (TimeChunk[]) pair.getValue();
//            String temp = "" + id + "\t";
//            for(int i=0; i<chunks.length; i++){
//               temp += chunks[i].getRetweets() + " " + chunks[i].getRetweetsReceived() + " ";
//            }
//            //userDataString.put(id, temp);
//        }
//    }
    
//    private void chunkPrint(){
//        //System.out.println("chunk printing... ");
//        Iterator it = userPerChunk.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            Long id = (long) pair.getKey();
//            TimeChunk[] chunks = (TimeChunk[]) pair.getValue();
//            String test = userDataString.get(id) + chunks[4].getRetweets() + " " + chunks[4].getRetweetsReceived() + " ";
//            userDataString.replace(id, test);
//        }
//    }