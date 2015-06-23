package pl.edu.agh.toik;

/**
 * Created by pkala on 5/26/15.
 */
class MyLink {
    private double weight;
    private String sourceVertex;
    private String targetVertex;

    public MyLink(double weight, String sourceVertex, String targetVertex) {
        this.sourceVertex = sourceVertex;
        this.targetVertex = targetVertex;
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return sourceVertex + "-" +targetVertex + " # " + weight;
    }
}