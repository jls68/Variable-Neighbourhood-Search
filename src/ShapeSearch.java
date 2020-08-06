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
    static int[] yBottomLine;

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
                        shapes[i] = (new Shape(0, 0));
                    }
                    else{
                        shapes[i] = (new Shape(height, width));
                    }
                }
                // Put the larger value into width to help with sorting the initial order
                else if (width < height) {
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
     * @return the greatest y value.
     */
    private static int getLargestY(){
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
     * Prints a message and then the shapes out in order.
     * @param shapes the list of all shapes.
     * @param Message to be displayed above the list of shapes.
     */
    private static void printShapeOrder(Shape[] shapes, String Message){
        System.out.println(Message);
        // Print out the first ten shapes
        for (Shape s: shapes) {
            System.out.println(s.toString());
        }
    }

    /**
     * Fits shapes onto the sheet
     * @param toAdd index numbers of shapes to add
     * @param shapes the list of shapes
     * @param zigzag alternates the direction shapes are placed
     * @param checkRotation if this is true then try both rotations to fit
     * @param checkForPerfect if this is true then look for a shape that matches the width to fit perfectly
     * @param moveXBack if this is true the next shape could be placed behind the last place shape if it is a perfect fit
     * @param findLowestUntil this is the limit of passes before the lowest y is no longer used first
     * @return the drawing dimensions of teh added shapes
     */
    private static DrawingDimensions[] fitShape(List<Integer> toAdd, Shape[] shapes, boolean zigzag, boolean checkRotation, boolean checkForPerfect, boolean moveXBack, int findLowestUntil){

        int x = 0, toBeDrawnIndex = 0, passes = 0;
        yBottomLine = new int[boxWidth]; // Record the y values along the top of all added shapes
        DrawingDimensions[] toBeDrawn = new DrawingDimensions[shapes.length];

        boolean reverse = false;
        // Until all shapes are added
        while (0 < toAdd.size()) {

            //If passes exceed 1 then move x to next lowest y value
            if (1 < passes) {
                int direction = 1;
                if (reverse) {
                    direction = -1;
                }

                // If x is in the bounds then find next y level to add the shape to
                if(x < boxWidth && x >= 0) {
                    int yOld = yBottomLine[x];
                    // Move across until on a new y level
                    while (x < boxWidth && x >= 0 && yOld == yBottomLine[x]) {
                        x += direction;
                    }

                    // If we have tried to fit into the lowest value too many times then just attempt the next y level instead
                    if(findLowestUntil <= passes){
                        System.out.println("Attempting pass " + passes + " after reaching " + findLowestUntil + " with " + toAdd.size() + " shapes left to add.");
                        if(passes > 100){
                            //At this point the program has failed as it has tried every single possible value along the maximum x axis.
                            System.out.println("The program has failed to fit " + toAdd.size() + " shapes");
                            return toBeDrawn;
                        }
                    }
                    else { // Else attempt to fit a shape in the lowest y level
                        int xNew = x;
                        // Find x coordinate of the first lowest y value on the bottom line
                        for (int i = x; i < boxWidth && i >= 0; i += direction) {
                            // If the current y value at i is less than the y value at the new x coordinate
                            if (yBottomLine[i] < yBottomLine[xNew]) {
                                // Then make i the new x value
                                xNew = i;
                            }
                        }
                        x = xNew;
                    }
                }
                // Next if statement should still happen if the above if statement occurs as the x could move out of bounds

                // If x is out of bounds then change direction and search the other way
                if (reverse && x < 0) {
                    x = 0;
                    reverse = false;
                } else if(x >= boxWidth) {
                    if(zigzag) {
                        x = boxWidth - 1;
                        reverse = true;
                    } else {
                        x = 0;
                    }
                }
            }

            // For each shape to still add
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
                // If checkRotation is true and width does not fit but height does then rotate
                if (checkRotation && width > widthToFitIn && height <= widthToFitIn) {
                    rotate = true;
                    width = height;
                    height = shapes[index].getWidth();
                }

                // If the shape can fit
                if (width <= widthToFitIn) {

                    // If checkForPerfect is true then search a first perfect width fit out of the remaining
                    if(checkForPerfect) {
                        int counter = 0;
                        while (width != widthToFitIn && i + counter < toAdd.size()){
                            // If the width of a later shape matches the widthToFitIn then have that as the current shape
                            if(shapes[toAdd.get(i + counter)].getWidth() == widthToFitIn){
                                rotate = false;
                                i += counter;
                                index = toAdd.get(i);
                                width = shapes[index].getWidth();
                                height = shapes[index].getHeight();
                            } else if (checkRotation && shapes[toAdd.get(i + counter)].getHeight() == widthToFitIn) {
                                rotate = true;
                                i += counter;
                                index = toAdd.get(i);
                                width = shapes[index].getHeight();
                                height = shapes[index].getWidth();
                            }else {
                                counter++;
                            }
                        }
                    }

                    // Set the variable of the x value for the shape to be drawn from
                    int shapeX = x;
                    // If reversed then we need to adjust x as we are looking at the right edge of where the shape will go
                    if (reverse) {
                        shapeX -= width;
                        shapeX++; // Adjust for the fact that the left point is inclusive
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

                        if(moveXBack) {
                            // Move x back to fill in gaps
                            while (x < boxWidth - 2 && x >= 0) {
                                if (yBottomLine[x] < yBottomLine[x + 1]) {
                                    break;
                                }
                                x++;
                            }
                        }

                    } else {

                        // Adjust yBottomLine
                        for (int whereShapePlaced = x; whereShapePlaced < x + width; whereShapePlaced++) {
                            yBottomLine[whereShapePlaced] = newY;
                        }

                        // Adjust x
                        x += width;

                        if(moveXBack) {
                            // Move x back to fill in gaps
                            while (x > 0 && x < boxWidth) {
                                if (yBottomLine[x] < yBottomLine[x - 1]) {
                                    break;
                                }
                                x--;
                            }
                        }
                    }


                    // A shape has been added so reset passes counter
                    passes = 0;

                    // Remove shape from the list to add
                    toAdd.remove(i);
                    // Break out of the loop of searching through each shape
                    break;
                }
            }

            passes++;
        }
        return toBeDrawn;
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
                }
            }
        }

        Shape[] shapes = readCSV(filePath, columnNumber, limitToTen); //set to limit input to 10 shapes maximum

        // Sort the array of shapes from largest area first to smallest area last
        Arrays.sort(shapes, Collections.reverseOrder());

        //printShapeOrder(shapes, "Initial Order");

        // Create a list of indexes of the shapes to add so we can remove shapes from this list as they get added
        ArrayList<Integer> toAdd = new ArrayList<>();
        for (int i = 0; i < shapes.length; i++) {
            toAdd.add(i);
        }

        // The shapes fitted to the sheet will be stored to toBeDrawn
        DrawingDimensions[] toBeDrawn = fitShape(toAdd, shapes, false, false, false, false, 0);
        System.out.println("First fit gave a height of " + getLargestY());

        int currentHeight = getLargestY();

        // Second test

        // Reset toAdd
        toAdd.clear();
        for (int i = 0; i < shapes.length; i++) {
            toAdd.add(i);
        }

        // The shapes fitted to the sheet will be stored to toBeDrawn2
        DrawingDimensions[] toBeDrawn2 = fitShape(toAdd, shapes, true, true, true, true, 5);
        System.out.println("Second fit gave a height of " + getLargestY());

        // If the new largest y value is less than the previous height
        if(getLargestY() < currentHeight){
            // Then make that the current best option
            toBeDrawn = toBeDrawn2;
            currentHeight = getLargestY();
        }

        //-------------------------------------------------------------------------------------
        //finish timing program
        long finalTime = System.nanoTime();
        //Please do not remove or change the format of this output message
        System.out.println("Processed " + shapes.length + " shapes in " + (finalTime - initialTime) / 1E9 + " secs.");

        // Report how much space was used to fit all the shapes
        System.out.println("Used space = " + currentHeight * boxWidth);

        // Set up the graphical display
        new GraphicalDisplay(boxWidth, currentHeight, toBeDrawn);
    }
}