package esu.algorithm.ui;

import esu.algorithm.EsuTree;
import esu.algorithm.RandomGraph;
import esu.algorithm.UndirectedGraph;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * The old layout spread each level evenly across the full width of the tree,
 * so a node sat nowhere near its parent and the connecting lines sprawled
 * across everything. These pin the properties that make a tree readable.
 */
public class TreeLayoutTest {

    private EsuTree finished(UndirectedGraph graph, int subgraphSize) {
        EsuTree tree = new EsuTree(graph, subgraphSize);
        while (tree.step()) {
            tree.clearStepLog();
        }
        return tree;
    }

    private TreeLayout layoutOf(String file, int subgraphSize) {
        return TreeLayout.of(finished(
                UndirectedGraph.fromFile(new File("samples/" + file)),
                subgraphSize));
    }

    @Test
    public void givesEveryNodeABox() {
        TreeLayout layout = layoutOf("bowtie.txt", 3);

        assertFalse(layout.boxes().isEmpty());
        for (TreeLayout.Box box : layout.boxes()) {
            assertNotNull(layout.get(box.getId()));
        }
    }

    @Test
    public void centresEveryParentOverItsChildren() {
        TreeLayout layout = layoutOf("cluster.txt", 4);

        for (TreeLayout.Box box : layout.boxes()) {
            List<TreeLayout.Box> children = layout.childrenOf(box);
            if (children.isEmpty()) {
                continue;
            }
            double first = children.get(0).centreX();
            double last = children.get(children.size() - 1).centreX();
            assertEquals((first + last) / 2, box.centreX(), 0.001,
                    box.getId() + " is not centred over its children");
        }
    }

    @Test
    public void neverOverlapsTwoBoxesOnTheSameRow() {
        TreeLayout layout = layoutOf("sample-small.txt", 4);

        for (int level = 0; level <= layout.getDepth(); level++) {
            List<TreeLayout.Box> row = new ArrayList<>(layout.row(level));
            row.sort((a, b) -> Double.compare(a.getX(), b.getX()));
            for (int i = 1; i < row.size(); i++) {
                assertTrue(row.get(i - 1).getX() + row.get(i - 1).getWidth()
                                <= row.get(i).getX(),
                        "boxes overlap on level " + level + ": "
                                + row.get(i - 1).getId() + " and "
                                + row.get(i).getId());
            }
        }
    }

    @Test
    public void putsDeeperNodesFurtherDown() {
        TreeLayout layout = layoutOf("cluster.txt", 4);

        for (TreeLayout.Box box : layout.boxes()) {
            for (TreeLayout.Box child : layout.childrenOf(box)) {
                assertTrue(child.getY() > box.getY(),
                        child.getId() + " is not below " + box.getId());
            }
        }
    }

    @Test
    public void survivesAGraphWithNoEdgesAtAll() {
        TreeLayout layout = TreeLayout.of(
                finished(new UndirectedGraph(3), 3));

        assertEquals(1, layout.boxes().size());
        assertEquals(0, layout.getDepth());
    }

    @Test
    public void keepsBoxesInsideTheReportedExtent() {
        TreeLayout layout = TreeLayout.of(finished(
                RandomGraph.generate(11, new Random(5)), 4));

        for (TreeLayout.Box box : layout.boxes()) {
            assertTrue(box.getX() >= 0, "box starts left of the tree");
            assertTrue(box.getX() + box.getWidth() <= layout.getWidth() + 0.001,
                    box.getId() + " extends past the reported width");
            assertTrue(box.getY() + box.getHeight() <= layout.getHeight() + 0.001,
                    box.getId() + " extends past the reported height");
        }
    }
}
