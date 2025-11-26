package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;
import org.torproject.descriptor.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class GetNodeFamily {

    public static void updateFamilies(Node[] nodes) throws Exception {
        Map<String, Node> fingerprintToNode = new HashMap<>();
        for (Node node : nodes) {
            fingerprintToNode.put(base64ToHex(node.getFingerprint()).toUpperCase(), node);
        }

        File serverDescDir = new File("descriptors/recent/relay-descriptors/server-descriptors");
        DescriptorReader reader = DescriptorSourceFactory.createDescriptorReader();

        for (Descriptor desc : reader.readDescriptors(serverDescDir)) {
            if (desc instanceof RelayServerDescriptor) {
                RelayServerDescriptor relayDesc = (RelayServerDescriptor) desc;
                String fp = relayDesc.getFingerprint();

//                System.out.println("fp normal: " + fp);

                String normalizedFp = fp.toUpperCase().replace(" ", "");
//                System.out.println("fp normalized: " + normalizedFp);
                Node node = fingerprintToNode.get(normalizedFp);
                if (node != null) {
                    System.out.println("Node != null...");
                    List<String> familyEntries = relayDesc.getFamilyEntries();
                    System.out.println(familyEntries);
                    node.setFamily(familyEntries != null ? familyEntries.toString() : "[]");
                }
            }
        }
    }

    public static String base64ToHex(String base64) {
        byte[] bytes = Base64.getDecoder().decode(base64);
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}