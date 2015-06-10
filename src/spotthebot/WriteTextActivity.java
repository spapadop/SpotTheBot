package spotthebot;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

/**
 *
 * @author Sokratis
 */
public class WriteTextActivity {
    
    private static MongoDBHandler mongo;
    
    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, MongoException, UnknownHostException {
   
        mongo = new MongoDBHandler();
        int counter = 1;
        DBCursor cursor = mongo.getFollowedUsersColl().find(); //get all followed users
        
        while (cursor.hasNext()) { //for every user we have been following
            
            DBObject user = cursor.next(); //get one record
            System.out.println(counter + ". working on: " + user.get("id_str"));
            counter++;
            PrintWriter writer = new PrintWriter(user.get("id_str").toString()+".json", "UTF-8"); //create a text file for him
            
            BasicDBObject tweetsQuery = new BasicDBObject(); //make a query to get original tweets of user
            tweetsQuery.put("user_id", user.get("id_str"));
                        
            DBCursor tweets = mongo.getTweetsColl().find(tweetsQuery);
            int k=1;
            writer.println("======================================= ORIGINAL TWEETS BY USER ======================================");
            while (tweets.hasNext()) { //for every tweet
                DBObject tweet = tweets.next(); //store user object
                
                writer.println( k +". " + tweet.get("id_str")+ ": " + tweet.get("text"));
                k++;
            }
            
            BasicDBObject retweetsQuery = new BasicDBObject(); //make a query to count retweets of user
            retweetsQuery.put("retweetedUserID", user.get("id_str"));
            
            DBCursor retweets = mongo.getRetweetsColl().find(retweetsQuery);
            if(retweets.count()>0){
                k=1;
                writer.println();
                writer.println();
                writer.println("====================================== USER RETWEETED THESE TWEETS =====================================");
                while (retweets.hasNext()) { //for every tweet
                    DBObject retweet = retweets.next(); //store user object
                   
                    BasicDBObject quer = new BasicDBObject(); //make a query to count retweets of user
                    quer.put("id_str", retweet.get("originalTweetID"));
                    DBObject  tw = mongo.getTweetsColl().findOne(quer);
                    writer.println( k +". " + retweet.get("originalTweetID") + " >> " +retweet.get("originalUserID")+ ": " + tw.get("text"));
                    k++;
                }
            }
                
            writer.println("========================================================================================================");
            writer.close();
        }
    }
}
