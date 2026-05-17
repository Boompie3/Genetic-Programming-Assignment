import java.io.Serializable;

public abstract class Node implements Serializable {
    private static final long serialVersionUID = 1L;

    public Node left;
    public Node right;
    
    // Evaluate node given the feature set of a row
    public abstract double evaluate(double[] features);
    
    // For printing the tree/expression
    @Override
    public abstract String toString();
    
    // Clone method for crossover/mutation
    public abstract Node cloneNode();
    
    // Depth evaluation
    public int getDepth() {
        int leftDepth = (left != null) ? left.getDepth() : 0;
        int rightDepth = (right != null) ? right.getDepth() : 0;
        return 1 + Math.max(leftDepth, rightDepth);
    }
    
    // Get total number of nodes in this subtree
    public int getSize() {
        int leftSize = (left != null) ? left.getSize() : 0;
        int rightSize = (right != null) ? right.getSize() : 0;
        return 1 + leftSize + rightSize;
    }
    
    // Collect all nodes into a flat list for random selection
    public void collectNodes(java.util.List<Node> list) {
        list.add(this);
        if (left != null) left.collectNodes(list);
        if (right != null) right.collectNodes(list);
    }
}