package spotthebot;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.JSONException;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Runs through the list of tweets in our database and analyzes the data,
 * extracting the users that had more retweets.
 *
 * @author Sokratis Papadopoulos
 */
public class RetweetObserver {

    // variables for handling nosql database Mongo
    private MongoClient mclient;
    private DB tweetsDB;
    private DBCollection tweetsColl;

    private Configuration config;

    //private ArrayList<TwitterUser> usersColl;
    private HashSet<Long> uniqueUsers; //used to check if a user has already occured in the database
    private ArrayList<TwitterUser> usersColl; //all users found in our database
    private HashSet<Long> highlyRTed; //users that have highly retweeted tweets

    /**
     *
     */
    public RetweetObserver() throws JSONException {
        //initializing the lists
        usersColl = new ArrayList<>();
        uniqueUsers = new HashSet<>();
        highlyRTed = new HashSet<>();

        //configuration();
        //initializeMongo();
        //calculateRTs();
    }

    /**
     * The configuration details of our application as developer mode of Twitter
     * API
     */
    private void configuration() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API
        config = cb.build();
    }

    /**
     * Initializing the attributes of MongoDB. First I create a MongoClient
     * object (mclient) and then the database (tweets) and the collection in it
     * (tweetsColl).
     */
    private void initializeMongo() {
        try {
            mclient = new MongoClient("localhost", 27017);
            tweetsDB = mclient.getDB("tweets");
            tweetsColl = tweetsDB.createCollection("tweetsColl", null);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashSet<Long> getUniqueUsers() {
        return uniqueUsers;
    }

    public ArrayList<TwitterUser> getUsersColl() {
        return usersColl;
    }

    public HashSet<Long> getHighlyRTed() {
        return highlyRTed;
    }

    public void printAll() {
        for (TwitterUser user : usersColl) {
            System.out.println("UserID: " + user.getUserID());
            System.out.println("Retweets received: " + user.getRetweetsReceived());
            System.out.println("Last RTed: " + user.getLastRTed());
            Iterator it = user.getTimesRetweeted().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                System.out.println(pair.getKey() + " = " + pair.getValue());
            }
            System.out.println("---------------------");
        }
    }

}






   /*
    private void calculateRTs() throws JSONException{
        DBCursor cursor = tweetsColl.find(); //get a cursor that will run throughout the collection.
        
        //starts the loop throughout the collection of tweets
        while(cursor.hasNext()){
            DBObject obj = cursor.next();  
            JSONObject jobj=new JSONObject(obj.toString());
            //TwitterUser user = new TwitterUser(); //at the end create the TwitterUser
            int pos=0;
            
            String userID= jobj.getJSONObject("user").getString("id_str"); //gets the userID
            
            if(uniqueUsers.containsKey(userID)){ //if user exists already in our ArrayList usersColl
                usersColl.get(uniqueUsers.get(userID)).increaseTweets();
            }else{
                uniqueUsers.put(userID, pos);
                pos++;
                //usersColl.add(new TwitterUser(userID, 1));
            }
                      
                    
            uniqueUsers.get(userID);
            //TODO: find highly RTed tweets.
            // and add them to highlyRTed list
            
            if(jobj.has("retweeted_status")){
                //user.increaseRetweets();
            }
        }
    }
    */
