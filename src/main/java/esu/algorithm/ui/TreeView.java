/*
 * The scrolling, zoomable canvas the search tree is drawn on.
 */
package esu.algorithm.ui;

import esu.algorithm.EsuNode;
import esu.algorithm.EsuTree;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
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
    /** What a search opens at: readable, whatever the finished tree's size. */
    private static final double START_ZOOM = 1;

    private final Pane canvas = new Pane();
    private final ScrollPane scroll = new ScrollPane();

    private TreeLayout layout;
    private Set<String> deadEnds = Set.of();
    private int foundAt;
    private boolean follow = true;

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
     * Opens at a readable size looking at the root, rather than zoomed out to
     * hold a tree that has not been built yet: at step one there is a single
     * box, and fitting the finished tree's width renders it too small to read.
     *
     * @param finalTree    the completed tree, which fixes the layout
     * @param subgraphSize the level whose nodes are complete subgraphs
     */
    public void setTree(EsuTree finalTree, int subgraphSize) {
        this.layout = TreeLayout.of(finalTree);
        this.deadEnds = findDeadEnds(finalTree);
        this.foundAt = subgraphSize;
        setZoom(START_ZOOM);
        Platform.runLater(() -> lookAt(layout.get(TreeLayout.ROOT_ID)));
    }

    /**
     * Draw the tree as it stands at a step.
     *
     * @param currentTree the tree at the step being shown
     * @param activeId    subgraph of the node being worked on, or null
     * @param traceId     subgraph of the node to trace back to the root
     */
    public void show(EsuTree currentTree, String activeId, String traceId) {
        if (layout == null) {
            return;
        }
        Set<String> path = traceId == null
                ? Set.of() : Set.copyOf(layout.pathToRoot(traceId));
        canvas.getChildren().setAll(TreeRenderer.render(layout,
                presentNodes(currentTree), deadEnds, foundAt, activeId, path));

        // Keep up with the search rather than making the viewer chase it: the
        // tree grows down and to the right, off the edge of the window.
        if (follow && activeId != null) {
            lookAt(layout.get(activeId));
        }
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
        // The tree hangs from the root, so the top is what to show.
        scroll.setHvalue(0.5);
        scroll.setVvalue(0);
    }

    /**
     * Scroll so a box is in view, roughly centred.
     *
     * @param box the box to look at; ignored when null
     */
    public void lookAt(TreeLayout.Box box) {
        if (box == null || layout == null) {
            return;
        }
        Bounds view = scroll.getViewportBounds();
        double zoom = getZoom();
        double contentWidth = layout.getWidth() * zoom;
        double contentHeight = layout.getHeight() * zoom;

        if (contentWidth > view.getWidth()) {
            scroll.setHvalue(clamp((box.centreX() * zoom - view.getWidth() / 2)
                    / (contentWidth - view.getWidth())));
        }
        if (contentHeight > view.getHeight()) {
            scroll.setVvalue(clamp((box.centreY() * zoom - view.getHeight() / 2)
                    / (contentHeight - view.getHeight())));
        }
    }

    /**
     * @param follow true to keep the node being built in view as it steps
     */
    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
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
    private Set<String> presentNodes(EsuTree tree) {
        Set<String> present = new HashSet<>();
        ArrayList<EsuNode>[] levels = tree.getNodesByLevel();
        for (int level = 1; level < levels.length; level++) {
            for (EsuNode node : levels[level]) {
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
    private Set<String> findDeadEnds(EsuTree finalTree) {
        Set<String> dead = new HashSet<>();
        ArrayList<EsuNode>[] levels = finalTree.getNodesByLevel();
        for (int level = 1; level < levels.length - 1; level++) {
            for (EsuNode node : levels[level]) {
                if (node.getChildren().isEmpty()) {
                    dead.add(node.getSubgraphAsString());
                }
            }
        }
        return dead;
    }
}
