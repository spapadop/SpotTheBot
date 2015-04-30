package spotthebot;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import java.net.UnknownHostException;

class MongoDBHandler{
	
    private MongoClient m;
    private DB randomDB;
    private DBCollection usersColl; //all user{} info of json
    private DBCollection tweetsColl; //all info of tweets except user{}, where we just store the id_str
    private DBCollection retweetsColl; //id_str of two users, createdAt & tweetID of original  

    public MongoDBHandler() throws UnknownHostException, MongoException {
        try {
            m = new MongoClient("localhost", 27017);
            randomDB = m.getDB("random");
            usersColl = randomDB.getCollection("users");
            tweetsColl = randomDB.getCollection("tweets");
            retweetsColl = randomDB.getCollection("retweets");
            
            createIndexes(); //creates the (unique) indexes to our collections
                        
        } catch (UnknownHostException e) {
                e.printStackTrace();
        } catch (MongoException e) {
                e.printStackTrace();
        }
    }
    
    /**
     * Add (unique) indexes to collections
     */
    public void createIndexes(){
        usersColl.ensureIndex(new BasicDBObject("id_str", 1), new BasicDBObject("unique", true));

        tweetsColl.ensureIndex(new BasicDBObject("id_str", 1), new BasicDBObject("unique", true));
        tweetsColl.ensureIndex(new BasicDBObject("user_id", 1));
        tweetsColl.ensureIndex(new BasicDBObject("created_at", 1));

        retweetsColl.ensureIndex(new BasicDBObject("originalTweetID", 1), new BasicDBObject("unique", true));
        retweetsColl.ensureIndex(new BasicDBObject("originalUserID", 1));
        retweetsColl.ensureIndex(new BasicDBObject("created_at", 1));
    }
		
    public void addObjectToRetweetsColl(DBObject obj) {
        retweetsColl.insert(obj);
    }
    
    public void addObjectToTweetsColl(DBObject obj) {      
        tweetsColl.insert(obj);
    }
    
    public void addObjectToUsersColl(DBObject obj) {
        usersColl.insert(obj);
        //usersColl.insert(obj, WriteConcern.ACKNOWLEDGED)
    }
    
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
