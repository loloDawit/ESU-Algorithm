package esu.algorithm.ui;

import esu.algorithm.EsuNode;
import esu.algorithm.EsuSession;
import esu.algorithm.EsuTree;
import esu.algorithm.UndirectedGraph;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * What the tree renderer actually builds.
 *
 * Nothing checked this before, and the shapes it draws are the whole point of
 * the app: a box in the wrong state, or no box at all, is invisible to every
 * other test in the project.
 */
public class TreeRendererTest extends FxTest {

    private EsuSession session(String file, int size) {
        return new EsuSession(
                UndirectedGraph.fromFile(new File("samples/" + file)), size);
    }

    /** The rectangles the renderer produces for a finished search. */
    private List<Rectangle> boxesOf(EsuSession session, Set<String> picked) {
        return onFxThread(() -> {
            TreeLayout layout = TreeLayout.of(session.getFinalTree());
            List<Node> drawn = TreeRenderer.render(layout,
                    presentIn(session.getCurrentTree()), deadEndsIn(session),
                    session.getSubgraphSize(), null, Set.of(), picked);
            List<Rectangle> boxes = new ArrayList<>();
            for (Node node : drawn) {
                if (node instanceof Rectangle rectangle) {
                    boxes.add(rectangle);
                }
            }
            return boxes;
        });
    }

    private Set<String> presentIn(EsuTree tree) {
        Set<String> present = new HashSet<>();
        ArrayList<EsuNode>[] levels = tree.getNodesByLevel();
        for (int level = 1; level < levels.length; level++) {
            for (EsuNode node : levels[level]) {
                present.add(node.getSubgraphAsString());
            }
        }
        return present;
    }

    private Set<String> deadEndsIn(EsuSession session) {
        Set<String> dead = new HashSet<>();
        ArrayList<EsuNode>[] levels = session.getFinalTree().getNodesByLevel();
        for (int level = 1; level < levels.length - 1; level++) {
            for (EsuNode node : levels[level]) {
                if (node.getChildren().isEmpty()) {
                    dead.add(node.getSubgraphAsString());
                }
            }
        }
        return dead;
    }

    private long countWithClass(List<Rectangle> boxes, String styleClass) {
        return boxes.stream()
                .filter(box -> box.getStyleClass().contains(styleClass))
                .count();
    }

    @Test
    public void drawsABoxForTheRootAndEveryNodeReached() {
        EsuSession session = session("bowtie.txt", 3);
        session.goToStep(session.getTotalSteps());

        List<Rectangle> boxes = boxesOf(session, Set.of());

        // Every node of the finished tree, plus the root.
        int nodes = 1;
        for (ArrayList<EsuNode> level : session.getFinalTree().getNodesByLevel()) {
            nodes += level.size();
        }
        assertEquals(nodes - 1, boxes.size());
        assertEquals(1, countWithClass(boxes, "t-root"));
    }

    @Test
    public void drawsOnlyWhatTheSearchHasReached() {
        EsuSession session = session("bowtie.txt", 3);

        session.goToStep(0);

        // Before the first step there is nothing but the beginning.
        assertEquals(1, boxesOf(session, Set.of()).size());
    }

    @Test
    public void marksCompletedSubgraphsAsFound() {
        EsuSession session = session("bowtie.txt", 3);
        session.goToStep(session.getTotalSteps());

        List<Rectangle> boxes = boxesOf(session, Set.of());

        assertEquals(session.getSubgraphCount(),
                countWithClass(boxes, "t-complete"));
    }

    @Test
    public void marksBranchesThatDiedAsDeadEnds() {
        EsuSession session = session("bowtie.txt", 4);
        session.goToStep(session.getTotalSteps());

        List<Rectangle> boxes = boxesOf(session, Set.of());

        assertEquals(deadEndsIn(session).size(), countWithClass(boxes, "t-dead"));
        assertTrue(countWithClass(boxes, "t-dead") > 0,
                "this graph should have branches that die");
    }

    @Test
    public void picksOutTheSubgraphsItIsAskedTo() {
        EsuSession session = session("cluster.txt", 4);
        session.goToStep(session.getTotalSteps());
        Set<String> picked =
                session.subgraphsWithShape(session.getShapes().get(0).shape());

        List<Rectangle> boxes = boxesOf(session, picked);

        assertTrue(picked.size() > 1, "pick more than one, or this proves little");
        assertEquals(picked.size(), countWithClass(boxes, "t-picked"));
    }

    @Test
    public void givesEveryBoxExactlyOneState() {
        EsuSession session = session("cluster.txt", 4);
        session.goToStep(session.getTotalSteps());

        for (Rectangle box : boxesOf(session, Set.of())) {
            long states = box.getStyleClass().stream()
                    .filter(name -> List.of("t-root", "t-pending", "t-active",
                            "t-complete", "t-dead").contains(name))
                    .count();
            assertEquals(1, states,
                    "a box carried " + states + " states: " + box.getStyleClass());
        }
    }
}
