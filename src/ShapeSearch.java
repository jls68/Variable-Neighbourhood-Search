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

    static final int OPTIONS = 6;
    static int boxWidth;
    static int k;
    static String kType;

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
        // Print out fitting option settings
        System.out.print("Fitting option settings are: ");
        for (Boolean opt: x._options) {
            System.out.print(opt.toString() + " ");
        }
        System.out.println();
        System.out.println("Used space = " + x.getScore() * boxWidth);
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


    private static Solution VND(Solution xBest, int kMax){
        k = 1;
        do{
            // Find the best neighbor in neighborhood k
            Solution xNew;
            if(kType.equals("OptionsAndKMove")){
                xNew = xBest.getBestInNeighborhood(k, "kMove");
                xNew = xNew.getBestInNeighborhood(k, "Options");
            }
            else if(kType.equals("OptionsAndRandomMove")){
                xNew = xBest.getBestInNeighborhood(k, "RandomMove");
                xNew = xNew.getBestInNeighborhood(k, "Options");
            }
            else {
                xNew = xBest.getBestInNeighborhood(k, kType);
            }
            // Change neighborhood
            xBest = NeighbourhoodChange(xBest, xNew);
        } while (k < kMax);
        return xBest;
    }

    public static void main(String[] args) {

        //start timing program
        final long initialTime = System.nanoTime();
        //-----------------------------------------------------------------------------

        // The default values that are used if arguments are not given
        int columnNumber = 1;
        String filePath = "ShapeLists/GivenLists.csv";
        boolean limitToTen = false;
        kType = "";

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
                    else if(args[i].equals("RandomMove") || args[i].equals("Options") || args[i].equals("OptionsAndKMove") || args[i].equals("OptionsAndRandomMove") ){
                        kType = args[i];
                    }
                }
            }
        }

        Shape[] shapes = readCSV(filePath, columnNumber, limitToTen); //set to limit input to 10 shapes maximum

        // Sort the array of shapes from largest area first to smallest area last
        Arrays.sort(shapes, Collections.reverseOrder());

        //printShapeOrder(shapes, "Initial Order");

        // Initialise fitting options for the initial solution as all false
        boolean[] options = new boolean[OPTIONS];
        // This seem to be the optimal settings
        options[1] = true;
        options[2] = true;
        options[3] = true;
        //options[4] = true; // This option doesn't seem to have much affect

        // Create initial solution with order to add shapes, the shapes, the options to fit, and the box width
        Solution xBest = new Solution(shapes, options, boxWidth);
        System.out.println("First fit used an area of " + xBest.getScore());

        // Variable Neighbourhood Descent
        xBest = VND(xBest, shapes.length - 1);

        //-------------------------------------------------------------------------------------
        //finish timing program
        long finalTime = System.nanoTime();
        //Please do not remove or change the format of this output message
        System.out.println("Processed " + shapes.length + " shapes in " + (finalTime - initialTime) / 1E9 + " secs.");

        // Report how much space was used to fit all the shapes


        printSummary(xBest, filePath + " " + columnNumber + " " + kType);

        // Set up the graphical display
        new GraphicalDisplay(boxWidth, xBest.getScore() + 5, xBest.getDrawDimensions());
    }
}