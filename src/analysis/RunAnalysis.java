package analysis;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.ParseException;

/**
 * This is the main class which runs the whole analysis of the project.
 * It connects with the database and goes through all the analysis we
 * have implemented in the other classes.
 * 
 * @author Sokratis Papadopoulos
 */
public class RunAnalysis {
    
    public static void main(String[] args) throws UnknownHostException, ParseException, FileNotFoundException, UnsupportedEncodingException, IOException {
        
        
        //======== DIRECTLY FROM DATASET (MONGODB) ========
        CalculateChunksAndWindows first = new CalculateChunksAndWindows();
        UsersStats second = new UsersStats();
        SuspiciousTweetActivity third = new SuspiciousTweetActivity();
        UsersRetweetActivity fourth = new UsersRetweetActivity();
        
        //========== ANALYZE TIME-WINDOWS DATA ============
        UsersWithCharacteristics fifth = new UsersWithCharacteristics();
        ConvertToOccurrences sixth = new ConvertToOccurrences();
        MetricsPerFeature seventh = new MetricsPerFeature();      
        
    }
}
