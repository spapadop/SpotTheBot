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
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private String description;
    private String fullName;
    private String imgURL;
    private String bannerURL;
    private String linkToProf;
    
    private HashMap<Long, String> binData;
    
    private void loadBinData() throws IOException{
        binData = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\randomSample\\run1\\sample.txt"));
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

    public AnnotationUser(ArrayList<User> detailedUsers) throws UnknownHostException, FileNotFoundException, UnsupportedEncodingException, IOException, JSONException {
        mongo = new MongoDBHandler();
        loadBinData();   

        writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\results.txt", "UTF-8");
        writeTemplate();

        for (User u : detailedUsers) {
            id = u.getId();
            screenName = u.getScreenName();
            verified = u.isVerified();
            setAge(u.getCreatedAt().toString());
            description = u.getDescription();
            fullName = u.getName();
            imgURL = u.getBiggerProfileImageURL();
            bannerURL = u.getProfileBannerURL();
            linkToProf = "https://twitter.com/" + screenName;

            HashMap<String, Integer> dupli = calcDuplicates(id);
            int numDuplicates = 0;
            for (Map.Entry pair : dupli.entrySet()) {
                if ((Integer) pair.getValue() != 1) {
                    numDuplicates++;
                }
            }

            HashMap<String, Integer> top5 = findTop5Dublicates(dupli);
            
            screenName = screenName.replaceAll("\"", "");;
            fullName = fullName.replaceAll("\"", "");;
            description = description.replaceAll("[\\t\\n\\r]", " ").toLowerCase().trim();
            description = description.replaceAll("\"", "");
            
            writer.print(id + "\t" + binData.get(id) + "\t" + fullName + "\t" + screenName + "\t" + verified + "\t" + age + "\t" + numDuplicates
                    + "\t" + description + "\t" + imgURL + "\t" + bannerURL + "\t" + linkToProf
            );

            for (Map.Entry pair : top5.entrySet()) {
                if (pair.getKey() != null) {
                    String query = "https://twitter.com/search?q=";
                    String[] words = pair.getKey().toString().split(" ");
                    query += words[0];
                    for (int i = 1; i < words.length; i++) {
                        query += "%20" + words[i];
                    }
                    query += "&src=typd";
                    writer.print("\t" + user.getCleanDirtyTexts().get(pair.getKey().toString()) + "\t" + pair.getValue() + "\t" + query);
                } else {
                    writer.print("\t" + null + "\t" + null + "\t" + null);
                }
            }

            if (top5.size() < 5) {
                for (int i = 0; i < (5 - top5.size()); i++) {
                    writer.print("\t" + null + "\t" + null + "\t" + null);
                }
            }

            String[] topWords = findTop10Words();
            for (int i = 0; i < 10; i++) {
                writer.print("\t" + topWords[i]);
            }

            writer.print("\n");
        }

        writer.close();

        removeDeletedUsers();

    }

    private String[] findTop10Words() throws FileNotFoundException, IOException {
        String[] top10words = new String[10];
        ArrayList<String> texts = user.getCleanTexts();

        HashMap<String, Integer> words = new HashMap<>();

        for (String text : texts) {
            String[] tokens = text.split(" ");
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
        stopwords.add(",");
        stopwords.add(".");
        stopwords.add(":");
        stopwords.add(";");
        stopwords.add("!");
        stopwords.add("-");
        

        for (String sw : stopwords) {
            words.remove(sw);
        }

        for (int i = 0; i < 10; i++) {
            int max = -10;
            String maxKey = null;
            for (Map.Entry pair : words.entrySet()) {
                if ((Integer) pair.getValue() > max) {
                    maxKey = pair.getKey().toString();
                    max = (int) pair.getValue();
                }
            }

            top10words[i] = maxKey;
            words.remove(maxKey);
        }

        return top10words;
    }

    private HashMap<String, Integer> findTop5Dublicates(HashMap<String, Integer> dupli) {
        HashMap<String, Integer> top = new HashMap<>();

        for (int i = 0; i < 5; i++) {

            int max = -10;
            String maxKey = null;
            for (Map.Entry pair : dupli.entrySet()) {
                if ((Integer) pair.getValue() > max) {
                    maxKey = pair.getKey().toString();
                    max = (int) pair.getValue();
                }
            }

            top.put(maxKey, dupli.get(maxKey));
            dupli.remove(maxKey);
        }

        return top;
    }

    private HashMap<String, Integer> calcDuplicates(Long id) throws JSONException {
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
        }

        for (DBObject tweet : user.getStatuses()) {
            JSONObject jobj = new JSONObject(tweet.toString());
            user.addTweet(jobj.getString("text"));
        }
        return user.duplicates();
    }

    private void writeTemplate() throws FileNotFoundException, UnsupportedEncodingException {
        writer.print("userID" + "\t" + "Feature" + "\t" + "timesOccuredInBin" + "\t" + "binNum" + "\t" + "FullName" + "\t" + "ScreenName" + "\t" + "Verified" + "\t" + "Age" + "\t" + "numDuplicates");
        writer.print("\t" + "Description");
        writer.print("\t" + "Profile picture");
        writer.print("\t" + "Profile banner");
        writer.print("\t" + "Link to profil");
        writer.print("\t" + "tweet1" + "\t" + "numTimesPosted1" + "\t" + "Link_to_live_feed_for_text1");
        writer.print("\t" + "tweet2" + "\t" + "numTimesPosted2" + "\t" + "Link_to_live_feed_for_text2");
        writer.print("\t" + "tweet3" + "\t" + "numTimesPosted3" + "\t" + "Link_to_live_feed_for_text3");
        writer.print("\t" + "tweet4" + "\t" + "numTimesPosted4" + "\t" + "Link_to_live_feed_for_text4");
        writer.print("\t" + "tweet5" + "\t" + "numTimesPosted5" + "\t" + "Link_to_live_feed_for_text5");
        writer.print("\t" + "word1" + "\t" + "word2" + "\t" + "word3" + "\t" + "word4" + "\t" + "word5" + "\t" + "word6" + "\t" + "word7" + "\t" + "word8" + "\t" + "word9" + "\t" + "word10");
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
        writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\finalResults.txt", "UTF-8");

        File file = new File("C:\\Users\\sokpa\\Desktop\\run1_deleted.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        HashSet<Long> delUsers = new HashSet<>();
        String line;
        while ((line = reader.readLine()) != null) {
            delUsers.add(Long.parseLong(line));
        }
        reader.close();

        file = new File("C:\\Users\\sokpa\\Desktop\\results.txt");
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
