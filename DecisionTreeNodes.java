// Conditional routing split node
class SplitNode extends GPNode {
    private final int featureIndex;
    private final double threshold;
    private final GPNode leftChild;  // Chosen if feature <= threshold
    private final GPNode rightChild; // Chosen if feature > threshold

    public SplitNode(int featureIndex, double threshold, GPNode leftChild, GPNode rightChild) {
        this.featureIndex = featureIndex;
        this.threshold = threshold;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    @Override
    public double evaluate(double[] features) {
        if (features[featureIndex] <= threshold) {
            return leftChild.evaluate(features);
        } else {
            return rightChild.evaluate(features);
        }
    }
}

// Class label leaf node
class ClassLabelNode extends GPNode {
    private final int classLabel; // 0 (no-recurrence) or 1 (recurrence)

    public ClassLabelNode(int classLabel) {
        this.classLabel = classLabel;
    }

    @Override
    public double evaluate(double[] features) {
        return this.classLabel;
    }
}