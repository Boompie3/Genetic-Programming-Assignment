import java.io.*;
import java.util.*;

public class GPTestingSuite {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Assignment Constraint: Initial configurations via CLI parameters
        System.out.print("Enter seed value: ");
        long seed = scanner.nextLong();
        scanner.nextLine(); // Clear buffer

        System.out.print("Enter filepath to the CSV file (e.g., Breast_test.csv): ");
        String csvFilePath = scanner.nextLine();

        System.out.println("\nSelect Operational Mode:");
        System.out.println("1 - [MOCK DEVELOPMENT MODE] (Run right now with a temporary dummy tree)");
        System.out.println("2 - [PRODUCTION RUN MODE]   (Load actual model file from your partner)");
        System.out.print("Choice: ");
        int mode = scanner.nextInt();
        scanner.nextLine();

        Node modelToTest = null;
        boolean isDecisionTree = false;

        if (mode == 1) {
            System.out.println("\nChoose type of mock algorithm to evaluate:");
            System.out.println("1 - Mock Arithmetic Classifier");
            System.out.println("2 - Mock Decision Tree");
            System.out.print("Choice: ");
            int subChoice = scanner.nextInt();
            
                if (subChoice == 1) {
                    modelToTest = generateMockArithmeticTree();
                    isDecisionTree = false;
                    System.out.println("\n--- Initializing Mock Arithmetic Testing Pipeline (Seed: " + seed + ") ---");
                } else {
                    modelToTest = generateMockDecisionTree();
                    isDecisionTree = true;
                    System.out.println("\n--- Initializing Mock Decision Tree Testing Pipeline (Seed: " + seed + ") ---");
                }
        } else {
            System.out.print("Enter filepath to the trained serialized model (.ser): ");
            String modelPath = scanner.nextLine();
            
            System.out.println("Is the model you are loading a Decision Tree? (y/n): ");
            String typeChoice = scanner.nextLine().trim().toLowerCase();
            isDecisionTree = typeChoice.equals("y");

            try {
                modelToTest = loadModel(modelPath);
                System.out.println("\n--- Initializing Production Testing Pipeline (Seed: " + seed + ") ---");
            } catch (Exception e) {
                System.err.println("Failed to serialize model: " + e.getMessage());
                scanner.close();
                return;
            }
        }

        try {
            // Load and parse the instances cleanly matching your exact dataset columns
            List<InstanceData> testDataset = parseCSV(csvFilePath);
            System.out.println("Data parsed successfully. Total processing instances: " + testDataset.size());

            // Run evaluation engine
                executeClassification(modelToTest, testDataset, isDecisionTree);

        } catch (IOException e) {
            System.err.println("File System Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    // Class to explicitly pair labels and features matching your specific CSV column offsets
    private static class InstanceData {
        int targetClass;     // Column 0
        double[] features;   // Columns 1 to 9 (9 total attributes)

        public InstanceData(int targetClass, double[] features) {
            this.targetClass = targetClass;
            this.features = features;
        }
    }

    // Custom CSV parser handling your exact format where target is the first column
    private static List<InstanceData> parseCSV(String path) throws IOException {
        List<InstanceData> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // Skip text header row safely
                if (isHeader && line.toLowerCase().contains("class")) {
                    isHeader = false;
                    continue;
                }

                String[] columns = line.split(",");
                
                // Column 0 is your classification category label (0 or 1)
                int actualClass = Integer.parseInt(columns[0].trim());
                
                // Columns 1 to 9 are the 9 distinct processing attributes
                double[] featureSet = new double[columns.length - 1];
                for (int i = 1; i < columns.length; i++) {
                    featureSet[i - 1] = Double.parseDouble(columns[i].trim());
                }

                records.add(new InstanceData(actualClass, featureSet));
            }
        }
        return records;
    }

    // Handles metric generations and clocks microsecond runtime
    private static void executeClassification(Node tree, List<InstanceData> dataset, boolean isDecisionTree) {
        int tp = 0, fp = 0, tn = 0, fn = 0;

        // Nanosecond clock tracking initiation
        long start = System.nanoTime();

        for (InstanceData instance : dataset) {
            double expressionOutput = tree.evaluate(instance.features);
            int finalPrediction;

            if (isDecisionTree) {
                // Decision trees natively output direct classification target paths (0.0 or 1.0)
                finalPrediction = (expressionOutput >= 0.5) ? 1 : 0;
            } else {
                // Keep arithmetic testing consistent with training-time classification.
                double sigmoid = 1.0 / (1.0 + Math.exp(-expressionOutput));
                finalPrediction = (sigmoid > 0.5) ? 1 : 0;
            }

            // Confusion matrix calculation loop
            if (instance.targetClass == 1 && finalPrediction == 1) tp++;
            else if (instance.targetClass == 0 && finalPrediction == 1) fp++;
            else if (instance.targetClass == 0 && finalPrediction == 0) tn++;
            else if (instance.targetClass == 1 && finalPrediction == 0) fn++;
        }

        long end = System.nanoTime();
        double executionTimeMs = (end - start) / 1_000_000.0;

        // Accuracy and F-Measure calculations
        double accuracy = ((double)(tp + tn) / dataset.size()) * 100.0;
        double precision = (tp + fp) > 0 ? (double) tp / (tp + fp) : 0;
        double recall = (tp + fn) > 0 ? (double) tp / (tp + fn) : 0;
        double fMeasure = (precision + recall) > 0 ? 2.0 * ((precision * recall) / (precision + recall)) : 0.0;

        // Matches Table 2 reporting standards perfectly
        System.out.println("\n====================== RESULTS ======================");
        System.out.printf("Test Accuracy: %.2f%%\n", accuracy);
        System.out.printf("F-measure:     %.4f\n", fMeasure);
        System.out.printf("Runtime:       %.2f ms\n", executionTimeMs);
        System.out.println("=====================================================");
    }

    // Deserialization importer for final phase execution
    private static Node loadModel(String filepath) throws IOException, ClassNotFoundException {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(new java.io.FileInputStream(filepath))) {
            return (Node) ois.readObject();
        }
    }

    // Generates a mock arithmetic tree: (Feature_0 - Feature_2) * 2.5
    private static Node generateMockArithmeticTree() {
        MathNode mul = new MathNode("*");
        MathNode sub = new MathNode("-");
        sub.left = new FeatureNode(0);
        sub.right = new FeatureNode(2);
        mul.left = sub;
        mul.right = new ConstantNode(2.5);
        return mul;
    }

    // Generates a mock decision tree: If Feature_2 <= 4 then Class 0 else Class 1
    private static Node generateMockDecisionTree() {
        ConditionNode root = new ConditionNode(2, 4.0);
        root.left = new ClassLeafNode(0);
        root.right = new ClassLeafNode(1);
        return root;
    }
}