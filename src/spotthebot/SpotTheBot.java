package spotthebot;

import java.util.Timer;
import twitter4j.JSONException;

/**
 * This project is aiming to follow highly retweeted users activity in Twitter.
 * It uses Twitter Streaming API, in order to analyze tweets and get those
 * highly retweeted users. Then, it follows their activity and analyze
 * their behavior. Database will be useful for further analysis.
 * @author Sokratis Papadopoulos
 */
public class SpotTheBot {
    
    public static void main(String[] args) throws JSONException, InterruptedException {
        
        Crawler crawl = new Crawler(); 

        //create task to be executed every 10 minutes
        Timer time = new Timer(); 
        time.schedule(crawl, 0, 600000); //600.000 milliseconds = 10 minutes

    }
}
