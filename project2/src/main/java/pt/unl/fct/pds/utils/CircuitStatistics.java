package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;


public class CircuitStatistics {
    
    private int totalCircuits = 0;
    private int circuits1Country = 0;
    private int circuits2Countries = 0;
    private int circuits3Countries = 0;
    private long totalBandwidth = 0;
    
    public void recordCircuit(Node[] path) {
        if (path == null || path.length != 3) return;
        
        totalCircuits++;
        
        int uniqueCountries = countUniqueCountries(
            path[0].getCountry(),
            path[1].getCountry(),
            path[2].getCountry()
        );
        
        switch (uniqueCountries) {
            case 1: circuits1Country++; break;
            case 2: circuits2Countries++; break;
            case 3: circuits3Countries++; break;
        }
        
        totalBandwidth += path[0].getBandwidth() + path[1].getBandwidth() + path[2].getBandwidth();
    }
    
    public void printSummary() {
        if (totalCircuits == 0) {
            System.out.println("No circuits recorded");
            return;
        }
        
        System.out.println("\n--- Circuit Statistics ---");
        System.out.printf("Total circuits: %d\n", totalCircuits);
        System.out.printf("  1 country:  %d (%.1f%%)\n", 
            circuits1Country, 100.0 * circuits1Country / totalCircuits);
        System.out.printf("  2 countries: %d (%.1f%%)\n", 
            circuits2Countries, 100.0 * circuits2Countries / totalCircuits);
        System.out.printf("  3 countries: %d (%.1f%%)\n", 
            circuits3Countries, 100.0 * circuits3Countries / totalCircuits);
        System.out.printf("Average bandwidth: %d\n", totalBandwidth / totalCircuits);
    }
    
    private int countUniqueCountries(String c1, String c2, String c3) {
        int count = 0;
        if (c1 != null && !c1.isEmpty()) count++;
        if (c2 != null && !c2.isEmpty() && !c2.equals(c1)) count++;
        if (c3 != null && !c3.isEmpty() && !c3.equals(c1) && !c3.equals(c2)) count++;
        return count;
    }
    
    public int getTotalCircuits() { return totalCircuits; }
    public int getCircuits1Country() { return circuits1Country; }
    public int getCircuits2Countries() { return circuits2Countries; }
    public int getCircuits3Countries() { return circuits3Countries; }
    public long getAverageBandwidth() { 
        return totalCircuits > 0 ? totalBandwidth / totalCircuits : 0; 
    }
}
