/*
 * Turning a laid-out tree into shapes.
 */
package esu.algorithm.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Class TreeRenderer
 *
 * Draws the boxes and the connectors between them, and colours each box by
 * what the algorithm did with that node.
 *
 * Connectors are elbows rather than straight diagonals: with the tidy layout
 * a child sits under its parent, so a drop, a short run across and a drop in
 * reads as a tree. The old diagonals were a symptom of nodes being placed
 * nowhere near their parents.
 */
public class TreeRenderer {


    private static final Font LABEL_FONT =
            Font.font("SF Mono", FontWeight.SEMI_BOLD, 12);
    private static final Font CAPTION_FONT = Font.font("SF Pro Text", 10);

    private TreeRenderer() {
    }

    /**
     * Build every shape for the tree as it stands at one step.
     *
     * @param layout    positions for the whole finished tree
     * @param present   ids of the nodes that exist at this step
     * @param deadEnds  ids that never gained a child in the finished tree
     * @param foundAt   the level whose nodes are complete subgraphs
     * @param activeId  the node being worked on, or null
     * @param path      ids from the root to the node of interest
     * @return shapes to add to a pane, connectors first
     */
    public static List<Node> render(TreeLayout layout, Set<String> present,
            Set<String> deadEnds, int foundAt, String activeId,
            Set<String> path, Set<String> picked) {

        List<Node> shapes = new ArrayList<>();

        // Connectors first so the boxes sit on top of them.
        for (TreeLayout.Box box : layout.boxes()) {
            if (box.isRoot() || !isVisible(box, present)) {
                continue;
            }
            TreeLayout.Box parent = layout.get(box.getParentId());
            if (parent == null || !isVisible(parent, present)) {
                continue;
            }
            boolean onPath = path.contains(box.getId())
                    && path.contains(parent.getId());
            shapes.add(elbow(parent, box, onPath));
        }

        for (TreeLayout.Box box : layout.boxes()) {
            if (!isVisible(box, present)) {
                continue;
            }
            shapes.addAll(boxShapes(box, deadEnds, foundAt, activeId, path,
                    picked));
        }
        return shapes;
    }

    /** The root is always drawn; other nodes only once they exist. */
    private static boolean isVisible(TreeLayout.Box box, Set<String> present) {
        return box.isRoot() || present.contains(box.getId());
    }

    /**
     * A parent-to-child connector: down out of the parent, across, then down
     * into the child.
     *
     * @param parent the box above
     * @param child  the box below
     * @param onPath true to draw it as part of the traced path
     * @return the connector
     */
    private static Polyline elbow(TreeLayout.Box parent, TreeLayout.Box child,
            boolean onPath) {
        double midY = (parent.getY() + parent.getHeight() + child.getY()) / 2;
        Polyline line = new Polyline(
                parent.centreX(), parent.getY() + parent.getHeight(),
                parent.centreX(), midY,
                child.centreX(), midY,
                child.centreX(), child.getY());
        line.getStyleClass().add(onPath ? "t-edge t-edge-path" : "t-edge");
        if (onPath) {
            line.getStyleClass().add("t-edge-path");
        }
        return line;
    }

    /**
     * A box and its label, coloured by the node's state.
     *
     * @param box      the node to draw
     * @param deadEnds ids that never gained a child
     * @param foundAt  the level whose nodes are complete subgraphs
     * @param activeId the node being worked on, or null
     * @param path     ids from the root to the node of interest
     * @return the rectangle, its label, and the root's caption
     */
    private static List<Node> boxShapes(TreeLayout.Box box,
            Set<String> deadEnds, int foundAt, String activeId,
            Set<String> path, Set<String> picked) {

        Rectangle rect = new Rectangle(box.getX(), box.getY(),
                box.getWidth(), box.getHeight());
        rect.setArcWidth(8);
        rect.setArcHeight(8);
        rect.getStyleClass().add("t-box");
        rect.getStyleClass().add(stateOf(box, deadEnds, foundAt, activeId));
        if (path.contains(box.getId()) && !box.getId().equals(activeId)) {
            rect.getStyleClass().add("t-on-path");
        }
        if (picked.contains(box.getId())) {
            rect.getStyleClass().add("t-picked");
        }

        List<Node> shapes = new ArrayList<>();
        shapes.add(rect);
        Text label = centredText(box.getLabel(), LABEL_FONT,
                box.centreX(), box.centreY());
        label.getStyleClass().add("t-text");
        if (deadEnds.contains(box.getId()) && !box.isRoot()) {
            label.getStyleClass().add("t-text-dead");
        }
        shapes.add(label);

        if (box.isRoot()) {
            // The root is not a subgraph, and nothing on screen said so.
            // Above the box: below it runs the row of connectors.
            Text caption = centredText("no vertices chosen", CAPTION_FONT,
                    box.centreX(), box.getY() - 9);
            caption.getStyleClass().add("t-caption");
            shapes.add(caption);
        }
        return shapes;
    }

    /**
     * Which style class describes what the algorithm did with a node.
     *
     * @param box      the node
     * @param deadEnds ids that never gained a child
     * @param foundAt  the level whose nodes are complete subgraphs
     * @param activeId the node being worked on, or null
     * @return the style class to add
     */
    private static String stateOf(TreeLayout.Box box, Set<String> deadEnds,
            int foundAt, String activeId) {
        if (box.isRoot()) {
            return "t-root";
        }
        if (box.getId().equals(activeId)) {
            return "t-active";
        }
        if (box.getLevel() == foundAt) {
            return "t-complete";
        }
        if (deadEnds.contains(box.getId())) {
            return "t-dead";
        }
        return "t-pending";
    }

    /**
     * Text centred on a point, measured rather than estimated.
     *
     * @param content what to write
     * @param font    the face to write it in
     * @param centreX horizontal centre
     * @param centreY vertical centre of the text
     * @return the positioned text
     */
    private static Text centredText(String content, Font font,
            double centreX, double centreY) {
        Text text = new Text(content);
        text.setFont(font);
        text.setX(centreX - text.getLayoutBounds().getWidth() / 2);
        text.setY(centreY + text.getLayoutBounds().getHeight() / 4);
        return text;
    }
}
