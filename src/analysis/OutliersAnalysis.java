package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author sokpa
 */
public class OutliersAnalysis {
    
    public OutliersAnalysis() throws IOException{
        findDifference();
        findLastFramesRTs();
        findTimeFramesOfHighIqr();
        findTimeFramesOfHighMaxRTAndHighIqr();
    }
    
    /**
     * Find users that exist in f6 that dont exist in f4.
     * Then find the maximumRT for these users in any time window.
     */
    private static void findDifference() throws IOException{
        File file4 = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\newRes12\\results12-filtered-retOnly_4_susp.txt");
        File file6 = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\newRes12\\results12-filtered-retOnly_6_susp.txt");
        BufferedReader reader = null;
        HashSet<Long> f4ids = new HashSet<>();
        HashSet<Long> f6_f4ids = new HashSet<>();
        
        try {
            reader = new BufferedReader(new FileReader(file4));
            String text = reader.readLine();
            text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                f4ids.add(Long.parseLong(splited[0]));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        System.out.println("size of f4: " + f4ids.size());
        
        try {
            reader = new BufferedReader(new FileReader(file6));
            String text = reader.readLine();
            text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                if(!f4ids.contains(Long.parseLong(splited[0])))
                {
                    f6_f4ids.add(Long.parseLong(splited[0]));
                }
                
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        HashMap<Long,Integer> users = new HashMap<>();
        System.out.println("size of f6_f4: "+ f6_f4ids.size());
        // create an iterator
        // check values
        for(long t: f6_f4ids){
            users.put(t, 0);
        }
        
        File file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results2-filtered-retOnly.txt");
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                if(f6_f4ids.contains(Long.parseLong(splited[1])))
                {
                    if(Integer.parseInt(splited[4]) > users.get(Long.parseLong(splited[1])));
                        users.replace(Long.parseLong(splited[1]), Integer.parseInt(splited[4]));
                }
                
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        Iterator it = users.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        
        
        
    }    
    
    
    
    private static void findLastFramesRTs() throws IOException{
        File file = new File("C:\\Users\\sokpa\\Documents\\NetBeansProjects\\Test\\xristes.txt");
        BufferedReader reader = null;
        Long[][] users = new Long[20][6];
        HashSet<Long> ids = new HashSet<>();
        int pos = 0;
        int[] point = new int[20];
        
        for(int i=0; i<20; i++){
            point[i] = 1;
        }
        
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                ids.add(Long.parseLong(splited[0]));
                users[pos][0] = Long.parseLong(splited[0]);
                pos++;
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results1-filtered-retOnly.txt");
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                if(ids.contains(Long.parseLong(splited[1])))
                {
                    for(int i=0; i<20; i++){
                        if(users[i][0] == Long.parseLong(splited[1])){
                            users[i][point[i]] = Long.parseLong(splited[5]);
                            point[i]++;
                        }
                    }
                    
                }
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
//        Iterator it = i.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry pair = (Map.Entry)it.next();
//            System.out.println(pair.getValue());
//            it.remove(); // avoids a ConcurrentModificationException
//        }
    
        
    }
    
    private static void findTimeFramesOfHighIqr() throws IOException{
        File file = new File("C:\\Users\\sokpa\\Documents\\NetBeansProjects\\Test\\xristes.txt");
        BufferedReader reader = null;
        HashMap<Long, Double> iqrs = new HashMap<>();
        HashMap<Long, Integer> frames = new HashMap<>();
        
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                iqrs.put(Long.parseLong(splited[0]), -10.0);
                frames.put(Long.parseLong(splited[0]), -1);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results2-filtered-retOnly.txt");
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                if(iqrs.containsKey(Long.parseLong(splited[1])))
                {
                    if(Double.parseDouble(splited[6]) > iqrs.get(Long.parseLong(splited[1]))){
                        iqrs.replace(Long.parseLong(splited[1]), Double.parseDouble(splited[6]));
                        frames.replace(Long.parseLong(splited[1]), Integer.parseInt(splited[0]));
                    }
                }
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        Iterator it = frames.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
    
    private static void findTimeFramesOfHighMaxRTAndHighIqr() throws IOException{
        File file = new File("C:\\Users\\sokpa\\Documents\\NetBeansProjects\\Test\\xristes.txt");
        BufferedReader reader = null;
        HashMap<Long, OutliersDetails> users = new HashMap<>();
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                users.put(Long.parseLong(splited[0]), new OutliersDetails(Long.parseLong(splited[0])));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results12-filtered-retOnly.txt");
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                if(users.containsKey(Long.parseLong(splited[1])))
                {
                    if(Integer.parseInt(splited[4]) > users.get(Long.parseLong(splited[1])).getMaxRT()){
                        users.get(Long.parseLong(splited[1])).setMaxRT(Integer.parseInt(splited[4]));
                        users.get(Long.parseLong(splited[1])).setMaxRTtf(Integer.parseInt(splited[0]));                        
                    }
                    
                    if(Double.parseDouble(splited[6]) > users.get(Long.parseLong(splited[1])).getIqrRT()){
                        users.get(Long.parseLong(splited[1])).setIqrRT(Double.parseDouble(splited[6]));
                        users.get(Long.parseLong(splited[1])).setIqrRTtf(Integer.parseInt(splited[0]));
                    }
                }
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        
        Iterator it = users.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            OutliersDetails temp = (OutliersDetails) pair.getValue();
            System.out.println(temp.getId() + " " + temp.getMaxRT() + " " + temp.getMaxRTtf() + " " + temp.getIqrRT() + " " + temp.getIqrRTtf());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
    
}
