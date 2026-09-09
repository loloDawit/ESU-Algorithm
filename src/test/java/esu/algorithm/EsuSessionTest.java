package esu.algorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A session reaches a step by replaying the algorithm rather than by keeping
 * a copy of the tree after every step. These tests exist because that is the
 * one thing about the approach that can silently go wrong: a replayed step
 * must be indistinguishable from a stepped-to one.
 */
public class EsuSessionTest {

    /**
     * Every node of the tree, level by level, as text. Starts at level 1:
     * the root carries no lists of its own and never changes.
     */
    private String describe(EsuTree tree) {
        StringBuilder out = new StringBuilder();
        ArrayList<EsuNode>[] levels = tree.getNodesByLevel();
        for (int depth = 1; depth < levels.length; depth++) {
            for (EsuNode node : levels[depth]) {
                out.append(node.getSubgraphAsString())
                   .append(node.getPossibleStepsAsString())
                   .append(node.getSubgraphNeighborsAsString())
                   .append(' ');
            }
            out.append('|');
        }
        return out.toString();
    }

    /** The tree state after stepping forward n times, the slow honest way. */
    private String byStepping(UndirectedGraph graph, int subgraphSize, int n) {
        EsuTree tree = new EsuTree(graph, subgraphSize);
        for (int i = 0; i < n; i++) {
            tree.step();
            tree.clearStepLog();
        }
        return describe(tree);
    }

    @ParameterizedTest(name = "{0} at k={1}")
    @CsvSource({
        "bowtie.txt, 3", "bowtie.txt, 4",
        "cluster.txt, 3", "cluster.txt, 4",
        "sample-small.txt, 3", "sample-small.txt, 5"
    })
    public void jumpingToAStepMatchesSteppingToIt(String file, int subgraphSize) {
        UndirectedGraph graph =
                UndirectedGraph.fromFile(new File("samples/" + file));
        EsuSession session = new EsuSession(graph, subgraphSize);

        for (int step = 0; step <= session.getTotalSteps(); step++) {
            session.goToStep(step);
            assertEquals(byStepping(graph, subgraphSize, step),
                    describe(session.getCurrentTree()),
                    "state differs at step " + step);
        }
    }

    @Test
    public void steppingBackwardsAgreesWithSteppingForwards() {
        UndirectedGraph graph = RandomGraph.generate(9, new Random(3));
        EsuSession forward = new EsuSession(graph, 4);
        EsuSession backward = new EsuSession(graph, 4);

        backward.goToStep(backward.getTotalSteps());
        for (int i = 0; i < 5; i++) {
            backward.stepBack();
        }
        forward.goToStep(forward.getTotalSteps() - 5);

        assertEquals(describe(forward.getCurrentTree()),
                describe(backward.getCurrentTree()));
    }

    @Test
    public void reportsTheSameSubgraphCountTheAlgorithmFinds() {
        UndirectedGraph graph =
                UndirectedGraph.fromFile(new File("samples/cluster.txt"));
        EsuTree reference = new EsuTree(graph, 4);
        while (reference.step()) {
            reference.clearStepLog();
        }

        EsuSession session = new EsuSession(graph, 4);

        assertEquals(reference.getSubGraphs().size(), session.getSubgraphCount());
    }

    @Test
    public void namesTheSubgraphBeingBuiltAtThisStep() {
        EsuSession session = new EsuSession(
                UndirectedGraph.fromFile(new File("samples/bowtie.txt")), 3);

        session.goToStep(7);

        // Step 7 builds node {1}; the graph panel highlights those vertices.
        assertEquals(java.util.List.of(1), session.getActiveSubgraph());
    }

    @Test
    public void offersTheVerticesTheCurrentNodeCouldAddNext() {
        EsuSession session = new EsuSession(
                UndirectedGraph.fromFile(new File("samples/bowtie.txt")), 3);

        session.goToStep(7);

        // {1} finished with 2 approved and 0 rejected by the labelling rule.
        assertEquals(java.util.List.of(2), session.getActiveExtension());
    }

    @Test
    public void hasNothingHighlightedBeforeTheSearchStarts() {
        EsuSession session = new EsuSession(
                UndirectedGraph.fromFile(new File("samples/bowtie.txt")), 3);

        assertTrue(session.getActiveSubgraph().isEmpty());
        assertTrue(session.getActiveExtension().isEmpty());
    }

    @Test
    public void summarisesEveryStepOfTheSearch() {
        EsuSession session = new EsuSession(
                UndirectedGraph.fromFile(new File("samples/bowtie.txt")), 3);

        List<EsuSession.Step> history = session.getHistory();

        assertEquals(session.getTotalSteps(), history.size());
        assertEquals(1, history.get(0).number());
        assertEquals(session.getTotalSteps(),
                history.get(history.size() - 1).number());
    }

    @Test
    public void saysWhichSubgraphEachStepBuilt() {
        EsuSession session = new EsuSession(
                UndirectedGraph.fromFile(new File("samples/bowtie.txt")), 3);

        // Every entry names the node that step finished, which is what makes
        // a history row worth clicking: going to that step must agree.
        for (EsuSession.Step entry : session.getHistory()) {
            session.goToStep(entry.number());
            ArrayList<StepInfo> log = session.getCurrentLog();
            assertEquals(entry.subgraph(),
                    log.get(log.size() - 1).getCallerSubgraph(),
                    "step " + entry.number() + " disagrees with its summary");
        }
    }

    @Test
    public void hasNoHistoryForASearchWithNothingToDo() {
        EsuSession session = new EsuSession(new UndirectedGraph(3), 3);

        assertTrue(session.getHistory().isEmpty());
    }

    @Test
    public void willNotStepPastTheEnd() {
        EsuSession session = new EsuSession(
                UndirectedGraph.fromFile(new File("samples/bowtie.txt")), 3);

        session.goToStep(session.getTotalSteps());

        assertFalse(session.stepForward());
        assertEquals(session.getTotalSteps(), session.getCurrentStep());
    }

    @Test
    public void willNotStepBeforeTheStart() {
        EsuSession session = new EsuSession(
                UndirectedGraph.fromFile(new File("samples/bowtie.txt")), 3);

        assertFalse(session.stepBack());
        assertEquals(0, session.getCurrentStep());
    }
}
