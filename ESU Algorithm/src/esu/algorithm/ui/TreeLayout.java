/*
 * Where every node of the search tree goes.
 */
package esu.algorithm.ui;

import esu.algorithm.ESUNode;
import esu.algorithm.ESUTree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Class TreeLayout
 *
 * Positions the tree so that a node sits directly under its parent.
 *
 * The 2018 layout spread each level evenly across the full width of the tree,
 * placing nodes by their index within the level. A node therefore sat nowhere
 * near its parent, and the connecting lines had to sprawl diagonally across
 * everything, which is why the picture looked like a web rather than a tree.
 *
 * This walks the tree bottom-up: leaves take the next free slot along the
 * row, and every parent is centred over its children. Subtrees occupy
 * disjoint ranges of slots, so nothing can overlap.
 *
 * Free of JavaFX, so the geometry can be tested without a display.
 */
public class TreeLayout {

    /** Every box is the same width, which keeps parents safely centred. */
    private static final double BOX_WIDTH = 74;
    private static final double BOX_HEIGHT = 30;
    private static final double COLUMN_GAP = 16;
    private static final double ROW_GAP = 46;

    /** What the root box says, since it stands for choosing nothing yet. */
    public static final String ROOT_ID = "[root]";
    public static final String ROOT_LABEL = "start";

    /**
     * One positioned node.
     */
    public static class Box {

        private final String id;
        private final String label;
        private final int level;
        private final String parentId;
        private double x;
        private final double y;

        Box(String id, String label, int level, String parentId, double y) {
            this.id = id;
            this.label = label;
            this.level = level;
            this.parentId = parentId;
            this.y = y;
        }

        /** @return the node's subgraph string, which identifies it */
        public String getId() {
            return id;
        }

        /** @return what to draw inside the box */
        public String getLabel() {
            return label;
        }

        /** @return depth in the tree, 0 being the root */
        public int getLevel() {
            return level;
        }

        /** @return the parent's id, or null for the root */
        public String getParentId() {
            return parentId;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return BOX_WIDTH;
        }

        public double getHeight() {
            return BOX_HEIGHT;
        }

        public double centreX() {
            return x + BOX_WIDTH / 2;
        }

        public double centreY() {
            return y + BOX_HEIGHT / 2;
        }

        /** @return true if this is the root, which is not a subgraph */
        public boolean isRoot() {
            return parentId == null;
        }
    }

    private final Map<String, Box> boxes = new LinkedHashMap<>();
    private final Map<String, List<Box>> children = new LinkedHashMap<>();
    private int depth;
    private double width;
    private double height;

    private double nextLeafX;

    private TreeLayout() {
    }

    /**
     * Lay out a finished tree.
     *
     * @param tree the tree to position, normally the final state
     * @return positions for every node in it
     */
    public static TreeLayout of(ESUTree tree) {
        TreeLayout layout = new TreeLayout();
        layout.build(tree);
        return layout;
    }

    private void build(ESUTree tree) {
        ArrayList<ESUNode>[] levels = tree.getNodesByLevel();

        // The root is not in getNodesByLevel's usable form, so it is placed
        // explicitly and everything on level 1 is treated as its child.
        Box root = new Box(ROOT_ID, ROOT_LABEL, 0, null, 0);
        boxes.put(ROOT_ID, root);
        children.put(ROOT_ID, new ArrayList<>());

        for (int level = 1; level < levels.length; level++) {
            for (ESUNode node : levels[level]) {
                String id = node.getSubgraphAsString();
                String parentId = level == 1
                        ? ROOT_ID : node.getParent().getSubgraphAsString();
                Box box = new Box(id, labelFor(node), level, parentId,
                        level * (BOX_HEIGHT + ROW_GAP));
                boxes.put(id, box);
                children.computeIfAbsent(parentId, key -> new ArrayList<>())
                        .add(box);
                children.computeIfAbsent(id, key -> new ArrayList<>());
            }
        }

        place(root);

        // Depth comes from the boxes that exist, not from the tree's declared
        // height: a graph with no edges produces a root and nothing else.
        depth = 0;
        for (Box box : boxes.values()) {
            depth = Math.max(depth, box.getLevel());
        }

        width = Math.max(nextLeafX - COLUMN_GAP, BOX_WIDTH);
        height = (depth + 1) * BOX_HEIGHT + depth * ROW_GAP;
    }

    /**
     * Position a node once its children are positioned: a leaf takes the next
     * free slot, a parent is centred over the children it produced.
     *
     * @param box the node to place
     */
    private void place(Box box) {
        List<Box> kids = childrenOf(box);
        if (kids.isEmpty()) {
            box.x = nextLeafX;
            nextLeafX += BOX_WIDTH + COLUMN_GAP;
            return;
        }
        for (Box kid : kids) {
            place(kid);
        }
        double first = kids.get(0).centreX();
        double last = kids.get(kids.size() - 1).centreX();
        box.x = (first + last) / 2 - BOX_WIDTH / 2;
    }

    /**
     * The vertices of a node, without set notation: the boxes are small and
     * the punctuation meant nothing to a reader without a key.
     *
     * @param node the node to label
     * @return its vertices separated by spaces
     */
    private String labelFor(ESUNode node) {
        LinkedList<Integer> vertices = new LinkedList<>();
        node.getSubGraph(vertices);
        StringBuilder out = new StringBuilder();
        for (Integer vertex : vertices) {
            out.append(out.length() == 0 ? "" : " ").append(vertex);
        }
        return out.toString();
    }

    /**
     * @param id a node's subgraph string
     * @return its box, or null if the tree has no such node
     */
    public Box get(String id) {
        return boxes.get(id);
    }

    /**
     * @return every box, root first
     */
    public Collection<Box> boxes() {
        return boxes.values();
    }

    /**
     * @param box the parent
     * @return its children, left to right
     */
    public List<Box> childrenOf(Box box) {
        return children.getOrDefault(box.getId(), Collections.emptyList());
    }

    /**
     * @param level depth to fetch
     * @return the boxes at that depth
     */
    public List<Box> row(int level) {
        List<Box> row = new ArrayList<>();
        for (Box box : boxes.values()) {
            if (box.getLevel() == level) {
                row.add(box);
            }
        }
        return row;
    }

    /**
     * The chain of nodes from the root down to a node, which is the sequence
     * of choices that produced it.
     *
     * @param id the node to trace back from
     * @return ids from the root to that node, empty if it is not in the tree
     */
    public List<String> pathToRoot(String id) {
        List<String> path = new ArrayList<>();
        Box box = boxes.get(id);
        while (box != null) {
            path.add(0, box.getId());
            box = box.getParentId() == null ? null : boxes.get(box.getParentId());
        }
        return path;
    }

    /** @return depth of the deepest level, 0 if only the root exists */
    public int getDepth() {
        return depth;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}
