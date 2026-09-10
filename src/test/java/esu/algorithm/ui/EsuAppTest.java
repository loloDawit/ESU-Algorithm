package esu.algorithm.ui;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * That the window is built with everything it is supposed to have.
 *
 * Saving the results once disappeared during a rebuild and stayed gone until
 * someone happened to read the README: nothing referred to it, so nothing
 * broke. A control that quietly stops being built is invisible to every test
 * that does not look at the window itself.
 */
public class EsuAppTest extends FxTest {

    /** Build the real window, off screen, and hand back its scene. */
    private Scene window() {
        return onFxThread(() -> {
            Stage stage = new Stage();
            new EsuApp().start(stage);
            Scene scene = stage.getScene();
            // Built and styled is all this needs; showing it is not.
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            stage.close();
            return scene;
        });
    }

    private static void collect(Node node, List<Node> found) {
        found.add(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collect(child, found);
            }
        }
    }

    private List<String> buttonLabels(Scene scene) {
        List<Node> all = new ArrayList<>();
        onFxThread(() -> {
            collect(scene.getRoot(), all);
            return null;
        });
        List<String> labels = new ArrayList<>();
        for (Node node : all) {
            if (node instanceof ButtonBase button && button.getText() != null
                    && !button.getText().isBlank()) {
                labels.add(button.getText());
            }
        }
        return labels;
    }

    private List<String> menuItems(Scene scene) {
        List<Node> all = new ArrayList<>();
        onFxThread(() -> {
            collect(scene.getRoot(), all);
            return null;
        });
        List<String> items = new ArrayList<>();
        for (Node node : all) {
            if (node instanceof MenuBar bar) {
                for (Menu menu : bar.getMenus()) {
                    for (MenuItem item : menu.getItems()) {
                        if (item.getText() != null) {
                            items.add(item.getText());
                        }
                    }
                }
            }
        }
        return items;
    }

    @Test
    public void offersEveryControlTheReadmeSaysItHas() {
        List<String> buttons = buttonLabels(window());

        for (String expected : List.of("Open graph", "Random", "Save results",
                "Fit", "Play")) {
            assertTrue(buttons.contains(expected),
                    "no control labelled '" + expected + "'; found " + buttons);
        }
    }

    @Test
    public void offersAPillForEverySubgraphSizeItSupports() {
        List<String> buttons = buttonLabels(window());

        for (int size = EsuApp.MIN_SUBGRAPH_SIZE;
                size <= EsuApp.MAX_SUBGRAPH_SIZE; size++) {
            assertTrue(buttons.contains(String.valueOf(size)),
                    "no pill for subgraph size " + size);
        }
    }

    @Test
    public void offersTheMenusAndTheirShortcuts() {
        Scene scene = window();

        List<String> items = menuItems(scene);
        for (String expected : List.of("Show step history", "Show shapes found",
                "Play or pause", "Next step", "Previous step")) {
            assertTrue(items.contains(expected),
                    "no menu item '" + expected + "'; found " + items);
        }
    }

    @Test
    public void startsWithNothingToActOnDisabled() {
        Scene scene = window();
        List<Node> all = new ArrayList<>();
        onFxThread(() -> {
            collect(scene.getRoot(), all);
            return null;
        });

        // Until a graph is open, stepping through a search makes no sense.
        for (Node node : all) {
            if (node instanceof ButtonBase button
                    && "Play".equals(button.getText())) {
                assertTrue(button.isDisabled(),
                        "Play is live before a graph is loaded");
            }
        }
    }
}
