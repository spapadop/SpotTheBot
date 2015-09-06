package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Normalizes the data based on the formula: 
 * X' = a + \frac{\left(X-X_{min}\right)\left(b-a\right)}{X_{max} - X_{min}}
 *
 * @author Sokratis Papadopoulos
 */
public class NormalizationOfData {

    private double[][] min;
    private double[][] max;

    private double a;
    private double b;
    
    private int win;
    
    private PrintWriter writer;
    
    private static final int NUM_WIN = 44; //43,44

    public NormalizationOfData() throws FileNotFoundException, IOException {
        min = new double[NUM_WIN][5]; 
        max = new double[NUM_WIN][5];

        for(int i = 0; i < NUM_WIN; i++){
            for (int j = 0; j < 5; j++) {
                min[i][j] = Double.MAX_VALUE;
                max[i][j] = Double.MIN_VALUE;
            }
        }

        a = 0.1;
        b = 0.9;

        calculateMinMax();
        normalizeData();

    }
    
    private void calculateMinMax() throws FileNotFoundException, UnsupportedEncodingException, IOException{
        File file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results2-filtered-retOnly.txt");
        BufferedReader reader = null;
        win = 1;
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                if (Integer.parseInt(splited[0]) != win) {
                    win++;
                }
                checkLimits(splited);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    private void normalizeData() throws FileNotFoundException, UnsupportedEncodingException, IOException{
        File file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results2-filtered-retOnly.txt");
        BufferedReader reader = null;
        writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results2-filtered-retOnly-normalizedGood.txt", "UTF-8");
        writer.println("window userID RT minRT maxRT avgRT iqrRT");
        win = 1;
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();

            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                if (Integer.parseInt(splited[0]) != win) {
                    win++;
                }
                normalizeWindow(splited);
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
            
            writer.close();
        }
        
    }

    private void checkLimits(String[] splited) {

        for (int i = 0; i < 5; i++) {
            if (Double.parseDouble(splited[i + 2]) < min[win-1][i]) {
                min[win-1][i] = Double.parseDouble(splited[i+2]);
            }
            
            if (Double.parseDouble(splited[i + 2]) > max[win-1][i]) {
                max[win-1][i] = Double.parseDouble(splited[i+2]);
            }
        }
    }
    
    private void normalizeWindow(String[] splited){
        
        long userID = Long.parseLong(splited[1]);
        double rt    =  a + ( (Double.parseDouble(splited[2]) - min[win-1][0])*(b-a) ) / (max[win-1][0] - min[win-1][0]);
        double minRt =  a + ( (Double.parseDouble(splited[3]) - min[win-1][1])*(b-a) ) / (max[win-1][1] - min[win-1][1]);
        double maxRt =  a + ( (Double.parseDouble(splited[4]) - min[win-1][2])*(b-a) ) / (max[win-1][2] - min[win-1][2]);
        double avgRt =  a + ( (Double.parseDouble(splited[5]) - min[win-1][3])*(b-a) ) / (max[win-1][3] - min[win-1][3]);
        double iqrRt =  a + ( (Double.parseDouble(splited[6]) - min[win-1][4])*(b-a) ) / (max[win-1][4] - min[win-1][4]);
        
        writer.printf("%d %d %f %f %f %f %f \n", win, userID, rt, minRt, maxRt, avgRt, iqrRt);
        
    }

}
