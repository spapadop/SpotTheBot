/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spotthebot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.User;

/**
 *
 * @author Sokratis
 */
public abstract class TwitterUser implements User {
    private String id; //stores the userID
    private int tweets; // number of user's tweets  
    private int retweets; // number of user's retweets
    private int timesRTed; //number of times user's tweets were RTed
    private String accountAge; // how old the account is
    private int followers; // number of user's followers
    private int followees; // number of user's followees
    private double ratio; // ratio of followers/followees
    private boolean isVerified; // is the account verified or not


    public TwitterUser(){
        this.id = "";
        this.tweets = 0;
        this.retweets = 0;
        this.timesRTed = 0;
        this.followers = 0;
        this.followees = 0;
        this.ratio = 0;
        this.isVerified = false;
    }
    
    public TwitterUser (String userID){
        this.id = userID;
        this.tweets = 0;
        this.followers = 0;
        this.followees = 0;
        this.ratio = 0;
        this.isVerified = false;
    }
    
    public TwitterUser (String userID, int tweets){
        this.id = userID;
        this.tweets = tweets;
        this.followers = 0;
        this.followees = 0;
        this.ratio = 0;
        this.isVerified = false;
    }
    
    public TwitterUser (String userID, int followers, int followees, String accountAge){
        this.id = userID;
        this.tweets = 0;
        this.followers = followers;
        this.followees = followees;
        this.accountAge = accountAge;
        ratio = (double) followers/followees;
        this.isVerified = false;
    }
    
    public TwitterUser (String userID, int tweets, int followers, int followees, String accountAge){
        this.id = userID;
        this.tweets = tweets;
        this.followers = followers;
        this.followees = followees;
        this.accountAge = accountAge;
        ratio = (double) followers/followees;
        this.isVerified = false;
    }
    
    public TwitterUser (String userID, int tweets, int followers, int followees, String accountAge, boolean isVerified){
        this.id = userID;
        this.tweets = tweets;
        this.followers = followers;
        this.followees = followees;
        this.accountAge = accountAge;
        ratio = (double) followers/followees;
        this.isVerified = isVerified;
    }

    /**
     * 
     * @param createdAge 
     */
    public void setAccountAge(String createdAge) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date =new Date();
        setAccountAge(createdAge,dateFormat.format(date).toString());
    }

    /**
     * 
     * @param createdAge
     * @param currentDate 
     */
    public void setAccountAge(String createdAge,String currentDate){
       SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
       
       Date d1;
       Date d2;
        try {
            d1 = format.parse(createdAge);
            d2 = format.parse(currentDate);
            
            long diff = d2.getTime() - d1.getTime();
            
            long diffSeconds = diff / 1000 % 60;
            long diffMinutes = diff / (60 * 1000) % 60;
            long diffHours = diff / (60 * 60 * 1000) % 24;
            long diffDays = diff / (24 * 60 * 60 * 1000);
            
            StringBuilder sb = new StringBuilder();
            sb.append(diffDays).append("days, ").append(diffHours).append("hours, ").append(diffMinutes).append("minutes, ").append(diffSeconds).append("seconds.");
            
            this.accountAge = sb.toString();
        } catch (ParseException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    public String getUserID() {
        return id;
    }

    public void setUserID(String userID) {
        this.id = userID;
    }

    public double getRatio() {
        ratio = (double) followers/followees; //just in case it weren't calculated yet
        return ratio;
    }

    public String getAccountAge() {
        return accountAge;
    }    
    
    public void increaseRetweets(){
        retweets++;
    }
    
    public void increaseTweets(){
        tweets++;
    }
}
