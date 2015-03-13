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
 * Crawls tweets from Streaming API.
 *
 * @author Sokratis Papadopoulos
 */
public class Crawler extends TimerTask {

//    private Twitter twitter;
    // variables for handling nosql database Mongo
    private MongoClient mclient;
    private DB tweetsDB;
    private DBCollection tweetsColl;

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
            tweetsDB = mclient.getDB("tweets");
            tweetsColl = tweetsDB.createCollection("tweetsColl", null);
        } catch (UnknownHostException ex) {
            Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Listener that will connect to Streaming API and receive the tweet stream
     */
    private void startListener() {

        //I use Twitter4J library to connect to Twitter API
        //twitter=new TwitterFactory(config).getInstance();
        listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                try {

                    //User user = status.getRetweetedStatus().getUser(); //user whose tweet was retweeted
                    String json = DataObjectFactory.getRawJSON(status);
                    DBObject jsonObj = (DBObject) JSON.parse(json);

                    JSONObject jobj = new JSONObject(jsonObj.toString());

                    if (!jobj.getJSONObject("retweeted_status").getString("id_str").isEmpty()) { //FOUND A RETWEET

                        Long userID = status.getRetweetedStatus().getUser().getId(); //gets userID of original tweet
                        Date at = status.getCreatedAt(); //gets date the retweet created (current)
                        Long tweetID = status.getRetweetedStatus().getId(); //gets original tweetID

                        //System.out.println("RETWEET of the original: User: " +userID + " tweetID: "+ tweetID + " at: " + at);    
                        tweetsColl.insert(jsonObj); //insert retweet on mongoDB

                        boolean flag = false;
                        //check if user already exist in our database
                        for (Long id : users.getUniqueUsers()) {
                            if (Objects.equals(id, userID)) {
                                flag = true;
                                break;
                            }
                        }

                        if (flag) { //if user exists
                            int pos = 0;
                            for (TwitterUser fuser : users.getUsersColl()) {
                                if (Objects.equals(fuser.getUserID(), userID)) {
                                    break;
                                }
                                pos++;
                            }
                            users.getUsersColl().get(pos).update(tweetID, at);
                            //System.out.println("Existing user of usersColl updated!" + " at pos: " + pos);

                        } else { //if user doesnt exist
                            users.getUniqueUsers().add(userID);
                            users.getUsersColl().add(new TwitterUser(userID, tweetID, at));
                            //System.out.println("New user added to usersColl");
                        }
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

        String keywords[] = {"greece", "buy", "now", "yes", "money", "xxx", "bet"};
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

    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

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

                    if (getDateDiff(lastRTedTime, currentTime, TimeUnit.DAYS) <= 7 && fuser.getRetweetsReceived() > 4) {
                        users.getHighlyRTed().add(fuser.getUserID());
                    } else {
                        users.getHighlyRTed().remove(fuser.getUserID());
                    }
                } catch (ParseException ex) {
                    Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            System.out.println("--------------- printing highly RTed! ---------------------");
            for (Long id : users.getHighlyRTed()) {
                System.out.println(id);
            }

            //System.out.println("------------------------------------------------------------");

            if (users.getHighlyRTed() != null) {
                
                if (trackingUsers == null) { //first time list for users
                    if(users.getHighlyRTed().size() >1){
                        trackingUsers = new UserTracker(users.getHighlyRTed());
                    }
                    
                } else { //update existing list of users
                    //trackingUsers.update(users.getHighlyRTed());
                }
            }

        } else {
            System.out.println("List of highly retweeted users currently empty");
        }
    }
}
