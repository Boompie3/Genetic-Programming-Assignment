# GP Breast Cancer Classifier - Execution Guide

## Quick Start

### Prerequisites
- Java 8 or higher installed
- Training data: `../Breast_train.csv` (297 samples, 9 features)
- Test data: `../Breast_test.csv` (86 samples, 9 features)

### Basic Execution (Default Paths)

```bash
cd Genetic-Programming-Assignment/src
javac *.java
java Main
```

**Output**: 30-run comparison table (Symbolic vs Logical GP) in terminal

### Custom Data Paths

```bash
java Main /path/to/train.csv /path/to/test.csv
```

Example:
```bash
java Main ../Breast_train.csv ../Breast_test.csv
java Main ~/data/training.csv ~/data/testing.csv
```

## What It Does

The executable automatically runs a **30-iteration batch experiment**:

1. **Symbolic GP (Arithmetic Classifier)** × 30 runs
   - Seeds: 1001-1030
   - Operators: +, −, ×, ÷
   - Evolves arithmetic expressions to classify

2. **Logical GP (Decision Tree)** × 30 runs
   - Seeds: 2001-2030
   - Nodes: IF/THEN/ELSE conditions
   - Evolves decision trees to classify

3. **Generates Comparison Table** with:
   - Training Accuracy (% ± std dev)
   - Test Accuracy (% ± std dev)
   - F-measure (± std dev)
   - Statistical T-test (two-tailed)

## Output Format

```
=== GP Breast Cancer Classifier: 30-Run Batch Comparison ===

Training File: ../Breast_train.csv
Test File:     ../Breast_test.csv

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


======================== COMPARISON TABLE (30 runs) ========================
Metric                  | Symbolic   | Logical    | Difference
--------
Training Accuracy (%)   |   83.47%   |   82.16%   |    1.31%
  ± Std Dev             |  ± 1.98%   |  ± 2.14%   |

Test Accuracy (%)       |   79.07%   |   81.40%   |    2.33%
  ± Std Dev             |  ± 3.21%   |  ± 2.95%   |

F-measure               |   0.6234   |   0.6891   |   0.0657
  ± Std Dev             |  ± 0.0451  |  ± 0.0389  |
=========================================================================

T-test on Test Accuracy: t = -2.1543
(Two-tailed significance test between Symbolic and Logical)
```

## Performance

- **Single Run**: ~60-120 seconds (100 generations)
- **Total Runtime**: ~60-120 minutes (60 runs total)
  - Symbolic: 30 runs × ~90s = ~45 min
  - Logical: 30 runs × ~90s = ~45 min
  - Plus data loading and table generation

## Files Generated

During execution:
- `best_model.ser` - Serialized best individual (overwritten each run)

## System Architecture

```
Main.java
├── main(args[]) → Parses command-line paths
├── run30ComparisonAuto() → Orchestrates 60 runs
│   ├── Loop 1: 30× Symbolic GP with different seeds
│   ├── Loop 2: 30× Logical GP with different seeds
│   └── printComparisonTable() → Statistical summary

GPEngine.java
├── run(suppress) → 100-generation evolution
├── getLastTrainAccuracy() → Returns training accuracy
└── getLastRuntimeNs() → Returns runtime

Supporting Classes:
├── DataLoader.java → CSV parsing (9 features, binary labels)
├── Individual.java → Fitness wrapper for trees
├── Node.java (abstract) → Base class for all tree nodes
├── MathNode.java → Arithmetic operators (+, −, ×, ÷)
├── FeatureNode.java → Feature inputs (x[0]...x[8])
├── ConstantNode.java → Numeric constants
├── ConditionNode.java → IF-THEN-ELSE routing
└── ClassLeafNode.java → Terminal outputs (0 or 1)
```

## Command-Line Arguments

| Argument | Default | Purpose |
|----------|---------|---------|
| None | Uses defaults | Runs with `../Breast_train.csv`, `../Breast_test.csv` |
| `train_path` | - | Custom training CSV path |
| `train_path test_path` | - | Custom train AND test paths |

## Error Handling

If data files are not found:
```
Error: ../Breast_train.csv (No such file or directory)
```

Solution: Ensure CSV files are in the correct relative path, or provide absolute paths:
```bash
java Main /mnt/c/Users/socce/Documents/COS314/ASS3/Breast_train.csv \
         /mnt/c/Users/socce/Documents/COS314/ASS3/Breast_test.csv
```

## Notes

- **No interactive prompts** - Runs automatically with defaults
- **No menu** - Directly executes Mode 4 (30-run comparison)
- **Reproducible** - Fixed seed sequence (1001-1030, 2001-2030)
- **Parallel-friendly** - Can run multiple instances with different seed offsets

## Troubleshooting

### Compilation fails
```bash
javac -version  # Check Java 8+
javac *.java    # Compile all files
```

### File not found errors
- Check working directory: `pwd` should show `.../Genetic-Programming-Assignment/src`
- Verify CSV files exist: `ls ../Breast_train.csv ../Breast_test.csv`

### Out of memory (rare)
If processing large datasets:
```bash
java -Xmx4g Main
```

---

**Author**: COS314 Assignment 3  
**Language**: Java 1.8.0+  
**Data**: Breast Cancer Wisconsin Dataset (9 features, 297 train / 86 test)
