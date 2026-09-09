/*
 * The shape of a subgraph, independent of which vertices happen to form it.
 */
package esu.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class Shape
 *
 * Two subgraphs have the same Shape when one can be turned into the other by
 * renaming its vertices. {0, 1, 2} and {2, 3, 4} in a bowtie are both
 * triangles, and a tool that only reports vertex sets never says so.
 *
 * Enumerating connected subgraphs is the first step of motif discovery. This
 * is the second: without it there is nothing to compare against a random
 * network, because there are no shapes to count.
 *
 * The canonical form is found by brute force -- every relabelling is tried and
 * the smallest result kept. Industrial tools use nauty, which matters at the
 * sizes they work at; here a subgraph has at most a few vertices, so a few
 * hundred relabellings cost microseconds and need no library. It is also the
 * one description of the method that fits in a sentence.
 */
public final class Shape {

    /**
     * Beyond this the number of relabellings, which is size factorial, stops
     * being trivial. The visualizer does not offer sizes near it.
     */
    public static final int MAX_SIZE = 8;

    private final int size;
    /** Smallest adjacency bitstring over every relabelling. */
    private final long canonical;
    private final int edges;

    private Shape(int size, long canonical, int edges) {
        this.size = size;
        this.canonical = canonical;
        this.edges = edges;
    }

    /**
     * The shape of the subgraph a set of vertices induces.
     *
     * @param graph    the graph they belong to
     * @param vertices the subgraph's vertices, in any order
     * @return its shape
     */
    public static Shape of(UndirectedGraph graph, List<Integer> vertices) {
        int size = vertices.size();
        if (size > MAX_SIZE) {
            throw new IllegalArgumentException(
                    "subgraphs of more than " + MAX_SIZE + " vertices are not "
                    + "classified: there are too many relabellings to try");
        }

        boolean[][] adjacency = new boolean[size][size];
        int edges = 0;
        for (int row = 0; row < size; row++) {
            for (int column = row + 1; column < size; column++) {
                if (graph.getNeighbors(vertices.get(row))
                        .contains(vertices.get(column))) {
                    adjacency[row][column] = true;
                    adjacency[column][row] = true;
                    edges++;
                }
            }
        }

        int[] order = new int[size];
        for (int at = 0; at < size; at++) {
            order[at] = at;
        }
        long smallest = Long.MAX_VALUE;
        for (int[] relabelling : permutations(order)) {
            smallest = Math.min(smallest, bits(adjacency, relabelling));
        }
        return new Shape(size, smallest, edges);
    }

    /**
     * Group subgraphs by shape, most frequent first.
     *
     * @param graph     the graph they came from
     * @param subgraphs the subgraphs found
     * @return each distinct shape with how often it occurred
     */
    public static List<Count> classify(UndirectedGraph graph,
            List<List<Integer>> subgraphs) {

        Map<Shape, Integer> tally = new LinkedHashMap<>();
        for (List<Integer> subgraph : subgraphs) {
            tally.merge(of(graph, subgraph), 1, Integer::sum);
        }

        List<Count> counts = new ArrayList<>();
        for (Map.Entry<Shape, Integer> entry : tally.entrySet()) {
            counts.add(new Count(entry.getKey(), entry.getValue()));
        }
        counts.sort(Comparator.comparingInt(Count::count).reversed());
        return counts;
    }

    /**
     * The adjacency of a subgraph as a bitstring, reading the upper triangle
     * row by row under one relabelling of its vertices.
     */
    private static long bits(boolean[][] adjacency, int[] relabelling) {
        long out = 0;
        for (int row = 0; row < relabelling.length; row++) {
            for (int column = row + 1; column < relabelling.length; column++) {
                out <<= 1;
                if (adjacency[relabelling[row]][relabelling[column]]) {
                    out |= 1;
                }
            }
        }
        return out;
    }

    /** Every ordering of the given values, by Heap's algorithm. */
    private static List<int[]> permutations(int[] values) {
        List<int[]> out = new ArrayList<>();
        permute(values.clone(), values.length, out);
        return out;
    }

    private static void permute(int[] values, int upTo, List<int[]> out) {
        if (upTo == 1) {
            out.add(values.clone());
            return;
        }
        for (int at = 0; at < upTo; at++) {
            permute(values, upTo - 1, out);
            int swapWith = (upTo % 2 == 0) ? at : 0;
            int held = values[swapWith];
            values[swapWith] = values[upTo - 1];
            values[upTo - 1] = held;
        }
    }

    /**
     * What people call this shape, where they call it anything.
     *
     * Only the small ones have names in common use. Above four vertices the
     * drawing says more than a label would.
     *
     * @return the name, or an empty string if it has none
     */
    public String name() {
        if (size == 2) {
            return "edge";
        }
        if (size == 3) {
            return edges == 3 ? "triangle" : "path";
        }
        if (size == 4) {
            switch (edges) {
                case 3: return degrees()[3] == 3 ? "star" : "path";
                case 4: return degrees()[3] == 3 ? "triangle and tail" : "cycle";
                case 5: return "diamond";
                case 6: return "clique";
                default: return "";
            }
        }
        return "";
    }

    /** Vertex degrees, ascending, recovered from the canonical form. */
    private int[] degrees() {
        int[] degrees = new int[size];
        long remaining = canonical;
        for (int row = size - 1; row >= 0; row--) {
            for (int column = size - 1; column > row; column--) {
                if ((remaining & 1) == 1) {
                    degrees[row]++;
                    degrees[column]++;
                }
                remaining >>= 1;
            }
        }
        Arrays.sort(degrees);
        return degrees;
    }

    /** @return how many vertices the shape has */
    public int size() {
        return size;
    }

    /** @return how many edges join them */
    public int edges() {
        return edges;
    }

    /**
     * Whether two of the shape's vertices are joined, in its canonical form.
     * Enough to draw it.
     *
     * @param from one vertex, 0 to size - 1
     * @param to   another
     * @return true if the canonical form joins them
     */
    public boolean joins(int from, int to) {
        if (from == to) {
            return false;
        }
        int low = Math.min(from, to);
        int high = Math.max(from, to);

        // bits() shifts left as it goes, so the first pair it writes ends up
        // in the highest bit: a pair's position is counted from the end.
        int pairs = size * (size - 1) / 2;
        int index = 0;
        for (int row = 0; row < size; row++) {
            for (int column = row + 1; column < size; column++) {
                if (row == low && column == high) {
                    return ((canonical >> (pairs - 1 - index)) & 1) == 1;
                }
                index++;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Shape)) {
            return false;
        }
        Shape that = (Shape) other;
        return size == that.size && canonical == that.canonical;
    }

    @Override
    public int hashCode() {
        return 31 * size + Long.hashCode(canonical);
    }

    @Override
    public String toString() {
        String name = name();
        return name.isEmpty()
                ? size + " vertices, " + edges + " edges" : name;
    }

    /**
     * A shape and how often it was found.
     *
     * @param shape the shape
     * @param count how many subgraphs had it
     */
    public record Count(Shape shape, int count) {
    }
}
