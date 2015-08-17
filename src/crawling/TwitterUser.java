package crawling;

import com.mongodb.DBObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.User;

/**
 * Represents a Twitter User with the main data that we desire.
 * It stores userID, the number of retweets he received, the date when he 
 * was lastly retweeted and the number of retweets he got per tweet he did.
 * 
 * @author Sokratis Papadopoulos
 */
public class TwitterUser {

    private Long id; //stores the userID
    private long tweets; // number of tweets the user did
    private long retweets; //number of retweets the user did
    private long retweetsReceived; //number of times user's tweets were RTed === MAYBE WE WANT ALL RETWEETS AND NOT 1 FOR EACH
    private double retweetsTweetsRatio; //ratio of retweets done / tweets done
    private double tweetsRetweeted; //number of user's tweets retweeted
    private double avgTweetsPerHour; //average number of tweets done by user per hour
    private double avgRetweetsPerHour; // average number of retweets done by user per hour
    private double avgRtReceivedPerHour; // average number of retweets received by user per hour
    private HashMap<Long, Date> tweetsDate; //collection of tweets the user did, together with the date.
    private HashMap<Long, Date> retweetsDate; //collection of retweets the user did, together with the date.
    private HashMap<Long, Date> retweetsReceivedDate; //collection of retweets the user received, together with the date.
    private long timeFollowed; // total time we followed the user (hours)
    
    private static final int PACKAGE = 1; //package of characteristics checked
   
    /**
     * Construct a TwitterUser object.
     */
    public TwitterUser() {
        this.id = null;
    }

    /**
     * Construct a TwitterUser object for a given userID.
     * 
     * @param userId
     */
    public TwitterUser(Long userId) {
        this.id = userId;
        this.tweets=0;
        this.retweets=0;
        this.retweetsReceived=0;
        this.retweetsTweetsRatio=0;
        this.tweetsRetweeted=0;
        this.avgTweetsPerHour=0;
        this.avgRetweetsPerHour=0;
        this.avgRtReceivedPerHour=0;
        this.tweetsDate = new HashMap<>();
        this.retweetsDate = new HashMap<>();
        this.retweetsReceivedDate = new HashMap<>();
        this.timeFollowed=0;
    }
         
    public void addTweet(Long tweetId, Date at){
        tweetsDate.put(tweetId, at);
    }
    
    public void addRetweet(Long retweetId, Date at){
        retweetsDate.put(retweetId, at);
    }
    
    public void addRetweetReceived(Long rtReceivedId, Date at){
        retweetsReceivedDate.put(rtReceivedId, at);
    }
    
    /**
     * Calculate all remaining values based on current input, since its final.
     */
    public void finished(Date arrived, Date left){
        if(tweets !=0){
            this.retweetsTweetsRatio = (double) retweets/tweets;
        } else{
            this.retweetsTweetsRatio = 0;
        }
        
//        System.out.println("Arrived: " + arrived);
//        System.out.println("Left: " + left);
        timeFollowed = getDateDiff(arrived, left, TimeUnit.HOURS);
        if(timeFollowed!=0){
            this.avgTweetsPerHour = (double) tweets/timeFollowed;
            this.avgRetweetsPerHour = (double) retweets/timeFollowed;
            this.avgRtReceivedPerHour = (double) retweetsReceived/timeFollowed;
        }
        
        
//        for (Long tweetId : this.tweetsDate.keySet()) {
//            if (retweetsReceivedDate.containsKey(tweetId))
//                this.tweetsRetweeted ++;
//        }
        
        if(!retweetsReceivedDate.isEmpty()){
            tweetsRetweeted = tweets/retweetsReceivedDate.size();
        }
    }
    
    
    // =========================================================================
    
    
    /**
     * 
     * @param createdAge
     * @return 
     */
    public Long setAccountAge(String createdAge) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);
            Date accountCreationTime = dateFormat.parse(createdAge);
            Date currentTime = new Date();
            dateFormat.format(currentTime);
            
            return getDateDiff(accountCreationTime, currentTime, TimeUnit.DAYS);

        } catch (ParseException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
        
    /**
     * Calculates the friends/followers ratio
     * @return the ratio
     */
    public double friendsFollowersRatio(int friends, int followers){
                         
        return (double) friends/followers;
    }
    
    /**
     * TODO: work on this function. Checking some variables regarding tweets
     * @param tweets
     * @return 
     */
    public boolean checkTweetsDetails(ArrayList<DBObject> tweets) {
        
        if (!tweets.isEmpty()) {
            int[] mentions = new int[tweets.size()];
            int[] hashtags = new int[tweets.size()];
            int[] urls = new int[tweets.size()];
            int replies=0, i=0;
            
            //TODO: WORK ON THESE!!!
//            double urlRatio;
//            HashSet<String> uniqueDomains;
//            int numberOfUniqueDomains; //facebook.com, twitter.com, youtube.com....
//            HashSet<String> uniqueURLs;
//            double domainRatio;
            
            HashMap<String, Integer> tweetsPerSource = new HashMap<>();
            
            for(DBObject tweet: tweets){
                try {
                    JSONObject jobj = new JSONObject(tweet.toString());
                    
                    //======= GETTING REPLY_TO_STATUS (true/false)=======
                    if (tweet.get("in_reply_to_status_id_str") != null) {
                        replies++;
                    }
                    //======= GETTING SOURCE =======
                    String source = tweet.get("source").toString();
                    org.jsoup.nodes.Document doc = Jsoup.parse(source);
                    Element link = doc.select("a").first();
                    String sourceName = link.text();            

                    if (tweetsPerSource.containsKey(sourceName)) {
                        tweetsPerSource.put(sourceName, tweetsPerSource.get(sourceName) + 1);
                    } else {
                        tweetsPerSource.put(sourceName, 1);
                    }
                    
                    JSONObject entities = jobj.getJSONObject("entities"); //getting inside "entities" in json
                    
                    //======= GETTING MENTIONS =======
                    JSONArray mentionArray = entities.getJSONArray("user_mentions");
                    mentions[i] = mentionArray.length();

                    //======= GETTING HASHTAGS =======
                    JSONArray hashtagArray = entities.getJSONArray("hashtags");
                    hashtags[i] = hashtagArray.length();

                    //======= GETTING URLs =======
                    JSONArray urlArray = entities.getJSONArray("urls");
                    urls[i] = urlArray.length();

                    //======= GETTING EXPANDED URLs =======
                    if (urls[i] != 0) { //if URLs exist, it takes the expanded version of them
                        
                        String[] expanded_url = new String[urlArray.length()];
                        for (int j = 0; j < urlArray.length(); j++) {
                            expanded_url[j] = urlArray.getJSONObject(j).getString("expanded_url");
                        }
                    }
                    
                    
                    //do something with text
                    //tweet.addAndProcessTweet(jobj.getString("text"));
                    
                    
                    
                    i++;
                } catch (JSONException ex) {
                    Logger.getLogger(TwitterUser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
            double avHashtags = calcAverage(hashtags);
            double avURLs = calcAverage(urls);
            double avMentions = calcAverage(mentions);
            String source = calculateMostFrequentSource(tweetsPerSource);
            
            System.out.println("1. mentions: " + avMentions);
            System.out.println("2. hashtags: " + avHashtags);
            System.out.println("3. urls: " + avURLs);
            System.out.println("4. replies: " + replies);
            System.out.println("5. freq source: " + source);
//            System.out.println("6. favourites: " + favourites);
//            System.out.println("7. description: " + hasDescription);
//            System.out.println("8. geoEnabled: " + geoEnabled);
//            System.out.println("9. timeZone: " + timeZone);
//            System.out.println("10. profileOptimized: " + defaultProfile);
//            System.out.println("11. defaultAvatar: " + defaultAvatar);
//            System.out.println("12. isTranslator: " + isTranslator);
//            System.out.println("13. lists: " + lists);
//            System.out.println("14. location: " + location);
//            System.out.println("15. isProtected: " + isProtected);
            
            
            return ( (avHashtags >= 4) && (avURLs >= 1) && (avMentions >= 0.2) );
            
        } else {
            return false;
        }
    }
    
    /**
     * calculates the most frequent source for the user after examining all the
     * sources that he used.
     * @param tweetsPerSource
     */
    public String calculateMostFrequentSource(HashMap<String,Integer> tweetsPerSource) {
        Integer maxFrequency = -1;
        String mostFrequentSource = null;
        Iterator it = tweetsPerSource.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry user = (Map.Entry) it.next();

            if (maxFrequency < (Integer) user.getValue()) {
                maxFrequency = (Integer) user.getValue();
                mostFrequentSource = (String) user.getKey();
            }
        }
        return mostFrequentSource;
    }

    
    /**
     * 
     * @param array
     * @return 
     */
    private double calcAverage(int[] array){
        
        double sum=0;
        for (int j = 0; j < array.length; j++) {
            sum+= array[j];
        }
        return sum/array.length;
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
     * Prints all available information for a user 
     * as taken from his record in mongoDB.
     * 
     * @param user 
     * @return true of false, depending on if he satisfies the given requirements. 
     */
    public boolean checkUserDetails(DBObject user){
        boolean verified  = objectBool(user.get("verified"));
        int friends = Integer.parseInt(user.get("friends_count").toString());
        int followers = Integer.parseInt(user.get("followers_count").toString());
        double ratio = friendsFollowersRatio(friends, followers);
        Long accountAge = setAccountAge(user.get("created_at").toString());
        int favourites = Integer.parseInt(user.get("favourites_count").toString());
//        boolean hasDescription = objectBool(user.get("description").toString());
//        boolean geoEnabled = objectBool(user.get("geo_enabled").toString());
//        boolean timeZone = objectBool(user.get("time_zone").toString());
        boolean defaultProfile = objectBool(user.get("default_profile").toString());
        boolean defaultAvatar = objectBool(user.get("default_profile_image").toString());
        boolean isTranslator = objectBool(user.get("is_translator").toString());
        int lists = Integer.parseInt(user.get("listed_count").toString());
        boolean location = objectBool(user.get("is_translator").toString());
        boolean isProtected = objectBool(user.get("protected").toString());
        
        System.out.println("1. verified: " + verified);
        System.out.println("2. friends: " + friends);
        System.out.println("3. followers: " + followers);
        System.out.println("4. ratio: " + ratio);
        System.out.println("5. accountAge: " + accountAge);
        System.out.println("6. favourites: " + favourites);
//        System.out.println("7. description: " + hasDescription);
//        System.out.println("8. geoEnabled: " + geoEnabled);
//        System.out.println("9. timeZone: " + timeZone);
        System.out.println("10. profileOptimized: " + defaultProfile);
        System.out.println("11. defaultAvatar: " + defaultAvatar);
        System.out.println("12. isTranslator: " + isTranslator);
        System.out.println("13. lists: " + lists);
        System.out.println("14. location: " + location);
        System.out.println("15. isProtected: " + isProtected);
        
        //********************************
        //TODO: "screen_name": @sokpapadop
        //********************************
         
        switch(PACKAGE){ //checking in which variables package test we are.
            case 1: 
                return true;    //((!verified) && (ratio > 1.4) && (accountAge < 1000) && (favourites <10));
            case 2:
                return false;
            case 3:
                return false;
            case 4: 
                return false;
            default:
                return false;
        }
    }
    
    /**
     * checks if a an object is true or not.
     * 
     * @param value
     * @return true if verified, false if not.
     */
    public boolean objectBool(Object value){

        if(value.toString().equals("false")){
            return false;
        }
        return true;
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTweets() {
        return tweets;
    }

    public void setTweets(long tweets) {
        this.tweets = tweets;
    }

    public long getRetweets() {
        return retweets;
    }

    public void setRetweets(long retweets) {
        this.retweets = retweets;
    }

    public long getRetweetsReceived() {
        return retweetsReceived;
    }

    public void setRetweetsReceived(long retweetsReceived) {
        this.retweetsReceived = retweetsReceived;
    }

    public double getRetweetsTweetsRatio() {
        return retweetsTweetsRatio;
    }

    public void setRetweetsTweetsRatio(double retweetsTweetsRatio) {
        this.retweetsTweetsRatio = retweetsTweetsRatio;
    }

    public double getAvgTweetsPerHour() {
        return avgTweetsPerHour;
    }

    public void setAvgTweetsPerHour(double avgTweetsPerHour) {
        this.avgTweetsPerHour = avgTweetsPerHour;
    }

    public double getAvgRetweetsPerHour() {
        return avgRetweetsPerHour;
    }

    public void setAvgRetweetsPerHour(double avgRetweetsPerHour) {
        this.avgRetweetsPerHour = avgRetweetsPerHour;
    }

    public double getAvgRtReceivedPerHour() {
        return avgRtReceivedPerHour;
    }

    public void setAvgRtReceivedPerHour(double avgRtReceivedPerHour) {
        this.avgRtReceivedPerHour = avgRtReceivedPerHour;
    }

    public HashMap<Long, Date> getTweetsDate() {
        return tweetsDate;
    }

    public void setTweetsDate(HashMap<Long, Date> tweetsDate) {
        this.tweetsDate = tweetsDate;
    }

    public HashMap<Long, Date> getRetweetsDate() {
        return retweetsDate;
    }

    public void setRetweetsDate(HashMap<Long, Date> retweetsDate) {
        this.retweetsDate = retweetsDate;
    }

    public HashMap<Long, Date> getRetweetsReceivedDate() {
        return retweetsReceivedDate;
    }

    public void setRetweetsReceivedDate(HashMap<Long, Date> retweetsReceivedDate) {
        this.retweetsReceivedDate = retweetsReceivedDate;
    }

    public long getTimeFollowed() {
        return timeFollowed;
    }

    public void setTimeFollowed(long timeFollowed) {
        this.timeFollowed = timeFollowed;
    }

    public double getTweetsRetweeted() {
        return tweetsRetweeted;
    }

    public void setTweetsRetweeted(double tweetsRetweeted) {
        this.tweetsRetweeted = tweetsRetweeted;
    }
    
}
