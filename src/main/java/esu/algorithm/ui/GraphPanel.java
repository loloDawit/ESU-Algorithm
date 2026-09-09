/*
 * A drawing of the input graph, with the subgraph under construction
 * highlighted inside it.
 */
package esu.algorithm.ui;

import esu.algorithm.UndirectedGraph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Class GraphPanel
 *
 * Draws the graph being searched and shows where the current subgraph sits in
 * it. Until now the app showed a tree of vertex sets and never the graph, so
 * {2, 4} was a label with nothing to point at.
 *
 * Vertices are placed on a circle. That is deterministic, needs no layout
 * library, and stays readable at the sizes this app is for; a force-directed
 * layout would look better on large graphs, which are unreadable here anyway.
 */
public class GraphPanel extends Pane {

    private static final double VERTEX_RADIUS = 17;
    /** Kept clear of the edge so labels and strokes are not clipped. */
    private static final double MARGIN = 26;


    private UndirectedGraph graph;
    private Set<Integer> subgraph = new HashSet<>();
    private Set<Integer> extension = new HashSet<>();

    public GraphPanel() {
        widthProperty().addListener((obs, was, now) -> draw());
        heightProperty().addListener((obs, was, now) -> draw());
    }

    /**
     * Show a different graph, clearing any highlighting.
     *
     * @param graph the graph to draw, or null to show nothing
     */
    public void setGraph(UndirectedGraph graph) {
        this.graph = graph;
        this.subgraph = new HashSet<>();
        this.extension = new HashSet<>();
        draw();
    }

    /**
     * Highlight where the algorithm currently is.
     *
     * @param subgraph  vertices in the subgraph being built
     * @param extension vertices it could add next
     */
    public void highlight(Collection<Integer> subgraph,
            Collection<Integer> extension) {
        this.subgraph = new HashSet<>(subgraph);
        this.extension = new HashSet<>(extension);
        draw();
    }

    /**
     * Redraw from scratch. The graphs here are small enough that rebuilding
     * every shape is simpler than tracking which ones changed.
     */
    private void draw() {
        getChildren().clear();
        if (graph == null || getWidth() <= 0 || getHeight() <= 0) {
            return;
        }

        List<Integer> vertices = visibleVertices();
        if (vertices.isEmpty()) {
            return;
        }

        double[][] at = positions(vertices);

        // Edges first, so vertices sit on top of them.
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < vertices.size(); j++) {
                if (!adjacent(vertices.get(i), vertices.get(j))) {
                    continue;
                }
                boolean inside = subgraph.contains(vertices.get(i))
                        && subgraph.contains(vertices.get(j));
                Line edge = new Line(at[i][0], at[i][1], at[j][0], at[j][1]);
                edge.getStyleClass().add("g-edge");
                if (inside) {
                    edge.getStyleClass().add("g-edge-in");
                }
                getChildren().add(edge);
            }
        }

        for (int i = 0; i < vertices.size(); i++) {
            getChildren().addAll(vertexShapes(vertices.get(i), at[i]));
        }
    }

    /**
     * A vertex circle and its number, coloured by its part in the current step.
     *
     * @param vertex the vertex to draw
     * @param at     its centre as {x, y}
     * @return the circle and the label
     */
    private List<javafx.scene.Node> vertexShapes(int vertex, double[] at) {
        boolean chosen = subgraph.contains(vertex);
        boolean candidate = !chosen && extension.contains(vertex);

        String state = chosen ? "g-chosen" : candidate ? "g-candidate" : "g-plain";

        Circle circle = new Circle(at[0], at[1], VERTEX_RADIUS);
        circle.getStyleClass().addAll("g-vertex", state);

        Text label = new Text(Integer.toString(vertex));
        label.setFont(Font.font("SF Mono", FontWeight.BOLD, 13));
        label.getStyleClass().addAll("g-label", "g-label-" + state.substring(2));
        // Centre the label on the circle using its own measured size.
        label.setX(at[0] - label.getLayoutBounds().getWidth() / 2);
        label.setY(at[1] + label.getLayoutBounds().getHeight() / 4);

        List<javafx.scene.Node> shapes = new ArrayList<>();
        shapes.add(circle);
        shapes.add(label);
        return shapes;
    }

    /**
     * Evenly spaced points on the largest circle that fits, starting at the
     * top so a given graph always looks the same.
     *
     * @param vertices vertices to place, in drawing order
     * @return one {x, y} pair per vertex
     */
    private double[][] positions(List<Integer> vertices) {
        double centreX = getWidth() / 2;
        double centreY = getHeight() / 2;
        double radius = Math.min(centreX, centreY) - MARGIN;

        double[][] at = new double[vertices.size()][2];
        if (vertices.size() == 1) {
            at[0] = new double[]{centreX, centreY};
            return at;
        }
        for (int i = 0; i < vertices.size(); i++) {
            double angle = 2 * Math.PI * i / vertices.size() - Math.PI / 2;
            at[i][0] = centreX + radius * Math.cos(angle);
            at[i][1] = centreY + radius * Math.sin(angle);
        }
        return at;
    }

    /**
     * @return vertices that take part in at least one edge, in order
     */
    private List<Integer> visibleVertices() {
        List<Integer> vertices = new ArrayList<>();
        for (int vertex = 0; vertex < graph.getSize(); vertex++) {
            if (!graph.getNeighbors(vertex).isEmpty()) {
                vertices.add(vertex);
            }
        }
        return vertices;
    }

    private boolean adjacent(int from, int to) {
        return graph.getNeighbors(from).contains(to);
    }
}
