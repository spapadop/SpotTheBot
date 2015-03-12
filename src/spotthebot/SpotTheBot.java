package spotthebot;

import java.util.Timer;
import twitter4j.JSONException;

/**
 *
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
        time.schedule(crawl, 0, 8000); // Create Repetitively task for every 1 minute

    }
}
