public class ClassLeafNode extends Node {
    int predictedClass; // 0 or 1
    
    public ClassLeafNode(int predictedClass) {
        this.predictedClass = predictedClass;
    }
    
    @Override
    public double evaluate(double[] features) {
        return predictedClass;
    }
    
    @Override
    public String toString() {
        return "Class_" + predictedClass;
    }
    
    @Override
    public Node cloneNode() {
        return new ClassLeafNode(this.predictedClass);
    }
}