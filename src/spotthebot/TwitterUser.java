package spotthebot;

//import java.text.DateFormat;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import twitter4j.User;

/**
 *
 * @author Sokratis
 */
public class TwitterUser {

    private Long id; //stores the userID
    private int retweetsReceived; //number of times user's tweets were RTed
    private Date lastRTed; //date of the last retweet received occured
    private HashMap<Long, Integer> timesRetweeted; //number of times each of his tweet is retweeted

//    private int tweets; // number of user's tweets 
//    private int retweets; // number of user's retweets    
//    private String accountAge; // how old the account is
//    private int followers; // number of user's followers
//    private int followees; // number of user's followees
//    private double ratio; // ratio of followers/followees
//    private boolean isVerified; // is the account verified or not
    public TwitterUser() {
        this.id = null;
        this.retweetsReceived = 1;
        this.lastRTed = null;
        this.timesRetweeted = new HashMap<>();
    }

    public TwitterUser(Long userID, Date now) {
        this.id = userID;
        this.retweetsReceived = 1;
        this.lastRTed = now;
        this.timesRetweeted = new HashMap<>();
    }

    public TwitterUser(Long userID, Long tweetID, Date now) {
        this.id = userID;
        this.retweetsReceived = 1;
        this.lastRTed = now;
        this.timesRetweeted = new HashMap<>();
        this.timesRetweeted.put(tweetID, 1);
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

    public HashMap<Long, Integer> getTimesRetweeted() {
        return this.timesRetweeted;
    }

    public void increaseRetweetsReceived() {
        this.retweetsReceived++;
    }

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

//
//    /**
//     * 
//     * @param createdAge 
//     */
//    public void setAccountAge(String createdAge) {
//        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//        Date date =new Date();
//        setAccountAge(createdAge,dateFormat.format(date).toString());
//    }
//    /**
//     * 
//     * @param createdAge
//     * @param currentDate 
//     */
//    public void setAccountAge(String createdAge,String currentDate){
//       SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//       
//       Date d1;
//       Date d2;
//        try {
//            d1 = format.parse(createdAge);
//            d2 = format.parse(currentDate);
//            
//            long diff = d2.getTime() - d1.getTime();
//            
//            long diffSeconds = diff / 1000 % 60;
//            long diffMinutes = diff / (60 * 1000) % 60;
//            long diffHours = diff / (60 * 60 * 1000) % 24;
//            long diffDays = diff / (24 * 60 * 60 * 1000);
//            
//            StringBuilder sb = new StringBuilder();
//            sb.append(diffDays).append("days, ").append(diffHours).append("hours, ").append(diffMinutes).append("minutes, ").append(diffSeconds).append("seconds.");
//            
//            this.accountAge = sb.toString();
//        } catch (ParseException ex) {
//            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
//        }
//   }
//    
//    public int getFollowers() {
//        return followers;
//    }
//
//    public void setFollowers(int followers) {
//        this.followers = followers;
//    }
//
//    public int getFollowees() {
//        return followees;
//    }
//
//    public void setFollowees(int followees) {
//        this.followees = followees;
//    }
//    public double getRatio() {
//        ratio = (double) followers/followees; //just in case it weren't calculated yet
//        return ratio;
//    }
//
//    public String getAccountAge() {
//        return accountAge;
//    }    
//    
//    public void increaseRetweets(){
//        retweets++;
//    }
//    
//    public void increaseTweets(){
//        tweets++;
//    }
}
