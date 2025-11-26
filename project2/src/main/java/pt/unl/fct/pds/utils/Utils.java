package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class Utils {

    //TODO Esta bem?
    public static boolean isSameFamily(Node node1, Node node2) {
        if (node1 == null || node2 == null) return false;

        String node1HexFingerprint = "$" + base64ToHex(node1.getFingerprint()).toUpperCase();
        String node2HexFingerprint = "$" + base64ToHex(node2.getFingerprint()).toUpperCase();

        String family1 = node1.getFamily();
        String family2 = node2.getFamily();

        boolean node1ListsNode2 = family1 != null && family1.contains(node2HexFingerprint);
        boolean node2ListsNode1 = family2 != null && family2.contains(node1HexFingerprint);

        boolean result = node1ListsNode2 || node2ListsNode1;

//        System.out.println("Same family? " + result);

        return result;
    }

    public static boolean isSameSubnet(Node node1, Node node2) {
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

    public static boolean hasSuitableExitPolicy(Node node, String port) {
        String policy = node.getExitPolicy();
        if (policy == null || policy.isEmpty()) return false;

        policy = policy.toLowerCase();
        if (policy.contains("reject 1-65535")) {
            // se a policy rejeita tudo retorna false
            return false;
        }

        boolean isAccept = policy.contains("accept");
        if (!isAccept) return false; // se apenas tem reject

        String[] splitPolicy = policy.split(" ");
        String[] ports = splitPolicy[1].split(",");

        for (String p : ports) {
            p = p.trim();
            if (p.isEmpty()) continue;
            if (p.contains("-")) {

                String[] parts = p.split("-");
                if (parts.length == 2) {
                    try {
                        int min = Integer.parseInt(parts[0]);
                        int max = Integer.parseInt(parts[1]);

                        if (Integer.parseInt(port) >= min && Integer.parseInt(port) <= max) return true;
                    } catch (NumberFormatException ignore) {}
                }
            } else {
                try {
                    if (Integer.parseInt(p) == Integer.parseInt(port)) return true;
                } catch (NumberFormatException ignore) {}
            }
        }
        return false;
    }

    public static boolean hasFlag(Node node, String flag) {
        if (node.getFlags() == null) return false;
        for (String f : node.getFlags()) {
            if (f.equals(flag)) return true;
        }
        return false;
    }

    public static String base64ToHex(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }




    public static List<Node> filterGuardsInList(List<Node> base, Map<Node, Integer> filter) {
        List<Node> result = new ArrayList<>();
        for (Node n : base) {
            if (filter.keySet().contains(n)) {
                result.add(n);
            }
        }
        return result;
    }


}
