/*
 * The shapes among the subgraphs found, drawn and counted.
 */
package esu.algorithm.ui;

import esu.algorithm.Shape;
import esu.algorithm.UndirectedGraph;
import java.util.List;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

/**
 * Class ShapesPanel
 *
 * What the search found, grouped by shape rather than listed by vertices:
 * six triangles and four paths, instead of ten sets of numbers.
 *
 * Choosing a row picks that shape's subgraphs out in the tree, which is the
 * question the panel invites: where are they?
 */
public class ShapesPanel extends VBox {

    /** Size of the little drawing beside each row. */
    private static final double TILE = 34;
    private static final double VERTEX_RADIUS = 3.4;
    /** Size of a drawn subgraph in the grid below, which carries numbers. */
    private static final double INSTANCE_TILE = 62;
    private static final double INSTANCE_VERTEX_RADIUS = 8.5;

    private final ListView<Shape.Count> shapes = new ListView<>();
    private final Label summary = new Label();
    private final Label instancesTitle = new Label("Where they are");
    private final FlowPane instances = new FlowPane(8, 8);
    private final Consumer<List<Integer>> onInstanceChosen;
    private UndirectedGraph graph;

    /**
     * @param onChosen given the chosen shape, or null when nothing is chosen
     */
    /**
     * @param onChosen         given the chosen shape, or null when none is
     * @param onInstanceChosen given one subgraph's vertices when its drawing
     *                         is clicked, so it can be shown in the graph
     */
    public ShapesPanel(Consumer<Shape> onChosen,
            Consumer<List<Integer>> onInstanceChosen) {
        this.onInstanceChosen = onInstanceChosen;
        Label title = new Label("Shapes found");
        title.getStyleClass().add("panel-title");
        summary.getStyleClass().add("caption");

        shapes.getStyleClass().add("shapes-list");
        shapes.setCellFactory(view -> new ShapeCell());
        shapes.getSelectionModel().selectedItemProperty().addListener(
                (obs, was, now) -> onChosen.accept(now == null ? null : now.shape()));

        instancesTitle.getStyleClass().add("panel-title");
        instancesTitle.setVisible(false);
        instances.getStyleClass().add("instances");

        ScrollPane instancesScroll = new ScrollPane(instances);
        instancesScroll.setFitToWidth(true);
        instancesScroll.getStyleClass().add("instances-scroll");
        VBox.setVgrow(instancesScroll, Priority.ALWAYS);

        shapes.setPrefHeight(140);
        getChildren().addAll(title, summary, shapes,
                instancesTitle, instancesScroll);
        setSpacing(6);
        getStyleClass().add("shapes-panel");
    }

    /**
     * The graph the subgraphs come from, needed to draw them.
     *
     * @param graph the graph being searched
     */
    public void setGraph(UndirectedGraph graph) {
        this.graph = graph;
    }

    /**
     * Draw each subgraph having the chosen shape, keeping its real vertex
     * numbers: identical shape, different vertices, which is the whole point
     * of grouping them.
     *
     * @param subgraphs the vertices of each subgraph to draw
     */
    public void showInstances(List<List<Integer>> subgraphs) {
        instances.getChildren().clear();
        instancesTitle.setVisible(!subgraphs.isEmpty());

        for (List<Integer> subgraph : subgraphs) {
            VBox tile = new VBox(2, drawSubgraph(subgraph), caption(subgraph));
            tile.setAlignment(Pos.CENTER);
            tile.getStyleClass().add("instance");
            tile.setOnMouseClicked(event -> {
                for (javafx.scene.Node other : instances.getChildren()) {
                    other.getStyleClass().remove("is-on");
                }
                tile.getStyleClass().add("is-on");
                onInstanceChosen.accept(subgraph);
            });
            instances.getChildren().add(tile);
        }
    }

    private Label caption(List<Integer> subgraph) {
        StringBuilder text = new StringBuilder();
        for (Integer vertex : subgraph) {
            text.append(text.length() == 0 ? "" : " ").append(vertex);
        }
        Label label = new Label(text.toString());
        label.getStyleClass().add("instance-label");
        return label;
    }

    /**
     * One subgraph drawn the way the input graph is: vertices on a circle,
     * numbered, joined where the graph joins them.
     *
     * @param subgraph the vertices to draw
     * @return the drawing
     */
    private Group drawSubgraph(List<Integer> subgraph) {
        Group drawing = new Group();
        int size = subgraph.size();
        double centre = INSTANCE_TILE / 2;
        double radius = centre - INSTANCE_VERTEX_RADIUS - 3;

        double[][] at = new double[size][2];
        for (int index = 0; index < size; index++) {
            double angle = 2 * Math.PI * index / size - Math.PI / 2;
            at[index][0] = centre + radius * Math.cos(angle);
            at[index][1] = centre + radius * Math.sin(angle);
        }

        for (int from = 0; from < size; from++) {
            for (int to = from + 1; to < size; to++) {
                if (graph == null || !graph.getNeighbors(subgraph.get(from))
                        .contains(subgraph.get(to))) {
                    continue;
                }
                Line edge = new Line(at[from][0], at[from][1],
                        at[to][0], at[to][1]);
                edge.getStyleClass().add("instance-edge");
                drawing.getChildren().add(edge);
            }
        }
        for (int index = 0; index < size; index++) {
            Circle dot = new Circle(at[index][0], at[index][1],
                    INSTANCE_VERTEX_RADIUS);
            dot.getStyleClass().add("instance-vertex");
            drawing.getChildren().add(dot);

            Text number = new Text(String.valueOf(subgraph.get(index)));
            number.getStyleClass().add("instance-number");
            number.setBoundsType(TextBoundsType.VISUAL);
            number.setX(at[index][0] - number.getLayoutBounds().getWidth() / 2);
            number.setY(at[index][1] + number.getLayoutBounds().getHeight() / 2);
            drawing.getChildren().add(number);
        }
        return drawing;
    }

    /**
     * Show the shapes of a different search.
     *
     * @param counts each shape with how often it occurred
     */
    public void setShapes(List<Shape.Count> counts) {
        shapes.setItems(FXCollections.observableArrayList(counts));
        int total = counts.stream().mapToInt(Shape.Count::count).sum();
        summary.setText(counts.size() + " shape" + (counts.size() == 1 ? "" : "s")
                + " among " + total + " subgraph" + (total == 1 ? "" : "s"));
    }

    public void clear() {
        shapes.setItems(FXCollections.observableArrayList());
        summary.setText("");
        instances.getChildren().clear();
        instancesTitle.setVisible(false);
    }

    /** A row: the shape drawn, what it is called, and how many there were. */
    private static class ShapeCell extends ListCell<Shape.Count> {

        @Override
        protected void updateItem(Shape.Count count, boolean empty) {
            super.updateItem(count, empty);
            if (empty || count == null) {
                setGraphic(null);
                return;
            }

            Label name = new Label(describe(count.shape()));
            name.getStyleClass().add("shape-name");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label tally = new Label(Integer.toString(count.count()));
            tally.getStyleClass().add("shape-count");

            HBox row = new HBox(9, draw(count.shape()), name, spacer, tally);
            row.setAlignment(Pos.CENTER_LEFT);
            setGraphic(row);
        }

        /** The name where there is one, the size and edge count otherwise. */
        private String describe(Shape shape) {
            String name = shape.name();
            return name.isEmpty()
                    ? shape.size() + " vertices, " + shape.edges() + " edges"
                    : name;
        }

        /**
         * The shape itself, vertices evenly spaced on a circle. Small enough
         * to sit in a row, which is why the vertices are dots rather than
         * numbered: which vertex is which is exactly what a shape ignores.
         */
        private Group draw(Shape shape) {
            Group drawing = new Group();
            int size = shape.size();
            double centre = TILE / 2;
            double radius = centre - VERTEX_RADIUS - 2;

            double[][] at = new double[size][2];
            for (int vertex = 0; vertex < size; vertex++) {
                double angle = 2 * Math.PI * vertex / size - Math.PI / 2;
                at[vertex][0] = centre + radius * Math.cos(angle);
                at[vertex][1] = centre + radius * Math.sin(angle);
            }

            for (int from = 0; from < size; from++) {
                for (int to = from + 1; to < size; to++) {
                    if (!shape.joins(from, to)) {
                        continue;
                    }
                    Line edge = new Line(at[from][0], at[from][1],
                            at[to][0], at[to][1]);
                    edge.getStyleClass().add("shape-edge");
                    drawing.getChildren().add(edge);
                }
            }
            for (double[] point : at) {
                Circle dot = new Circle(point[0], point[1], VERTEX_RADIUS);
                dot.getStyleClass().add("shape-vertex");
                drawing.getChildren().add(dot);
            }
            return drawing;
        }
    }
}
