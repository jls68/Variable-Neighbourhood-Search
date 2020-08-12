import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

public class ShapeSearch extends Canvas {

    static int boxWidth;
    static int k;

    /**
     * Reads the shapes in from a csv file
     *
     * @param   filePath: must be of the form "___.csv"
     *          columnNumber: which column of the CSV is to be read
     *          limitToTen: boolean that controls whether input is limited for debug purposes
     *
     * @return  a 1D array of shapes, either limited to 10 or else the size of the CSV column
     */
    private static Shape[] readCSV(String filePath, int columnNumber, boolean limitToTen) {
        Shape[] shapes = null;

        int column = (columnNumber - 1) * 4;

        // Parse CSV file into BufferedReader
        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            // Process header
            String line = br.readLine();
            String[] cells = line.split(",");

            //extract name from header
            String name = cells[column + 2];
            if(name.equals("M1a")){
                boxWidth = 40;
            }
            else{
                boxWidth = 100;
            }

            line = br.readLine();
            cells = line.split(",");

            //extract size from header
            int size = Integer.parseInt(cells[column + 2]);

            line = br.readLine();
            cells = line.split(",");
            int itemArea = Integer.parseInt(cells[column + 2]);

            // Skip the next two lines
            br.readLine();
            br.readLine();

            //if boolean is toggled, limit number of inputs
            if (limitToTen) {
                size = 10;
            }

            shapes = new Shape[size];

            // Process each shape
            for (int i = 0; i < size; i++) {
                // Convert each line of the CSV file into a shape object
                line = br.readLine();
                cells = line.split(",");
                int width = Integer.parseInt(cells[column + 1]);
                int height = Integer.parseInt(cells[column + 2]);

                // Make sure the shapes has a width that can fit
                if(width > boxWidth){
                    // If height can't fit either then we can't use that shape
                    if(height > boxWidth) {
                        System.out.println("The shape " + i + " has a height and width greater than " + boxWidth);
                        // We add an empty shape so we can still use the rest of the list
                        shapes[i] = (new Shape("Missing", 0, 0));
                    }
                    else{
                        shapes[i] = (new Shape(cells[column], height, width));
                    }
                }
                // Put the larger value into width to help with sorting the initial order
                else if (width < height) {
                    shapes[i] = (new Shape(cells[column], height, width));
                } else {
                    shapes[i] = (new Shape(cells[column], width, height));
                }
            }
            //after reading all the shapes, we are done with using the bufferedreader
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return shapes;
    }

    /**
     * Prints a message and then the shapes out in order.
     * @param x the solution to print details about.
     * @param Message to be displayed above the list of shapes.
     */
    private static void printSummary(Solution x, String Message){
        System.out.println(Message);
        // Print out the order of shapes
        System.out.print("Shape order to add: ");
        for (Shape s: x._shapesOrder) {
            System.out.print(s.getId() + " ");
        }
        System.out.println();
        System.out.println("Used space = " + (x.getScore() * boxWidth));
    }

    /**
     * This function either resets k to 1 and makes a move to a better current best solution ot increments k
     * @param xBest the current best solution
     * @param xNew the next solution to move to
     * @return the best of the two solutions
     */
    private static Solution NeighbourhoodChange(Solution xBest, Solution xNew){
        if(xNew.score < xBest.score){
            xBest = xNew;
            System.out.println("New fit at k=" + k + " with an area of " + xBest.getScore());
            k = 1;
        }
        else{
            k++;
        }
        return xBest;
    }

    /**
     * Variable Neighbourhood Descent
     * Check all neighbourhoods and return best solution
     *
     * @param x the current  solution
     * @param kMax teh max k neighbour to search up to
     * @return best solution in neighbourhoods
     */
    private static Solution VND(Solution x, int kMax){
        k = 1;
        do{
            // Find the best neighbor in neighborhood k
            Solution xNew = x.getBestInNeighborhood(k);
            // Change neighborhood
            x = NeighbourhoodChange(x, xNew);
        } while (k < kMax);
        return x;
    }

    /**
     * Reduced Variable Neighbourhood Search
     * Repeatedly iterates over neighbourhoods, picking a single random solution from each neighbourhood, until a fixed amount of time expires
     *
     * @param x the current solution
     * @param kMax the max neighbourhoods to test
     * @param tMax the max amount of time to search for the best solution
     * @return the best solution found
     */
    private static Solution RVNS(Solution x, int kMax, long tMax){
        long t;
        // Get current time to help measure how long the RVNS runs for.
        long tStart = System.nanoTime();
        do{
            k = 1;
            do{
                Solution xShook = x.Shake(k);
                x = NeighbourhoodChange(x, xShook);
            } while (k < kMax);
            // Save current CPU time minus initial start time into t for the elapsed time
            t = System.nanoTime() - tStart;
        } while (t < tMax);
        return x;
    }

    public static void main(String[] args) {

        // The default values that are used if arguments are not given
        int seconds = 1;
        int seed = 0;
        int columnNumber = 1;
        String filePath = "ShapeLists/GivenLists.csv";
        boolean limitToTen = false;
        int kMax;

        // Allow other shape lists to be selected
        if (args.length >= 2) {
            columnNumber = Integer.parseInt(args[0]);
            filePath = args[1];

            // Allow extra options to be selected
            if (args.length >= 3) {
                // Check for matching options in the 3rd or later arguments
                for (int i = 2; i < args.length; i++) {
                    if (args[i].equals("limit")) {
                        limitToTen = true;
                    }
                    else if (args[i].equals("t-") && i + 1 < args.length){
                        try {
                            seconds = Integer.parseInt(args[i + 1]);
                        } catch(Exception e){
                            System.out.println("To set the time limit for RVNS add the argument 't-' followed by the number of seconds in the next argument");
                        }
                    }
                    else if (args[i].equals("S-") && i + 1 < args.length){
                        try {
                            seed = Integer.parseInt(args[i + 1]);
                        } catch(Exception e){
                            System.out.println("To set the random seed for random move add the argument 'S-' followed by the seed number in the next argument");
                        }
                    }
                }
            }
        }

        Shape[] shapes = readCSV(filePath, columnNumber, limitToTen); //set to limit input to 10 shapes maximum

        // kMax equals the number of shapes times the number of different changes that could be made
        kMax = shapes.length * 3;

        // Sort the array of shapes from largest area first to smallest area last
        Arrays.sort(shapes, Collections.reverseOrder());

        // Create initial solution with order to add shapes, the shapes, the options to fit, and the box width
        Solution xBest = new Solution(shapes, boxWidth, seed);
        System.out.println("First fit used an area of " + xBest.getScore());


        //-----------------------------------------------------------------------------------VND start
        //start timing program
        long initialTime = System.nanoTime();

        // Variable Neighbourhood Descent
        xBest = VND(xBest, kMax);

        //finish timing program
        long finalTime = System.nanoTime();
        //-------------------------------------------------------------------------------------VND end

        //Please do not remove or change the format of this output message
        System.out.println("Processed " + shapes.length + " shapes in " + (finalTime - initialTime) / 1E9 + " secs.");

        // Report how much space was used to fit all the shapes
        printSummary(xBest, "VND: " + filePath + " " + columnNumber);

        //---------------------------------------------------------------------------------------RVNS start
        // Second search method to compare against
        Solution xBestSecond = new Solution(shapes, boxWidth, seed);
        System.out.println("First fit used a height of " + xBestSecond.getScore());

        //start timing program
        initialTime = System.nanoTime();

        // Reduced Variable Neighbourhood Search
        xBestSecond = RVNS(xBestSecond, kMax, (long) (1E9 * seconds));

        //finish timing program
        finalTime = System.nanoTime();
        //---------------------------------------------------------------------------------------RVNS end

        //Please do not remove or change the format of this output message
        System.out.println("Processed " + shapes.length + " shapes in " + (finalTime - initialTime) / 1E9 + " secs.");

        // Report how much space was used to fit all the shapes
        printSummary(xBestSecond, "RVNS: " + filePath + " " + columnNumber);

        if(xBestSecond.getScore() < xBest.getScore()){
            xBest = xBestSecond;
            System.out.println("RVNS best found solution being drawn");
        }
        else{
            System.out.println("VND best found solution being drawn with an area of " + xBest.getScore() * boxWidth);
        }

        // Set up the graphical display
        new GraphicalDisplay(boxWidth, xBest.getScore() + 5, xBest.getDrawDimensions());
    }
}