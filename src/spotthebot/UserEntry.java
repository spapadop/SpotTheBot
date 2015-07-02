package spotthebot;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sokratis
 */
public class UserEntry {
    private int retweets;
    private int minRtPerHour;
    private int maxRtPerHour;
    private float avgRtPerHour;
    private Date firstRetweet;
    private Date lastRetweet;
    
    private int RTreceived;
    private int minRTrecPerHour;
    private int maxRTrecPerHour;
    private float avgRTrecPerHour;
    private Date firstRTreceived;
    private Date lastRTreceived;
    
    public UserEntry(){
        retweets = 0;
        minRtPerHour = Integer.MAX_VALUE;
        maxRtPerHour = 0;
        avgRtPerHour = 0;
        firstRetweet = null;
        lastRetweet = null;
        
        RTreceived = 0;
        minRTrecPerHour = Integer.MAX_VALUE;
        maxRTrecPerHour = 0;
        avgRTrecPerHour = 0;
        firstRTreceived = null;
        lastRTreceived = null;
    }
    
    public UserEntry(Date when, boolean did){
        retweets = 0;
        minRtPerHour = Integer.MAX_VALUE;
        maxRtPerHour = 0;
        avgRtPerHour = 0;
        firstRetweet = null;
        lastRetweet = null;
        
        RTreceived = 0;
        minRTrecPerHour = Integer.MAX_VALUE;
        maxRTrecPerHour = 0;
        avgRTrecPerHour = 0;
        firstRTreceived = null;
        lastRTreceived = null;
        
        newTweet(when,did);
    }
    
    public void newTweet(Date when, boolean did){
        if(did){ //retweet done
            newRetweetDone(when);
        } else { //retweeet received
            newRetweetReceived(when);
        }
    }
    
    private void newRetweetDone(Date when){
        retweets++;
        if(firstRetweet == null){ // first retweet done observed
            firstRetweet = when;
            avgRtPerHour = 1; // we use the avg as counter for current hour each time
        } else {
            if ((getDateDiff(lastRetweet, when, TimeUnit.HOURS) > 0)||(when.getHours() != lastRetweet.getHours())){ //next hour
                checkRtMinMax();
                avgRtPerHour = 1; //start over the counter

            } else { //if we are still in same hour, increase the counter
                avgRtPerHour++;
            }
        }
        lastRetweet = when;
    }
    
    private void newRetweetReceived(Date when){
        RTreceived++;
        if(firstRTreceived == null){ // first retweet received observed
            firstRTreceived = when;
            avgRTrecPerHour = 1; // we use the avg as counter for current hour each time
        } else {
            if ((getDateDiff(lastRTreceived, when, TimeUnit.HOURS) > 0)||(when.getHours() != lastRTreceived.getHours())){ //next hour
                checkRTrecMinMax();
                avgRTrecPerHour = 1; //start over the counter

            } else {
                avgRTrecPerHour++; //if we are still in same hour, increase the counter
            }
        }
        lastRTreceived = when;
    }
    
    private void checkRtMinMax(){
        if (avgRtPerHour < minRtPerHour) //check if its min
            minRtPerHour = (int) avgRtPerHour;

        if (avgRtPerHour > maxRtPerHour) //check if its max
            maxRtPerHour = (int) avgRtPerHour;
    }
    
    private void checkRTrecMinMax(){
        if (avgRTrecPerHour < minRTrecPerHour) //check if its min
            minRTrecPerHour = (int) avgRTrecPerHour;

        if (avgRTrecPerHour > maxRTrecPerHour) //check if its max
            maxRTrecPerHour = (int) avgRTrecPerHour;
    }
    
    
    public void finish(){
        checkRtMinMax();
        checkRTrecMinMax();
        if(lastRetweet!=null && firstRetweet!=null && getDateDiff(firstRetweet, lastRetweet, TimeUnit.HOURS)!=0)
            avgRtPerHour = (float) retweets / getDateDiff(firstRetweet, lastRetweet, TimeUnit.HOURS);
        else
            avgRtPerHour =0;
        
        if(firstRTreceived!=null && lastRTreceived!=null && getDateDiff(firstRTreceived, lastRTreceived, TimeUnit.HOURS)!=0)
            avgRTrecPerHour = (float) RTreceived / getDateDiff(firstRTreceived, lastRTreceived, TimeUnit.HOURS);
        else
            avgRTrecPerHour =0;
    }
    
    
    /**
     * Computes the time difference between two dates.
     * 
     * @param date1
     * @param date2
     * @param timeUnit
     * @return the time difference between dates
     */
    public static long getDateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }   

    public int getRetweets() {
        return retweets;
    }

    public void setRetweets(int retweets) {
        this.retweets = retweets;
    }

    public int getMinRtPerHour() {
        return minRtPerHour;
    }

    public void setMinRtPerHour(int minRtPerHour) {
        this.minRtPerHour = minRtPerHour;
    }

    public int getMaxRtPerHour() {
        return maxRtPerHour;
    }

    public void setMaxRtPerHour(int maxRtPerHour) {
        this.maxRtPerHour = maxRtPerHour;
    }

    public float getAvgRtPerHour() {
        return avgRtPerHour;
    }

    public void setAvgRtPerHour(int avgRtPerHour) {
        this.avgRtPerHour = avgRtPerHour;
    }

    public int getRTreceived() {
        return RTreceived;
    }

    public void setRTreceived(int RTreceived) {
        this.RTreceived = RTreceived;
    }

    public int getMinRTrecPerHour() {
        return minRTrecPerHour;
    }

    public void setMinRTrecPerHour(int minRTrecPerHour) {
        this.minRTrecPerHour = minRTrecPerHour;
    }

    public int getMaxRTrecPerHour() {
        return maxRTrecPerHour;
    }

    public void setMaxRTrecPerHour(int maxRTrecPerHour) {
        this.maxRTrecPerHour = maxRTrecPerHour;
    }

    public float getAvgRTrecPerHour() {
        return avgRTrecPerHour;
    }

    public void setAvgRTrecPerHour(int avgRTrecPerHour) {
        this.avgRTrecPerHour = avgRTrecPerHour;
    }
    
}
