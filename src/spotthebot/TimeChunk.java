/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package spotthebot;

/**
 * The data that corresponds to a user at a specific time chunk (1 hour).
 * @author Sokratis Papadopoulos
 */
public class TimeChunk {
    //private int chunkID;
    private int retweets;
    private int retweetsReceived;
    
    public TimeChunk(){
        retweets=0;
        retweetsReceived=0;
    }

    public void increaseRetweets(){
        retweets++;
    }
    
    public void increaseRetweetsReceived(){
        retweetsReceived++;
    }
    
    public int getRetweets() {
        return retweets;
    }

    public void setRetweets(int retweets) {
        this.retweets = retweets;
    }

    public int getRetweetsReceived() {
        return retweetsReceived;
    }

    public void setRetweetsReceived(int retweetsReceived) {
        this.retweetsReceived = retweetsReceived;
    }
    
}
