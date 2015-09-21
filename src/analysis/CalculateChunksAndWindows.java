package analysis;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import crawling.MongoDBHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Analyzes the data collected and provides as a result a text file filled with
 * all 5-hours-time-window data per each user.
 *
 * @author Sokratis Papadopoulos
 */
public class CalculateChunksAndWindows {

    private static MongoDBHandler mongo; //used to connect with mongoDB data
    private static HashMap<Long, TimeChunk[]> userPerChunk; //stores the 5 time-window chunks for each user
    private final DBCursor cursor; //used for iterating through retweets in db

    private final PrintWriter writer; //writes results into file   
    private int recordCounter; //indicates in which record are we in
    private Date last; //used for changing chunks according to record hour
    private int globalCounter; // indicates the total time-chunk we are in
    private int winCounter; //indicates in which time-window are we in
    private int chunkCounter; //indicates in which time-chunk are we in a time-window [0-5]
    private final SimpleDateFormat format; // decodes the format of date of records

    public CalculateChunksAndWindows() throws UnknownHostException, ParseException, FileNotFoundException, UnsupportedEncodingException, IOException {
        mongo = new MongoDBHandler();
        cursor = mongo.getRetweetsColl().find(); //get all retweets in our database

        userPerChunk = new HashMap<>();
        
        writer = new PrintWriter("results-per-time-window.txt", "UTF-8");

        chunkCounter = 0;
        winCounter = 0;
        recordCounter = 0;
        last = new Date();
        last.setHours(00); //set as the first occurance 21:20:55 and 00:54:06
        last.setMinutes(54);
        last.setSeconds(06);
        format = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                                            Locale.ENGLISH); //CHECK
        globalCounter = 0;

//        System.out.println("loading users...");
        loadUsers();
//        System.out.println("users loaded.");
        calculateChunk();

        writer.close();
        int howManyLines = countLines("results-per-time-window.txt");
        System.out.println("Dirty file has: " + howManyLines + " lines.");
        
        removeZeroEntries("results-per-time-window.txt");
        howManyLines = countLines("results-per-time-window-clear.txt");
        
        System.out.println("Clean file has: " + howManyLines + " lines.");
    }

    /**
     * Loads all users we have on database into HashMap to initialize user
     * chunks data.
     *
     */
    private void loadUsers() {
        
        DBCursor userCursor = mongo.getUsersColl().find();
        while (userCursor.hasNext()) {
            DBObject user = userCursor.next();
            long id = Long.parseLong(user.get("id_str").toString());

            TimeChunk[] chunks = new TimeChunk[5];
            for (int i = 0; i < chunks.length; i++) {
                chunks[i] = new TimeChunk();
            }
            userPerChunk.put(id, chunks);
        }
    }

    /**
     * Starts the calculation of a chunk for each user. Note: In the very first
     * call, it calculates 5 chunks, in order to create the first time-window.
     *
     * @throws ParseException
     */
    private void calculateChunk() throws ParseException {

        while (cursor.hasNext()) {
            recordCounter++;
            
            DBObject rt = cursor.next();
            long userO = Long.parseLong(rt.get("originalUserID").toString());
            long userRT = Long.parseLong(rt.get("retweetedUserID").toString());
            //System.out.println("first!!: " + rt.get("created_at").toString());
            Date when = format.parse(rt.get("created_at").toString());

            if (when.getHours() != last.getHours() && last.getMinutes() == when.getMinutes() && last.getSeconds() == when.getSeconds()) {
                System.out.println("chunk: " + globalCounter + " stopped at record: " + recordCounter);
                System.out.println("from: " + last);
                System.out.println("to: " + when);
                System.out.println("");
                chunkCounter++;
                globalCounter++;
                
                if (chunkCounter == 5) {
                    chunkCounter = 4;
                    last = when;
                    calculateTimeWindow();
                    shiftOneRight(userO, userRT);
                    break;
                }
                last = when;
            }

            userPerChunk.get(userO)[chunkCounter].increaseRetweetsReceived();
            userPerChunk.get(userRT)[chunkCounter].increaseRetweets();
        }
        
        System.out.println("chunk: " + globalCounter + " stopped at record: " + recordCounter);
    }

    /**
     * Calculates the data for the current time-window using the available 5
     * chunks data for each user (existing in HashMap).
     */
    private void calculateTimeWindow() {
        winCounter++;
//        if (winCounter==2)
//            System.exit(1);

        Iterator it = userPerChunk.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Long id = (long) pair.getKey();
            TimeWindow win = new TimeWindow((TimeChunk[]) pair.getValue());
            printWindow(id, win);
        }
    }

    /**
     * Prints to file the data of a window for a specific user.
     *
     * @param id
     * @param win
     */
    private void printWindow(Long id, TimeWindow win) {
        writer.println(winCounter + " " + id + " " + win.print());
    }

    /**
     * Shifts the chunks on the ΗashΜap one position back in order to compute in
     * the last position the new chunk data.
     *
     * @param userO
     * @param userRT
     * @throws ParseException
     */
    private void shiftOneRight(long userO, long userRT) throws ParseException {

        //shift all values one position back and set last chunk to 0 values
        for (TimeChunk[] value : userPerChunk.values()) {
            for (int i = 1; i < value.length; i++) {
                value[i - 1].setRetweets(value[i].getRetweets());
                value[i - 1].setRetweetsReceived(value[i].getRetweetsReceived());
            }
            value[value.length - 1].setRetweets(0);
            value[value.length - 1].setRetweetsReceived(0);
        }

        //putting the first record of the new chunk
        userPerChunk.get(userO)[4].increaseRetweetsReceived();
        userPerChunk.get(userRT)[4].increaseRetweets();

        if (cursor.hasNext()) {
            calculateChunk();
        }
    }
    
    private void removeZeroEntries(String readPath) throws FileNotFoundException, UnsupportedEncodingException {

        File file = new File(readPath);
        BufferedReader reader;
        PrintWriter writer = new PrintWriter("results-per-time-window-clear.txt", "UTF-8");

        reader = new BufferedReader(new FileReader(file));

//        System.out.println("start reading...");
        try {
            boolean flag;
            String text;

            while ((text = reader.readLine()) != null) {
                flag = false;
                String[] splited = text.split("\\s+");
                for (int i = 2; i < 12; i++) {
                    if (!splited[i].equals("0") && !splited[i].equals("0.0")) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    writer.println(text);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

//        System.out.println("finished reading...");
        writer.close();
//        System.out.println("finished writing...");
    }
    
    private int countLines(String path) throws FileNotFoundException, IOException {
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(path)));
        lnr.skip(Long.MAX_VALUE);
//        System.out.println(lnr.getLineNumber() + 1); //Add 1 because line index starts at 0
        // Finally, the LineNumberReader object should be closed to prevent resource leak
        lnr.close();

        return lnr.getLineNumber() + 1;
    }
    
}
