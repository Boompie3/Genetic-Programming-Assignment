public class Individual implements Comparable<Individual> {
    public Node root;
    public double fitness = -1; 
    public double tp, tn, fp, fn;
    
    public Individual(Node root) {
        this.root = root;
    }
    
    // Evaluate fitness (Accuracy)
    public void evaluateFitness(DataLoader data, boolean isLogical) {
        int correct = 0;
        tp = 0; tn = 0; fp = 0; fn = 0;
        
        for (int i = 0; i < data.labels.length; i++) {
            double predictedVal = root.evaluate(data.features[i]);
            // Sigmoid/Thresholding for symbolic
            int predictedClass = 0;
            if (isLogical) {
                predictedClass = predictedVal > 0.5 ? 1 : 0;
            } else {
                predictedClass = (1.0 / (1.0 + Math.exp(-predictedVal))) > 0.5 ? 1 : 0;
            }
            
            int actualClass = data.labels[i];
            
            if (predictedClass == actualClass) {
                correct++;
                if (actualClass == 1) tp++;
                else tn++;
            } else {
                if (actualClass == 1) fn++;
                else fp++;
            }
        }
        this.fitness = (double) correct / data.labels.length;
    }
    
    public double getFMeasure() {
        double precision = (tp + fp == 0) ? 0 : tp / (tp + fp);
        double recall = (tp + fn == 0) ? 0 : tp / (tp + fn);
        return (precision + recall == 0) ? 0 : 2 * (precision * recall) / (precision + recall);
    }
    
    @Override
    public int compareTo(Individual o) {
        return Double.compare(o.fitness, this.fitness); // Descending order
    }
}