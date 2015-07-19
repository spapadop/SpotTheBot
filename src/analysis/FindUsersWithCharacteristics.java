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
 *
 * @author Sokratis
 */
public class FindUsersWithCharacteristics {
    private final HashSet<Long> usersOverFiveRT;
    private final HashSet<Long> usersOverTwoMinRT;
    private final HashSet<Long> usersOverTenMaxRT;
    
    private PrintWriter writer;
    
    public FindUsersWithCharacteristics() throws FileNotFoundException, UnsupportedEncodingException{
        
        usersOverFiveRT = new HashSet<>();
        usersOverTwoMinRT = new HashSet<>();
        usersOverTenMaxRT = new HashSet<>();
        
        File file = new File("H:\\Thesis\\runs\\run2\\analysis\\results2-per-time-window.txt");
        BufferedReader reader = null;

        System.out.println("start reading...");
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                checkWindowData(splited);             
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
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
