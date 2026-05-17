import java.util.Random;

public class MathNode extends Node {
    String op;
    
    public MathNode(String op) {
        this.op = op;
    }
    
    @Override
    public double evaluate(double[] features) {
        double l = left.evaluate(features);
        double r = right.evaluate(features);
        switch (op) {
            case "+": return l + r;
            case "-": return l - r;
            case "*": return l * r;
            case "/": 
                if (r == 0) return 1; // Protected division
                return l / r; 
        }
        return 0;
    }
    
    @Override
    public String toString() {
        return "(" + left.toString() + " " + op + " " + right.toString() + ")";
    }
    
    @Override
    public Node cloneNode() {
        MathNode n = new MathNode(this.op);
        n.left = this.left.cloneNode();
        n.right = this.right.cloneNode();
        return n;
    }
}