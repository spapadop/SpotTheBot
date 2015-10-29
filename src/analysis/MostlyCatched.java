
package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author sokpa
 */
public class MostlyCatched {
    
    public MostlyCatched() throws FileNotFoundException, IOException{
        HashSet<Long> raw= getOnlyRaw();
        HashSet<Long> norm = getOnlyNorm();

        HashSet<Long> bestRaw = new HashSet<>();
        HashSet<Long> bestNorm = new HashSet<>();
        
        HashMap<Long, Integer> minRTraw = getHashMap("raw12\\results12-filtered-retOnly_3_susp");
        HashMap<Long, Integer> maxRTraw = getHashMap("raw12\\results12-filtered-retOnly_4_susp");
        HashMap<Long, Integer> avgRTraw = getHashMap("raw12\\results12-filtered-retOnly_5_susp");
        HashMap<Long, Integer> iqrRTraw = getHashMap("raw12\\results12-filtered-retOnly_6_susp");
        
        HashMap<Long, Integer> minRTnorm = getHashMap("norm12\\results12-filtered-retOnly-normalized_3_susp");
        HashMap<Long, Integer> maxRTnorm = getHashMap("norm12\\results12-filtered-retOnly-normalized_4_susp");
        HashMap<Long, Integer> avgRTnorm = getHashMap("norm12\\results12-filtered-retOnly-normalized_5_susp");
        HashMap<Long, Integer> iqrRTnorm = getHashMap("norm12\\results12-filtered-retOnly-normalized_6_susp");
        
        System.out.println("RAW TIME ======================================");
        
        for(Long id: raw){
            
//            if(minRTraw.containsKey(id) && minRTraw.get(id) >= 2){
//                System.out.println("2");
//                bestRaw.add(id);
//            }
            
//            if(maxRTraw.containsKey(id) && maxRTraw.get(id) >= 7){
//                System.out.println("3");
//                bestRaw.add(id);
//            }
//            
//            if(avgRTraw.containsKey(id) && avgRTraw.get(id) >= 0){
//                System.out.println("4");
//                bestRaw.add(id);
//            }
            
            if(iqrRTraw.containsKey(id)){
                System.out.println("5");
                bestRaw.add(id);
            }
        }
        
        System.out.println("NORMALIZED TIME =================================");
        
        for(Long id: norm){
            
//            if(minRTnorm.containsKey(id) && minRTnorm.get(id) >= 5){
//                System.out.println("2");
//                bestNorm.add(id);
//            }
            
//            if(maxRTnorm.containsKey(id) && maxRTnorm.get(id) >= 5){
//                System.out.println("3");
//                bestNorm.add(id);
//            }
//            
//            if(avgRTnorm.containsKey(id) && avgRTnorm.get(id) >= 4){
//                System.out.println("4");
//                bestNorm.add(id);
//            }
//            
//            if(iqrRTnorm.containsKey(id)){
//                System.out.println("5");
//                bestNorm.add(id);
//            }
        }
        
        PrintWriter writer = new PrintWriter("norm.txt");
        for(Long id : bestNorm){
            writer.println(id);
        }
        writer.close();
        
        writer = new PrintWriter("raw.txt");
        for(Long id : bestRaw){
            writer.println(id);
        }
        writer.close();
        
        
//        findCommonsAndDifferents("black_list", "black_list", false);
//        for(int i=3; i<=6; i++){
//            findCommonsAndDifferents("results12-filtered-retOnly_"+i+"_susp", "results12-filtered-retOnly-normalized_"+i+"_susp", true);
//        }
    }
    
    private static HashMap<Long,Integer> getHashMap(String filename) throws FileNotFoundException, IOException{
        HashMap<Long, Integer> map = new HashMap<>();
        File file = new File(filename+ ".txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                map.put(Long.parseLong(splited[0]), Integer.parseInt(splited[1]));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        return map;
    }
    
    private static HashSet<Long> getOnlyRaw() throws IOException{
        HashSet<Long> raw = new HashSet<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("outliersUsers\\3456_onlyRaw.txt"));
            String text= reader.readLine();
            while ((text = reader.readLine()) != null) {
                raw.add(Long.parseLong(text));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        return raw;
    }
    
    private static HashSet<Long> getOnlyNorm() throws IOException{
        HashSet<Long> norm = new HashSet<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("outliersUsers\\3456_onlyNorm.txt"));
            String text= reader.readLine();
            while ((text = reader.readLine()) != null) {
                norm.add(Long.parseLong(text));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        return norm;
    }
    
}
