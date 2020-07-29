import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class ShapeSearch {

    //filename: must be of the form "___.csv"
    //columnNumber: which column of the CSV is to be read
    //limit: number of shapes to be read in

    //returns a 1D array of shapes, of length [limit]
    static Shape[] readCSV(String fileName, int columnNumber, boolean limitToTen) {
        Shape[] shapes = null;

        int column = (columnNumber - 1) * 4;

        // Parse CSV file into BufferedReader
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            // Process header
            String line = br.readLine();
            String[] cells = line.split(",");

            //extract name from header
            String name = cells[column + 2];

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

        int columnNumber = 1;
        String fileName = "GivenLists.csv";

        Shape[] shapes = readCSV(fileName, columnNumber, true); //set to limit input to 10 shapes maximum
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


    }
}