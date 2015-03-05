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
        time.schedule(crawl, 0, 3000); // Create Repetitively task for every 1 minute

//        for (int i = 0; i <= 5; i++) {
//                System.out.println("Execution in Main Thread...." + i);
//
//                Thread.sleep(2000);
//                if (i == 5) {
//                        System.out.println("Application Terminates");
//                        System.exit(0);
//                }
//        }
        //RetweetObserver observe = new RetweetObserver();
        //UserTracker track = new UserTracker(observe.getHighlyRTed());
    }
}
