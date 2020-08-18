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

    static boolean debug = false;
    static int boxWidth;
    static int k;
    static int cost;
    enum Method {
        VND,
        RVNS,
        BVNS,
        GVNS,
        SVNS
    }

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
    private static void printSummary(Solution[] x, long[] runLengths, int[] runCosts, int i, String Message){
        if(debug) {
            // Print out the order of shapes
            System.out.print("Shape order to add: ");
            for (Shape s : x[i].getOrder()) {
                System.out.print(s.getId() + " ");
            }
            System.out.println();
        }

        long aveTime =runLengths[0];
        int lowT = 0;
        int aveCost = runCosts[0];
        int lowC = 0;
        // Find the average and lowest of cost
        for (int j = 1; j < runCosts.length; j++){
            aveTime += runLengths[j];
            aveCost += runCosts[j];
            if(runCosts[j] < runCosts[lowC]){
                lowC = j;
            }
            if(runLengths[j] < runLengths[lowT]){
                lowT = j;
            }
        }
        aveTime /= runLengths.length;
        aveCost /= runCosts.length;

        // Report the best result
        System.out.println(Message + " found the least area of " + (x[i].getScore() * boxWidth + " after searching " + runCosts[i] + " solutions over " + runLengths[i] / 1E9 + " seconds."));

        // Report average and lowest cost
        System.out.println("The average cost was " + aveCost + " with the lowest cost being " + runCosts[lowC] + " from run " + (lowC + 1));

        // Report average and lowest run length
        System.out.println("The average run length was " + aveTime / 1E9 + " with the lowest run length being " + runLengths[lowT] / 1E9 + " from run " + (lowT + 1));

        System.out.println();
    }

    /**
     * This function either resets k to 1 and makes a move to a better current best solution ot increments k
     * @param xBest the current best solution
     * @param xNew the next solution to move to
     * @return the best of the two solutions
     */
    private static Solution NeighbourhoodChange(Solution xBest, Solution xNew){
        if(xNew.getScore() < xBest.getScore()){
            xBest = xNew;
            if(debug){
                System.out.println("New fit at k=" + k + " with an area of " + xBest.getScore());
            }
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
            // Add to cost the length whenever we search the entire neighbourhood
            cost += x.getOrder().length;
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
                // Add one to cost whenever we find a random neighbourhood
                cost++;
                Solution xShook = x.Shake(k);
                x = NeighbourhoodChange(x, xShook);
            } while (k < kMax);
            // Save current CPU time minus initial start time into t for the elapsed time
            t = System.nanoTime() - tStart;
        } while (t < tMax);
        return x;
    }

    /**
     * Basic Variable Neighbourhood Search
     * @param x the current solution
     * @param kMax the max neighbourhoods to test
     * @param tMax the max amount of time to search for the best solution
     * @return the best solution found
     */
    private static Solution BVNS(Solution x, int kMax, long tMax){
        long t;
        // Get current time to help measure how long the RVNS runs for.
        long tStart = System.nanoTime();
        do{
            k = 1;
            do{
                // Add one to cost whenever we find a random neighbourhood
                cost++;
                Solution xShook = x.Shake(k);                   // Shaking
                Solution xFirst = xShook.FirstImprovment(k);   // Local Search
                // Add to cost whenever we find the neighbourhood with improvement
                cost += xShook.getFirstImprovementCost();
                x = NeighbourhoodChange(x, xFirst);            // Change neighborhood
            } while (k < kMax);
            // Save current CPU time minus initial start time into t for the elapsed time
            t = System.nanoTime() - tStart;
        } while (t < tMax);
        return x;
    }

    /**
     * General Variable Neighbourhood Search
     * @param x the current solution
     * @param lMax the local max neighbourhoods to test in the VND
     * @param kMax the max neighbourhoods to test
     * @param tMax the max amount of time to search for the best solution
     * @return the best solution found
     */
    private static Solution GVNS(Solution x, int lMax, int kMax, long tMax){
        long t;
        // Get current time to help measure how long the RVNS runs for.
        long tStart = System.nanoTime();
        do{
            k = 1;
            do{
                // Add one to cost whenever we find a random neighbourhood
                cost++;
                Solution xShook = x.Shake(k);      // Shaking
                int storedK = k;
                Solution xVND = VND(x, lMax);      // VND
                k = storedK;
                x = NeighbourhoodChange(x, xVND);  // Change neighborhood
            } while (k < kMax);
            // Save current CPU time minus initial start time into t for the elapsed time
            t = System.nanoTime() - tStart;
        } while (t < tMax);
        return x;
    }

    /**
     * Skewed Variable Neighbourhood Search
     *
     * @param xBest the current best solution
     * @param xNew the next solution to move to
     * @param alpha the multiplier to take a distant solution
     * @return the best of the two solutions
     */
    private static Solution NeighbourhoodChangeS(Solution xBest, Solution xNew, double alpha){
        int a = (int)(alpha * p(xBest, xNew));
        if(xNew.getScore() - a < xBest.getScore()){
            xBest = xNew;
            if(debug) {
                System.out.println("Skewed fit at k=" + k + " with an area of " + xBest.getScore());
            }
            k = 1;
        }
        else{
            k++;
        }
        return xBest;
    }

    /**
     * Measure the distance between solutions.
     * @param x1 a solution
     * @param x2 a different solution
     * @return the best solution
     */
    private static int p(Solution x1, Solution x2){
        // Find the distance between the two solutions
        int distance = 0;
        Shape[] s1 = x1.getOrder();
        Shape[] s2 = x2.getOrder();
        // Increase the distance for each different shape
        for (int i = 0; i < s1.length; i++) {
            if(!s1[i].equals(s2[i])){
                distance++;
            }
        }
        return distance;
    }

    /**
     * Skewed Variable Neighbourhood Search
     * @param x the current solution
     * @param kMax the max neighbourhoods to test
     * @param tMax the max amount of time to search for the best solution
     * @param alpha the multiplier to take a distant solution
     * @return the best solution
     */
    private static Solution SVNS(Solution x, int kMax, long tMax, double alpha){
        long t;
        // Get current time to help measure how long the RVNS runs for.
        long tStart = System.nanoTime();
        Solution xBest = x;
        do{
            k = 1;
            do{
                // Add one to cost whenever we find a random neighbourhood
                cost++;
                Solution xShook = x.Shake(k);
                if(xShook.getScore() < 65){
                    System.out.print("");
                }
                Solution xFirst = xShook.FirstImprovment(k);
                // Add to cost whenever we find the neighbourhood with improvement
                cost += xShook.getFirstImprovementCost();
                x = NeighbourhoodChangeS(x, xFirst, alpha);
                if(x.getScore() < xBest.getScore()){
                    xBest = x;
                }
            } while(k < kMax);
            x = xBest;
            // Save current CPU time minus initial start time into t for the elapsed time
            t = System.nanoTime() - tStart;
        } while (t < tMax);
        return x;
    }


    private static Solution runMethod(Method method, int kMax, int tMax, int lMax, double alpha, Shape[] shapes, int seed, String fileInfo, int runs){

        long[] runLengths = new long[runs];
        int[] runCosts = new int[runs];
        Solution[] x = new Solution[runs];
        int bestIndex = 0;

        // Repeat test with a different seed for each run
        for(int i = 0; i < runs; i++){
            // Change the seed value
            seed += 1000;

            //start timing program
            long initialTime = System.nanoTime();

            // Create initial solution with order to add shapes, the shapes, the options to fit, and the box width
            x[i] = new Solution(shapes, boxWidth, seed);
            if (debug) {
                System.out.println("First fit used an area of " + x[i].getScore());
            }
            // Start cost at one as we have created the initial solution
            cost = 1;

            if (method == Method.VND) {
                // Variable Neighbourhood Descent
                x[i] = VND(x[i], kMax);
            } else if (method == Method.RVNS) {
                // Reduced Variable Neighbourhood Search
                x[i] = RVNS(x[i], kMax, tMax);
            } else if (method == Method.BVNS) {
                // Basic Variable Neighbourhood Search
                x[i] = BVNS(x[i], kMax, tMax);
            } else if (method == Method.GVNS) {
                // General Variable Neighbourhood Search
                x[i] = GVNS(x[i], lMax, kMax, tMax);
            } else if (method == Method.SVNS) {
                // Reduced Variable Neighbourhood Search
                x[i] = SVNS(x[i], lMax, tMax, alpha);
            }

            //finish timing program
            long finalTime = System.nanoTime();

            // Keep track of best solution
            if(x[i].getScore() < x[bestIndex].getScore()){
                bestIndex = i;
            }
            else if(x[i].getScore() == x[bestIndex].getScore()){
                if(runCosts[i] < runCosts[bestIndex]){
                    bestIndex = i;
                }
            }

            runLengths[i] = finalTime - initialTime;
            runCosts[i] = cost;

            System.out.println("Run " + (i + 1) + " of " + method + " least area = " + x[i].getScore() + " after testing " + runCosts[i] + " Solutions");
            //Please do not remove or change the format of this output message
            System.out.println("Processed " + x[i].getOrder().length + " shapes in " + (runLengths[i]) / 1E9  + " secs.");
        }

        // Report how much space was used to fit all the shapes
        printSummary(x, runLengths, runCosts, bestIndex, method.toString() + ": " + fileInfo);

        return x[bestIndex];
    }

    public static void main(String[] args) {

        int kMax, lMax;
        // The default values that are used if arguments are not given
        int tMax = 1;
        double alpha = 0.1;
        int seed = 4563;
        int columnNumber = 1;
        String filePath = "ShapeLists/GivenLists.csv";
        boolean limitToTen = false;
        Method thisMethod = null;

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
                    else if (args[i].equals("-t") && i + 1 < args.length){
                        try {
                            tMax = Integer.parseInt(args[i + 1]);
                        } catch(Exception e){
                            System.out.println("To set the time limit for RVNS add the argument 't-' followed by the number of seconds in the next argument");
                        }
                    }
                    else if (args[i].equals("-S") && i + 1 < args.length){
                        try {
                            seed = Integer.parseInt(args[i + 1]);
                        } catch(Exception e){
                            System.out.println("To set the random seed for random move add the argument 'S-' followed by the seed number in the next argument");
                        }
                    } else if (args[i].equals("debug")){
                        debug = true;
                    } else {
                        for (Method m: Method.values()) {
                            if(args[i].equals(m.toString())){
                                thisMethod = m;
                            }
                        }
                    }
                }
            }
        }
        String fileInfo = filePath + " " + columnNumber;

        Shape[] shapes = readCSV(filePath, columnNumber, limitToTen); //set to limit input to 10 shapes maximum

        // kMax equals the number of shapes times the number of different changes that could be made
        kMax = shapes.length * 3;
        lMax = shapes.length / 10;
        // Convert tMax from seconds to nanoseconds
        tMax *= 1E9;

        // Sort the array of shapes from largest area first to smallest area last
        Arrays.sort(shapes, Collections.reverseOrder());

        Solution x = null;
        String bestMethod = "";

        if(thisMethod == null) {

            // Try each different type of search method
            for (Method m : Method.values()) {
                Solution xNew = runMethod(m, kMax, tMax, lMax, alpha, shapes, seed, fileInfo, 10);
                if (x == null || xNew.getScore() < x.getScore()) {
                    x = xNew;
                    bestMethod = m.toString();
                }
            }
        }
        else{
            x = runMethod(thisMethod, kMax, tMax, lMax, alpha, shapes, seed, fileInfo, 10);
            bestMethod = thisMethod.toString();
        }
        
        // State best solution
        System.out.println(bestMethod + " found best solution with an area of " + x.getScore() * boxWidth);

        // Set up the graphical display
        new GraphicalDisplay(boxWidth, x.getScore() + 5, x.getDrawDimensions());
    }
}