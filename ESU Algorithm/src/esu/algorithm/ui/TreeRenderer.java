/*
 * Turning a laid-out tree into shapes.
 */
package esu.algorithm.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.paint.Color;
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

    /** A node that has not been reached yet, or is still expanding. */
    public static final Color PENDING_FILL = Color.web("#ffffff");
    public static final Color PENDING_STROKE = Color.web("#aab4c0");
    /** The node the current step is working on. */
    public static final Color ACTIVE_FILL = Color.web("#fff3cd");
    public static final Color ACTIVE_STROKE = Color.web("#b06f00");
    /** A subgraph of the requested size: an actual result. */
    public static final Color COMPLETE_FILL = Color.web("#d7f0dc");
    public static final Color COMPLETE_STROKE = Color.web("#2e7d4f");
    /** A branch that ran out of valid vertices before reaching size k. */
    public static final Color DEADEND_FILL = Color.web("#f4f6f8");
    public static final Color DEADEND_STROKE = Color.web("#c7cdd4");
    /** The root, which stands for having chosen nothing yet. */
    public static final Color ROOT_FILL = Color.web("#eef2f9");
    public static final Color ROOT_STROKE = Color.web("#8fa2bd");

    private static final Color EDGE = Color.web("#cfd6de");
    /** The chain of choices leading to the node being looked at. */
    private static final Color PATH = Color.web("#2f6fed");

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
            Set<String> path) {

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
            shapes.addAll(boxShapes(box, deadEnds, foundAt, activeId, path));
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
        line.setStroke(onPath ? PATH : EDGE);
        line.setStrokeWidth(onPath ? 2.2 : 1.2);
        line.setFill(null);
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
            Set<String> path) {

        Rectangle rect = new Rectangle(box.getX(), box.getY(),
                box.getWidth(), box.getHeight());
        rect.setArcWidth(8);
        rect.setArcHeight(8);
        rect.setStrokeWidth(1.2);

        Color labelFill = Color.web("#20303f");

        if (box.isRoot()) {
            rect.setFill(ROOT_FILL);
            rect.setStroke(ROOT_STROKE);
            labelFill = Color.web("#41566f");
        } else if (box.getId().equals(activeId)) {
            rect.setFill(ACTIVE_FILL);
            rect.setStroke(ACTIVE_STROKE);
            rect.setStrokeWidth(2.4);
        } else if (box.getLevel() == foundAt) {
            rect.setFill(COMPLETE_FILL);
            rect.setStroke(COMPLETE_STROKE);
            rect.setStrokeWidth(1.8);
        } else if (deadEnds.contains(box.getId())) {
            rect.setFill(DEADEND_FILL);
            rect.setStroke(DEADEND_STROKE);
            rect.getStrokeDashArray().addAll(4.0, 3.0);
            labelFill = Color.web("#93a0ad");
        } else {
            rect.setFill(PENDING_FILL);
            rect.setStroke(PENDING_STROKE);
        }

        if (path.contains(box.getId()) && !box.getId().equals(activeId)) {
            rect.setStroke(PATH);
            rect.setStrokeWidth(2.0);
        }

        List<Node> shapes = new ArrayList<>();
        shapes.add(rect);
        shapes.add(centredText(box.getLabel(), LABEL_FONT, labelFill,
                box.centreX(), box.centreY()));

        if (box.isRoot()) {
            // The root is not a subgraph, and nothing on screen said so.
            // Above the box: below it runs the row of connectors.
            shapes.add(centredText("no vertices chosen", CAPTION_FONT,
                    Color.web("#8a97a6"), box.centreX(), box.getY() - 9));
        }
        return shapes;
    }

    /**
     * Text centred on a point, measured rather than estimated.
     *
     * @param content what to write
     * @param font    the face to write it in
     * @param fill    the colour
     * @param centreX horizontal centre
     * @param centreY vertical centre of the text
     * @return the positioned text
     */
    private static Text centredText(String content, Font font, Color fill,
            double centreX, double centreY) {
        Text text = new Text(content);
        text.setFont(font);
        text.setFill(fill);
        text.setX(centreX - text.getLayoutBounds().getWidth() / 2);
        text.setY(centreY + text.getLayoutBounds().getHeight() / 4);
        return text;
    }
}
