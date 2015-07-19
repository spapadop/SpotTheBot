package crawling;

import analysis.FindUsersWithCharacteristics;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Timer;
import twitter4j.JSONException;

/**
 * This project is aiming to follow highly retweeted users activity in Twitter.
 * It uses Twitter Streaming API, in order to analyze tweets and get those
 * highly retweeted users. Then, it follows their activity and analyze
 * their behavior. Database will be useful for further analysis.
 * @author Sokratis Papadopoulos
 */
public class SpotTheBot {
    
    private static final int TASK_REPEAT = 3600000; // one hour: 3600000 | time-repeat of checking task for updating list of potential spammers | 60.000 milliseconds = 1 minute
    
    public static void main(String[] args) throws JSONException, InterruptedException, MongoException, UnknownHostException, ParseException, FileNotFoundException, UnsupportedEncodingException {
   
//        MongoDBHandler mongo = new MongoDBHandler();
//        HashSet<Long> ids = new HashSet<>();
//        int count =0;
//        DBCursor c = mongo.getUsersColl().find(); //get all (re)tweets in our database
//        while (c.hasNext()) {
//            DBObject u = c.next(); 
//            long id = Long.parseLong(u.get("id_str").toString());
//            if (ids.contains(id))
//                count++;
//            else
//                ids.add(id);
//        }
//        
//        System.out.println("counter: " + count);
//        System.out.println("size of hash: " + ids.size());
//          ConvertToOccurrences an = new ConvertToOccurrences();
        
        FindUsersWithCharacteristics a = new FindUsersWithCharacteristics();
        
//        Crawler crawl = new Crawler(); 
//
//        //create task to be executed every 1 hour
//        Timer time = new Timer(); 
//        time.schedule(crawl, 0, TASK_REPEAT);
        
    }
}
