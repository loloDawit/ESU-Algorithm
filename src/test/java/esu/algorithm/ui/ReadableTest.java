package esu.algorithm.ui;

import esu.algorithm.EsuSession;
import esu.algorithm.UndirectedGraph;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Whether the words can be read against what is behind them.
 *
 * ThemeTest compares the two stylesheets as text, which catches a rule the
 * dark one forgets. This asks the question the other way round, of the styles
 * JavaFX actually resolves: whatever the sheets say, is this label a different
 * colour from its background?
 *
 * The step log once rendered dark text on a dark ground for exactly the
 * reason no rule caught: the class that lost its colour was on a label inside
 * a cell, not on anything either sheet named directly.
 */
public class ReadableTest extends FxTest {

    /** Below this, text and ground are too close to tell apart. */
    private static final double LEAST_DIFFERENCE = 0.18;

    /** Perceived brightness, so that dark-on-dark and light-on-light both fail. */
    private static double brightness(Color colour) {
        return 0.299 * colour.getRed()
                + 0.587 * colour.getGreen()
                + 0.114 * colour.getBlue();
    }

    /** The nearest ancestor that actually paints something. */
    private static Color groundBehind(Node node) {
        for (Parent at = node.getParent(); at != null; at = at.getParent()) {
            if (at instanceof Region region && region.getBackground() != null
                    && !region.getBackground().getFills().isEmpty()) {
                var fill = region.getBackground().getFills().get(0).getFill();
                if (fill instanceof Color colour && colour.getOpacity() > 0.9) {
                    return colour;
                }
            }
        }
        return null;
    }

    private static void collectText(Node node, List<Node> found) {
        if (node instanceof Label || node instanceof Text) {
            found.add(node);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectText(child, found);
            }
        }
    }

    /** A panel with a search loaded, styled for one appearance. */
    private Parent shapesPanel(boolean dark) {
        return onFxThread(() -> {
            EsuSession session = new EsuSession(
                    UndirectedGraph.fromFile(new File("samples/cluster.txt")), 4);
            ShapesPanel panel = new ShapesPanel(shape -> { }, vertices -> { });
            panel.setGraph(session.getGraph());
            panel.setShapes(session.getShapes());
            panel.showInstances(
                    session.subgraphsOfShape(session.getShapes().get(0).shape()));
            return style(panel, dark);
        });
    }

    @ParameterizedTest(name = "dark={0}")
    @ValueSource(booleans = {false, true})
    public void everyWordStandsOutFromWhatIsBehindIt(boolean dark) {
        Parent panel = shapesPanel(dark);

        List<Node> words = new ArrayList<>();
        onFxThread(() -> {
            collectText(panel, words);
            return null;
        });
        assertFalse(words.isEmpty(), "found nothing to read");

        for (Node word : words) {
            Color ink = inkOf(word);
            Color ground = groundBehind(word);
            if (ink == null || ground == null) {
                continue;
            }
            assertTrue(Math.abs(brightness(ink) - brightness(ground))
                            > LEAST_DIFFERENCE,
                    text(word) + ": " + ink + " on " + ground);
        }
    }

    @ParameterizedTest(name = "dark={0}")
    @ValueSource(booleans = {false, true})
    public void theStepLogCanBeRead(boolean dark) {
        // The list cell's label is the thing that went wrong, so build the
        // list the app builds rather than a bare label.
        Parent panel = onFxThread(() -> {
            ListView<String> log = new ListView<>();
            log.getItems().add("2 is less than or equal to this branch's first step.");
            Label line = new Label(log.getItems().get(0));
            line.getStyleClass().add("log-line");
            javafx.scene.layout.VBox host = new javafx.scene.layout.VBox(line);
            host.getStyleClass().add("side-panel");
            return style(host, dark);
        });

        List<Node> words = new ArrayList<>();
        onFxThread(() -> {
            collectText(panel, words);
            return null;
        });

        for (Node word : words) {
            Color ink = inkOf(word);
            Color ground = groundBehind(word);
            if (ink == null) {
                continue;
            }
            assertNotNull(ground, "nothing paints behind the log line");
            assertTrue(Math.abs(brightness(ink) - brightness(ground))
                            > LEAST_DIFFERENCE,
                    "log line " + ink + " on " + ground);
        }
    }

    /**
     * The colour a piece of text is drawn in.
     *
     * A Label's skin puts a LabeledText inside it, so walking the scene finds
     * both the Label and the Text it renders through; either can answer.
     */
    private static Color inkOf(Node word) {
        javafx.scene.paint.Paint paint = word instanceof Label label
                ? label.getTextFill() : ((Text) word).getFill();
        return paint instanceof Color colour ? colour : null;
    }

    private static String text(Node node) {
        return node instanceof Label label ? label.getText() : ((Text) node).getText();
    }
}
