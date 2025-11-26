package pt.unl.fct.pds.utils;

import pt.unl.fct.pds.model.Node;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class ConsensusCacheManager {
    
    private final String consensusFile;
    private final String cacheFile;
    private final boolean useCache;
    private final ConsensusParser parser;
    
    private static final String CSV_SEPARATOR = ",";
    private static final String CSV_QUOTE = "\"";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final String CSV_HEADER = 
        "Nickname,Fingerprint,TimePublished,IPAddress,ORPort,DirPort,Flags,Version,Bandwidth,Country,ExitPolicy";
    
    
    public ConsensusCacheManager(String consensusFile, String cacheFile, boolean useCache) {
        this.consensusFile = consensusFile;
        this.cacheFile = cacheFile;
        this.useCache = useCache;
        this.parser = new ConsensusParser(consensusFile);
    }
    
    
    public ConsensusCacheManager(String consensusFile, boolean useCache) {
        this(consensusFile, "nodes_cache.csv", useCache);
    }
    
    
    public Node[] getNodes() {
        if (useCache && isCacheValid()) {
            System.out.println("Loading from cache...");
            try {
                Node[] cachedNodes = loadFromCache();
                return cachedNodes;
            } catch (Exception e) {
                System.out.println("Cache invalid, parsing consensus...");
            }
        }

        Node[] nodes = parseAndCacheProgressively();
        
        return nodes;
    }
    
    
    private Node[] parseAndCacheProgressively() {
        List<Node> nodes = new ArrayList<>();
        BufferedWriter csvWriter = null;
        
        try {
            Path cachePath = Paths.get(cacheFile);
            if (cachePath.getParent() != null) {
                Files.createDirectories(cachePath.getParent());
            }
            
            csvWriter = new BufferedWriter(new FileWriter(cacheFile));
            csvWriter.write(CSV_HEADER);
            csvWriter.newLine();
            csvWriter.flush();
            
            BufferedWriter finalWriter = csvWriter;
            int[] counter = {0};
            
            Node[] parsedNodes = parser.parseConsensusWithCallback(node -> {
                try {
                    finalWriter.write(nodeToCSVRow(node));
                    finalWriter.newLine();
                    finalWriter.flush();
                    
                    counter[0]++;
                    if (counter[0] % 100 == 0) {
                        System.out.print("\r Cached " + counter[0] + " nodes...");
                    }
                } catch (IOException e) {
                    System.err.println("Error writing to cache: " + e.getMessage());
                }
            });
            
            System.out.println("\r Cached " + parsedNodes.length + " nodes          ");
            return parsedNodes;
            
        } catch (IOException e) {
            System.err.println("Error creating cache file: " + e.getMessage());
            return parser.parseConsensus();
        } finally {
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                } catch (IOException e) {
                    System.err.println("Error closing cache file: " + e.getMessage());
                }
            }
        }
    }

    private boolean isCacheValid() {
        Path cachePath = Paths.get(cacheFile);
        
        if (!Files.exists(cachePath)) {
            System.out.println("Cache file does not exist: " + cacheFile);
            return false;
        }
        
        if (!Files.isReadable(cachePath)) {
            System.err.println("Cache file is not readable: " + cacheFile);
            return false;
        }
        
        try {
            long fileSize = Files.size(cachePath);
            if (fileSize < CSV_HEADER.length()) {
                System.out.println("Cache file is too small: " + cacheFile);
                return false;
            }
        } catch (IOException e) {
            System.err.println("Error checking cache file size: " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    
    private void saveToCache(Node[] nodes) throws IOException {
        Path cachePath = Paths.get(cacheFile);
        if (cachePath.getParent() != null) {
            Files.createDirectories(cachePath.getParent());
        }
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile))) {
            writer.write(CSV_HEADER);
            writer.newLine();
            
            for (Node node : nodes) {
                writer.write(nodeToCSVRow(node));
                writer.newLine();
            }
        }
    }
    
   
    private Node[] loadFromCache() throws IOException {
        List<Node> nodes = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(cacheFile))) {
            String header = reader.readLine();
            if (header == null || !header.equals(CSV_HEADER)) {
                throw new IllegalArgumentException("Invalid CSV header. Expected: " + CSV_HEADER);
            }
            
            String line;
            int lineNumber = 1; 
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue; 
                }
                
                try {
                    Node node = csvRowToNode(line);
                    nodes.add(node);
                } catch (Exception e) {
                    System.err.println("Warning: Skipping invalid row at line " + lineNumber + ": " + e.getMessage());
                    
                }
            }
        }
        
        if (nodes.isEmpty()) {
            throw new IllegalArgumentException("Cache file contains no valid node data");
        }
        
        return nodes.toArray(new Node[0]);
    }
    
   
    private String nodeToCSVRow(Node node) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(escapeCsvField(node.getNickname()));
        sb.append(CSV_SEPARATOR);
        
        sb.append(escapeCsvField(node.getFingerprint()));
        sb.append(CSV_SEPARATOR);
        
        String timeStr = (node.getTimePublished() != null) 
            ? node.getTimePublished().format(DATE_FORMATTER) 
            : "";
        sb.append(escapeCsvField(timeStr));
        sb.append(CSV_SEPARATOR);
        
        sb.append(escapeCsvField(node.getIpAddress()));
        sb.append(CSV_SEPARATOR);
        
        sb.append(node.getOrPort());
        sb.append(CSV_SEPARATOR);
        
        sb.append(node.getDirPort());
        sb.append(CSV_SEPARATOR);
        
        String flagsStr = (node.getFlags() != null) 
            ? String.join("|", node.getFlags()) 
            : "";
        sb.append(escapeCsvField(flagsStr));
        sb.append(CSV_SEPARATOR);
        
        sb.append(escapeCsvField(node.getVersion()));
        sb.append(CSV_SEPARATOR);
        
        sb.append(node.getBandwidth());
        sb.append(CSV_SEPARATOR);
        
        sb.append(escapeCsvField(node.getCountry()));
        sb.append(CSV_SEPARATOR);
        
        sb.append(escapeCsvField(node.getExitPolicy()));
        
        return sb.toString();
    }
    
    
    private Node csvRowToNode(String csvRow) {
        String[] fields = parseCSVLine(csvRow);
        
        if (fields.length != 11) {
            throw new IllegalArgumentException("Expected 11 fields, found " + fields.length);
        }
        
        Node node = new Node();
        
        try {
            node.setNickname(fields[0]);
            node.setFingerprint(fields[1]);
            
            if (!fields[2].isEmpty()) {
                node.setTimePublished(LocalDateTime.parse(fields[2], DATE_FORMATTER));
            }
            
            node.setIpAddress(fields[3]);
            node.setOrPort(Integer.parseInt(fields[4]));
            node.setDirPort(Integer.parseInt(fields[5]));
            
            if (!fields[6].isEmpty()) {
                node.setFlags(fields[6].split("\\|"));
            } else {
                node.setFlags(new String[0]);
            }
            
            node.setVersion(fields[7]);
            node.setBandwidth(Integer.parseInt(fields[8]));
            node.setCountry(fields[9]);
            node.setExitPolicy(fields[10]);
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing CSV fields: " + e.getMessage(), e);
        }
        
        return node;
    }
    
    
    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        
        if (field.contains(CSV_SEPARATOR) || field.contains(CSV_QUOTE) || field.contains("\n") || field.contains("\r")) {
            String escaped = field.replace(CSV_QUOTE, CSV_QUOTE + CSV_QUOTE);
            return CSV_QUOTE + escaped + CSV_QUOTE;
        }
        
        return field;
    }
    
    
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++; 
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
    

    public Node[] refreshCache() {
        System.out.println("Forcing cache refresh...");
        Node[] nodes = parser.parseConsensus();
        
        try {
            saveToCache(nodes);
            System.out.println("Cache refreshed successfully with " + nodes.length + " nodes");
        } catch (IOException e) {
            System.err.println("Warning: Failed to save refreshed cache: " + e.getMessage());
        }
        
        return nodes;
    }
    
   
    public boolean deleteCache() {
        try {
            Path cachePath = Paths.get(cacheFile);
            if (Files.exists(cachePath)) {
                Files.delete(cachePath);
                System.out.println("Cache file deleted: " + cacheFile);
                return true;
            }
        } catch (IOException e) {
            System.err.println("Error deleting cache file: " + e.getMessage());
        }
        return false;
    }
    
    
    public String getCacheInfo() {
        Path cachePath = Paths.get(cacheFile);
        StringBuilder info = new StringBuilder();
        
        info.append("Cache File: ").append(cacheFile).append("\n");
        info.append("Exists: ").append(Files.exists(cachePath)).append("\n");
        
        if (Files.exists(cachePath)) {
            try {
                info.append("Size: ").append(Files.size(cachePath)).append(" bytes\n");
                info.append("Last Modified: ").append(Files.getLastModifiedTime(cachePath)).append("\n");
                
                long lineCount = Files.lines(cachePath).count();
                info.append("Total Lines: ").append(lineCount).append("\n");
                info.append("Node Count: ").append(lineCount - 1).append(" (excluding header)\n");
            } catch (IOException e) {
                info.append("Error reading cache info: ").append(e.getMessage()).append("\n");
            }
        }
        
        info.append("Cache Enabled: ").append(useCache).append("\n");
        
        return info.toString();
    }
}
