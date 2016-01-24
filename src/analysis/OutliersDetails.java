package analysis;

/**
 *
 * @author Sokratis Papadopoulos
 */
public class OutliersDetails {
    
    private long id;
    private int maxRT;
    private double iqrRT;
    
    private int maxRTtf;
    private int iqrRTtf;
    
    public OutliersDetails(Long id){
        this.id = id;
        maxRT = -1;
        iqrRT = -1.0;
        maxRTtf = -1;
        iqrRTtf = -1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMaxRT() {
        return maxRT;
    }

    public void setMaxRT(int maxRT) {
        this.maxRT = maxRT;
    }

    public double getIqrRT() {
        return iqrRT;
    }

    public void setIqrRT(double iqrRT) {
        this.iqrRT = iqrRT;
    }

    public int getMaxRTtf() {
        return maxRTtf;
    }

    public void setMaxRTtf(int maxRTtf) {
        this.maxRTtf = maxRTtf;
    }

    public int getIqrRTtf() {
        return iqrRTtf;
    }

    public void setIqrRTtf(int iqrRTtf) {
        this.iqrRTtf = iqrRTtf;
    }
    
}
