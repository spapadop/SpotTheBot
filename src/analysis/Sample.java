package analysis;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

/**
 * This class is used to implement the sampling of users from 10 defined bins
 * for each feature (minRT,maxRT,avgRT,iqrRT).
 *
 * @author Sokratis Papadopoulos
 */
public final class Sample {

    private static HashSet<Long> sample;

    private static Double[][] minRTranges;
    private static Double[][] maxRTranges;
    private static Double[][] avgRTranges;
    private static Double[][] iqrRTranges;
    private static final HashMap<Long, Integer>[] bins = (HashMap<Long, Integer>[]) new HashMap<?, ?>[10];

    private static BufferedReader reader;
    private static PrintWriter writer;

    private static PrintWriter[] writeInCol;

    private static String line;
    private static String[] tokens;

    private static final boolean DEBUG = false;

    public static void main(String[] args) throws IOException, ParseException, URISyntaxException {

        setBins(); //loads the all 4 bins ranges into double[][] arrays.
        if (DEBUG) {
            printBins();
        }

        for (int runNum = 1; runNum <= 2; runNum++) {
            System.out.println("*************************************");

            //where we gather all users final sample (1000)
            sample = new HashSet<>();
            writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\newData\\run" + runNum + "\\sample.txt", "UTF-8");
            writer.println("UserID" + "\t" + "Feature" + "\t" + "timesOccuredInBin" + "\t" + "binNum");
            writeInCol = new PrintWriter[4];
            for (int i = 0; i < 4; i++) {
                writeInCol[i] = new PrintWriter("C:\\Users\\sokpa\\Desktop\\newData\\run" + runNum + "\\sampleCol" + i + ".txt", "UTF-8");
                writeInCol[i].println("userID" + "\t" + "timesOccuredInBin" + "\t" + "binNum");
            }

            //for each feature-column collect 250 users (4*250=1000)
            for (int i = 3; i <= 6; i++) {
                take250users(runNum, i);
            }

            reader.close();
            writer.close();
            for (int i = 0; i < 4; i++) {
                writeInCol[i].close();
            }
        }
    }

    /**
     * Take 250 users out of the specified feature's bins.
     *
     * @param runNum
     * @param col
     * @return
     * @throws IOException
     */
    public static void take250users(int runNum, int col) throws IOException {

        //initialize hashmaps - bins
        for (int j = 0; j < 10; j++) {
            bins[j] = new HashMap<>();
        }

        String feature = null;
        if (col == 3) {
            loadUsersToBinsOfMinRT(runNum, col);
            feature = "minRT";
        } else if (col == 4) {
            loadUsersToBinsOfMaxRT(runNum, col);
            feature = "maxRT";
        } else if (col == 5) {
            loadUsersToBinsOfAvgRT(runNum, col);
            feature = "avgRT";
        } else if (col == 6) {
            loadUsersToBinsOfIqrRT(runNum, col);
            feature = "iqrRT";
        } else {
            System.out.println("ERROR! Column out of bounds.");
        }

        int usersFromCol = 0;
        for (int i = 9; i >= 0; i--) {
            int count = 0;
            while (count < 25 && !bins[i].isEmpty()) { //while 25 ids are not yet extracted and bin is not empty
                List<Long> keysAsArray = new ArrayList<>(bins[i].keySet());
                Random r = new Random();
                Long randomUserId = keysAsArray.get(r.nextInt(keysAsArray.size()));
                Integer value = bins[i].get(randomUserId);

                //add max user to sample
                if (sample.add(randomUserId)) { //added IF to avoid duplicates
                    usersFromCol++;
                    count++;
                    writeInCol[col - 3].println(randomUserId + "\t" + value + "\t" + (i + 1));
                    writer.println(randomUserId + "\t" + feature + "\t" + value + "\t" + (i + 1));
                }
                bins[i].remove(randomUserId); //remove user from list
            }
        }

        System.out.println("Initially, col: " + col + " contributed: " + usersFromCol);

        int remaining = 250 - usersFromCol;
        if (remaining > 0) {
            //if there's a gap to 250, we'll collect more users from this col
            System.out.println("Extracting " + remaining + " more users from col " + col);

            do {
                for (int i = 9; i >= 0; i--) { //take one more user from each bin
                    boolean taken = false;
                    while (remaining > 0 && !bins[i].isEmpty() && !taken) { //while 25 ids are not yet extracted and bin is not empty
                        List<Long> keysAsArray = new ArrayList<>(bins[i].keySet());
                        Random r = new Random();
                        Long randomUserId = keysAsArray.get(r.nextInt(keysAsArray.size()));
                        Integer value = bins[i].get(randomUserId);

                        //add max user to sample
                        if (sample.add(randomUserId)) { //added IF to avoid duplicates
                            remaining--;
                            taken = true;
                            usersFromCol++;
                            writeInCol[col - 3].println(randomUserId + "\t" + value + "\t" + (i + 1));
                            writer.println(randomUserId + "\t" + feature + "\t" + value + "\t" + (i + 1));
                        }

                        bins[i].remove(randomUserId); //remove user from list
                    }
                }
            } while (remaining != 0);
        }
        System.out.println("Finally, col: " + col + " contributed: " + usersFromCol);
        System.out.println("sample size after: " + sample.size());
        System.out.println("");
    }

    //****************** LOADING USERS TO BINS *********************************
    private static void loadUsersToBinsOfMaxRT(int runNum, int col) throws FileNotFoundException, IOException {
        reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results" + runNum + "-filtered-retOverTwo.txt"));
        line = reader.readLine(); //header out          
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            Long userID = Long.parseLong(tokens[1]);
            Double number = Double.parseDouble(tokens[col]);

            for (int i = 0; i < 10; i++) {
                if (number >= maxRTranges[i][0] && number <= maxRTranges[i][1]) {

                    if (bins[i].containsKey(userID)) {
                        bins[i].replace(userID, bins[i].get(userID) + 1);
                    } else {
                        bins[i].put(userID, 1);
                    }

                    break;
                }
            }

            line = reader.readLine();
        }

        reader.close();

        if (DEBUG) {
            System.out.println("********FOR COL = " + col + " **************");
            for (int i = 0; i < 10; i++) {
                System.out.println(i + ". " + bins[i].size());
            }
            System.out.println("*******************************************");
        }
    }

    private static void loadUsersToBinsOfAvgRT(int runNum, int col) throws FileNotFoundException, IOException {
        reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results" + runNum + "-filtered-retOverTwo.txt"));
        line = reader.readLine(); //header out          
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            Long userID = Long.parseLong(tokens[1]);
            Double number = Double.parseDouble(tokens[col]);

            for (int i = 0; i < 10; i++) {
                if (number >= avgRTranges[i][0] && number < avgRTranges[i][1]) {

                    if (bins[i].containsKey(userID)) {
                        bins[i].replace(userID, bins[i].get(userID) + 1);
                    } else {
                        bins[i].put(userID, 1);
                    }

                    break;
                }
            }

            line = reader.readLine();
        }

        reader.close();

        if (DEBUG) {
            System.out.println("********FOR COL = " + col + " **************");
            for (int i = 0; i < 10; i++) {
                System.out.println(i + ". " + bins[i].size());
            }
            System.out.println("*******************************************");
        }
    }

    private static void loadUsersToBinsOfIqrRT(int runNum, int col) throws FileNotFoundException, IOException {
        reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results" + runNum + "-filtered-retOverTwo.txt"));
        line = reader.readLine(); //header out          
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            Long userID = Long.parseLong(tokens[1]);
            Double number = Double.parseDouble(tokens[col]);

            for (int i = 0; i < 10; i++) {
                if (number >= iqrRTranges[i][0] && number < iqrRTranges[i][1]) {

                    if (bins[i].containsKey(userID)) {
                        bins[i].replace(userID, bins[i].get(userID) + 1);
                    } else {
                        bins[i].put(userID, 1);
                    }

                    break;
                }
            }

            line = reader.readLine();
        }

        reader.close();

        if (DEBUG) {
            System.out.println("********FOR COL = " + col + " **************");
            for (int i = 0; i < 10; i++) {
                System.out.println(i + ". " + bins[i].size());
            }
            System.out.println("*******************************************");
        }
    }

    private static void loadUsersToBinsOfMinRT(int runNum, int col) throws FileNotFoundException, IOException {
        reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results" + runNum + "-filtered-retOverTwo.txt"));
        line = reader.readLine(); //header out        
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            Long userID = Long.parseLong(tokens[1]);
            Double number = Double.parseDouble(tokens[col]);

            for (int i = 0; i < 10; i++) {
                if (number >= minRTranges[i][0] && number <= minRTranges[i][1]) {

                    if (bins[i].containsKey(userID)) {
                        bins[i].replace(userID, bins[i].get(userID) + 1);
                    } else {
                        bins[i].put(userID, 1);
                    }

                    break;
                }
            }

            line = reader.readLine();
        }

        reader.close();

        if (DEBUG) {
            System.out.println("********FOR COL = " + col + " **************");
            for (int i = 0; i < 10; i++) {
                System.out.println(i + ". " + bins[i].size());
            }
            System.out.println("*******************************************");
        }
    }

    //******************** SETTING UP BINS *************************************
    /**
     * Initializing ranges to configure bins.
     *
     * @throws java.io.FileNotFoundException
     */
    public static void setBins() throws FileNotFoundException, IOException {

        minRTranges = new Double[10][2];
        readRanges("minRTranges", 3);
        maxRTranges = new Double[10][2];
        readRanges("maxRTranges", 4);
        avgRTranges = new Double[10][2];
        readRanges("avgRTranges", 5);
        iqrRTranges = new Double[10][2];
        readRanges("iqrRTranges", 6);

    }

    /**
     * For the feature it's called (minRT,maxRT,avgRT,iqrRT) it reads the ranges
     * for the bins from a relevant text file.
     *
     * @param typeRanges
     * @param col
     * @throws IOException
     */
    public static void readRanges(String typeRanges, int col) throws IOException {
        reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\ranges\\" + typeRanges + ".txt"));

        int counter = 0;
        line = reader.readLine();
        while (line != null) {
            tokens = line.split("\\s+");
            Double low = Double.parseDouble(tokens[0]);
            Double high = Double.parseDouble(tokens[1]);

            if (col == 3) {
                minRTranges[counter][0] = low;
                minRTranges[counter][1] = high;
            } else if (col == 4) {
                maxRTranges[counter][0] = low;
                maxRTranges[counter][1] = high;
            } else if (col == 5) {
                avgRTranges[counter][0] = low;
                avgRTranges[counter][1] = high;
            } else if (col == 6) {
                iqrRTranges[counter][0] = low;
                iqrRTranges[counter][1] = high;
            } else {
                System.out.println("ERROR: Non recognized column-feature.");
            }

            counter++;
            line = reader.readLine();
        }
        reader.close();
    }

    /**
     * Function used to print the bin ranges, checking if everything was loaded
     * properly from relevant files.
     */
    public static void printBins() {
        System.out.println("============================");
        System.out.println("----PRINTING BINS: minRT----");
        for (int i = 0; i < 10; i++) {
            System.out.println("[" + minRTranges[i][0] + "," + minRTranges[i][1] + "]");
        }

        System.out.println("");
        System.out.println("----PRINTING BINS: maxRT----");
        for (int i = 0; i < 10; i++) {
            System.out.println("[" + maxRTranges[i][0] + "," + maxRTranges[i][1] + "]");
        }

        System.out.println("");
        System.out.println("----PRINTING BINS: avgRT----");
        for (int i = 0; i < 10; i++) {
            System.out.println("[" + avgRTranges[i][0] + "," + avgRTranges[i][1] + ")");
        }

        System.out.println("");
        System.out.println("----PRINTING BINS: iqrRT----");
        for (int i = 0; i < 10; i++) {
            System.out.println("[" + iqrRTranges[i][0] + "," + iqrRTranges[i][1] + ")");
        }
        System.out.println("============================");
    }
}
