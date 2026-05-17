\# GP Breast Cancer Classifier

A Java-based Genetic Programming system for binary classification on the Breast Cancer Wisconsin Dataset.

The program automatically performs a **30-run comparative experiment** between:

- **Symbolic GP (Arithmetic Classifier)**
- **Logical GP (Decision Tree Classifier)**

and generates statistical performance comparisons including:

- Training Accuracy
- Test Accuracy
- F-measure
- Standard Deviation
- Two-tailed T-test

---

# Requirements

- Java 8 or higher
- Executable JAR file:
  - `GPClassifier.jar`
- Dataset files:
  - `Breast_train.csv`
  - `Breast_test.csv`

Check Java installation:

```bash
java -version
```

---

# Project Structure

Place all files in the same directory:

```text
GPClassifier.jar
Breast_train.csv
Breast_test.csv
README.md
```

---

# Running the Program

## Default Execution

Run the JAR:

```bash
java -jar GPClassifier.jar
```

The program automatically loads:

```text
Breast_train.csv
Breast_test.csv
```

---

# Seed Prompt

When execution starts, the program prompts for a base seed value:

```text
Enter base seed value:
```

Example:

```text
Enter base seed value: 1000
```

The program then automatically generates seeds for all 30 runs.

Example using base seed `1000`:

## Symbolic GP Seeds

```text
1001 → 1030
```

## Logical GP Seeds

```text
2001 → 2030
```

---

# Using Custom Dataset Paths

You can provide custom CSV paths:

```bash
java -jar GPClassifier.jar /path/to/train.csv /path/to/test.csv
```

Example:

```bash
java -jar GPClassifier.jar ../Breast_train.csv ../Breast_test.csv
```

Windows example:

```bash
java -jar GPClassifier.jar C:\data\Breast_train.csv C:\data\Breast_test.csv
```

Linux/macOS example:

```bash
java -jar GPClassifier.jar ~/data/Breast_train.csv ~/data/Breast_test.csv
```

---

# Example Execution

```bash
java -jar GPClassifier.jar
```

Output:

```text
=== GP Breast Cancer Classifier: 30-Run Batch Comparison ===

Training File: Breast_train.csv
Test File:     Breast_test.csv

Enter base seed value: 1000

=== Loading Data ===
Training samples: 297
Test samples: 86

=== Running Symbolic (Arithmetic) 30 times ===
  Progress: 10/30
  Progress: 20/30
  Progress: 30/30 ✓

=== Running Logical (Decision Tree) 30 times ===
  Progress: 10/30
  Progress: 20/30
  Progress: 30/30 ✓
```

---

# Output Produced

The program automatically executes:

## 1. Symbolic GP (Arithmetic Classifier)

Runs 30 independent experiments using arithmetic expressions.

Operators used:

- `+`
- `−`
- `×`
- `÷`

---

## 2. Logical GP (Decision Tree)

Runs 30 independent experiments using IF-THEN-ELSE decision structures.

---

## 3. Statistical Comparison

The final report includes:

- Mean Training Accuracy
- Mean Test Accuracy
- F-measure
- Standard deviation
- Two-tailed T-test

---

# Example Comparison Table

```text
======================== COMPARISON TABLE (30 runs) ========================

Metric                  | Symbolic   | Logical    | Difference
------------------------------------------------------------------------
Training Accuracy (%)   |   83.47%   |   82.16%   |    1.31%
  ± Std Dev             |  ± 1.98%   |  ± 2.14%   |

Test Accuracy (%)       |   79.07%   |   81.40%   |    2.33%
  ± Std Dev             |  ± 3.21%   |  ± 2.95%   |

F-measure               |   0.6234   |   0.6891   |   0.0657
  ± Std Dev             |  ± 0.0451  |  ± 0.0389  |

=========================================================================
```

---

# Runtime

Approximate runtime:

| Task | Time |
|---|---|
| Single GP run | ~60–120 seconds |
| Full experiment | ~60–120 minutes |

---

# Files Generated

During execution:

```text
best_model.ser
```

This file stores the best evolved model and is overwritten after each run.

---

# Optional JVM Memory Allocation

For larger datasets or low-memory systems:

```bash
java -Xmx4g -jar GPClassifier.jar
```

---

# Troubleshooting

## Unable to access jarfile

Ensure the JAR exists in the current directory.

Linux/macOS:

```bash
ls
```

Windows:

```cmd
dir
```

---

## CSV File Not Found

Use absolute paths:

```bash
java -jar GPClassifier.jar \
/path/to/Breast_train.csv \
/path/to/Breast_test.csv
```

---

# Dataset

Breast Cancer Wisconsin Dataset

- 9 input features
- Binary classification
- 297 training samples
- 86 testing samples

---

# Technologies Used

- Java 8+
- Genetic Programming
- Symbolic Evolution
- Decision Tree Evolution
- Statistical Analysis

---
