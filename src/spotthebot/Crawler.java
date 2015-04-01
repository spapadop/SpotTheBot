package spotthebot;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.FilterQuery;
//import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
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

import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;

/**
 * Crawls tweets from Streaming API.
 *
 * @author Sokratis Papadopoulos
 */
public class Crawler extends TimerTask {

    //private Twitter twitter;
    
    // variables for handling nosql database Mongo
    private MongoClient mclient;
    private DB tweetsDB;
    private DBCollection tweetsColl; //all info of tweets except user{}, where we just store the id_str
    private DBCollection usersColl; //all user{} info of json
    private DBCollection retweetsColl; //id_str of two users, createdAt & tweetID of original

    // variables for handling twitter Streaming API
    private TwitterStream stream;
    private Configuration config;
    private StatusListener listener;
    private FilterQuery fq;

    private RetweetObserver users;
    private UserTracker trackingUsers;

    public Crawler() throws JSONException {
        
        users = new RetweetObserver();
        trackingUsers = null;
        configuration(); // configures my application as developer mode
        initializeMongo(); // creates the database and collection in mongoDB
        startListener(); // listener to Streaming API that will get the tweets
    }

    /**
     * The configuration details of our application as developer mode of Twitter
     * API
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
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Listener that will connect to Streaming API and receive the tweet stream
     */
    private void startListener() {

        listener = new StatusListener() {

            @Override
            public void onStatus(Status status) { //streaming tweets (not necessarily retweets)
                try {

                    //User user = status.getRetweetedStatus().getUser();//user whose tweet was retweeted
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
                        JSONObject obj = new JSONObject();
                        obj.put("originalUserID", originalUserID);
                        obj.put("retweetedUserID", retweetedUserID);
                        obj.put("created_at", at);
                        obj.put("originalTweetID", originalTweetID);
                        
                        DBObject dbobj = (DBObject) obj;
                        retweetsColl.insert(dbobj);
                        //====================================================//
                        
                        //==== INSERT ORIGINAL USER INTO USERS COLLECTION ====//
                        if(!checkIfUserExists(originalUserID, originalTweetID, at)){
                            DBObject originalUserDB = (DBObject) jObj.getJSONObject("retweeted_status").getJSONObject("user"); //json user attribute of original user
                            usersColl.insert(originalUserDB);   //insert original user on mongoDB
                        }
                        //====================================================//
                        
                        //=== INSERT RETWEETED USER INTO USERS COLLECTION ====//
                        //checkIfUserExists(retweetedUserID, originalTweetID, at);
                        //DBObject RTuserDB = (DBObject) jObj.getJSONObject("user"); //json user attribute of retweeted user
                        //usersColl.insert(RTuserDB);         //insert retweeted user on mongoDB
                        //====================================================//
                        
                        //=== INSERT ORIGINAL TWEET INTO TWEETS COLLECTION ===//
                        if(!checkIfTweetIDExists(originalTweetID)){ //if tweetID doesnt exist
                            JSONObject originalTweetJSON = jObj.getJSONObject("retweeted_status");

                            //replace whole user json attribute, with just userID
                            originalTweetJSON.remove("user");
                            originalTweetJSON.put("user", originalUserID);
                            DBObject originalTweetDB = (DBObject) originalTweetJSON;

                            tweetsColl.insert(originalTweetDB); //insert original tweet on mongoDB    
                        }
                        //====================================================//
                        
                    }

                } catch (JSONException ex) {
                    // Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
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
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };

        String keywords[] = {"I", "am", "is", "the", "a", "an", "and", "to", "we", "I", "it", "not", "so", "if", "go", "now", "come", "will", "on", "at"};
        String lang[] = {"en"};

        fq.track(keywords);
        fq.language(lang);

        stream.addListener(listener);
        stream.filter(fq);
    }

    /**
     * Creates a filter for getting the tweets with spam phrases from Twitter
     * Streaming API
     */
//    private void crawlStreamWithFilter() {
//
//        stream = new TwitterStreamFactory(config).getInstance();
//        stream.addListener(listener);
//        stream.filter(fq);
//    }
    
    /**
     * Checks if user exists or not and perform the proper actions.
     * @param checkingID
     * @param tweetID
     * @param at
     * @return true if user exists, false if doesnt exist.
     */
    public boolean checkIfUserExists(Long checkingID, Long tweetID, Date at){
                
        if(users.getUniqueUsers().contains(checkingID)){ //if user exists
            
            int pos = users.getUsersColl().indexOf(checkingID);
            if(pos != -1){
                users.getUsersColl().get(pos).update(tweetID, at);
                System.out.println("Existing user of usersColl updated!" + " at pos: " + pos);
            } else {
                System.out.println("User doesnt exist! --> indexOf returned -1");
            }
            return true;
            
        } else { //if user doesnt exist
            users.getUniqueUsers().add(checkingID);
            users.getUsersColl().add(new TwitterUser(checkingID, tweetID, at));
            //System.out.println("New user added to usersColl");
            return false;
        }
    }
    
    
    /**
     * 
     * @param checkingID
     * @return 
     */
    public boolean checkIfTweetIDExists(Long checkingID){
        if(users.getUniqueTweetIDs().contains(checkingID)){ //if tweet exists
            return true;
        } else {
            users.getUniqueTweetIDs().add(checkingID);
            return false;
        }
    }

    /**
     * 
     * @param date1
     * @param date2
     * @param timeUnit
     * @return 
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    /**
     * 
     */
    @Override
    public void run() {
        System.out.println("****** PERISTASIAKOS ELEGXOS ********");

        //users.printAll();
        if (!users.getUsersColl().isEmpty()) {

            for (TwitterUser fuser : users.getUsersColl()) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);
                    Date lastRTedTime = dateFormat.parse(fuser.getLastRTed().toString());

                    Date currentTime = new Date();
                    dateFormat.format(currentTime);

                    if (getDateDiff(lastRTedTime, currentTime, TimeUnit.DAYS) <= 7 && fuser.getRetweetsReceived() > 20) {
                        users.getHighlyRTed().add(fuser.getUserID());
                    } else {
                        users.getHighlyRTed().remove(fuser.getUserID());
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //System.out.println("--------------- printing highly RTed! ---------------------");
//            for (Long id : users.getHighlyRTed()) {
//                System.out.println(id);
//            }

            //System.out.println("------------------------------------------------------------");

            if (users.getHighlyRTed() != null) {
                
                if (trackingUsers == null) { //first time list for users
                    if(users.getHighlyRTed().size() >1){
                        trackingUsers = new UserTracker(users.getHighlyRTed());
                    }
                } else { //update existing list of users
                    System.out.println("><><>< Update List");
                    trackingUsers.update(users.getHighlyRTed());
                    for (Long id : users.getHighlyRTed()) {
                        System.out.println(id);
                    }
                }
            }

        } else {
            //System.out.println("List of highly retweeted users currently empty");
        }
    }
}
