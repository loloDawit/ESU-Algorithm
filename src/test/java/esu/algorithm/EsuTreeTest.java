package esu.algorithm;

import java.io.File;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Correctness tests for the ESU algorithm.
 *
 * ESU claims to enumerate every connected subgraph of size k exactly once.
 * That is checkable: brute force every k-subset of vertices, keep the
 * connected ones, and compare the set against what ESU produced. If ESU
 * misses one, invents one, or emits a duplicate, these fail.
 */
public class EsuTreeTest {

    /** Runs ESU to completion and returns its subgraphs as vertex sets. */
    private List<Set<Integer>> esuSubgraphs(UndirectedGraph graph, int k) {
        EsuTree tree = new EsuTree(graph, k);
        while (tree.step()) {
            tree.clearStepLog();
        }
        List<Set<Integer>> found = new ArrayList<>();
        for (LinkedList<Integer> subgraph : tree.getSubGraphs()) {
            found.add(new HashSet<>(subgraph));
        }
        return found;
    }

    /** Every connected k-subset, found by brute force. The oracle. */
    private Set<Set<Integer>> bruteForce(UndirectedGraph graph, int k) {
        Set<Set<Integer>> result = new HashSet<>();
        int n = graph.getSize();
        for (long mask = 0; mask < (1L << n); mask++) {
            if (Long.bitCount(mask) != k) {
                continue;
            }
            Set<Integer> candidate = new HashSet<>();
            for (int v = 0; v < n; v++) {
                if ((mask & (1L << v)) != 0) {
                    candidate.add(v);
                }
            }
            if (isConnected(graph, candidate)) {
                result.add(candidate);
            }
        }
        return result;
    }

    /** Flood fill inside the candidate set. */
    private boolean isConnected(UndirectedGraph graph, Set<Integer> vertices) {
        Deque<Integer> queue = new ArrayDeque<>();
        Set<Integer> seen = new HashSet<>();
        int start = vertices.iterator().next();
        queue.push(start);
        seen.add(start);
        while (!queue.isEmpty()) {
            for (Object neighbor : graph.getNeighbors(queue.pop())) {
                if (vertices.contains(neighbor) && seen.add((Integer) neighbor)) {
                    queue.push((Integer) neighbor);
                }
            }
        }
        return seen.size() == vertices.size();
    }

    @ParameterizedTest(name = "{0} at k={1}")
    @CsvSource({
        "bowtie.txt, 3", "bowtie.txt, 4", "bowtie.txt, 5",
        "cluster.txt, 3", "cluster.txt, 4", "cluster.txt, 5",
        "sample-small.txt, 3", "sample-small.txt, 4", "sample-small.txt, 5"
    })
    public void findsExactlyTheConnectedSubgraphsOfSizeK(String file, int k) {
        UndirectedGraph graph = UndirectedGraph.fromFile(new File("samples/" + file));

        List<Set<Integer>> found = esuSubgraphs(graph, k);

        assertEquals(bruteForce(graph, k), new HashSet<>(found));
    }

    @ParameterizedTest(name = "{0} at k={1}")
    @CsvSource({
        "bowtie.txt, 3", "cluster.txt, 3", "cluster.txt, 4", "sample-small.txt, 4"
    })
    public void findsEachSubgraphExactlyOnce(String file, int k) {
        UndirectedGraph graph = UndirectedGraph.fromFile(new File("samples/" + file));

        List<Set<Integer>> found = esuSubgraphs(graph, k);

        assertEquals(found.size(), new HashSet<>(found).size(), "duplicate subgraphs: " + found);
    }

    @Test
    public void findsNothingWhenNoSubgraphIsBigEnough() {
        UndirectedGraph graph = UndirectedGraph.fromFile(new File("samples/bowtie.txt"));

        // bowtie.txt has 5 vertices, so a 6-vertex subgraph cannot exist.
        assertTrue(esuSubgraphs(graph, 6).isEmpty());
    }
}
