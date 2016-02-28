package analysis;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
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
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import twitter4j.JSONException;
import twitter4j.JSONObject;

/**
 * Runs the features analysis based on our data collected. Calculates both
 * content-based and user-based feature values.
 *
 * @author Sokratis Papadopoulos
 */
public class FeaturesAnalysis {

    private MongoDBHandler mongo;
    private PrintWriter writer;
    private HashSet<Long> ids;
    private TwitterUser user;

    public FeaturesAnalysis() throws MongoException, UnknownHostException, FileNotFoundException, UnsupportedEncodingException, ParseException, URISyntaxException, JSONException, IOException {
        mongo = new MongoDBHandler();
        writer = new PrintWriter("test.txt", "UTF-8");
        ids = new HashSet<>();
        File file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results1-filtered-retOnly.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                ids.add(Long.parseLong(splited[1]));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        printHeader();

        for (Long id : ids) {
            user = new TwitterUser(id); //create user for given id

            //======== CONSTRUCT QUERIES =============
            BasicDBObject userQuery = new BasicDBObject();
            userQuery.put("id_str", user.getId().toString());
            BasicDBObject tweetsQuery = new BasicDBObject(); //Here we count the original tweets user did.
            tweetsQuery.put("user_id", user.getId().toString());
            BasicDBObject retweetsQuery = new BasicDBObject(); //Here we get the tweets the user retweeted.
            retweetsQuery.put("retweetedUserID", user.getId().toString());

            DBObject muser = mongo.getUsersColl().findOne(userQuery);

            // =========== USER DETAILS ==============
            user.setVerified(Boolean.getBoolean(muser.get("verified").toString()));
            user.setFollowers(Integer.parseInt(muser.get("followers_count").toString()));
            user.setFollowees(Integer.parseInt(muser.get("friends_count").toString()));
            user.setFavorites(Integer.parseInt(muser.get("favourites_count").toString()));
            user.setAge(muser.get("created_at").toString());
            user.setDefProf(Boolean.getBoolean(muser.get("default_profile").toString()));
            user.setDefProfImg(Boolean.getBoolean(muser.get("default_profile_image").toString()));
            try {
                user.setDescription(muser.get("description").toString());
            } catch (java.lang.NullPointerException ex) {
                //System.out.println("description not found.");
            }
            user.checkUserDetails(); //calculate all user details

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

            user.checkTweetsDetails();

            // ========== TIME FOLLOWED ==============
            user.setTimeFollowed(mongo.getTimeUserAppeared(user.getId()), mongo.getTimeUserDisappeared(user.getId()));

            //user.sout();
            printUser();

        }

        writer.close();
    }

    public FeaturesAnalysis(boolean flag) throws MongoException, UnknownHostException, FileNotFoundException, UnsupportedEncodingException, ParseException, URISyntaxException, JSONException, IOException {
        mongo = new MongoDBHandler();
        File file = new File("C:\\Users\\sokpa\\Desktop\\safe\\run2_all_final.csv");
        writer = new PrintWriter("test3.csv", "UTF-8");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            writer.println(text);

            while ((text = reader.readLine()) != null) {
                String[] tokens = text.split(",");
                Long id = Long.parseLong(tokens[0]);
                double entropy = calcEntropy(id);

                int i = 0;
                for (String token : tokens) {
                    if (i == tokens.length - 1) {
                        writer.print(entropy + "\n");
                    } else {
                        writer.print(token + ",");

                    }
                    i++;
                }

            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        writer.close();

    }

    public FeaturesAnalysis(boolean flag, boolean flag2) throws MongoException, UnknownHostException, FileNotFoundException, UnsupportedEncodingException, ParseException, URISyntaxException, JSONException, IOException {
        mongo = new MongoDBHandler();
        File file = new File("C:\\Users\\sokpa\\Desktop\\randomSample\\run1\\sample.txt");
        writer = new PrintWriter("test.txt", "UTF-8");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine(); //header-out
            writer.println("userID" + "\t" + "numDuplicates");
            while ((text = reader.readLine()) != null) {
                String[] tokens = text.split("\t");
                Long id = Long.parseLong(tokens[0]);

                HashMap<String, Integer> dupli = calcDuplicates(id);
                int copies = 0;
                for (Map.Entry pair : dupli.entrySet()) {
                    if ((Integer) pair.getValue() != 1) {
                        copies++;
                    }
                }

                HashMap<String, Integer> top5 = findTop5Dublicates(dupli);

                writer.println(id + "\t" + copies);

                for (Map.Entry pair : top5.entrySet()) {
                    writer.println(pair.getKey() + "\t" + pair.getValue());
                }

            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        writer.close();

    }

    private HashMap<String, Integer> findTop5Dublicates(HashMap<String, Integer> dupli) {
        HashMap<String, Integer> top = new HashMap<>();

        for (int i = 0; i < 5; i++) {

            int max = -10;
            String maxKey = null;
            for (Map.Entry pair : dupli.entrySet()) {
                if ((Integer) pair.getValue() > max) {
                    maxKey = pair.getKey().toString();
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
        BasicDBObject tweetsQuery = new BasicDBObject(); //Here we count the original tweets user did.
        tweetsQuery.put("user_id", user.getId().toString());
        BasicDBObject retweetsQuery = new BasicDBObject(); //Here we get the tweets the user retweeted.
        retweetsQuery.put("retweetedUserID", user.getId().toString());

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

    private double calcEntropy(Long id) throws JSONException {
        user = new TwitterUser(id); //create user for given id

        //======== CONSTRUCT QUERIES =============
        BasicDBObject tweetsQuery = new BasicDBObject(); //Here we count the original tweets user did.
        tweetsQuery.put("user_id", user.getId().toString());
        BasicDBObject retweetsQuery = new BasicDBObject(); //Here we get the tweets the user retweeted.
        retweetsQuery.put("retweetedUserID", user.getId().toString());

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
        return user.entropy();
    }

    /**
     * Prints the header of the output file.
     */
    private void printHeader() {
        writer.println("id followers followees, fol_fol_ratio age tweets retweets "
                + "rt_t_ratio fav verified hashtags avgHT percentageHT mentions "
                + "avgMentions percentageMentions urls avgUrls percentageUrls urlRatio "
                + "source urlDomainRatio timeFollowed defaultProf defaultProfImg compressionRatio entropy");
    }

    /**
     * Prints all information for current finished user.
     */
    private void printUser() {
        writer.printf("%d %d %d %f %d %d %d %f %d %d %d %f %f %d %f "
                + "%f %d %f %f %f %s %f %d %d %d %f %f\n",
                user.getId(), user.getFollowers(), user.getFollowees(), user.getFol_fol_ratio(),
                user.getAge(), user.getTweets(), user.getRetweets(), user.getRetweetsTweetsRatio(),
                user.getFavorites(), user.isVerified() ? 1 : 0, user.getHashtags(), user.getAvgHTperTweet(),
                user.getPercentageTweetsWithHT(), user.getMentions(), user.getAvgMentionsPerTweet(),
                user.getPercentageTweetsWithMentions(), user.getUrls(), user.getAvgURLsPerTweet(),
                user.getPercentageTweetsWithURL(), user.getUrlRatio(), user.getMostFrequentSource(),
                user.getUrlDomainRatio(), user.getTimeFollowed(), user.isDefProf() ? 1 : 0,
                user.isDefProfImg() ? 1 : 0, user.getCompressionRatio(), user.getEntropy());
    }

}
