import java.util.List;

public class Solution {

    DrawingDimensions[] toBeDrawn;
    int[] yBottomLine;
    int boxWidth;
    int score;

    public Solution(List<Integer> toAdd, Shape[] shapes, boolean[] options, int boxWidth){
        this.boxWidth = boxWidth;

        yBottomLine = new int[boxWidth];
        toBeDrawn = fitShape(toAdd, shapes, options[0], options[1], options[2], options[3], options[4]);
        score = getLargestY();
    }

    public int getScore() {
        return score;
    }

    public DrawingDimensions[] getDrawDimensions() {
        return toBeDrawn;
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
    private DrawingDimensions[] fitShape(List<Integer> toAdd, Shape[] shapes, boolean zigzag, boolean checkRotation, boolean checkForPerfect, boolean moveXBack, boolean findLowestUntil){

        int x = 0, toBeDrawnIndex = 0, passes = 0; // Record the y values along the top of all added shapes
        DrawingDimensions[] toBeDrawn = new DrawingDimensions[shapes.length];

        boolean reverse = false;
        // Until all shapes are added
        while (0 < toAdd.size()) {

            //If passes exceed 1 then move x to next lowest y value
            if (1 < passes) {

                // Determine the direction to move x
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

                    if (passes > 100) {
                        //At this point the program has failed as it has tried every single possible value along the maximum x axis.
                        System.out.println("The program has failed to fit " + toAdd.size() + " shapes");
                        return toBeDrawn; //TODO make sure if this happens it does not become the best solution

                    } else if(findLowestUntil && 4 < passes) { // Else if set then attempt to fit a shape in the lowest y level
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


    /**
     * Find the largest y value which a shape touches.
     *
     * @return the greatest y value.
     */
    private int getLargestY(){
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


}
