package pt.unl.fct.pds;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.utils.ConsensusParser;
import pt.unl.fct.pds.utils.ConsensusCacheManager;

import java.util.Arrays;


/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 
{
    public static void main( String[] args )
    {
        System.out.println("Welcome to the Circuit Simulator!");

        String consensusFile = "resources/consensus.txt";


        ConsensusCacheManager cacheManager = new ConsensusCacheManager(
            consensusFile,
            "nodes_cache.csv",  
            true                // Enable cache
        );
        Node[] nodes = cacheManager.getNodes();
        
        // Uncomment to use original parser without caching

        // ConsensusParser parser = new ConsensusParser(consensusFile);
        // Node[] nodes = parser.parseConsensus();

        System.out.println("Loaded " + nodes.length + " nodes");
        System.out.println("First 3 nodes: " + Arrays.toString(Arrays.copyOfRange(nodes, 0, Math.min(3, nodes.length))));
        
        


    }
}
