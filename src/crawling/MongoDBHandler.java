package crawling;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Handles everything regarding Mongo databases used for the application. It
 * creates 2 databases, one for the random tweets and one for the activity of
 * the suspicious users as we define them. It handles all additions/searches and
 * basically any interaction with DBs.
 *
 * @author Sokratis Papadopoulos
 */
public class MongoDBHandler {

    private MongoClient m;

    private DB randomDB; //random collection from API
    private DBCollection usersColl; //all user{} info of json
    private DBCollection tweetsColl; //all info of tweets except user{}, where we just store the user_id
    private DBCollection retweetsColl; //IDs of two users involved in retweet, createdAt of retweet & tweetID of original tweet

    private DB followedDB; //focused observing on suspicious users
    private DBCollection followedUsersColl; //storing all users details + starting and finish time of observing
    private DBCollection followedActivityColl; //storing all the activity of followed users
    // private DBCollection tweetsByUsersColl, followedUsersColl, tweetsRetweetedByUsersColl, repliesToUsersTweetsColl, retweetsOfUsersTweetsColl, repliesByUsersColl;

    private static final int MINUTES_OF_INACTIVITY_THRESHOLD = 600;

    /**
     * Constructor that creates the appropriate DB environment. It also orders
     * the creation of indexes in our collections.
     *
     * @throws UnknownHostException
     * @throws MongoException
     */
    public MongoDBHandler() throws UnknownHostException, MongoException {
        m = new MongoClient("localhost", 27017);

        randomDB = m.getDB("random");
        usersColl = randomDB.getCollection("users");
        tweetsColl = randomDB.getCollection("tweets");
        retweetsColl = randomDB.getCollection("retweets");

        followedDB = m.getDB("followed");
        followedUsersColl = followedDB.getCollection("users");
        followedActivityColl = followedDB.getCollection("activity");

        createIndexes(); //creates the (unique) indexes in all our collections
    }

    /**
     * Add (unique) indexes to collections.
     *
     */
    private void createIndexes() {

        //---- For random crawling
        usersColl.ensureIndex(new BasicDBObject("id_str", 1), new BasicDBObject("unique", true));

        tweetsColl.ensureIndex(new BasicDBObject("id_str", 1), new BasicDBObject("unique", true));
        tweetsColl.ensureIndex(new BasicDBObject("user_id", 1));
        tweetsColl.ensureIndex(new BasicDBObject("created_at", 1));

        retweetsColl.ensureIndex(new BasicDBObject("originalTweetID", 1));
        retweetsColl.ensureIndex(new BasicDBObject("originalUserID", 1));
        retweetsColl.ensureIndex(new BasicDBObject("retweetedUserID", 1));
        //retweetsColl.ensureIndex(new BasicDBObject("created_at", 1));

        //--- For followed users crawling
        followedUsersColl.ensureIndex(new BasicDBObject("id_str", 1), new BasicDBObject("unique", true));
        //followedActivityColl.ensureIndex(new BasicDBObject("id_str", 1), new BasicDBObject("unique", true));
    }

    /**
     * Adds object to retweets collection.
     *
     * @param obj
     * @return true if succeed, false elsewhere
     */
    public boolean addObjectToRetweetsColl(DBObject obj) {
        try {
            retweetsColl.insert(obj);
        } catch (MongoException e) { //if there is a duplicate
            return false;
        }
        return true;
    }

    /**
     * Adds object to tweets collection.
     *
     * @param obj
     * @return true if succeed, false elsewhere
     */
    public boolean addObjectToTweetsColl(DBObject obj) {
        try {
            tweetsColl.insert(obj);
        } catch (MongoException e) { //if there is a duplicate
            return false;
        }
        return true;
    }

    /**
     * Adds object to users collection.
     *
     * @param obj
     * @return true if succeed, false elsewhere
     */
    public boolean addObjectToUsersColl(DBObject obj) {
        try {
            usersColl.insert(obj);
        } catch (MongoException e) { //if there is a duplicate
            return false;
        }
        return true;
    }

    /**
     * Adds object to followed users collection.
     *
     * @param obj
     * @return true if succeed, false elsewhere
     */
    public boolean addObjectToFollowedUsers(DBObject obj) {
        try {
            followedUsersColl.insert(obj);
        } catch (MongoException e) { //if there is a duplicate
            return false;
        }
        return true;
    }

    /**
     * Adds object to followed users activity collection.
     *
     * @param obj
     * @return true if succeed, false elsewhere
     */
    public boolean addObjectToFollowedUsersActivity(DBObject obj) {
        try {
            followedActivityColl.insert(obj);
        } catch (MongoException e) { //if there is a duplicate
            return false;
        }
        return true;
    }

    /**
     * Finds a user in our collection, specified by his ID.
     *
     * @param id
     * @return true if found, false if not
     */
    public boolean findFollowedUser(String id) {
        BasicDBObject query = new BasicDBObject(); //make a query to count tweets of user
        query.put("id_str", id);
        return followedUsersColl.count(query) != 0;
    }

    
    public String findScreenName(String id){
        BasicDBObject query = new BasicDBObject(); //make a query to find screen name of user
        query.put("id_str", id);
        return usersColl.findOne(query).get("screen_name").toString();
    }
    
    /**
     * Updates the finish time of the observing activity of a user. It sets it
     * as the time for next checking, since we are going to follow for sure his
     * activity until then, and then we check again if we want to continue the
     * observing or not.
     *
     * @param id
     * @param updated
     */
    public void updateFinishTime(String id, DBObject updated) {
        BasicDBObject query = new BasicDBObject(); //make a query to find the specific user
        query.put("id_str", id);
        this.followedUsersColl.update(query, updated);
    }

    public void appendFinishTime(String id, DBObject newEntry) {
        BasicDBObject query = new BasicDBObject(); //make a query to find the specific user
        query.put("id_str", id);
        this.followedUsersColl.update(query, newEntry);
    }

    /**
     * Identify the list of suspicious users looking at several attributes while
     * making queries at our mongo database.
     *
     * @return the list of current suspicious users
     */
    public List<String> findSuspiciousUsers() {
        List<String> suspicious = new ArrayList<>();

        DBCursor cursor = usersColl.find(); //get a cursor that will run throughout the collection of users.

        while (cursor.hasNext()) { //for every user

            boolean guilty;
            DBObject user = cursor.next(); //store user object

            BasicDBObject tweetsQuery = new BasicDBObject(); //make a query to count tweets of user
            tweetsQuery.put("user_id", user.get("id_str"));

            BasicDBObject retweetsQuery = new BasicDBObject(); //make a query to count retweets of user
            retweetsQuery.put("originalUserID", user.get("id_str"));

            // na vgalw to active
            // kai na kanw queries mono gia tis teleutaies 10 wres
            if (tweetsColl.count(tweetsQuery) > 25 && retweetsColl.count(retweetsQuery) > 1000) { //if high number of tweets & retweets then set as guilty
                guilty = true;

                int followers = (int) user.get("followers_count");
                int friends = (int) user.get("friends_count");
                boolean verified = (boolean) user.get("verified");

                if (friends == 0) //to avoid division with zero
                {
                    friends = 1;
                }

//                System.out.println("FIND-SUSPICIOUS| "+user.get("id_str").toString() + " is " + isActive(user.get("id_str").toString()));
                if (verified || (followers > 200000 && followers / friends > 1000)) //if celebrity or verified or inactive --> not guilty || || !isActive(user.get("id_str").toString())
                {
                    guilty = false;
                }

                if (guilty) { //if not celebrity or verified or inactive, add to suspicious
                    //System.out.println("ADDING to suspicious list in RAM | " +user.get("id_str") + " active user has: "+followers+ " followers, " +friends+" followees, verified: " +verified + " and active: " + isActive(user.get("id_str").toString()));
                    String id = user.get("id_str").toString();
                    suspicious.add(id);
                }
            }
        }
        return suspicious;
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
     * Checks if a user is active or not.
     *
     * @param userID
     * @return true if active, false if inactive
     */
    private boolean isActive(String userID) {

        BasicDBObject query = new BasicDBObject();
        query.put("originalUserID", userID);

        DBCursor cursor = retweetsColl.find(query);
        while (cursor.hasNext()) { //for every retweet occured for user
            DBObject retweetOccured = cursor.next(); //store retweet object details

            Date lastRTed = (Date) retweetOccured.get("created_at");
            Date now = new Date();

//            System.out.println("TEST DATE | last RTed: " + retweetOccured.get("created_at") + " now: " + now);
//            System.out.println("hours inactive: "+ getDateDiff(lastRTed,now,TimeUnit.MINUTES) );
            if (getDateDiff(lastRTed, now, TimeUnit.MINUTES) < MINUTES_OF_INACTIVITY_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    public Date getTimeUserAppeared(Long id) throws ParseException {
        BasicDBObject query = new BasicDBObject();
        query.put("id_str", id.toString());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                                            Locale.ENGLISH);
        
        String date;
        try{
            //if it is normal field (not list-array)
            date = this.followedUsersColl.findOne(query).get("starting_time").toString();
        } catch (java.lang.NullPointerException ex){
            try{
                //if it is a list of dates
                DBCursor c = this.followedUsersColl.find(query);
                BasicDBList times = (BasicDBList) c.next().get("following_periods"); 
                BasicDBObject first = (BasicDBObject) times.get(0);
                date = first.get("starting_time").toString();
            } catch (java.util.NoSuchElementException e){
                return null;
            }
        } 
        
        Date d = dateFormat.parse(date);
        return d;
    }

    public Date getTimeUserDisappeared(Long id) throws ParseException {
        BasicDBObject query = new BasicDBObject();
        query.put("id_str", id.toString());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                                            Locale.ENGLISH);
        String date;
        try{
            //if it is normal field (not list-array)
            date = this.followedUsersColl.findOne(query).get("finish_time").toString();
        } catch (java.lang.NullPointerException ex){
            try{
                //if it is a list of dates
                DBCursor c = this.followedUsersColl.find(query);
                BasicDBList times = (BasicDBList) c.next().get("following_periods"); 
                BasicDBObject last = (BasicDBObject) times.get(times.size()-1);
                date = last.get("finish_time").toString();
            } catch (java.util.NoSuchElementException e){
                return null;
            }
        }
        
        Date d = dateFormat.parse(date);
        return d;
    }

//    public List<String> deleteInactiveUsers(List<String> suspicious, int time){
//        ArrayList<String> toDelete = new ArrayList<>();
//        boolean flag = false;
//        
//        for(String userID : suspicious){  
//            BasicDBObject query = new BasicDBObject(); //make a query to find retweets received by user
//            query.put("originalUserID", userID);
//            
//            DBCursor retweetCursor = this.retweetsColl.find(query); //get a cursor that will run throughout the collection of retweets and return the ones belong to user.
//            while (retweetCursor.hasNext()) {
//                DBObject retweetOccured = retweetCursor.next();
//                retweetOccured.get("createdAt");
//                //check if this date is too old
//                //if it is not --> flag = true and break -> o tupos einai active
//                
//            }
//            if(!flag){
//                toDelete.add(userID);
//            }
//        }
//        
//        return toDelete;
//    }
    
    /**
     * Counts the number of unique users in our database.
     * 
     * @return the number of users
     */
    private int checkUniqueUsersNumber(){
        HashSet<Long> ids = new HashSet<>();
        int count =0;
        DBCursor c = usersColl.find(); //get all users in our database
        while (c.hasNext()) {
            DBObject u = c.next(); 
            long id = Long.parseLong(u.get("id_str").toString());
            if (ids.contains(id))
                count++;
            else
                ids.add(id);
        }
        
        System.out.println("counter: " + count);
        System.out.println("size of hash: " + ids.size());
        return ids.size();
    }
    
    public void suspiciousTweetActivity() throws FileNotFoundException, UnsupportedEncodingException, MongoException, UnknownHostException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Documents\\NetBeansProjects\\Test\\badUsersLowEntropyIDS.txt"));
        String line = reader.readLine();
        
        while (line !=null) { //for every user
            //System.out.println("==========" + line + "===========");
            long id = Long.parseLong(line);
            
            PrintWriter writer = new PrintWriter(id + ".txt", "UTF-8"); //create a text file for him
            //System.out.println("starting tweets query");
            BasicDBObject tweetsQuery = new BasicDBObject(); //make a query to get original tweets of user
            tweetsQuery.put("user_id", line);

            DBCursor tweets = tweetsColl.find(tweetsQuery);
            int k = 1;
            //System.out.println("found relevant tweets");
            writer.println("======================================= ORIGINAL TWEETS BY USER ======================================");
            while (tweets.hasNext()) { //for every tweet
                DBObject tweet = tweets.next(); //store user object

                writer.println(k + ". " + tweet.get("id_str") + ": " + tweet.get("text"));
                k++;
            }

            //System.out.println("starting retweets query");
            BasicDBObject retweetsQuery = new BasicDBObject(); //make a query to count retweets of user
            retweetsQuery.put("retweetedUserID", line);

            DBCursor retweets = retweetsColl.find(retweetsQuery);
            //System.out.println("found relevant retweets");
            if (retweets.count() > 0) {
                k = 1;
                writer.println();
                writer.println();
                writer.println("====================================== USER RETWEETED THESE TWEETS =====================================");
                while (retweets.hasNext()) { //for every tweet
                    DBObject retweet = retweets.next(); //store user object

                    BasicDBObject quer = new BasicDBObject(); //make a query to count retweets of user
                    quer.put("id_str", retweet.get("originalTweetID"));
                    DBObject tw = tweetsColl.findOne(quer);
                    writer.println(k + ". " + retweet.get("originalTweetID") + " >> " + retweet.get("originalUserID") + ": " + tw.get("text"));
                    k++;
                }
            }

            writer.println("========================================================================================================");
            writer.close();
            line = reader.readLine();
        }
        reader.close();
    }
    
    // Getters
    public DBCollection getUsersColl() {
        return usersColl;
    }

    public DBCollection getTweetsColl() {
        return tweetsColl;
    }

    public DBCollection getRetweetsColl() {
        return retweetsColl;
    }

    public DBCollection getFollowedUsersColl() {
        return followedUsersColl;
    }

    public DBCollection getFollowedActivityColl() {
        return followedActivityColl;
    }

    public static int getMINUTES_OF_INACTIVITY_THRESHOLD() {
        return MINUTES_OF_INACTIVITY_THRESHOLD;
    }

}
