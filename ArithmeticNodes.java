// Feature extraction node
class FeatureNode extends GPNode {
    private final int featureIndex;

    public FeatureNode(int featureIndex) {
        this.featureIndex = featureIndex;
    }

    @Override
    public double evaluate(double[] features) {
        return features[featureIndex];
    }
}

// Constant value node
class ConstantNode extends GPNode {
    private final double value;

    public ConstantNode(double value) {
        this.value = value;
    }

    @Override
    public double evaluate(double[] features) {
        return value;
    }
}

// Addition Node
class AddNode extends GPNode {
    private final GPNode left, right;
    public AddNode(GPNode left, GPNode right) { this.left = left; this.right = right; }
    @Override public double evaluate(double[] f) { return left.evaluate(f) + right.evaluate(f); }
}

// Subtraction Node
class SubNode extends GPNode {
    private final GPNode left, right;
    public SubNode(GPNode left, GPNode right) { this.left = left; this.right = right; }
    @Override public double evaluate(double[] f) { return left.evaluate(f) - right.evaluate(f); }
}

// Multiplication Node
class MulNode extends GPNode {
    private final GPNode left, right;
    public MulNode(GPNode left, GPNode right) { this.left = left; this.right = right; }
    @Override public double evaluate(double[] f) { return left.evaluate(f) * right.evaluate(f); }
}

// Protected Division Node (Returns 1.0 if dividing by zero to prevent runtime crashes)
class DivNode extends GPNode {
    private final GPNode left, right;
    public DivNode(GPNode left, GPNode right) { this.left = left; this.right = right; }
    @Override 
    public double evaluate(double[] f) { 
        double r = right.evaluate(f);
        return (r == 0) ? 1.0 : left.evaluate(f) / r; 
    }
}
