package spotthebot;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.FilterQuery;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * Crawls random english tweets from Streaming API 
 * and stores them in appropriate mongoDB collections.
 * 
 * @author Sokratis Papadopoulos
 */
public class Crawler extends TimerTask {
    
    // variables for handling nosql database MongoDB
    private MongoDBHandler mongo;

    // variables for handling twitter Streaming API
    private TwitterStream stream;
    private Configuration config;
    private StatusListener listener;
    private FilterQuery fq;

    //private RetweetObserver users;
    private UserTracker trackingUsers;
    private int time;

    /**
     * Initializes a crawler object. 
     * Initializes basic variables and establishes connections
     * 
     * @throws JSONException
     * @throws MongoException 
     */
    public Crawler() throws JSONException {
        time=0;
        trackingUsers = null;
        configuration(); //configures my application as developer mode
        try {
            mongo = new MongoDBHandler(); //creates the database and collection in mongoDB
        } catch (UnknownHostException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MongoException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
        startListener(); //listener to Streaming API that will get the tweets
    }

    /**
     * The configuration details of our app as developer mode of TwitterAPI
     */
    private void configuration() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API
        config = cb.build();
        fq = new FilterQuery();
        stream = new TwitterStreamFactory(config).getInstance();
    }

    /**
     * Listener that will connect to Streaming API and receive the tweet stream.
     * Performs the storing to mongoDB collections for every retweet occured.
     */
    private void startListener() {

        listener = new StatusListener() {

            @Override
            public void onStatus(Status status) { //streaming tweets (not necessarily retweets)
                try {
                    String json = DataObjectFactory.getRawJSON(status); //gets the raw json form of tweet
                    
                    DBObject dbObj = (DBObject) JSON.parse(json);       //creates a DBObject out of json for mongoDB
                    JSONObject jObj = new JSONObject(dbObj.toString()); //creates a JSONObject out of the DBObject

                    if (!jObj.getJSONObject("retweeted_status").getString("id_str").isEmpty()) { //FOUND A RETWEET
                        
                        //========== STORING INTO LOCAL VARIABLES ============//
                        
                        Long originalUserID = status.getRetweetedStatus().getUser().getId(); //gets userID of original tweet
                        Long originalTweetID = status.getRetweetedStatus().getId(); //gets tweetID of original tweet
                        Long retweetedUserID = status.getUser().getId(); //gets the userID of retweeted user
                        Date at = status.getCreatedAt(); //gets date the retweet created (current)
                        
                        //====================================================//
                        
                        //===== INSERT RETWEET INTO RETWEETS COLLECTION ======//
                        //storing necessary details for every retweet occured 
                        //(like log file of RTs)
                        BasicDBObject document = new BasicDBObject();
                        document.put("originalTweetID", originalTweetID.toString()); //save original tweetID
                        document.put("originalUserID", originalUserID.toString());   //save original userID 
                        document.put("retweetedUserID", retweetedUserID.toString()); //save retweeter userID
                        document.put("created_at", at);                   //save retweet date
                        
                        mongo.addObjectToRetweetsColl(document); //insert onto mongoDB retweet collection
                        
                        //====================================================//
                        
                        //==== INSERT USERS INTO USERS COLLECTION ============//
                        
                        //original users
                        String originalUserDetails = jObj.getJSONObject("retweeted_status").getString("user"); //json user attribute of original user
                        DBObject originalUserToStore = (DBObject) JSON.parse(originalUserDetails);       //creates a DBObject out of json for mongoDB
                        mongo.addObjectToUsersColl(originalUserToStore);

                        //retweeter users
                        String retweeterUserDetails = jObj.getString("user"); //json user attribute of original user
                        DBObject retweeterUserToStore = (DBObject) JSON.parse(retweeterUserDetails);       //creates a DBObject out of json for mongoDB
                        mongo.addObjectToUsersColl(retweeterUserToStore); //insert retweeter user on mongoDB
                        
                        //====================================================//
                                                
                        //=== INSERT ORIGINAL TWEET INTO TWEETS COLLECTION ===//
                        
                        JSONObject tweetDetails = jObj.getJSONObject("retweeted_status"); 
                        tweetDetails.remove("user");
                        DBObject tweetToStore = (DBObject) JSON.parse(tweetDetails.toString()); 
                        tweetToStore.put("user_id", originalUserID.toString()); //put just id_str of original user
/*!!!!! DATE !!!!!!!!*/ //Date originalTweetDate = status.getRetweetedStatus().getCreatedAt(); //takes the date of original tweet
                        mongo.addObjectToTweetsColl(tweetToStore); //insert original tweet on mongoDB | rejects duplicates
                        
                        //====================================================//
                    }
                } catch (JSONException ex) {
                    //retweet_status not found -> not a retweet
                    //Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
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
                //Twitter4J Async Dispatcher
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };

        String keywords[] = {"I", "am", "is", "the", "a", "an", "and", "to", "we", "I", "it", "not", "so", "if", "go", "now", "come", "will", "on", "at", "do"};
        String lang[] = {"en"};

        fq.track(keywords);
        fq.language(lang);

        stream.addListener(listener);
        stream.filter(fq);
    }
    
    /**
     * Computes the time difference between two dates.
     * 
     * @param date1
     * @param date2
     * @param timeUnit
     * @return the time difference between dates
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * Runs every hour and checks for suspicious users to follow.
     * The list is being updated every time and so does the query to the
     * Steaming API with the wanted users.
     * 
     */
    @Override
    public void run() {
        System.out.println(time + " ****** PERISTASIAKOS ELEGXOS ********");
        List<String> suspicious = mongo.findSuspiciousUsers();            
        
        if(!suspicious.isEmpty()){
            System.out.println("We have the following suspicious users: ");
            for(String suser: suspicious)
                System.out.println(suser);
                    
            if (trackingUsers == null)  //first time list for users
                trackingUsers = new UserTracker(suspicious, mongo);
            else 
                trackingUsers.update(suspicious);
        } else {
            System.out.println("At the moment there are no suspicious users.");
        }
        
        System.out.println("-------------------------------------");   
        time++;
    }                
}