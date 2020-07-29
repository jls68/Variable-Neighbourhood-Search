import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
                shapes[i] = (new Shape(width, height));
            }

            // Print out the top ten shapes
            for (int i = 0; i < 10; i++){
                System.out.println(shapes[i].toString());
            }

            br.close();

        }
        catch(Exception e){
            System.out.println("Error: " + e.toString());
        }
    }
}