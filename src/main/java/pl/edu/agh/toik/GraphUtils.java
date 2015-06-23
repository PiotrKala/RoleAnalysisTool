package pl.edu.agh.toik;

import edu.uci.ics.jung.algorithms.blockmodel.StructurallyEquivalent;
import edu.uci.ics.jung.algorithms.blockmodel.VertexPartition;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import org.apache.commons.collections15.Transformer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by pkala on 5/26/15.
 */
public class GraphUtils {
    private static final int ALPHA = 100;

    static class ValueComparator implements Comparator<String> {

        Map<String, Double> base;
        public ValueComparator(Map<String, Double> base) {
            this.base = base;
        }

        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    private static Transformer<MyLink, Double> wtTransformer = new Transformer<MyLink,Double>() {
        public Double transform(MyLink link) {
            return link.getWeight();
        }
    };

    public static Graph<String, MyLink> getGraph(String filepath, String projectDir) {
        if(filepath.contains("json")) {
            return graphFromJson(filepath);
        } else if(filepath.contains("gml")) {
            return graphFromGml(filepath, projectDir);
        }
        System.out.println("Unknown format");
        return null;
    }

    // Execute python script to convert gml to json
    public static Graph<String, MyLink> graphFromGml(String filepath, String projectDir) {
        String outputFile = filepath.replaceAll("gml", "json");
        ProcessBuilder pb = new ProcessBuilder("./convert.py", "-i", filepath, "-o", outputFile);
        pb.inheritIO();
        pb.directory(new File(projectDir));
        try {
            pb.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(!(new File(outputFile).isFile())) {
            try {
                System.out.println("Waiting for converting to finish...");
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Finished converting to");
        return graphFromJson(outputFile);
    }

    public static Graph<String, MyLink> graphFromJson(String filepath) {
        Graph<String, MyLink> graph = new UndirectedSparseMultigraph<String, MyLink>();

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(filepath));
            JSONObject jsonObject = (JSONObject) obj;

            JSONArray vertices = (JSONArray) jsonObject.get("nodes");
            for(Object vertex : vertices) {
                JSONObject jsonVertex = (JSONObject) vertex;
                graph.addVertex(jsonVertex.get("name").toString());
            }

            JSONArray edges = (JSONArray) jsonObject.get("links");
            for(Object edge : edges) {
                JSONObject jsonEdge = (JSONObject) edge;
                int sourceVertexIndex = Integer.parseInt(jsonEdge.get("source").toString());
                int targetVertexIndex = Integer.parseInt(jsonEdge.get("target").toString());
                Double weight = Double.parseDouble(jsonEdge.get("value").toString());
                String sourceVertex = graph.getVertices().toArray()[sourceVertexIndex].toString();
                String targetVertex = graph.getVertices().toArray()[targetVertexIndex].toString();
                graph.addEdge(new MyLink(weight, sourceVertex, targetVertex), sourceVertex, targetVertex);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return graph;
    }

    public static TreeMap<String, Double> verticesBetweenness(Graph<String, MyLink> graph) {
        BetweennessCentrality<String, MyLink> bcb = new BetweennessCentrality<String, MyLink>(graph, wtTransformer);
        HashMap<String, Double> result= new HashMap<String, Double>();

        for(int i = 0; i < graph.getVertices().size(); i++) {
            String vertex = graph.getVertices().toArray()[i].toString();
            result.put(vertex, bcb.getVertexScore(vertex));
        }
        ValueComparator bvc =  new ValueComparator(result);
        TreeMap<String,Double> sortedResult = new TreeMap<String,Double>(bvc);
        sortedResult.putAll(result);
        return sortedResult;
    }

    public static TreeMap<String, Double> verticesPageRank(Graph<String, MyLink> graph) {
        PageRank<String, MyLink> pageRank = new PageRank(graph, wtTransformer, ALPHA);
        HashMap<String, Double> result= new HashMap<String, Double>();
        for(int i = 0; i < graph.getVertices().size(); i++) {
            String vertex = graph.getVertices().toArray()[i].toString();
            result.put(vertex, pageRank.getVertexScore(vertex));
        }
        ValueComparator bvc =  new ValueComparator(result);
        TreeMap<String,Double> sortedResult = new TreeMap<String,Double>(bvc);
        sortedResult.putAll(result);
        return sortedResult;
    }

    public static HashMap<String, Role> markRoles(
            Graph<String, MyLink> graph, double beetweennesPercentage, double pageRankPercentage, TreeMap<String, Double> betweenness, TreeMap<String, Double> pageRanks) {

        int limitBeetweenness = (int) Math.round(graph.getVertexCount() * beetweennesPercentage / 100);
        int limitPageRank = (int) Math.round(graph.getVertexCount() * pageRankPercentage / 100);
        HashMap<String, Role> result = new HashMap<String, Role>();
        HashMap<String, Role> mediators = new HashMap<String, Role>();
        HashMap<String, Role> standards = new HashMap<String, Role>();
        HashMap<String, Role> influentials = new HashMap<String, Role>();

        int counter = 0;
        for(Map.Entry<String,Double> entry : betweenness.entrySet()) {
            if(counter >= limitBeetweenness) break;
            counter++;
            String key = entry.getKey();
            mediators.put(key, Role.MEDIATOR);
        }

        counter = 0;
        for(Map.Entry<String,Double> entry : pageRanks.entrySet()) {
            if(counter >= limitPageRank) break;
            counter++;
            String key = entry.getKey();
            if(!betweenness.containsKey(key)) {
                influentials.put(key, Role.INFLUENTIAL);
            }
        }

        for(Map.Entry<String,Double> entry : pageRanks.entrySet()) {
            String key = entry.getKey();
            if(!betweenness.containsKey(key) && !pageRanks.containsKey(key) ) {
                standards.put(key, Role.STANDARD);
            }
        }

        result.putAll(standards);
        result.putAll(influentials);
        result.putAll(mediators);
        return result;
    }

    public static HashMap<String, String> divideStructurally(Graph<String, MyLink> graph){
        StructurallyEquivalent<String, MyLink> divider = new StructurallyEquivalent<String, MyLink>();
        VertexPartition<String, MyLink> partition = divider.transform(graph);
        HashMap<String, String> result = new HashMap<String, String>();
        Collection<Set<String>> rolesCollection = partition.getVertexPartitions();
        int i = 1;
        for( Set<String> roleCollection : rolesCollection ){
            String roleName = "strucural" + i;
            for( String vertexName : roleCollection ){
                result.put(vertexName, roleName);
            }
            ++i;
        }
        return result;
    }
}
