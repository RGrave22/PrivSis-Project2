package pt.unl.fct.pds.experiments;

import pt.unl.fct.pds.model.Node;
import pt.unl.fct.pds.utils.CircuitStatistics;
import pt.unl.fct.pds.utils.CircuitDisplay;
import pt.unl.fct.pds.utils.SimpleTorPathSelector;
import pt.unl.fct.pds.utils.GeographicallyAwarePathSelector;

public class ExperimentRunner {

    private final Node[] nodes;

    public CircuitStatistics simpleStats;
    public CircuitStatistics geoStats;

    public ExperimentRunner(Node[] nodes) {
        this.nodes = nodes;
        simpleStats = new CircuitStatistics();
        geoStats = new CircuitStatistics();
    }

    public CircuitStatistics runStandardTor(int numCircuits, String port) {
        System.out.println("\n=== Standard Tor Path Selection ===");

        SimpleTorPathSelector selector = new SimpleTorPathSelector(nodes);


        for (int i = 1; i <= numCircuits; i++) {
            System.out.println("\n--- Circuit " + i + " ---");
            Node[] path = selector.selectPath(port);

            if (path != null) {
                simpleStats.recordCircuit(path);
                CircuitDisplay.printCircuit(path, i);
            }
        }

        simpleStats.exportBandwidthsToCsv(true);

        return simpleStats;
    }


    public CircuitStatistics runGeographyAware(double alpha, double beta, int numCircuits, String port) {
        System.out.println("\n=== Geography-Aware Selection ===");
        System.out.printf("Parameters: α=%.2f, β=%.2f\n", alpha, beta);

        GeographicallyAwarePathSelector selector = new GeographicallyAwarePathSelector(nodes, alpha, beta);

        for (int i = 1; i <= numCircuits; i++) {
            System.out.println("\n--- Geo Circuit " + i + " ---");
            Node[] path = selector.selectPath(port);

            if (path != null) {
                geoStats.recordCircuit(path);
                CircuitDisplay.printCircuit(path, i);
            }
        }

        geoStats.exportBandwidthsToCsv(false);

        return geoStats;
    }


    public void compareStats() {
        if (simpleStats == null || geoStats == null) {
            System.out.println("Run both StandardTor and GeographyAware before comparing.");
            return;
        }

        System.out.println();
        System.out.println();
        System.out.println("=== SIMPLE TOR SUMMARY ===");
        simpleStats.printSummary();

        System.out.println();
        System.out.println("=== GEO AWARE SUMMARY ===");
        geoStats.printSummary();


        System.out.println("\n=== COMPARING SIMPLE TOR vs GEO-AWARE ===");

        System.out.println("\n[General]");
        System.out.printf("Total circuits (simple): %d%n", simpleStats.getTotalCircuits());
        System.out.printf("Total circuits (geo):    %d%n", geoStats.getTotalCircuits());

        System.out.println("\n[Metric 1] Node set cardinality");
        System.out.printf("Global  - simple: %d | geo: %d%n",
                simpleStats.getUniqueNodesGlobal(), geoStats.getUniqueNodesGlobal());
        System.out.printf("Guards  - simple: %d | geo: %d%n",
                simpleStats.getUniqueGuards(), geoStats.getUniqueGuards());
        System.out.printf("Middles - simple: %d | geo: %d%n",
                simpleStats.getUniqueMiddles(), geoStats.getUniqueMiddles());
        System.out.printf("Exits   - simple: %d | geo: %d%n",
                simpleStats.getUniqueExits(), geoStats.getUniqueExits());

        System.out.println("\n[Metric 2] Shannon entropy H(X) over countries");
        System.out.printf("Global  - simple: %.4f | geo: %.4f%n",
                simpleStats.getGlobalCountryEntropy(), geoStats.getGlobalCountryEntropy());
        System.out.printf("Guards  - simple: %.4f | geo: %.4f%n",
                simpleStats.getGuardCountryEntropy(), geoStats.getGuardCountryEntropy());
        System.out.printf("Middles - simple: %.4f | geo: %.4f%n",
                simpleStats.getMiddleCountryEntropy(), geoStats.getMiddleCountryEntropy());
        System.out.printf("Exits   - simple: %.4f | geo: %.4f%n",
                simpleStats.getExitCountryEntropy(), geoStats.getExitCountryEntropy());

        System.out.println("\n[Metric 3] Circuit bandwidth (min node bandwidth)");
        System.out.printf("Average - simple: %.2f KB/s | geo: %.2f KB/s%n",
                simpleStats.getAverageBandwidth(), geoStats.getAverageBandwidth());
        System.out.printf("Min     - simple: %d KB/s   | geo: %d KB/s%n",
                simpleStats.getMinBandwidth(), geoStats.getMinBandwidth());
        System.out.printf("Max     - simple: %d KB/s   | geo: %d KB/s%n",
                simpleStats.getMaxBandwidth(), geoStats.getMaxBandwidth());
        System.out.println();


    }

}

