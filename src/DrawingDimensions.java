import java.awt.*;

public class DrawingDimensions {
    int x;
    int y;
    Shape shape;
    boolean rotate;

    public DrawingDimensions(int X, int Y, Shape shape, boolean rotate){
        x = X;
        y = Y;
        this.shape = shape;
        this.rotate = rotate;
    }

    public void draw(Graphics g, int enl) {
        int h, w;
        if (rotate) {
            h = shape.getWidth();
            w = shape.getHeight();
        } else {
            w = shape.getWidth();
            h = shape.getHeight();
        }

        // Increased in size to make the shapes easier to see
        int X = x * enl;
        int Y = y * enl;
        int W = w * enl;
        int H = h * enl;

        // Fill inner colour
        g.setColor(Color.CYAN);
        //g.fillRect(X, Y, W, H);

        // Draw outline
        g.setColor(Color.BLACK);
        g.drawRect(X, Y, W, H);

        // Add the index number to the shape
        g.drawString(shape.getId(), X + 1, Y + g.getFont().getSize());
    }
}
