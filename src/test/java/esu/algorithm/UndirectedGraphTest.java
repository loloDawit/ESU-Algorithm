package esu.algorithm;

import java.io.File;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for reading a graph from file.
 *
 * The bug these exist for: the matrix used to be sized to the largest vertex
 * id, so that vertex was out of bounds and every edge touching it was dropped
 * without a word. bowtie.txt lost vertex 4 and with it a whole triangle.
 */
public class UndirectedGraphTest {

    private File sample(String name) {
        return new File("samples/" + name);
    }

    @Test
    public void keepsTheHighestNumberedVertex() {
        UndirectedGraph g = UndirectedGraph.fromFile(sample("bowtie.txt"));

        // bowtie.txt: 0-1, 0-2, 1-2, 2-3, 2-4, 3-4. Vertex 4 is the highest.
        ArrayList<Integer> neighborsOfFour = g.getNeighbors(4);

        assertNotNull(neighborsOfFour, "vertex 4 is outside the matrix");
        assertTrue(neighborsOfFour.contains(2), "edge 2-4 was dropped");
        assertTrue(neighborsOfFour.contains(3), "edge 3-4 was dropped");
    }

    @Test
    public void sizesTheMatrixToHoldEveryVertex() {
        assertEquals(5, UndirectedGraph.fromFile(sample("bowtie.txt")).getSize());
    }

    @Test
    public void readsEveryEdgeAsUndirected() {
        UndirectedGraph g = UndirectedGraph.fromFile(sample("bowtie.txt"));

        assertTrue(g.getNeighbors(2).contains(4));
        assertTrue(g.getNeighbors(4).contains(2));
    }
}
