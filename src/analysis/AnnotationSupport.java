package analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitter4j.JSONException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.api.UsersResources;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Class is used in order to provide some details needed for the creation of annotation.
 * 
 * @author Sokratis Papadopoulos
 */
public class AnnotationSupport extends TimerTask{
    
    public Twitter twitter;
    public UsersResources temp;
    // variables for handling twitter Streaming API
    private Configuration config;
    private Long[] users;
    
    public int pos=0;
    
    private final ArrayList<User> detailedUsers;
    
    private PrintWriter writeDeleted;
    
    public AnnotationSupport() throws TwitterException, IOException{
        configuration();
        usersList();
        detailedUsers = new ArrayList<>();
        writeDeleted = new PrintWriter("C:\\Users\\sokpa\\Desktop\\run2_deleted.txt", "UTF-8");
    }
    
    private void configuration(){
        //CONNECT TO TWITTER API
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API
        config = cb.build();
        twitter = new TwitterFactory(config).getInstance();
    }
    
    public final void usersList() throws FileNotFoundException, IOException{
        users = new Long[1000];
        int i=0;
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\randomSample\\run2\\sample.txt"));
        String line= reader.readLine(); //header
        
        line = reader.readLine();//body
        while (line != null) {
            String[] tokens = line.split("\t");
            users[i++] = Long.parseLong(tokens[0]);
            line = reader.readLine();
        }
        reader.close();
    }
    
    
    @Override
    public void run() {
        
        for(int i=pos; i<pos+100; i++){ //+100
            try {
                detailedUsers.add(twitter.showUser(users[i]));
            } catch (TwitterException ex) {
                if (ex.getErrorCode()!=88){
                    
                    writeDeleted.println(users[i]);
                    System.out.println(users[i]);
                    System.out.println(ex.getErrorCode());
                    
                }
            }
        }
        
        pos+=100; //+100
        if(pos==1000){
            
            try {
                writeDeleted.close();
                AnnotationUser usersAnn = new AnnotationUser(detailedUsers);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AnnotationSupport.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(AnnotationSupport.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(AnnotationSupport.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException | URISyntaxException ex) {
                Logger.getLogger(AnnotationSupport.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            System.exit(0);

        }
    }
}
