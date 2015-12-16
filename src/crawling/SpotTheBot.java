package crawling;

import analysis.FeaturesAnalysis;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import twitter4j.JSONException;
import twitter4j.User;

/**
 * This project is aiming to follow highly retweeted users activity in Twitter.
 * It uses Twitter Streaming API, in order to analyze tweets and get those
 * highly retweeted users. Then, it follows their activity and analyze their
 * behavior. Database will be useful for further analysis.
 *
 * @author Sokratis Papadopoulos
 */
public class SpotTheBot {

    private static final int TASK_REPEAT = 3600000; // one hour: 3600000 | time-repeat of checking task for updating list of potential spammers | 60.000 milliseconds = 1 minute

    public static void main(String[] args) throws JSONException, MongoException, UnknownHostException, FileNotFoundException, IOException, UnsupportedEncodingException, URISyntaxException, ParseException {

//        Crawler crawl = new Crawler(); //starts the crawling
//
//        //create task to be executed every 1 hour
//        Timer time = new Timer();
//        time.schedule(crawl, 0, TASK_REPEAT);

        
//        MongoDBHandler mongo = new MongoDBHandler();
//        PrintWriter writer = new PrintWriter("whitelist.txt", "UTF8");
//        HashSet<Long> black_list = new HashSet<>();
//        HashSet<Long> white_sample_list = new HashSet<>();
//        File file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\outliers\\raw12\\black_list.txt");
//        BufferedReader reader = null;
//        
//        try {
//            reader = new BufferedReader(new FileReader(file));
//            String text= reader.readLine();
//            while ((text = reader.readLine()) != null) {
//                black_list.add(Long.parseLong(text));
//            }
//        } finally {
//            try {
//                if (reader != null) {
//                    reader.close();
//                }
//            } catch (IOException e) {
//            }
//        }
//
//        int total = (int) mongo.getUsersColl().count();
//        long count = 0;
//        long numSamples = 1097;
//        Random rand = new Random();
//
//        while (count < numSamples) {
//            //System.out.println("vamos " + count);
//            int random = rand.nextInt(total);
//            DBObject u = mongo.getUsersColl().find().skip(random).limit(1).next();
//            long id = Long.parseLong(u.get("id_str").toString());
//            if (!black_list.contains(id)) {
//                count++;
//                System.out.println(id);
//                white_sample_list.add(id);
//            }
//        }
//        
//        white_sample_list.stream().forEach((id) -> {
//            writer.println(id);
//        });
//
//        writer.close();
    }

}
