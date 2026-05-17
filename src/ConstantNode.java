public class ConstantNode extends Node {
    double value;
    public ConstantNode(double value) { this.value = value; }
    @Override
    public double evaluate(double[] features) { return value; }
    @Override
    public String toString() { return String.format("%.4f", value); }
    @Override
    public Node cloneNode() { return new ConstantNode(this.value); }
}