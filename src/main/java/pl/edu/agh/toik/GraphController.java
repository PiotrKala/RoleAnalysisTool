package pl.edu.agh.toik;

/**
 * Created by pkala on 5/9/15.
 */

import com.sun.org.apache.xpath.internal.operations.Mod;
import edu.uci.ics.jung.graph.Graph;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@Controller
@RequestMapping("/graphs")
public class GraphController implements ServletContextAware {

    private ServletContext servletContext;
    private final String GRAPHS_DIR = "WEB-INF/graphs/";
    private HashSet<String> graphsFiles = new HashSet<String>();
    private Map graphsRoles = new HashMap<String, HashMap<String, Role>>();
    private Map graphsStructuralRoles = new HashMap<String, HashMap<String, Role>>();
    private HashMap<String, Graph<String, MyLink>> graphs = new HashMap<String, Graph<String, MyLink>>();
    private HashMap<String, List<MyLink>> graphsEdges = new HashMap<String, List<MyLink>>();
    private HashMap<String, TreeMap<String, Double>> graphsBeetwenness = new HashMap<String, TreeMap<String, Double>>();
    private HashMap<String, TreeMap<String, Double>> graphsPageRank = new HashMap<String, TreeMap<String, Double>>();
    private GraphType typeDisplayed = GraphType.WITH_ROLES;
    private static int mediatorsPer = 30;
    private static int influentialPer = 40;

    private void updateGrpahsFilesSet() {
        File[] files = new File(getGraphsDirectory()).listFiles();
        graphsFiles.clear();
        if (files == null) return;
        for (File file : files) {
            if (file.isFile() ) {
                Graph<String, MyLink> graph = GraphUtils.getGraph(file.getAbsolutePath(), getProjectDirectory());
                graphsFiles.add(file.getName().replaceAll("gml", "json"));

                if(!graphsBeetwenness.containsKey(file.getName())) {
                    TreeMap<String, Double> betweenness = GraphUtils.verticesBetweenness(graph);
                    TreeMap<String, Double> pageRanks = GraphUtils.verticesPageRank(graph);
                    graphsBeetwenness.put(file.getName(), betweenness);
                    graphsPageRank.put(file.getName(), pageRanks);
                }

                TreeMap<String, Double> betweenness = graphsBeetwenness.get(file.getName());
                TreeMap<String, Double> pageRanks = graphsPageRank.get(file.getName());
                HashMap<String, Role> roles = GraphUtils.markRoles(graph, mediatorsPer, influentialPer, betweenness, pageRanks);
                graphsRoles.put(file.getName(), roles);

                if(!graphs.containsKey(file.getName())) {
                    graphs.put(file.getName(), graph);

                }
                List<MyLink> edges = new ArrayList<MyLink>(graph.getEdges());
                graphsEdges.put(file.getName(), edges);

                this.graphsStructuralRoles.put(file.getName(), GraphUtils.divideStructurally(graph));
            }
        }
    }

    private String getGraphsDirectory() {
        return servletContext.getRealPath("/") + GRAPHS_DIR;
    }

    private String getProjectDirectory() {
        return servletContext.getRealPath("../../");
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String graphPage(ModelMap model) {
        this.typeDisplayed = GraphType.WITH_ROLES;
        updateGrpahsFilesSet();
        prepareModel(model);
        return "graphs";
    }

    @RequestMapping(value="/upload", method=RequestMethod.POST)
    public String handleFileUpload(ModelMap model, @RequestParam("file") MultipartFile file) {
        String name = file.getOriginalFilename();
        prepareModel(model);
        if (!file.isEmpty()) {
            model.addAttribute("uploaded", true);
            try {
                byte[] bytes = file.getBytes();

                // Creating the directory to store file
                File dir = new File(getGraphsDirectory());

                // Create the file on server
                File serverFile = new File(dir.getPath() + File.separator + name);
                BufferedOutputStream stream = new BufferedOutputStream(
                        new FileOutputStream(serverFile, false));
                stream.write(bytes);
                stream.close();

                System.out.println("Server File Location=" + serverFile.getAbsolutePath());
            } catch (Exception e) {

            }
        }
        updateGrpahsFilesSet();
        return "graphs";
    }

    @RequestMapping(value="/switchToStructural", method = RequestMethod.GET)
    public String switchToStructural(ModelMap model){
        this.typeDisplayed = GraphType.STRUCTURAL;
        File[] files = new File(getGraphsDirectory()).listFiles();
        graphsFiles.clear();
        if (files == null) return "graphs";
        for (File file : files) {
            if (file.isFile() ) {
                Graph<String, MyLink> graph = GraphUtils.getGraph(file.getAbsolutePath(), getProjectDirectory());
                graphsFiles.add(file.getName().replaceAll("gml", "json"));

                HashMap<String, String> roles = GraphUtils.divideStructurally(graph);
                this.graphsRoles.put(file.getName(), roles);
            }
        }
        prepareModel(model);
        return "graphs";
    }

    @RequestMapping(value="/updateRoles", method=RequestMethod.POST)
    public String handleRolesInput(ModelMap model, @RequestParam("mediator") Integer mediatorP,@RequestParam("influential") Integer influentialP) {
        this.influentialPer = influentialP;
        this.mediatorsPer = mediatorP;
        updateGrpahsFilesSet();
        prepareModel(model);
        return "graphs";
    }

    private void prepareModel(ModelMap model) {
        model.addAttribute("graphsEdges", graphsEdges);
        model.addAttribute("typeDisplayed", typeDisplayed);
        model.addAttribute("graphsFiles", graphsFiles);
        model.addAttribute("graphsRoles", graphsRoles);
        model.addAttribute("uploaded", false);
        model.addAttribute("mediatorsPer", mediatorsPer);
        model.addAttribute("influentialPer", influentialPer);
        model.addAttribute("graphStructuralRoles", graphsStructuralRoles);
    }

}