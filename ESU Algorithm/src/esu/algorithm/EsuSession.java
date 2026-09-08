/*
 * One run of the ESU algorithm over one graph, positioned at a step.
 */
package esu.algorithm;

import java.util.ArrayList;

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

    private final UndirectedGraph graph;
    private final int subgraphSize;

    /** The finished tree: layout, dead ends and totals all come from here. */
    private final ESUTree finalTree;
    private final int totalSteps;
    private final int subgraphCount;

    /** The tree as it stood at currentStep, rebuilt by replay. */
    private ESUTree currentTree;
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

        ESUTree tree = new ESUTree(graph, subgraphSize);
        int steps = 0;
        while (tree.step()) {
            tree.clearStepLog();
            steps++;
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

        ESUTree tree = new ESUTree(graph, subgraphSize);
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
     * @return the tree as it stands at the current step
     */
    public ESUTree getCurrentTree() {
        return currentTree;
    }

    /**
     * @return the finished tree, for layout and for telling dead ends apart
     */
    public ESUTree getFinalTree() {
        return finalTree;
    }

    /**
     * @return log entries describing the step just taken
     */
    public ArrayList<StepInfo> getCurrentLog() {
        return currentTree.getLog();
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
