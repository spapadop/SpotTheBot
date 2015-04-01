package spotthebot;

import java.util.Timer;
import twitter4j.JSONException;

/**
 * This project is aiming to follow highly retweeted users in Twitter.
 * It uses Twitter Streaming API, in order to analyze tweets and get those
 * highly retweeted users. Then, it follows their activity and analyze
 * their behavior. Database will be useful for further analysis.
 * @author Sokratis Papadopoulos
 */
public class SpotTheBot {

    /**
     * @param args the command line arguments
     * @throws twitter4j.JSONException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws JSONException, InterruptedException {
        Crawler crawl = new Crawler();

        Timer time = new Timer(); // Instantiate Timer Object
        time.schedule(crawl, 0, 3600000); // Create Repetitively task for every 1 hour

    }
}
