package pt.unl.fct.pds;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.ConsensusCacheManager;
import pt.unl.fct.pds.experiments.ExperimentRunner;

/**
 * Tor Circuit Simulator - Main Application
 */
public class Project2 
{
    public static void main(String[] args) {
        System.out.println("=== Tor Circuit Simulator ===\n");

        // Load nodes from cache or consensus
        Node[] nodes = loadNodes();
        System.out.println("✓ Loaded " + nodes.length + " nodes\n");
        
        // Run experiments
        ExperimentRunner runner = new ExperimentRunner(nodes);
        
        // Quick demo mode (3 circuits each)
        System.out.println("=== Quick Demo ===");
        System.out.println("Low = Low Risk Path Selection");
        System.out.println("Medium = Medium Risk Path Selection");
        System.out.println("High = High Risk Path Selection");

        runner.runStandardTor(3);
        runner.runGeographyAware(0.5, 0.5, 3);
        
        // Comparison mode (100 circuits each)
        System.out.println("\n=== Performance Comparison ===");
        runner.setQuietMode(true);
        runner.compareConfigurations(100);
        
        System.out.println("\n=== Complete ===");
    }
    
    private static Node[] loadNodes() {
        ConsensusCacheManager manager = new ConsensusCacheManager(
            "resources/consensus.txt",
            "nodes_cache.csv",
            true
        );
        return manager.getNodes();
    }
}
