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
import java.util.List;

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
     * @return 1 if found, 0 if not
     */
    public long findFollowedUser(String id){
        BasicDBObject query = new BasicDBObject(); //make a query to count tweets of user
        query.put("id_str", id);
        return this.followedUsersColl.count(query);
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
            
            boolean guilty = true;
            DBObject user = cursor.next(); //store user object
            
            BasicDBObject tweetsQuery = new BasicDBObject(); //make a query to count tweets of user
            tweetsQuery.put("user_id", user.get("id_str"));
            
            BasicDBObject retweetsQuery = new BasicDBObject(); //make a query to count retweets of user
            retweetsQuery.put("originalUserID", user.get("id_str"));
            
            //FIRST FILTER
            if(tweetsColl.count(tweetsQuery) > 5 && retweetsColl.count(retweetsQuery)> 5){ //if high number of tweets & retweets 
                
                int folers = (int) user.get("followers_count");
                int friends = (int) user.get("friends_count");
                boolean verified = (boolean) user.get("verified");
                if (friends == 0)
                    friends = 1;

                if ( (folers > 200000 && folers/friends > 100) || verified  ) //if celebrity or verified --> not guilty
                    guilty = false;                                         //note: maybe celebrities or verified are getting paid to tweet stuff...

//                if(tweetsColl.count(tweetsQuery) >10) 
//                    System.out.println("user: " + user.get("id_str") + 
//                                        " tweets: " + tweetsColl.count(tweetsQuery) + 
//                                        " retweets: " + retweetsColl.count(retweetsQuery) + 
//                                        " verified: " + user.get("verified"));

                if(guilty){ //if not celebrity, add to suspicious
                    System.out.println("ADDING to suspicious list | " + user.get("id_str")+ " user has: "+ folers+ " followers, " + friends +" followees and verified: " + verified);
                    String id = user.get("id_str").toString();
                    suspicious.add(id);
                }
            }
        }
        
        return suspicious;
    }
    
    public List<String> deleteInactiveUsers(List<String> suspicious, int time){
        ArrayList<String> toDelete = new ArrayList<>();
        boolean flag = false;
        
        for(String userID : suspicious){  
            BasicDBObject query = new BasicDBObject(); //make a query to find retweets received by user
            query.put("originalUserID", userID);
            
            DBCursor retweetCursor = this.retweetsColl.find(query); //get a cursor that will run throughout the collection of retweets and return the ones belong to user.
            while (retweetCursor.hasNext()) {
                DBObject retweetOccured = retweetCursor.next();
                retweetOccured.get("createdAt");
                //check if this date is too old
                //if it is not --> flag = true and break -> o tupos einai active
                
            }
            if(!flag){
                toDelete.add(userID);
            }
        }
        
        return toDelete;
    }
    
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
