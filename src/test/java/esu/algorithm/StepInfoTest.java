package esu.algorithm;

import java.io.File;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins what the step log says.
 *
 * StepInfo used to deep-copy the whole tree on every log entry, purely so
 * caller and target could point into a snapshot. These tests fix the rendered
 * output so that copy can be removed without changing what a user reads.
 */
public class StepInfoTest {

    /** The log for the nth step of ESU over bowtie.txt at the given size. */
    private ArrayList<StepInfo> logForStep(int wanted, int subgraphSize) {
        UndirectedGraph graph =
                UndirectedGraph.fromFile(new File("samples/bowtie.txt"));
        EsuTree tree = new EsuTree(graph, subgraphSize);
        int step = 0;
        while (tree.step()) {
            if (++step == wanted) {
                return tree.getLog();
            }
            tree.clearStepLog();
        }
        throw new IllegalArgumentException("no step " + wanted);
    }

    private String rendered(int step, int subgraphSize) {
        StringBuilder out = new StringBuilder();
        for (StepInfo entry : logForStep(step, subgraphSize)) {
            out.append(entry.render()).append('\n');
        }
        return out.toString();
    }

    @Test
    public void describesBuildingANodeFromItsParent() {
        assertEquals(
                "Creating new node.\n"
                + "Create node {0, 2}, copy {0}'s possible steps () into "
                + "{0, 2}'s possible steps () and subgraph neighbors [].\n"
                + "Getting 2's neighbors: { 0, 1, 3, 4 }.\n"
                + "0 is less than or equal to this branch's first step (0). "
                + "Validation denied.\n"
                + "1 was not found in {0, 2}'s data. Validation continues.\n"
                + "1 was found in {0}'s data. Validation denied.\n"
                + "3 was not found in {0, 2}'s data. Validation continues.\n"
                + "3 was not found in {0}'s data. Validation continues.\n"
                + "Validation approved for 3.\n"
                + "3 added to {0, 2}'s lists.\n"
                + "4 was not found in {0, 2}'s data. Validation continues.\n"
                + "4 was not found in {0}'s data. Validation continues.\n"
                + "Validation approved for 4.\n"
                + "4 added to {0, 2}'s lists.\n"
                + "Finished making node {0, 2}.\n",
                rendered(4, 3));
    }

    @Test
    public void describesTheLabellingRuleRejectingALowerVertex() {
        assertEquals(
                "Creating new node.\n"
                + "Getting 1's neighbors: { 0, 2 }.\n"
                + "0 is less than or equal to this branch's first step (1). "
                + "Validation denied.\n"
                + "2 was not found in {1}'s data. Validation continues.\n"
                + "Validation approved for 2.\n"
                + "2 added to {1}'s lists.\n"
                + "Finished making node {1}.\n",
                rendered(7, 3));
    }

    @Test
    public void namesTheNodeEachEntryIsAbout() {
        ArrayList<StepInfo> log = logForStep(7, 3);

        // The first entry is logged before the node is given its vertex, so
        // it has no subgraph yet. Everything after it is about that node,
        // which is what lets clicking a log line highlight the right box.
        assertEquals("{}", log.get(0).getCallerSubgraph());
        for (StepInfo entry : log.subList(1, log.size())) {
            assertEquals("{1}", entry.getCallerSubgraph());
        }
    }
}
