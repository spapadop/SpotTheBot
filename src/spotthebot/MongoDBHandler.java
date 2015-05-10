package spotthebot;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Handles everything regarding Mongo databases used for the application.
 * It creates 2 databases, one for the random tweets and one for the activity
 * of the suspicious users as we define them. It handles all additions/searches
 * and basically any interaction with DBs.
 * 
 * @author Sokratis Papadopoulos
 */
class MongoDBHandler{
	
    private MongoClient m;
    
    private DB randomDB; //random collection from API
    private DBCollection usersColl; //all user{} info of json
    private DBCollection tweetsColl; //all info of tweets except user{}, where we just store the user_id
    private DBCollection retweetsColl; //IDs of two users involved in retweet, createdAt of retweet & tweetID of original tweet
    
    private DB followedDB; //focused observing on suspicious users
    private DBCollection followedUsersColl; //storing all users details + starting and finish time of observing
    private DBCollection followedActivityColl; //storing all the activity of followed users
        // private DBCollection tweetsByUsersColl, followedUsersColl, tweetsRetweetedByUsersColl, repliesToUsersTweetsColl, retweetsOfUsersTweetsColl, repliesByUsersColl;

    private static final int MINUTES_OF_INACTIVITY_THRESHOLD = 45; 

    /**
     * Constructor that creates the appropriate DB environment.
     * It also orders the creation of indexes in our collections.
     * 
     * @throws UnknownHostException
     * @throws MongoException 
     */
    public MongoDBHandler() throws UnknownHostException, MongoException {
        try {
            m = new MongoClient("localhost", 27017);
            
            randomDB = m.getDB("random");
            usersColl = randomDB.getCollection("users");
            tweetsColl = randomDB.getCollection("tweets");
            retweetsColl = randomDB.getCollection("retweets"); 
            
            followedDB = m.getDB("followed");
            followedUsersColl = followedDB.getCollection("users");
            followedActivityColl = followedDB.getCollection("activity");
            
            createIndexes(); //creates the (unique) indexes in all our collections
                        
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Add (unique) indexes to collections.
     * 
     */
    public void createIndexes(){
        
        //---- For random crawling
        usersColl.ensureIndex(new BasicDBObject("id_str", 1), new BasicDBObject("unique", true));

        tweetsColl.ensureIndex(new BasicDBObject("id_str", 1), new BasicDBObject("unique", true));
        tweetsColl.ensureIndex(new BasicDBObject("user_id", 1));
        tweetsColl.ensureIndex(new BasicDBObject("created_at", 1));

        retweetsColl.ensureIndex(new BasicDBObject("originalTweetID", 1));
        retweetsColl.ensureIndex(new BasicDBObject("originalUserID", 1));
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
        try{
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
        try{
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
        try{
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
    public boolean addObjectToFollowedUsers(DBObject obj){
        try{
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
    public boolean addObjectToFollowedUsersActivity(DBObject obj){
        try{
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
    public boolean findFollowedUser(String id){
        BasicDBObject query = new BasicDBObject(); //make a query to count tweets of user
        query.put("id_str", id);
        return followedUsersColl.count(query) != 0;
    }
    
    /**
     * Updates the finish time of the observing activity of a user.
     * It sets it as the time for next checking, since we are going to 
     * follow for sure his activity until then, and then we check again if we
     * want to continue the observing or not.
     * 
     * @param id
     * @param updated 
     */
    public void updateFinishTime(String id, DBObject updated){
        BasicDBObject query = new BasicDBObject(); //make a query to find the specific user
        query.put("id_str", id);
        this.followedUsersColl.update(query,updated);
    }
    
    /**
     * Identify the list of suspicious users looking at several attributes
     * while making queries at our mongo database.
     * 
     * @return the list of current suspicious users
     */
    public List<String> findSuspiciousUsers(){
        List<String> suspicious = new ArrayList<>();    
        
        DBCursor cursor = usersColl.find(); //get a cursor that will run throughout the collection of users.
        
        while (cursor.hasNext()) { //for every user
            
            boolean guilty = false;
            DBObject user = cursor.next(); //store user object
            
            BasicDBObject tweetsQuery = new BasicDBObject(); //make a query to count tweets of user
            tweetsQuery.put("user_id", user.get("id_str"));
            
            BasicDBObject retweetsQuery = new BasicDBObject(); //make a query to count retweets of user
            retweetsQuery.put("originalUserID", user.get("id_str"));
            
            if(tweetsColl.count(tweetsQuery) > 25 && retweetsColl.count(retweetsQuery)> 50){ //if high number of tweets & retweets then set as guilty
                guilty = true;
                
                int followers = (int) user.get("followers_count");
                int friends = (int) user.get("friends_count");
                boolean verified = (boolean) user.get("verified");
                
                if (friends == 0) //to avoid division with zero
                    friends = 1;

//                System.out.println("FIND-SUSPICIOUS| "+user.get("id_str").toString() + " is " + isActive(user.get("id_str").toString()));
                if ( verified || (followers > 200000 && followers/friends > 100) || !isActive(user.get("id_str").toString())  ) //if celebrity or verified or inactive --> not guilty
                    guilty = false; 

                if(guilty){ //if not celebrity or verified or inactive, add to suspicious
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
     * @param userID
     * @return true if active, false if inactive
     */
    private boolean isActive(String userID){
        
        BasicDBObject query = new BasicDBObject(); 
        query.put("originalUserID", userID);
           
        DBCursor cursor = retweetsColl.find(query); 
        while (cursor.hasNext()) { //for every retweet occured for user
            DBObject retweetOccured = cursor.next(); //store retweet object details
            
            Date lastRTed = (Date) retweetOccured.get("created_at");
            Date now = new Date();
            
//            System.out.println("TEST DATE | last RTed: " + retweetOccured.get("created_at") + " now: " + now);
//            System.out.println("hours inactive: "+ getDateDiff(lastRTed,now,TimeUnit.MINUTES) );
            if(getDateDiff(lastRTed, now, TimeUnit.MINUTES) < MINUTES_OF_INACTIVITY_THRESHOLD)
                return true;
        }
        return false;
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
    
    
    // Getters
    
    public DBCollection getUsersColl(){
        return usersColl;
    }
    
    public DBCollection getTweetsColl(){
        return tweetsColl;
    }
    
    public DBCollection getRetweetsColl(){
        return retweetsColl;
    }
	
}
