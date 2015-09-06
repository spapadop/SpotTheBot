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
public class NormalizedOccurrences {
    
    private final HashMap<Double, Integer> retweets;
    private final HashMap<Double, Integer> minRetweets;
    private final HashMap<Double, Integer> maxRetweets;
    private final HashMap<Double, Integer> avgRetweets;
    private final HashMap<Double, Integer> iqrRetweets;
    
    private PrintWriter writer;

    public NormalizedOccurrences() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        
        retweets = new HashMap<>();
        minRetweets = new HashMap<>();
        maxRetweets = new HashMap<>();
        avgRetweets = new HashMap<>();
        iqrRetweets = new HashMap<>();

        File file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results1-filtered-retOnly-normalized.txt");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();

            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                addToMaps(splited);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results2-filtered-retOnly-normalized.txt");

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();

            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                addToMaps(splited);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        startWriting();
    }

    private void addToMaps(String[] splited) {
        addToMap(Double.parseDouble(splited[2]), retweets);
        addToMap(Double.parseDouble(splited[3]), minRetweets);
        addToMap(Double.parseDouble(splited[4]), maxRetweets);
        addToMap(Double.parseDouble(splited[5]), avgRetweets);
        addToMap(Double.parseDouble(splited[6]), iqrRetweets);
    }
    
    private void addToMap(Double key, HashMap<Double, Integer> map) {
        if (map.containsKey(key)) {
            map.replace(key, map.get(key) + 1);
        } else {
            map.put(key, 1);
        }
    }

    private void startWriting() throws FileNotFoundException, UnsupportedEncodingException {
        print(retweets, "Retweets");
        print(minRetweets, "MinRetweets");
        print(maxRetweets, "MaxRetweets");
        print(avgRetweets, "AvgRetweets");
        print(iqrRetweets, "IqrRetweets");
    }
    
    private void print(HashMap<Double, Integer> map, String type) throws FileNotFoundException, UnsupportedEncodingException {
        writer = new PrintWriter("results" + type + "12.txt", "UTF-8");
        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            double key = (double) pair.getKey(); //+1 to avoid zeros
            writer.println(key + "\t" + pair.getValue()); 
            it.remove(); // avoids a ConcurrentModificationException
        }
        writer.close();
    }
        
}
