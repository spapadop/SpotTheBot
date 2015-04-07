package spotthebot;

import java.util.Date;
import java.util.HashMap;

/**
 * Represents a Twitter User with the main data that we desire.
 * It stores userID, the number of retweets he received, the date when he 
 * was lastly retweeted and the number of retweets he got per tweet he did.
 * 
 * @author Sokratis Papadopoulos
 */
public class TwitterUser {

    private Long id; //stores the userID
    private int retweetsReceived; //number of times user's tweets were RTed
    private Date lastRTed; //date of the last retweet received occured
    private HashMap<Long, Integer> timesRetweeted; //number of times each of his tweet is retweeted [TweetID - NoRTed]
    
    /**
     * Construct a TwitterUser object.
     * Set the retweets counter to 1.
     */
    public TwitterUser() {
        this.id = null;
        this.retweetsReceived = 1;
        this.lastRTed = null;
        this.timesRetweeted = new HashMap<>();
    }

    /**
     * Construct a TwitterUser object.
     * Set received retweets counter to 1, last retweeted to current date 
     * and insert the retweet to the appropriate hashmap.
     * 
     * @param userID
     * @param tweetID
     * @param now 
     */
    public TwitterUser(Long userID, Long tweetID, Date now) {
        this.id = userID;
        this.retweetsReceived = 1;
        this.lastRTed = now;
        this.timesRetweeted = new HashMap<>();
        this.timesRetweeted.put(tweetID, 1);
    }
    
    /**
     * Updates data for current User.
     * It assigns a new (current) date for the last retweet that the user got 
     * and it increases the total number of retweets received.
     * 
     * @param tweetID
     * @param at 
     */
    public void update(Long tweetID, Date at) {

        this.setLastRTed(at); //update last retweet occured
        this.increaseRetweetsReceived(); //incease total retweets received

        //update the HashMap of tweets with number of their retweets.
        if (this.timesRetweeted.get(tweetID) == null) {
            this.timesRetweeted.put(tweetID, 1);
        } else {
            int value = this.timesRetweeted.get(tweetID) + 1;
            this.timesRetweeted.put(tweetID, value);
        }
    }

    public Long getUserID() {
        return id;
    }

    public void setUserID(Long userID) {
        this.id = userID;
    }

    public Date getLastRTed() {
        return this.lastRTed;
    }

    public void setLastRTed(Date now) {
        this.lastRTed = now;
    }

    public int getRetweetsReceived() {
        return this.retweetsReceived;
    }
    
    public void increaseRetweetsReceived() {
        this.retweetsReceived++;
    }

    public HashMap<Long, Integer> getTimesRetweeted() {
        return this.timesRetweeted;
    }

}
