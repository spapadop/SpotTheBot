package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Spots which users from our database have some specific characteristics that
 * make them suspicious. These characteristics checked are:
 * a) over total five Retweets in a time window
 * b) over two min Retweets in a time window
 * c) over ten max Retweets in a time window
 * 
 * Then, it exports the relative users into files. Additionally, it makes all 
 * the possible combination of characteristics and print the relative users 
 * in different text files as well. (example: users with a & b)
 * 
 * @author Sokratis Papadopoulos
 */
public class UsersWithCharacteristics {
    private final HashSet<Long> usersOverFiveRT;
    private final HashSet<Long> usersOverTwoMinRT;
    private final HashSet<Long> usersOverTenMaxRT;
    
    private PrintWriter writer;
    
    public UsersWithCharacteristics() throws FileNotFoundException, UnsupportedEncodingException, IOException{
        
        usersOverFiveRT = new HashSet<>();
        usersOverTwoMinRT = new HashSet<>();
        usersOverTenMaxRT = new HashSet<>();
        
        File file = new File("H:\\Thesis\\runs\\run2\\analysis\\results-per-time-window.txt");
        BufferedReader reader = null;

        System.out.println("start reading...");
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                checkWindowData(splited);             
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        System.out.println("finished reading...");
        
        startWriting();
    }
    
    private void checkWindowData(String[] splited){
        
        //checking retweets done > 4
        if(Integer.parseInt(splited[2]) > 4){
            this.usersOverFiveRT.add(Long.parseLong(splited[1]));
        }
        
        //checking min retweets > 1
        if(Integer.parseInt(splited[3]) > 1){
            this.usersOverTwoMinRT.add(Long.parseLong(splited[1]));
        }
        
        //checking max retweets > 5
        if(Integer.parseInt(splited[4]) > 9){
            this.usersOverTenMaxRT.add(Long.parseLong(splited[1]));
        }
        
    }
    
    private void startWriting() throws FileNotFoundException, UnsupportedEncodingException{
        print(this.usersOverFiveRT, "UsersWithHighRTs");
        print(this.usersOverTwoMinRT, "UsersWithHighMinRTs");
        print(this.usersOverTenMaxRT, "UsersWithHighMaxRTs");
        print(this.usersOverFiveRT, this.usersOverTwoMinRT, "UsersWithHighRTsAndMinRTs");
        print(this.usersOverFiveRT, this.usersOverTenMaxRT, "UsersWithHighRTsAndMaxRTs");
        print(this.usersOverTwoMinRT, this.usersOverTenMaxRT, "UsersWithHighMinRTsAndMaxRTs");
        print(this.usersOverFiveRT, this.usersOverTwoMinRT, this.usersOverTenMaxRT, "UsersWithHighRTsAndMinRTsAndMaxRTs");
    }
    
    /**
     * 
     * @param set
     * @param type
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void print(HashSet<Long> set, String type) throws FileNotFoundException, UnsupportedEncodingException{
        writer = new PrintWriter("results" + type +".txt", "UTF-8");  
        Iterator<Long> it = set.iterator();
        while(it.hasNext()){
           writer.println(it.next());
        }
        writer.close();
    }
    
    /**
     * 
     * @param set
     * @param type
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void print(HashSet<Long> set, HashSet<Long> set2, String type) throws FileNotFoundException, UnsupportedEncodingException{
        writer = new PrintWriter("results" + type +".txt", "UTF-8");  
        Iterator<Long> it = set.iterator();
        while(it.hasNext()){
            Long id = it.next();
            if(set2.contains(id))
                writer.println(id);
        }
        writer.close();
    }
    
    /**
     * 
     * @param set
     * @param type
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void print(HashSet<Long> set, HashSet<Long> set2, HashSet<Long> set3, String type) throws FileNotFoundException, UnsupportedEncodingException{
        writer = new PrintWriter("results" + type +".txt", "UTF-8");  
        Iterator<Long> it = set.iterator();
        while(it.hasNext()){
            Long id = it.next();
            if(set2.contains(id) && set3.contains(id))
                writer.println(id);
        }
        writer.close();
    }
    
}
