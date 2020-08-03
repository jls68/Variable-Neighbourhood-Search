import java.awt.*;

public class DrawingDimensions {
    int x;
    int y;
    String i;
    Shape shape;
    boolean rotate;

    public DrawingDimensions(int X, int Y, int index, Shape shape, boolean rotate){
        x = X;
        y = Y;
        i = String.valueOf(index);
        this.shape = shape;
        this.rotate = rotate;
    }

    public void draw(Graphics g) {
        int h, w;
        if (rotate) {
            h = shape.getWidth();
            w = shape.getHeight();
        } else {
            w = shape.getWidth();
            h = shape.getHeight();
        }
        g.drawRect(x, y, w, h);

        // Reduce the dimensions so there is empty space between each shape to help check for overlap
        //g.drawRect(x - 1, y + 1,  w - 2, h - 2);
        // Add the index number to the shape
        //g.drawString(i, x, y + g.getFont().getSize());
    }
}
