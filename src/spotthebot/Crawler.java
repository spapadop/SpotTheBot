package spotthebot;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * Crawls tweets from Streaming API using as filter spam phrases.
 * @author Sokratis Papadopoulos
 */
public class Crawler {
    
    private Twitter twitter;
    
    // variables for handling nosql database Mongo
     private MongoClient mclient;
     private DB tweetsDB;
     private DBCollection tweetsColl;
     
     // variables for handling twitter Streaming API
     private TwitterStream stream;
     private StatusListener listener;
     private Configuration config;
     private FilterQuery fq;
     
     public Crawler(){
        configuration(); // configures my application as developer mode
        initializeMongo(); // creates the database and collection in mongoDB
        startListener(); // listener to Streaming API that will get the tweets
        startFiltering(); // filter query to only take the tweets that include spam phrases
    }
    
     /**
      * The configuration details of our application as developer mode of Twitter API
      */
    private void configuration() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API
        config=cb.build();
    }
    
    /**
    * Initializing the attributes of MongoDB.
    * First I create a MongoClient object (mclient) and then the database (tweets) and the collection in it (tweetsColl).
    */
    private void initializeMongo(){        
        try {
            mclient = new MongoClient("localhost",27017);
            tweetsDB = mclient.getDB("tweets");
            tweetsColl = tweetsDB.createCollection("tweetsColl", null);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Listener that will connect to Streaming API and receive the tweet stream
     */
    private void startListener(){
        //I use Twitter4J library to connect to Twitter API
        twitter=new TwitterFactory(config).getInstance();
        
        listener=new StatusListener() {

            @Override
            public void onStatus(Status status) {
                String json=DataObjectFactory.getRawJSON(status);
                DBObject jsonObj=(DBObject) JSON.parse(json);
                tweetsColl.insert(jsonObj);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public void onTrackLimitationNotice(int i) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public void onScrubGeo(long l, long l1) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }

            @Override
            public void onException(Exception excptn) {
                throw new UnsupportedOperationException("Not supported yet."); 
            }
        };
    }
    
    /**
     * Creates a filter for getting the tweets with spam phrases from Twitter Streaming API
     */
    private void startFiltering(){
        fq=new FilterQuery(); 
        //read a file of spam words and put them in keywords[]
        String keywords[]=new String[100];
        keywords[0] = "free";
        keywords[1] = "now";
        fq.track(keywords);
        
        stream=new TwitterStreamFactory(config).getInstance();
        stream.addListener(listener);
        stream.filter(fq);
    }
}
