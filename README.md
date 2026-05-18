COS314 Assignment 3: GP Breast Cancer Classifier
Marchant Grootboom u21549232
Warona Moleboge u23770912
----------------------------

1. PROJECT DESCRIPTION
----------------------
This project implements a fully automated Genetic Programming (GP) classification 
engine from scratch in Java. It compares two structural approaches for predicting 
breast cancer recurrence on the Breast Cancer Wisconsin Dataset:
- Symbolic GP (Arithmetic Classifier with Sigmoid activation)
- Logical GP (Decision Tree Classifier with conditional routing)

The program automatically performs a 30-run batch comparative experiment and 
generates statistical performance comparisons including Test/Train Accuracies, 
F-measure, Standard Deviations, and a Two-Tailed T-test.

2. DIRECTORY STRUCTURE
----------------------
- Assignment 3/
    - Breast_train.csv     (Training dataset)
    - Breast_test.csv      (Testing dataset)
    - GPClassifier.jar     (Executable packaged program)
    - README.txt           (This file)
    - COS314A3.pdf         (Official project report)
    - src/                 (Source code directory)
        - Main.java        (Entry point, batch runner, and evaluation logic)
        - DataLoader.java  (Dataset parsing and extraction)
        - GPEngine.java    (Evolutionary algorithm and population management)
        - Individual.java  (Fitness evaluation wrapper)
        - Node.java        (Abstract base class for all tree nodes)
        - MathNode.java    (Symbolic GP operators)
        - ConditionNode.java (Logical GP splitters)
        - FeatureNode.java (Dataset attribute terminals)
        - ConstantNode.java (Numeric scalar terminals)
        - ClassLeafNode.java (Classification terminals)

3. COMPILATION (From Source)
----------------------------
If you wish to compile and run the raw source code instead of the JAR, navigate 
into the 'src' directory and compile using the Java Development Kit (JDK):

cd src
javac *.java

To run the compiled source directly (pointing to the CSVs in the parent folder):
java Main ../Breast_train.csv ../Breast_test.csv

4. EXECUTION (Standalone JAR)
-----------------------------
To run the pre-packaged executable JAR file from the main directory:

java -jar GPClassifier.jar Breast_train.csv Breast_test.csv

5. REPLICATION & EXPECTED OUTPUT
--------------------------------
When execution begins, the program will prompt you for a base seed value. 
To perfectly replicate the results, tables, and statistics documented in the 
official report, please use the following parameter:

- Base Seed Value: 1000

The system will use this to dynamically spin up sequential seeds for the 
Symbolic runs (1001 to 1030) and the Logical runs (2001 to 2030).

Expected Results for Base Seed 1000 (Aggregated 30-Run Batch):
- Decision Tree: Test Accuracy ~52.64% | F-measure ~0.3379
- GP Classifier: Test Accuracy ~46.59% | F-measure ~0.3590
- Two-Tailed T-test Result: t = -4.3271 (Statistically Significant)