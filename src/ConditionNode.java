import java.util.Random;

public class ConditionNode extends Node {
    int featureIndex;
    double threshold;
    
    public ConditionNode(int featureIndex, double threshold) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
    }
    
    @Override
    public double evaluate(double[] features) {
        // If feature > threshold, go left (THEN), else go right (ELSE)
        double featureVal = features[Math.min(featureIndex, features.length - 1)];
        if (featureVal > threshold) {
            return left.evaluate(features);
        } else {
            return right.evaluate(features);
        }
    }
    
    @Override
    public String toString() {
        return "IF(x[" + featureIndex + "] > " + String.format("%.1f", threshold) + ") THEN [" + left.toString() + "] ELSE [" + right.toString() + "]";
    }
    
    @Override
    public Node cloneNode() {
        ConditionNode n = new ConditionNode(this.featureIndex, this.threshold);
        n.left = this.left.cloneNode();
        n.right = this.right.cloneNode();
        return n;
    }
}