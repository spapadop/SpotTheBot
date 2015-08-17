package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Provides metrics (avg, var, std, med) for each column of time windows.
 * 
 * @author Sokratis Papadopoulos
 */
public class MetricsPerFeature {
    private double[] data;
    private int size;
    private int counter;
    
    private double avg;
    private double variance;
    private double stdDev;
    private double median;
    //private double iqr; //not enough RAM to calculate it
    
    
    public MetricsPerFeature() throws FileNotFoundException, UnsupportedEncodingException, IOException{
        
        String filePath = "C:\\Users\\sokpa\\Desktop\\Thesis\\data_analysis\\analysis\\results-per-time-window.txt";
        String clearPath = "clearResults.txt";
        removeZeroEntries(filePath);
        
        counter= 0;
        data = new double[countLines(filePath)];// 
        size = data.length;
        
        File file = new File(clearPath);
        BufferedReader reader = null;

        System.out.println("start reading...");
        try {
            for(int i=2; i<12;i++){
                counter=0;
                String text = null;
                reader = new BufferedReader(new FileReader(file));
                System.out.println("working on i=" + i);
                while ((text = reader.readLine()) != null) {
                    String[] splited = text.split("\\s+");
                    data[counter++] = Double.parseDouble(splited[i]);
                }
                calculateMetrics();
                writeResults(i);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        System.out.println("finished reading...");
    }
    
    private void removeZeroEntries(String readPath) throws FileNotFoundException, UnsupportedEncodingException{
        
        File file = new File(readPath);
        BufferedReader reader;
        PrintWriter writer = new PrintWriter("clearResults.txt", "UTF-8"); 

        reader = new BufferedReader(new FileReader(file));
        
        System.out.println("start reading...");
        try {
            boolean flag;
            String text;
                
            while ((text = reader.readLine()) != null) {
                flag = false;
                String[] splited = text.split("\\s+");
                for(int i=2; i<12;i++){
                    if(!splited[i].equals("0") && !splited[i].equals("0.0")){
                        flag = true;
                        break;
                    }
                }
                if(flag){
                    writer.println(text);
                }
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
        writer.close();
        System.out.println("finished writing...");            
    }
    
    private void writeResults(int i) throws FileNotFoundException, UnsupportedEncodingException{
        System.out.println("writing results...");
        PrintWriter writer = new PrintWriter("results" + i+ ".txt", "UTF-8"); 
        writer.println("AVG \t VAR \t STD \t MED \t IQR");
        writer.printf("%.2f\t%.2f\t%.2f\t%.2f\t",avg,variance,stdDev,median);
        //writer.println(iqr);
        
        System.out.println("finished writing");
        writer.close();
    }
    
    private void calculateMetrics(){
        //System.out.println("calculate metrics");
        this.avg = getMean();
        //System.out.println("mean done.");
        this.variance = getVariance();
        //System.out.println("variance done.");
        this.stdDev = getStdDev();
        //System.out.println("stdDev done.");
        this.median = getMedian();
        //System.out.println("median done.");
        
//        DescriptiveStatistics dsRT = new DescriptiveStatistics(data);
//        iqr = dsRT.getPercentile(75) - dsRT.getPercentile(25);
//        System.out.println("iqr done.");
        
    }
    
    double getMean()
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        return sum/size;
    }

    double getVariance()
    {
        double mean = getMean();
        double temp = 0;
        for(double a :data)
            temp += (mean-a)*(mean-a);
        return temp/size;
    }

    double getStdDev()
    {
        return Math.sqrt(getVariance());
    }

    public double getMedian() 
    {
       Arrays.sort(data);

       if (data.length % 2 == 0) 
       {
          return (data[(data.length / 2) - 1] + data[data.length / 2]) / 2.0;
       } 
       else 
       {
          return data[data.length / 2];
       }
    }
    
    private int countLines(String path) throws FileNotFoundException, IOException{
        LineNumberReader  lnr = new LineNumberReader(new FileReader(new File(path)));
        lnr.skip(Long.MAX_VALUE);
        System.out.println(lnr.getLineNumber() + 1); //Add 1 because line index starts at 0
        // Finally, the LineNumberReader object should be closed to prevent resource leak
        lnr.close();
        
        return lnr.getLineNumber() + 1;
    }
    
}
