package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PathSelector {
    
    private final Node[] allNodes;
    private final Random random;
    
    private double alpha = 0.0;  // Weight multiplier for nodes in SAME country
    private double beta = 0.0;   // Weight multiplier for nodes in DIFFERENT country
    
    public PathSelector(Node[] nodes) {
        this.allNodes = nodes;
        this.random = new Random();
    }
    
    public PathSelector(Node[] nodes, long seed) {
        this.allNodes = nodes;
        this.random = new Random(seed);
    }
    

    public void setGeographyParameters(double alpha, double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }
    

    public Node[] selectPath() {
        Node exit = selectExit();
        if (exit == null) return null;
        
        Node guard = selectGuard(exit);
        if (guard == null) return null;
        
        Node middle = selectMiddle(guard, exit);
        if (middle == null) return null;
        
        return new Node[]{guard, middle, exit};
    }
    

    private Node selectExit() {
        List<Node> candidates = new ArrayList<>();
        
        for (Node node : allNodes) {
            if (hasFlag(node, "Fast") && hasSuitableExitPolicy(node)) {
                candidates.add(node);
            }
        }
        
        if (candidates.isEmpty()) return null;
        return selectByBandwidth(candidates, null);
    }

    private Node selectGuard(Node exit) {
        List<Node> candidates = new ArrayList<>();
        
        for (Node node : allNodes) {
            if (hasFlag(node, "Guard") && 
                !isSameFamily(node, exit) && 
                !isSameSubnet(node, exit)) {
                candidates.add(node);
            }
        }
        
        if (candidates.isEmpty()) return null;
        
        List<String> selectedCountries = new ArrayList<>();
        selectedCountries.add(exit.getCountry());
        
        return selectByBandwidth(candidates, selectedCountries);
    }
    

    private Node selectMiddle(Node guard, Node exit) {
        List<Node> candidates = new ArrayList<>();
        
        for (Node node : allNodes) {
            if (hasFlag(node, "Fast") &&
                !isSameFamily(node, guard) &&
                !isSameFamily(node, exit) &&
                !isSameSubnet(node, guard) &&
                !isSameSubnet(node, exit) &&
                !node.equals(guard) &&
                !node.equals(exit)) {
                candidates.add(node);
            }
        }
        
        if (candidates.isEmpty()) return null;
        
        List<String> selectedCountries = new ArrayList<>();
        selectedCountries.add(guard.getCountry());
        selectedCountries.add(exit.getCountry());
        
        return selectByBandwidth(candidates, selectedCountries);
    }
    

    private Node selectByBandwidth(List<Node> candidates, List<String> selectedCountries) {
        if (candidates.isEmpty()) return null;
        
        double totalWeight = 0.0;
        double[] weights = new double[candidates.size()];
        
        for (int i = 0; i < candidates.size(); i++) {
            Node node = candidates.get(i);
            double weight = node.getBandwidth();
            
            if (selectedCountries != null && (alpha != 0.0 || beta != 0.0)) {
                String nodeCountry = node.getCountry();
                
                if (nodeCountry != null) {
                    boolean sameCountry = false;
                    for (String country : selectedCountries) {
                        if (country != null && country.equals(nodeCountry)) {
                            sameCountry = true;
                            break;
                        }
                    }
                    
                    if (sameCountry) {
                        weight *= (1.0 - alpha);
                    } else {
                        weight *= (1.0 + beta);
                    }
                }
            }
            
            weights[i] = weight;
            totalWeight += weight;
        }
        
        if (totalWeight == 0) {
            return candidates.get(random.nextInt(candidates.size()));
        }
        
        double rand = random.nextDouble() * totalWeight;
        double cumulative = 0.0;
        
        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights[i];
            if (rand <= cumulative) {
                return candidates.get(i);
            }
        }
        
        return candidates.get(candidates.size() - 1);
    }
    

    private boolean hasFlag(Node node, String flag) {
        if (node.getFlags() == null) return false;
        for (String f : node.getFlags()) {
            if (f.equals(flag)) return true;
        }
        return false;
    }
    

    private boolean hasSuitableExitPolicy(Node node) {
        String policy = node.getExitPolicy();
        if (policy == null || policy.isEmpty()) return false;
        
       
        return policy.toLowerCase().contains("accept") || 
               !policy.toLowerCase().contains("reject *:*");
    }
    
    
    private boolean isSameFamily(Node node1, Node node2) {
        if (node1 == null || node2 == null) return false;
        
       
        return node1.getFingerprint().equals(node2.getFingerprint());
    }
    

    private boolean isSameSubnet(Node node1, Node node2) {
        if (node1 == null || node2 == null) return false;
        
        String ip1 = node1.getIpAddress();
        String ip2 = node2.getIpAddress();
        
        if (ip1 == null || ip2 == null) return false;
        
        // Extract /16 subnet 
        String[] parts1 = ip1.split("\\.");
        String[] parts2 = ip2.split("\\.");
        
        if (parts1.length < 2 || parts2.length < 2) return false;
        
        return parts1[0].equals(parts2[0]) && parts1[1].equals(parts2[1]);
    }
}
