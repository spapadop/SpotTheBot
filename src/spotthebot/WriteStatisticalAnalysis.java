/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spotthebot;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Sokratis
 */
public class WriteStatisticalAnalysis {
    
    private static MongoDBHandler mongo;
    private static ArrayList<TwitterUser> users;
    
    public static void main(String[] args) throws UnknownHostException, FileNotFoundException, UnsupportedEncodingException, ParseException {
        
        users = new ArrayList<>();
        PrintWriter writer = new PrintWriter("results.txt", "UTF-8"); 
        mongo = new MongoDBHandler();
//        int counter = 1;
        DBCursor cursor = mongo.getFollowedUsersColl().find(); //get all users of random collection
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss Z YYYY");
        
//        System.out.println("we have a total of " + mongo.getFollowedUsersColl().count() + " users.");
        while (cursor.hasNext()) { //for every user in random collection database
            
            DBObject muser = cursor.next(); //get one record
//            System.out.println(counter + ". working on: " + muser.get("id_str"));
//            counter++;
            
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
            
//            System.out.println(user.getId() + " ------------");
//            System.out.println("tweets:" + user.getTweets());
//            System.out.println("retweets: " + user.getRetweets());
//            DecimalFormat df = new DecimalFormat("#.##");
//            System.out.println("ratio: " + df.format(user.getRetweetsTweetsRatio()));
//            System.out.println("retweets received: " + user.getRetweetsReceived());
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
