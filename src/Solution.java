import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Solution {

    Random rand = new Random();
    int[] yBottomLine; // Record the y values along the top of all added shapes
    DrawingDimensions[] toBeDrawn;
    Shape[] _shapesOrder;
    boolean[] _options;
    int _boxWidth;
    int score;

    public Solution(Shape[] shapesOrder, boolean[] options, int boxWidth){
        _shapesOrder = shapesOrder;
        _options = options;
        _boxWidth = boxWidth;

        yBottomLine = new int[_boxWidth];
        toBeDrawn = fitShape(_shapesOrder, _options[0], _options[1], _options[2], _options[3], _options[4], _options[5]);

        score = getLargestY();
    }

    public int getScore() {
        return score;
    }

    public DrawingDimensions[] getDrawDimensions() {
        return toBeDrawn;
    }

    public Solution getBestInNeighborhood(int k, String type){
        // Create an array of neighborhood solutions
        Solution[] neighborhood;

        // Used to search neighbours that have different fitting options
        if (type.equals("Options")){
            int length = _options.length;
            neighborhood = new Solution[length];
            for (int i = 0; i < length; i++) {
                boolean[] newOptions = _options.clone();
                // Change k many options
                for(int j = 0; j < k; j++){
                    int index = i + j;
                    while(index >= _options.length){
                        index -= _options.length;
                    }
                    newOptions[index] = !newOptions[index];
                }
                // Add the new solution
                neighborhood[i] = new Solution(_shapesOrder, newOptions, _boxWidth);
            }
        }

        // Create neighbours of solution that have k difference
        else {
            // Decrement k by one as we first try rotating each shape before moving them
            k--;

            int length = _shapesOrder.length;
            neighborhood = new Solution[length];
            for (int i = 0; i < length; i++) {
                Shape[] newOrder;

                if(k == 0){
                    // Rotate shape at i
                    newOrder = rotateShape(i, _shapesOrder);
                }
                else if (type.equals("RandomMove")) {
                    // K shapes moved at random
                    newOrder = moveKShapes(i, k);
                }
                else {
                    // Push shape at i, k positions up the queue
                    newOrder = moveShapeByK(i, k, _shapesOrder);
                }
                // Add the new solution
                neighborhood[i] = new Solution(newOrder, _options, _boxWidth);
            }
        }

        // Find best neighbourhood solution
        Solution xBest = neighborhood[0];
        for (int i = 1; i < neighborhood.length; i++) {
            if(neighborhood[i].getScore() < xBest.score){
                xBest = neighborhood[i];
            }
        }

        return  xBest;
    }

    /**
     * Move a shape up the queue to be fitted by a certain amount
     * @param i index of the shape to be moved
     * @param k amount of positions to move up
     * @return a new order of shapes
     */
    private Shape[] moveShapeByK(int i, int k, Shape[] shapesOrder){
        int newPosition = i - k;
        // If the new position is less than 0
        if(newPosition < 0) {
            // Then add to end of queue
            newPosition = shapesOrder.length + newPosition;
        }
        // Create the new shape array
        Shape[] newOrder = new Shape[shapesOrder.length];
        int toAddIndex = 0;
        for(int j = 0; j < shapesOrder.length; j++){
            // If we reach the shape to skip
            if(j == i){
                toAddIndex++;
            }

            if(j == newPosition){
                newOrder[j] = _shapesOrder[i];
            } else{
                newOrder[j] = _shapesOrder[toAddIndex];
                toAddIndex++;
            }
        }
        return newOrder;
    }

    /**
     * Move an amount of shapes to random positions of the queue
     * @param i index of first shape to move
     * @param k amount of shapes to move
     * @return a new order of shapes
     */
    private Shape[] moveKShapes(int i, int k){

        Shape[] newOrder = moveShapeByK(i, rand.nextInt(_shapesOrder.length - 2) + 1, _shapesOrder);
        for(int j = 0; j < k; j++){
            newOrder = moveShapeByK(i, rand.nextInt(newOrder.length - 2) + 1, newOrder);
        }
        return newOrder;
    }

    /**
     * Rotate the shape at index i if possible
     * @param i index of the shape to rotate
     * @return a new list of shapes
     */
    private Shape[] rotateShape(int i, Shape[] shapesOrder){

        // Copy current shapes
        Shape[] newShapes = shapesOrder.clone();

        int newWidth = newShapes[i].getHeight();
        // If the rotated shape would not fit on the sheet then do not rotate it
        if(newWidth > _boxWidth){
            return newShapes;
        }

        int newHeight = newShapes[i].getWidth();

        // Create a copy of the old shape at i but with swapped dimensions
        newShapes[i] = new Shape(newShapes[i].getId(), newWidth, newHeight);
        return newShapes;
    }

    /**
     * Fits shapes onto the sheet
     * @param shapesOrder the list of shapes in order to be fitted
     * @param zigzag alternates the direction shapes are placed
     * @param checkRotation if this is true then try both rotations to fit
     * @param checkForPerfect if this is true then look for a shape that matches the width to fit perfectly
     * @param moveXBack if this is true the next shape could be placed behind the last place shape if it is a perfect fit
     * @param findLowestUntil this is the limit of passes before the lowest y is no longer used first
     * @param strictOrder says that each shape has to be added in order and that order can not be changed
     * @return the drawing dimensions of teh added shapes
     */
    private DrawingDimensions[] fitShape(Shape[] shapesOrder, boolean zigzag, boolean checkRotation, boolean checkForPerfect, boolean moveXBack, boolean findLowestUntil, boolean strictOrder){

        int x = 0, toBeDrawnIndex = 0, passes = 0;
        DrawingDimensions[] toBeDrawn = new DrawingDimensions[shapesOrder.length];

        // Create a list of the shapes to be added so they can be removed from the list as they get added
        List<Shape> toAdd = new ArrayList<>();
        toAdd.addAll(Arrays.asList(shapesOrder));
        
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
                if(x < _boxWidth && x >= 0) {
                    int yOld = yBottomLine[x];
                    // Move across until on a new y level
                    while (x < _boxWidth && x >= 0 && yOld == yBottomLine[x]) {
                        x += direction;
                    }

                    if(findLowestUntil && passes == 3) { // Else if set then attempt to fit a shape in the lowest y level
                        int xNew = x;
                        // Find x coordinate of the first lowest y value on the bottom line
                        for (int i = x; i < _boxWidth && i >= 0; i += direction) {
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
                } else if(x >= _boxWidth) {
                    if(zigzag) {
                        x = _boxWidth - 1;
                        reverse = true;
                    } else {
                        x = 0;
                    }
                }
            }

            // For each shape to still add
            for (int i = 0; i < toAdd.size(); i++) {

                // Get the shape's dimensions
                int width = toAdd.get(i).getWidth();
                int height = toAdd.get(i).getHeight();

                // Calculate width to fit shape into
                int widthToFitIn = 0;
                if (reverse) {
                    for (int j = x; j >= 0 && yBottomLine[j] <= yBottomLine[x]; j--) {
                        widthToFitIn++;
                    }
                } else {
                    for (int j = x; j < _boxWidth && yBottomLine[j] <= yBottomLine[x]; j++) {
                        widthToFitIn++;
                    }
                }

                // Rotate to fit
                boolean rotate = false;
                // If checkRotation is true and width does not fit but height does then rotate
                if (checkRotation && width > widthToFitIn && height <= widthToFitIn) {
                    rotate = true;
                    width = height;
                    height = toAdd.get(i).getWidth();
                }

                // If the shape can fit
                if (width <= widthToFitIn) {

                    // If checkForPerfect is true then search a first perfect width fit out of the remaining
                    if(checkForPerfect) {
                        int counter = 0;
                        while (width != widthToFitIn && i + counter < toAdd.size()){
                            // If the width of a later shape matches the widthToFitIn then have that as the current shape
                            if(toAdd.get(i + counter).getWidth() == widthToFitIn){
                                rotate = false;
                                i += counter;
                                width = toAdd.get(i).getWidth();
                                height = toAdd.get(i).getHeight();
                            } else if (checkRotation && toAdd.get(i + counter).getHeight() == widthToFitIn) {
                                rotate = true;
                                i += counter;
                                width = toAdd.get(i).getHeight();
                                height = toAdd.get(i).getWidth();
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
                    toBeDrawn[toBeDrawnIndex] = new DrawingDimensions(shapeX, yBottomLine[x], toAdd.get(i), rotate);
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
                            while (x < _boxWidth - 2 && x >= 0) {
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
                            while (x > 0 && x < _boxWidth) {
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
                // Else if can't find a fit then do a local search for best placement
                else if (passes > 8){
                    int bestX = 0;
                    int lowestMaxY = -1;
                    for(int left = 0; left < _boxWidth - width; left++){
                        int yMax = 0;
                        for(int xValue = left; xValue < left + width; xValue++){
                            if(yBottomLine[xValue] > yMax){
                                yMax = yBottomLine[xValue];
                            }
                        }
                        if(lowestMaxY == -1 || lowestMaxY > yMax){
                            lowestMaxY = yMax;
                            bestX = left;
                        }
                    }
                    // Add the shape
                    toBeDrawn[toBeDrawnIndex] = new DrawingDimensions(bestX, lowestMaxY, toAdd.get(i), rotate);
                    toBeDrawnIndex++;
                    // Adjust yBottomLine
                    for (int whereShapePlaced = bestX; whereShapePlaced < bestX + width; whereShapePlaced++) {
                        yBottomLine[whereShapePlaced] = lowestMaxY;
                    }

                    // Adjust x
                    x = bestX + width;

                    // A shape has been added so reset passes counter
                    passes = 0;

                    // Remove shape from the list to add
                    toAdd.remove(i);
                    // Break out of the loop of searching through each shape
                    break;
                }
                // If strict order is true then if current shape does not fit then do not try to fit the next shape
                else if(strictOrder){
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
