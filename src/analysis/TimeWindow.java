package analysis;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Stores all information regarding a time window (5 hours) for a user.
 * 
 * @author Sokratis Papadopoulos
 */
public class TimeWindow {
//    private int windowID;
//    private Date from;
//    private Date to;
    
    private int totalRetweets;
    private int minRtPerHour;
    private int maxRtPerHour;
    private float avgRtPerHour;    
    private double iqrRT;
    
    private int totalRetweetsReceived;
    private int minRTrecPerHour;
    private int maxRTrecPerHour;
    private float avgRTrecPerHour;
    private double iqrRTrec;
    
    public TimeWindow(){
//        this.windowID = -1;
//        this.from = null;
//        this.to = null;
        
        this.totalRetweets = 0;
        this.minRtPerHour = Integer.MAX_VALUE;
        this.maxRtPerHour = 0;
        this.avgRtPerHour = 0;    
    
        this.totalRetweetsReceived = 0;
        this.minRTrecPerHour = Integer.MAX_VALUE;
        this.maxRTrecPerHour = 0;
        this.avgRTrecPerHour = 0;
    }
    
    public TimeWindow(TimeChunk[] chunks){
//        this.windowID = id;
//        this.from = from;
//        this.to = to;
        
        this.totalRetweets = 0;
        this.minRtPerHour = Integer.MAX_VALUE;
        this.maxRtPerHour = 0;
        this.avgRtPerHour = 0;    
    
        this.totalRetweetsReceived = 0;
        this.minRTrecPerHour = Integer.MAX_VALUE;
        this.maxRTrecPerHour = 0;
        this.avgRTrecPerHour = 0;
        
        double[] dataRT = new double[5]; // obtain data here
        double[] dataRTrec = new double[5]; // obtain data here
        
        for(int i=0; i< chunks.length; i++){
            dataRT[i] = chunks[i].getRetweets();
            dataRTrec[i] = chunks[i].getRetweetsReceived();
            
            this.totalRetweets += chunks[i].getRetweets();
            this.totalRetweetsReceived += chunks[i].getRetweetsReceived();
            
            if(chunks[i].getRetweets() > this.maxRtPerHour){
                this.maxRtPerHour = chunks[i].getRetweets();
            }
            
            if(chunks[i].getRetweetsReceived() > this.maxRTrecPerHour){
                this.maxRTrecPerHour = chunks[i].getRetweetsReceived();
            }
            
            if(chunks[i].getRetweets() < this.minRtPerHour){
                this.minRtPerHour = chunks[i].getRetweets();
            }
            
            if(chunks[i].getRetweetsReceived() < this.minRTrecPerHour){
                this.minRTrecPerHour = chunks[i].getRetweetsReceived();
            }
        }
        
        this.avgRtPerHour =  (float) this.totalRetweets/5;
        this.avgRTrecPerHour = (float) this.totalRetweetsReceived/5;
        
        DescriptiveStatistics dsRT = new DescriptiveStatistics(dataRT);
        iqrRT = dsRT.getPercentile(75) - dsRT.getPercentile(25);
        
        DescriptiveStatistics dsRTrec = new DescriptiveStatistics(dataRTrec);
        iqrRTrec = dsRTrec.getPercentile(75) - dsRTrec.getPercentile(25);
    }
    
    public String print(){
        String result = totalRetweets + " " + minRtPerHour + " " + maxRtPerHour + " " + avgRtPerHour + " " + iqrRT + " "
                + totalRetweetsReceived + " " + minRTrecPerHour + " " + maxRTrecPerHour + " " + avgRTrecPerHour + " " + iqrRTrec;
        
        return result;
    }

    public int getTotalRetweets() {
        return totalRetweets;
    }

    public void setTotalRetweets(int totalRetweets) {
        this.totalRetweets = totalRetweets;
    }

    public int getMinRtPerHour() {
        return minRtPerHour;
    }

    public void setMinRtPerHour(int minRtPerHour) {
        this.minRtPerHour = minRtPerHour;
    }

    public int getMaxRtPerHour() {
        return maxRtPerHour;
    }

    public void setMaxRtPerHour(int maxRtPerHour) {
        this.maxRtPerHour = maxRtPerHour;
    }

    public float getAvgRtPerHour() {
        return avgRtPerHour;
    }

    public void setAvgRtPerHour(float avgRtPerHour) {
        this.avgRtPerHour = avgRtPerHour;
    }

    public int getTotalRetweetsReceived() {
        return totalRetweetsReceived;
    }

    public void setTotalRetweetsReceived(int totalRetweetsReceived) {
        this.totalRetweetsReceived = totalRetweetsReceived;
    }

    public int getMinRTrecPerHour() {
        return minRTrecPerHour;
    }

    public void setMinRTrecPerHour(int minRTrecPerHour) {
        this.minRTrecPerHour = minRTrecPerHour;
    }

    public int getMaxRTrecPerHour() {
        return maxRTrecPerHour;
    }

    public void setMaxRTrecPerHour(int maxRTrecPerHour) {
        this.maxRTrecPerHour = maxRTrecPerHour;
    }

    public float getAvgRTrecPerHour() {
        return avgRTrecPerHour;
    }

    public void setAvgRTrecPerHour(float avgRTrecPerHour) {
        this.avgRTrecPerHour = avgRTrecPerHour;
    }
    
}
