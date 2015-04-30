package spotthebot;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.JSONException;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Runs through the list of tweets in our database and analyzes the data,
 * extracting the users that had more retweets.
 *
 * @author Sokratis Papadopoulos
 */
public class RetweetObserver {

    //sets that handles the users occured from API
    private HashSet<Long> uniqueUsers; //used to check if a user has already occured in the database
    private HashSet<Long> uniqueTweetIDs;
    private CopyOnWriteArrayList<TwitterUser> usersColl;
    private HashSet<Long> highlyRTed; //users that have highly retweeted tweets

    
    public RetweetObserver() throws JSONException {
        //initializing the lists
        usersColl = new CopyOnWriteArrayList<>();
        uniqueUsers = new HashSet<>();
        uniqueTweetIDs = new HashSet<>();
        highlyRTed = new HashSet<>();
        
        //calculateRTs();
    }

    public HashSet<Long> getUniqueUsers() {
        return uniqueUsers;
    }
    
    public HashSet<Long> getUniqueTweetIDs() {
        return uniqueTweetIDs;
    }
    
    public CopyOnWriteArrayList<TwitterUser> getUsersColl() {
        return usersColl;
    }

    public HashSet<Long> getHighlyRTed() {
        return highlyRTed;
    }
    
    /**
     * Checks if user exists or not and perform the proper actions.
     * 
     * @param checkingID
     * @param tweetID
     * @param at
     * @return true if user exists, false if doesnt exist.
     */
    public boolean checkIfUserExists(Long checkingID, Long tweetID, Date at){
                
        if(this.getUniqueUsers().contains(checkingID)){ //if user exists
            
            int pos = 0;
            for(TwitterUser user : this.getUsersColl()){
                if (Objects.equals(user.getUserID(), checkingID)){
                    break;
                }
                pos++;
            }
            
            if(pos != -1){
                this.getUsersColl().get(pos).update(tweetID, at);
                //System.out.println("Existing user of usersColl updated!" + " at pos: " + pos);
            } else {
                //System.out.println("User doesnt exist! --> problem at identifying position");
            }
            return true;
            
        } else { //if user doesnt exist
            this.getUniqueUsers().add(checkingID);
            this.getUsersColl().add(new TwitterUser(checkingID, tweetID, at));
            //System.out.println("New user added to usersColl");
            return false;
        }
    }
    
    /**
     * Checks if tweetID has already occurred in our database.
     * If so, it returns true, else it returns false, after entering the new
     * tweetID to the uniqueTweetIDs list.
     * 
     * @param checkingID
     * @return true if found, false if not found.
     */
    public boolean checkIfTweetIDExists(Long checkingID){
        if(this.getUniqueTweetIDs().contains(checkingID)){ //if tweet exists
            //System.out.println("tweet exists");
            return true;
        } else {
            //System.out.println("tweet doesnt exist");
            this.getUniqueTweetIDs().add(checkingID);
            return false;
        }
    }
    
    /**
     * Inserts the upcoming information into the current databases.
     * It receives a retweet and it updates the appropriate values.
     * 
     * @param checkingID
     * @param tweetID
     * @param at 
     */
    public void updateListOfUsers(Long checkingID, Long tweetID, Date at){
                
        int pos = 0;
        boolean flag = false;
        for(TwitterUser user : usersColl){
            if (Objects.equals(user.getUserID(), checkingID)){
                flag = true;
                break;
            }
            pos++;
        }

        if(flag){
            usersColl.get(pos).update(tweetID, at);
            //System.out.println("Existing user of usersColl updated!" + " at pos: " + pos);
        } else {
            usersColl.add(new TwitterUser(checkingID, tweetID, at));
            //System.out.println("User doesnt exist! --> problem at identifying position");
        }
    }
        
    
    
    /**
     * Prints all users with their info, that occurred in our database.
     * 
     */
    public void printAll() {
        
        for (TwitterUser user : usersColl) {
            
            if(user.getRetweetsReceived() > 5){
                
                System.out.println("UserID: " + user.getUserID());
                System.out.println("Retweets received: " + user.getRetweetsReceived());
                System.out.println("Last RTed: " + user.getLastRTed());
                
                Iterator it = user.getTimesRetweeted().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    System.out.println(pair.getKey() + " = " + pair.getValue());
                }
                
                //System.out.println("---------------------");
            }
        }
    }
}
