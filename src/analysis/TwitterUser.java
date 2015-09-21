package analysis;

import com.mongodb.DBObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import twitter4j.JSONArray;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.User;

/**
 * Represents a Twitter User with the main data that we desire. We collect both
 * user-based and content-based features data.
 *
 * @author Sokratis Papadopoulos
 */
public class TwitterUser {

    private Long id; //stores the userID

    //FOLLOWERS - FOLLOWEES
    private int followers;
    private int followees;
    private double fol_fol_ratio; //followers/followees

    //ACOUNT AGE
    private long age;

    //TWEETS - RETWEETS
    private long tweets; // number of tweets the user did
    private long retweets; //number of retweets the user did
    private double retweetsTweetsRatio; //ratio of retweets done / tweets done
    private ArrayList<DBObject> statuses;
    private ArrayList<String> texts;

    //FAVORITES
    private int favorites;

    //VERIFIED
    private boolean verified;

    //DESCRIPTION
    private String description;
    
    //DEFAULT PROFILE & IMG
    private boolean defProf;
    private boolean defProfImg;
    
    //HASHTAGS
    private int hashtags;
    private double avgHTperTweet;
    private double percentageTweetsWithHT;

    //MENTIONS
    private int mentions;
    private double avgMentionsPerTweet;
    private double percentageTweetsWithMentions;

    //URLS
    private int urls;
    private double avgURLsPerTweet;
    private double percentageTweetsWithURL;
    private double urlRatio;
    private HashSet<String> uniqueURLs;

    //SOURCES
    private HashMap<String, Integer> tweetsPerSource;
    private String mostFrequentSource;

    //DOMAINS
    private HashSet<String> uniqueDomains;
    private double urlDomainRatio;

    //TIME FOLLOWED
    private long timeFollowed; // total time we followed the user (hours)

    //NEW UNEXPLORED STUFF
    private double compressionRatio;
    private int entropy;

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

        this.followers = 0;
        this.followees = 0;
        this.fol_fol_ratio = 0;

        this.age = 0;

        this.tweets = 0;
        this.retweets = 0;
        this.retweetsTweetsRatio = 0;
        this.statuses = new ArrayList<>();

        this.favorites = 0;

        this.verified = false;

        this.hashtags = 0;
        this.avgHTperTweet = 0;
        this.percentageTweetsWithHT = 0;

        this.mentions = 0;
        this.avgMentionsPerTweet = 0;
        this.percentageTweetsWithMentions = 0;

        this.urls = 0;
        this.avgURLsPerTweet = 0;
        this.percentageTweetsWithURL = 0;
        this.urlRatio = 0;
        this.uniqueURLs = new HashSet<>();

        this.description = null;

        this.tweetsPerSource = new HashMap<>();
        this.mostFrequentSource = null;

        this.uniqueDomains = new HashSet<>();
        this.urlDomainRatio = 0;

        this.timeFollowed = 0;

        this.defProf = false;
        this.defProfImg = false;

        this.compressionRatio = 0;
        this.entropy = 0;

    }

    /**
     * Collects information for a user as taken from his record in mongoDB.
     *
     * @param ver
     * @param friends
     * @param u
     * @param createdAt
     * @param fav
     * @param desc
     * @param defPro
     * @param defProImg
     * @param fol
     */
    public void checkUserDetails() {

//        setVerified(objectBool(ver));
//        setFollowees(friends);
//        setFollowers(fol);
        setFol_fol_ratio(calculateRatio(followers, followees));
//        setAge(createdAt);
//        setFavorites(fav);
//        setDescription(desc);
//        setDefProf(objectBool(defPro));
//        setDefProfImg(objectBool(defProImg));

    }

    /**
     * Collects content-based information for a user based on his tweets.
     *
     * @throws java.net.URISyntaxException
     * @throws twitter4j.JSONException
     */
    public void checkTweetsDetails() throws URISyntaxException, JSONException, IOException {

        this.setRetweetsTweetsRatio(calculateRatio(retweets, tweets));
        int[] mentionsArr = new int[statuses.size()];
        int[] hashtagsArr = new int[statuses.size()];
        int[] urlsArr = new int[statuses.size()];
        
        int counter = 0;
        int ht = 0, m = 0, url = 0; //unique tweets that has an entity

        for (DBObject tweet : statuses) {
            
            JSONObject jobj = new JSONObject(tweet.toString());
            JSONObject entities = jobj.getJSONObject("entities"); //getting inside "entities" in json

            //======= GETTING MENTIONS =======
            JSONArray mentionArray = entities.getJSONArray("user_mentions");
            mentionsArr[counter] = mentionArray.length();

            //======= GETTING HASHTAGS =======
            JSONArray hashtagArray = entities.getJSONArray("hashtags");
            hashtagsArr[counter] = hashtagArray.length();

            //======= GETTING URLs =======
            JSONArray urlArray = entities.getJSONArray("urls");
            urlsArr[counter] = urlArray.length();

            //======= GETTING EXPANDED URLs =======
            String[] expanded_url = new String[urlArray.length()];
            if (urlsArr[counter] != 0) { //if URLs exist, it takes the expanded version of them

                for (int j = 0; j < urlArray.length(); j++) {
                    expanded_url[j] = urlArray.getJSONObject(j).getString("expanded_url");
                }
            }

            //======= GETTING SOURCE =======
            try{
                getSourceAndAddToMap(tweet.get("source").toString());
            } catch (java.lang.NullPointerException ex){
                //System.out.println("null pointer on source " + tweet.get("source").toString());
            }
            
            mentions += mentionsArr[counter];
            hashtags += hashtagsArr[counter];
            urls += urlsArr[counter];

            if (mentionsArr[counter] != 0) {
                m++;
            }
            if (hashtagsArr[counter] != 0) {
                ht++;
            }
            if (urlsArr[counter] != 0) {
                url++;
            }
            
            for (String expUrl : expanded_url) {
                addUniqueURL(expUrl);
            }

            counter++;
        }

        setAvgMentionsPerTweet(calcAverage(mentionsArr));
        setAvgHTperTweet(calcAverage(hashtagsArr));
        setAvgURLsPerTweet(calcAverage(urlsArr));

        setPercentageTweetsWithMentions(calcPercentage(m));
        setPercentageTweetsWithHT(calcPercentage(ht));
        setPercentageTweetsWithURL(calcPercentage(url));

        setUrlRatio(calculateRatio(uniqueURLs.size(), urls));

        calculateMostFrequentSource();
        try{
            calculateUniqueDomains();
        } catch (java.net.URISyntaxException ex){
            //System.out.println("url syntax exception");
        }
        
        doCompression();
    }
    
    public void addTweet(String t){
        texts.add(t);
    }
    
    /**
     * prints to console for testing features values.
     */
    public void sout(){
        System.out.println("=== USER: " + id + "===");
        System.out.println("1. verified: " + verified);
        System.out.println("2. friends: " + followees);
        System.out.println("3. followers: " + followers);
        System.out.println("4. fol_fol_ratio: " + fol_fol_ratio);
        System.out.println("5. accountAge: " + age);
        System.out.println("6. favourites: " + favorites);
        System.out.println("7. description: " + description);
        System.out.println("8. defaultProfile: " + defProf);
        System.out.println("9. defaultProfileImage: " + defProfImg);    
        System.out.println("10. tweets: " + tweets);
        System.out.println("11. retweets: " + retweets);
        System.out.println("12. rt_t_ratio: " + retweetsTweetsRatio);
        System.out.println("13. mentions: " + mentions);
        System.out.println("14. avgMentions: " + avgMentionsPerTweet);
        System.out.println("15. percentageMentions: " + percentageTweetsWithMentions);
        System.out.println("16. hashtags: " + hashtags);
        System.out.println("17. avgHashtags: " + avgHTperTweet);
        System.out.println("18. percentageHashtags: " + percentageTweetsWithHT);
        System.out.println("19. urls: " + urls);
        System.out.println("20. avgUrls: " + avgURLsPerTweet);
        System.out.println("21. percentageUrls: " + percentageTweetsWithURL);
        System.out.println("22. urlRatio: " + urlRatio);
        System.out.println("23. source: " + mostFrequentSource);
        System.out.println("24. url_domain_Ratio: " + urlDomainRatio);
        System.out.println("25. timeFollowed: " + timeFollowed); 
        System.out.println("26. compressionRatio: " + compressionRatio);
    }

    /**
     * Calculates any requested ratio.
     *
     * @param numerator
     * @param denominator
     * @return requested ratio or 0 if denominator is 0
     */
    public double calculateRatio(long numerator, long denominator) {
        double ratio;
        if (denominator != 0) {
            ratio = (double) numerator / denominator;
        } else {
            ratio = 0;
        }
        return ratio;
    }

    /**
     * Calculates the average of any given array.
     *
     * @param array
     * @return
     */
    private double calcAverage(int[] array) {

        double sum = 0;
        for (int j = 0; j < array.length; j++) {
            sum += array[j];
        }

        if (array.length != 0) {
            return sum / array.length;
        } else {
            return 0;
        }
    }

    /**
     * Calculates any percentage in tweets.
     *
     * @param numerator
     * @return
     */
    private double calcPercentage(double numerator) {
        if (tweets != 0) {
            return (100 * numerator / tweets);
        } else {
            return 0;
        }
    }

    /**
     * checks if a an object is true or not.
     *
     * @param value
     * @return true if verified, false if not.
     */
    public boolean objectBool(Object value) {

        if (value.toString().equals("false")) {
            return false;
        }
        return true;
    }

    /**
     * Gets a source name and adds it to relevant structures.
     *
     * @param source
     */
    private void getSourceAndAddToMap(String source) {
        org.jsoup.nodes.Document doc = Jsoup.parse(source);
        Element link = doc.select("a").first();
        String name = link.text();
        if (tweetsPerSource.containsKey(name)) {
            tweetsPerSource.replace(name, tweetsPerSource.get(name) + 1);
        } else {
            tweetsPerSource.put(name, 1);
        }
    }

    /**
     * calculates the most frequent source for the user after examining all the
     * sources that he used.
     *
     */
    public void calculateMostFrequentSource() {
        Integer maxFrequency = -1;
        Iterator it = tweetsPerSource.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry tPs = (Map.Entry) it.next();
            if (maxFrequency < (Integer) tPs.getValue()) {
                maxFrequency = (Integer) tPs.getValue();
                mostFrequentSource = (String) tPs.getKey();
            }
        }
    }

    /**
     * Adds a url to the unique urls list. If it exists already, the hashset is
     * going to reject it.
     *
     * @param url
     */
    public void addUniqueURL(String url) {
        uniqueURLs.add(url);
    }

    /**
     * Calculates the unique domains used by urls, the domain ratio
     *
     * @throws URISyntaxException
     */
    public void calculateUniqueDomains() throws URISyntaxException {
        for (String aUrl : uniqueURLs) {
            uniqueDomains.add(getDomainName(aUrl));
        }
        urlDomainRatio = calculateRatio(uniqueDomains.size(), urls);
    }

    /**
     * Extracts the domain name out of a string.
     *
     * @param url
     * @return
     * @throws URISyntaxException
     */
    public static String getDomainName(String url) throws URISyntaxException {
        try{
            URI uri = new URI(url);
            String domain = uri.getHost();
            return domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch(java.lang.NullPointerException ex){
            //System.out.println("null pointer inside getDomainName");
        }
        return null;
    }

    /**
     * Calculates the difference between two dates, at any time unit.
     *
     * @param date1
     * @param date2
     * @param timeUnit
     * @return
     */
    public long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }
    
    public static byte[] compressToByte(final String data) throws IOException {
        if (data == null || data.length() == 0){
            return null;
        } else {
            byte[] bytes = data.getBytes();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream os = new GZIPOutputStream(baos);
            os.write(bytes, 0, bytes.length);
            os.close();
            byte[] result = baos.toByteArray();
            return result;
        }
    }
    
    public static String unCompressString(final byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return null;
        } else {
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            GZIPInputStream is = new GZIPInputStream(bais);
            byte[] tmp = new byte[256];
            while (true)
            {
                int r = is.read(tmp);
                if (r < 0)
                {
                    break;
                }
                buffer.write(tmp, 0, r);
            }
            is.close();

            byte[] content = buffer.toByteArray();
            return new String(content, 0, content.length);
        }
    }
    
    public void doCompression() throws IOException{
        int uncompressed=0;
        int compressed=0;
        for(String text: texts){
            uncompressed += text.getBytes().length;
            compressed += compressToByte(text).length;
        }
        setCompressionRatio(calculateRatio(uncompressed, compressed));
    } 

    //===================== GETTERS & SETTERS ==================================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowees() {
        return followees;
    }

    public void setFollowees(int followees) {
        this.followees = followees;
    }

    public double getFol_fol_ratio() {
        return fol_fol_ratio;
    }

    public void setFol_fol_ratio(double fol_fol_ratio) {
        this.fol_fol_ratio = fol_fol_ratio;
    }

    public long getAge() {
        return age;
    }

    public void setAge(String createdAge) {

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            Date accountCreationTime = dateFormat.parse(createdAge);
            Date currentTime = new Date();
            dateFormat.format(currentTime);

            this.age = getDateDiff(accountCreationTime, currentTime, TimeUnit.DAYS);

        } catch (ParseException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    public double getRetweetsTweetsRatio() {
        return retweetsTweetsRatio;
    }

    public void setRetweetsTweetsRatio(double retweetsTweetsRatio) {
        this.retweetsTweetsRatio = retweetsTweetsRatio;
    }

    public ArrayList<DBObject> getStatuses() {
        return statuses;
    }

    public void setStatuses(ArrayList<DBObject> statuses) {
        this.statuses = statuses;
    }

    public void addStatus(DBObject status) {
        statuses.add(status);
    }

    public int getFavorites() {
        return favorites;
    }

    public void setFavorites(int favorites) {
        this.favorites = favorites;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public int getHashtags() {
        return hashtags;
    }

    public void setHashtags(int hashtags) {
        this.hashtags = hashtags;
    }

    public double getAvgHTperTweet() {
        return avgHTperTweet;
    }

    public void setAvgHTperTweet(double avgHTperTweet) {
        this.avgHTperTweet = avgHTperTweet;
    }

    public double getPercentageTweetsWithHT() {
        return percentageTweetsWithHT;
    }

    public void setPercentageTweetsWithHT(double percentageTweetsWithHT) {
        this.percentageTweetsWithHT = percentageTweetsWithHT;
    }

    public int getMentions() {
        return mentions;
    }

    public void setMentions(int mentions) {
        this.mentions = mentions;
    }

    public double getAvgMentionsPerTweet() {
        return avgMentionsPerTweet;
    }

    public void setAvgMentionsPerTweet(double avgMentionsperTweet) {
        this.avgMentionsPerTweet = avgMentionsperTweet;
    }

    public double getPercentageTweetsWithMentions() {
        return percentageTweetsWithMentions;
    }

    public void setPercentageTweetsWithMentions(double percentageTweetsWithMentions) {
        this.percentageTweetsWithMentions = percentageTweetsWithMentions;
    }

    public int getUrls() {
        return urls;
    }

    public void setUrls(int urls) {
        this.urls = urls;
    }

    public double getAvgURLsPerTweet() {
        return avgURLsPerTweet;
    }

    public void setAvgURLsPerTweet(double avgURLsPerTweet) {
        this.avgURLsPerTweet = avgURLsPerTweet;
    }

    public double getPercentageTweetsWithURL() {
        return percentageTweetsWithURL;
    }

    public void setPercentageTweetsWithURL(double percentageTweetsWithURL) {
        this.percentageTweetsWithURL = percentageTweetsWithURL;
    }

    public double getUrlRatio() {
        return urlRatio;
    }

    public void setUrlRatio(double urlRatio) {
        this.urlRatio = urlRatio;
    }

    public HashSet<String> getUniqueURLs() {
        return uniqueURLs;
    }

    public void setUniqueURLs(HashSet<String> uniqueURLs) {
        this.uniqueURLs = uniqueURLs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public HashMap<String, Integer> getTweetsPerSource() {
        return tweetsPerSource;
    }

    public void setTweetsPerSource(HashMap<String, Integer> tweetsPerSource) {
        this.tweetsPerSource = tweetsPerSource;
    }

    public String getMostFrequentSource() {
        return mostFrequentSource;
    }

    public void setMostFrequentSource(String mostFrequentSource) {
        this.mostFrequentSource = mostFrequentSource;
    }

    public HashSet<String> getUniqueDomains() {
        return uniqueDomains;
    }

    public void setUniqueDomains(HashSet<String> uniqueDomains) {
        this.uniqueDomains = uniqueDomains;
    }

    public double getUrlDomainRatio() {
        return urlDomainRatio;
    }

    public void setUrlDomainRatio(double url_domain_ratio) {
        this.urlDomainRatio = url_domain_ratio;
    }

    public long getTimeFollowed() {
        return timeFollowed;
    }

    public void setTimeFollowed(String start, String end) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        Date st = dateFormat.parse(start);
        Date en = dateFormat.parse(end);
        this.timeFollowed = getDateDiff(st, en, TimeUnit.HOURS);
    }

    public void setTimeFollowed(long hours) {
        this.timeFollowed = hours;
    }

    public void setTimeFollowed(DBObject rec) throws ParseException {
        if (rec != null) {
            setTimeFollowed(rec.get("starting_time").toString(), rec.get("finish_time").toString());
        } else {
            setTimeFollowed(0);
        }
    }

    public boolean isDefProf() {
        return defProf;
    }

    public void setDefProf(boolean defProf) {
        this.defProf = defProf;
    }

    public boolean isDefProfImg() {
        return defProfImg;
    }

    public void setDefProfImg(boolean defProfImg) {
        this.defProfImg = defProfImg;
    }

    public ArrayList<String> getTexts() {
        return texts;
    }

    public void setTexts(ArrayList<String> texts) {
        this.texts = texts;
    }

    public double getCompressionRatio() {
        return compressionRatio;
    }

    public void setCompressionRatio(double compressionRatio) {
        this.compressionRatio = compressionRatio;
    }

    public int getEntropy() {
        return entropy;
    }

    public void setEntropy(int entropy) {
        this.entropy = entropy;
    }

}
