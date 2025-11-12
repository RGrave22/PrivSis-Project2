package pt.unl.fct.pds;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.model.Circuit;
import pt.unl.fct.pds.utils.ConsensusParser;

import java.util.Arrays;


/**
 * Application for Tor Path Selection alternatives.
 *
 */
public class Project2 
{
    public static void main( String[] args )
    {
        // Here we write our logic to choose circuits!
        System.out.println("Welcome to the Circuit Simulator!");

        //Ficheiro com o consensus
        String consensusFile = "resources/consensus.txt";

        ConsensusParser parser = new ConsensusParser(consensusFile);
        Node[] nodes = parser.parseConsensus();

        System.out.println(Arrays.toString(nodes));


    }
}
