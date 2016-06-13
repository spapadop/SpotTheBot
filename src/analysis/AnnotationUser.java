package analysis;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import crawling.MongoDBHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.shortdistance.LevenshteinDistance;
import twitter4j.JSONException;
import twitter4j.JSONObject;
import twitter4j.User;

/**
 *
 * @author Sokratis Papadopoulos
 */
public class AnnotationUser {

    private final MongoDBHandler mongo;
    private PrintWriter writer;
    private TwitterUser user;
    private long id;
    private String screenName;
    private boolean verified;
    private int age;
    private int numDuplicates;
    private String description;
    private String fullName;
    private String imgURL;
    private String bannerURL;
    private String linkToProf;

    private ArrayList<AnnotationTweet> userTweets;
    private ArrayList<AnnotationTweet> userTweetsTopFive;
    private ArrayList<AnnotationTweet> userTweetsUnique;

    private HashMap<String, Integer> retweetUsers;

    private HashMap<Long, String> binData;

    private void loadBinData() throws IOException {
        binData = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\randomSample\\run2\\sample.txt"));
        String line = reader.readLine(); //header

        line = reader.readLine();//body
        while (line != null) {
            String[] tokens = line.split("\t");

            Long userID = Long.parseLong(tokens[0]);
            String sampleDataLine = tokens[1] + "\t" + tokens[2] + "\t" + tokens[3];

            binData.put(userID, sampleDataLine);
            line = reader.readLine();
        }
        reader.close();
    }

    private void calculateNumberOfDuplicates() {
        numDuplicates = 0;

        for (AnnotationTweet t : userTweets) {
            //System.out.println(t.getCleanText());
            if (!existsInUserTweetsUnique(t.getCleanText())) {
                userTweetsUnique.add(t);
            }
        }
        //System.out.println("");
        //System.out.println("=============================");
        //System.out.println("");
        for (AnnotationTweet t : userTweetsUnique) {
            //System.out.println(t.getCleanText() + "\t" + t.getNumTimesPosted());
            if (t.getNumTimesPosted() > 1) {
                numDuplicates++;
            }
        }
    }

    private boolean existsInUserTweetsUnique(String cleanText) {
        for (AnnotationTweet text : userTweetsUnique) {
            if (isSimilar(text.getCleanText(), cleanText)) {
                return true;
            }
        }
        return false;
    }

    public AnnotationUser(ArrayList<User> detailedUsers) throws UnknownHostException, FileNotFoundException, UnsupportedEncodingException, IOException, JSONException, URISyntaxException {

        mongo = new MongoDBHandler();
        loadBinData();

        writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\results2.txt", "UTF-8");
        writeTemplate();

        for (User u : detailedUsers) {
            userTweets = new ArrayList<>();
            userTweetsTopFive = new ArrayList<>();
            userTweetsUnique = new ArrayList<>();
            id = u.getId();
            screenName = u.getScreenName();
            verified = u.isVerified();
            setAge(u.getCreatedAt().toString());
            description = u.getDescription();
            fullName = u.getName();
            imgURL = u.getBiggerProfileImageURL();
            bannerURL = u.getProfileBannerURL();
            linkToProf = "https://twitter.com/" + screenName;
            retweetUsers = new HashMap<>();

            calcDuplicates();
            calculateNumberOfDuplicates();
            findTop5Dublicates();

            screenName = screenName.replaceAll("\"", "");;
            fullName = fullName.replaceAll("\"", "");;
            description = description.replaceAll("[\\t\\n\\r]", " ").toLowerCase().trim();
            description = description.replaceAll("\"", "");
            
            fullName = Normalizer.normalize(fullName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
            screenName = Normalizer.normalize(screenName, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
            description = Normalizer.normalize(description, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");

            writer.print(id + "\t" + binData.get(id) + "\t" + fullName + "\t" + screenName + "\t" + verified + "\t" + age + "\t" + numDuplicates
                    + "\t" + description + "\t" + imgURL + "\t" + bannerURL + "\t" + linkToProf
            );

            for (AnnotationTweet t : userTweetsTopFive) {
                if (t != null) {
                    writer.print("\t" + t.getText() + "\t" + t.getNumTimesPosted() + "\t" + Normalizer.normalize(t.getLiveFeedLink(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "") + "\t" + t.getTweetID());
                } else {
                    writer.print("\t" + "n/a" + "\t" + "n/a" + "\t" + "n/a" + "\t" + "n/a");
                }
            }

            if (userTweetsTopFive.size() < 5) {
                for (int i = 0; i < (5 - userTweetsTopFive.size()); i++) {
                    writer.print("\t" + "n/a" + "\t" + "n/a" + "\t" + "n/a" + "\t" + "n/a");
                }
            }

            String[][] topWords = findTop10Words();
            for (int i = 0; i < 10; i++) {
                writer.print("\t" + Normalizer.normalize(topWords[i][0], Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "") + "\t" + topWords[i][1]);
            }

//            HashMap<String, Integer> topWords = findTop10Words();
//            Iterator it = topWords.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry pair = (Map.Entry)it.next();
//                writer.print("\t" + pair.getKey() + "\t" + pair.getValue());
//                it.remove(); // avoids a ConcurrentModificationException
//            }
            
            //print url-domain:
            writer.printf("\t" + "%.2f", user.getUrlDomainRatio());

            // sources 
            int counter = 0;
            while (counter < 10 && !user.getTweetsPerSource().isEmpty()) {
                String maxKey = user.calculateMostFrequentSource();
                writer.print("\t" + Normalizer.normalize(maxKey, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "") + "\t" + user.getTweetsPerSource().get(maxKey));
                user.removeSource(maxKey);
                counter++;
            }

            if (counter < 10) {
                for (int i = counter; i < 10; i++) {
                    writer.print("\t" + "n/a" + "\t" + "0");
                }
            }

            //users retweeted
            counter = 0;
            while (counter < 10 && !retweetUsers.isEmpty()) {
                String maxKey = findMostRetweetedUser();
                writer.print("\t" + mongo.findScreenName(maxKey) + "\t" + retweetUsers.get(maxKey));
                retweetUsers.remove(maxKey);
                counter++;
            }

            if (counter < 10) {
                for (int i = counter; i < 10; i++) {
                    writer.print("\t" + "n/a" + "\t" + "0");
                }
            }
            
            //domains
            counter = 0;
            while (counter < 10 && !user.getTweetsPerDomain().isEmpty()) {
                String maxKey = user.calculateMostFrequentDomain();
                String temp = Normalizer.normalize(maxKey, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
                writer.print("\t" + temp + "\t" + user.getTweetsPerDomain().get(maxKey));
                user.removeDomain(maxKey);
                counter++;
            }

            if (counter < 10) {
                for (int i = counter; i < 10; i++) {
                    writer.print("\t" + "n/a" + "\t" + "0");
                }
            }
            
            writer.print("\n");
        }

        writer.close();

        removeDeletedUsers();

    }

    private String findMostRetweetedUser() {
        int max = -10;
        String maxKey = null;
        for (Map.Entry pair : retweetUsers.entrySet()) {
            if ((Integer) pair.getValue() > max) {
                maxKey = pair.getKey().toString();
                max = (int) pair.getValue();
            }
        }
        return maxKey;
    }

    private String[][] findTop10Words() throws FileNotFoundException, IOException {
        String[][] top10words = new String[10][2];
        int pos = 0;

        ArrayList<String> texts = new ArrayList<>();
        for (AnnotationTweet t : userTweets) {
            texts.add(t.getCleanText());
        }

        HashMap<String, Integer> words = new HashMap<>();

        for (String text : texts) {
            String[] tokens = text.split("\\s+");
            for (int i = 0; i < tokens.length; i++) {
                if (words.containsKey(tokens[i])) {
                    words.replace(tokens[i], words.get(tokens[i]) + 1);
                } else {
                    words.put(tokens[i], 1);
                }
            }
        }

        //remove stopwords
        File file = new File("C:\\Users\\sokpa\\Desktop\\annotation\\stopwords.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        HashSet<String> stopwords = new HashSet<>();
        String stopword;
        while ((stopword = reader.readLine()) != null) {
            stopwords.add(stopword);
        }

        stopwords.add("");
        stopwords.add("\t");
        stopwords.add("\n");
        stopwords.add("\r");
        stopwords.add(" "); 
        stopwords.add(",");
        stopwords.add(".");
        stopwords.add("..");    
        stopwords.add("...");
        stopwords.add(":");
        stopwords.add(";");
        stopwords.add("!");
        stopwords.add("-");
        stopwords.add("?");
        stopwords.add("??");
        stopwords.add("???");
        stopwords.add("????");
        stopwords.add("?????");
        stopwords.add("&");
        stopwords.add("d");
        stopwords.add("u");
        stopwords.add("i");
        stopwords.add("!!");
        stopwords.add("!!!");
        stopwords.add("r");
        stopwords.add("&amp;");

        for (String sw : stopwords) {
            words.remove(sw);
        }

        for (int i = 0; i < 10; i++) {
            int max = 0;
            String maxKey = "stopword" + i;
            for (Map.Entry pair : words.entrySet()) {
                if ((Integer) pair.getValue() > max) {
                    maxKey = pair.getKey().toString();
                    max = (int) pair.getValue();
                }
            }

            top10words[pos][0] = maxKey;
            top10words[pos][1] = "" + max;
            pos++;
            //top10words.put(maxKey, max);
            words.remove(maxKey);
        }

        return top10words;
    }

    private void findTop5Dublicates() {

        for (int i = 0; i < 5; i++) {
            int max = -10;
            AnnotationTweet maxObject = null;
            for (AnnotationTweet t : userTweetsUnique) {
                if (t.getNumTimesPosted() > max && !existsInTopFive(t.getCleanText())) {
                    maxObject = t;
                    max = t.getNumTimesPosted();
                }
            }
            userTweetsTopFive.add(maxObject);
            userTweetsUnique.remove(maxObject);

        }
    }

    private boolean existsInTopFive(String cleanText) {
        for (AnnotationTweet text : userTweetsTopFive) {
            if (isSimilar(text.getCleanText(), cleanText)) {
                return true;
            }
        }
        return false;
    }

    private void calcDuplicates() throws JSONException, URISyntaxException, IOException {
        user = new TwitterUser(id); //create user for given id

        //======== CONSTRUCT QUERIES =============
        BasicDBObject userQuery = new BasicDBObject();
        userQuery.put("id_str", user.getId().toString());
        BasicDBObject tweetsQuery = new BasicDBObject(); //Here we count the original tweets user did.
        tweetsQuery.put("user_id", user.getId().toString());
        BasicDBObject retweetsQuery = new BasicDBObject(); //Here we get the tweets the user retweeted.
        retweetsQuery.put("retweetedUserID", user.getId().toString());

//        // =========== USER DETAILS ==============
//        DBObject muser = mongo.getUsersColl().findOne(userQuery);
//
//        verified = Boolean.getBoolean(muser.get("verified").toString());
//        setAge(muser.get("created_at").toString());
//        screenName = muser.get("screen_name").toString().replaceAll("\"", "");;
//        fullName = muser.get("name").toString().replaceAll("\"", "");;
//        linkToProf = "https://twitter.com/" + screenName;
//
//        try {
//            imgURL = muser.get("profile_image_url").toString();
//        } catch (java.lang.NullPointerException ex) {
//            //System.out.println("IMGURL not found.");
//        }
//
//        try {
//            bannerURL = muser.get("profile_banner_url").toString();
//        } catch (java.lang.NullPointerException ex) {
//            //System.out.println("BANNERURL not found.");
//        }
//
//        try {
//            description = muser.get("description").toString();
//            description = description.replaceAll("[\\t\\n\\r]", " ").toLowerCase().trim();
//            description = description.replaceAll("\"", "");
//        } catch (java.lang.NullPointerException ex) {
//            //System.out.println("DESCRIPTION not found.");
//        }
        // ========= TWEETS DETAILS =============
        user.setTweets(mongo.getTweetsColl().count(tweetsQuery)); //original tweets user did
        DBCursor tweets = mongo.getTweetsColl().find(tweetsQuery);
        while (tweets.hasNext()) {
            DBObject status = tweets.next();
            user.addStatus(status);
        }

        // ========= RETWEETS DETAILS ===========
        user.setRetweets(mongo.getRetweetsColl().count(retweetsQuery)); //retweets user did
        DBCursor retweets = mongo.getRetweetsColl().find(retweetsQuery);

        BasicDBObject findTweet = new BasicDBObject();
        while (retweets.hasNext()) {
            DBObject status = retweets.next();
            findTweet.put("id_str", status.get("originalTweetID").toString());
            user.addStatus(mongo.getTweetsColl().findOne(findTweet));

            String originalUserID = status.get("originalUserID").toString();
            if (!retweetUsers.containsKey(originalUserID)) {
                retweetUsers.put(originalUserID, 1);
            } else {
                retweetUsers.replace(originalUserID, (retweetUsers.get(originalUserID) + 1));
            }
        }

        for (DBObject tweet : user.getStatuses()) {
            JSONObject jobj = new JSONObject(tweet.toString());
            user.addTweet(jobj.getString("text"));

            userTweets.add(new AnnotationTweet(jobj.getString("id_str"), jobj.getString("text"))); //from tweets col
        }

        user.checkTweetsDetails();

        //all tweets are now gathered into userTweets collection. 
        //Now, we calculate duplicates
        if (!userTweets.isEmpty()) {
            for (AnnotationTweet at : userTweets) { //for every tweet-text in collection...
                //we make a loop in the collection to find calculateNumberPosted, counting them.
                for (Iterator<AnnotationTweet> iterator = userTweets.iterator(); iterator.hasNext();) {
                    AnnotationTweet other = iterator.next();
                    if (isSimilar(at.getCleanText(), other.getCleanText())) {
                        at.increaseNumTimesPosted();
                    }
                }
            }

        }
    }

    /**
     * Calculates the similarity of two given texts and returns if they are
     * similar or not.
     *
     * @param one
     * @param two
     * @return true if duplicate, false if not.
     */
    public boolean isSimilar(String one, String two) {

        int distance = LevenshteinDistance.computeDistance(one, two);
        double normalized_distance = (double) distance / (one.length() + two.length());

        if (normalized_distance < 0.1) {
            return true;
        } else {
            return false;
        }
    }

    private void writeTemplate() throws FileNotFoundException, UnsupportedEncodingException {
        writer.print("userID" + "\t" + "feature" + "\t" + "timesOccurredInBin" + "\t" + "binNum" + "\t" + "fullName" + "\t" + "screenName" + "\t" + "verified" + "\t" + "age" + "\t" + "numDuplicates");
        writer.print("\t" + "description");
        writer.print("\t" + "profImg");
        writer.print("\t" + "banner");
        writer.print("\t" + "profileLink");
        writer.print("\t" + "tweet1" + "\t" + "numTimesPosted1" + "\t" + "liveFeedLink1" + "\t" + "tweet1ID");
        writer.print("\t" + "tweet2" + "\t" + "numTimesPosted2" + "\t" + "liveFeedLink2" + "\t" + "tweet2ID");
        writer.print("\t" + "tweet3" + "\t" + "numTimesPosted3" + "\t" + "liveFeedLink3" + "\t" + "tweet3ID");
        writer.print("\t" + "tweet4" + "\t" + "numTimesPosted4" + "\t" + "liveFeedLink4" + "\t" + "tweet4ID");
        writer.print("\t" + "tweet5" + "\t" + "numTimesPosted5" + "\t" + "liveFeedLink5" + "\t" + "tweet5ID");
        writer.print("\t" + "word1" + "\t" + "valueWord1"
                + "\t" + "word2" + "\t" + "valueWord2"
                + "\t" + "word3" + "\t" + "valueWord3"
                + "\t" + "word4" + "\t" + "valueWord4"
                + "\t" + "word5" + "\t" + "valueWord5"
                + "\t" + "word6" + "\t" + "valueWord6"
                + "\t" + "word7" + "\t" + "valueWord7"
                + "\t" + "word8" + "\t" + "valueWord8"
                + "\t" + "word9" + "\t" + "valueWord9"
                + "\t" + "word10" + "\t" + "valueWord10"
                + "\t" + "urlDomainRatio"
                + "\t" + "source1" + "\t" + "valueSource1"
                + "\t" + "source2" + "\t" + "valueSource2"
                + "\t" + "source3" + "\t" + "valueSource3"
                + "\t" + "source4" + "\t" + "valueSource4"
                + "\t" + "source5" + "\t" + "valueSource5"
                + "\t" + "source6" + "\t" + "valueSource6"
                + "\t" + "source7" + "\t" + "valueSource7"
                + "\t" + "source8" + "\t" + "valueSource8"
                + "\t" + "source9" + "\t" + "valueSource9"
                + "\t" + "source10" + "\t" + "valueSource10"
                + "\t" + "retweeter1" + "\t" + "valueRetweeter1"
                + "\t" + "retweeter2" + "\t" + "valueRetweeter2"
                + "\t" + "retweeter3" + "\t" + "valueRetweeter3"
                + "\t" + "retweeter4" + "\t" + "valueRetweeter4"
                + "\t" + "retweeter5" + "\t" + "valueRetweeter5"
                + "\t" + "retweeter6" + "\t" + "valueRetweeter6"
                + "\t" + "retweeter7" + "\t" + "valueRetweeter7"
                + "\t" + "retweeter8" + "\t" + "valueRetweeter8"
                + "\t" + "retweeter9" + "\t" + "valueRetweeter9"
                + "\t" + "retweeter10" + "\t" + "valueRetweeter10"
                + "\t" + "domain1" + "\t" + "valueDomain1"
                + "\t" + "domain2" + "\t" + "valueDomain2"
                + "\t" + "domain3" + "\t" + "valueDomain3"
                + "\t" + "domain4" + "\t" + "valueDomain4"
                + "\t" + "domain5" + "\t" + "valueDomain5"
                + "\t" + "domain6" + "\t" + "valueDomain6"
                + "\t" + "domain7" + "\t" + "valueDomain7"
                + "\t" + "domain8" + "\t" + "valueDomain8"
                + "\t" + "domain9" + "\t" + "valueDomain9"
                + "\t" + "domain10" + "\t" + "valueDomain10"        
        );

        writer.print("\n");
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

    /**
     * Calculates the difference between two dates, at any time unit.
     *
     * @param date1
     * @param date2
     * @param timeUnit
     * @return
     */
    public int getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        long temp = timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
        int toReturn = (int) temp;
        return toReturn;
    }

    private void removeDeletedUsers() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\finalResults2.txt", "UTF-8");

        File file = new File("C:\\Users\\sokpa\\Desktop\\run2_deleted.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        HashSet<Long> delUsers = new HashSet<>();
        String line;
        while ((line = reader.readLine()) != null) {
            delUsers.add(Long.parseLong(line));
        }
        reader.close();

        file = new File("C:\\Users\\sokpa\\Desktop\\results2.txt");
        reader = new BufferedReader(new FileReader(file));
        line = reader.readLine(); //header out
        writer.println(line);
        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split("\t");
            if (!delUsers.contains(Long.parseLong(tokens[0]))) {
                writer.println(line);
            }
        }

        reader.close();
        writer.close();
    }

}
