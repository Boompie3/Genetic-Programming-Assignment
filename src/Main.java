import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static class ResultMetrics {
        double trainAccuracy;
        double testAccuracy;
        double fMeasure;
        long runtimeNs;
        
        ResultMetrics(double trainAccuracy, double testAccuracy, double fMeasure, long runtimeNs) {
            this.trainAccuracy = trainAccuracy;
            this.testAccuracy = testAccuracy;
            this.fMeasure = fMeasure;
            this.runtimeNs = runtimeNs;
        }
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== GP Breast Cancer Classifier ===");

        System.out.println("Select Mode:");
        System.out.println("1. Training Demonstration");
        System.out.println("2. Testing / Classification (load saved model)");
        System.out.println("3. Train Then Test (single run)");
        System.out.println("4. Run 30 Iterations & Compare Models");
        int mode = promptInt(scanner, "Choice (1, 2, 3, or 4): ");

        try {
            if (mode == 1) {
                runTraining(scanner);
            } else if (mode == 2) {
                runTesting(scanner);
            } else if (mode == 3) {
                runTrainThenTest(scanner);
            } else if (mode == 4) {
                run30Comparison(scanner);
            } else {
                System.out.println("Invalid mode selected.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        scanner.close();
    }

    private static void runTraining(Scanner scanner) throws Exception {
        long seed = promptLong(scanner, "Enter Seed Value (integer): ");
        String trainPath = promptExistingFilePath(scanner, "Enter filepath for training data (default: ../Breast_train.csv): ", "../Breast_train.csv");

        boolean isLogical = askModelType(scanner);

        System.out.println("\nInitializing Training Demonstration...");
        System.out.println("Seed: " + seed);
        System.out.println("Train File: " + trainPath);
        System.out.println("Model: " + (isLogical ? "Logical" : "Symbolic"));

        DataLoader trainData = new DataLoader(trainPath);
        GPEngine engine = new GPEngine(seed, trainData, isLogical);
        engine.run(false); // false = show verbose output
    }

    private static void runTesting(Scanner scanner) throws Exception {
        long seed = promptLong(scanner, "Enter Seed Value (integer): ");
        String testPath = promptExistingFilePath(scanner, "Enter filepath for test data (default: ../Breast_test.csv): ", "../Breast_test.csv");
        String modelPath = promptExistingFilePath(scanner, "Enter filepath for serialized model (.ser) (default: best_model.ser): ", "best_model.ser");

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
        long seed = promptLong(scanner, "Enter Seed Value (integer): ");
        String trainPath = promptExistingFilePath(scanner, "Enter filepath for training data (default: ../Breast_train.csv): ", "../Breast_train.csv");
        String testPath = promptExistingFilePath(scanner, "Enter filepath for test data (default: ../Breast_test.csv): ", "../Breast_test.csv");

        boolean isLogical = askModelType(scanner);

        System.out.println("\nInitializing Train-Then-Test...");
        System.out.println("Seed: " + seed);
        System.out.println("Train File: " + trainPath);
        System.out.println("Test File: " + testPath);
        System.out.println("Model: " + (isLogical ? "Logical" : "Symbolic"));

        DataLoader trainData = new DataLoader(trainPath);
        GPEngine engine = new GPEngine(seed, trainData, isLogical);
        engine.run(false); // false = show output

        DataLoader testData = new DataLoader(testPath);
        Node model = loadModel("best_model.ser");
        evaluateAndReport(model, testData, isLogical);
    }

    private static void run30Comparison(Scanner scanner) throws Exception {
        String trainPath = promptExistingFilePath(scanner, "Enter filepath for training data (default: ../Breast_train.csv): ", "../Breast_train.csv");
        String testPath = promptExistingFilePath(scanner, "Enter filepath for test data (default: ../Breast_test.csv): ", "../Breast_test.csv");

        System.out.println("\n=== Running 30 Iterations for Both Models ===");
        System.out.println("Training: " + trainPath);
        System.out.println("Testing: " + testPath);

        DataLoader trainData = new DataLoader(trainPath);
        DataLoader testData = new DataLoader(testPath);

        List<ResultMetrics> symbolicResults = new ArrayList<>();
        List<ResultMetrics> logicalResults = new ArrayList<>();

        System.out.println("\nRunning Symbolic (Arithmetic) 30 times...");
        for (int i = 1; i <= 30; i++) {
            long seed = 1000 + i;
            GPEngine engine = new GPEngine(seed, trainData, false);
            engine.run(true); // true = suppress verbose output
            
            Node model = loadModel("best_model.ser");
            long evalStart = System.nanoTime();
            ResultMetrics testMetrics = computeMetrics(model, trainData, testData, false);
            long evalTime = System.nanoTime() - evalStart;
            
            testMetrics.trainAccuracy = engine.getLastTrainAccuracy();
            testMetrics.runtimeNs = evalTime;
            symbolicResults.add(testMetrics);
            if (i % 10 == 0) System.out.println("  Completed: " + i + "/30");
        }

        System.out.println("\nRunning Logical (Decision Tree) 30 times...");
        for (int i = 1; i <= 30; i++) {
            long seed = 2000 + i;
            GPEngine engine = new GPEngine(seed, trainData, true);
            engine.run(true); // true = suppress verbose output
            
            Node model = loadModel("best_model.ser");
            long evalStart = System.nanoTime();
            ResultMetrics testMetrics = computeMetrics(model, trainData, testData, true);
            long evalTime = System.nanoTime() - evalStart;
            
            testMetrics.trainAccuracy = engine.getLastTrainAccuracy();
            testMetrics.runtimeNs = evalTime;
            logicalResults.add(testMetrics);
            if (i % 10 == 0) System.out.println("  Completed: " + i + "/30");
        }

        printComparisonTable(symbolicResults, logicalResults);
    }

    private static void printComparisonTable(List<ResultMetrics> symbolic, List<ResultMetrics> logical) {
        double[] symbolicTrain = new double[symbolic.size()];
        double[] symbolicTest = new double[symbolic.size()];
        double[] symbolicF = new double[symbolic.size()];
        
        double[] logicalTrain = new double[logical.size()];
        double[] logicalTest = new double[logical.size()];
        double[] logicalF = new double[logical.size()];

        for (int i = 0; i < symbolic.size(); i++) {
            symbolicTrain[i] = symbolic.get(i).trainAccuracy;
            symbolicTest[i] = symbolic.get(i).testAccuracy;
            symbolicF[i] = symbolic.get(i).fMeasure;
        }
        for (int i = 0; i < logical.size(); i++) {
            logicalTrain[i] = logical.get(i).trainAccuracy;
            logicalTest[i] = logical.get(i).testAccuracy;
            logicalF[i] = logical.get(i).fMeasure;
        }

        double symbolicTrainMean = mean(symbolicTrain);
        double symbolicTestMean = mean(symbolicTest);
        double symbolicFMean = mean(symbolicF);
        
        double logicalTrainMean = mean(logicalTrain);
        double logicalTestMean = mean(logicalTest);
        double logicalFMean = mean(logicalF);

        double symbolicTrainStd = std(symbolicTrain);
        double symbolicTestStd = std(symbolicTest);
        double symbolicFStd = std(symbolicF);
        
        double logicalTrainStd = std(logicalTrain);
        double logicalTestStd = std(logicalTest);
        double logicalFStd = std(logicalF);

        System.out.println("\n\n======================== COMPARISON TABLE (30 runs) ========================");
        System.out.println(String.format("%-30s | %10s | %10s | %10s", "Metric", "Symbolic", "Logical", "Difference"));
        System.out.println("------------------------------------------------------------------------");
        System.out.println(String.format("Training Accuracy (%%)   | %8.2f%% | %8.2f%% | %8.2f%%", 
            symbolicTrainMean, logicalTrainMean, Math.abs(symbolicTrainMean - logicalTrainMean)));
        System.out.println(String.format("  ± Std Dev             | ±%7.2f%% | ±%7.2f%% |", symbolicTrainStd, logicalTrainStd));
        System.out.println("");
        System.out.println(String.format("Test Accuracy (%%)       | %8.2f%% | %8.2f%% | %8.2f%%",
            symbolicTestMean, logicalTestMean, Math.abs(symbolicTestMean - logicalTestMean)));
        System.out.println(String.format("  ± Std Dev             | ±%7.2f%% | ±%7.2f%% |", symbolicTestStd, logicalTestStd));
        System.out.println("");
        System.out.println(String.format("F-measure               | %8.4f  | %8.4f  | %8.4f",
            symbolicFMean, logicalFMean, Math.abs(symbolicFMean - logicalFMean)));
        System.out.println(String.format("  ± Std Dev             | ±%7.4f  | ±%7.4f  |", symbolicFStd, logicalFStd));
        System.out.println("=========================================================================\n");

        // T-test for test accuracy
        double tStatistic = tTest(symbolicTest, logicalTest);
        System.out.println(String.format("T-test on Test Accuracy: t = %.4f", tStatistic));
        System.out.println("(Two-tailed significance test between Symbolic and Logical)\n");
    }

    private static double mean(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    private static double std(double[] values) {
        double m = mean(values);
        double sumSq = 0;
        for (double v : values) sumSq += (v - m) * (v - m);
        return Math.sqrt(sumSq / values.length);
    }

    private static double tTest(double[] group1, double[] group2) {
        double mean1 = mean(group1);
        double mean2 = mean(group2);
        double std1 = std(group1);
        double std2 = std(group2);
        double pooledStd = Math.sqrt((std1 * std1 + std2 * std2) / 2.0);
        double se = pooledStd * Math.sqrt(1.0 / group1.length + 1.0 / group2.length);
        return se > 0 ? (mean1 - mean2) / se : 0;
    }

    private static ResultMetrics computeMetrics(Node model, DataLoader trainData, DataLoader testData, boolean isLogical) {
        // Compute training accuracy
        int trainCorrect = 0;
        for (int i = 0; i < trainData.labels.length; i++) {
            double out = model.evaluate(trainData.features[i]);
            int pred = (isLogical ? (out > 0.5) : (1.0 / (1.0 + Math.exp(-out)) > 0.5)) ? 1 : 0;
            if (pred == trainData.labels[i]) trainCorrect++;
        }
        double trainAccuracy = ((double) trainCorrect / trainData.labels.length) * 100.0;

        // Compute test metrics
        int tp = 0, tn = 0, fp = 0, fn = 0;
        long start = System.nanoTime();
        for (int i = 0; i < testData.labels.length; i++) {
            double out = model.evaluate(testData.features[i]);
            int pred = (isLogical ? (out > 0.5) : (1.0 / (1.0 + Math.exp(-out)) > 0.5)) ? 1 : 0;
            int actual = testData.labels[i];
            if (actual == 1 && pred == 1) tp++;
            else if (actual == 0 && pred == 1) fp++;
            else if (actual == 0 && pred == 0) tn++;
            else fn++;
        }
        long end = System.nanoTime();

        double testAccuracy = ((double) (tp + tn) / testData.labels.length) * 100.0;
        double precision = (tp + fp == 0) ? 0.0 : (double) tp / (tp + fp);
        double recall = (tp + fn == 0) ? 0.0 : (double) tp / (tp + fn);
        double fMeasure = (precision + recall == 0) ? 0.0 : 2.0 * (precision * recall) / (precision + recall);

        return new ResultMetrics(trainAccuracy, testAccuracy, fMeasure, end - start);
    }

    private static boolean askModelType(Scanner scanner) {
        System.out.println("Select Model:");
        System.out.println("1. Symbolic (Arithmetic Classifier)");
        System.out.println("2. Logical (Decision Tree)");
        while (true) {
            int modelChoice = promptInt(scanner, "Choice (1 or 2): ");
            if (modelChoice == 1) {
                return false;
            }
            if (modelChoice == 2) {
                return true;
            }
            System.out.println("Please enter 1 or 2.");
        }
    }

    private static Node loadModel(String modelPath) throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelPath))) {
            return (Node) ois.readObject();
        }
    }

    private static ResultMetrics evaluateAndReport(Node model, DataLoader data, boolean isLogical) {
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
        
        return new ResultMetrics(0, accuracy, fMeasure, end - start);
    }

    private static int promptInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    private static long promptLong(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid integer. Try again.");
            }
        }
    }

    private static String promptExistingFilePath(Scanner scanner, String prompt, String defaultPath) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                input = defaultPath;
            }

            Path path = Paths.get(input);
            if (!Files.exists(path)) {
                System.out.println("Path does not exist. Please enter a valid file path.");
                continue;
            }
            if (!Files.isRegularFile(path)) {
                System.out.println("That path points to a directory, not a file. Please enter a file path.");
                continue;
            }
            if (!Files.isReadable(path)) {
                System.out.println("File is not readable. Please choose another file.");
                continue;
            }
            return input;
        }
    }
}