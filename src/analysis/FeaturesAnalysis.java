package analysis;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import crawling.MongoDBHandler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import twitter4j.JSONException;

/**
 * Runs the features analysis based on our data collected. Calculates 
 * both content-based and user-based feature values.
 * 
 * @author Sokratis Papadopoulos
 */
public class FeaturesAnalysis {
    MongoDBHandler mongo;
    PrintWriter writer;
    TwitterUser user;
    
    public FeaturesAnalysis() throws MongoException, UnknownHostException, FileNotFoundException, UnsupportedEncodingException, ParseException, URISyntaxException, JSONException, IOException{
        mongo = new MongoDBHandler();
        writer = new PrintWriter("featureAnalysisPerUser.txt", "UTF-8");
        
        DBCursor cursor = mongo.getUsersColl().find(); // ALL random users
        
        printHeader();
        while (cursor.hasNext()) { 
            
            DBObject muser = cursor.next();    
            user = new TwitterUser((Long.parseLong(muser.get("id_str").toString())));
            user.setVerified(Boolean.getBoolean(muser.get("verified").toString()));
            user.setFollowers(Integer.parseInt(muser.get("followers_count").toString()));
            user.setFollowees(Integer.parseInt(muser.get("friends_count").toString()));
            user.setFavorites(Integer.parseInt(muser.get("favourites_count").toString()));
            user.setAge(muser.get("created_at").toString());
            user.setDefProf(Boolean.getBoolean(muser.get("default_profile").toString()));
            user.setDefProfImg(Boolean.getBoolean(muser.get("default_profile_image").toString()));
            try{
                user.setDescription(muser.get("description").toString());
            } catch (java.lang.NullPointerException ex){
                //System.out.println("description not found.");
            }
            
            //======== CONSTRUCT QUERIES =============
            BasicDBObject tweetsQuery = new BasicDBObject(); 
            tweetsQuery.put("user_id", user.getId().toString());
            BasicDBObject retweetsQuery = new BasicDBObject(); 
            retweetsQuery.put("retweetedUserID", user.getId().toString());
            BasicDBObject followedQuery = new BasicDBObject();
            followedQuery.put("id_str", user.getId().toString());

            // =========== USER DETAILS ==============
            user.checkUserDetails();
            
            // ========= (RE)TWEETS DETAILS ===========
            user.setTweets(mongo.getTweetsColl().count(tweetsQuery)); 
            user.setRetweets(mongo.getRetweetsColl().count(retweetsQuery)); //store number of retweets the user did
            DBCursor tweets = mongo.getTweetsColl().find(tweetsQuery);
            while (tweets.hasNext()) { 
                DBObject status = tweets.next();
                user.addStatus(status);
                user.addTweet(status.get("text").toString());
            }
            user.checkTweetsDetails();

            // ========== TIME FOLLOWED ==============
            DBObject rec = mongo.getFollowedUsersColl().findOne(followedQuery);
            user.setTimeFollowed(rec);
            
            //user.sout();
            printUser();

        }
        
        writer.close();
    }
    
    /**
     * Prints the header of the output file.
     */
    private void printHeader(){
        writer.println("id followers followees, fol_fol_ratio age tweets retweets "
                + "rt_t_ratio fav verified hashtags avgHT percentageHT mentions "
                + "avgMentions percentageMentions urls avgUrls percentageUrls urlRatio "
                + "source urlDomainRatio timeFollowed defaultProf defaultProfImg compressionRatio");
    }
    
    /**
     * Prints all information for current finished user.
     */
    private void printUser(){ 
        writer.printf("%d %d %d %f %d %d %d %f %d %d %d %f %f %d %f "
                + "%f %d %f %f %f %s %f %d %d %d %f\n", 
                user.getId(), user.getFollowers(), user.getFollowees(), user.getFol_fol_ratio(),
                user.getAge(), user.getTweets(), user.getRetweets(), user.getRetweetsTweetsRatio(),
                user.getFavorites(), user.isVerified(), user.getHashtags(), user.getAvgHTperTweet(), 
                user.getPercentageTweetsWithHT(), user.getMentions(), user.getAvgMentionsPerTweet(),
                user.getPercentageTweetsWithMentions(), user.getUrls(), user.getAvgURLsPerTweet(),
                user.getPercentageTweetsWithURL(), user.getUrlRatio(), user.getMostFrequentSource(),
                user.getUrlDomainRatio(), user.getTimeFollowed(), user.isDefProf(),
                user.isDefProfImg(), user.getCompressionRatio());
    }
    
}
