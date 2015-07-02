package spotthebot;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import twitter4j.ConnectionLifeCycleListener;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

/**
 * Crawling the activity of specific followed users list.
 * The list is being updated at specific times.
 * 
 * @author Sokratis Papadopoulos
 */
public class UserTracker {
    
    //variable handling mongo
    private MongoDBHandler mongo;
    
    // variables for handling twitter Streaming API
    private TwitterStream stream;
    private StatusListener listener;
    private Configuration config;
    private FilterQuery fq;

    private List<String> suspicious;
    
    /**
     * Basic constructor of a User Tracker. 
     * Doesnt provide suspicious users list.
     * 
     */
    public UserTracker() {
        this.suspicious = null;
        mongo = null;
        fq = new FilterQuery();
        configuration();
        startListener();
    }
    
    /**
     * Creating a thread for running the tracking.
     * 
     * @param suspicious
     * @param mongo
     */
    public UserTracker(List<String> suspicious, MongoDBHandler mongo) {
        this.suspicious = suspicious;
        this.mongo = mongo;
        fq = new FilterQuery();
        configuration();
        addUsersToFollowedUsers();
        startListener();
    }

    /**
     * The configuration details of our app as developer mode of Twitter APIs.
     * 
     */
    private void configuration() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey("0cc8fkRgUfzX5fYK14m211vhE");
        cb.setOAuthConsumerSecret("45d3sLIiEG0suWxEGBECTWP0tXJL6hJQwqqNCvo04eeGKjL8Al");
        cb.setOAuthAccessToken("43403340-aUeWfSgfYpYSDmoeVzaPXF1aaiBAo3IL7zgIXwahU");
        cb.setOAuthAccessTokenSecret("Tc40irSU8G15IvvEu6EuVjsaM1xQAVCDzJoaSTnxYVFOI");
        cb.setJSONStoreEnabled(true); //We use this as we pull json files from Twitter Streaming API
        config = cb.build();
        
        //stream = new TwitterStreamFactory(config).getInstance();
    }
    
    /**
     * Implements the adding of users to the suspicious list. 
     * It makes the appropriate updates or actions for every user.
     * 
     */
    private void addUsersToFollowedUsers(){
        //System.out.println("addUsersToFollowedUsers | adding users to followed list in mongo");
 
        Date now = new Date();
        for (String id : suspicious) { //for every user that is suspicious
            
            Date finishTime=new Date(now.getTime() + (60 * 60000)); //finish time is set as current + 1hour
            if (!mongo.findFollowedUser(id)){ //new user
                //System.out.println("TIME TO ADD NEW USER TO FOLLOWED!");
                BasicDBObject user = new BasicDBObject();
                user.put("id_str", id); //save user's id
                //user.put("starting_time", now);   
                //user.put("finish_time", finishTime);  
                //mongo.addObjectToFollowedUsers(user);
                //System.out.println("==Inserted to mongo: " + id + " " + now + " --> " + finishTime);
                       
                List<BasicDBObject> times = new ArrayList<>();
                BasicDBObject time = new BasicDBObject();
                time.put("starting_time", now);   
                time.put("finish_time", finishTime); 
                times.add(time);
                user.put("following_periods", times);
                mongo.addObjectToFollowedUsers(user);
                
                
            } else {//user exists -> update finish time 
                //System.out.println("TIME TO [UPDATE] A FOLLOWED USER!");
                //BasicDBObject updated = new BasicDBObject().append("$set", new BasicDBObject().append("finish_time", finishTime)); //finish_time now + next check!
                //mongo.updateFinishTime(id, updated); 
                //System.out.println(id + "==Old user updated finish time.");   
                
                BasicDBObject time = new BasicDBObject();
                time.put("starting_time", now);   
                time.put("finish_time", finishTime); 

                BasicDBObject update = new BasicDBObject();
                update.put("$push", new BasicDBObject("following_periods",time));

                mongo.appendFinishTime(id, update);
            }
        }
    }
    
    /**
     * Shutdowns the previous query to API, updates the following list and
     * starts over the query with the renewed list.
     * 
     * @param newcomers 
     */
    public void update (List<String> newcomers){
        //System.out.println("(update) the suspicious list in mongoDB");
        stopStreaming();
        suspicious = newcomers;
        addUsersToFollowedUsers(); //performs actions to form the final new suspicious users to follow (deletes inactive users)
        startListener();
    }
    
    /**
     *Receives all information about the list of following users.
     * Stores that information at different collections in mongoDB.
     */
    public void startListener() { 
        
        listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                String json = DataObjectFactory.getRawJSON(status);
                DBObject jsonObj = (DBObject) JSON.parse(json);
                mongo.addObjectToFollowedUsersActivity(jsonObj);
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onTrackLimitationNotice(int i) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onScrubGeo(long l, long l1) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onException(Exception excptn) {
                //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        
        if(suspicious!=null){
            addFilter();
        }
    }
    
    /**
     * Prepares the appropriate users list to follow their activity.
     * Performs the query in Streaming API.
     */
    private void addFilter(){
        
        long[] userIDs = new long[this.suspicious.size()];
        int i = 0;
        
        for (String id : suspicious) {
            userIDs[i++] = Long.parseLong(id);
        }
        
        if(userIDs.length>0){

            fq.follow(userIDs); // follow users' activity

            stream = new TwitterStreamFactory(config).getInstance();
            stream.addListener(listener);
            stream.filter(fq);
            
//          //print all userIDs that are going to be followed
//          for (int j=0; j<userIDs.length; j++) 
//              System.out.println(userIDs[j]);         
        }
    }
    
    /**
     * Shut downs the crawling for current list of followed users.
     * alternative if simple shutdown dont work.
     */
    private void stopStreaming() {

        if (stream == null) {
            return;
        }
        
        stream.addConnectionLifeCycleListener(new ConnectionLifeCycleListener() {

            @Override
            public void onConnect() {

            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onCleanUp() {
                stream = null;
            }
        });

        stream.shutdown();
  
        while (stream != null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
    }
}
