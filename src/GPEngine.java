import java.util.*;

public class GPEngine {
    Random random;
    DataLoader trainData;
    int popSize = 200;
    int maxGenerations = 100;
    int tournamentSize = 3;
    double crossoverRate = 0.8;
    double mutationRate = 0.2;
    int maxInitialDepth = 5;
    int NUM_FEATURES = 9;
    boolean isLogical;
    
    List<Individual> population;
    
    public GPEngine(long seed, DataLoader data, boolean isLogical) {
        this.random = new Random(seed);
        this.trainData = data;
        this.isLogical = isLogical;
        this.population = new ArrayList<>();
    }
    
    public void run() {
        initializePopulation();
        
        for (int gen = 1; gen <= maxGenerations; gen++) {
            evaluatePopulation();
            Collections.sort(population); // highest fitness first
            
            Individual best = population.get(0);
            
            System.out.printf("Generation %d | Best Fitness (Accuracy): %.4f | F-Measure: %.4f | Depth: %d\n", 
                gen, best.fitness, best.getFMeasure(), best.root.getDepth());
                
            if (gen == 1 || gen == maxGenerations) {
                System.out.println("Best Tree: " + best.root.toString());
            }
            
            if (best.fitness >= 1.0) {
                System.out.println("Perfect solution found!");
                break;
            }
            
            population = createNextGeneration();
        }
        // After evolution completes, serialize best model for testing/replication
        try {
            saveBestModel("best_model.ser");
            System.out.println("Best model serialized to best_model.ser");
        } catch (Exception e) {
            System.err.println("Failed to save best model: " + e.getMessage());
        }
    }

    // Serialize the current best individual's tree to a file
    public void saveBestModel(String filepath) throws java.io.IOException {
        if (population == null || population.isEmpty()) return;
        Individual best = population.get(0);
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(filepath))) {
            oos.writeObject(best.root);
        }
    }
    
    // Ramped Half-and-Half placeholder
    private void initializePopulation() {
        for (int i = 0; i < popSize; i++) {
            int depth = random.nextInt(maxInitialDepth) + 1;
            boolean fullMethod = random.nextBoolean();
            Node root = generateTree(depth, fullMethod);
            population.add(new Individual(root));
        }
    }
    
    private Node generateTree(int maxDepth, boolean fullMethod) {
        if (maxDepth == 1 || (!fullMethod && random.nextDouble() < 0.2)) {
            return generateLeaf();
        }
        
        if (isLogical) {
            ConditionNode n = new ConditionNode(random.nextInt(NUM_FEATURES), (random.nextDouble() * 10) - 5); // threshold between -5 and 5 roughly
            n.left = generateTree(maxDepth - 1, fullMethod);
            n.right = generateTree(maxDepth - 1, fullMethod);
            return n;
        } else {
            String[] ops = {"+", "-", "*", "/"};
            MathNode n = new MathNode(ops[random.nextInt(ops.length)]);
            n.left = generateTree(maxDepth - 1, fullMethod);
            n.right = generateTree(maxDepth - 1, fullMethod);
            return n;
        }
    }
    
    private Node generateLeaf() {
        if (isLogical) {
            return new ClassLeafNode(random.nextInt(2)); // returns 0 or 1
        } else {
            return new FeatureNode(random.nextInt(NUM_FEATURES));
        }
    }
    
    private void evaluatePopulation() {
        for (Individual ind : population) {
            if (ind.fitness == -1) {
                ind.evaluateFitness(trainData, isLogical);
            }
        }
    }
    
    private List<Individual> createNextGeneration() {
        List<Individual> nextGen = new ArrayList<>();
        // Elitism (keep top 2)
        nextGen.add(population.get(0));
        nextGen.add(population.get(1));
        
        while (nextGen.size() < popSize) {
            if (random.nextDouble() < crossoverRate) {
                Individual p1 = tournamentSelect();
                Individual p2 = tournamentSelect();
                Node offspringRoot = crossover(p1.root, p2.root);
                nextGen.add(new Individual(offspringRoot));
            } else {
                Individual p = tournamentSelect();
                Node offspringRoot = pointMutation(p.root.cloneNode());
                nextGen.add(new Individual(offspringRoot));
            }
        }
        return nextGen;
    }
    
    // Standard Subtree Crossover
    private Node crossover(Node parent1, Node parent2) {
        Node offspring = parent1.cloneNode();
        
        List<Node> nodesP1 = new ArrayList<>();
        offspring.collectNodes(nodesP1);
        
        List<Node> nodesP2 = new ArrayList<>();
        parent2.collectNodes(nodesP2);
        
        // Pick random crossover points
        Node point1 = nodesP1.get(random.nextInt(nodesP1.size()));
        Node point2 = nodesP2.get(random.nextInt(nodesP2.size()));
        
        // Swap subtrees (we replace point1's logic with point2's clone)
        // Note: Since point1 is already part of the offspring tree by reference, 
        // modifying its children modifies the offspring tree.
        // We handle type-matching conservatively for crossover by replacing its children and data
        if (point1 instanceof MathNode && point2 instanceof MathNode) {
            ((MathNode)point1).op = ((MathNode)point2).op;
            point1.left = (point2.left != null) ? point2.left.cloneNode() : null;
            point1.right = (point2.right != null) ? point2.right.cloneNode() : null;
        } else if (point1 instanceof ConditionNode && point2 instanceof ConditionNode) {
            ((ConditionNode)point1).featureIndex = ((ConditionNode)point2).featureIndex;
            ((ConditionNode)point1).threshold = ((ConditionNode)point2).threshold;
            point1.left = (point2.left != null) ? point2.left.cloneNode() : null;
            point1.right = (point2.right != null) ? point2.right.cloneNode() : null;
        } else if (point1 instanceof FeatureNode && point2 instanceof FeatureNode) {
            ((FeatureNode)point1).featureIndex = ((FeatureNode)point2).featureIndex;
        } else if (point1 instanceof ClassLeafNode && point2 instanceof ClassLeafNode) {
            ((ClassLeafNode)point1).predictedClass = ((ClassLeafNode)point2).predictedClass;
        } else {
            // Just returning parent 1 clone if types prevent clean swap
            return offspring;
        }
        
        return offspring;
    }
    
    // Point Mutation: Change the operator or feature of a random node
    private Node pointMutation(Node root) {
        List<Node> nodes = new ArrayList<>();
        root.collectNodes(nodes);
        
        Node point = nodes.get(random.nextInt(nodes.size()));
        
        if (point instanceof MathNode) {
            String[] ops = {"+", "-", "*", "/"};
            ((MathNode) point).op = ops[random.nextInt(ops.length)];
        } else if (point instanceof ConditionNode) {
            if (random.nextBoolean()) {
                ((ConditionNode) point).featureIndex = random.nextInt(NUM_FEATURES);
            } else {
                ((ConditionNode) point).threshold = (random.nextDouble() * 10) - 5;
            }
        } else if (point instanceof FeatureNode) {
            ((FeatureNode) point).featureIndex = random.nextInt(NUM_FEATURES);
        } else if (point instanceof ClassLeafNode) {
            ((ClassLeafNode) point).predictedClass = random.nextInt(2);
        }
        
        return root;
    }
    
    private Individual tournamentSelect() {
        Individual best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Individual curr = population.get(random.nextInt(popSize));
            if (best == null || curr.fitness > best.fitness) {
                best = curr;
            }
        }
        return best;
    }
}