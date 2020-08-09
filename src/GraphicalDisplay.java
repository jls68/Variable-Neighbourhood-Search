import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

public class GraphicalDisplay extends JFrame {

    // Constant to increase the size of the display by
    int ENLARGEMENT = 2;
    // Array of shapes is global to allow main and paint method access
    DrawingDimensions[] toBeDrawn;

    // Declare an instance of the drawing canvas,
    // which is an inner class called DrawCanvas extending javax.swing.JPanel.
    private DrawCanvas canvas;

    // Declare variables for a timer to draw each shape separately
    private int count = 1;
    private int timeSlice = 100;  // How many milliseconds in between each shape being drawn
    private Timer timer = new Timer(timeSlice, (e) -> update());

    // Constructor to set up the GUI components and event handlers
    public GraphicalDisplay(int boxWidth, int boxHeight, DrawingDimensions[] toBeDrawn) {
        canvas = new DrawCanvas();    // Construct the drawing canvas
        canvas.setPreferredSize(new Dimension((boxWidth) * ENLARGEMENT, boxHeight * ENLARGEMENT));
        this.toBeDrawn = toBeDrawn;

        // Set the Drawing JPanel as the JFrame's content-pane
        Container cp = getContentPane();
        cp.add(canvas);
        // or "setContentPane(canvas);"

        setDefaultCloseOperation(EXIT_ON_CLOSE);   // Handle the CLOSE button
        pack();              // Either pack() the components; or setSize()
        setTitle("ShapeSearch Graphic");  // "super" JFrame sets the title
        setVisible(true);    // "super" JFrame show
        timer.start();
    }

    /**
     * Define inner class DrawCanvas, which is a JPanel used for custom drawing.
     */
    private class DrawCanvas extends JPanel {
        // Override paintComponent to perform your own painting
        @Override
        public void paintComponent(Graphics g) {
            // Call the draw method for each shapes' DrawingDimensions to be drawn
            /*for (DrawingDimensions d : toBeDrawn) {
                d.draw(g, ENLARGEMENT);
            }*/
            for (int i = 0; i < count; i++) {
                toBeDrawn[i].draw(g, ENLARGEMENT);
            }
        }
    }

    /**
     * Repaint the canvas with one more shape until all shapes are drawn
     */
    public void update() {
        if (count < toBeDrawn.length) {
            canvas.repaint();
            count++;
        }
        else{
            timer.stop();
        }
    }

}
