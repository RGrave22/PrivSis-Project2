package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;


public class CircuitDisplay {
    
    private static boolean verboseMode = true;
    
    public static void setVerboseMode(boolean verbose) {
        verboseMode = verbose;
    }
    
    public static void printCircuit(Node[] path, int circuitNumber) {
        if (path == null || path.length != 3) {
            System.out.println("Circuit " + circuitNumber + ": FAILED");
            return;
        }
        
        if (verboseMode) {

            System.out.println("Guard:  " + formatNode(path[0]));
            System.out.println("Middle: " + formatNode(path[1]));
            System.out.println("Exit:   " + formatNode(path[2]));
            printGeographyInfo(path);
        } else {
            // Compact format
            System.out.printf("Circuit %d: %s -> %s -> %s\n",
                circuitNumber,
                formatCompact(path[0]),
                formatCompact(path[1]),
                formatCompact(path[2])
            );
        }
    }
    
    private static void printGeographyInfo(Node[] path) {
        String guardCountry = path[0].getCountry();
        String middleCountry = path[1].getCountry();
        String exitCountry = path[2].getCountry();
        
        int uniqueCountries = countUniqueCountries(guardCountry, middleCountry, exitCountry);
        
        String risk = uniqueCountries == 3 ? "LOW" : 
                     uniqueCountries == 2 ? "MEDIUM" : "HIGH";
        
        System.out.printf("Geography: %d countries (%s)\n", uniqueCountries, risk);
    }
    
    private static String formatNode(Node node) {
        return String.format("%-20s %15s (%s) BW: %d",
            truncate(node.getNickname(), 20),
            node.getIpAddress(),
            node.getCountry() != null ? node.getCountry() : "??",
            node.getBandwidth()
        );
    }
    
    private static String formatCompact(Node node) {
        return String.format("%s(%s)",
            truncate(node.getNickname(), 12),
            node.getCountry() != null ? node.getCountry() : "??"
        );
    }
    
    private static String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) : str;
    }
    
    private static int countUniqueCountries(String c1, String c2, String c3) {
        int count = 0;
        if (c1 != null && !c1.isEmpty()) count++;
        if (c2 != null && !c2.isEmpty() && !c2.equals(c1)) count++;
        if (c3 != null && !c3.isEmpty() && !c3.equals(c1) && !c3.equals(c2)) count++;
        return count;
    }
}
