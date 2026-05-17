import java.io.*;
import java.util.Scanner;

public class GPTestingSuite {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== GP Breast Cancer Testing & Evaluation ===");
        
        // 1. Request Assignment Constraints via CLI
        System.out.print("Enter seed value (for logging/replication): ");
        long seed = scanner.nextLong();
        scanner.nextLine(); // Clear buffer

        System.out.print("Enter filepath to the unseen test data (e.g., Breast_test.csv): ");
        String testDataPath = scanner.nextLine();

        System.out.print("Enter filepath to the trained serialized model (e.g., best_model.ser): ");
        String modelPath = scanner.nextLine();

        System.out.println("Select Model Type you are evaluating:");
        System.out.println("1. Symbolic (Arithmetic Classifier)");
        System.out.println("2. Logical (Decision Tree)");
        System.out.print("Choice (1 or 2): ");
        int modelChoice = scanner.nextInt();
        boolean isLogical = (modelChoice == 2);

        System.out.println("\n--- Initializing Final Production Testing Pipeline (Seed: " + seed + ") ---");

        try {
            // 2. Load the Test Data using the partner's shared DataLoader
            DataLoader testData = new DataLoader(testDataPath);
            System.out.println("Test data parsed successfully. Total unseen instances: " + testData.labels.length);

            // 3. Load the best trained model
            Node bestModel = loadModel(modelPath);
            System.out.println("Pre-trained model deserialized successfully.");

            // 4. Run classification metrics
            executeClassification(bestModel, testData, isLogical);

        } catch (IOException e) {
            System.err.println("File System Error: Could not locate file. " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Deserialization Error: The model structure does not match. " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    // Handles metric generations and clocks microsecond runtime
    private static void executeClassification(Node tree, DataLoader dataset, boolean isLogical) {
        int tp = 0, fp = 0, tn = 0, fn = 0;

        // Start Nanosecond clock tracking
        long start = System.nanoTime();

        for (int i = 0; i < dataset.labels.length; i++) {
            double expressionOutput = tree.evaluate(dataset.features[i]);
            int predictedClass = 0;

            if (isLogical) {
                // Decision trees output direct classification threshold paths
                predictedClass = expressionOutput > 0.5 ? 1 : 0;
            } else {
                // Symbolic models map outcomes through your partner's mathematical Sigmoid wrapper
                predictedClass = (1.0 / (1.0 + Math.exp(-expressionOutput))) > 0.5 ? 1 : 0;
            }

            int actualClass = dataset.labels[i];

            // Confusion matrix calculation
            if (predictedClass == actualClass) {
                if (actualClass == 1) tp++;
                else tn++;
            } else {
                if (actualClass == 1) fn++;
                else fp++;
            }
        }

        // End clock tracking
        long end = System.nanoTime();
        double executionTimeMs = (end - start) / 1_000_000.0;

        // Accuracy and F-Measure calculations
        double testAccuracy = ((double)(tp + tn) / dataset.labels.length) * 100.0;
        double precision = (tp + fp) > 0 ? (double) tp / (tp + fp) : 0;
        double recall = (tp + fn) > 0 ? (double) tp / (tp + fn) : 0;
        double fMeasure = (precision + recall) > 0 ? 2.0 * ((precision * recall) / (precision + recall)) : 0.0;

        // Matches Table 2 reporting standards perfectly
        System.out.println("\n====================== RESULTS ======================");
        System.out.printf("Test Accuracy: %.2f%%\n", testAccuracy);
        System.out.printf("F-measure:     %.4f\n", fMeasure);
        System.out.printf("Runtime:       %.2f ms\n", executionTimeMs);
        System.out.println("=====================================================");
    }

    // Deserialization importer for final phase execution
    private static Node loadModel(String filepath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filepath))) {
            return (Node) ois.readObject();
        }
    }
}