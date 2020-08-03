public class Shape implements Comparable<Shape>
{
    private int width;
    private int height;
    private int area;

    public Shape(int width, int height){
        this.width = width;
        this.height = height;
        area = width * height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getArea() {
        return area;
    }

    // Compare Two shapes based on their area
    /**
     * @param   anotherShape - The Shape to be compared.
     * @return  A negative integer, zero, or a positive integer as this shape
     *          is less than, equal to, or greater than the supplied shape object.
     */
    @Override
    public int compareTo(Shape anotherShape) {
        return this.getArea() - anotherShape.getArea();
    }

    // Two shapes are equal if their width and height are equal
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shape shape = (Shape) o;
        return width == shape.getWidth() && height == shape.getHeight();
    }

    @Override
    public String toString() {
        return "Shape{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}