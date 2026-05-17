import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== GP Breast Cancer Classifier ===");

        System.out.println("Select Mode:");
        System.out.println("1. Training Demonstration");
        System.out.println("2. Testing / Classification (load saved model)");
        System.out.println("3. Train Then Test (single run)");
        System.out.print("Choice (1, 2, or 3): ");
        int mode = scanner.nextInt();

        try {
            if (mode == 1) {
                runTraining(scanner);
            } else if (mode == 2) {
                runTesting(scanner);
            } else if (mode == 3) {
                runTrainThenTest(scanner);
            } else {
                System.out.println("Invalid mode selected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        scanner.close();
    }

    private static void runTraining(Scanner scanner) throws Exception {
        System.out.print("Enter Seed Value (integer): ");
        long seed = scanner.nextLong();

        System.out.print("Enter filepath for training data: ");
        String trainPath = scanner.next();

        boolean isLogical = askModelType(scanner);

        System.out.println("\nInitializing Training Demonstration...");
        System.out.println("Seed: " + seed);
        System.out.println("Train File: " + trainPath);
        System.out.println("Model: " + (isLogical ? "Logical" : "Symbolic"));

        DataLoader trainData = new DataLoader(trainPath);
        GPEngine engine = new GPEngine(seed, trainData, isLogical);
        engine.run();
    }

    private static void runTesting(Scanner scanner) throws Exception {
        System.out.print("Enter Seed Value (integer): ");
        long seed = scanner.nextLong();

        System.out.print("Enter filepath for test data: ");
        String testPath = scanner.next();

        System.out.print("Enter filepath for serialized model (.ser): ");
        String modelPath = scanner.next();

        boolean isLogical = askModelType(scanner);

        System.out.println("\nInitializing Testing / Classification...");
        System.out.println("Seed: " + seed);
        System.out.println("Test File: " + testPath);
        System.out.println("Model File: " + modelPath);
        System.out.println("Model: " + (isLogical ? "Logical" : "Symbolic"));

        DataLoader testData = new DataLoader(testPath);
        Node model = loadModel(modelPath);
        evaluateAndReport(model, testData, isLogical);
    }

    private static void runTrainThenTest(Scanner scanner) throws Exception {
        System.out.print("Enter Seed Value (integer): ");
        long seed = scanner.nextLong();

        System.out.print("Enter filepath for training data: ");
        String trainPath = scanner.next();

        System.out.print("Enter filepath for test data: ");
        String testPath = scanner.next();

        boolean isLogical = askModelType(scanner);

        System.out.println("\nInitializing Train-Then-Test...");
        System.out.println("Seed: " + seed);
        System.out.println("Train File: " + trainPath);
        System.out.println("Test File: " + testPath);
        System.out.println("Model: " + (isLogical ? "Logical" : "Symbolic"));

        DataLoader trainData = new DataLoader(trainPath);
        GPEngine engine = new GPEngine(seed, trainData, isLogical);
        engine.run();

        DataLoader testData = new DataLoader(testPath);
        Node model = loadModel("best_model.ser");
        evaluateAndReport(model, testData, isLogical);
    }

    private static boolean askModelType(Scanner scanner) {
        System.out.println("Select Model:");
        System.out.println("1. Symbolic (Arithmetic Classifier)");
        System.out.println("2. Logical (Decision Tree)");
        System.out.print("Choice (1 or 2): ");
        int modelChoice = scanner.nextInt();
        return modelChoice == 2;
    }

    private static Node loadModel(String modelPath) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            return (Node) ois.readObject();
        }
    }

    private static void evaluateAndReport(Node model, DataLoader data, boolean isLogical) {
        int tp = 0;
        int tn = 0;
        int fp = 0;
        int fn = 0;

        long start = System.nanoTime();
        for (int i = 0; i < data.labels.length; i++) {
            double out = model.evaluate(data.features[i]);
            int pred;
            if (isLogical) {
                pred = (out > 0.5) ? 1 : 0;
            } else {
                double sigmoid = 1.0 / (1.0 + Math.exp(-out));
                pred = (sigmoid > 0.5) ? 1 : 0;
            }

            int actual = data.labels[i];
            if (actual == 1 && pred == 1) tp++;
            else if (actual == 0 && pred == 1) fp++;
            else if (actual == 0 && pred == 0) tn++;
            else fn++;
        }
        long end = System.nanoTime();

        double accuracy = ((double) (tp + tn) / data.labels.length) * 100.0;
        double precision = (tp + fp == 0) ? 0.0 : (double) tp / (tp + fp);
        double recall = (tp + fn == 0) ? 0.0 : (double) tp / (tp + fn);
        double fMeasure = (precision + recall == 0) ? 0.0 : 2.0 * (precision * recall) / (precision + recall);
        double runtimeMs = (end - start) / 1_000_000.0;

        System.out.println("\n====================== RESULTS ======================");
        System.out.printf("Test Accuracy: %.2f%%\n", accuracy);
        System.out.printf("F-measure:     %.4f\n", fMeasure);
        System.out.printf("Runtime:       %.2f ms\n", runtimeMs);
        System.out.println("=====================================================");
    }
}