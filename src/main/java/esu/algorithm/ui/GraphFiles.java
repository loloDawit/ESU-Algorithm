/*
 * Everything the window does with the filesystem.
 */
package esu.algorithm.ui;

import esu.algorithm.EsuTree;
import esu.algorithm.RandomGraph;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Random;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * Class GraphFiles
 *
 * Opening a graph, saving results, and writing a generated graph out.
 *
 * Gathered here so the window is not also a file handler, and so the dialogs
 * are configured the same way each time: the chooser used to accumulate a
 * fresh extension filter on every open.
 */
public class GraphFiles {

    /** Where generated graphs are written, beside the graphs that ship. */
    private static final String SAMPLES = "samples";

    private GraphFiles() {
    }

    /**
     * Ask for a graph file to open.
     *
     * @param owner window the dialog belongs to
     * @return the chosen file, or null if the dialog was dismissed
     */
    public static File chooseGraph(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open a graph file");
        chooser.setInitialDirectory(startingDirectory());
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Graph files (*.txt)", "*.txt"));
        return chooser.showOpenDialog(owner);
    }

    /**
     * Generate a random connected graph and write it out as an ordinary graph
     * file, so it can be reopened, edited or kept.
     *
     * @param random source of randomness
     * @return the file written
     * @throws IOException if it could not be written
     */
    public static File writeRandomGraph(Random random) throws IOException {
        File file = new File(SAMPLES, "random-graph.txt");
        file.getParentFile().mkdirs();
        return RandomGraph.writeToFile(RandomGraph.generate(random), file);
    }

    /**
     * Ask where to save the subgraphs found, and write them there.
     *
     * @param owner        window the dialog belongs to
     * @param tree         the finished search
     * @param subgraphSize the size that was searched for
     * @param source       name of the graph they came from, or null
     * @throws IOException if the file could not be written
     */
    public static void saveResults(Window owner, EsuTree tree,
            int subgraphSize, String source) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save the subgraphs found");
        chooser.setInitialFileName("subgraphs.txt");
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));

        File file = chooser.showSaveDialog(owner);
        if (file == null) {
            return;
        }
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(tree.getSubGraphs().size() + " connected subgraphs of "
                    + "size " + subgraphSize + " in "
                    + (source == null ? "the current graph" : source));
            out.println();
            for (LinkedList<Integer> subgraph : tree.getSubGraphs()) {
                StringBuilder line = new StringBuilder();
                for (Integer vertex : subgraph) {
                    line.append(line.length() == 0 ? "" : " ").append(vertex);
                }
                out.println(line);
            }
        }
    }

    /** The samples directory when it exists, the user's home otherwise. */
    private static File startingDirectory() {
        File samples = new File(SAMPLES);
        return samples.isDirectory()
                ? samples : new File(System.getProperty("user.home"));
    }
}
