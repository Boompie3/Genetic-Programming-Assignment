import java.io.Serializable;

public abstract class GPNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Evaluates this node given an array of features.
     * @param features Array containing the 9 feature variables (indices 0 to 8).
     * @return A double value representing the numerical evaluation or classification path decision.
     */
    public abstract double evaluate(double[] features);
}


// this was created for testing purposes