public class FeatureNode extends Node {
    int featureIndex;
    
    public FeatureNode(int index) {
        this.featureIndex = index;
    }
    
    @Override
    public double evaluate(double[] features) {
        return features[Math.min(featureIndex, features.length - 1)];
    }
    
    @Override
    public String toString() {
        return "x[" + featureIndex + "]";
    }
    
    @Override
    public Node cloneNode() {
        return new FeatureNode(this.featureIndex);
    }
}