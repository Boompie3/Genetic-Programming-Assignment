import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== GP Breast Cancer Classifier ===");
        
        System.out.print("Enter Seed Value (integer): ");
        long seed = scanner.nextLong();
        
        System.out.print("Enter filepath for training data: ");
        String filepath = scanner.next();
        
        System.out.println("Select Model:");
        System.out.println("1. Symbolic (Arithmetic Classifier)");
        System.out.println("2. Logical (Decision Tree)");
        System.out.print("Choice (1 or 2): ");
        int modelChoice = scanner.nextInt();
        
        System.out.println("\nInitializing Training Demonstration...");
        System.out.println("Seed: " + seed);
        System.out.println("File: " + filepath);
        System.out.println("Model: " + (modelChoice == 1 ? "Symbolic" : "Logical"));
        
        try {
            // Load data
            DataLoader data = new DataLoader(filepath);
            
            // Initialize and Run GPEngine
            boolean isLogical = (modelChoice == 2);
            GPEngine engine = new GPEngine(seed, data, isLogical);
            engine.run();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        scanner.close();
    }
}