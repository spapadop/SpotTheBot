package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import twitter4j.JSONException;

/**
 * Other functions used as ad-hoc, to fix something. 
 * 
 * @author Sokratis Papadopoulos
 */
public class SupportingFunctions {

    private static PrintWriter writer;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParseException, URISyntaxException {

    }

    public static void calculateNumberOfTimeFramesInRange() throws IOException {
        BufferedReader reader;
        Scanner scanIn = new Scanner(System.in);
        int run;
        do {
            System.out.print("Run(1,2): ");
            run = scanIn.nextInt();
        } while (run != 1 && run != 2);

        int arraySize;
        if (run == 1) {
            arraySize = 614652;
            reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results1-filtered-retOverTwo.txt"));
        } else {
            arraySize = 708728;
            reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results2-filtered-retOverTwo.txt"));
        }
        String line = reader.readLine();

        int col;
        do {
            System.out.print("Column number (4=max,5=avg,6=iqr): "); //if 4 then change the limit! Hard coded
            col = scanIn.nextInt();
        } while (col != 4 && col != 5 && col != 6);

        double[] metric = new double[arraySize];
        int pos = 0;

        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            metric[pos++] = Double.parseDouble(tokens[col]);

            line = reader.readLine();
        }
        reader.close();
        Arrays.sort(metric);

        int res = 1;
        do {
            double high = 0, low = 0;
            int counter = 0;

            System.out.print("Insert range: ");
            low = scanIn.nextDouble();
            high = scanIn.nextDouble();

            int posFoundLow = 0;
            while (metric[posFoundLow] < low) {
                posFoundLow++;
            }
            while (posFoundLow < arraySize && metric[posFoundLow] < high) {
                counter++;
                posFoundLow++;
            }

            System.out.println("[" + low + "," + high + "] :" + counter);

            //System.out.println("Continue?(1,0=exit): ");
            //res = scanIn.nextInt();
        } while (res != 0);
        scanIn.close();

    }

    public static void calculateNumberOfUsersInRanges() throws FileNotFoundException, IOException {

        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results2-filtered-retOverTwo.txt"));
        Scanner scanIn = new Scanner(System.in);

        int col;
        do {
            System.out.print("Column number (3=min,4=max,5=avg,6=iqr): ");
            col = scanIn.nextInt();
        } while (col != 3 && col != 4 && col != 5 && col != 6); //3,4: <= || 5,6: < [hard coded]

        int res = 1;
        do {
            double high = 0, low = 0;
            reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results2-filtered-retOverTwo.txt"));
            String line = reader.readLine(); //header

            System.out.print("Insert range: ");
            low = scanIn.nextDouble();
            high = scanIn.nextDouble();

            HashMap<Long, Double> map = new HashMap<>(); //UserID - NoOfOccurences
//            HashMap<Long, Integer> minRT = new HashMap<>(); //i=3
//            HashMap<Long, Integer> maxRT = new HashMap<>(); //i=4
//            HashMap<Long, Double> avgRT = new HashMap<>();  //i=5
//            HashMap<Long, Double> iqrRT = new HashMap<>(); //i=6

            line = reader.readLine();
            while (line != null) {
                String[] tokens = line.split("\\s+");
                Long userID = Long.parseLong(tokens[1]);
                if (Double.parseDouble(tokens[col]) >= low && Double.parseDouble(tokens[col]) < high) { //careful with limits!
                    if (map.containsKey(userID)) {
                        map.replace(userID, map.get(userID) + 1);
                    } else {
                        map.put(userID, 1.0);
                    }
                }

                line = reader.readLine();
            }

            System.out.println("[" + low + "," + high + "] :" + map.size());

//            for (Map.Entry pair : map.entrySet()) {
//                if ((Double)pair.getValue() > 10.0)
//                    System.out.println(pair.getKey() + " " +pair.getValue());
//            } 
            reader.close();
        } while (res != 0);

        scanIn.close();

    }

    public static void calculateMinMaxOfFeatures() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results2-filtered-retOverTwo.txt"));
        //header
        String line = reader.readLine();
        double max = 0;
        double min = 12312321;
        //body
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            if (Double.parseDouble(tokens[6]) > max) {
                max = Double.parseDouble(tokens[6]);
            }
            if (Double.parseDouble(tokens[6]) < min) {
                min = Double.parseDouble(tokens[6]);
            }
            line = reader.readLine();
        }
        reader.close();
        System.out.println(min + "-" + max);
    }

    public static void countUniqueUsersWithOverTwoRetweetsInTimeFrames() throws FileNotFoundException, IOException {
        HashSet<Long> ids = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\newData\\results2-filtered-retOverTwo.txt"));
        //header
        String line = reader.readLine();
        //body
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            ids.add(Long.parseLong(tokens[1]));
            line = reader.readLine();
        }
        reader.close();
        System.out.println(ids.size());
    }

    public static void keepUsersWithOverTwoRetweetsInTimeFrames() throws FileNotFoundException, IOException {
        writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\newData\\results12-filtered-retOverTwo.txt");
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\data\\results12-filtered-retOnly.txt"));
        //header
        String line = reader.readLine();
        writer.println(line);
        //body
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split("\\s+");
            if (Integer.parseInt(tokens[2]) > 2) {
                writer.println(line);
            }
            line = reader.readLine();
        }
        reader.close();
        writer.close();
    }

    public static void findUsersWithLowEntropy() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\run2_all_final_entropy.csv"));
        writer = new PrintWriter("badUsersLowEntropyIDS.txt", "UTF-8");
        String line = reader.readLine();
        line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",");
            if ((Double.parseDouble(tokens[tokens.length - 1]) <= 2) && ((Double.parseDouble(tokens[5]) >= 35) || (Double.parseDouble(tokens[6]) >= 35))) {
                writer.println(tokens[0]);
            }

            line = reader.readLine();
        }
        reader.close();
        writer.close();
    }

    public static void normalizeEntropy() throws FileNotFoundException, IOException {

        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\run1_black.csv"));
        PrintWriter writer = new PrintWriter("norm.csv", "UTF-8");
        writer.println(reader.readLine());
        String line = reader.readLine();
        while (line != null) {
            String[] tokens = line.split(",");
            double normed = 0;
            if (Math.log((Double.parseDouble(tokens[5]) + Double.parseDouble(tokens[6]))) != 0) {
                normed = Double.parseDouble(tokens[tokens.length - 1]) / Math.log((Double.parseDouble(tokens[5]) + Double.parseDouble(tokens[6])));
            } else {
                normed = -11;
                System.out.println("MIDEN REEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
            }
            System.out.println(tokens[tokens.length - 1] + " / log(" + tokens[5] + "+" + tokens[6] + ")" + " = " + normed);

            int i = 0;
            for (String token : tokens) {
                if (i == tokens.length - 1) {
                    writer.print(normed + "\n");
                } else {
                    writer.print(token + ",");

                }
                i++;
            }

            line = reader.readLine();
        }
        reader.close();
        writer.close();
    }

    public static void getSample() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\run2_white_final_entropy.csv"));
        writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\run2_white_entropy_sample.csv");
        String line = reader.readLine();
        writer.println(line);
        List<String> lines = new ArrayList<String>();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        Random r = new Random();
        for (int i = 0; i < 1310; i++) { //1097 - 1310
            String randomLine = lines.get(r.nextInt(lines.size()));
            writer.println(randomLine);
        }
        reader.close();
        writer.close();
    }

    public static void getSameOct2015Sample() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\run1_white_sample1.csv"));
        writer = new PrintWriter("run1_white_sample_oct15.csv");
        String line = reader.readLine();
        List<String> lines = new ArrayList<String>();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        for (int i = 0; i < lines.size(); i++) {
            String randomLine = lines.get(i);
            String[] tokens = randomLine.split(",");
            String newLine = tokens[0] + ","
                    + tokens[5] + ","
                    + tokens[9] + ","
                    + tokens[10] + ","
                    + tokens[11] + ","
                    + tokens[12] + ","
                    + tokens[13] + ","
                    + tokens[14] + ","
                    + tokens[15] + ","
                    + tokens[16] + ","
                    + tokens[17] + ","
                    + tokens[18] + ","
                    + tokens[19] + ","
                    + tokens[21] + ","
                    + tokens[22];

            writer.println(newLine);
        }
        writer.close();
    }

    public static void getOct2015Sample() throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\sokpa\\Desktop\\run1_white_final.csv"));
        writer = new PrintWriter("run1_white_sample_oct15.csv");
        String line = reader.readLine();
        List<String> lines = new ArrayList<String>();
        while (line != null) {
            lines.add(line);
            line = reader.readLine();
        }
        Random r = new Random();
        for (int i = 0; i < 1097; i++) {
            String randomLine = lines.get(r.nextInt(lines.size()));
            String[] tokens = randomLine.split(",");
            String newLine = tokens[0] + ","
                    + tokens[5] + ","
                    + tokens[9] + ","
                    + tokens[10] + ","
                    + tokens[11] + ","
                    + tokens[12] + ","
                    + tokens[13] + ","
                    + tokens[14] + ","
                    + tokens[15] + ","
                    + tokens[16] + ","
                    + tokens[17] + ","
                    + tokens[18] + ","
                    + tokens[19] + ","
                    + tokens[21] + ","
                    + tokens[22];

            writer.println(newLine);
        }
        writer.close();
    }

    public static void fixTextProblem() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        File file = new File("C:\\Users\\sokpa\\Documents\\NetBeansProjects\\Test\\newtry.txt");
        writer = new PrintWriter("pameMagkes.txt", "UTF8");
        BufferedReader reader = null;
        printHeader();
        try {
            reader = new BufferedReader(new FileReader(file));
            String text;
            double num = -100;
            long id = -100;
            while ((text = reader.readLine()) != null) {
                System.out.println(text);
                String[] tokens = text.split("\\s+");
                try {
                    num = Double.parseDouble(tokens[2]);
                    writer.print(num + " ");

                } catch (java.lang.NumberFormatException ex) {
                    writer.print("\n");
                    int i = 0;
                    while (tokens[2].charAt(i) != '=') {
                        System.out.println(tokens[2].charAt(i) + "!= =");
                        i++;
                    }
                    num = Double.parseDouble(tokens[2].substring(0, i));
                    writer.print(num + " ");
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

        writer.close();
    }

    public static void october2015users() throws FileNotFoundException, URISyntaxException, UnsupportedEncodingException, IOException, JSONException {
        File dir = new File("C:\\Users\\sokpa\\Desktop\\outliersOctober2015\\outliers_withk8_tweets");
        writer = new PrintWriter("please.txt", "UTF8");

        printHeader();
        for (File file : dir.listFiles()) {
            String[] tokens = file.getName().split("_");
            Long userID = Long.parseLong(tokens[0]);
            TwitterUser user = new TwitterUser(userID);
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("\n");
            while (scanner.hasNext()) {
                String line = scanner.next();
                tokens = line.split("\t");
                user.addTweet(tokens[1]);
            }
            user.checkTweetsDetails();

            scanner.close();
            user.sout();
            printUser(user);
        }

        writer.close();
    }

    public static void printHeader() {
        writer.print("id tweets hashtags avgHTperTweet percTweetsWithHT mentions avgMentionsPerTweet percTweetsWithMentions urls avgURLsPerTweet percTweetsWithURLs urlRatio urlDomainRatio compression entropy");

    }

    public static void printUser(TwitterUser user) {
        writer.printf("%d %d %d %f %f %d %f %f %d %f %f %f %f %f %f\n",
                user.getId(), user.getTweets(), user.getHashtags(), user.getAvgHTperTweet(),
                user.getPercentageTweetsWithHT(), user.getMentions(), user.getAvgMentionsPerTweet(),
                user.getPercentageTweetsWithMentions(), user.getUrls(), user.getAvgURLsPerTweet(),
                user.getPercentageTweetsWithURL(), user.getUrlRatio(), user.getUrlDomainRatio(),
                user.getCompressionRatio(), user.getEntropy());
    }

    private static void findNegatives() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        File file = new File("C:\\Users\\sokpa\\Desktop\\run2_black_final.csv");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();

            while ((text = reader.readLine()) != null) {
                String[] tokens = text.split(",");
                double write;
                for (int i = 0; i < tokens.length; i++) {
                    write = Double.parseDouble(tokens[i]);
                    if (write <= 0) {
                        System.out.println(tokens[0]);
                        break;
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
    }

    private static void deleteNegatives() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        File file = new File("C:\\Users\\sokpa\\Desktop\\run2_white_final.csv");
        BufferedReader reader = null;
        PrintWriter writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\run2_white_final2.csv", "UTF8");

        HashSet<Long> negatives = new HashSet<>();
        File file2 = new File("C:\\Users\\sokpa\\Desktop\\users.txt");
        BufferedReader reader2 = null;

        try {
            reader2 = new BufferedReader(new FileReader(file2));
            String text;
            while ((text = reader2.readLine()) != null) {
                negatives.add(Long.parseLong(text));
            }
        } finally {
            try {
                if (reader2 != null) {
                    reader2.close();
                }
            } catch (IOException e) {
            }
        }
        System.out.println(negatives.size());

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();

            while ((text = reader.readLine()) != null) {
                String[] tokens = text.split(",");
                if (!negatives.contains(Long.parseLong(tokens[0]))) {
                    writer.println(text);
                } else {
                    System.out.println("wtf?");
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
        writer.close();
    }

    private static void addOnes() throws FileNotFoundException, IOException {
        File file = new File("C:\\Users\\sokpa\\Desktop\\run2_black_final_entropy.csv");
        BufferedReader reader = null;
        PrintWriter writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\run2_black_final_entropy_new.csv", "UTF8");

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            writer.println(text);

            while ((text = reader.readLine()) != null) {
                String[] tokens = text.split(",");
                for (int i = 0; i < tokens.length; i++) {
                    double write;
                    try {
                        if (i == (tokens.length - 1)) {
                            write = Double.parseDouble(tokens[i]) + 1.0;
                            writer.print(write + "\n");
                        } else {
                            writer.print(tokens[i] + ",");
                        }
                    } catch (java.lang.NumberFormatException ex) {
                        System.out.println(text);
                    }
                }
            }
        } finally {
            try {
                if (reader != null) {
                    //System.out.println(counter);
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        writer.close();
    }

    private static void findZeros() throws FileNotFoundException, IOException {
        File file = new File("C:\\Users\\sokpa\\Desktop\\blackListed_run1_noBool.csv");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                String[] tokens = text.split(",");
                if ((Integer.parseInt(tokens[1]) == 0) || (Integer.parseInt(tokens[2]) == 0)) {
                    System.out.println("miden!");
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
    }

    private static void write10firstLines(String path, String outpath) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        File file = new File(path);
        BufferedReader reader = null;
        PrintWriter writer = new PrintWriter(outpath, "UTF8");
        String text;
        int counter = 0;

        try {
            reader = new BufferedReader(new FileReader(file));
            while ((text = reader.readLine()) != null && (counter < 10)) {
                writer.println(text);
                counter++;
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        writer.close();
    }

    private static void convertToCSV() throws FileNotFoundException, IOException {
        File file = new File("C:\\Users\\sokpa\\Desktop\\outliersOctober2015\\outliersOct2015features.txt");
        BufferedReader reader = null;
        PrintWriter writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\outliersOctober2015\\outliersOct2015features.csv", "UTF8");

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            String[] tokens = text.split("\\s+");
            for (String token : tokens) {
                if (token.equals("entropy")) {
                    writer.print(token + "\n");
                } else {
                    writer.print(token + ",");
                }
            }
            while ((text = reader.readLine()) != null) {
                tokens = text.split("\\s+");
                for (int i = 0; i < tokens.length; i++) {
                    if (isNumeric(tokens[i]) == true && (i != (tokens.length - 1))) {
                        writer.print(tokens[i] + ",");
                    } else if (i == (tokens.length - 1)) {
                        writer.print(tokens[i] + "\n");
                    } else {

                        if (isNumeric(tokens[i + 1]) == true) {
                            writer.print(tokens[i] + ",");
                        } else if (isNumeric(tokens[i + 2]) == true) {
                            writer.print(tokens[i] + "_" + tokens[i + 1] + ",");
                            i += 1;
                        } else if (isNumeric(tokens[i + 3]) == true) {
                            writer.print(tokens[i] + "_" + tokens[i + 1] + "_" + tokens[i + 2] + ",");
                            i += 2;
                        } else if (isNumeric(tokens[i + 4]) == true) {
                            writer.print(tokens[i] + "_" + tokens[i + 1] + "_" + tokens[i + 2] + "_" + tokens[i + 3] + ",");
                            i += 3;
                        } else if (isNumeric(tokens[i + 5]) == true) {
                            writer.print(tokens[i] + "_" + tokens[i + 1] + "_" + tokens[i + 2] + "_" + tokens[i + 3] + "_" + tokens[i + 4] + ",");
                            i += 4;
                        } else {
                            writer.print(tokens[i] + "_" + tokens[i + 1] + "_" + tokens[i + 2] + "_" + tokens[i + 3] + "_" + tokens[i + 4] + "_" + tokens[i + 5] + ",");
                            i += 5;
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
        writer.close();
    }

    private static void removeBools() throws FileNotFoundException, IOException {
        File file = new File("C:\\Users\\sokpa\\Desktop\\run2_white.txt");
        BufferedReader reader = null;
        PrintWriter writer = new PrintWriter("C:\\Users\\sokpa\\Desktop\\run2_white.csv", "UTF8");

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            String[] tokens = text.split("\\s+");

            //header
            for (int i = 0; i < tokens.length; i++) {
                if (i != 9 && i != 20 && i != 23 && i != 24) { //no verified, prof, profImg
                    if (i != tokens.length - 1) {
                        writer.print(tokens[i] + ",");
                    } else {
                        writer.print(tokens[i] + "\n");
                    }
                }
            }

            //data
            while ((text = reader.readLine()) != null) {
                tokens = text.split("\\s+");
                for (int i = 0; i < 21; i++) {
                    if (i != 9) {
                        if (i != 20) { //source
                            writer.print(tokens[i] + ",");
                        } else {
                            String complete = tokens[tokens.length - 6] + "," //urlDomainRatio
                                    + tokens[tokens.length - 5] + "," //timeFollowed
                                    + tokens[tokens.length - 2] + "," //compression 
                                    + tokens[tokens.length - 1] + "\n"; //entropy
                            writer.print(complete);
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
        writer.close();
    }

    private static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private static void writeColumns(String path, int c1, int c2) throws FileNotFoundException, IOException {
        File file = new File(path);
        BufferedReader reader = null;
        String text;
        PrintWriter writer = new PrintWriter("col" + c1 + "-" + c2 + ".txt", "UTF8");

        try {
            reader = new BufferedReader(new FileReader(file));
            while ((text = reader.readLine()) != null) {
                String[] tokens = text.split("\\s+");
                writer.println(tokens[c1] + "\t" + tokens[c2]);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
        writer.close();
    }

    private static void splitWriteFromBlackList() throws FileNotFoundException, IOException {
        HashSet<Long> black_list = new HashSet<>();
        File file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\outliers\\raw1\\black_list.txt");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            while ((text = reader.readLine()) != null) {
                black_list.add(Long.parseLong(text));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        //take the records of black listed people
        PrintWriter writer = new PrintWriter("blackListed_run1.txt", "UTF8");
        PrintWriter writer2 = new PrintWriter("whiteListed_run1.txt", "UTF8");
        file = new File("C:\\Users\\sokpa\\Desktop\\run1_features.txt");

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine();
            writer.println(text);
            writer2.println(text);
            while ((text = reader.readLine()) != null) {
                String[] tokens = text.split("\\s+");
                if (black_list.contains(Long.parseLong(tokens[0]))) {
                    writer.println(text);
                } else {
                    writer2.println(text);
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
        writer.close();
        writer2.close();
    }

    private static void findCommonsAndDifferents(String rawName, String normName, boolean flag) throws IOException {
        HashSet<Long> all = new HashSet<>();
        HashSet<Long> common = new HashSet<>();
        HashSet<Long> raw = new HashSet<>();
        HashSet<Long> onlyRaw = new HashSet<>();
        HashSet<Long> norm = new HashSet<>();
        HashSet<Long> onlyNorm = new HashSet<>();

        File rawFile = new File("raw12\\" + rawName + ".txt");
        File normFile = new File("norm12\\" + normName + ".txt");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(rawFile));
            String text;
            if (!flag) {
                text = reader.readLine();
            } else {
                text = reader.readLine();
                text = reader.readLine();
            }

            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                all.add(Long.parseLong(splited[0]));
                raw.add(Long.parseLong(splited[0]));
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        try {
            reader = new BufferedReader(new FileReader(normFile));
            String text;
            if (!flag) {
                text = reader.readLine();
            } else {
                text = reader.readLine();
                text = reader.readLine();
            }
            while ((text = reader.readLine()) != null) {
                String[] splited = text.split("\\s+");
                all.add(Long.parseLong(splited[0]));
                norm.add(Long.parseLong(splited[0]));

                if (raw.contains(Long.parseLong(splited[0]))) {
                    common.add(Long.parseLong(splited[0]));
                } else {
                    onlyNorm.add(Long.parseLong(splited[0]));
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

        for (Long id : raw) {
            if (!norm.contains(id)) {
                onlyRaw.add(id);
            }
        }

        System.out.println(rawName);
        System.out.println("All: " + all.size());
        System.out.println("Common: " + common.size());
        System.out.println("OnlyRaw: " + onlyRaw.size());
        System.out.println("OnlyNorm: " + onlyNorm.size());
        System.out.println();

        // printing
        printHashSet(all, "all" + rawName);
        printHashSet(common, "common" + rawName);
        printHashSet(onlyRaw, "onlyRaw" + rawName);
        printHashSet(onlyNorm, "onlyNorm" + rawName);

    }

    private static void printHashSet(HashSet<Long> users, String name) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(name + ".txt");
        writer.println(users.size());
        for (Long id : users) {
            writer.println(id);
        }
        writer.close();

    }

    private static void joinTwoFiles(String path1, String path2, String result) throws FileNotFoundException, IOException {
        File file = new File(path1);
        BufferedReader reader = null;
        PrintWriter writer = new PrintWriter(result);

        try {
            reader = new BufferedReader(new FileReader(file));
            String text;
            while ((text = reader.readLine()) != null) {
                writer.println(text);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        file = new File(path2);

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = reader.readLine(); //take the header out
            while ((text = reader.readLine()) != null) {
                writer.println(text);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

        writer.close();
    }

    private static void printLines() throws FileNotFoundException, IOException {
        File file = new File("C:\\Users\\sokpa\\Desktop\\newThesis\\data_analysis\\analysis\\data\\results1-filtered-retOnlyGood.txt");
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text;
            while ((text = reader.readLine()) != null) {
                //String[] splited = text.split("\\s+");
                System.out.println(text);
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private static int countLines(String path) throws FileNotFoundException, IOException {
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(path)));
        lnr.skip(Long.MAX_VALUE);
        System.out.println(lnr.getLineNumber() + 1); //Add 1 because line index starts at 0
        // Finally, the LineNumberReader object should be closed to prevent resource leak
        lnr.close();

        return lnr.getLineNumber() + 1;
    }

    private static void printFile(String path) throws UnsupportedEncodingException, FileNotFoundException, IOException {
        File file = new File(path);
        BufferedReader reader = null;
        String text;
        int counter = 0;

        try {
            reader = new BufferedReader(new FileReader(file));
            while ((text = reader.readLine()) != null && (counter < 10)) {
                System.out.println(text);
                counter++;
            }
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }
    }

}
