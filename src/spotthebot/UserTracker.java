package spotthebot;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * Crawling the activity of specific followed users list.
 * The list is being updated at specific times.
 * 
 * @author Sokratis Papadopoulos
 */
public class UserTracker {

    // variables for handling nosql database Mongo
    private MongoClient mclient;
    private DB randomDB, followedDB;
    private DBCollection tweetsColl, usersColl, retweetsColl;
    private DBCollection tweetsByUsersColl, followedUsersColl, tweetsRetweetedByUsersColl, repliesToUsersTweetsColl, retweetsOfUsersTweetsColl, repliesByUsersColl;
    private DBCollection followedResultsColl;
    
    // variables for handling twitter Streaming API
    private TwitterStream stream;
    private StatusListener listener;
    private Configuration config;
    private FilterQuery fq;

    private HashSet<Long> highlyRTed;

    public UserTracker() {
        this.highlyRTed = null;
        fq = new FilterQuery();
        configuration();
        initializeMongo();
        startListener();
    }
    
    /**
     * creating a thread for running the tracking
     * @param highlyRTed 
     */
    public UserTracker(HashSet<Long> highlyRTed) {
        this.highlyRTed = highlyRTed;
        fq = new FilterQuery();
        configuration();
        initializeMongo();
        startListener();
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
        
        //stream = new TwitterStreamFactory(config).getInstance();
    }

    /**
     * Initializing the attributes of MongoDB. First I create a MongoClient
     * object (mclient) and then the database (tweets) and the collection in it
     * (tweetsColl).
     */
    private void initializeMongo() {
        try {
            mclient = new MongoClient("localhost", 27017);
            randomDB = mclient.getDB("random");
            usersColl = randomDB.getCollection("users");
            tweetsColl = randomDB.getCollection("tweets");
            retweetsColl = randomDB.getCollection("retweets");
            
            followedDB = mclient.getDB("followed");
            followedResultsColl = followedDB.getCollection("results"); //all things returned by API
            //followedUsersColl = followedDB.getCollection("users"); //users that were followed
            //tweetsByUsersColl = followedDB.getCollection("tweets"); //tweets created by followed users
            //tweetsRetweetedByUsersColl = followedDB.getCollection("retweets"); //tweets that were RTed by followed users
            //repliesToUsersTweetsColl = followedDB.getCollection("replies"); //replies to any tweet created by followed users
            //retweetsOfUsersTweetsColl = followedDB.getCollection("retweetsToUsersTweets"); //retweets of any tweet created by followed users
            //repliesByUsersColl = followedDB.getCollection("repliesByUsers"); //manual replies, created by followed users
            
            //createIndexes();

        } catch (UnknownHostException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Creates the indexes used in mongoDB to fasten the queries
     * TODO: Unique constraint doesn't work.
     */
    private void createIndexes(){
                        
        // create index on userIDs, ascending | prevents duplicates
        BasicDBObject query = new BasicDBObject("id_str", 1).append("unique", "true");
        usersColl.createIndex(query);

        // create index on "creating times of original tweets, ascending
        tweetsColl.createIndex(new BasicDBObject("created_at", 1));  

        // create index on tweetID of the original tweets, ascending | prevents duplicates
        tweetsColl.createIndex(new BasicDBObject("id", 1).append("unique", true));  

        // create index on "id_str", ascending
        //retweetsColl.createIndex(new BasicDBObject("created_at", 1));      
        
    }
    
    /**
     * Shutdowns the previous query to API, updates the following list and
     * starts over the query with the renewed list.
     * 
     * @param newcomers 
     */
    public void update (HashSet<Long> newcomers){
        stream.shutdown();
        this.highlyRTed = newcomers;
        this.startListener();
    }
    
    /**
     *Receives all information about the list of following users.
     * Stores that information at different collections in mongoDB.
     */
    public void startListener() { //System.out.println("Starts listener for tracking users");
        
        listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                User user = status.getUser();
                Long id = user.getId();
                
                if(highlyRTed!= null){

                    String json = DataObjectFactory.getRawJSON(status);
                    DBObject jsonObj = (DBObject) JSON.parse(json);
                    followedResultsColl.insert(jsonObj);
                    
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onTrackLimitationNotice(int i) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onScrubGeo(long l, long l1) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onException(Exception excptn) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        if(highlyRTed!=null){
            addFilter();
        }
    }
    
    /**
     * Prepares the appropriate users list to follow their activity.
     * Performs the query in Streaming API.
     */
    private void addFilter(){
        
        long userIDs[] = new long[highlyRTed.size()];
        int i = 0;
        
        for (Long id : highlyRTed) {
            userIDs[i++] = id;
        }
        
        if(userIDs.length>0){

            fq.follow(userIDs); // follow users' activity

            stream = new TwitterStreamFactory(config).getInstance();
            stream.addListener(listener);
            stream.filter(fq);
            
//          //print all userIDs that are going to be followed
//          for (int j=0; j<userIDs.length; j++) {
//              System.out.println(userIDs[j]);
//          }
        }
    }
    
    /**
     * Shut downs the crawling for current list of followed users.
     * alternative if simple shutdown dont work.
     */
    private void stopStreaming() {

        if (stream == null) {
            return;
        }
        
        stream.addConnectionLifeCycleListener(new ConnectionLifeCycleListener() {

            @Override
            public void onConnect() {

            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onCleanUp() {
                stream = null;
            }
        });

        stream.shutdown();
        

        while (stream != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        if (stream == null) {
            System.out.println("Stream Stopped");
        }
    }
}
