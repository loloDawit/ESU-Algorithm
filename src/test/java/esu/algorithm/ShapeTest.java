package esu.algorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Grouping the subgraphs found by their shape.
 *
 * Enumerating connected subgraphs is the first step of motif discovery; this
 * is the second. Until subgraphs are grouped by shape, {0, 1, 2} and {1, 2, 3}
 * are just two different answers, and there is nothing to be said about which
 * shapes occur more often than chance would explain.
 */
public class ShapeTest {

    private UndirectedGraph sample(String name) {
        return UndirectedGraph.fromFile(new File("samples/" + name));
    }

    /** A graph where every vertex is joined to every other. */
    private UndirectedGraph complete(int size) {
        UndirectedGraph graph = new UndirectedGraph(size);
        for (int from = 0; from < size; from++) {
            for (int to = from + 1; to < size; to++) {
                graph.insertNode(from, to);
            }
        }
        return graph;
    }

    /** A straight line of vertices, 0-1-2-...-n. */
    private UndirectedGraph path(int size) {
        UndirectedGraph graph = new UndirectedGraph(size);
        for (int at = 0; at + 1 < size; at++) {
            graph.insertNode(at, at + 1);
        }
        return graph;
    }

    @Test
    public void callsTheSameShapeTheSame() {
        UndirectedGraph triangles = sample("bowtie.txt");

        // bowtie.txt is two triangles sharing vertex 2, so both are one shape.
        assertEquals(Shape.of(triangles, List.of(0, 1, 2)),
                Shape.of(triangles, List.of(2, 3, 4)));
    }

    @Test
    public void callsDifferentShapesDifferent() {
        UndirectedGraph graph = sample("bowtie.txt");

        // {0, 1, 2} is a triangle; {0, 1, 3} would be, but 3 joins only 2.
        assertNotEquals(Shape.of(graph, List.of(0, 1, 2)),
                Shape.of(graph, List.of(1, 2, 3)));
    }

    @Test
    public void doesNotCareWhichOrderTheVerticesArrive() {
        UndirectedGraph graph = sample("cluster.txt");

        // Deliberately lopsided: {0, 1, 3, 4} is a triangle with a tail, so
        // relabellings genuinely differ. A symmetric subgraph such as a
        // complete one would pass this test against any implementation,
        // canonical or not, and an earlier version of it did exactly that.
        List<Integer> vertices = new ArrayList<>(List.of(0, 1, 3, 4));
        Shape expected = Shape.of(graph, vertices);

        // The same subgraph, relabelled every way round, is the same shape.
        Random random = new Random(4);
        for (int attempt = 0; attempt < 50; attempt++) {
            Collections.shuffle(vertices, random);
            assertEquals(expected, Shape.of(graph, vertices),
                    "order " + vertices + " gave a different shape");
        }
    }

    @Test
    public void findsOneShapeInACompleteGraph() {
        UndirectedGraph graph = complete(6);

        // Every subgraph of a complete graph is itself complete.
        assertEquals(1, distinctShapes(graph, 4).size());
    }

    @Test
    public void findsOneShapeAlongAPath() {
        UndirectedGraph graph = path(7);

        // Every connected run of 3 vertices in a line is the same line.
        assertEquals(1, distinctShapes(graph, 3).size());
    }

    @Test
    public void separatesTheTriangleFromThePath() {
        // A triangle with a tail: 0-1-2-0 and 2-3.
        UndirectedGraph graph = new UndirectedGraph(4);
        graph.insertNode(0, 1);
        graph.insertNode(1, 2);
        graph.insertNode(2, 0);
        graph.insertNode(2, 3);

        assertEquals(2, distinctShapes(graph, 3).size());
    }

    @Test
    public void namesTheShapesPeopleHaveNamesFor() {
        UndirectedGraph graph = sample("bowtie.txt");

        assertEquals("triangle", Shape.of(graph, List.of(0, 1, 2)).name());
        assertEquals("path", Shape.of(graph, List.of(1, 2, 3)).name());
    }

    @Test
    public void countsHowOftenEachShapeOccurs() {
        UndirectedGraph graph = sample("bowtie.txt");
        EsuSession session = new EsuSession(graph, 3);

        List<Shape.Count> counts = Shape.classify(graph, session.getSubgraphs());

        // Six subgraphs of size 3, and every one of them is accounted for.
        assertEquals(6, counts.stream().mapToInt(Shape.Count::count).sum());
        // Most frequent first, so the eye lands on what matters.
        for (int i = 1; i < counts.size(); i++) {
            assertTrue(counts.get(i - 1).count() >= counts.get(i).count(),
                    "counts are not in descending order");
        }
    }

    @Test
    public void reportsTheEdgesOfTheShapeItself() {
        UndirectedGraph graph = sample("bowtie.txt");

        Shape triangle = Shape.of(graph, List.of(0, 1, 2));

        // A triangle joins all three of its pairs.
        assertTrue(triangle.joins(0, 1));
        assertTrue(triangle.joins(1, 2));
        assertTrue(triangle.joins(0, 2));
        assertFalse(triangle.joins(0, 0), "nothing joins itself");
    }

    @Test
    public void reportsEdgesConsistentlyWithItsEdgeCount() {
        UndirectedGraph graph = sample("cluster.txt");

        // Whatever the shape, walking its pairs must find exactly as many
        // edges as it says it has. This is what the drawing depends on, and
        // it was wrong in a way no other test could see: every pair read the
        // same bit, so shapes with different edges drew identically.
        for (int size = 3; size <= 4; size++) {
            for (Shape shape : distinctShapes(graph, size)) {
                int joined = 0;
                for (int from = 0; from < shape.size(); from++) {
                    for (int to = from + 1; to < shape.size(); to++) {
                        if (shape.joins(from, to)) {
                            joined++;
                        }
                    }
                }
                assertEquals(shape.edges(), joined,
                        shape + " reports " + shape.edges() + " edges but "
                        + "joins " + joined + " pairs");
            }
        }
    }

    @Test
    public void drawsDifferentShapesDifferently() {
        UndirectedGraph graph = sample("cluster.txt");
        Set<String> drawings = new LinkedHashSet<>();

        for (Shape shape : distinctShapes(graph, 4)) {
            StringBuilder drawing = new StringBuilder();
            for (int from = 0; from < shape.size(); from++) {
                for (int to = from + 1; to < shape.size(); to++) {
                    drawing.append(shape.joins(from, to) ? '1' : '0');
                }
            }
            drawings.add(drawing.toString());
        }

        // Three shapes must produce three pictures, or the panel lies.
        assertEquals(distinctShapes(graph, 4).size(), drawings.size());
    }

    /** The distinct shapes among every size-k subgraph of a graph. */
    private Set<Shape> distinctShapes(UndirectedGraph graph, int size) {
        EsuSession session = new EsuSession(graph, size);
        Set<Shape> shapes = new LinkedHashSet<>();
        for (List<Integer> subgraph : session.getSubgraphs()) {
            shapes.add(Shape.of(graph, subgraph));
        }
        return shapes;
    }
}
