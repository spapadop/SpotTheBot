package analysis;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import crawling.MongoDBHandler;
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
 * Gathers the retweet activity of all users found in database and prints it in
 * a text file.
 *
 * @author Sokratis Papadopoulos
 */
public class UsersRetweetActivity {

    private MongoDBHandler mongo; //connect with database
    private HashMap<Long, UserEntry> users; // 

    public UsersRetweetActivity() throws UnknownHostException, ParseException, FileNotFoundException, UnsupportedEncodingException {

        users = new HashMap<>();
        mongo = new MongoDBHandler();

        DBCursor cursor = mongo.getRetweetsColl().find(); //get all (re)tweets in our database
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss Z YYYY");

        while (cursor.hasNext()) {
            DBObject rt = cursor.next();
            long userO = Long.parseLong(rt.get("originalUserID").toString());
            long userRT = Long.parseLong(rt.get("retweetedUserID").toString());
            Date when = format.parse(rt.get("created_at").toString());

            if (users.containsKey(userO)) {
                users.get(userO).newTweet(when, false); //false for receiving retweet
            } else {
                users.put(userO, new UserEntry(when, false));
            }

            if (users.containsKey(userRT)) {
                users.get(userRT).newTweet(when, true); //true for doing retweet
            } else {
                users.put(userRT, new UserEntry(when, true));
            }

        }

        //finish up - calculate AVG
        for (UserEntry u : users.values()) {
            u.finish();
        }

        System.out.println("size of hashmap: " + users.size());

        //checking if we missed some users
//        DBCursor c = mongo.getUsersColl().find(); //get all (re)tweets in our database
//        while (c.hasNext()) {
//            DBObject u = c.next(); 
//            long id = Long.parseLong(u.get("id_str").toString());
//            if (!users.containsKey(id))
//                System.out.println("User: "+ id + " does not exist !!!");
//        }
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
        writer.close();
    }
}
