import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.awt.Canvas;
import java.awt.Graphics;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class ShapeSearch extends Canvas {

    // Constant to increase the size of the display by
    static int ENLARGEMENT = 2;

    // Array of shapes is global to allow main and paint method access
    static DrawingDimensions[] toBeDrawn;
    static int boxWidth;

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

                // Put the larger value into width to help with sorting the initial order
                if (width < height) {
                    shapes[i] = (new Shape(height, width));
                } else {
                    shapes[i] = (new Shape(width, height));
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
     * Find the largest y value which a shape touches.
     *
     * @param yBottomLine an integer array that holds all the y values that make up the bottom line of the shape stack.
     *
     * @return the greatest y value.
     */
    private static int getLargestY(int[] yBottomLine){
        int yLargest = 0;
        // Find the greatest value in yBottomLine
        for (int y: yBottomLine) {
            if(y > yLargest) {
                yLargest = y;
            }
        }
        // Calculate and return space used
        return yLargest;
    }

    /**
     * Calculates the area needed to fit all the shapes on.
     *
     * @param yBottomLine an integer array that holds all the y values that make up the bottom line of the shape stack.
     *
     * @return the greatest y value times the box width.
     */
    private static int getUsedSpace(int[] yBottomLine){
        int yLargest = getLargestY(yBottomLine);
        // Calculate and return space used
        return yLargest * boxWidth;
    }

    /**
     * Prints a message and then the shapes out in order.
     * @param shapes the list of all shapes.
     * @param Message to be displayed above the list of shapes.
     */
    private static void printShapeOrder(Shape[] shapes, String Message){
        System.out.println(Message);
        // Print out the first ten shapes
        for (int i = 0; i < shapes.length; i++) {
            System.out.println(shapes[i].toString());
        }
    }

    /**
     * Fits shapes onto the sheet
     * @param toAdd index numbers of shapes to add
     * @param shapes the list of shapes
     * @return the y values of the end shapes
     */
    private static int[] fitShape(List<Integer> toAdd, Shape[] shapes){

        int x = 0, toBeDrawnIndex = 0, passes = 0;
        int[] yBottomLine = new int[boxWidth]; // Record the y values along the top of all added shapes
        toBeDrawn = new DrawingDimensions[shapes.length];

        boolean reverse = false;
        // Until all shapes are added
        while (0 < toAdd.size()) {

            //If passes exceed 1 then move x across
            if (1 < passes) {
                if (reverse) {
                    if (x > 0) {
                        int currY = yBottomLine[x];
                        for (; x >= 0; x--) {
                            if (yBottomLine[x] > currY) {
                                break;
                            }
                        }
                    }

                    if (x <= 0) {
                        x = 0;
                        reverse = false;
                    }
                } else {
                    // If x is at end then reverse direction and set x to the edge
                    if (x < boxWidth) {
                        int currY = yBottomLine[x];
                        for (; x < boxWidth; x++) {
                            if (yBottomLine[x] > currY) {
                                break;
                            }
                        }
                    }

                    if (x >= boxWidth) {
                        x = boxWidth - 1;
                        reverse = true;
                    }
                }
            }

            // For each shape left to add
            for (int i = 0; i < toAdd.size(); i++) {

                // Get the shape's dimensions
                int index = toAdd.get(i);
                int width = shapes[index].getWidth();
                int height = shapes[index].getHeight();

                // Calculate width to fit shape into
                int widthToFitIn = 0;
                if (reverse) {
                    for (int j = x; j >= 0 && yBottomLine[j] <= yBottomLine[x]; j--) {
                        widthToFitIn++;
                    }
                } else {
                    for (int j = x; j < boxWidth && yBottomLine[j] <= yBottomLine[x]; j++) {
                        widthToFitIn++;
                    }
                }

                // Rotate to fit
                boolean rotate = false;

                    /*
                    // If width does not fit but height does then rotate
                    if (width > widthToFitIn && height <= widthToFitIn) {
                        rotate = true;
                        width = height;
                        height = shapes[index].getWidth();
                    }
                    */

                // If the shape can fit
                if (width <= widthToFitIn) {

                    // Set the variable of the x value for the shape to be drawn from
                    int shapeX = x;
                    // If reversed then we need to adjust x as we are looking at the right edge of where the shape will go
                    if (reverse) {
                        shapeX -= width;
                    }

                    // Add the shape
                    toBeDrawn[toBeDrawnIndex] = new DrawingDimensions(shapeX, yBottomLine[x], index, shapes[index], rotate);
                    toBeDrawnIndex++;
                    int newY = yBottomLine[x] + height;

                    if(reverse){
                        // Adjust yBottomLine
                        for (int whereShapePlaced = x; whereShapePlaced > x - width; whereShapePlaced--) {
                            yBottomLine[whereShapePlaced] = newY;
                        }

                        // Adjust x
                        x -= width;

                        // Move x back to fill in gaps
                        while (x < boxWidth - 2 && x >= 0) {
                            if (yBottomLine[x] < yBottomLine[x + 1]) {
                                break;
                            }
                            x++;
                        }

                    } else {

                        // Adjust yBottomLine
                        for (int whereShapePlaced = x; whereShapePlaced < x + width; whereShapePlaced++) {
                            yBottomLine[whereShapePlaced] = newY;
                        }

                        // Adjust x
                        x += width;

                        // Move x back to fill in gaps
                        while (x > 0 && x < boxWidth) {
                            if (yBottomLine[x] < yBottomLine[x - 1]) {
                                break;
                            }
                            x--;
                        }
                    }


                    // A shape has been added so reset passes counter
                    passes = 0;

                    // Remove shape from the list to add
                    toAdd.remove(i);
                    //i--;
                    break;
                }
            }

            passes++;
        }
        return yBottomLine;
    }

    /**
     * Sets up the graphical display
     *
     * @param boxHeight height of the frame to be displayed in
     */
    private static void setUpGraphic(int boxHeight){
        JFrame frame = new JFrame("ShapeSearch Graphic");
        Canvas canvas = new ShapeSearch();
        canvas.setSize(boxWidth * ENLARGEMENT, boxHeight * ENLARGEMENT); // Increased in size to make the shapes easier to see
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Creates the graphical display of the shapes being fit to the sheet.
     *
     * @param g the graphical object that the shapes will be drawn onto.
     */
    public void paint(Graphics g) {
        try {
            for (int i = 0; i < toBeDrawn.length; i++) {
                // Draw the shape
                toBeDrawn[i].draw(g, ENLARGEMENT);
                // Wait so we can see each shape being added
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //start timing program
        final long initialTime = System.nanoTime();
        //-----------------------------------------------------------------------------

        // The default values that are used if arguments are not given
        int columnNumber = 1;
        String filePath = "ShapeLists/GivenLists.csv";
        boolean limitToTen = false;

        // Allow other shape lists to be selected
        if (args.length <= 2) {
            columnNumber = Integer.parseInt(args[0]);
            filePath = "ShapeLists/" + args[1];

            // Allow extra options to be selected
            if (args.length <= 3) {
                // Check for matching options in the 3rd or later arguments
                for (int i = 2; i < args.length; i++) {
                    if (args[i] == "limit") {
                        limitToTen = true;
                    }
                }
            }
        }

        Shape[] shapes = readCSV(filePath, columnNumber, limitToTen); //set to limit input to 10 shapes maximum

        // Sort the array of shapes from largest area first to smallest area last
        Arrays.sort(shapes, Collections.reverseOrder());

        //printShapeOrder(shapes, "Initial Order");



        // Create a list of indexes of the shapes to add so we can remove shapes from this list as they get added
        ArrayList<Integer> toAdd = new ArrayList<Integer>();
        for (int i = 0; i < shapes.length; i++) {
            toAdd.add(i);
        }

        // The shapes fitted to the sheet will be stored to global variable toBeDrawn
        int[] yBottomLine = fitShape(toAdd, shapes);

        //-------------------------------------------------------------------------------------
        //finish timing program
        long finalTime = System.nanoTime();
        //Please do not remove or change the format of this output message
        System.out.println("Processed " + shapes.length + " shapes in " + (finalTime - initialTime) / 1E9 + " secs.");

        // Report how much space was used to fit all the shapes
        System.out.println("Used space = " + getUsedSpace(yBottomLine));
        // Set up the graphical display
        setUpGraphic(getLargestY(yBottomLine) + 10);
    }
}