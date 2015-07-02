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
import java.util.Map;

/**
 *
 * @author Sokratis Papadopoulos
 */
public class WriteStatisticsRT {
    
    private static MongoDBHandler mongo;
    private static HashMap<Long, UserEntry> users; 
    
    public static void main(String[] args) throws UnknownHostException, FileNotFoundException, UnsupportedEncodingException, ParseException {
   
        users = new HashMap<>();
        mongo = new MongoDBHandler(); 
        
        DBCursor cursor = mongo.getRetweetsColl().find(); //get all (re)tweets in our database
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss Z YYYY");
        
        while (cursor.hasNext()) {
            DBObject rt = cursor.next(); 
            long userO = Long.parseLong(rt.get("originalUserID").toString());
            long userRT = Long.parseLong(rt.get("retweetedUserID").toString());
            Date when = format.parse(rt.get("created_at").toString());
            
            if (users.containsKey(userO)){
                users.get(userO).newTweet(when, false); //false for receiving retweet
            } else {
                users.put(userO, new UserEntry(when,false));
            }
            
            if (users.containsKey(userRT)){
                users.get(userRT).newTweet(when, true); //true for doing retweet
            } else {
                users.put(userRT, new UserEntry(when,true));
            }
            
        }
        
        //finish up - calculate AVG
        for (UserEntry u : users.values()) {
            u.finish();
        }
        
        PrintWriter writer = new PrintWriter("results.txt", "UTF-8"); 
        for (Map.Entry<Long, UserEntry> entry : users.entrySet()) {
            writer.println(entry.getKey() + " " + entry.getValue().getRetweets() 
                                          + " " + entry.getValue().getMinRtPerHour() 
                                          + " " + entry.getValue().getMaxRtPerHour() 
                                          + " " + entry.getValue().getAvgRtPerHour() 
                                          + " " + entry.getValue().getRTreceived()
                                          + " " + entry.getValue().getMinRTrecPerHour()
                                          + " " + entry.getValue().getMaxRTrecPerHour()
                                          + " " + entry.getValue().getAvgRTrecPerHour()
            );
        }
                
    }   
}
