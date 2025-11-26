package pt.unl.fct.pds.utils;


import pt.unl.fct.pds.model.Node;

import java.util.*;

public class GeographicallyAwarePathSelector {

    private final Node[] allNodes;

    private final Map<Node, Integer> sampledGuards = new HashMap<>();
    private final Map<Node, Integer> confirmedGuards = new HashMap<>();

    private final int MAX_SAMPLE_USES = 3;
    private final int MAX_CONFIRMED_USES = 100;

    private List<Node> removedNodes = new ArrayList<>();

    private double alpha = 0.0;  // Weight multiplier for nodes in SAME country
    private double beta = 0.0;   // Weight multiplier for nodes in DIFFERENT country


    public GeographicallyAwarePathSelector(Node[] nodes, double alpha, double beta) {
        this.allNodes = nodes;
        this.alpha = alpha;
        this.beta = beta;
    }

    public Node[] selectPath(String port) {
        System.out.println("[Sampled Guards]");
        for(Node node : sampledGuards.keySet()) {
            System.out.println("Node: " + node.getNickname() + " uses: " + sampledGuards.get(node));
        }
        System.out.println("[Confirmed Guards]");
        for(Node node : confirmedGuards.keySet()) {
            System.out.println("Node: " + node.getNickname() + " uses: " + confirmedGuards.get(node));
        }

        Node exit = selectExit(port);
        if (exit == null) return null;

        Node guard = selectGuard(exit);
        if (guard == null) return null;

        Node middle = selectMiddle(guard, exit);
        if (middle == null) return null;

        if(!removedNodes.contains(guard)) {
            updateSampledGuards(guard);
        }
        removedNodes.remove(guard);

        for(Node node : removedNodes) {
            System.out.println("[Removed Guards]: " + node.getNickname());
        }

        return new Node[]{guard, middle, exit};
    }

    private Node selectExit(String port) {
        List<Node> candidates = new ArrayList<>();

        for (Node node : allNodes) {
            if (Utils.hasFlag(node, "Fast") && Utils.hasSuitableExitPolicy(node, port) && !node.getCountry().equals("Unknown")) {
                candidates.add(node);
            }
        }

        return weightedRandomSelectByBandwidthExit(candidates);
    }

    private Node selectGuard(Node exit) {
        List<Node> candidates = new ArrayList<>();

        for (Node node : allNodes) {
            if (Utils.hasFlag(node, "Guard") &&
                    !Utils.isSameFamily(node, exit) &&
                    !Utils.isSameSubnet(node, exit) &&
                    !node.getCountry().equals("Unknown")) {
                candidates.add(node);
            }
        }


        List<Node> confirmedCandidates = Utils.filterGuardsInList(candidates, confirmedGuards);
        confirmedCandidates.removeIf(guard -> confirmedGuards.getOrDefault(guard, 0) >= MAX_CONFIRMED_USES);

        if (!confirmedCandidates.isEmpty()) {
            Node chosen = weightedRandomSelectByBandwidthGuard(confirmedCandidates, exit);
            confirmedGuards.put(chosen, confirmedGuards.getOrDefault(chosen, 0) + 1);

            if (confirmedGuards.get(chosen) >= MAX_CONFIRMED_USES) {
                System.out.println("Got Max uses, deleting:  " + chosen);
                confirmedGuards.remove(chosen);
                removedNodes.add(chosen);
            }

            System.out.println("[Confirmed]: " + chosen);
            return chosen;
        }

        List<Node> sampledCandidates = Utils.filterGuardsInList(candidates, sampledGuards);
        if (!sampledCandidates.isEmpty()) {
            Node chosen = weightedRandomSelectByBandwidthGuard(sampledCandidates, exit);

            System.out.println("[Sampled]: " + chosen);

            return chosen;
        }

        System.out.println("[No confirmed nor sampled]");
        return weightedRandomSelectByBandwidthGuard(candidates, exit);
    }

    private Node selectMiddle(Node guard, Node exit) {
        List<Node> candidates = new ArrayList<>();

        for (Node node : allNodes) {
            if (Utils.hasFlag(node, "Fast") &&
                    !Utils.isSameFamily(node, guard) &&
                    !Utils.isSameFamily(node, exit) &&
                    !Utils.isSameSubnet(node, guard) &&
                    !Utils.isSameSubnet(node, exit) &&
                    !node.getCountry().equals("Unknown")) {
                candidates.add(node);
            }
        }

//        List<String> selectedCountries = new ArrayList<>();
//        selectedCountries.add(guard.getCountry());
//        selectedCountries.add(exit.getCountry());

        return weightedRandomSelectByBandwidthMiddle(candidates, guard, exit);
    }

    private Node weightedRandomSelectByBandwidthGuard(List<Node> candidates, Node exit) {
        if (candidates.isEmpty()) return null;

        double totalWeight = 0.0;
        double[] weights = new double[candidates.size()];
        String exitCountry = (exit != null) ? exit.getCountry() : null;

        for (int i = 0; i < candidates.size(); i++) {
            Node node = candidates.get(i);
            double weight = node.getBandwidth();

            if (alpha != 0.0 && exitCountry != null && node.getCountry() != null) {
                if (!node.getCountry().equalsIgnoreCase(exitCountry)) {
                    weight *= (1.0 + alpha);
                }
            }

            weights[i] = weight;
            totalWeight += weight;
        }

        if (totalWeight == 0) {
            return candidates.get((int) (Math.random() * candidates.size()));
        }

        double rand = Math.random() * totalWeight;
        double cumulative = 0.0;

        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights[i];
            if (rand <= cumulative) {
                return candidates.get(i);
            }
        }
        return candidates.get(candidates.size() - 1);
    }


    private Node weightedRandomSelectByBandwidthExit(List<Node> candidates) {
        if (candidates.isEmpty()) return null;

        double totalWeight = 0.0;
        for (Node node : candidates) {
            totalWeight += node.getBandwidth();
        }

        double rand = Math.random() * totalWeight;
        double cumulative = 0.0;

        for (Node node : candidates) {
            cumulative += node.getBandwidth();
            if (rand <= cumulative) {
                return node;
            }
        }
        return candidates.get(candidates.size() - 1);
    }

    private Node weightedRandomSelectByBandwidthMiddle(List<Node> candidates, Node guard, Node exit) {
        if (candidates.isEmpty()) return null;

        double totalWeight = 0.0;
        double[] weights = new double[candidates.size()];

        for (int i = 0; i < candidates.size(); i++) {
            Node node = candidates.get(i);
            double weight = node.getBandwidth();

            String nodeCountry = node.getCountry();
            String guardCountry = guard.getCountry();
            String exitCountry = exit.getCountry();

            int c = 1;
            if (nodeCountry != null && guardCountry != null && exitCountry != null) {
                boolean diffGuard = !nodeCountry.equalsIgnoreCase(guardCountry);
                boolean diffExit = !nodeCountry.equalsIgnoreCase(exitCountry);

                if (diffGuard && diffExit) {
                    c = 3;
                } else if (diffGuard || diffExit) {
                    c = 2;
                } else {
                    c = 1;
                }
            }

            weights[i] = weight * (1 + (beta * c));
            totalWeight += weights[i];
        }

        if (totalWeight == 0) {
            return candidates.get((int) (Math.random() * candidates.size()));
        }

        double rand = Math.random() * totalWeight;
        double cumulative = 0.0;

        for (int i = 0; i < candidates.size(); i++) {
            cumulative += weights[i];
            if (rand <= cumulative) {
                return candidates.get(i);
            }
        }

        return candidates.get(candidates.size() - 1);
    }



    public void updateSampledGuards(Node selectedGuard) {
        if (confirmedGuards.containsKey(selectedGuard)) {
            return;
        }

        if (!sampledGuards.containsKey(selectedGuard)) {
            sampledGuards.put(selectedGuard, 1);

            if (sampledGuards.size() > MAX_SAMPLE_USES) {
                Iterator<Node> it = sampledGuards.keySet().iterator();
                if (it.hasNext()) {
                    Node oldest = it.next();
                    sampledGuards.remove(oldest);
                    promoteToConfirmedGuard(oldest);
                }
            }

        } else {
            sampledGuards.put(selectedGuard, sampledGuards.get(selectedGuard) + 1);

            if (sampledGuards.get(selectedGuard) >= MAX_SAMPLE_USES) {
                sampledGuards.remove(selectedGuard);
                promoteToConfirmedGuard(selectedGuard);
            }
        }
    }

    public void promoteToConfirmedGuard(Node guard) {
        if (!confirmedGuards.containsKey(guard)) {
            confirmedGuards.put(guard, 0);
        }
    }




}
