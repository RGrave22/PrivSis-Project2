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

        System.out.println("=== Tor Circuit Simulator ===\n");

        DescriptorCollector descriptorCollector = DescriptorSourceFactory.createDescriptorCollector();
        descriptorCollector.collectDescriptors(
                "https://collector.torproject.org",
                new String[] { "/recent/relay-descriptors/server-descriptors/" },
                0L,  // baixa todos os arquivos independentemente do tempo modificado
                new File("descriptors"),  // diretório local para salvar os arquivos
                false  // não apaga arquivos locais que não existem mais remoto
        );
        System.out.println("Download concluído.");

        // Load nodes from cache or consensus
        Node[] nodes = loadNodes();
        System.out.println("Loaded " + nodes.length + " nodes\n");

        File consensus = new File("resources/consensus.txt");
        GetNodeFamily.updateFamilies(nodes);

        for(Node n : nodes) {
            System.out.println(n.toString());
        }

        ExperimentRunner runner = new ExperimentRunner(nodes);


        // Quick demo mode (3 circuits each)
        System.out.println("=== Quick Demo ===");
        System.out.println("Low = Low Risk Path Selection");
        System.out.println("Medium = Medium Risk Path Selection");
        System.out.println("High = High Risk Path Selection");

        runner.runStandardTor(3, portToUse);
        runner.runGeographyAware(0.5, 0.5, 3, portToUse);
        
        // Comparison mode (100 circuits each)
        System.out.println("\n=== Performance Comparison ===");
        runner.setQuietMode(true);
        runner.compareConfigurations(100, portToUse);
        
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
