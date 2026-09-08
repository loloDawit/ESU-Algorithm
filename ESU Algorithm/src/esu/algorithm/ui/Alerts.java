/*
 * The dialogs the app shows when something needs saying.
 */
package esu.algorithm.ui;

import javafx.scene.control.Alert;

/**
 * Class Alerts
 *
 * Each message says what happened and what to do about it. The 2018 version
 * also had one that refused to start until you ticked a box promising your
 * graph was undirected, which is the only kind the app supports.
 */
public class Alerts {

    private Alerts() {
    }

    /**
     * Report that the search finished without finding anything, so a tree of
     * partial branches is not mistaken for a result.
     *
     * @param subgraphSize the k that was searched for
     */
    public static void displayNoSubgraphs(int subgraphSize) {
        show(Alert.AlertType.INFORMATION, "No subgraphs found",
                "No connected subgraphs of size " + subgraphSize,
                "The tree shows how far each branch got before it ran out of "
                + "valid vertices. None reached size " + subgraphSize + ", so "
                + "nothing on screen is a result.\n\n"
                + "Try a smaller size, or a graph with more edges.");
    }

    /**
     * @param name the file that could not be read
     */
    public static void displayUnreadableFile(String name) {
        show(Alert.AlertType.ERROR, "Cannot read graph",
                "Nothing usable in " + name,
                "A graph file is one edge per line: two whitespace-separated "
                + "whole numbers, such as \"0 1\".");
    }

    /**
     * @param reason why the file could not be written
     */
    public static void displayCouldNotWrite(String reason) {
        show(Alert.AlertType.ERROR, "Cannot save graph",
                "The generated graph could not be written",
                reason == null ? "No further detail." : reason);
    }

    /**
     * @param cause what went wrong while enumerating
     */
    public static void displayCouldNotRun(Throwable cause) {
        show(Alert.AlertType.ERROR, "Search failed",
                "The algorithm stopped before finishing",
                cause == null ? "No further detail." : String.valueOf(cause));
    }

    private static void show(Alert.AlertType type, String title,
            String header, String body) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(body);
        alert.showAndWait();
    }
}
