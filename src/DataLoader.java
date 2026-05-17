import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataLoader {
    public double[][] features;
    public int[] labels;
    
    public DataLoader(String filepath) throws IOException {
        List<double[]> featureList = new ArrayList<>();
        List<Integer> labelList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                // First column is class (0 or 1)
                labelList.add(Integer.parseInt(parts[0]));
                
                // Rest are features (9 columns)
                double[] row = new double[parts.length - 1];
                for (int i = 1; i < parts.length; i++) {
                    row[i - 1] = Double.parseDouble(parts[i]);
                }
                featureList.add(row);
            }
        }
        
        this.features = featureList.toArray(new double[0][]);
        this.labels = new int[labelList.size()];
        for(int i = 0; i < labels.length; i++) {
            this.labels[i] = labelList.get(i);
        }
    }
}