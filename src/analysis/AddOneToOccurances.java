package analysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

/**
 *Adds one to each occurance to avoid zeros (0) and then saves them into a new
 * file.
 * 
 * @author Sokratis Papadopoulos
 */
public class AddOneToOccurances {
 
    /**
     * @param args the command line arguments
     * @throws java.io.UnsupportedEncodingException
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws UnsupportedEncodingException, FileNotFoundException {

        PrintWriter writer = new PrintWriter("C:\\Users\\Sokratis\\Documents\\NetBeansProjects\\SpotTheBot\\resultsRetweetsRec2.txt", "UTF-8");        
        Scanner s = new Scanner(new File("C:\\Users\\Sokratis\\Documents\\NetBeansProjects\\SpotTheBot\\resultsRetweetsRec.txt"));

        int num;
        int num2;
        while(s.hasNextInt()){
            num = s.nextInt() +1;
            num2 = s.nextInt();
            writer.println(num + "\t" + num2);
        }

//        float num;
//        int num2;        
//        while(s.hasNextFloat()){
//            num = s.nextFloat() +1;
//            num2 = s.nextInt();
//            writer.println(num + "\t" + num2);
//        }

        writer.close();

    }

}
