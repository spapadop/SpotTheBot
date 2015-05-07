package spotthebot;

import com.mongodb.DBObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
    private int retweetsReceived; //number of times user's tweets were RTed
    private Date lastRTed; //date of the last retweet received occured
    private HashMap<Long, Integer> tweetsWithTimesRetweeted; //number of times each of his tweet is retweeted [TweetID - NoRTed]
    
    private static final int PACKAGE = 1; // time-repeat of checking task for updating list of potential spammers | 60.000 milliseconds = 1 minute
   
    /**
     * Construct a TwitterUser object.
     * Set the retweets counter to 1.
     */
    public TwitterUser() {
        this.id = null;
        this.retweetsReceived = 1;
        this.lastRTed = null;
        this.tweetsWithTimesRetweeted = new HashMap<>();
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
        this.tweetsWithTimesRetweeted = new HashMap<>();
        this.tweetsWithTimesRetweeted.put(tweetID, 1);
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
        if (this.tweetsWithTimesRetweeted.get(tweetID) == null) {
            this.tweetsWithTimesRetweeted.put(tweetID, 1);
        } else {
            int value = this.tweetsWithTimesRetweeted.get(tweetID) + 1;
            this.tweetsWithTimesRetweeted.put(tweetID, value);
        }
    }
    
    /**
     * 
     * @param user 
     * @return  
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
     * checks if a user is verified or not.
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
            double urlRatio;
            HashSet<String> uniqueDomains;
            int numberOfUniqueDomains; //facebook.com, twitter.com, youtube.com....
            HashSet<String> uniqueURLs;
            double domainRatio;
            
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
    
    
    
    
    
    
//    //domain stuff
//    public void calculateUniqueDomains(ArrayList<DBObject> tweets) throws URISyntaxException {
//        
//        
//        for (String url : uniqueURLs) {
//            uniqueDomains.add(getDomainName(url));
//        }
//        numberOfUniqueDomains = uniqueDomains.size();
//        if (noURL != 0) {
//            domainRatio = (double) numberOfUniqueDomains / noURL;
//        } else {
//            domainRatio = 0;
//        }
//    }
//
//    public static String getDomainName(String url) throws URISyntaxException {
//        URI uri = new URI(url);
//        String domain = uri.getHost();
//        return domain.startsWith("www.") ? domain.substring(4) : domain;
//    }
    
    
//    //doubles

//    /**
//     * Cleans tweets text from URLs and RT@ and keeps it pure text.
//     *
//     * @param newTweet
//     */
//    public void addAndProcessTweet(String newTweet) {
//
//        String[] elements = newTweet.split("@");
//        //@gregclermont no, there is not. ^TS
//        if (elements.length != 0) {
//            String cleanedTweet = "";
//            for (int i = 0; i < elements.length; i++) {
//                String[] newElements = elements[i].split(" ");
//                for (int j = 1; j < newElements.length; j++) {
//                    cleanedTweet += newElements[j] + " ";
//                }
//            }
//            cleanedTweet = cleanedTweet.replaceAll("https?://\\S+\\s?", "");
//            tweets.add(cleanedTweet);
//        } else {
//            tweets.add(newTweet);
//        }
//    }
    
    
//     /**
//     * Using the levenshtein distance it calculates how many tweets are actually
//     * duplicates.
//     */
//    public int calculateCopies() {
//
//        if (!tweets.isEmpty()) {
//            HashSet<String> uniqueTexts = new HashSet<>(); //used to store the unique texts of tweets
//            boolean flag = false;
//
//            uniqueTexts.add(tweets.get(0));
//
//            for (int i = 1; i < tweets.size(); i++) {
//
//                for (String text : uniqueTexts) {
//
//                    int distance = LevenshteinDistance.computeDistance(tweets.get(i), text);
//                    double normalized_distance = (double) distance / (tweets.get(i).length() + text.length());
//
//                    if (normalized_distance < 0.1) {
//                        copiedTweets++;
//                        flag = true;
//                        break;
//                    }
//                }
//
//                if (!flag) {
//                    uniqueTexts.add(tweets.get(i));
//                    flag = false;
//                }
//            }
//        } else {
//            copiedTweets = 0;
//        }
//        
//        
//        return 0;
//    }
    
    
    //length of dormancy period (date of first post - account creation date)
    
    
    
    
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
        return this.tweetsWithTimesRetweeted;
    }

}
