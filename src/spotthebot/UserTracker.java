package spotthebot;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
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
 *
 * @author Sokratis
 */
public class UserTracker {

    // variables for handling nosql database Mongo
    private MongoClient mclient;
    private DB tweetsDB, trackedTweetsDB;
    private DBCollection tweetsColl, usersColl, retweetsColl, trackedTweetsColl;

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
        //startFiltering();
    }
    
    /**
     * creating a thread for running the tracking
     * @param highlyRTed 
     */
    public UserTracker(HashSet<Long> highlyRTed) {
//         this.highlyRTed = new HashSet<>();
        this.highlyRTed = highlyRTed;
        fq = new FilterQuery();
        configuration();
        initializeMongo();
        startListener();
        //startFiltering();
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
            tweetsDB = mclient.getDB("twitter");
            usersColl = tweetsDB.createCollection("users", null);
            tweetsColl = tweetsDB.createCollection("tweets", null);
            retweetsColl = tweetsDB.createCollection("retweets", null);

            //gets the followed users collection from mongodb
            trackedTweetsDB = mclient.getDB("trackedUsers");
            trackedTweetsColl = trackedTweetsDB.createCollection("trackedData", null);

        } catch (UnknownHostException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void update (HashSet<Long> newcomers){
        stream.shutdown();
        this.highlyRTed = newcomers;
        this.startListener();
    }
    
//    
//    public void renewList(HashSet<Long> newcomers){
//        if(highlyRTed!=null){
//            highlyRTed.clear();
//            highlyRTed = newcomers;
//        } else {
//            highlyRTed = newcomers;
//        }
//        startFiltering();
//    }

    
    /**
     *
     */
    public void startListener() {
        //System.out.println("Starts listener for tracking users");
        listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                User user = status.getUser();
                Long id = user.getId();
                boolean flag = false;
                if(highlyRTed!= null){
                    //System.out.println("exoume lista energi");
                    //System.out.println(highlyRTed.size());
//                    for (Long fuserID : highlyRTed) {
//                        System.out.println("to id tou trexontos user tweet: " +id);
//                        if (Objects.equals(id, fuserID)) {
//                            System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Found a tweet from a tracked user!");
//                            System.out.println("userID: " + fuserID);
//                            flag = true;
//                            break;
//                        }
//                    }
//                    if (flag) {
                    String json = DataObjectFactory.getRawJSON(status);
                    //System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Taking the JSON form of a tweet from tracked user!");
                    DBObject jsonObj = (DBObject) JSON.parse(json);
                    trackedTweetsColl.insert(jsonObj);
//                    }
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
            //stopStreaming();
            
            long userIDs[] = new long[highlyRTed.size()];
            int i = 0;
            for (Long id : highlyRTed) {
                userIDs[i++] = id;
            }
            if(userIDs.length>0){
                
                //print all userIDs that are going to be followed
//                for (int j=0; j<userIDs.length; j++) {
//                    System.out.println(userIDs[j]);
//                }
                
                fq.follow(userIDs); // follow users' activity

                stream = new TwitterStreamFactory(config).getInstance();
                stream.addListener(listener);
                stream.filter(fq);
            }
        }
    }

    /**
     * TODO the filter query
     *
     * @param listener
     */
    private void startFiltering() {
        
        System.out.println("Start Filtering:");
        
        long userIDs[] = new long[highlyRTed.size()];
        int i = 0;
        for (Long id : highlyRTed) {
            userIDs[i++] = id;
        }
        
        for (int j=0; j<userIDs.length; j++) {
            System.out.println(userIDs[j]);
        }

        fq.follow(userIDs);

        stream = new TwitterStreamFactory(config).getInstance();
        stream.addListener(listener);
        stream.filter(fq);
    }
    
    /**
     * Shut downs the crawling.
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
            System.out.println("$$$$$$$$$$$$$$$ Stream Stopped");
        }
    }
}
