import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class ShapeSearch {
    public static void main(String[] args) {

        // Variable to hold which list of shapes to use
        int opt = 1;

        try{
            // Read in the arguemnt for which option of list to use
            //TODO

            // Adjust opt to point to the correct column
            opt = (opt - 1) * 4;

            // Parse CSV file into BufferedReader
            BufferedReader br = new BufferedReader(new FileReader("GivenLists.csv"));

            // Read in the header of the CSV file
            String line = br.readLine();
            String[] cells = line.split(",");
            String name = cells[opt + 2];

            line = br.readLine();
            cells = line.split(",");
            int size = Integer.parseInt(cells[opt + 2]);

            // For testing limit size to 10
            size = 10;

            line = br.readLine();
            cells = line.split(",");
            int itemArea = Integer.parseInt(cells[opt + 2]);

            // Skip the next two lines
            br.readLine();
            br.readLine();

            // Declare an array of Shape objects to be used
            Shape[] shapes = new Shape[size];

            // Read each shape
            for(int i = 0; i < size; i++){
                // Convert each line of the CSV file into a shape object
                line = br.readLine();
                cells = line.split(",");
                int width = Integer.parseInt(cells[opt + 1]);
                int height = Integer.parseInt(cells[opt + 2]);
                // Put the larger value into width to help with sorting the initial order
                if (width < height){
                    shapes[i] = (new Shape(height, width));
                }
                else {
                    shapes[i] = (new Shape(width, height));
                }
            }



            System.out.println("All shapes in order recorded shapes:");
            // Print out the first ten shapes
            for (int i = 0; i < size; i++){
                System.out.println(shapes[i].toString());
            }

            // Sort the array of shapes from largest area first to smallest area last
            Arrays.sort(shapes, Collections.reverseOrder());


            System.out.println("All shapes in order of largest area shapes:");
            // Print out the top ten shapes
            for (int i = 0; i < size; i++){
                System.out.println(shapes[i].toString());
            }

            br.close();

        }
        catch(Exception e){
            System.out.println("Error: " + e.toString());
        }
    }
}