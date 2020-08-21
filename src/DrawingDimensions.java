import java.awt.*;

public class DrawingDimensions {

    //James_Sheaf-Morrison_1314151_Meleena_Radcliffe_1319196

    int x;
    int y;
    Shape shape;

    public DrawingDimensions(int X, int Y, Shape shape){
        x = X;
        y = Y;
        this.shape = shape;
    }

    public void draw(Graphics g, int enl) {

        // Increased in size to make the shapes easier to see
        int X = x * enl;
        int Y = y * enl;
        int W = shape.getWidth() * enl;
        int H = shape.getHeight() * enl;

        // Fill inner colour
        g.setColor(Color.CYAN);
        g.fillRect(X, Y, W, H);

        // Draw outline
        g.setColor(Color.BLACK);
        g.drawRect(X, Y, W, H);

        // Add the index number to the shape
        g.drawString(shape.getId(), X + 1, Y + g.getFont().getSize());
    }
}
