package spotthebot;

import com.mongodb.BasicDBObject;
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
    private MongoClient mclient;
    private DB randomDB;
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
        configuration(); //configures my application as developer mode
        initializeMongo(); //creates the database and collection in mongoDB
        startListener(); //listener to Streaming API that will get the tweets
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
            randomDB = mclient.getDB("random");
            usersColl = randomDB.getCollection("users");
            tweetsColl = randomDB.getCollection("tweets");
            retweetsColl = randomDB.getCollection("retweets");
            
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
        usersColl.createIndex(new BasicDBObject("id_str", 1));

        // create index on "creating times of original tweets, ascending
        tweetsColl.createIndex(new BasicDBObject("created_at", 1));  

        // create index on tweetID of the original tweets, ascending | prevents duplicates
        tweetsColl.createIndex(new BasicDBObject("id", 1));  

        // create index on "id_str", ascending
        //retweetsColl.createIndex(new BasicDBObject("created_at", 1));  

        //usersColl.ensureIndex(new BasicDBObject("url", 1), new BasicDBObject("unique", true));
        //usersColl.ensureIndex(new BasicDBObject("id_str", 1), null, true);
        //tweetsColl.createIndex(new BasicDBObject("retweet_count", 1), new BasicDBObject("unique", true));
        //usersColl.ensureIndex(new BasicDBObject("id_str", 1));
        //usersColl.createIndex(new BasicDBObject("id_str", 1).append("unique", true));  
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
                        document.put("originalTweetID", originalTweetID); //save original tweetID
                        document.put("originalUserID", originalUserID);   //save original userID 
                        document.put("retweetedUserID", retweetedUserID); //save retweeter userID
                        document.put("created_at", at);                   //save retweet date
                        
                        retweetsColl.insert(document); //insert onto mongoDB retweet collection
                        
                        //====================================================//
                        
                        //==== INSERT USERS INTO USERS COLLECTION ============//
                        //original users
                        
                        //if(!users.checkIfUserExists(originalUserID, originalTweetID, at)){
                            String originalUserDetails = jObj.getJSONObject("retweeted_status").getString("user"); //json user attribute of original user
                            DBObject originalUserToStore = (DBObject) JSON.parse(originalUserDetails);       //creates a DBObject out of json for mongoDB
                            usersColl.insert(originalUserToStore);   //insert original user on mongoDB

                            users.updateListOfUsers(originalUserID, originalTweetID, at);
                        //}
                        
                        //retweeter users
                        String retweeterUserDetails = jObj.getString("user"); //json user attribute of original user
                        DBObject retweeterUserToStore = (DBObject) JSON.parse(retweeterUserDetails);       //creates a DBObject out of json for mongoDB
                        usersColl.insert(retweeterUserToStore);   //insert original user on mongoDB
                        
                        //====================================================//
                                                
                        //=== INSERT ORIGINAL TWEET INTO TWEETS COLLECTION ===//
                        //if(!users.checkIfTweetIDExists(originalTweetID)){
                            JSONObject tweetDetails = jObj.getJSONObject("retweeted_status"); 
                            tweetDetails.remove("user"); 
                            DBObject tweetToStore = (DBObject) JSON.parse(tweetDetails.toString()); 
                            tweetToStore.put("user_id", originalUserID); //put just id_str of original user
                            tweetsColl.insert(tweetToStore); //insert original tweet on mongoDB | rejects duplicates
                        //}
                        
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
     * Computes the time difference between two dates
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
     * Runs every hour and checks for highlyRTed users to follow.
     * The list is being updated every time and so does the query to the
     * Steaming API with the wanted users.
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

                    //if the last retweet occured less than 7 days ago and the user has received more than 20 retweets in a tweet
                    if (getDateDiff(lastRTedTime, currentTime, TimeUnit.DAYS) <= 5 && fuser.getRetweetsReceived() > 20) {
                        users.getHighlyRTed().add(fuser.getUserID());
                    } else {
                        users.getHighlyRTed().remove(fuser.getUserID());
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            

            if (users.getHighlyRTed() != null) {
                
                if (trackingUsers == null) { //first time list for users
                    if(users.getHighlyRTed().size() >0){
                        trackingUsers = new UserTracker(users.getHighlyRTed());
                    }
                } else { //update existing list of users
                    System.out.println("><><>< Update List");
                    trackingUsers.update(users.getHighlyRTed());
                }
            }
            
            System.out.println("--------------- printing highly RTed! ---------------------");
            for (Long id : users.getHighlyRTed()) { System.out.println(id); }
            System.out.println("------------------------------------------------------------");

        } else {
            System.out.println("List of highly retweeted users currently empty");
        }
    }
}
