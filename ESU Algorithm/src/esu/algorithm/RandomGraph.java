/*
 * Random graph generation, for trying the algorithm without hunting for a
 * file. Deliberately produces the only shape ESU has an opinion about:
 * unweighted, undirected, 0-based integer ids, and connected.
 */
package esu.algorithm;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Class RandomGraph
 *
 * The 2018 generator drew lettered vertices and random edge weights, neither
 * of which ESU can use, and there was no path from what it drew into the
 * algorithm. This one produces a graph the visualizer can actually run.
 */
public class RandomGraph {

    /** Vertex counts that lay out legibly and finish quickly. */
    public static final int MIN_VERTICES = 6;
    public static final int MAX_VERTICES = 10;

    private RandomGraph() {
    }

    /**
     * generate
     *
     * Builds a connected graph by first laying down a spanning tree over a
     * shuffled vertex order, which guarantees connectivity, then adding a few
     * extra edges so there is something for the algorithm to branch on.
     *
     * @param size   number of vertices
     * @param random source of randomness
     * @return a connected graph on vertices 0..size-1
     */
    public static UndirectedGraph generate(int size, Random random) {
        UndirectedGraph graph = new UndirectedGraph(size);

        ArrayList<Integer> order = new ArrayList<>();
        for (int vertex = 0; vertex < size; vertex++) {
            order.add(vertex);
        }
        Collections.shuffle(order, random);

        // spanning tree: every vertex after the first attaches to one before it
        for (int i = 1; i < size; i++) {
            graph.insertNode(order.get(i), order.get(random.nextInt(i)));
        }

        // a handful of extra edges, so the tree has branches worth watching
        int extras = size / 2 + random.nextInt(size);
        for (int i = 0; i < extras; i++) {
            int from = random.nextInt(size);
            int to = random.nextInt(size);
            if (from != to) {
                graph.insertNode(from, to);
            }
        }
        return graph;
    }

    /**
     * generate
     *
     * @param random source of randomness
     * @return a connected graph of a random legible size
     */
    public static UndirectedGraph generate(Random random) {
        return generate(MIN_VERTICES
                + random.nextInt(MAX_VERTICES - MIN_VERTICES + 1), random);
    }

    /**
     * writeToFile
     *
     * Writes the graph in the same edge-per-line format fillGraph reads, so a
     * generated graph is an ordinary graph file that can be reopened or kept.
     *
     * @param graph graph to write
     * @param file  destination
     * @return the file written to
     * @throws IOException if the file cannot be written
     */
    public static File writeToFile(UndirectedGraph graph, File file)
            throws IOException {
        try (PrintWriter out = new PrintWriter(file)) {
            for (int from = 0; from < graph.getSize(); from++) {
                for (Object to : graph.getNeighbors(from)) {
                    if ((Integer) to > from) {          // each edge once
                        out.println(from + " " + to);
                    }
                }
            }
        }
        return file;
    }
}
