package analysis;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import twitter4j.JSONException;

import crawling.MongoDBHandler;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import twitter4j.TwitterException;

/**
 * This is the main class which runs the whole analysis of the project. It
 * connects with the database and goes through all the analysis we have
 * implemented in the other classes.
 *
 * @author Sokratis Papadopoulos
 */
public class RunAnalysis {

    public static void main(String[] args) throws UnknownHostException, ParseException, FileNotFoundException, UnsupportedEncodingException, IOException, MongoException, URISyntaxException, JSONException, TwitterException {

        AnnotationUser u = new AnnotationUser();
        
        
//        AnnotationSupport checkUsers = new AnnotationSupport();
//                
//        //create task to be executed every 1 hour
//        Timer time = new Timer();
//        time.schedule(checkUsers, 0, 1800000);
        
//        MongoDBHandler m = new MongoDBHandler();
//        DBCursor cursor = m.getFollowedUsersColl().find();
//
//        while (cursor.hasNext()) { //for every user
//
//            DBObject user = cursor.next(); //store user object
//
//            BasicDBObject tweetsQuery = new BasicDBObject(); //make a query to count tweets of user
//            tweetsQuery.put("user_id", user.get("id_str"));
//
//            BasicDBObject retweetsQuery = new BasicDBObject(); //make a query to count retweets of user
//            retweetsQuery.put("originalUserID", user.get("id_str"));
//
//            // na vgalw to active
//            // kai na kanw queries mono gia tis teleutaies 10 wres
//            if (m.getTweetsColl().count(tweetsQuery) > 25 && m.getRetweetsColl().count(retweetsQuery) > 1000) { //if high number of tweets & retweets then set as guilty
//                
//            } else {
//                System.out.println(user.get("id_str") + " " + m.getTweetsColl().count(tweetsQuery) + " " + m.getRetweetsColl().count(retweetsQuery));
//            }
//        }
        
        //========== DIRECTLY FROM DATASET (MONGODB) ========
        //CalculateChunksAndWindows first = new CalculateChunksAndWindows();
        //UsersStats second = new UsersStats();
        //SuspiciousTweetActivity third = new SuspiciousTweetActivity();
        //UsersRetweetActivity fourth = new UsersRetweetActivity();
        //FeaturesAnalysis fa = new FeaturesAnalysis(true, true);

        //========== ANALYZE TIME-WINDOWS DATA ============
        //UsersWithCharacteristics fifth = new UsersWithCharacteristics();
        //ConvertToOccurrences sixth = new ConvertToOccurrences();
        //MetricsPerFeature seventh = new MetricsPerFeature();
        
        //========== NORMALIZE TIME-WINDOWS DATA ============
        //NormalizationOfData fin = new NormalizationOfData();
        //NormalizedOccurrences fin2 = new NormalizedOccurrences();
        
        //========== ANALYSE OUTLIERS =========================
        //OutliersAnalysis an = new OutliersAnalysis();
    }
}
