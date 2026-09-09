package esu.algorithm;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The random generator feeds straight into ESU, so it has to produce graphs
 * ESU can say something about: 0-based integer ids, no self loops, and
 * connected, since a disconnected pile of vertices makes a dull tree.
 */
public class RandomGraphTest {

    @Test
    public void usesEveryVertexIdFromZeroUp() {
        UndirectedGraph g = RandomGraph.generate(8, new Random(1));

        assertEquals(8, g.getSize());
        for (int v = 0; v < 8; v++) {
            assertFalse(g.getNeighbors(v).isEmpty(), "vertex " + v + " is isolated");
        }
    }

    @Test
    public void generatesConnectedGraphs() {
        for (int seed = 0; seed < 25; seed++) {
            UndirectedGraph g = RandomGraph.generate(9, new Random(seed));
            assertTrue(isConnected(g), "seed " + seed + " produced a split graph");
        }
    }

    @Test
    public void neverLinksAVertexToItself() {
        for (int seed = 0; seed < 25; seed++) {
            UndirectedGraph g = RandomGraph.generate(7, new Random(seed));
            for (int v = 0; v < g.getSize(); v++) {
                assertFalse(g.getNeighbors(v).contains(v), "self loop at " + v);
            }
        }
    }

    @Test
    public void writesAFileThatReadsBackIdentically() throws Exception {
        UndirectedGraph original = RandomGraph.generate(7, new Random(42));

        File file = RandomGraph.writeToFile(original,
                Files.createTempFile("esu-test", ".txt").toFile());
        UndirectedGraph reloaded = UndirectedGraph.fromFile(file);

        assertEquals(original.getSize(), reloaded.getSize());
        for (int v = 0; v < original.getSize(); v++) {
            assertEquals(original.getNeighbors(v), reloaded.getNeighbors(v));
        }
    }

    private boolean isConnected(UndirectedGraph g) {
        Deque<Integer> queue = new ArrayDeque<>();
        Set<Integer> seen = new HashSet<>();
        queue.push(0);
        seen.add(0);
        while (!queue.isEmpty()) {
            for (Object n : g.getNeighbors(queue.pop())) {
                if (seen.add((Integer) n)) {
                    queue.push((Integer) n);
                }
            }
        }
        return seen.size() == g.getSize();
    }
}
