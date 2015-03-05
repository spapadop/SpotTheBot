/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    private DBCollection tweetsColl, trackedTweetsColl;

    // variables for handling twitter Streaming API
    private TwitterStream stream;
    private StatusListener listener;
    private Configuration config;
    private FilterQuery fq;

    private HashSet<Long> highlyRTed;

    public UserTracker() {
//         highlyRTed = new ArrayList<>();
        configuration();
        initializeMongo();
        startListener();
        //startFiltering();
    }

    public UserTracker(HashSet<Long> highlyRTed) {
//         this.highlyRTed = new HashSet<>();
        this.highlyRTed = highlyRTed;
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

            //gets the followed users collection from mongodb
            trackedTweetsDB = mclient.getDB("TrackedTweets");
            trackedTweetsColl = trackedTweetsDB.createCollection("trackedTweetsColl", null);

        } catch (UnknownHostException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    public void startListener() {

        listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                User user = status.getUser();
                Long id = user.getId();
                boolean flag = false;
                for (Long fuserID : highlyRTed) {
                    if (Objects.equals(id, fuserID)) {
                        System.out.println("Found a tweet from a tracked user!");
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    String json = DataObjectFactory.getRawJSON(status);
                    System.out.println("Taking the JSON form of a tweet from tracked user!");
                    DBObject jsonObj = (DBObject) JSON.parse(json);
                    trackedTweetsColl.insert(jsonObj);
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
               
        //fq = new FilterQuery();

        long userIDs[] = new long[highlyRTed.size()];
        int i = 0;
        for (Long id : highlyRTed) {
            userIDs[i++] = id;
        }

        //fq.follow(userIDs);
        //fq.track(userIDs);
        
        fq = new FilterQuery(0, userIDs);

        stream = new TwitterStreamFactory(config).getInstance();
        stream.addListener(listener);
        stream.filter(fq);
        

    }

    /**
     * TODO the filter query
     *
     * @param listener
     */
    private void startFiltering() {
        fq = new FilterQuery();

        long userIDs[] = new long[highlyRTed.size()];
        int i = 0;
        for (Long id : highlyRTed) {
            userIDs[i++] = id;
        }

        fq.follow(userIDs);

        stream = new TwitterStreamFactory(config).getInstance();
        stream.addListener(listener);
        stream.filter(fq);
    }

}
