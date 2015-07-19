package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Sokratis Papadopoulos
 */
public class ConvertToOccurrences {
    
    private final HashMap<Integer, Integer> retweets;
    private final HashMap<Integer, Integer> minRetweets;
    private final HashMap<Integer, Integer> maxRetweets;
    private final HashMap<Float, Integer> avgRetweets;
    private final HashMap<Float, Integer> iqrRetweets;
    
    private final HashMap<Integer, Integer> retweetsRec;
    private final HashMap<Integer, Integer> minRetweetsRec;
    private final HashMap<Integer, Integer> maxRetweetsRec;
    private final HashMap<Float, Integer> avgRetweetsRec;
    private final HashMap<Float, Integer> iqrRetweetsRec;
    
    private PrintWriter writer;
    
    public ConvertToOccurrences() throws FileNotFoundException, UnsupportedEncodingException {
        retweets = new HashMap<>();
        minRetweets = new HashMap<>();
        maxRetweets = new HashMap<>();
        avgRetweets = new HashMap<>();
        iqrRetweets = new HashMap<>();
        
        retweetsRec = new HashMap<>();
        minRetweetsRec = new HashMap<>();
        maxRetweetsRec = new HashMap<>();
        avgRetweetsRec = new HashMap<>();
        iqrRetweetsRec = new HashMap<>();
        
        File file = new File("results.txt");
        BufferedReader reader = null;

        System.out.println("start reading...");
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                addToMaps(splited);
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
    
    /**
     * 
     * @param splited 
     */
    private void addToMaps(String[] splited){
        addToMap(Integer.parseInt(splited[2]), retweets);
        addToMap(Integer.parseInt(splited[3]), minRetweets);
        addToMap(Integer.parseInt(splited[4]), maxRetweets);
        addToMapF(Float.parseFloat(splited[5]), avgRetweets);
        addToMapF(Float.parseFloat(splited[6]), iqrRetweets);

        addToMap(Integer.parseInt(splited[7]), retweetsRec);
        addToMap(Integer.parseInt(splited[8]), minRetweetsRec);
        addToMap(Integer.parseInt(splited[9]), maxRetweetsRec);
        addToMapF(Float.parseFloat(splited[10]), avgRetweetsRec);
        addToMapF(Float.parseFloat(splited[11]), iqrRetweetsRec);
    }
    
    /**
     * 
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void startWriting() throws FileNotFoundException, UnsupportedEncodingException{
        print(retweets, "Retweets");
        print(minRetweets, "MinRetweets");
        print(maxRetweets, "MaxRetweets");
        printF(avgRetweets, "AvgRetweets");
        printF(iqrRetweets, "IqrRetweets");
        
        print(retweetsRec, "RetweetsRec");
        print(minRetweetsRec, "MinRetweetsRec");
        print(maxRetweetsRec, "MaxRetweetsRec");
        printF(avgRetweetsRec, "AvgRetweetsRec");
        printF(iqrRetweetsRec, "IqrRetweetsRec");
    }
    
    /**
     * 
     * @param map
     * @param type
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void print(HashMap<Integer, Integer> map, String type) throws FileNotFoundException, UnsupportedEncodingException{
        writer = new PrintWriter("results" + type +".txt", "UTF-8");  
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            writer.println(pair.getKey() + "\t" + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        writer.close();
    }
    
    /**
     * 
     * @param map
     * @param type
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException 
     */
    private void printF(HashMap<Float, Integer> map, String type) throws FileNotFoundException, UnsupportedEncodingException{
        writer = new PrintWriter("results" + type +".txt", "UTF-8");  
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            writer.println(pair.getKey() + "\t" + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        writer.close();
    }
    
    /**
     * 
     * @param key
     * @param map 
     */
    private void addToMap(Integer key, HashMap<Integer,Integer> map){
        if (map.containsKey(key)){
            map.replace(key, map.get(key)+1);
        } else {
            map.put(key, 1);
        }
    }
    
    /**
     * 
     * @param key
     * @param map 
     */
    private void addToMapF(Float key, HashMap<Float,Integer> map){
        if (map.containsKey(key)){
            map.replace(key, map.get(key)+1);
        } else {
            map.put(key, 1);
        }
    }
    
}
