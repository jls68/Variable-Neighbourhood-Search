import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.awt.Canvas;
import java.awt.Graphics;
import java.util.concurrent.TimeUnit;
import javax.swing.JFrame;

public class ShapeSearch extends Canvas {

    // Array of shapes is global to allow main and paint method access
    static ArrayList<DrawingDimensions> toBeDrawn;
    static int boxWidth;

    /**
     * @param   filePath: must be of the form "___.csv"
     *          columnNumber: which column of the CSV is to be read
     *          limitToTen: boolean that controls whether input is limited for debug purposes
     *
     * @return  a 1D array of shapes, either limited to 10 or else the size of the CSV column
     */
    static Shape[] readCSV(String filePath, int columnNumber, boolean limitToTen) {
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
            if(name == "M1a"){
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

    public static void main(String[] args) {

        //start timing program
        final long initialTime = System.nanoTime();
        //-----------------------------------------------------------------------------

        int columnNumber = 1;
        String filePath = "ShapeLists/GivenLists.csv";

        Shape[] shapes = readCSV(filePath, columnNumber, false); //set to limit input to 10 shapes maximum
        int size = shapes.length;

        System.out.println("All shapes in order recorded shapes:");
        // Print out the first ten shapes
        for (int i = 0; i < size; i++) {
            System.out.println(shapes[i].toString());
        }

        // Sort the array of shapes from largest area first to smallest area last
        Arrays.sort(shapes, Collections.reverseOrder());


        System.out.println("All shapes in order of largest area shapes:");
        // Print out the top ten shapes
        for (int i = 0; i < size; i++) {
            System.out.println(shapes[i].toString());
        }

        //########### Start of the shape fitting ###########
        int boxHeight = 1200;

        int x = 0;
        // Record the y values along the top of all added shapes
        int[] yBottomLine = new int[boxWidth];

        boolean reverse = false;

        toBeDrawn = new ArrayList<DrawingDimensions>();

        JFrame frame = new JFrame("ShapeSearch Graphic");
        Canvas canvas = new ShapeSearch();
        canvas.setSize(boxWidth, boxHeight);
        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);

        // Create a list of indexes of the shapes to add so we can remove shapes from this list as they get added
        ArrayList<Integer> toAdd = new ArrayList<Integer>();
        for (int i = 0; i < shapes.length; i++) {
            toAdd.add(i);
        }

        // Keep track of how many times we search all available shapes before we add one
        int passes = 0;

        try {
            // Until all shapes are added
            while (1 < toAdd.size()) {

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


                        if (reverse) {
                            // Add the shape
                            toBeDrawn.add(new DrawingDimensions(x - width, yBottomLine[x], index, shapes[index], rotate));

                            passes = 0; // A shape has been added so reset passes counter

                            int newY = yBottomLine[x] + height;
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
                            // Add the shape
                            toBeDrawn.add(new DrawingDimensions(x, yBottomLine[x], index, shapes[index], rotate));

                            passes = 0; // A shape has been added so reset passes counter

                            int newY = yBottomLine[x] + height;

                            // Extend height if needed
                            if (newY > canvas.getHeight()) {
                                canvas.setSize(boxWidth, newY);
                            }

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

                        // Remove shape from the list to add
                        toAdd.remove(i);
                        //i--;
                        break;
                    }
                }

                passes++;
            }

            //-------------------------------------------------------------------------------------
            //finish timing program
            long finalTime = System.nanoTime();
            //Please do not remove or change the format of this output message
            System.out.println("Processed " + size + " shapes in " + (finalTime - initialTime) / 1E9 + " secs.");

        } catch (Exception e) {
            System.out.println("Error: " + e.toString());
        }
    }

    public void paint(Graphics g) {
        try {
            for (DrawingDimensions d : toBeDrawn
            ) {
                // Draw the shape
                d.draw(g);
                // Wait so we can see each shape being added
                TimeUnit.MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}