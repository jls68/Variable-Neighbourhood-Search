public class Solution {

    int[] toAddOrder;
    boolean[] opts;
    int score;
    int limit;
    int k;

    public Solution(int[] toAdd, boolean[] options, int height, int testLowerLimit, int currentNeighbourhood){
        toAddOrder = toAdd;
        opts = options;
        score = height;
        limit = testLowerLimit;
        k = currentNeighbourhood;
    }

    public int[] getToAddOrder() {
        return toAddOrder;
    }

    public boolean[] getOpts() {
        return opts;
    }

    public int getScore() {
        return score;
    }

    public int getLimit() {
        return limit;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
