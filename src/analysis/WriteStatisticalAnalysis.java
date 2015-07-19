/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package analysis;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import crawling.MongoDBHandler;
import crawling.TwitterUser;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 *
 * @author Sokratis Papadopoulos
 */
public class WriteStatisticalAnalysis {
    
    private static MongoDBHandler mongo;
    private static ArrayList<TwitterUser> users;
//    private static Integer randomUsers;
    
    public static void main(String[] args) throws UnknownHostException, FileNotFoundException, UnsupportedEncodingException, ParseException {
   
        users = new ArrayList<>();
        mongo = new MongoDBHandler(); 
        
        collectStatsForRandomUsers();
        //collectStatsForFollowedUsers();
        
    }
    
    private static void collectStatsForRandomUsers() throws FileNotFoundException, UnsupportedEncodingException, ParseException{
//        randomUsers = 10000;
//        Random rng = new Random(); // Ideally just create one instance globally
//        Set<Integer> generated = new HashSet<Integer>();
//        while (generated.size() < randomUsers)
//        {
//            Integer next = rng.nextInt(2700000) + 1;
//            // As we're adding to a set, this will automatically do a containment check
//            generated.add(next);
//        }
        
        PrintWriter writer = new PrintWriter("resultsRandom.txt", "UTF-8"); 
        DBCursor cursor = mongo.getUsersColl().find(); //get all users of random collection
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss Z YYYY");
        cursor.skip(2701900);
        while (cursor.hasNext()) { //for every user in random collection database
            DBObject muser = cursor.next(); //get one record            
            TwitterUser user = new TwitterUser((Long.parseLong(muser.get("id_str").toString())));  
            
            // ======= WORKING ON TWEETS MADE BY USER ==========
            
            BasicDBObject tweetsQuery = new BasicDBObject(); //make a query to get original tweets of user
            tweetsQuery.put("user_id", muser.get("id_str"));
            
            user.setTweets(mongo.getTweetsColl().count(tweetsQuery)); //store number of original tweets of user
            
            DBCursor tweets = mongo.getTweetsColl().find(tweetsQuery);
            while (tweets.hasNext()) { //for every tweet
                DBObject tweet = tweets.next(); 
                user.addTweet(Long.parseLong(tweet.get("id_str").toString()), format.parse(tweet.get("created_at").toString())); 
            }
            
            // ======= WORKING ON RETWEETS THE USER DID ============
            
            BasicDBObject retweetsQuery = new BasicDBObject(); //make a query to count retweets of user
            retweetsQuery.put("retweetedUserID", muser.get("id_str"));
            user.setRetweets(mongo.getRetweetsColl().count(retweetsQuery)); //store number of retweets the user did
            
            DBCursor retweets = mongo.getRetweetsColl().find(retweetsQuery);
            while (retweets.hasNext()) { //for every retweet user did
                DBObject retweet = retweets.next(); 
                user.addRetweet(Long.parseLong(retweet.get("originalTweetID").toString()), format.parse(retweet.get("created_at").toString()));
            }
            
            // ========== WORKING ON RETWEETS THE USER HAS RECEIVED ==========
            
            BasicDBObject rtReceivedQuery = new BasicDBObject();
            rtReceivedQuery.put("originalUserID", muser.get("id_str"));
            user.setRetweetsReceived(mongo.getRetweetsColl().count(rtReceivedQuery));
            
            DBCursor rtReceived = mongo.getRetweetsColl().find(rtReceivedQuery);
            while (rtReceived.hasNext()) { //for every retweet user received
                DBObject rec = rtReceived.next(); 
                user.addRetweetReceived(Long.parseLong(rec.get("originalTweetID").toString()), format.parse(rec.get("created_at").toString()));
            }
                     
            users.add(user);  
        }     
        
        
//        for(TwitterUser u : users){
//            System.out.println("writing user...");
//            writer.println("------------ " + u.getId() + " ------------");
//            writer.println("tweets:" + u.getTweets());
//            writer.println("retweets: " + u.getRetweets());
//            writer.println("ratio: " + u.getRetweetsTweetsRatio());
//            writer.println("retweets received: " + u.getRetweetsReceived());
//            writer.println("average tweets per hour:" + u.getAvgTweetsPerHour());
//            writer.println("average retweets per hour:" + u.getAvgRetweetsPerHour());
//            writer.println("average retweets received per hour:" + u.getAvgRtReceivedPerHour());
//            writer.println("hours followed: " + u.getTimeFollowed());
//            writer.println("-------------------------------------------");
//            writer.println();
//        }
        
        
        for(TwitterUser u : users){
            writer.printf("%d %d %d %.2f %.2f %d %.2f %.2f %.2f %d %n", u.getId(), u.getTweets(), u.getRetweets(), u.getRetweetsTweetsRatio(),u.getTweetsRetweeted(),u.getRetweetsReceived(),u.getAvgTweetsPerHour(),u.getAvgRetweetsPerHour(),u.getAvgRtReceivedPerHour(),u.getTimeFollowed());
//            writer.println(u.getId() 
//                        + " " + u.getTweets()
//                        + " " + u.getRetweets()
//                        + " " + u.getRetweetsTweetsRatio() 
//                        + " " + u.getTweetsRetweeted()
//                        + " " + u.getRetweetsReceived() 
//                        + " " + u.getAvgTweetsPerHour() 
//                        + " " + u.getAvgRetweetsPerHour()
//                        + " " + u.getAvgRtReceivedPerHour()
//                        + " " + u.getTimeFollowed());
            writer.flush();
            //writer.println();
        }
        writer.close();
    
        
        
        
        
    }
    
    private static void collectStatsForFollowedUsers() throws FileNotFoundException, UnsupportedEncodingException, ParseException{
        PrintWriter writer = new PrintWriter("resultsFollowed.txt", "UTF-8"); 
        DBCursor cursor = mongo.getFollowedUsersColl().find(); //get all users of random collection
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss Z YYYY");
        
        while (cursor.hasNext()) { //for every user in random collection database
            
            DBObject muser = cursor.next(); //get one record            
            TwitterUser user = new TwitterUser((Long.parseLong(muser.get("id_str").toString())));  
            
            // ======= WORKING ON TWEETS MADE BY USER ==========
            
            BasicDBObject tweetsQuery = new BasicDBObject(); //make a query to get original tweets of user
            tweetsQuery.put("user_id", muser.get("id_str"));
            
            user.setTweets(mongo.getTweetsColl().count(tweetsQuery)); //store number of original tweets of user
            
            DBCursor tweets = mongo.getTweetsColl().find(tweetsQuery);
            while (tweets.hasNext()) { //for every tweet
                DBObject tweet = tweets.next(); 
                user.addTweet(Long.parseLong(tweet.get("id_str").toString()), format.parse(tweet.get("created_at").toString())); 
            }
            
            // ======= WORKING ON RETWEETS THE USER DID ============
            
            BasicDBObject retweetsQuery = new BasicDBObject(); //make a query to count retweets of user
            retweetsQuery.put("retweetedUserID", muser.get("id_str"));
            user.setRetweets(mongo.getRetweetsColl().count(retweetsQuery)); //store number of retweets the user did
            
            DBCursor retweets = mongo.getRetweetsColl().find(retweetsQuery);
            while (retweets.hasNext()) { //for every retweet user did
                DBObject retweet = retweets.next(); 
                user.addRetweet(Long.parseLong(retweet.get("originalTweetID").toString()), format.parse(retweet.get("created_at").toString()));
            }
            
            // ========== WORKING ON RETWEETS THE USER HAS RECEIVED ==========
            
            BasicDBObject rtReceivedQuery = new BasicDBObject();
            rtReceivedQuery.put("originalUserID", muser.get("id_str"));
            user.setRetweetsReceived(mongo.getRetweetsColl().count(rtReceivedQuery));
            
            DBCursor rtReceived = mongo.getRetweetsColl().find(rtReceivedQuery);
            while (rtReceived.hasNext()) { //for every retweet user received
                DBObject rec = rtReceived.next(); 
                user.addRetweetReceived(Long.parseLong(rec.get("originalTweetID").toString()), format.parse(rec.get("created_at").toString()));
            }
            
            user.finished(mongo.getTimeUserAppeared(user.getId()), mongo.getTimeUserDisappeared(user.getId())); 
                        
            users.add(user);            
        }     
        
        
//        for(TwitterUser u : users){
//            System.out.println("writing user...");
//            writer.println("------------ " + u.getId() + " ------------");
//            writer.println("tweets:" + u.getTweets());
//            writer.println("retweets: " + u.getRetweets());
//            writer.println("ratio: " + u.getRetweetsTweetsRatio());
//            writer.println("retweets received: " + u.getRetweetsReceived());
//            writer.println("average tweets per hour:" + u.getAvgTweetsPerHour());
//            writer.println("average retweets per hour:" + u.getAvgRetweetsPerHour());
//            writer.println("average retweets received per hour:" + u.getAvgRtReceivedPerHour());
//            writer.println("hours followed: " + u.getTimeFollowed());
//            writer.println("-------------------------------------------");
//            writer.println();
//        }
        
        
        for(TwitterUser u : users){
            writer.printf("%d %d %d %.2f %.2f %d %.2f %.2f %.2f %d %n", u.getId(), u.getTweets(), u.getRetweets(), u.getRetweetsTweetsRatio(),u.getTweetsRetweeted(),u.getRetweetsReceived(),u.getAvgTweetsPerHour(),u.getAvgRetweetsPerHour(),u.getAvgRtReceivedPerHour(),u.getTimeFollowed());
//            writer.println(u.getId() 
//                        + " " + u.getTweets()
//                        + " " + u.getRetweets()
//                        + " " + u.getRetweetsTweetsRatio() 
//                        + " " + u.getTweetsRetweeted()
//                        + " " + u.getRetweetsReceived() 
//                        + " " + u.getAvgTweetsPerHour() 
//                        + " " + u.getAvgRetweetsPerHour()
//                        + " " + u.getAvgRtReceivedPerHour()
//                        + " " + u.getTimeFollowed());
            writer.flush();
            //writer.println();
        }
        writer.close();
    }
        
}
