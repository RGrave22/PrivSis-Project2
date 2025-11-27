package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.model.Circuit;
import java.util.*;
import java.io.*;

public class CircuitStatistics {

    private List<Circuit> circuits = new ArrayList<>();

    //MÉTRICA 1: CARDINALITY
    private Map<String, Integer> globalNodeCounts = new HashMap<>();
    private Map<String, Integer> guardCounts = new HashMap<>();
    private Map<String, Integer> middleCounts = new HashMap<>();
    private Map<String, Integer> exitCounts = new HashMap<>();

    //MÉTRICA 2: SHANNON ENTROPY
    private Map<String, Integer> countryGlobal = new HashMap<>();
    private Map<String, Integer> countryGuard = new HashMap<>();
    private Map<String, Integer> countryMiddle = new HashMap<>();
    private Map<String, Integer> countryExit = new HashMap<>();

    //MÉTRICA 3: BANDWIDTH
    private List<Integer> circuitBandwidths = new ArrayList<>();

    public void recordCircuit(Node[] path) {
        if (path == null || path.length != 3) return;

        int minBw = Integer.MAX_VALUE;
        for (Node n : path) {
            minBw = Math.min(minBw, n.getBandwidth());
        }

        Circuit circuit = new Circuit(circuits.size() + 1, path, minBw);
        circuits.add(circuit);
        circuitBandwidths.add(minBw);


        for (int i = 0; i < path.length; i++) {
            Node node = path[i];

            globalNodeCounts.merge(node.getNickname(), 1, Integer::sum);

            switch (i) {
                case 0: guardCounts.merge(node.getNickname(), 1, Integer::sum); break;
                case 1: middleCounts.merge(node.getNickname(), 1, Integer::sum); break;
                case 2: exitCounts.merge(node.getNickname(), 1, Integer::sum); break;
            }

            String country = node.getCountry();
            if (country != null) {
                countryGlobal.merge(country, 1, Integer::sum);

                switch (i) {
                    case 0: countryGuard.merge(country, 1, Integer::sum); break;
                    case 1: countryMiddle.merge(country, 1, Integer::sum); break;
                    case 2: countryExit.merge(country, 1, Integer::sum); break;
                }
            }
        }
    }

    //Metric 1
    public int getUniqueNodesGlobal() {
        return globalNodeCounts.size();
    }

    public int getUniqueGuards() {
        return guardCounts.size();
    }

    public int getUniqueMiddles() {
        return middleCounts.size();
    }

    public int getUniqueExits() {
        return exitCounts.size();
    }

    //Metric 2
    public double getGlobalCountryEntropy() {
        int total = circuits.size() * 3; // m * 3 -> para entrar como total do shannonEntropy
        return shannonEntropy(countryGlobal, total);
    }

    public double getGuardCountryEntropy() {
        int total = circuits.size(); // m -> para entrar como total do shannonEntropy
        return shannonEntropy(countryGuard, total);
    }

    public double getMiddleCountryEntropy() {
        int total = circuits.size(); // m -> para entrar como total do shannonEntropy
        return shannonEntropy(countryMiddle, total);
    }

    public double getExitCountryEntropy() {
        int total = circuits.size(); // m -> para entrar como total do shannonEntropy
        return shannonEntropy(countryExit, total);
    }

    private double shannonEntropy(Map<String, Integer> counts, int total) {
        if (total == 0) return 0.0;

        double sum = 0.0;
        for (int c : counts.values()) {
            double p = (double) c / total;
            if (p > 0) {
                sum += (p * (Math.log(p) / Math.log(2)));
            }
        }

        return -1.0 * sum;
    }

    //Metric 3
    public List<Integer> getCircuitBandwidths() {
        return new ArrayList<>(circuitBandwidths);
    }

    public double getAverageBandwidth() {
        return circuitBandwidths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    public int getMinBandwidth() {
        return circuitBandwidths.stream()
                .mapToInt(Integer::intValue)
                .min()
                .orElse(0);
    }

    public int getMaxBandwidth() {
        return circuitBandwidths.stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
    }


    public int getTotalCircuits() {
        return circuits.size();
    }

    public List<Circuit> getCircuits() {
        return new ArrayList<>(circuits);
    }


    public void exportBandwidthsToCsv(boolean isSimple) {
        String filename = isSimple ? "bandwidth_simple.csv" : "bandwidth_geo.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("circuit_id,bandwidth");
            for (int i = 0; i < circuitBandwidths.size(); i++) {
                writer.printf("%d,%d%n", i + 1, circuitBandwidths.get(i));
            }
            System.out.println("Exported bandwidths to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing " + filename + ": " + e.getMessage());
        }
    }


    public void printSummary() {
//        System.out.println("\n=== CIRCUIT STATISTICS SUMMARY ===");
        System.out.println("Total circuits: " + getTotalCircuits());

        System.out.println("\n--- Cardinality ---");
        System.out.printf("Unique nodes (global): %d%n", getUniqueNodesGlobal());
        System.out.printf("Unique guards: %d%n", getUniqueGuards());
        System.out.printf("Unique middles: %d%n", getUniqueMiddles());
        System.out.printf("Unique exits: %d%n", getUniqueExits());

        System.out.println("\n--- Shannon Entropy ---");
        System.out.printf("Global: %.4f bits%n", getGlobalCountryEntropy());
        System.out.printf("Guards: %.4f bits%n", getGuardCountryEntropy());
        System.out.printf("Middles: %.4f bits%n", getMiddleCountryEntropy());
        System.out.printf("Exits: %.4f bits%n", getExitCountryEntropy());

        System.out.println("\n--- Bandwidth ---");
        System.out.printf("Average: %.2f KB/s%n", getAverageBandwidth());
        System.out.printf("Min: %d KB/s%n", getMinBandwidth());
        System.out.printf("Max: %d KB/s%n", getMaxBandwidth());
        System.out.println();

        int i = 0;
        for(String n : globalNodeCounts.keySet()){
            System.out.println(i + ": " + n);
            i++;
        }

        int j = 0;
        for(String c : countryGlobal.keySet()){
            System.out.println(j + ": " + c);
            j++;
        }
    }
}
