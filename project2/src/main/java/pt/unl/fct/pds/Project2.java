package pt.unl.fct.pds;

import org.torproject.descriptor.DescriptorCollector;
import org.torproject.descriptor.DescriptorSourceFactory;
import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.ConsensusCacheManager;
import pt.unl.fct.pds.experiments.ExperimentRunner;
import pt.unl.fct.pds.utils.GetNodeFamily;

import java.io.File;

/**
 * Tor Circuit Simulator - Main Application
 */
public class Project2 
{
    public static void main(String[] args) throws Exception {
        String portToUse = "443";
        double alpha = 0.8;
        double beta = 0.5;

        int numCircuitsForSimpleTor = 100;
        int numCircuitsForGeoAware = 100;

        System.out.println("=== Tor Circuit Simulator ===\n");

        DescriptorCollector descriptorCollector = DescriptorSourceFactory.createDescriptorCollector();
        descriptorCollector.collectDescriptors(
                "https://collector.torproject.org",
                new String[] { "/recent/relay-descriptors/server-descriptors/" },
                0L,
                new File("descriptors"),
                false
        );
        System.out.println("Descriptors Download concluded.");

        // Load nodes from cache or consensus -> to make avoid waiting too much on testing
        Node[] nodes = loadNodes();
        System.out.println("Loaded " + nodes.length + " nodes\n");

        File consensus = new File("resources/consensus.txt");
        GetNodeFamily.updateFamilies(nodes);

//        for(Node n : nodes) {
//            System.out.println(n.toString());
//        }

        ExperimentRunner runner = new ExperimentRunner(nodes);

        
        System.out.println("=== Quick Demo ===");
        System.out.println("Low = Low Risk Path Selection");
        System.out.println("Medium = Medium Risk Path Selection");
        System.out.println("High = High Risk Path Selection");


        runner.runStandardTor(numCircuitsForSimpleTor, portToUse);

        runner.runGeographyAware(alpha, beta, numCircuitsForGeoAware, portToUse);
        
        runner.compareStats();

        
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
