package pt.unl.fct.pds.experiments;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.PathSelector;
import pt.unl.fct.pds.utils.CircuitStatistics;
import pt.unl.fct.pds.utils.CircuitDisplay;


public class ExperimentRunner {
    
    private final Node[] nodes;
    private boolean quietMode = false;
    
    public ExperimentRunner(Node[] nodes) {
        this.nodes = nodes;
    }
    
    public void setQuietMode(boolean quiet) {
        this.quietMode = quiet;
        CircuitDisplay.setVerboseMode(!quiet);
    }
    

    public CircuitStatistics runStandardTor(int numCircuits, String port) {
        if (!quietMode) {
            System.out.println("\n=== Standard Tor Path Selection ===");
        }
        
        PathSelector selector = new PathSelector(nodes);
        return runExperiment(selector, numCircuits, port);
    }
    

    public CircuitStatistics runGeographyAware(double alpha, double beta, int numCircuits, String port) {
        if (!quietMode) {
            System.out.println("\n=== Geography-Aware Selection ===");
            System.out.printf("Parameters: α=%.2f, β=%.2f\n", alpha, beta);
        }
        
        PathSelector selector = new PathSelector(nodes);
        selector.setGeographyParameters(alpha, beta);
        return runExperiment(selector, numCircuits, port);
    }
    

    private CircuitStatistics runExperiment(PathSelector selector, int numCircuits, String port) {
        CircuitStatistics stats = new CircuitStatistics();
        
        for (int i = 1; i <= numCircuits; i++) {
            Node[] path = selector.selectPath(port);
            
            if (path != null) {
                stats.recordCircuit(path);
                if (!quietMode && i <= 3) {
                    CircuitDisplay.printCircuit(path, i);
                }
            }
        }
        
        if (!quietMode) {
            stats.printSummary();
        }
        
        return stats;
    }
    

    public void compareConfigurations(int numCircuits, String port) {
        System.out.println("\n=== Comparing Configurations ===\n");
        
        double[][] configs = {
            {0.0, 0.0},  // Standard
            {0.3, 0.3},  // Light
            {0.5, 0.5},  // Medium
            {0.7, 0.7},  // Strong
        };
        
        System.out.println("Running " + numCircuits + " circuits per configuration...\n");
        
        for (double[] config : configs) {
            double alpha = config[0];
            double beta = config[1];
            
            PathSelector selector = new PathSelector(nodes);
            selector.setGeographyParameters(alpha, beta);
            
            CircuitStatistics stats = new CircuitStatistics();
            for (int i = 0; i < numCircuits; i++) {
                Node[] path = selector.selectPath(port);
                if (path != null) {
                    stats.recordCircuit(path);
                }
            }
            
            System.out.printf("α=%.1f, β=%.1f: ", alpha, beta);
            System.out.printf("3-country=%.1f%%, Avg BW=%d\n",
                100.0 * stats.getCircuits3Countries() / stats.getTotalCircuits(),
                stats.getAverageBandwidth()
            );
        }
    }
}
