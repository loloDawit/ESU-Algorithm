/*
 * The application: one window showing the graph, the search tree, and where
 * the algorithm currently is.
 */
package esu.algorithm.ui;

import esu.algorithm.ESUNode;
import esu.algorithm.EsuSession;
import esu.algorithm.RandomGraph;
import esu.algorithm.StepInfo;
import esu.algorithm.UndirectedGraph;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Class EsuApp
 *
 * One window: the input graph and the step log down the left, the search tree
 * filling the rest, controls along the top and bottom.
 *
 * All state lives in the EsuSession. Every view reads from it, so they cannot
 * disagree about which step is on screen.
 */
public class EsuApp extends Application {

    public static final int MIN_SUBGRAPH_SIZE = 2;
    /** Above this the tree has more boxes than anyone can read. */
    public static final int MAX_SUBGRAPH_SIZE = 6;

    private EsuSession session;
    private File currentFile;
    private int subgraphSize = 4;

    private final Random random = new Random();

    /** Tree drawing state, all derived from the session's finished tree. */
    private TreeLayout layout;
    private Set<String> deadEnds = Set.of();
    /** Which node the picture is explaining: the active one, or a clicked one. */
    private String focusId;

    private final GraphPanel graphPanel = new GraphPanel();
    private final Pane treePane = new Pane();
    private final ScrollPane treeScroll = new ScrollPane();
    private final ListView<String> stepLog = new ListView<>();
    private final Label statusLabel = new Label();
    private final Label subgraphLabel = new Label();
    private final Label extensionLabel = new Label();
    private final ProgressIndicator busy = new ProgressIndicator();

    private final Button openButton = new Button("Open graph");
    private final Button randomButton = new Button("Random");
    private final Button saveButton = new Button("Save results");
    private final Button startButton = new Button("«");
    private final Button prevButton = new Button("‹");
    private final Button nextButton = new Button("›");
    private final Button endButton = new Button("»");
    private final ToggleButton playButton = new ToggleButton("Play");
    private final Button fitButton = new Button("Fit");
    private final Button zoomInButton = new Button("+");
    private final Button zoomOutButton = new Button("−");
    private final Slider zoomSlider = new Slider(0.02, 2, 1);
    private final Slider speedSlider = new Slider(1, 20, 6);
    private final HBox sizePills = new HBox(4);
    private final ToggleGroup sizeGroup = new ToggleGroup();

    private Timeline player;

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(buildLayout(), 1180, 780);
        scene.getStylesheets().add(
                getClass().getResource("esu.css").toExternalForm());

        wireControls();
        showEmptyState();

        stage.setScene(scene);
        stage.setTitle("ESU Visualizer");
        stage.show();
    }

    // ------------------------------------------------------------------
    // layout
    // ------------------------------------------------------------------

    private Parent buildLayout() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setLeft(buildSidePanel());
        root.setCenter(buildTreeArea());
        root.setBottom(buildBottomBar());
        return root;
    }

    private Node buildTopBar() {
        buildSizePills();
        zoomSlider.setPrefWidth(120);

        Label sizeCaption = new Label("Subgraph size");
        sizeCaption.getStyleClass().add("caption");
        Label zoomCaption = new Label("Zoom");
        zoomCaption.getStyleClass().add("caption");

        HBox bar = new HBox(8, openButton, randomButton, saveButton,
                separator(), sizeCaption, sizePills,
                separator(), zoomCaption, zoomOutButton, zoomSlider,
                zoomInButton, fitButton);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(9, 12, 9, 12));
        bar.getStyleClass().add("top-bar");
        return bar;
    }

    /** One toggle per allowed subgraph size, as in the reference design. */
    private void buildSizePills() {
        for (int size = MIN_SUBGRAPH_SIZE; size <= MAX_SUBGRAPH_SIZE; size++) {
            ToggleButton pill = new ToggleButton(Integer.toString(size));
            pill.setUserData(size);
            pill.setToggleGroup(sizeGroup);
            pill.getStyleClass().add("pill");
            pill.setSelected(size == subgraphSize);
            sizePills.getChildren().add(pill);
        }
    }

    private Node buildSidePanel() {
        Label graphTitle = new Label("Input graph");
        graphTitle.getStyleClass().add("panel-title");
        graphPanel.setPrefHeight(280);
        graphPanel.setMinHeight(220);
        graphPanel.getStyleClass().add("graph-panel");

        subgraphLabel.getStyleClass().add("set-chosen");
        extensionLabel.getStyleClass().add("set-candidate");

        VBox sets = new VBox(4,
                labelled("Subgraph", subgraphLabel),
                labelled("Extension", extensionLabel));
        sets.setPadding(new Insets(10, 0, 6, 0));

        Label logTitle = new Label("This step");
        logTitle.getStyleClass().add("panel-title");
        VBox.setVgrow(stepLog, Priority.ALWAYS);

        VBox side = new VBox(6, graphTitle, graphPanel, sets, logTitle, stepLog);
        side.setPadding(new Insets(12));
        side.setPrefWidth(340);
        side.setMinWidth(340);
        side.getStyleClass().add("side-panel");
        return side;
    }

    private Node labelled(String name, Label value) {
        Label caption = new Label(name);
        caption.getStyleClass().add("caption");
        HBox row = new HBox(8, caption, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node buildTreeArea() {
        // A Group reports the scaled size of its content, which is what makes
        // the scroll pane's bars track the zoom.
        treeScroll.setContent(new Group(treePane));
        treeScroll.setPannable(true);
        treeScroll.getStyleClass().add("tree-scroll");

        busy.setMaxSize(48, 48);
        busy.setVisible(false);

        StackPane stack = new StackPane(treeScroll, busy);
        stack.getStyleClass().add("tree-area");
        return stack;
    }

    private Node buildBottomBar() {
        statusLabel.getStyleClass().add("status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label speedCaption = new Label("Speed");
        speedCaption.getStyleClass().add("caption");
        speedSlider.setPrefWidth(110);

        HBox bar = new HBox(8, statusLabel, spacer, startButton, prevButton,
                playButton, nextButton, endButton,
                separator(), speedCaption, speedSlider);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(9, 12, 9, 12));
        bar.getStyleClass().add("bottom-bar");

        VBox bottom = new VBox(buildLegend(), bar);
        return bottom;
    }

    private Node buildLegend() {
        HBox legend = new HBox(16,
                swatch(TreeRenderer.ACTIVE_FILL, TreeRenderer.ACTIVE_STROKE,
                        "working on", false),
                swatch(TreeRenderer.COMPLETE_FILL, TreeRenderer.COMPLETE_STROKE,
                        "subgraph found", false),
                swatch(TreeRenderer.DEADEND_FILL, TreeRenderer.DEADEND_STROKE,
                        "dead end", true),
                swatch(TreeRenderer.PENDING_FILL, TreeRenderer.PENDING_STROKE,
                        "still expanding", false),
                separator(),
                caption("each box is a subgraph being built  \u00b7  the line down to it is how it got there"));
        legend.setAlignment(Pos.CENTER_LEFT);
        legend.setPadding(new Insets(6, 12, 6, 12));
        legend.getStyleClass().add("legend-bar");
        return legend;
    }

    private Node swatch(javafx.scene.paint.Color fill,
            javafx.scene.paint.Color stroke, String text, boolean dashed) {
        Rectangle box = new Rectangle(15, 11, fill);
        box.setStroke(stroke);
        if (dashed) {
            box.getStrokeDashArray().addAll(3.0, 2.0);
        }
        HBox entry = new HBox(6, box, caption(text));
        entry.setAlignment(Pos.CENTER_LEFT);
        return entry;
    }

    private Label caption(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("caption");
        return label;
    }

    private Node separator() {
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setPrefHeight(18);
        return separator;
    }

    // ------------------------------------------------------------------
    // wiring
    // ------------------------------------------------------------------

    private void wireControls() {
        openButton.setOnAction(event -> chooseFile());
        randomButton.setOnAction(event -> generateRandomGraph());
        randomButton.setTooltip(new Tooltip(
                "Generate a random connected graph and run ESU on it"));
        saveButton.setOnAction(event -> saveResults());
        saveButton.setTooltip(new Tooltip(
                "Write the subgraphs found to a text file"));

        startButton.setTooltip(new Tooltip("Back to the first step"));
        endButton.setTooltip(new Tooltip("Jump to the finished tree"));
        fitButton.setTooltip(new Tooltip("Zoom so the whole tree fits"));

        startButton.setOnAction(event -> goTo(0));
        endButton.setOnAction(event -> goTo(session.getTotalSteps()));
        prevButton.setOnAction(event -> {
            session.stepBack();
            refresh();
        });
        nextButton.setOnAction(event -> {
            session.stepForward();
            refresh();
        });

        playButton.setOnAction(event -> {
            if (playButton.isSelected()) {
                startPlayback();
            } else {
                stopPlayback();
            }
        });
        speedSlider.valueProperty().addListener((obs, was, now) -> {
            if (player != null) {
                startPlayback();
            }
        });

        zoomSlider.valueProperty().addListener((obs, was, now) -> {
            treePane.setScaleX(now.doubleValue());
            treePane.setScaleY(now.doubleValue());
        });
        zoomInButton.setOnAction(event -> nudgeZoom(0.15));
        zoomOutButton.setOnAction(event -> nudgeZoom(-0.15));
        fitButton.setOnAction(event -> fitToWindow());

        sizeGroup.selectedToggleProperty().addListener((obs, was, now) -> {
            if (now == null) {
                was.setSelected(true);   // never leave the group empty
                return;
            }
            int chosen = (Integer) now.getUserData();
            if (chosen != subgraphSize) {
                subgraphSize = chosen;
                if (currentFile != null) {
                    loadGraph(currentFile);
                }
            }
        });

        stepLog.getSelectionModel().selectedIndexProperty()
                .addListener((obs, was, now) -> highlightFromLog(now.intValue()));

        // Entries are sentences, so wrap them rather than clipping to one
        // line behind a horizontal scrollbar.
        stepLog.setCellFactory(view -> new ListCell<>() {
            private final Label text = new Label();
            {
                text.setWrapText(true);
                text.getStyleClass().add("log-line");
                text.maxWidthProperty().bind(
                        stepLog.widthProperty().subtract(34));
                setGraphic(text);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                text.setText(empty ? "" : item);
                setGraphic(empty ? null : text);
            }
        });
    }

    // ------------------------------------------------------------------
    // loading
    // ------------------------------------------------------------------

    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open a graph file");
        File samples = new File("samples");
        chooser.setInitialDirectory(samples.isDirectory()
                ? samples : new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Graph files (*.txt)", "*.txt"));

        File file = chooser.showOpenDialog(openButton.getScene().getWindow());
        if (file != null) {
            loadGraph(file);
        }
    }

    private void generateRandomGraph() {
        try {
            File file = new File("samples/random-graph.txt");
            file.getParentFile().mkdirs();
            RandomGraph.writeToFile(RandomGraph.generate(random), file);
            loadGraph(file);
        } catch (IOException e) {
            Alerts.displayCouldNotWrite(e.getMessage());
        }
    }

    /**
     * Write the subgraphs found to a file the user picks.
     */
    private void saveResults() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save the subgraphs found");
        chooser.setInitialFileName("subgraphs.txt");
        chooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));

        File file = chooser.showSaveDialog(saveButton.getScene().getWindow());
        if (file == null) {
            return;
        }
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(session.getSubgraphCount() + " connected subgraphs of "
                    + "size " + session.getSubgraphSize()
                    + " in " + (currentFile == null
                            ? "the current graph" : currentFile.getName()));
            out.println();
            for (LinkedList<Integer> subgraph
                    : session.getFinalTree().getSubGraphs()) {
                StringBuilder line = new StringBuilder();
                for (Integer vertex : subgraph) {
                    line.append(line.length() == 0 ? "" : " ").append(vertex);
                }
                out.println(line);
            }
        } catch (IOException e) {
            Alerts.displayCouldNotWrite(e.getMessage());
        }
    }

    /**
     * Build a session for a file, off the UI thread.
     *
     * Enumerating is fast on the graphs this draws, but it is unbounded work
     * on someone else's file, and it used to run on the JavaFX thread and
     * freeze the window with no way out.
     *
     * @param file the graph file to load
     */
    private void loadGraph(File file) {
        UndirectedGraph graph = UndirectedGraph.fromFile(file);
        if (graph == null) {
            Alerts.displayUnreadableFile(file.getName());
            return;
        }

        stopPlayback();
        setBusy(true);
        currentFile = file;

        Task<EsuSession> work = new Task<>() {
            @Override
            protected EsuSession call() {
                return new EsuSession(graph, subgraphSize);
            }
        };
        work.setOnSucceeded(event -> {
            setBusy(false);
            adopt(work.getValue(), file);
        });
        work.setOnFailed(event -> {
            setBusy(false);
            Alerts.displayCouldNotRun(work.getException());
        });

        Thread thread = new Thread(work, "esu-enumerate");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Take on a freshly built session and draw it.
     *
     * @param built the session that finished enumerating
     * @param file  the file it came from
     */
    private void adopt(EsuSession built, File file) {
        session = built;
        layout = TreeLayout.of(session.getFinalTree());
        deadEnds = findDeadEnds(session.getFinalTree());

        graphPanel.setGraph(session.getGraph());
        setControlsEnabled(true);
        goTo(0);
        javafx.application.Platform.runLater(this::fitToWindow);

        if (session.getSubgraphCount() == 0) {
            Alerts.displayNoSubgraphs(subgraphSize);
        }
    }

    // ------------------------------------------------------------------
    // display
    // ------------------------------------------------------------------

    private void goTo(int step) {
        session.goToStep(step);
        refresh();
    }

    /** Redraw everything from the session. The single place that does. */
    private void refresh() {
        if (session == null) {
            return;
        }
        List<StepInfo> log = session.getCurrentLog();
        String active = log.isEmpty()
                ? null : log.get(log.size() - 1).getCallerSubgraph();
        focusId = active;
        drawTree(active, active);

        graphPanel.highlight(session.getActiveSubgraph(),
                session.getActiveExtension());
        subgraphLabel.setText(setText(session.getActiveSubgraph()));
        extensionLabel.setText(setText(session.getActiveExtension()));

        stepLog.getItems().clear();
        for (StepInfo entry : log) {
            stepLog.getItems().add(entry.render());
        }

        updateStatus();
        updateButtons();
    }

    private String setText(List<Integer> vertices) {
        if (vertices.isEmpty()) {
            return "—";
        }
        StringBuilder out = new StringBuilder();
        for (Integer vertex : vertices) {
            out.append(out.length() == 0 ? "" : ", ").append(vertex);
        }
        return out.toString();
    }

    private void updateStatus() {
        if (session == null) {
            statusLabel.setText("Open a graph, or press Random to generate one");
            return;
        }
        int found = session.getSubgraphCount();
        statusLabel.setText("Step " + session.getCurrentStep()
                + " of " + session.getTotalSteps()
                + "     ·     " + found + " subgraph"
                + (found == 1 ? "" : "s") + " of size " + subgraphSize);
    }

    private void updateButtons() {
        boolean atStart = session.getCurrentStep() <= 0;
        boolean atEnd = session.getCurrentStep() >= session.getTotalSteps();
        startButton.setDisable(atStart);
        prevButton.setDisable(atStart);
        nextButton.setDisable(atEnd);
        endButton.setDisable(atEnd);
    }

    /**
     * Draw the tree as it stands, tracing the path down to one node.
     *
     * @param activeId the node being worked on, highlighted amber
     * @param traceId  the node to trace back to the root, or null
     */
    private void drawTree(String activeId, String traceId) {
        Set<String> present = presentNodes();
        Set<String> path = traceId == null
                ? Set.of() : new HashSet<>(layout.pathToRoot(traceId));
        treePane.getChildren().setAll(TreeRenderer.render(layout, present,
                deadEnds, session.getSubgraphSize(), activeId, path));
    }

    /** Ids of the nodes that exist at the step being shown. */
    private Set<String> presentNodes() {
        Set<String> present = new HashSet<>();
        ArrayList<ESUNode>[] levels =
                session.getCurrentTree().getNodesByLevel();
        for (int level = 1; level < levels.length; level++) {
            for (ESUNode node : levels[level]) {
                present.add(node.getSubgraphAsString());
            }
        }
        return present;
    }

    /**
     * Nodes that never gained a child in the finished tree, which is what
     * makes them dead ends. Asking the current tree would be wrong: mid-run a
     * node has no children only because it has not expanded yet.
     *
     * @param finalTree the finished tree
     * @return ids of the branches that died
     */
    private Set<String> findDeadEnds(esu.algorithm.ESUTree finalTree) {
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

    private void highlightFromLog(int index) {
        List<StepInfo> log = session == null
                ? List.of() : session.getCurrentLog();
        if (index < 0 || index >= log.size() || layout == null) {
            return;
        }
        drawTree(focusId, log.get(index).getCallerSubgraph());
    }

    private void showEmptyState() {
        setControlsEnabled(false);
        updateStatus();
    }

    private void setControlsEnabled(boolean loaded) {
        for (Node control : new Node[]{startButton, prevButton, nextButton,
                endButton, playButton, speedSlider, fitButton, zoomInButton,
                zoomOutButton, zoomSlider, saveButton}) {
            control.setDisable(!loaded);
        }
    }

    private void setBusy(boolean running) {
        busy.setVisible(running);
        openButton.setDisable(running);
        randomButton.setDisable(running);
        sizePills.setDisable(running);
        if (running) {
            setControlsEnabled(false);
        }
    }

    // ------------------------------------------------------------------
    // zoom and playback
    // ------------------------------------------------------------------

    private void nudgeZoom(double delta) {
        zoomSlider.setValue(clamp(zoomSlider.getValue() + delta,
                zoomSlider.getMin(), zoomSlider.getMax()));
    }

    /** Zoom so the whole tree is visible, and scroll back to the top left. */
    private void fitToWindow() {
        if (layout == null) {
            return;
        }
        Bounds view = treeScroll.getViewportBounds();
        if (layout.getWidth() <= 0 || view.getWidth() <= 0) {
            return;
        }
        double factor = Math.min(view.getWidth() / layout.getWidth(),
                view.getHeight() / layout.getHeight()) * 0.92;
        zoomSlider.setValue(clamp(factor,
                zoomSlider.getMin(), zoomSlider.getMax()));
        treeScroll.setHvalue(0);
        treeScroll.setVvalue(0);
    }

    private double clamp(double value, double low, double high) {
        return Math.max(low, Math.min(high, value));
    }

    private void startPlayback() {
        stopPlayback();
        if (session == null) {
            playButton.setSelected(false);
            return;
        }
        Duration tick = Duration.seconds(1.0 / speedSlider.getValue());
        player = new Timeline(new KeyFrame(tick, event -> {
            if (!session.stepForward()) {
                stopPlayback();
                playButton.setSelected(false);
                return;
            }
            refresh();
        }));
        player.setCycleCount(Animation.INDEFINITE);
        player.play();
        playButton.setText("Pause");
    }

    private void stopPlayback() {
        if (player != null) {
            player.stop();
            player = null;
        }
        playButton.setText("Play");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
