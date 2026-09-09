/*
 * The scrolling, zoomable canvas the search tree is drawn on.
 */
package esu.algorithm.ui;

import esu.algorithm.ESUNode;
import esu.algorithm.ESUTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Class TreeView
 *
 * Owns the tree drawing and everything about looking at it: what is on
 * screen, how far it is zoomed, and fitting it to the window.
 *
 * The window used to hold the pane, the scroll pane, the zoom factor and the
 * layout itself, and every method that touched any of them. Keeping them
 * together here means the window asks for a step to be shown and is done.
 */
public class TreeView extends StackPane {

    /** Low enough that a large tree can be fitted; see fit(). */
    public static final double MIN_ZOOM = 0.02;
    public static final double MAX_ZOOM = 2;

    private final Pane canvas = new Pane();
    private final ScrollPane scroll = new ScrollPane();

    private TreeLayout layout;
    private Set<String> deadEnds = Set.of();
    private int foundAt;

    public TreeView() {
        // A Group reports the scaled size of its content, which is what makes
        // the scroll bars track the zoom.
        scroll.setContent(new Group(canvas));
        scroll.setPannable(true);
        scroll.getStyleClass().add("tree-scroll");
        getChildren().add(scroll);
        getStyleClass().add("tree-area");
    }

    /**
     * Show a finished search, positioned at its first step.
     *
     * @param finalTree    the completed tree, which fixes the layout
     * @param subgraphSize the level whose nodes are complete subgraphs
     */
    public void setTree(ESUTree finalTree, int subgraphSize) {
        this.layout = TreeLayout.of(finalTree);
        this.deadEnds = findDeadEnds(finalTree);
        this.foundAt = subgraphSize;
    }

    /**
     * Draw the tree as it stands at a step.
     *
     * @param currentTree the tree at the step being shown
     * @param activeId    subgraph of the node being worked on, or null
     * @param traceId     subgraph of the node to trace back to the root
     */
    public void show(ESUTree currentTree, String activeId, String traceId) {
        if (layout == null) {
            return;
        }
        Set<String> path = traceId == null
                ? Set.of() : Set.copyOf(layout.pathToRoot(traceId));
        canvas.getChildren().setAll(TreeRenderer.render(layout,
                presentNodes(currentTree), deadEnds, foundAt, activeId, path));
    }

    public void clear() {
        canvas.getChildren().clear();
        layout = null;
    }

    /**
     * Zoom so the whole tree is visible, and scroll back to the top left.
     *
     * Measures the tree rather than the canvas: the canvas is stretched to the
     * viewport, so measuring it would never zoom out.
     */
    public void fit() {
        if (layout == null) {
            return;
        }
        Bounds view = scroll.getViewportBounds();
        if (layout.getWidth() <= 0 || view.getWidth() <= 0) {
            return;
        }
        setZoom(Math.min(view.getWidth() / layout.getWidth(),
                view.getHeight() / layout.getHeight()) * 0.92);
        scroll.setHvalue(0);
        scroll.setVvalue(0);
    }

    public double getZoom() {
        return canvas.getScaleX();
    }

    /**
     * @param zoom scale factor, clamped to what the view supports
     */
    public void setZoom(double zoom) {
        double clamped = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
        canvas.setScaleX(clamped);
        canvas.setScaleY(clamped);
    }

    /** Ids of the nodes that exist at the step being shown. */
    private Set<String> presentNodes(ESUTree tree) {
        Set<String> present = new HashSet<>();
        ArrayList<ESUNode>[] levels = tree.getNodesByLevel();
        for (int level = 1; level < levels.length; level++) {
            for (ESUNode node : levels[level]) {
                present.add(node.getSubgraphAsString());
            }
        }
        return present;
    }

    /**
     * Nodes that never gained a child, which is what makes them dead ends.
     * Asking the current tree would be wrong: mid-run a node has no children
     * only because it has not expanded yet.
     */
    private Set<String> findDeadEnds(ESUTree finalTree) {
        Set<String> dead = new HashSet<>();
        ArrayList<ESUNode>[] levels = finalTree.getNodesByLevel();
        for (int level = 1; level < levels.length - 1; level++) {
            for (ESUNode node : levels[level]) {
                if (node.getChildren().isEmpty()) {
                    dead.add(node.getSubgraphAsString());
                }
            }
        }
        return dead;
    }
}
