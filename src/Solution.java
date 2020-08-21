import java.util.*;

public class Solution {

    //James_Sheaf-Morrison_1314151_Meleena_Radcliffe_1319196

    private Random rand;
    private int[] yBottomLine; // Record the y values along the top of all added shapes
    private DrawingDimensions[] toBeDrawn;
    private Shape[] _shapesOrder;
    private int _boxWidth;
    private int _seed;
    private int score;
    private int firstImprovementCost;
    private final double moveTypes = 3;

    public Solution(Shape[] shapesOrder, int boxWidth, int seed){
        _shapesOrder = shapesOrder;
        _boxWidth = boxWidth;
        _seed = seed;
        rand = new Random(_seed);

        yBottomLine = new int[_boxWidth];
        toBeDrawn = fitShape(_shapesOrder);

        score = getLargestY();
    }

    public int getScore() {
        return score;
    }

    /**
     * Gives the amount of neighbourhoods searched before first improvement returned a result
     * @return the cost from the most recent firstimprovement
     */
    public int getFirstImprovementCost() {
        return firstImprovementCost;
    }

    public Shape[] getOrder(){
        return _shapesOrder;
    }

    public DrawingDimensions[] getDrawDimensions() {
        return toBeDrawn;
    }

    /**
     * Pick random solution from kth neighbourhood around x
     *
     * @param k the neighbour index
     * @return a random solution in k neighbourhood
     */
    public Solution Shake(int k){
        // Pick a random index of a shape in the solution
        int i = rand.nextInt(_shapesOrder.length);
        // Use that random index to get a shape order of kth neighbourhood
        Shape[] neighbourShapes = getNeighbour(i, k);
        return new Solution(neighbourShapes, _boxWidth, _seed);
    }

    /**
     * Finds the first solution that improves in the neighbourhood
     *
     * @param k the neighbour index
     * @return the same solution or the first improvement in the neighbours
     */
    public Solution FirstImprovment(int k) {
        Solution xNew;
        // Create neighbours of solution that have k difference
        int length = _shapesOrder.length;
        for (int i = 0; i < length; i++) {
            Shape[] newOrder = getNeighbour(i, k);
            // Add the new solution
            xNew = new Solution(newOrder, _boxWidth, _seed);

            // If the new solution is better then return it
            if (xNew.getScore() < this.getScore()) {
                firstImprovementCost = i + 1;
                return xNew;
            }
        }

        firstImprovementCost = length;
        // With no better solution found return the same solution
        return this;
    }

    /**
     * Finds the best solution in the neighbourhood
     *
     * @param k the neighbour index
     * @return the best solution out of the neighbours
     */
    public Solution getBestInNeighborhood(int k) {
        Solution xNew, xBest = null;

        // Create neighbours of solution that have k difference
        int length = _shapesOrder.length;
        for (int i = 0; i < length; i++) {
            Shape[] newOrder = getNeighbour(i, k);
            // Add the new solution
            xNew = new Solution(newOrder, _boxWidth, _seed);

            if (xBest == null || xNew.getScore() < xBest.getScore()) {
                xBest = xNew;
            }
        }

        return xBest;
    }

    /**
     * Finds the specified neighbour of this  solution
     *
     * @param i index of the shape to be moved
     * @param k amount of positions to move up
     * @return a new array of shapes
     */
    private Shape[] getNeighbour(int i, int k) {
        // After k iterates through each shape do a different neighbourhood change
        int kLocal = (int)(Math.ceil(k / moveTypes));
        // Attempt to rotate k number of shapes from i up
        if (k % moveTypes == 1 && kLocal <= _shapesOrder.length) {
            Shape[] newOrder = _shapesOrder.clone();
            for(int j = 0; j < kLocal; j++) {
                // Calculate next shape's index
                int index = i + j;
                if(index >= newOrder.length){
                    index -= newOrder.length;
                }
                // Rotate shape at index
                newOrder = rotateShape(index, newOrder);
            }
            return newOrder;
        }else if (k % moveTypes == 2 && kLocal < _shapesOrder.length) {
            // Push shape at i, k positions up the queue
            return moveShapeByK(i, kLocal, _shapesOrder);
        } else {
            // K shapes moved and rotated at random
            return moveKShapes(i, kLocal);
        }

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
            newPosition += (shapesOrder.length - 1);
        }
        // Create the new shape array

        List<Shape> orderList = new LinkedList<>(Arrays.asList(shapesOrder));
        orderList.remove(i);
        orderList.add(newPosition, shapesOrder[i]);

        return orderList.toArray(new Shape[shapesOrder.length]);
    }

    /**
     * Move and rotate an amount of shapes to random positions of the queue
     * @param i index of first shape to move
     * @param k amount of shapes to move
     * @return a new configuration of shapes
     */
    private Shape[] moveKShapes(int i, int k){

        Shape[] newOrder = _shapesOrder.clone();
        for(int j = 0; j < k; j++){
            // Rotate each shape at random
            if(rand.nextBoolean()){
                newOrder = rotateShape(i, newOrder);
            }
            // Get random movement amount
            int move = rand.nextInt(newOrder.length - 1) + 1;
            newOrder = moveShapeByK(i, move, newOrder);
        }
        return newOrder;
    }

    /**
     * Fits shapes onto the sheet
     * @param shapesOrder the list of shapes in order to be fitted
     * @return the drawing dimensions of teh added shapes
     */
    private DrawingDimensions[] fitShape(Shape[] shapesOrder) {

        int x = 0, toBeDrawnIndex = 0, passes = 0;
        DrawingDimensions[] toBeDrawn = new DrawingDimensions[shapesOrder.length];

        // Create a list of the shapes to be added so they can be removed from the list as they get added
        List<Shape> toAdd = new ArrayList<>(Arrays.asList(shapesOrder));

        // Until all shapes are added
        while (0 < toAdd.size()) {

            //If passes exceed 1 then move x to next lowest y value
            if (1 < passes) {

                // If x is in the bounds then find next y level to add the shape to
                if (x < _boxWidth && x >= 0) {
                    int yOld = yBottomLine[x];
                    // Move across until on a new y level
                    while (x < _boxWidth && x >= 0 && yOld == yBottomLine[x]) {
                        x++;
                    }
                }
                // Next if statement should still happen if the above if statement occurs as the x could move out of bounds

                // If x is out of bounds then go back to left
                if (x >= _boxWidth) {
                    x = 0;
                }
            }

            // For each shape to still add
            for (int i = 0; i < toAdd.size(); i++) {

                // Get the shape's dimensions
                int width = toAdd.get(i).getWidth();
                int height = toAdd.get(i).getHeight();

                // Calculate width to fit shape into
                int widthToFitIn = 0;
                for (int j = x; j < _boxWidth && yBottomLine[j] <= yBottomLine[x]; j++) {
                    widthToFitIn++;
                }

                // If the shape can fit
                if (width <= widthToFitIn) {

                    // Add the shape
                    toBeDrawn[toBeDrawnIndex] = new DrawingDimensions(x, yBottomLine[x], toAdd.get(i));
                    toBeDrawnIndex++;
                    int newY = yBottomLine[x] + height;


                    // Adjust yBottomLine
                    for (int whereShapePlaced = x; whereShapePlaced < x + width; whereShapePlaced++) {
                        yBottomLine[whereShapePlaced] = newY;
                    }

                    // Adjust x
                    x += width;

                    // Move x back to fill in gaps
                    while (x > 0 && x < _boxWidth) {
                        if (yBottomLine[x] < yBottomLine[x - 1]) {
                            break;
                        }
                        x--;
                    }


                    // A shape has been added so reset passes counter
                    passes = 0;

                    // Remove shape from the list to add
                    toAdd.remove(i);
                    // Break out of the loop of searching through each shape
                    break;
                }
                // Else if can't find a fit then do a local search for best placement
                else if (passes > 8) {
                    int bestX = 0;
                    int lowestMaxY = -1;
                    for (int left = 0; left < _boxWidth - width; left++) {
                        int yMax = 0;
                        for (int xValue = left; xValue < left + width; xValue++) {
                            if (yBottomLine[xValue] > yMax) {
                                yMax = yBottomLine[xValue];
                            }
                        }
                        if (lowestMaxY == -1 || lowestMaxY > yMax) {
                            lowestMaxY = yMax;
                            bestX = left;
                        }
                    }
                    // Add the shape
                    toBeDrawn[toBeDrawnIndex] = new DrawingDimensions(bestX, lowestMaxY, toAdd.get(i));
                    toBeDrawnIndex++;
                    // Adjust yBottomLine
                    for (int whereShapePlaced = bestX; whereShapePlaced < bestX + width; whereShapePlaced++) {
                        yBottomLine[whereShapePlaced] = lowestMaxY + height;
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
