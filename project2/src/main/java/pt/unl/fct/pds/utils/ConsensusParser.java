package pt.unl.fct.pds.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.unl.fct.pds.model.Node;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.exception.GeoIp2Exception;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.URI;

public class ConsensusParser {
    String filename;

    private DatabaseReader geoReader;

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public ConsensusParser() {}

    public ConsensusParser(String filename) {
        this.filename = filename;

        try {
            File database = new File("GeoLite2-Country.mmdb");
            this.geoReader = new DatabaseReader.Builder(database).build();
            System.out.println("GeoIP database loaded successfully!");
        } catch (IOException e) {
            System.err.println("GeoIP database not found");
            this.geoReader = null;
        }
    }

    public String getFilename() {return filename;}
    public void setFilename(String filename) {this.filename = filename;}

    @FunctionalInterface
    public interface NodeCallback {
        void onNodeParsed(Node node);
    }

    public Node[] parseConsensus() {
        return parseConsensusWithCallback(null);
    }

    public Node[] parseConsensusWithCallback(NodeCallback callback) {
        List<Node> nodes = new ArrayList<Node>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));

            String line;
            Node currentNode = null;

            while((line = br.readLine()) != null) {
                String[] split = line.split(" ");

                if(split.length == 0){
                    continue;
                }

                switch (split[0]) {
                    case "r":

                        if(currentNode != null){
                            nodes.add(currentNode);
                            if(callback != null) {
                                callback.onNodeParsed(currentNode);
                            }
                        }

                        currentNode = new Node();
                        currentNode.setNickname(split[1]);
                        currentNode.setFingerprint(split[2]);

                        try {
                            String dateTimeStr = split[4] + " " + split[5];
                            LocalDateTime dateTime = LocalDateTime.parse(
                                    dateTimeStr,
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                            );
                            currentNode.setTimePublished(dateTime);
                        } catch (Exception e) {
                            System.err.println("Erro a fazer parse da data de publicação");
                        }

                        currentNode.setIpAddress(split[6]);
                        currentNode.setOrPort(Integer.parseInt(split[7]));
                        currentNode.setDirPort(Integer.parseInt(split[8]));

                        break;

                    case "a":
                        break;

                    case "s":
                        String[] flags = new String[split.length - 1];
                        for(int i = 1; i < split.length; i++){
                            flags [i-1] = split[i];
                        }
                        currentNode.setFlags(flags);

                        break;

                    case "v":
                        currentNode.setVersion(split[1] + " " + split[2]);
                        break;

                    case "pr":
                        // não vai ser usado para o nosso projeto
                        break;

                    case "w":
                        String[] splitOnEqual = split[1].split("=");
                        currentNode.setBandwidth(Integer.parseInt(splitOnEqual[1]));
                        break;

                    case "p":
                        String policy = String.join(" ", Arrays.copyOfRange(split, 1, split.length));
                        currentNode.setExitPolicy(policy);

                        //no fim para definir a geolocalização
                        String country = getCountryFromIP(currentNode.getIpAddress());
                        currentNode.setCountry(country);

                        break;

                    default:
                        break;
                }
            }

            //da add do ultimo
            if(currentNode != null){
                nodes.add(currentNode);
                if(callback != null) {
                    callback.onNodeParsed(currentNode);
                }
            }

            return nodes.toArray(new Node[0]);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCountryFromIP(String ip) {
        // Try GeoIP database first (fast!)
        if (geoReader != null) {
            try {
                InetAddress ipAddress = InetAddress.getByName(ip);
                CountryResponse response = geoReader.country(ipAddress);
                String countryCode = response.getCountry().getIsoCode();

                if (countryCode != null && !countryCode.isEmpty()) {
                    System.out.println("Country encontrado para o node na db: " + countryCode);
                    return countryCode;
                }
            } catch (IOException | GeoIp2Exception e) {
                // IP not found in database, fall through to API
            }
        }

        // Fallback to HTTP API (slow!)
        try {
            String url = String.format("http://ip-api.com/json/%s?fields=countryCode", ip);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() != 200) {
                return "Unknown";
            }

            JsonNode node = mapper.readTree(resp.body());
            String country = node.path("countryCode").asText("Unknown");

            return country;

        } catch (Exception e) {
            return "Unknown";
        }
    }

    public void close() {
        try {
            if (geoReader != null) {
                geoReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
