/*
 * One run of the ESU algorithm over one graph, positioned at a step.
 */
package esu.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class EsuSession
 *
 * Holds a graph, a subgraph size, and where you currently are in the search.
 *
 * The visualizer used to keep a full copy of the tree after every step so it
 * could go backwards, which cost steps x tree in memory and put a hard ceiling
 * on the subgraph size. This reaches a step by replaying the algorithm
 * instead: enumerating from scratch takes single-digit to a few hundred
 * milliseconds even on graphs far larger than this app draws legibly, so
 * re-running beats remembering. Two trees are held rather than hundreds.
 *
 * Deliberately free of any JavaFX reference, so the algorithm and this
 * position-in-the-search idea stay usable from anything.
 */
public class EsuSession {

    /**
     * One line of the search's history: which step it was, and what it built.
     *
     * @param number   the step, counting from 1
     * @param subgraph the node that step finished, e.g. "{0, 2}"
     */
    public record Step(int number, String subgraph) {

        @Override
        public String toString() {
            return number + "  \u00b7  " + subgraph;
        }
    }

    private final UndirectedGraph graph;
    private final int subgraphSize;

    /** The finished tree: layout, dead ends and totals all come from here. */
    private final EsuTree finalTree;
    private final int totalSteps;
    private final int subgraphCount;

    /**
     * A line per step, kept because it is only a string each: the search is
     * already run to completion on construction, so the whole history costs
     * nothing next to the tree copies this design exists to avoid.
     */
    private final List<Step> history = new ArrayList<>();

    /** Worked out on first asking; the search does not change. */
    private List<Shape.Count> shapes;

    /** The tree as it stood at currentStep, rebuilt by replay. */
    private EsuTree currentTree;
    private int currentStep;

    /**
     * Runs the search to completion to learn its shape, then rewinds to the
     * start. This is the expensive call; everything after it is cheap.
     *
     * @param graph        the graph to search
     * @param subgraphSize the k in "connected subgraphs of size k"
     */
    public EsuSession(UndirectedGraph graph, int subgraphSize) {
        this.graph = graph;
        this.subgraphSize = subgraphSize;

        EsuTree tree = new EsuTree(graph, subgraphSize);
        int steps = 0;
        while (tree.step()) {
            ArrayList<StepInfo> log = tree.getLog();
            history.add(new Step(++steps, log.isEmpty()
                    ? "" : log.get(log.size() - 1).getCallerSubgraph()));
            tree.clearStepLog();
        }
        this.finalTree = tree;
        this.totalSteps = steps;
        this.subgraphCount = tree.getSubGraphs().size();

        goToStep(0);
    }

    /**
     * Move to a given step, replaying from the start to get there.
     *
     * @param step step to show, clamped to 0..totalSteps
     */
    public final void goToStep(int step) {
        int target = Math.max(0, Math.min(totalSteps, step));

        EsuTree tree = new EsuTree(graph, subgraphSize);
        for (int taken = 0; taken < target; taken++) {
            tree.clearStepLog();     // keep only the last step's log
            tree.step();
        }
        this.currentTree = tree;
        this.currentStep = target;
    }

    /**
     * Advance one step. Steps forward on the live tree rather than replaying.
     *
     * @return true if a step was taken, false at the end of the search
     */
    public boolean stepForward() {
        if (currentStep >= totalSteps) {
            return false;
        }
        currentTree.clearStepLog();
        currentTree.step();
        currentStep++;
        return true;
    }

    /**
     * Go back one step, by replaying to the step before this one.
     *
     * @return true if a step was taken, false at the start of the search
     */
    public boolean stepBack() {
        if (currentStep <= 0) {
            return false;
        }
        goToStep(currentStep - 1);
        return true;
    }

    /**
     * The vertices of the subgraph this step is building, for highlighting in
     * a drawing of the graph.
     *
     * @return the active node's vertices, empty before the search starts
     */
    public List<Integer> getActiveSubgraph() {
        EsuNode active = findActiveNode();
        if (active == null) {
            return Collections.emptyList();
        }
        LinkedList<Integer> vertices = new LinkedList<>();
        active.getSubGraph(vertices);
        return new ArrayList<>(vertices);
    }

    /**
     * The vertices the active node could still add, which is what the
     * algorithm is choosing between at this step.
     *
     * @return the active node's remaining candidates, empty if there is none
     */
    public List<Integer> getActiveExtension() {
        EsuNode active = findActiveNode();
        if (active == null) {
            return Collections.emptyList();
        }
        List<Integer> extension = new ArrayList<>(active.getPossibleSteps());
        Collections.sort(extension);
        return extension;
    }

    /**
     * The node the last log entry is about, located in the current tree by
     * its subgraph. Log entries carry the subgraph rather than the node so
     * that they cost nothing to keep.
     *
     * @return the node being built, or null if the step names none
     */
    private EsuNode findActiveNode() {
        ArrayList<StepInfo> log = currentTree.getLog();
        if (log.isEmpty()) {
            return null;
        }
        String wanted = log.get(log.size() - 1).getCallerSubgraph();
        ArrayList<EsuNode>[] levels = currentTree.getNodesByLevel();
        for (int depth = 1; depth < levels.length; depth++) {
            for (EsuNode node : levels[depth]) {
                if (node.getSubgraphAsString().equals(wanted)) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * @return the tree as it stands at the current step
     */
    public EsuTree getCurrentTree() {
        return currentTree;
    }

    /**
     * @return the finished tree, for layout and for telling dead ends apart
     */
    public EsuTree getFinalTree() {
        return finalTree;
    }

    /**
     * @return log entries describing the step just taken
     */
    public ArrayList<StepInfo> getCurrentLog() {
        return currentTree.getLog();
    }

    /**
     * The subgraphs the search found, each as its vertices.
     *
     * @return one list of vertices per subgraph
     */
    public List<List<Integer>> getSubgraphs() {
        List<List<Integer>> out = new ArrayList<>();
        for (LinkedList<Integer> subgraph : finalTree.getSubGraphs()) {
            out.add(List.copyOf(subgraph));
        }
        return out;
    }

    /**
     * The shapes among the subgraphs found, most frequent first.
     *
     * Classified once, on first asking, since it does not change.
     *
     * @return each distinct shape with how often it occurred
     */
    public List<Shape.Count> getShapes() {
        if (shapes == null) {
            shapes = Shape.classify(graph, getSubgraphs());
        }
        return shapes;
    }

    /**
     * The subgraphs having a given shape, as their vertices, so they can be
     * drawn.
     *
     * Always every one of them, whatever step is being shown: the shapes are
     * a property of the finished search, and the counts beside them are
     * final counts.
     *
     * @param shape the shape to look for
     * @return the vertices of each subgraph having it
     */
    public List<List<Integer>> subgraphsOfShape(Shape shape) {
        List<List<Integer>> found = new ArrayList<>();
        for (List<Integer> subgraph : getSubgraphs()) {
            if (Shape.of(graph, subgraph).equals(shape)) {
                found.add(subgraph);
            }
        }
        return found;
    }

    /**
     * The tree nodes whose subgraph has a given shape, named the way the tree
     * names them, so a view can pick them out.
     *
     * @param shape the shape to look for
     * @return those nodes' subgraph strings, empty if none has that shape
     */
    public Set<String> subgraphsWithShape(Shape shape) {
        Set<String> found = new LinkedHashSet<>();
        for (List<Integer> subgraph : getSubgraphs()) {
            if (Shape.of(graph, subgraph).equals(shape)) {
                StringBuilder name = new StringBuilder("{");
                for (Integer vertex : subgraph) {
                    name.append(name.length() == 1 ? "" : ", ").append(vertex);
                }
                found.add(name.append("}").toString());
            }
        }
        return found;
    }

    /**
     * Every step of the search, in order, each naming what it built.
     *
     * @return the history, empty when there is nothing to search
     */
    public List<Step> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /**
     * @return which step is being shown, 0 meaning before the search starts
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * @return how many steps the whole search takes
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * @return how many subgraphs of the requested size exist
     */
    public int getSubgraphCount() {
        return subgraphCount;
    }

    /**
     * @return the subgraph size being searched for
     */
    public int getSubgraphSize() {
        return subgraphSize;
    }

    /**
     * @return the graph being searched
     */
    public UndirectedGraph getGraph() {
        return graph;
    }
}
